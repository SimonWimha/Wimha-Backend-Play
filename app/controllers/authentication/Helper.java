package controllers.authentication;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Cookie;
import models.Password;
import models.User;
import models.util.AuthenticationMethod;
import play.Logger;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

import java.util.UUID;

/**
 * Created by juliend on 02/09/14.
 */
public class Helper extends Controller {

    /**
     * Displays login page and writes original page in the cookie (to redirect after facebook connect or email pw login)
     * @param originUrl
     * @return
     */
    public static Result login(String originUrl) {
        User user = WimhaSecured.getCurrentUser(ctx());
        if (user != null) {
            return redirect(controllers.routes.Application.index());
        } else {
            try{
                ctx().session().put("original-url", originUrl);
            }catch(Exception e){
                Logger.error("Exception when putting origin url after going to login page", e);
            }
            if(WimhaSecured.getCurrentUserEvenUnregistered(ctx())==null){
                return ok(views.html.authentication.login.render(user, originUrl));
            }else{
                return ok(views.html.authentication.missingEmail.render(user, originUrl));
            }
        }
    }

    /**
     * displays login without writing in the cookie
     * @param originUrl
     * @return
     */
    public static Result loginWithoutOrigin() {
        User user = WimhaSecured.getCurrentUser(ctx());

        // check if the user is already logged
        if (user != null) {
            return redirect(controllers.routes.Application.index());
        } else {
            if(WimhaSecured.getCurrentUserEvenUnregistered(ctx())==null){
                return ok(views.html.authentication.login.render(user, ""));
            }else{
                return ok(views.html.authentication.missingEmail.render(user, ""));
            }
        }
    }

    /**
     * Delete session and redirect homepage
     * @return
     */
    public static Result logout() {
        play.mvc.Http.Cookie cookie = ctx().request().cookies().get(Cookie.cookieName);
        if(cookie!=null){
            String idInSession = cookie.value();
            Cookie.remove(idInSession);
        }
        response().discardCookie(Cookie.cookieName);
        session().clear();
        return redirect(controllers.routes.Application.index());
    }

    /**
     * Used to log programatically the user, like after register, after clicking a link in email (validation flash), etc
     * @param user
     */
    public static void addSession(User user){
        String cookieId = Cookie.fromUser(user, "userpass");
        response().setCookie(Cookie.cookieName, cookieId);
    }

    /**
     * called on login page if user clicks the reset button
     * @return
     */
    public static Result sendResetPasswordMail() {
        DynamicForm requestParameters = play.data.Form.form().bindFromRequest();
        String userMail = requestParameters.get("email");
        User user = User.findUserByEmail(userMail);

        ObjectNode json = Json.newObject();

        if(user!=null){
            user.token= UUID.randomUUID();
            user.save();
            MailService.resetPassword(user, String.valueOf(user.token));

            json.put("success", "mail_sent");
            return ok(json);
        }else{
            json.put("error", "user_not_found");
            return notFound(json);
        }
    }

    /**
     * called on login page if user clicks the reset button
     * @param token
     * @return
     */
    public static Result resetPasswordSubmit(String token) {
        User user=User.findUserByToken(token);

        DynamicForm requestParameters = play.data.Form.form().bindFromRequest();
        String newPassword = requestParameters.get("newPassword");
        String confirmNewPassword = requestParameters.get("confirmNewPassword");

        ObjectNode json = Json.newObject();
        ObjectNode error = Json.newObject();

        if(user==null) {
            error.put("user", "The password has already been reseted. Going to home page ...");
            json.put("error", error);
            return ok(json);
        }
        if(!newPassword.equals(confirmNewPassword)) {
            error.put("confirmNewPassword", "Passwords do not match");
            json.put("error", error);
            return ok(json);
        }
        if(newPassword.length()<6) {
            error.put("newPassword", "At least 6 characters please");
            json.put("error", error);
            return ok(json);
        }
        user.authenticationMethod = AuthenticationMethod.USERNAME_PASSWORD;
        user.password = new Password(newPassword);
        user.token=null;
        user.save();
        addSession(user);
        return ok();
    }

    /**
     * user comes on a "reset password" page, he is logged and he can change his password.
     * @param token
     * @return
     */
    public static Result resetPasswordPage(String token) {
        User user=User.findUserByToken(token);
        if(user!=null){
            return ok(views.html.authentication.reset_password.render(token,false,false,false));
        }else{
            return ok("lien périmé");
        }
    }
}
