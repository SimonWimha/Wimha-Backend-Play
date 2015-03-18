package controllers.social;

import controllers.authentication.WimhaSecured;
import models.User;
import models.notification.DelayedMailNotification;
import models.socialAction.LikeFlashAction;
import models.socialAction.LikeTotemAction;
import models.totem.Flash;
import models.totem.Tribu;
import play.mvc.Controller;
import play.mvc.Result;

public class Like extends Controller {


	public enum TypeofLike {
		FlashThanks, CommentThanks, UnknowAction;

	};

	public static LikeFlashAction alreadyLiked(Flash flash) {
		User user = WimhaSecured.getCurrentUser(ctx());
		return LikeFlashAction.alreadyLiked(user, flash);
	}

	public static Result like(String flash_id) {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user==null){
            return unauthorized();
		}

		Flash flash = Flash.findById(flash_id);
		if(flash ==null){
			return badRequest();
		}

        if (alreadyLiked(flash)!=null){
            return ok();
        }

		LikeFlashAction like=LikeFlashAction.like(user, flash);
		if(like!=null){
			DelayedMailNotification.newLikeFlash(user, flash);

            return ok();
   		}else{
			return badRequest();
		}
	}

	public static Result unlike(String flashId) {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user==null){
            return unauthorized();
		}

		LikeFlashAction.unlike(user,flashId);
		return ok();
	}

	public static LikeTotemAction alreadyLikedTotem(Tribu tribu) {
		User user = WimhaSecured.getCurrentUser(ctx());
		return LikeTotemAction.alreadyLiked(user, tribu);
	}

	public static Result likeTotem(String totemId) {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user==null){
			return unauthorized();
		}

		Tribu tribu = Tribu.findById(totemId);
		if(tribu ==null){
			return badRequest();
		}

		LikeTotemAction like=LikeTotemAction.like(user, tribu);
		if(like!=null){
			DelayedMailNotification.newLikeTotem(user, tribu);
            return ok();
   		}else{
			return badRequest();
		}
	}

	public static Result unlikeTotem(String totemId) {
		User user = WimhaSecured.getCurrentUser(ctx());
		if(user==null){
            return unauthorized();
		}

		LikeTotemAction.unlike(user,totemId);
		return ok();
	}

}
