package models.notification;

import models.User;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.UUID;

@Entity
public class NotificationPreference extends Model {
	// if we want to change a notification name, then change it in the DB, the JS and Scala part ! ! !
	public static String newFollowerOfYourTotem = "newFollowerOfYourTotem";
	public static String newLikeOnYourTotem = "newLikeOnYourTotem";
	public static String newLikeOnYourFlash = "newLikeOnYourFlash";
	public static String newFlashOnATotemYouFollow = "newFlashOnATotemYouFollow";
	public static String commentOneOfTheMessagesOnMyTotem = "commentOneOfTheMessagesOnMyTotem";
	public static String commentMyFlash = "commentMyFlash";
    public static String commentAFlashIHaveCommented = "commentAFlashIHaveCommented";
    public static String ambassadorsMail = "ambassadorsMail";

	@Id
	public UUID id;

	@ManyToOne
	private User user;

	@Column
	private String group_name;


	public NotificationPreference(final User user, final String groupName) {
		this.user=user;
		this.group_name=groupName;
	}

	protected static Finder<UUID, NotificationPreference> finder = new Finder<UUID, NotificationPreference>(UUID.class, NotificationPreference.class);
	public static boolean isSubscribed(final User user, final String groupName){
		if(user==null){
			return false;
		}
		try{
			List<NotificationPreference> notifs = finder.where()
			.eq("t0.user_id", user.id)
			.eq("t0.group_name", groupName)
			.findList();
			if(notifs!=null && notifs.size()>0){
				return true;
			}
		} catch(Exception e){
            Logger.error("Exception querying settings in DB", e);
        }
		return false;
	}

	public static boolean subscribe(final User user, final String groupName){
		List<NotificationPreference> notifs = finder.where()
		.eq("t0.user_id", user.id)
		.eq("t0.group_name", groupName)
		.findList();
		if(notifs.size()>0){
			return false;
		} else{
			new NotificationPreference(user,groupName).save();
			return true;
		}

	}

	public static boolean unsubscribe(final User user, final String groupName){
		List<NotificationPreference> notifs = finder.where()
		.eq("t0.user_id", user.id)
		.eq("t0.group_name", groupName)
		.findList();
		if(notifs.size()>0){
			for(NotificationPreference notif:notifs){
				notif.delete();
			}
			return true;
		} else{
			return false;
		}

	}


}