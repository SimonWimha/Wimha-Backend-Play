package controllers;


import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.authentication.Helper;
import controllers.authentication.WimhaSecured;
import models.notification.NotificationPreference;
import models.Picture;
import models.User;
import models.notification.MobileNotificationQuestion;
import models.totem.Member;
import models.totem.Tribu;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;

import play.libs.ws.*;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import controllers.util.ModifyAccountFormTotem;

import java.io.File;
import java.util.regex.Pattern;


public class Settings extends Controller {


    static Form<ModifyAccountFormTotem> userForm = Form.form(ModifyAccountFormTotem.class);

    /**
     * displays settings page
     * @param error_size
     * @return
     */
	public static Result accountSettings(String error_size) {
		User user = WimhaSecured.getCurrentUser(ctx());
		ModifyAccountFormTotem initialData = new ModifyAccountFormTotem();

		if (user != null) {
			initialData.email = user.email;
			initialData.firstName = user.firstname;
			initialData.userId = user.id.toString();
		}else{
            return redirect(controllers.authentication.routes.Helper.login("%2f"));
		}

		return ok(views.html.settingsPage.render(user, userForm.fill(initialData), error_size, Helpers.isMobile()));
	}


    /**
     * handles settings modifications
     * @return
     */
	public static Result accountSettingsSubmit() {
    	Logger.info("Modifying user");
		User user = WimhaSecured.getCurrentUser(ctx());

		Form<ModifyAccountFormTotem> filledForm = userForm.bindFromRequest();

		if (user == null) {
			return notFound("User not found");
		}

		if (filledForm.hasErrors()) {
			ObjectNode error = Json.newObject();
			error.put("error", filledForm.errorsAsJson());
            Logger.info("errors : "+error.toString());
			return ok(error);
		} else {

			if (filledForm.get().email != null){
				filledForm.get().email = filledForm.get().email.toLowerCase();				
			}

			ModifyAccountFormTotem.update(user, filledForm.get());


			return ok();
		}

	}

    /**
     * handles totem modifications
     * @return
     */
    public static Result accountSettingsSubmitTotem() {
        User user = WimhaSecured.getCurrentUser(ctx());


        DynamicForm form = play.data.Form.form().bindFromRequest();
        String totemId = form.get("totemId");
        String question = form.get("question");
        String description = form.get("description");
        String vcardEmail = form.get("vcardEmail");
        String vcardFacebook = form.get("vcardFacebook");
        String vcardTwitter = form.get("vcardTwitter");

        if (user == null) {
            return notFound("User not found");
        }

        if (question==null || question.trim().isEmpty()) {
            ObjectNode json = Json.newObject();
            json.put("error", "questionEmpty");
            return ok(json);
        }

        if (!vcardEmail.isEmpty() && !rfc2822.matcher(vcardEmail).matches()) {
            ObjectNode json = Json.newObject();
            return ok(json);
        }

        Tribu tribu = Tribu.findById(totemId);
        Logger.info("Modifying totem "+ tribu +vcardEmail);

        tribu.question=question;
        tribu.description=description;
        tribu.owner_vcard_email = vcardEmail;
        tribu.owner_vcard_facebook = vcardFacebook;
        tribu.owner_vcard_twitter = vcardTwitter;
        tribu.update();

        String notifStr=form.get("notif");

        boolean alreadySent = MobileNotificationQuestion.alreadySentToday(tribu);
        Logger.info("Notif already sent "+alreadySent);

        int cpt = 0;
        if(!alreadySent && StringUtils.isNotEmpty(notifStr) && Boolean.valueOf(notifStr)) {

            for(Member member : tribu.members){
                Logger.info("Notification sending for member "+ member.id);

                if(member.user!=null) {
                    ObjectNode where = Json.newObject();
                    where.put("email", member.user.email);

                    ObjectNode data = Json.newObject();
                    data.put("alert", Messages.get("parse.notif.question") + question);
                    data.put("action", "com.wimha.app.COMMAND_UPDATE_QUESTION");
                    data.put("question", question);
                    data.put("id", member.id.toString());

                    ObjectNode json = Json.newObject();
                    json.put("where", where);
                    json.put("data", data);

                    WSResponse response = WS.url("https://api.parse.com/1/push")
                            .setHeader("X-Parse-Application-Id", Play.application().configuration().getString("parse.APP_ID"))
                            .setHeader("X-Parse-REST-API-Key", Play.application().configuration().getString("parse.APP_REST_API_ID"))
                            .post(json).get(5000);
                    Logger.info("Notification sent for "+ member.user.email +" / result :"+response.getBody());
                    cpt++;
                }else{
                    Logger.warn("MEmber non initialized " + member.id + " (no user id, so no email)");
                }

            }
            MobileNotificationQuestion.add(tribu);
        }

        ObjectNode json = Json.newObject();
        json.put("cpt", cpt);

        return ok(json);
    }

    /**
     * handles totem modifications
     * @return
     */
    public static Result accountSettingsSubmitTotemNotif() {
        User user = WimhaSecured.getCurrentUser(ctx());


        DynamicForm form = play.data.Form.form().bindFromRequest();
        String totemId = form.get("totemId");
        String question = form.get("question");

        if (user == null) {
            return notFound("User not found");
        }

        if (question==null || question.trim().isEmpty()) {
            ObjectNode json = Json.newObject();
            json.put("error", "questionEmpty");
            return ok(json);
        }

        Tribu tribu = Tribu.findById(totemId);
        Logger.info("Sending totem notif"+ tribu );

        tribu.question=question;
        tribu.update();


        if(MobileNotificationQuestion.alreadySentToday(tribu)){
            Logger.info("Notif already sent ");
            ObjectNode json = Json.newObject();
            json.put("error", "alreadysent");
            return ok(json);
        }

        int cpt = 0;

        Logger.info("Sending notif to "+tribu.members.size() + " members" );

        for(Member member : tribu.members){
            Logger.info("Notification sending for member "+ member.id);

            if(member.user!=null) {
                ObjectNode where = Json.newObject();
                where.put("email", member.user.email);

                ObjectNode data = Json.newObject();
                data.put("alert", Messages.get("parse.notif.question") + question);
                data.put("action", "com.wimha.app.COMMAND_UPDATE_QUESTION");
                data.put("question", question);
                data.put("id", member.id.toString());

                ObjectNode json = Json.newObject();
                json.put("where", where);
                json.put("data", data);

                WSResponse response = WS.url("https://api.parse.com/1/push")
                        .setHeader("X-Parse-Application-Id", Play.application().configuration().getString("parse.APP_ID"))
                        .setHeader("X-Parse-REST-API-Key", Play.application().configuration().getString("parse.APP_REST_API_ID"))
                        .post(json).get(5000);
                Logger.info("Notification sent for "+ member.user.email +" / result :"+response.getBody());
                cpt++;
            }else{
                Logger.warn("MEmber non initialized " + member.id + " (no user id, so no email)");
            }

        }
        MobileNotificationQuestion.add(tribu);

        ObjectNode json = Json.newObject();
        json.put("cpt", cpt);

        return ok(json);
    }

    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    /**
     * handles picture upload
     * @return
     */
	public static Result changePicture(){
		User user=WimhaSecured.getCurrentUser(ctx());
		MultipartFormData body = request().body().asMultipartFormData();
        File picture = body.getFile("upfile").getFile();
        FilePart pictureFilePart = body.getFile("upfile");
        if (picture != null) {
			try {
				if (picture.length() > 700000) {
					return redirect(routes.Settings.accountSettings("true"));
				}

				user.setPicture(picture, pictureFilePart.getContentType());

				// update in database
				user.update();

			} catch (Exception e) {
				return redirect(routes.Settings.accountSettings("true"));
			}
        }
       
        return redirect(routes.Settings.accountSettings("false"));
	}


    /**
     * handles picture upload
     * @return
     */
    public static Result changeTotemPageBackPicture(String totem_name){
        Tribu tribu = Tribu.findByName(totem_name);
        if(tribu ==null){
            return badRequest("totem you want to change background picture does not exist");
        }

        MultipartFormData body = request().body().asMultipartFormData();
        File picture = body.getFile("upfile").getFile();
        FilePart pictureFilePart = body.getFile("upfile");
        if (picture != null) {
            try {
                if(picture.length()>1000000){
                    flash("error", Messages.get("error.picture.size"));
                }else {
                    if(tribu.backgroundPicture!=null){
                        Picture.updatePicture(tribu.backgroundPicture.getId(), picture, pictureFilePart.getContentType());
                    }else{
                        tribu.backgroundPicture = Picture.save(picture, pictureFilePart.getContentType());
                    }
                    tribu.save();
                }
            } catch (Exception e) {
                Logger.error("Exception saving background picture", e);
            }
        }

        return redirect(controllers.routes.Application.myTotemPage(totem_name));
    }

    /**
     * displays notifications settings page
     * @return
     */
	public static Result notificationSettings() {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user==null){
			return redirect(controllers.authentication.routes.Helper.login("%2f"));
		}
		return ok(views.html.notificationSettings.render(user));	// user can be null if not logged in
	}

    /**
     * handles ajax notif setting modification
     * @param settingName
     * @return
     */
	public static Result subscribeNotification(String settingName){
		User user=(User) WimhaSecured.getCurrentUser(ctx());
		if(user==null){
			return badRequest();
		}
		if (NotificationPreference.subscribe(user, settingName)){
			return ok();
		}
		return badRequest();
	}

    public static Result unsubscribeNotification(String settingName){
        User user=(User) WimhaSecured.getCurrentUser(ctx());
        if(user==null){
            return badRequest();
        }
        if (NotificationPreference.unsubscribe(user, settingName)){
            return ok();
        }
        return badRequest();
    }

    public static Result unsubscribeFromMail(String userId, String group){
        User user= User.findById(userId);
        if(user==null){
            return unauthorized();
        }
        Helper.addSession(user);
        if (NotificationPreference.unsubscribe(user, group)){
            flash("unsubscribe", Messages.get("email.unsubscribe_mail."+group));
        }else{
            flash("unsubscribe", Messages.get("already_unsubscribe"));
        }
        return redirect(routes.Application.index());
    }

}
