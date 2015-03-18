package external_services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.User;
import models.comment.FollowedThread;
import models.notification.MobileNotificationNewFlash;
import models.notification.MobileNotificationQuestion;
import models.notification.NotificationPreference;
import models.totem.Flash;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;

/**
 * Created by juliend on 03/11/14.
 */
public class Parse {

    public static void newFlash(Flash flash){

        F.Promise.promise(
                new F.Function0<Void>() {
                    public Void apply() {
                        Logger.info("send parse Notif new flash ");
                        boolean alreadySent = MobileNotificationNewFlash.alreadySentToday(flash.tribu);
                        if(!alreadySent) {

                            for (FollowedThread followedThread : FollowedThread.getSubscribedUserByThread(flash.tribu.getThread())) {
                                User user = followedThread.getUser();

                                if (NotificationPreference.isSubscribed(user, NotificationPreference.newFlashOnATotemYouFollow) && !flash.flasher.equals(user)) {
                                    Logger.info(user.email + " subscribed");

                                    ObjectNode where = Json.newObject();
                                    where.put("email", user.email);
                                    where.put("deviceType", "android"); //todo add ios when app as a feed

                                    ObjectNode appVersion = Json.newObject();
                                    appVersion.put("$gte", "5.6");
                                    where.put("appVersion", appVersion);

                                    ObjectNode data = Json.newObject();
                                    data.put("alert", Messages.get("parse.notif.newFlash", flash.tribu.name));

                                    ObjectNode json = Json.newObject();
                                    json.put("where", where);
                                    json.put("data", data);

                                    //            Ideally write only in the channel
                                    //            ArrayNode channels = json.putArray("channels");
                                    //            channels.add(tribu.name);
                                    //            ArrayNode channels = json.putArray("channels");
                                    //            channels.add(tribu.name);
//
                                    WSResponse response = WS.url("https://api.parse.com/1/push")
                                            .setHeader("X-Parse-Application-Id", Play.application().configuration().getString("parse.APP_ID"))
                                            .setHeader("X-Parse-REST-API-Key", Play.application().configuration().getString("parse.APP_REST_API_ID"))
                                            .post(json).get(5000);
                                    Logger.info(response.asJson().toString());

                                }
                            }
                            MobileNotificationNewFlash.add(flash.tribu);

                        }
                        return null;
                    }
                }
        ).get(100000); //todo 100 sec to send all notifs to test

    }
}
