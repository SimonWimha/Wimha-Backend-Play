package models.socialAction;

import models.User;
import models.totem.Tribu;
import play.Logger;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.List;

@Entity
@DiscriminatorValue("LikeTotem")
public class LikeTotemAction extends LikeAction {

	@ManyToOne
	public Tribu tribu;

	public LikeTotemAction(final User liker, final Tribu tribu) {
		super(liker);
		this.tribu = tribu;
	}

	protected static Finder<Long, LikeTotemAction> finder = new Finder<Long, LikeTotemAction>(
			Long.class, LikeTotemAction.class);

	public static LikeTotemAction alreadyLiked(final User thanker, Tribu tribu) {
		try{
			List<LikeTotemAction> list=finder.where().eq("friend_id", thanker.id)
					.eq("tribu", tribu)
					.findList();
			if(list.size()>0){
				return list.get(0);
			}else{
				return null;
			}
		}catch(Exception e){
			Logger.error("Exception querying already liked totem ", e);
			return null;
		}
	}

	public static LikeTotemAction like(final User thanker, Tribu tribu) {
		LikeTotemAction res=new LikeTotemAction(thanker, tribu);
		res.save();
		return res;
	}

	public static void unlike(User user, String totem_id) {
		List<LikeTotemAction> list=finder.where().eq("friend_id", user.id)
				.eq("tribu_id", totem_id)
				.findList();
		for(LikeTotemAction like : list){
			like.delete();
		}
	}

	public static int countLikesForATotem(final Tribu tribu) {
		return finder.where().eq("tribu_id", tribu.id).findRowCount();
	}
}
