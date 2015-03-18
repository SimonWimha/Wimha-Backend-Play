package controllers.social;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.authentication.WimhaSecured;
import external_services.Facebook;
import models.User;
import models.comment.Comment;
import models.comment.FollowedThread;
import models.totem.Flash;
import models.totem.Tribu;
import models.comment.Thread;
import play.data.DynamicForm;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.Date;

/**
* 	Controller handling any request on the comment page.
*/
public class Comments extends Controller {

	/**
	*	Displays comments of the flash
	*/
	public static Result commentPageDisplay(String flash_id, String comingPage) {
		User user = WimhaSecured.getCurrentUser(ctx());
		Flash flash = Flash.findById(flash_id);
     	if(flash==null){
            return badRequest(views.html.error_pages.maintenance.render("This flash has been deleted"));
    	}
    	models.comment.Thread thread = flash.thread;
    	if(thread==null){
    		thread=new Thread(flash);
    		thread.save();
    		flash.thread = thread;
    		flash.save();
    	}
		return ok(views.html.social.commentPage.render(flash.tribu,flash, true, user, comingPage));
	}

    /**
     *	Displays comments of the flash
     */
    public static Result commentPageWS(String flash_id) {
        User user = WimhaSecured.getCurrentUser(ctx());
        Flash flash = Flash.findById(flash_id);
        if(user==null){
            return unauthorized();
        }
        if(flash==null){
            return badRequest();
        }
        models.comment.Thread thread = flash.thread;
        if(thread==null){
            thread=new Thread(flash);
            thread.save();
            flash.thread = thread;
            flash.save();
        }

        ObjectNode data = Json.newObject();
        data.put("thread_id", thread.getId().toString());

        ArrayNode comments = data.putArray("comments");
        for(Comment comment : Comment.getLastCommentsForThread( thread.getId(), 20 )) {
            User commentUser = comment.getUser();
            ObjectNode node = Json.newObject();
            node.put("name", commentUser.firstname);
            if(commentUser.picto!=null) {
                node.put("picture", commentUser.picto.url_h(100));
            }
            node.put("message", comment.getMessage());
            comments.add(node);
        }
        return ok(data);
    }


    
    public static Result getCommentPageWS(final String flashId, final String page) {
        User user = WimhaSecured.getCurrentUser(ctx());
        if(user==null){
            return unauthorized();
        }

        Flash flash = Flash.findById(flashId);
        if(flash==null){
            return badRequest();
        }
        Thread thread = flash.getThread();
        int limit = 4;
        String sql = "SELECT comment.id AS commentId "
                + "FROM comment, thread "
                + "WHERE comment.thread_id = :threadId";

        sql += " GROUP BY comment.id ORDER by comment.created_at DESC LIMIT "
                + limit + " OFFSET " + (limit * Integer.valueOf(page));

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("threadId", thread.getId());


        ObjectNode data = Json.newObject();
        data.put("thread_id", thread.getId().toString());

        ArrayNode comments = data.putArray("comments");
        for (SqlRow sqlRow : sqlQuery.findList()) {
            Comment comment= Comment.findById(sqlRow.getString("commentId"));
            User commentUser = comment.getUser();
            ObjectNode commentJson = Json.newObject();
            commentJson.put("name", commentUser.firstname + " "+ commentUser.lastname);
            if(commentUser.picto!=null) {
                commentJson.put("picture", commentUser.picto.url_h(100));
            }
            commentJson.put("message", comment.getMessage());
            comments.add(commentJson);
        }

        return Controller.ok(data);

    }

	/**
	*	Displays totem followers
	*/
	public static Result followersPageDisplay(String totemId, String comingPage) {
		User user = WimhaSecured.getCurrentUser(ctx());
        Tribu tribu = Tribu.findById(totemId);
        if(tribu == null){
            return notFound();
        }
        return ok(views.html.social.followersPage.render(tribu, true, user, comingPage));
	}

    /**
     * Ajax sumbission of the comment form
     * @param threadId
     * @return
     */
	public static Result submitComment(final String threadId){
		Thread thread = Thread.findById(threadId);
		User user = WimhaSecured.getCurrentUser(ctx());

		DynamicForm requestParameters = play.data.Form.form().bindFromRequest();
		String message = requestParameters.get("message");

		if(thread==null || user == null || message==null || message.replaceAll("\\s","").isEmpty()){
			return badRequest();
		}

		if(message.length()>1000){
			ObjectNode error = Json.newObject();
			error.put("error", "too long");
			return ok(error);
		}

		Comment comment=new Comment(thread, user, message);
		comment.save();

		thread.setLastUpdate(new Date());
		thread.save();


        //subscribe a commenter to the thread of comments
		if (FollowedThread.isSubscribe(user, thread)){
			FollowedThread.updateLastSee(user, thread);
		}else{
			FollowedThread.subscribeUser(user, thread);
		}

		User flasher = thread.flash.getFlasher();
		if (!FollowedThread.isSubscribe(flasher, thread)){
			FollowedThread.subscribeUser(flasher, thread);
		}

		User owner = thread.flash.getTribu().getOwner();
		if (!FollowedThread.isSubscribe(owner, thread)){
			FollowedThread.subscribeUser(owner, thread);
		}

		Facebook.sendFbCommentFlashNotification(user, comment);

		return ok();
	}

}