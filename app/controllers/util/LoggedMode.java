package controllers.util;

import models.User;
import models.totem.Flash;
import play.Logger;
import play.data.Form;

/**
 * For flash submit : parse form params of the logged mode.
 * todo : clean with the  new register process
 * Created by juliend on 20/08/14.
 */
public class LoggedMode {
    public final String id_fb;
    public final String pic_url;
    public final String twitter_consumer_key;
    public Boolean logged;
    public final Boolean postFb;
    public final Boolean postTw;

    public LoggedMode(Form<Flash> filledForm, User user) {
        String loggedStr=filledForm.data().get("logged");
        String postFbStr=filledForm.data().get("post_fb");
        String postTwStr=filledForm.data().get("post_twitter");

        id_fb = filledForm.data().get("id_fb") == null ? filledForm.data().get("fb_id") : filledForm.data().get("id_fb");
        pic_url = filledForm.data().get("pic_url");
        twitter_consumer_key = filledForm.data().get("twitter_consumer_key");

        Logger.info("[Submit] loggedStr " + loggedStr);
        if(loggedStr != null) {
            logged = Boolean.valueOf(loggedStr);

            // If no cookie, user considered not logged
            if(user==null){
                //todo uncomment after iOS update
                //logged=false;
            }
            Logger.info("[Submit] logged " + logged);
        }else{
            logged = false;
        }

        if(postFbStr != null){
            postFb = Boolean.valueOf(postFbStr);
            Logger.info("[Submit] postFb "+postFb);
        } else{
            postFb = false;
        }
        if(postTwStr != null){
            postTw = Boolean.valueOf(postTwStr);
            Logger.info("[Submit] postTw "+postTw);
        } else{
            postTw = false;
        }

    }
}
