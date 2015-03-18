package models.comment;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import models.User;
import models.totem.Flash;
import models.totem.Tribu;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Thread extends Model {

	@Id
	UUID id;

	@Required
	Date lastUpdate;

	@OneToMany(cascade = CascadeType.ALL)
	List<Comment> comments;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="tribu_id")
    public Tribu tribu;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="flash_id")
    public Flash flash;

	public Thread() {
		this.lastUpdate = new Date();
	}
	public Thread(Tribu tribu) {
		this.lastUpdate = new Date();
		this.tribu = tribu;
	}
	public Thread(Flash flash) {
		this.lastUpdate = new Date();
		this.flash= flash;
	}


	protected static Finder<UUID, Thread> finder = new Finder<UUID, Thread>(
		UUID.class, Thread.class);

	public UUID getId(){
		return this.id;
	}

	public Date getLastUpdate(){
		return this.lastUpdate;
	}

	public void setLastUpdate(final Date lastUpdate){
		this.lastUpdate = lastUpdate;
	}

	public List<Comment> getComments(){
		return this.comments;
	}

	public static Thread findById(final String id){
		if (id == null) {
			return null;
		}
		try {
			return finder.byId(UUID.fromString(id));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public Comment getLastComment(){
		return Comment.getLastCommentForThread(this.getId());
	}

	public List<Comment> getLastComments(final int nbMaxComments){
		return Comment.getLastCommentsForThread(this.getId(), nbMaxComments);
	}

	public static List<Thread> getTenLastThreadOfUser(final User user){
		String sql = "SELECT thread.id AS threadId, thread.saction_id, thread.last_update, thread.activity_id "
				+ "FROM thread "
				+ "JOIN followed_thread ON thread.id=followed_thread.thread_id "
				+ "WHERE followed_thread.user_id=:userId "
				+ "AND followed_thread.user_id NOT IN ( "
					+ " SELECT comment.user_id FROM comment " 
					+ " WHERE comment.thread_id = thread.id "
					+ " ORDER BY comment.last_update DESC LIMIT 1)"				+ "AND thread.last_update <= followed_thread.last_see "
				+ "ORDER BY thread.last_update DESC "
				+ "LIMIT 10";

		SqlQuery sqlQuery = Ebean.createSqlQuery(sql)
			.setParameter("userId", user.id);

		List<SqlRow> sqlRows = sqlQuery.findList();
		List<Thread> result = new ArrayList<Thread>();
		for (SqlRow sqlRow : sqlRows) {
			result.add(Thread.findById(sqlRow.getString("threadId")));
		}

		return result;
	}

	public static List<Thread> getLastUpdatedThreadForUser(final User user){
		String sql = "SELECT thread.id AS threadId, thread.saction_id, thread.last_update, thread.activity_id "
				+ "FROM thread "
				+ "JOIN followed_thread ON thread.id=followed_thread.thread_id "
				+ "WHERE followed_thread.user_id=:userId "
				+ "AND followed_thread.user_id NOT IN ( "
					+ " SELECT comment.user_id FROM comment " 
					+ " WHERE comment.thread_id = thread.id "
					+ " ORDER BY comment.last_update DESC LIMIT 1)"
				+ "AND thread.last_update > followed_thread.last_see "
				+ "ORDER BY thread.last_update DESC "
				+ "LIMIT 10";

		SqlQuery sqlQuery = Ebean.createSqlQuery(sql)
			.setParameter("userId", user.id);

		List<SqlRow> sqlRows = sqlQuery.findList();
		List<Thread> result = new ArrayList<Thread>();
		for (SqlRow sqlRow : sqlRows) {
			result.add(Thread.findById(sqlRow.getString("threadId")));
		}

		return result;
	}

    public static Thread threadForFlash(Flash flash){
        List<Thread> threadList = finder.where().eq("flash_id", flash.id).findList();
        if(!threadList.isEmpty()){
            return threadList.get(0);
        }
        return null;
    }
}