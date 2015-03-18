package controllers.social;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Helpers;
import controllers.authentication.WimhaSecured;
import models.User;
import models.socialAction.LikeFlashAction;
import models.totem.Flash;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Date;
import java.util.List;

/**
 * Created by juliend on 26/08/14.
 */
public class SocialFeed extends Controller {

    /**
     * displays feed
     * @return
     */
    public static Result socialFeed() {
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.social.socialFeed.render(user, Helpers.isMobile()));
    }

    /**
     * get feed next page in ajax
     * @param page
     * @return
     */
    public static Result getFeedPage(String page) {
        User user = WimhaSecured.getCurrentUser(ctx());

        long d=new Date().getTime();
        List<Flash> list= Flash.getFeed(user, Integer.valueOf(page));

        if(list !=null && !list.isEmpty()){
            ObjectNode html = Json.newObject();
            html.put("html", views.html.social.socialFeed_list.render(user, list).body());
            return ok(html);
        }

        return badRequest("empty");
    }

    /**
     * Generates a JSON containing flashs of tribus the user follows.
     * Sorted by most recent
     *
     * @param page
     * @return
     */
    public static Result getFeedPageWS(String page){
        User user = WimhaSecured.getCurrentUser(ctx());
        if(user==null){
            return unauthorized();
        }
        List<Flash> list= Flash.getFeed(user, Integer.valueOf(page));

        if(list != null && !list.isEmpty()){

            ObjectNode data = Json.newObject();
            ArrayNode flashs = data.putArray("flashs");

            for(Flash flash : list) {
                ObjectNode node = Json.newObject();
                node.put("flashId", flash.id.toString());
                node.put("alreadyLiked", LikeFlashAction.alreadyLiked(user, flash)!=null);
                node.put("tribu", flash.tribu.name);
                node.put("flasher", flash.name);
                node.put("message", flash.message);
                node.put("likesNb", LikeFlashAction.findLikesForAFlash(flash).size());
                node.put("commentsNb", flash.thread!=null ? flash.thread.getComments().size() : 0);
                if(flash.flasher.picto!=null) {
                    node.put("flasher_picture", flash.flasher.picto.url_w(100));
                }
                if(flash.picture!=null) {
                    node.put("picture", flash.picture.imageFlashForFeed()); // limit picture width to 400px for mobile feed
                }
                flashs.add(node);
            }

            return ok(data);
        }

        return badRequest("empty");
    }

}
