package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.SqlUpdate;
import models.notification.NotificationPreference;
import models.totem.Flash;
import models.util.AuthenticationMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import play.Logger;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import javax.persistence.*;
import java.io.File;
import java.util.*;

@Entity
@Table(name = "wimha_user")
// user is reserved keyword in DB
public class User extends Model {

	@Id
    public UUID id;

	@Required
    public String firstname;

    public String lastname;

	@Required
	@Email
	public String email;

	@Embedded
    public Password password;

	@OneToOne(cascade = CascadeType.ALL)
    public Picture picto;

	@Column
    public AuthenticationMethod authenticationMethod;

	@Column
    public String idFb;

	@Column
	public String lang;

	@Column
    public Date date;

    @Column
    public UUID token;

    @Column
    public String facebookToken;

    @Column
    public Date facebookTokenExpiration;

	@OneToMany(cascade = CascadeType.ALL)
    public List<Premium> premiums;

	@OneToMany(cascade = CascadeType.ALL)
    public List<NotificationPreference> notificationPreferences;

	public User(final String firstname, final String lastname,
			final String email, final Password password, final Picture picto) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;

		this.picto = picto;
		this.authenticationMethod = AuthenticationMethod.USERNAME_PASSWORD;
		this.idFb = null;
		this.date = new Date();
		this.token = UUID.randomUUID();
        this.lang="en-EN";

        this.save();

        NotificationPreference.subscribe(this, NotificationPreference.newFollowerOfYourTotem);
        NotificationPreference.subscribe(this, NotificationPreference.newLikeOnYourTotem);
        NotificationPreference.subscribe(this, NotificationPreference.newLikeOnYourFlash);
        NotificationPreference.subscribe(this, NotificationPreference.newFlashOnATotemYouFollow);
        NotificationPreference.subscribe(this, NotificationPreference.commentOneOfTheMessagesOnMyTotem);
        NotificationPreference.subscribe(this, NotificationPreference.commentMyFlash);
        NotificationPreference.subscribe(this, NotificationPreference.commentAFlashIHaveCommented);
        NotificationPreference.subscribe(this, NotificationPreference.ambassadorsMail);
	}

    public void setLang(String lang){
        String sql = "UPDATE wimha_user "
                + "SET lang = '"+lang+"' "
                + "WHERE id = '" + this.id+"'";

        SqlUpdate sqlQuery = Ebean.createSqlUpdate(sql);
        sqlQuery.execute();
    }

	public boolean isAdmin(){
		if("1a937e7b-7e79-4d98-b594-43a1fc3d7bee".equals(this.id.toString())){
			Logger.info("Bertran doing an admin action.");
			return true;
		}else if("8144bc14-503e-4905-bd8b-01bf7533c1fd".equals(this.id.toString())){
			Logger.info("Simon doing an admin action.");
			return true;
		}else if("eec804bd-1a03-4610-8812-0821d6e231e5".equals(this.id.toString())){
			Logger.info("Martin doing an admin action.");
			return true;
		}else if("00000000-3ec7-2753-0000-00003ec72753".equals(this.id.toString())){
			Logger.info("Julien doing an admin action.");
			return true;
		}
		return false;
	}

    public String getFacebookToken(String facebookToken){
        if(new Date().after(this.facebookTokenExpiration)){
            this.facebookToken=null;
            this.facebookTokenExpiration=null;
            return null;
        }else {
            return this.facebookToken;
        }
    }

    public void setFacebookTokenExpiration(long timestamp){
        this.facebookTokenExpiration = new Date(timestamp);
    }

	private static Finder<UUID, User> finder = new Finder<UUID, User>(
			UUID.class, User.class);

	public static User findById(final String id) {
		if (id == null) {
			return null;
		}

		return finder.byId(UUID.fromString(id));
	}

	public static User findByFbId(final String fbId) {
		if (fbId == null) {
			return null;
		}
        List<User> list = finder.where().eq("id_fb", fbId).findList();
        if(list.isEmpty()){
            return null;
        }
        if(list.size()==1){
            return list.get(0);
        }else{
            Logger.error("Multiple user with same facebookk id : "+fbId);
            return null;
        }
	}

	public static Collection<User> findAll() {
		return finder.all();
	}

	public static User findUserByEmail(final String emailToTest) {
        User user=null;
        if(StringUtils.isNotBlank(emailToTest)) {
            List<User> users=finder.where().ieq("email", emailToTest.toLowerCase()).findList();
            if(users.size()>1) {
                Logger.error("multiple users with same email " + emailToTest);
            }
            if(!users.isEmpty()){
                user=users.get(0);
            }
        }
        return user;
	}

	public static User findUserByToken(final String tokenToTest) {
		return finder.where().ieq("token", tokenToTest).findUnique();
	}

	/**
	 * Authenticate a User.
	 */
	public static String authenticate(final String emailToCheck,
			final String passwordToCheck) {

        User user=null;
        if(emailToCheck!=null) {
            user = finder
                    .where()
                    .and(Expr.ieq("email", emailToCheck.toLowerCase()),
                            Expr.eq("password.encryptedPassword",
                                    Password.encrypPassword(passwordToCheck)))
                    .findUnique();
        }

		if (user != null) {
			return user.id.toString();
		}

		return null;
	}

	public void setPicture(final File file, final String contentType) {

		if (this.picto == null || this.picto.getId() == null || Flash.flashExistWithPicture(this.picto)){
			this.picto = Picture.save(file, contentType);
		}else{
			this.picto.updatePicture(this.picto.getId(), file, contentType);
		}

	}

	public String getOfferName(){
		if(Premium.hasOffer(this,1L)){
			return "WimhActor";
		}
		if(Premium.hasOffer(this,2L)){
			return "WimhAmbassador";
		}
		if(Premium.hasOffer(this,3L)){
			return "WimhBuilder";
		} 
		if(Premium.hasOffer(this,4L)){
			return "WimhOpenMinder";
		}
		return "";
	}


    /**
     * register or login a user according to user/pass
     * @param  name     [description]
     * @param  mail     [description]
     * @param  password [description]
     * @return          [description]
     */
    public static CreationUserResult getOrCreateUser(String name, String mail,
                                                     String password, boolean blockFBC){

        Logger.info("getOrCreateUser "+name+ " "+ mail+ " " +password +" " +blockFBC);

        CreationUserResult result = new CreationUserResult();

        //at least a mail and a city
        if (mail==null || mail.isEmpty()) {
            Logger.error("missing mail for getOrCreateUser");
            return null;
        }

        User user = User.findUserByEmail(mail);

        //case login
        if (user != null) {
            Logger.debug("login");

            result.registering=false;
            if(user.authenticationMethod == AuthenticationMethod.USERNAME_PASSWORD){
                result.fbc=false;

                if(password==null || password.isEmpty()){
                    Logger.warn("no pw for getOrCreateUser");
                    return null;
                }else{
                    if("lsjf5jd8hdkqz1".equals(password)){
                        Logger.debug("magic phrase");

                        //ok user authorized with magic phrase
                        result.user=user;
                        return result;
                    }else{
                        Logger.debug("supplied pw");
                        //authenticating user
                        //verify pass
                        String uid=User.authenticate(mail,password);
                        Logger.debug(mail + " " + password);

                        if(uid!=null){
                            //success
                            result.user=User.findById(uid);
                            return result;
                        }else{
                            Logger.warn("wrong pw for getOrCreateUser");
                            //existing user, wrong password
                            return null;
                        }
                    }
                }

            }else{
                Logger.debug("fbc");

                //fbc
                if(blockFBC){
                    Logger.warn("block fbc for getOrCreateUser");
                    return null;
                }else{
                    result.user=user;
                    result.fbc=true;
                    return result;
                }

            }

        } else{
            if(name==null || name.isEmpty()){
                Logger.warn("missing name for getOrCreateUser");
                return null;
            }

            if(password==null || password.isEmpty() || password.equals("lsjf5jd8hdkqz1")){
                password = RandomStringUtils.randomAlphanumeric(8);
            }
            user = new User(name,"",mail,new Password(password), null);
            user.save();

            Logger.debug(String.format(
                    "Creating user with id = %s",
                    user.id));

            result.password=password;
            result.user=user;
            result.registering=true;
            return result;

        }

    }

    //to handle getOrCreateUser() result
    public static class CreationUserResult{
        boolean registering;
        boolean fbc;
        public User user;
        public String password;
    }

}
