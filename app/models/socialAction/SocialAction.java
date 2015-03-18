package models.socialAction;

import org.joda.time.DateTime;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SocialAction extends Model {

	@Id
	public Long id;

	@Column
	public DateTime date;


	public SocialAction() {
		date = new DateTime();
	}


	public String getDate() {
		return date.dayOfMonth().getAsText() + " " + date.monthOfYear().getAsText() + " " + date.yearOfCentury().getAsText();
	}

	protected static Finder<Long, SocialAction> finder = new Finder<Long, SocialAction>(
			Long.class, SocialAction.class);

	public static SocialAction findSocialActionById(final Long id) {
		return finder.byId(id);
	}

	public static Collection<SocialAction> findAll() {
		return finder.all();
	}

	public static SocialAction findById(final String id) {
		if (id == null) {
			return null;
		}
		try {
			return finder.byId(Long.valueOf(id));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

    public Long getId(){
        return this.id;
    }

}
