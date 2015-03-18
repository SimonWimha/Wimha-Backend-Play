package controllers.flash;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.authentication.Helper;
import controllers.authentication.WimhaSecured;
import controllers.util.LoggedMode;
import external_services.*;
import models.PendingImageUpload;
import models.Picture;
import models.User;
import models.comment.FollowedThread;
import models.comment.Thread;
import models.totem.Member;
import models.totem.Tribu;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.data.Form;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

import java.util.Date;
import java.util.UUID;

import static play.data.Form.form;

public class Flash extends Controller {

    private static final String KEY_LOCK_FLASH_WRITING = "writing flash";

    /**
     * Handles the submission of message form
     * @return [description]
     */
    public static Result submit() {
        User user = WimhaSecured.getCurrentUser(ctx());

		final Form<models.totem.Flash> filledForm = form(models.totem.Flash.class).bindFromRequest();

        //Find totem we're posting on with its name (from form)
        final String totemName=filledForm.data().get("totem_name");
    	final Tribu tribu = Tribu.findByName(totemName);
    	if(tribu ==null){
    		Logger.error("[Submit] tribu not found for post : "+filledForm.data().get("totem_name"));
    		return badRequest();
    	}
        Logger.info("[Submit] Flash tribu " + totemName);


        //Check form inputs (empty msg, name, email, lat/lng, etc..)
		if (filledForm.hasErrors()) {
			ObjectNode error = Json.newObject();
			error.put("error", filledForm.errorsAsJson());
            Logger.info("[Submit] Form errors :", error.toString());
			return ok(error);
		} else {

            if(Cache.get(KEY_LOCK_FLASH_WRITING+filledForm.data().get("mail"))!=null){
                Logger.error("[Submit] already flashing "+filledForm.data().get("mail"));
                return internalServerError();
            }

            try {

                //Verify this flash hasnt already been handled (requests can come twice from client)
                models.totem.Flash sameFlash = models.totem.Flash.findSame(filledForm.get().mail, filledForm.get().message, tribu);
                if (sameFlash != null) {
                    Logger.info("[Submit] flash already exists" + sameFlash.id);

                    //Sending json result to javascript
                    ObjectNode jsonResult = Json.newObject();

                    jsonResult.put("postDate", sameFlash.getDate());
                    jsonResult.put("timestamp", sameFlash.date_post + "");

                    //Resend signature for picture upload if this has failed on side mobile app
                    insertJSONPictureFields(sameFlash, filledForm, jsonResult);

                    return ok(jsonResult);
                }

                //Build flash object with Play mecanism
                final models.totem.Flash flash = filledForm.get();

                //from here, another dyno can't write another flash from the same flasher
                Cache.set(KEY_LOCK_FLASH_WRITING + flash.mail, "");

                //Initialize other fields manually
                flash.setTribu(tribu);

                //Fill member id with totemid eventually
                String member_id = filledForm.data().get("member_id");
                if (StringUtils.isBlank(member_id)) {
                    String totemId = filledForm.data().get("totem.id");
                    if (Member.findById(totemId) != null) {
                        member_id = totemId;
                        Logger.info("[Submit] Empty member_id but totem.id is the member");
                    } else {
                        Logger.info("[Submit] Empty member_id and totem.id is not a member");
                    }
                }

                if (StringUtils.isNotEmpty(member_id)) {
                    flash.member = Member.findById(member_id);  //can be null if tribe has no member
                } else {
                    Logger.error("[Submit] Empty member_id for setting flash member");
                }
                flash.token = UUID.randomUUID();
                String timestamp = filledForm.data().get("date");
                if (timestamp != null) {
                    flash.date_post = timestamp;
                } else {
                    flash.date_post = new Date().getTime() + "";
                }
                if (flash.question == null) {
                    flash.question = tribu.question;
                }

                flash.save();

                //Remove any cached flash count, to actualize myTP
                Cache.remove(models.totem.Flash.flashCountCacheKey(tribu));

                //release lock for the flasher to  submit other flashes
                Cache.remove(KEY_LOCK_FLASH_WRITING + flash.mail);

                //Result returned at the end. Will aggregate infos following
                ObjectNode jsonResult = Json.newObject();

                //Fill response with generated signature for mobile picture upload
                PendingImageUpload pendingImageUpload = insertJSONPictureFields(flash, filledForm, jsonResult);

                // Collecting infos of logged mode
                final LoggedMode loggedMode = new LoggedMode(filledForm, user);

                User.CreationUserResult infos = User.getOrCreateUser(
                        flash.getName(),
                        flash.getMail(),
                        "lsjf5jd8hdkqz1",
                        false);

                flash.flasher = infos.user;
                if (StringUtils.isNotBlank(loggedMode.id_fb)) {
                    flash.flasher.idFb = loggedMode.id_fb;
                }

                //subscribe flasher to thread
                final Thread thread = flash.getTribu().getThread();
                if (FollowedThread.isSubscribe(flash.flasher, thread)) {
                    FollowedThread.updateLastSee(flash.flasher, thread);
                } else {
                    FollowedThread.subscribeUser(flash.flasher, thread);
                }

                //Associate QR code to the flasher if not already done
                if (StringUtils.isNotEmpty(member_id)) {
                    Member.fill(member_id, flash);
                } else {
                    Logger.error("[Submit] Empty member_id for filling member");
                }

                //In parallel : lat/lng geocoding
                F.Promise<models.totem.Flash> promiseFlashGeocoded = F.Promise.promise(
                        () -> GoogleApi.geocoding(flash, filledForm.data().get("address"), filledForm.data().get("city"), filledForm.data().get("country"), filledForm.data().get("country_code"))
                );

                //Get results, store social posts, and store confirmation mail, waiting for mobile picture upload.
                promiseFlashGeocoded.map(flashGeocoded -> {

                    Twitter twitter = new Twitter(flash, filledForm);

                    //Store variables to use them in both case :
                    // if direct post (not wait for image upload)
                    // if pst must be stored in db waiting for image upload.
                    Facebook facebookPost = new Facebook(flash, filledForm);

                    //If flash has no picture to upload, we can validate now.
                    if (pendingImageUpload == null) {

                        sendValidationMail(loggedMode.logged, flash);
                        if (loggedMode.postTw) {
                            twitter.tweet();
                        }
                        if (loggedMode.postFb) {
                            facebookPost.postFlasher();
                        }
                        Facebook.postOwner(flash);

                        setFlasherPic(flash, loggedMode);

                    } else {

                        pendingImageUpload.storeValidationMail(loggedMode.logged, flash.mail);
                        if (loggedMode.postTw) {
                            pendingImageUpload.storeTweet(twitter.tweet, twitter.twitterAccessToken, twitter.twitterAccessTokenSecret, twitter.twitterConsumerKey, twitter.twitterConsumerSecret, twitter.picTweet);
                        }
                        if (loggedMode.postFb) {
                            pendingImageUpload.storeFBPost(facebookPost.message, facebookPost.flash.flasher);
                        }
                    }


                    return null;
                }
                ).get(10000);

                jsonResult.put("postDate", flash.getDate());
                jsonResult.put("timestamp", flash.date_post + "");

                Logger.info("[Submit] response submit sent");

                return ok(jsonResult);
            }catch(Exception e){
                //release lock for the flasher to  submit other flashes
                Cache.remove(KEY_LOCK_FLASH_WRITING+filledForm.data().get("mail"));
            }
            return internalServerError();
        }
    }

    /**
     * Filling user wimha picture with : facebook pic if fbc,twitter pic if twitter connect, or flash pic.
     * @param flash
     * @param loggedMode
     */
    private static void setFlasherPic(models.totem.Flash flash, LoggedMode loggedMode) {
        User flasher=flash.getFlasher();
        if(flasher!=null && flasher.picto==null){
            Picture flasherPic = null;
            Logger.info("[Submit] setting flsher pic");

            //loggedMode null if coming from PendingImageUpload
            if(loggedMode!=null) {
                //Set facebook picture if user facebook connect in app
                //Else : set flash picture if there is one
                if (StringUtils.isNotEmpty(loggedMode.id_fb)) {
                    Logger.info("[Submit] from fb");
                    flasherPic = Picture.save(Picture.fetchFacebookPictureFromUser(flasher, loggedMode.id_fb), "image/jpeg");
                } else if (loggedMode.twitter_consumer_key != null) {
                    Logger.info("[Submit] from tw");
                    flasherPic = Picture.fetchPictureFromUrl(loggedMode.pic_url);
                }
            }else{
                Logger.info("[Submit] from flash from cloudinary url");
                flasherPic=flash.picture;
            }
            if(flasherPic!=null) {
                Logger.info("[Submit] pic retrieved");
                flasher.picto = flasherPic;
                flasher.save();
            }
        }
    }

    /**
     * Fill response with generated signature for mobile picture upload
     * @param flash
     * @param filledForm
     * @param jsonResult
     * @return
     */
    private static PendingImageUpload insertJSONPictureFields(models.totem.Flash flash, Form<models.totem.Flash> filledForm, ObjectNode jsonResult) {
        if(flash.picture!=null || filledForm.data().containsKey("need_picture_id") && "true".equals(filledForm.data().get("need_picture_id"))){
            Picture flashPic=null;
            if(flash.picture!=null){
                flashPic=flash.picture;
            }else {
                flashPic=new Picture();
                flashPic.save();
                flash.picture=flashPic;
                flash.save();
            }
            String newId=flashPic.getId()+"";
            Logger.info("[Submit] existing picture id : " + newId);
            jsonResult.put("new_picture_id", newId);
            String signature=Picture.generateSignature(newId, flash.date_post);

            jsonResult.put("cloudinary_signature", signature);
            jsonResult.put("cloudinary_api_key", Picture.cloudinaryApiKey);
            return PendingImageUpload.put(signature,flash, flashPic);
        }
        return null;
    }

    /**
     * Send the final mail and validate flash if logged
     * @param logged
     * @param flash
     */
    private static void sendValidationMail(Boolean logged, models.totem.Flash flash) {
        // Don't ask email validation if user logged
        if (logged != null && logged) {
            Logger.info("[Submit] logged auto validated" + flash.id);
            flash.token=null;
            flash.save();
            
            MailService.loggedFlashValidation(flash);
            Parse.newFlash(flash);
        } else {
            Logger.info("[Submit] sending validation mail for flash " + flash.id);
            MailService.flashValidation(flash);
        }
    }

    /**
     * Handle the second request after flash submit, when a flash has a picture
     * @param signature
     * @param version
     * @param contentType
     * @return
     */
    public static Result updateVersionPicture(String signature, String version, String contentType) {
        PendingImageUpload infos=PendingImageUpload.get(signature);
        Picture picture=infos.picture;
        if(picture!=null && (infos.ok==null || !infos.ok)) {
            picture.setVersion(version);
            picture.setContentType(contentType);
            picture.save();
            PendingImageUpload.markOk(signature);

            //Send picture to cloudinary then send notifications.
            F.Promise.promise(
                    new F.Function0<Void>() {
                        public Void apply() {

                            if (infos.email!=null) {
                                sendValidationMail(infos.logged, infos.getFlash());
                                try {
                                    setFlasherPic(infos.getFlash(), null);
                                }catch(Exception e){
                                    Logger.error("[Submit] Error setting flasher pic", e);
                                }
                            }

                            return null;
                        }
                    }
            ).map(new F.Function<Void, Object>() {
                @Override
                public Object apply(Void aVoid) throws Throwable {
                    if (infos.fbPoster!=null) {
                        Facebook.postFlashFacebook(infos.flash, infos.messageFacebookPost, infos.fbPoster);
                    }
                    Facebook.postOwner(infos.flash);

                    if (infos.tweet!=null) {
                        Twitter.callApi(infos.tweet,
                                infos.twitterConsumerKey,
                                infos.twitterConsumerSecret,
                                infos.twitterAccessToken,
                                infos.twitterAccessTokenSecret,
                                infos.imageTweet);
                    }

                    return null;
                }
            });
        }
        Logger.info("[Submit] response version sent");
        return ok();
    }


    /**
     * Handles the validation link click in the mail
     * @param id
     * @param token
     * @return
     */
    public static Result validateToken(final String id, final String token){
        final models.totem.Flash flash = models.totem.Flash.findById(id);

        if(flash ==null){
            Logger.error("[Submit] Error validate with id:"+id+" and token: "+token);
            return badRequest(views.html.error_pages.maintenance.render(null));
        }else if(flash.token==null){
            Helper.addSession(flash.flasher);
            Logger.info("[Submit] Already validated message with id:"+id);
            return redirect(controllers.routes.Application.myTotemPage(flash.tribu.name));
        }else{
            flash.token=null;
            flash.save();
            Helper.addSession(flash.flasher);
            flash("validated","flash");
            return redirect(controllers.routes.Application.myTotemPage(flash.tribu.name));

        }

    }

    /**
     * Handles the validation link click in the mail
     * @param id
     * @return
     */
    public static Result validateNullToken(final String id){
        final models.totem.Flash flash = models.totem.Flash.findById(id);

        if(flash ==null){
            Logger.error("[Submit] Error validate with id:"+id+" no token ");
            return badRequest(views.html.error_pages.maintenance.render(null));
        }
        return redirect(controllers.routes.Application.myTotemPage(flash.tribu.name));


    }

}
