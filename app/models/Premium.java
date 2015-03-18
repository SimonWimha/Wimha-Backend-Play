package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Premium extends Model {
	private static final long serialVersionUID = 4625199658821567L;

	@Id
	public UUID id;

	@ManyToOne
	private User user;

	@Column
	private Long offer_id;

	@Column
	private Date dateFrom;

	@Column
	private Date dateTo;

	@Column
	private Integer nbInterests;


	private Premium(final User user, final Long offer,final int nbInterests) {
		this.user=user;
		this.offer_id=offer;
		this.dateFrom=new Date();
		this.nbInterests=nbInterests;

	}

	private Premium(final User user, final Long offer, final Date dateTo,final int nbInterests) {
		this.user=user;
		this.offer_id=offer;
		this.dateFrom=new Date();
		this.dateTo=dateTo;
		this.nbInterests=nbInterests;
	}
	
	public Date getDateFrom() {
		return this.dateFrom;
	}

	public Integer getNbInterests() {
		return this.nbInterests;
	}

	public static Premium subscribe(final User user, final Long offer,final int nbInterests){
		if(hasOffer(user,offer)){
			return null;
		} else{
			Premium res=new Premium(user,offer,nbInterests);
			res.save();
			return res;
		}
	}

	protected static Finder<UUID, Premium> finder = new Finder<UUID, Premium>(UUID.class, Premium.class);
	public static boolean hasOffer(final User user, final Long offer){
		Premium premium = finder.where()
		.eq("user_id", user.id)
		.eq("offer_id", offer)
		.findUnique();
		if(premium!=null){
			if(new Date().getTime()-premium.getDateFrom().getTime() > 0){
				return true;
			}
		}
		return false;
	}

	/*
	 * Time in s from to beginning of the offer to now
	 * @param  user  [description]
	 * @param  offer [description]
	 * @return       [description]
	 */
	public static long offerSince(final User user, final Long offer){
		Premium premium = finder.where()
		.eq("t0.user_id", user.id)
		.eq("t0.offer_id", offer)
		.findUnique();
		if(premium!=null){
			long ms=new Date().getTime()-premium.getDateFrom().getTime();
			return ms/1000;
		}
		return -1;
	}

	public static Integer findMaxInterests(final User user){
		List<Premium> premiums = finder.where()
		.eq("t0.user_id", user.id)
		.orderBy("t0.nb_interests, nb_interests desc")
		.findList();

		for(int i=0;i<premiums.size();i++){
			Premium premium=premiums.get(i);
			if(new Date().getTime()-premium.getDateFrom().getTime() > 0){
				if(premium.getNbInterests()!=null){
					return premium.getNbInterests();
				}
			}
		}
		return null;
	}

	public static int getMaxInterests(final User user){
		Integer res=findMaxInterests(user);
		if(res!=null){
			return res;
		}
		return 8;
	}

	/*
	 * Returns true for 6 days after the user has the offer
	 * @param  user [description]
	 * @return      [description]
	 */
	public static boolean displayNotyMaxInterest(final User user){
		if (hasOffer(user,1L) && (offerSince(user,1L)/3600) < 144){
			return true;
		}
		if (hasOffer(user,2L) && (offerSince(user,2L)/3600) < 144){
			return true;
		}
		if (hasOffer(user,3L) && (offerSince(user,3L)/3600) < 144){
			return true;
		}
		if (hasOffer(user,4L) && (offerSince(user,4L)/3600) < 144){
			return true;
		}
		return false;
	}
}