package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.authentication.WimhaSecured;
import controllers.social.SocialFeed;
import jsmessages.JsMessages;
import models.Url;
import models.User;
import models.comment.FollowedThread;
import models.comment.Thread;
import models.notification.DelayedMailNotification;
import models.socialAction.LikeFlashAction;
import models.totem.Flash;
import models.totem.Member;
import models.totem.Tribu;
import play.Logger;
import play.Play;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

import java.util.List;

/**
* 	Transerval features, common to several pages.
*/
public class Application extends Controller {

	//Used with isProd to avoid having analytics or metrics on val server.
    public static boolean VAL = play.Play.application().configuration().getBoolean("is_val");

	//Used for emails links or any other absolute links
	public static String SERVER_URL= getServerUrlFromConf();

    private static String getServerUrlFromConf() {
        if(play.Play.isProd()){
            if( VAL ) {
                return play.Play.application().configuration().getString("val_url");
            }else{
                return play.Play.application().configuration().getString("prod_url");
            }

        }else if(play.Play.isDev()){
            return play.Play.application().configuration().getString("dev_url");
        }else{
            return play.Play.application().configuration().getString("test_url");
        }
    }

    //Displays whether the feed or the totem family if no
    public static Result index() {
        User user = WimhaSecured.getCurrentUser(ctx());
        if(user!=null){
            return SocialFeed.socialFeed();
        }

        return redirect(routes.Application.totemIndexPage());
    }

    public static Result indexAjax() {
        ObjectNode html = Json.newObject();
        html.put("html", views.html.homePage_noel_ajax.render().body());
        return ok(html);
    }

    /**
     * Retrieves messages as a rss feed
     * @param  totemName [description]
     * @return            [description]
     */
    public static Result rss(final String totemName) {
        Tribu tribu = Tribu.findByName(totemName);
        if(tribu !=null){
            List<Flash> res= Flash.findAll(tribu);
            return ok(views.xml.rss.render(totemName, res));
        }else{
            return notFound();
        }

    }

    public static Result myTotemPage(String totemName){
        User user = WimhaSecured.getCurrentUser(ctx());
        Tribu tribu = Tribu.findByName(totemName);
        if(tribu ==null){
            return badRequest(views.html.error_pages.maintenance.render("This is not a totem"));
        }
        if(tribu.token!=null){
            return badRequest(views.html.error_pages.maintenance.render("This totem isn't validated yet"));
        }

        List<Flash> list = Flash.getMyTP(0, tribu, false);
        List<Flash> listFavorites = Flash.getMyTP(0, tribu, true);

        return ok(views.html.myTotemPage.render(tribu, tribu.getThread(), Helpers.isMobile(), user, list, listFavorites));
    }
    /**
     * get feed next page in ajax
     * @param page
     * @return
     */
    public static Result getMyTPPage(String page, String totemName, String favoriteOnly) {
        Tribu tribu=Tribu.findByName(totemName);
        List<Flash> list = Flash.getMyTP(Integer.valueOf(page), tribu, Boolean.valueOf(favoriteOnly));

        if(list !=null && !list.isEmpty()){
            ObjectNode html = Json.newObject();
            html.put("html", views.html.myTP_list.render(WimhaSecured.getCurrentUser(ctx()),tribu,list).body());
            return ok(html);
        }

        return badRequest("empty");
    }

    /*
     * Displays the google map
     * @param  totem_name [description]
     * @return            [description]
     */
    public static Result map(final String totem_name){
        List<Flash> res= Flash.findAll(Tribu.findByName(totem_name));
        return ok(views.html.flashPages.map.render(totem_name,res));
    }

    /*
     * Displays the google map
     * @param  totem_name [description]
     * @return            [description]
     */
    public static Result mapData(final String totem_name){
        Tribu tribu = Tribu.findByName(totem_name);
        if(tribu==null){
            return notFound();
        }
        List<Flash> res= Flash.findAll(tribu);
        return ok(views.html.elements.map_data.render(res));
    }


    //Subscribe a user to a thread (to follow flashs or comments)
	public static Result subscribeUser(final String threadId){
		User user=WimhaSecured.getCurrentUser(ctx());
		models.comment.Thread thread = models.comment.Thread.findById(threadId);
		if(user==null){
			return badRequest("User not found");
		}
		if(thread==null){
			return badRequest("Thread not found");
		}

		if(!FollowedThread.isSubscribe(user,thread)){
			FollowedThread.subscribeUser(user,thread);
			DelayedMailNotification.newFollower(user, threadId);
			return ok();
		}
		return badRequest("Already subscribed user");
	}

	public static Result unsubscribeUser(final String threadId){
		User user=WimhaSecured.getCurrentUser(ctx());
		models.comment.Thread thread = models.comment.Thread.findById(threadId);
		if(user==null){
			return badRequest("User not found");
		}
		if(thread==null){
			return badRequest("Thread not found");
		}

		if(FollowedThread.isSubscribe(user,thread)){
			Logger.info("subscribe user  "+user + " to thread " +thread);
			FollowedThread.unSubscribeUser(user,thread);
			return ok();
		}
		return badRequest("Not subscribed user");
	}

	//to unsubscribe of a thread from the mail
	public static Result unsubscribeFromMail(final String threadId, final String userId){
		User user=User.findById(userId);
		Thread thread = Thread.findById(threadId);

		if(user==null){
			return badRequest("User not found");
		}
		if(thread==null){
			return badRequest("Thread not found");
		}
		if(FollowedThread.isSubscribe(user,thread)){
            Logger.info("unsubscribe user  "+user + " to thread " +thread);

            FollowedThread.unSubscribeUser(user,thread);
			return ok("Vous avez bien été désinscrit");
		}
		return badRequest("Not subscribed user");
	}


	public static Result sitemap() {
        return ok(views.xml.sitemap.sitemap.render());
    }


    final static JsMessages messages = JsMessages.create(Play.application());
    public static Result jsMessages() {
        return ok(messages.generate("window.Messages"));
    }



    public static Result actoboard(){
        //where to push
        String url="";

        //calculate numbers
        int nb= Tribu.findAll().size();
        int nbActivated= Tribu.findAllActivated().size();

        //build json
        ObjectNode json = Json.newObject();
        json.put("totem_nb", "nb");
        json.put("activated_totem_nb", nbActivated);
        return ok(json);

    }

    public static Result faq() {
        return movedPermanently("https://www.wimha.com/totemFamily/#faq");
    }

    public static Result appIphone() {
        return movedPermanently("https://itunes.apple.com/fr/app/wimha/id873048515?mt=8");
    }

    public static Result appAndroid() {
        return movedPermanently("https://play.google.com/store/apps/details?id=com.wimha.app");
    }

    public static Result redirectShortUrl(String shortId) {
        String id="";
        Member member =Url.findMemberByShortId(shortId);
        Tribu tribu =Url.findTribuByShortId(shortId);

        if(member!=null){
            id=member.id.toString();
            if(member.blocked!=null && member.blocked){
                return unauthorized("this member is blocked");
            }

            if(member.tribu==null){
                return notFound("this member has no tribe");
            }

        }else if( tribu!=null) {
            id=tribu.id.toString();
        }else{
            return notFound("url not found in DB");
        }

        String target=SERVER_URL+"/id/"+ id;
        response().setHeader("location", target);
        return movedPermanently(target);
    }

    public static Result dailyDigest(){
        if(VAL && WimhaSecured.getCurrentUser(ctx()).isAdmin()) {
            MailService.dailyDigestScript();
            return ok("done");
        }
        return ok("not in prod ..");
    }


    /**
     * displays homepage
     * @return
     */
    public static Result totemIndexPage() {
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.homePage.render(user));
    }

    /**
     * displays tos
     * @return
     */
    public static Result terms() {
        return ok(views.html.terms.render());
    }

    /**
     * displays homepage
     * @return
     */
    public static Result totemIndexPageFr() {
        User user = WimhaSecured.getCurrentUser(ctx());
        ctx().changeLang("fr");
        return ok(views.html.homePage.render(user));
    }


    /**
     * When the first flash is auto validated ht mail isnt validated
     * the user mailed might want to remove the fake flash
     * @return
     */
    public static Result deleteFlash() {
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.deleteFlash.render(user));
    }


    /**
     * To test the render of an email
     * @return
     */
    public static Result emailPreview(){
        User user = User.findUserByEmail("jderenty@gmail.com");
        Tribu tribu = Tribu.findByName("Henri");
        java.util.List<models.socialAction.LikeFlashAction.UserLikes> list = LikeFlashAction.findMostLikedUsers(tribu);

        return ok(views.html.emails.inlined.ambassador_week.render(user, tribu, list));
    }

    /****** TOTEM LIST PREVIEW ******/

    public static Result totemList(){
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.totemList.render(user));
    }

    /****** PREVIEW GENID PAGE ******/

    public static Result genPublicId(){
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.genPublicId.render(user));
    }
}
