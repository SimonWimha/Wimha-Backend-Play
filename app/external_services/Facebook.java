package external_services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import controllers.authentication.WimhaSecured;
import models.notification.NotificationPreference;
import models.User;
import models.comment.Comment;
import models.comment.FollowedThread;
import models.comment.Thread;
import models.totem.Flash;
import models.totem.Tribu;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Facebook extends Controller {

    //restFB service to call to make graph api requests
    static String appId=play.Play.application().configuration().getString("facebook.apiKey");
    static String appSecret=play.Play.application().configuration().getString("facebook.secretKey");
	private static FacebookClient defaultFacebookClient = new DefaultFacebookClient(appId+"|"+appSecret);
    public final String message;

    public Flash flash;


    public Facebook(Flash flash, Form<Flash> filledForm){

        // If flasher is owner, avoid fb post twice on his wall (block flasher's post)
        this.flash = flash;
        this.message = flash.message + " - " + Messages.get("smallWords.on") + " " + flash.tribu.name + " ! - #TOTEM WIMHA #"+ flash.tribu.name+" - https://www.wimha.com/myTotemPage/"+ flash.tribu.name;

    }

    public void postFlasher() {
        boolean willBePostedTwice = flash.flasher != null && flash.flasher.equals(flash.tribu.owner) && flash.tribu.post_fb!=null && flash.tribu.post_fb;

        //Post flasher
        if(!willBePostedTwice){
            postFlashFacebook(flash, message, flash.flasher);
        }
    }


    public static void postOwner(Flash flash) {
        //post owner
        User owner = flash.tribu.owner;
        if (flash.tribu.post_fb != null && flash.tribu.post_fb && owner.idFb != null) {
            String message = Messages.get("facebook.post.owner") + " " + flash.tribu.name + " ! "
                    + flash.flasher.firstname + " : " + flash.message + " - #TOTEM WIMHA #"+ flash.tribu.name+"   https://www.wimha.com/myTotemPage/"
                    + flash.tribu.name;
            Facebook.postFlashFacebook(flash, message, owner);
        }
    }

    public static void postFlashFacebook(Flash flash, String message, User poster){

        FacebookClient facebookClient = poster.facebookToken!=null ? new DefaultFacebookClient(poster.facebookToken) : defaultFacebookClient;

        if(flash.picture!=null){
            facebookClient
                    .publish(poster.idFb + "/feed",
                            FacebookType.class,
                            Parameter.with("message", message),
                            Parameter.with("link", "https://www.wimha.com/myTotemPage/" + flash.tribu.name),
                            Parameter.with("name", flash.tribu.question),
                            Parameter.with("picture", flash.picture.imageWithTotem(flash.tribu)));
        }else{
            facebookClient
                    .publish(poster.idFb + "/feed",
                            FacebookType.class,
                            Parameter.with("message", message),
                            Parameter.with("link", "https://www.wimha.com/myTotemPage/" + flash.tribu.name),
                            Parameter.with("name", flash.tribu.question),
                            Parameter.with("picture", flash.tribu.picture.imageWithBG()));
        }

    }

    public static void sendNotifToUser(User user, String text, String url){

        try {
            defaultFacebookClient.publish(user.idFb + "/notifications", FacebookType.class,
                    Parameter.with("template", text),
                    Parameter.with("href", "?url=" + url));
        } catch (FacebookOAuthException e) {

            //200 = the user hasnt authorized .. dont care about those errors
            if (e.getErrorCode() != 200) {
                Logger.error("Wrong response code for fb notif : " + user.idFb + " " + text, e);
            }
        } catch (Exception e) {
            Logger.error("Exception fb notif " + user.idFb + " " + text, e);
        }
    }

    /**
     * Send fb notification + mail to all people susbscribed to comment thread
     * @param sender
     * @param comment
     */
	public static void sendFbCommentFlashNotification(User sender, Comment comment){
			Flash flash= comment.getThread().flash;
			Tribu tribu = flash.getTribu();

			List<FollowedThread> fThreads = FollowedThread.getSubscribedUserByThread(comment.getThread());

			String template = sender.firstname +" "+ "left a comment : " +"\""+ comment.getMessage() +"\".";
            stripAccents(template);
            template = StringUtils.abbreviate(template,170);
            template+="...";

			//for each subscribed user
			for(FollowedThread fThread : fThreads){
				User receiver = fThread.getUser();

				//only if totem owner and accepted notif or anyone subscribed or only if flasher and accepted notif or anyone subscribed
				if(!sender.equals(receiver) && (receiver.equals(tribu.getOwner()) && NotificationPreference.isSubscribed(receiver, NotificationPreference.commentOneOfTheMessagesOnMyTotem)

				|| receiver.equals(flash.getFlasher()) && NotificationPreference.isSubscribed(receiver, NotificationPreference.commentMyFlash)

				|| NotificationPreference.isSubscribed(receiver, NotificationPreference.commentAFlashIHaveCommented))){

					if(receiver.idFb != null){
						try{
							Logger.info("Sending fb notification to :"+receiver.idFb + " for totem "+ tribu.name);
                            defaultFacebookClient.publish(receiver.idFb+"/notifications", FacebookType.class,
							Parameter.with("template", template), 
							Parameter.with("href", "?url="+"/commentPage/"+comment.getThread().flash.id+"%23"+comment.getId()));
							}catch(FacebookOAuthException e){

                                //200 = the user hasnt authorized .. dont care about those errors
                                if(e.getErrorCode()!=200){
                                    Logger.error("Wrong response status of FB notif to "+receiver.idFb + " " + template, e);
                                }
							} catch(Exception e){
                                Logger.error("Exception FB notif to " + receiver.idFb, e);
							}
					}

					if(receiver.email!=null && !receiver.email.isEmpty()){
                        Logger.info("Sending mail notification to :"+receiver.email + " for totem "+ tribu.name);
                        MailService.commentFlash(fThread, comment, comment.getThread().flash, tribu);
                    }

				}

			}
	}

    /**
     * Send fb notiifcation and mail about new like to totem owner
     * @param liker
     * @param tribu
     */
	public static void sendLikeTotemNotification(final User liker, final Tribu tribu){
			String template = liker.firstname +" liked your totem \""+ tribu.name +"\".";
            stripAccents(template);
            template = StringUtils.abbreviate(template,170);
            template+="...";

			if(!liker.equals(tribu.getOwner())){
				if(tribu.getOwner().idFb != null){
					try{
						Logger.info("Sending fb notification to :"+ tribu.getOwner().idFb+ " for totem "+ tribu.name);
                        defaultFacebookClient.publish(tribu.getOwner().idFb+"/notifications", FacebookType.class,
						Parameter.with("template", template), 
						Parameter.with("href", "?url="+"/myTotemPage/"+ tribu.name));
						}catch(FacebookOAuthException e){

                            //200 = the user hasnt authorized .. dont care about those errors
                            if(e.getErrorCode()!=200){
                                Logger.error("Wrong response status of FB notif to "+ tribu.getOwner().idFb + " " + template, e);
                            }
						} catch(Exception e){
                            Logger.error("Exception FB notif to " + tribu.getOwner().idFb, e);
						}
				}
                if(tribu.getOwner().email!=null && !tribu.getOwner().email.isEmpty()){
                    Logger.info("Sending mail notification to :"+ tribu.getOwner().email + " for totem "+ tribu.name);
                    MailService.newLikeTotem(liker, tribu);
                }
			}
	}

    /**
     * Send fb notiifcation and mail about new like to the flasher
     * @param liker
     * @param tribu
     */
	public static void sendLikeFlashNotification(final User liker, final Flash flash, final Tribu tribu){
			String template = liker.firstname + " liked your message on the totem : " + "\"" + tribu.name +"\".";
            stripAccents(template);
            template = StringUtils.abbreviate(template,170);
            template+="...";

        if(!liker.equals(flash.getFlasher()) && flash.getFlasher().idFb != null){
				try{
					Logger.info("Sending fb notification to : "+ flash.getFlasher().idFb + " for totem "+ tribu.name);
                    defaultFacebookClient.publish(flash.getFlasher().idFb+"/notifications", FacebookType.class,
					Parameter.with("template", template), 
					Parameter.with("href", "?url="+"/myTotemPage/"+ tribu.name+"%23"+ flash.date_post));
                }catch(FacebookOAuthException e){

                    //200 = the user hasnt authorized .. dont care about those errors
                    if(e.getErrorCode()!=200){
                        Logger.error("Wrong response status of FB notif to "+ flash.getFlasher().idFb + " " + template, e);
                    }
                } catch(Exception e){
                    //Logger.error("Exception FB notif to " + position.getFlasher().getIdFb(), e);
                }
			}
            if(flash.getFlasher().email!=null && !flash.getFlasher().email.isEmpty()){
                Logger.info("Sending mail notification to :"+ flash.getFlasher().email + " for totem "+ tribu.name);
                MailService.newLikeFlash(liker, flash);
            }
	}

    /**
     * Send fb notiifcation and mail about new like to the flasher
     * @param liker
     * @param tribu
     * @param data
     */
    public static void sendFavoritedFlashNotification(final User liker, final Flash flash, final Tribu tribu, String data){
        String template = Messages.get("email.favorite.1") + tribu.name + Messages.get("email.favorite.2");
        stripAccents(template);
        template = StringUtils.abbreviate(template,170);
        template+="...";

        if(!liker.equals(flash.getFlasher()) && flash.getFlasher().idFb != null){
            try{
                Logger.info("Sending fb notification to : "+ flash.getFlasher().idFb + " for totem "+ tribu.name);
                defaultFacebookClient.publish(flash.getFlasher().idFb+"/notifications", FacebookType.class,
                        Parameter.with("template", template),
                        Parameter.with("href", "?url="+"/myTotemPage/"+ tribu.name+"%23"+ flash.date_post));
            }catch(FacebookOAuthException e){

                //200 = the user hasnt authorized .. dont care about those errors
                if(e.getErrorCode()!=200){
                    Logger.error("Wrong response status of FB notif to "+ flash.getFlasher().idFb + " " + template, e);
                }
            } catch(Exception e){
                //Logger.error("Exception FB notif to " + position.getFlasher().getIdFb(), e);
            }
        }
        if(flash.getFlasher().email!=null && !flash.getFlasher().email.isEmpty()){
            Logger.info("Sending mail notification to :"+ flash.getFlasher().email + " for totem "+ tribu.name);
            MailService.markedFavorite(flash, data);
        }
    }
    
	public static Result getPictureFb(final String idFb) {
		ObjectNode json = Json.newObject();
    	User user = WimhaSecured.getCurrentUser(ctx());

    	if (user.picto == null) {
	    	fetchFacebookPictureFromUser(user,idFb);
    	}

    	return ok(json);
	}


	public static void fetchFacebookPictureFromUser(final User user, final String idFb) {
		if (user != null && idFb != null) {
            Logger.info(user.id + " fetching fb picture with fb id : " + idFb);
	    	final String imgPath = "https://graph.facebook.com/" + idFb + "/picture?width=500";

            F.Promise.promise(
                    new F.Function0<Void>() {
                        public Void apply() {
                            try {
                                File file=new File("tmp/fbpicture.jpg");
                                FileUtils.copyURLToFile(new URL(imgPath), file);
                                String contentType = new javax.activation.MimetypesFileTypeMap().getContentType(file);
                                user.setPicture(file, "image/jpeg");
                                user.save();
                            } catch (IOException e) {
                                Logger.info(e.getMessage());
                            }
                            return null;
                        }
                    }
            );
		}

	}

    public static Result firstActivatePostOnFB(String totemId, String id){
        play.Logger.info("activating facebook post for totem "+totemId);
        User user= WimhaSecured.getCurrentUser(ctx());
        Tribu tribu = Tribu.findById(totemId);
        if(user!=null && user.equals(tribu.owner)){
            tribu.post_fb=true;
            tribu.save();

            if(user.idFb==null){
                user.idFb = id;
                user.save();
            }
            if(user.picto==null){
                fetchFacebookPictureFromUser(user,id);
            }
        }

        return ok();

    }

    public static Result activatePostOnFB(String totemId){
        play.Logger.info("activating facebook post for totem "+totemId);
        User user= WimhaSecured.getCurrentUser(ctx());
        Tribu tribu = Tribu.findById(totemId);
        if(user!=null && user.equals(tribu.owner)){
            tribu.post_fb=true;
            tribu.save();
        }
        return ok();

    }

    public static Result deactivatePostOnFB(String totemId){
        play.Logger.info("deactivating facebook post for totem "+totemId);
        User user= WimhaSecured.getCurrentUser(ctx());
        Tribu tribu = Tribu.findById(totemId);
       if(user!=null && user.equals(tribu.owner)){
            tribu.post_fb=false;
            tribu.save();
        }
        return ok();

    }

    public static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[^\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    /**
     * Handles facebook notification redirections
     * @return
     */
    public static Result fbRedirectPost() {
        return fbRedirect();
    }

    public static Result fbRedirect() {
        String url="";
        String anchor="";

        //looking in params for an "url" or an "anchor" or
        // the fb param telling we come from an invitation notif.
        // in this case, we redirect to wimha.com
        // if nothing found : wimha.com too.
        final Set<Map.Entry<String,String[]>> entries = request().queryString().entrySet();
        for (Map.Entry<String,String[]> entry : entries) {
            final String key = entry.getKey();
            final String value = Arrays.toString(entry.getValue());
            if("url".equals(key)){
                url=value.substring(1,value.length()-1);
            } else if("anchor".equals(key)){
                anchor=value.substring(1,value.length()-1);
            } else if("notif_t".equals(key) && "app_request".equals(value)) {
                url="/";
            }
        }
        if("".equals(url)){
            url="/";
        }else{
            try{
                url = URLDecoder.decode(url, "UTF-8");
            } catch(Exception e){
                Logger.error("Exception extracting fb_redirection url target ",e);
                url = ("/");
            }
        }

        return ok(views.html.error_pages.redirectfb.render(url,anchor));
    }


}