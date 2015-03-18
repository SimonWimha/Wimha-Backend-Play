package models;

import org.joda.time.DateTime;
import play.api.libs.Crypto;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
public class Cookie extends Model{

    //Name of the cookie in the browser
    public final static String cookieName="id";

    //Duration of the session in month
    public final static int cookieMaxAge=3;

	@Id
	public String id;

    @Column
    public String userId;

    @Column
    public String provider;

    @Column
    public Date creationDate;

    @Column
    public Date lastUsed;

    @Column
    public Date expirationDate;

	public Cookie() {
        id = Crypto.sign("");
	}


    private static Model.Finder<UUID, Cookie> finder = new Model.Finder<UUID, Cookie>(UUID.class, Cookie.class);

    public static String fromRequest(String idInSession) {
        Cookie cookie = finder.where().eq("id", idInSession).findUnique();
        if(cookie!=null) {
            return cookie.userId;
        }
        return null;
    }

    public static String fromUser(User user, String provider) {
        Cookie cookie=new Cookie();
        cookie.id = Crypto.sign(user.id+"");

        //Remove eventual existing cookies
        List<Cookie> existingCookies = finder.where().eq("id", cookie.id).findList();
        if(existingCookies.size()>0){
            existingCookies.get(0).delete();
        }

        cookie.userId = user.id+"";
        cookie.provider = provider;
        cookie.creationDate = new Date();
        cookie.lastUsed = new Date();
        cookie.expirationDate = DateTime.now().plusMonths(cookieMaxAge).toDate();
        cookie.save();
        return cookie.id;
    }

    public static void remove(String idInSession) {
        Cookie cookie = finder.where().eq("id", idInSession).findUnique();
        if(cookie!=null) {
            cookie.delete();
        }
    }
}
