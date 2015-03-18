package models.comment;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Lists;
import models.User;
import models.totem.Flash;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.libs.Json;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.mvc.Controller;
import play.mvc.Result;

@Entity
@Table(name="comment")
public class Comment extends Model {
	private static final long serialVersionUID = 497625852364951673L;

	@Id
	private UUID id;
	
	@Required
	private Date createdAt;	

	@Required
	private Date lastUpdate;

	@Required
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="thread_id", referencedColumnName="id")
	private Thread thread;

	@ManyToOne
    @PrimaryKeyJoinColumn
	private User user;

	@Required
	private String message;
	@Required
	private String messageNoHtml;

	public Comment(final Thread thread, final User user,
	 final String message, final String messageNoHtml) {
		this.createdAt = new Date();
		this.lastUpdate = new Date();
		this.thread = thread;
		this.user = user;
		this.message = message;
		this.messageNoHtml = messageNoHtml;
	}

	public Comment(final Thread thread, final User user,
	 final String message) {
		this.createdAt = new Date();
		this.lastUpdate = new Date();
		this.thread = thread;
		this.user = user;
		this.message = message;
		this.messageNoHtml = message;
	}

	protected static Finder<UUID, Comment> finder = new Finder<UUID, Comment>(
		UUID.class, Comment.class);

	public UUID getId(){
		return this.id;
	}

	public Date getCreatedAt(){
		return this.createdAt;
	}

	public Date getLastUpdate(){
		return this.lastUpdate;
	}

	public void setLastUpdate(final Date lastUpdate){
		this.lastUpdate = lastUpdate;
	}

	public Thread getThread(){
		return this.thread;
	}

	public User getUser(){
		return this.user;
	}

	public String getMessage(){
		return this.message;
	}

	public String getMessageNoHtml(){
		return this.messageNoHtml;
	}
	public void setMessage(final String message){
		this.message = message;
	}

	public static List<Comment> getLastCommentsForRSS(){
		return finder.where()
		.orderBy("t0.created_at DESC")
		.setMaxRows(10)
		.findList();
	}

	public static Comment getLastCommentForThread(final UUID threadId){
		return finder.where().eq("t0.thread_id", threadId).orderBy("t0.created_at DESC").setMaxRows(1).findUnique();
	}

	public static List<Comment> getLastCommentsForThread(final UUID threadId, final int nbMaxComments){
		return Lists.reverse(finder.where().eq("t0.thread_id", threadId).orderBy("t0.created_at DESC").setMaxRows(nbMaxComments).findList());
	}

    public static Comment findById(String id) {
        return finder.byId(UUID.fromString(id));
    }


}