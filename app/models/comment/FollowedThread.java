package models.comment;

import com.avaje.ebean.Expr;
import models.User;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="followed_thread")
public class FollowedThread extends Model {
	private static final long serialVersionUID = 785986356L;
	
	@Id
	private UUID id;

	@Required
	@ManyToOne
	@JoinColumn(name="user_id", referencedColumnName="id")
	private User user;

	@Required
	@ManyToOne
	@JoinColumn(name="thread_id", referencedColumnName="id")
	private Thread thread;

	@Required
	private Date lastSee;

	public FollowedThread(final User user, final Thread thread){
		this.user = user;
		this.thread = thread;
		this.lastSee = new Date();
	}

	protected static Finder<UUID, FollowedThread> finder = new Finder<UUID, FollowedThread>(
		UUID.class, FollowedThread.class);

	public UUID getId(){
		return this.id;
	}

	public User getUser(){
		return this.user;
	}

	public Thread getThread(){
		return this.thread;
	}

	public Date getLastSee(){
		return this.lastSee;
	}

	public void setLastSee(final Date lastSee){
		this.lastSee = lastSee;
	}

	public static boolean isSubscribe(final User user, final Thread thread){
		if(user !=null && thread !=null && finder.where().and(Expr.ieq("t0.user_id", user.id.toString()), Expr.eq("t0.thread_id", thread.getId())).findUnique() != null){
			return true;
		}else{
			return false;
		}
	}

	public static void subscribeUser(final User user, final Thread thread){
		FollowedThread fThread = new FollowedThread(user, thread);
		fThread.save();
	}

	public static void unSubscribeUser(final User user, final Thread thread){
		FollowedThread fThread = finder.where().and(Expr.ieq("t0.user_id", user.id.toString()), Expr.eq("t0.thread_id", thread.getId())).findUnique();
		fThread.delete();
	}

	public static void updateLastSee(final User user, final Thread thread){
		try{
			FollowedThread fThread = finder.where().and(Expr.ieq("t0.user_id", user.id.toString()), Expr.eq("t0.thread_id", thread.getId())).findUnique();
			fThread.setLastSee(new Date());
			fThread.save();			
		} catch(Exception e){
			Logger.error("Cant updateLastSee : Exception", e);
		}

	}

	public static List<FollowedThread> getFollowedThreadByUser(final User user){
		return finder.where().eq("t0.user_id", user.id).findList();
	}

    public static List<FollowedThread> getFollowedTotemThreadByUser(final User user){
        return finder.where().eq("t0.user_id", user.id).isNotNull("thread.tribu").findList();
    }

	public static FollowedThread getFollowedThreadByUserAndThread(final Thread thread, final User user){
		return finder.where().eq("t0.user_id", user.id).eq("t0.thread_id", thread.getId()).findUnique();
	}

	public static List<FollowedThread> getSubscribedUserByThread(final Thread thread){
		return finder.where().eq("t0.thread_id", thread.getId()).findList();
	}
}