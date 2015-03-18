package controllers;

import controllers.authentication.WimhaSecured;
import models.User;
import models.comment.Comment;
import models.notification.DelayedMailNotification;
import models.notification.NotificationPreference;
import models.totem.Flash;
import models.totem.Tribu;
import org.apache.commons.lang3.StringUtils;
import org.jboss.netty.util.internal.StringUtil;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import services.MailService;

/**
 * Created by juliend on 03/09/14.
 */
public class Admin extends Controller {

    /**
     *
     * @param id
     * @return
     */
    public static Result delete(final String id){
        final Flash flash = Flash.findById(id);

        if(flash ==null){
            Logger.error("Error delete with id:" + id);
            return badRequest(views.html.error_pages.maintenance.render(null));
        }

        MailService.deletionRequest(flash);

        return ok(views.html.deleteFlash.render(flash.getFlasher()));
    }


    /**
     *
     * @param id
     * @return
     */
    public static Result ownerDelete(final String id){
        User user = WimhaSecured.getCurrentUser(ctx());
        final Flash flash = Flash.findById(id);

        if(flash ==null || user==null || !user.equals(flash.tribu.getOwner())){
            Logger.error("Error delete with id:" + id);
            return badRequest();
        }

        flash.delete();

        return ok();
    }

    /**
     *
     * @param id
     * @return
     */
    public static Result ownerDeleteComment(final String id){
        User user = WimhaSecured.getCurrentUser(ctx());
        final Comment comment = Comment.findById(id);
        Flash pos=comment.getThread().flash;
        Tribu tot=pos.getTribu();
        User owner=tot.getOwner();
        if(comment==null || user==null || !user.equals(owner)){
            Logger.error("Error delete with id:"+id);
            return badRequest();
        }

        comment.delete();

        return ok();
    }


    /**
     *
     * @param id
     * @return
     */
    public static Result block(final String id){
        final Flash flash = Flash.findById(id);

        if(flash ==null){
            Logger.error("Error delete with id:"+id);
            return badRequest(views.html.error_pages.maintenance.render(null));
        }

        flash.member.blocked=true;
        flash.member.save();

        MailService.blockedMember(flash);

        return ok(views.html.deleteFlash.render(flash.getFlasher()));
    }


    public static Result scriptFlasherPics(){
        int cpt=0;
        for(User user:User.findAll()){
            if(user.picto==null){
                Flash lastFlash= Flash.getLastMessage(user);
                if(lastFlash!=null) {
                    user.picto = lastFlash.picture;
                    user.save();
                    cpt++;
                }
            }
        }
        return ok(cpt+" images setées");
    }


    public static Result favorite(String flashId, String value){
        Boolean favorite=Boolean.valueOf(value);
        Flash flash=Flash.findById(flashId);
        if(favorite==null || flash==null){
            return badRequest();
        }

        flash.favorite=favorite;
        flash.save();

        if(favorite) {
            DynamicForm form = Form.form().bindFromRequest();
            String text = form.get("text");
            if(StringUtils.isNotBlank(text)){
                DelayedMailNotification.favorite(flash, text);
            }
        }

        return ok();
    }

    /**
     * To simplify genid : fill name and ajax request to get id
     * @param tribuName
     * @return
     */
    public static Result getTribuId(String tribuName){
        return ok(Tribu.findByName(tribuName).id+"");
    }

}
