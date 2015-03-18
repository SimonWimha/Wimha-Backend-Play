package controllers.authentication;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Application;
import controllers.util.UserForm;
import models.User;
import models.util.AuthenticationMethod;
import org.apache.commons.lang3.StringUtils;
import patch.PatchedForm;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

public class Register extends Controller {

	final static Form<UserForm> registerForm = new PatchedForm<UserForm>(
			UserForm.class);

    /**
     * handles ajax when typing mail
     * @param mail
     * @return
     */
	public static Result checkMail(final String mail){
		User user = User.findUserByEmail(mail);
		ObjectNode json = Json.newObject();

		if(user !=null && user.authenticationMethod.equals(AuthenticationMethod.USERNAME_PASSWORD)){
			json.put("exist","true");
			json.put("type","mdp");
		} else if(user!=null){
			json.put("exist","true");
			json.put("type","fbc");
		} else{
			json.put("exist","false");
		}
		return ok(json);
	}

    /**
     * handles case fbc returns no email
     * @param mail
     * @return
     */
    public static Result checkFbId(final String fbId){
        User user = User.findByFbId(fbId);
        ObjectNode json = Json.newObject();

        if(user != null && StringUtils.isNotBlank(user.email)){
            json.put("hasEmail","true");
            json.put("email",user.email);
        } else{
            json.put("hasEmail","false");
        }
        return ok(json);
    }

    /**
     * Called in ajax after clic on Facebook Connect of login page
     * @return
     */
    public static Result doFacebookConnect() {
        DynamicForm form = play.data.Form.form().bindFromRequest();

        //Fetch form data
        String email = form.data().get("email");

        String firstname = form.data().get("firstname");
        String lastname = form.data().get("lastname");
        String fbId = form.data().get("fbId");
        String fbToken = form.data().get("fbToken");

        //Use name if no firstname/ lastname filled
        if(StringUtils.isBlank(firstname)){
            firstname = form.data().get("name");
        }

        //Find use eventually already in DB with it's email or fb id
        String event = "login";
        User user = User.findByFbId(fbId);
        if(user==null){
             user = User.findUserByEmail(email);
        }

        //Else he is registering.
        if(user==null){
            event = "register";
            user = new User(firstname, lastname, email, null, null);
        }else if(StringUtils.isBlank(user.email)){
            user.email = email;
        }

        //update the user
        ObjectNode json = Json.newObject();
        Logger.info("FBC "+user.id + " "+fbId);
        user.idFb=fbId;
        user.facebookToken=fbToken;
        user.save();

        //create session
        Helper.addSession(user);

        //Send to ajax if user has no mail we must redirect to the missingMail page
        if(StringUtils.isNotBlank(user.email)){
            json.put("result", "success");
            json.put("hasEmail","true");
            json.put("email",user.email);
        } else{
            json.put("result", "error");
            json.put("hasEmail","false");
        }

        return ok(json);
    }


    /**
     * handles login credentials submission
     * @return
     */
	public static Result doRegister() {
    	Logger.info("Registering user");

		Form<UserForm> filledForm = registerForm.bindFromRequest();
		DynamicForm form = play.data.Form.form().bindFromRequest();

		//form validation only for new user
        if (form.get("user.email").equals("") || User.findUserByEmail(form.get("user.email"))==null && filledForm.hasErrors()) {
			ObjectNode error = Json.newObject();
			error.put("error", filledForm.errorsAsJson());
            Logger.info("Register errors");
            Logger.info(error.asText());
			return ok(error);
		} else {
            //login user
            User.CreationUserResult infos;
            if (User.findUserByEmail(form.get("user.email")) != null) {
                infos = User.getOrCreateUser("",
                        form.get("user.email"),
                        form.get("password"),
                        true
                );
            } else {
                //register user
                infos = User.getOrCreateUser(filledForm.get().user.firstname,
                        filledForm.get().user.email,
                        form.get("password"),
                        true
                );
            }

            User newUser = null;
            if (infos != null) {
                newUser = infos.user;
            }

            ObjectNode json = Json.newObject();
            if (newUser != null) {
                Helper.addSession(newUser);

                String event="";
                if (infos.password != null) {
                    MailService.confirmationMail(newUser, infos.password);
                    json.put("type","register");
                    event="register";
                }else{
                    json.put("type","login");
                    json.put("name", newUser.firstname+ " "+ newUser.lastname);
                    if(newUser.picto!=null)
                        json.put("picture", newUser.picto.url_w(100));

                    event="login";

                }

            } else {
                ObjectNode passwordError = Json.newObject();
                passwordError.put("password", Messages.get("error.login.wrongPassword"));
                json.put("error", passwordError);
            }
            return ok(json);
        }
	}

    /**
     * displays a page asking for user's mail
     * for after facebok connect if the user doesnt share his mail
     * @param originUrl
     * @return
     */
    public static Result missingEmail(String originUrl) {
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.authentication.missingEmail.render(user, originUrl));
    }


    /**
     * handles mail submission
     * @return
     */
    public static Result giveEmail() {

        DynamicForm form = play.data.Form.form().bindFromRequest();
        String mail = form.get("give_email");

        //form validation only for new user
        User user=WimhaSecured.getCurrentUserEvenUnregistered(ctx());
        user.email=mail;
        user.save();

        Logger.info("giving mail user "+user.id+" "+mail);
        return ok("mail_ok");
    }

    /**
     * handles validation link in mails
     * @param token
     * @return
     */
    public static Result validateToken(String token) {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user!=null){
			if(user.token!=null){
				if (user.token.toString().equals(token)){
					user.token=null;
                    user.save();
					//MailService.sendRegisterMail(user);
					return Application.index();
				} else{
					// Trying to validate another account than the one connected
					// Log out and retry
				}
			} else{
				//Email already confirmed
			}

		} else{
			User userFound=User.findUserByToken(token);
			if(userFound!=null){
                user.token=null;
                user.save();
                Helper.addSession(userFound);
				return Application.index();

			}
		}
		return redirect(controllers.authentication.routes.Helper.login("%2f"));
	}

    public static Result validateTokenAndLog(String token, String id) {
        User user = WimhaSecured.getCurrentUser(ctx());
        if(user!=null){
            if(user.token!=null){
                if (user.token.toString().equals(token)){
                    user.token=null;
                    user.save();                    //MailService.sendRegisterMail(user);
                    return Application.index();
                } else{
                    // Trying to validate another account than the one connected
                    // Log out and retry
                }
            } else{
                //Email already confirmed
            }

        } else{
            User userFound=User.findUserByToken(token);
            if(userFound!=null){
                userFound.token=null;
                userFound.save();
                Helper.addSession(userFound);
                return Application.index();

            }else{
                return Application.index();
            }
        }
        return redirect(controllers.authentication.routes.Helper.login("%2f"));
    }



}
