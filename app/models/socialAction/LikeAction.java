package models.socialAction;

import models.User;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class LikeAction extends SocialAction {

	@ManyToOne
	@JoinColumn(name = "friend_id")
	public User liker;

	public LikeAction(final User liker) {
		this.liker = liker;
	}


}
