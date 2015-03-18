package models.notification;

import external_services.Facebook;
import models.User;
import models.comment.Thread;
import models.totem.Flash;
import models.totem.Tribu;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class DelayedMailNotification extends Model {
	
	@Id
	public UUID id;

	@ManyToOne
	public User sender;

	@ManyToOne
	public User receiver;

	@ManyToOne
	public Tribu tribu;

	@ManyToOne(cascade= CascadeType.ALL)
	public Flash flash;

	@Column
	public String type;

	@Column
	public Date dateSaved;

	@Column
	public int state;

    @Column
    public String data;

    protected static Finder<UUID, DelayedMailNotification> finder = new Finder<UUID, DelayedMailNotification>(UUID.class, DelayedMailNotification.class);


    public static void newFollower(final User user, final String threadId){
		Thread thread = Thread.findById(threadId);
		Logger.info("new follower "+user+ " of totem " +thread.tribu);
		if(!NotificationPreference.isSubscribed(thread.tribu.owner, NotificationPreference.newFollowerOfYourTotem)){
			Logger.info("not subscribed");
			return;
		}

		if(finder.where().eq("type","newFollower").eq("sender_id",user.id).eq("tribu_id",thread.tribu.id).findUnique() ==null ) {
			Logger.info("new follower "+user+ " of totem " +thread.tribu);
			DelayedMailNotification sendMail = new DelayedMailNotification();
			sendMail.sender = user;
			sendMail.receiver = thread.tribu.owner;
			sendMail.tribu = thread.tribu;
			sendMail.type = "newFollower";
			sendMail.dateSaved = new Date();
			sendMail.state = 0;
			sendMail.save();
		}else{
			Logger.info("already sent");
		}

	}

	public static void newLikeTotem(final User user, final Tribu tribu){
		Logger.info("newLikeTotem "+user+ " of totem " + tribu.name);
		if(!NotificationPreference.isSubscribed(tribu.owner, NotificationPreference.newLikeOnYourTotem)){
			Logger.info("not subscribed");
			return;
		}

		if(finder.where().eq("type","newLikeTotem").eq("sender_id",user.id).eq("tribu_id", tribu.id).findUnique() ==null ) {
			DelayedMailNotification sendMail = new DelayedMailNotification();
			sendMail.sender = user;
			sendMail.receiver = tribu.owner;
			sendMail.tribu = tribu;
			sendMail.type = "newLikeTotem";
			sendMail.dateSaved = new Date();
			sendMail.state = 0;
			sendMail.save();
		}else{
			Logger.info("already sent");
		}

	}

	public static void newLikeFlash(final User user, final Flash flash){
		Logger.info("newLikeFlash "+user+ " of flash " +flash.id);
		if(!NotificationPreference.isSubscribed(flash.getFlasher(), NotificationPreference.newLikeOnYourFlash)){
			Logger.info("not subscribed");
			return;
		}
		
		if(finder.where().eq("type","newLikeFlash").eq("sender_id",user.id).eq("flash_id",flash.id).findUnique() ==null ) {
			DelayedMailNotification sendMail = new DelayedMailNotification();
			sendMail.sender = user;
			sendMail.receiver = flash.getFlasher();
			sendMail.flash = flash;
			sendMail.tribu = flash.tribu;
			sendMail.type = "newLikeFlash";
			sendMail.dateSaved = new Date();
			sendMail.state = 0;
			sendMail.save();
		}else{
			Logger.info("already sent");
		}

	}
    
    public static void favorite(final Flash flash, String text){
        Logger.info("favoritedFlash " +flash.id);

        //todo add to notif settings a boolean

        if(finder.where().eq("type","favoritedFlash").eq("flash_id",flash.id).findUnique() ==null ) {
            DelayedMailNotification sendMail = new DelayedMailNotification();
            sendMail.sender = flash.tribu.owner;
            sendMail.receiver = flash.getFlasher();
            sendMail.flash = flash;
            sendMail.tribu = flash.tribu;
            sendMail.data = text;
            sendMail.type = "favoritedFlash";
            sendMail.dateSaved = new Date();
            sendMail.state = 0;
            sendMail.save();
        }else{
            Logger.info("already sent");
        }

    }


				
	public static void send(){
        for(DelayedMailNotification sendMail : finder.where().eq("state", 0).findList()) {
        	if(sendMail.type.equals("newFollower")){
	        	sendMail.state=1;
	        	sendMail.save();
                services.MailService.newFollower(sendMail.receiver, sendMail.sender, sendMail.tribu);
                Logger.info("mail follow from "+ sendMail.sender.email+" to " + sendMail.receiver.email +" about totem " + sendMail.tribu.name);
			}else if(sendMail.type.equals("newLikeFlash")){
	        	sendMail.state=1;
	        	sendMail.save();
                Facebook.sendLikeFlashNotification(sendMail.sender, sendMail.flash, sendMail.tribu);
                Logger.info("mail newLikeFlash from "+ sendMail.sender.email+" to " + sendMail.receiver.email +" about totem " + sendMail.tribu.name);
			}else if(sendMail.type.equals("newLikeTotem")){
	        	sendMail.state=1;
	        	sendMail.save();
                Facebook.sendLikeTotemNotification(sendMail.sender, sendMail.tribu);
                Logger.info("mail newLikeTotem from "+ sendMail.sender.email+" to " + sendMail.receiver.email +" about totem " + sendMail.tribu.name);
        	}
            else if(sendMail.type.equals("favoritedFlash")){
                sendMail.state=1;
                sendMail.save();
                Facebook.sendFavoritedFlashNotification(sendMail.tribu.getOwner(), sendMail.flash, sendMail.tribu, sendMail.data);
                Logger.info("mail favoritedFlash from "+sendMail.tribu.getOwner().email + " to " + sendMail.receiver.email +" about totem " + sendMail.tribu.name);
            }

        }
	}

}