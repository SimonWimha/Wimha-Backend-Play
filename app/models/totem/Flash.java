package models.totem;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import models.Picture;
import models.User;
import models.comment.Thread;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Model;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Entity
public class Flash extends Model {

	@Id
    public UUID id;

    @Column
    public UUID token;

    @ManyToOne
    @Column(name = "user_id")
    public User flasher;

    @ManyToOne
    public Tribu tribu;

    @OneToOne(mappedBy = "flash")
    public Thread  thread;

    @Column
    @Required
    public String name;

    @Column
    @Required
    public String mail;

    @Column
    @Required
    public String message;

    @Column
    public String date_post;

    @Column
    @Required
    public String lat;

    @Column
    @Required
    public String lon;

    @Column
    public String address;

    @Column
    public String city;

    @Column
    public String country;

    @Column
    public String country_code;

    @Column
    public String question;

    @Column
    public Boolean favorite;

    @Column
    @OneToOne
    public Picture picture;

    protected static Finder<UUID, Flash> finder = new Finder<UUID, Flash>(UUID.class, Flash.class);

    @ManyToOne
    public Member member;

    public Flash(Tribu tribu) {
        this.tribu = tribu;
        this.token = UUID.randomUUID();
        this.question = new String(tribu.question);
    }
    
    public Flash(Tribu tribu, String mail, String name, String lat, String lon, String address,
                 String message) {
        this.tribu = tribu;
        this.mail = mail;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.address = address;
        this.message = message;
        this.date_post = new Date().getTime()+"";
        this.token = UUID.randomUUID();
        this.question = new String(tribu.question);

    }


    public models.comment.Thread getThread(){
        Thread thread=Thread.threadForFlash(this);
        if(thread==null){
            thread=new Thread(this);
            thread.save();
            this.thread=thread;
            this.save();
        }
        return thread;
    }

    public Tribu getTribu(){
        return this.tribu;
    }

    public User getFlasher(){
        return this.flasher;
    }
    public void setTribu(Tribu tribu){
        this.tribu = tribu;
    }
    public String getName(){
        return this.name;
    }
    public String getMessage(){
    	return this.message;
    }
    public String getMail(){
    	return this.mail;
    }
    public String getLat(){
    	return this.lat;
    }
    public String getLon(){
    	return this.lon;
    }
    public String getAddress(){
    	return this.address;
    }


    public static List<Flash> getFeed(final User user, final int page) {
        int limit = 4;
        List<Flash> result = new ArrayList<Flash>();

        if(user!=null) {
            String sql = "SELECT flash.id AS flashId "
                    + "FROM flash, tribu "
                    + "WHERE flash.token IS NULL "
                    + "AND tribu.id = flash.tribu_id "
                    + "AND EXISTS ( " +
                    "SELECT * FROM followed_thread WHERE followed_thread.user_id = :userId AND EXISTS ( " +
                    "SELECT * FROM thread WHERE thread.id = followed_thread.thread_id " +
                    "AND thread.tribu_id = tribu.id " +
                    ") " +
                    ")";

            sql += " GROUP BY flash.id ORDER by flash.date_post DESC LIMIT "
                    + limit + " OFFSET " + (limit * page);

            SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
            sqlQuery.setParameter("userId", user.id);

            List<SqlRow> sqlRows = sqlQuery.findList();

            for (SqlRow sqlRow : sqlRows) {
                Flash p = Flash.findById(sqlRow.getString("flashId"));
                if (p.member == null || p.member.blocked == null || !p.member.blocked) {
                    result.add(p);
                }
            }
        }

        return result;

    }


    public static List<Flash> getMyTP(final int page, Tribu tribu, boolean favorite) {
        int limit = 7;

        String sql = "SELECT flash.id AS flashId "
                + "FROM flash "
                + "WHERE flash.token IS NULL "
                + "AND tribu_id = :tribuId ";

        if(favorite){
            sql += "AND flash.favorite IS TRUE ";
        }


        sql += " GROUP BY flash.id ORDER by flash.date_post DESC LIMIT "
                + limit + " OFFSET " + (limit * page);

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("tribuId", tribu.id);

        List<SqlRow> sqlRows = sqlQuery.findList();

        List<Flash> result = new ArrayList<Flash>();
        for (SqlRow sqlRow : sqlRows) {
            Flash p= Flash.findById(sqlRow.getString("flashId"));
            if(p.member==null || p.member.blocked==null || !p.member.blocked) {
                result.add(p);
            }
        }

        return result;

    }


    public static String flashCountCacheKey(Tribu tribu){
        if(tribu!=null) {
            return "myTP" + tribu.id;
        }
        return null;
    }

    public static Integer flashCount(final Tribu tribu){
        Integer cachedList = (Integer) Cache.get(flashCountCacheKey(tribu));

        if(cachedList!=null){
            return cachedList;
        }

        int rowCount = finder.where().eq("t0.tribu_id", tribu.id).isNull("token").or(Expr.isNull("member.blocked"),Expr.eq("member.blocked", false)).findRowCount();

        Cache.set(flashCountCacheKey(tribu), rowCount);
        return rowCount;
    }

    /**
     * Get list of all tribes flash (used for map)
     * @param tribu
     * @return
     */
    public static List<Flash> findAll(final Tribu tribu){
        List<Flash> cachedList = null;//(List<Flash>) Cache.get(flashCountCacheKey(tribu));

        if(cachedList!=null){
            //return cachedList;
        }

        String sql = "SELECT flash.id AS flashId "
                + "FROM flash "
                + "WHERE tribu_id = :tribu_id "
                + "AND token IS NULL ";

        sql += " GROUP BY flash.id ORDER by flash.date_post DESC";

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("tribu_id", tribu.id);

        List<SqlRow> sqlRows = sqlQuery.findList();

        List<Flash> result = new ArrayList<Flash>();
        for (SqlRow sqlRow : sqlRows) {
            Flash p= Flash.findById(sqlRow.getString("flashId"));
            if(p.member==null || p.member.blocked==null || !p.member.blocked) {
                result.add(p);
            }
        }
        //Cache.set(flashCountCacheKey(tribu), result);
        return result;
    }

    /**
     * Used to ensure no duplicates in flash submit
     * @param tribu
     * @return
     */
    public static List<Flash> findAllEvenNotValidated(final Tribu tribu){
        return finder.where()
                .eq("t0.tribu_id", tribu.id)
                .orderBy("date_post desc")
                .findList();
    }
    public static Flash findById(final String id) {
        if (id == null) {
            return null;
        }
        try {
            return finder.byId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            // malformed UUID
            return null;
        }
    }

    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    public Map<String, List<ValidationError>> validate() {

        Map<String, List<ValidationError>> errorList = new HashMap<String, List<ValidationError>>();

        // if from before to
        if (mail==null || mail.equals("")) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("mail", "Please enter your email : )" , null));
            errorList.put("mail", toErrors);
        } else if (!rfc2822.matcher(mail.toLowerCase()).matches()) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("mail", "Are you sure about your mail address ?" , null));
            errorList.put("mail", toErrors);
        } 
        if (name==null || name.equals("")) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("name", "Please enter your name : )" , null));
            errorList.put("name", toErrors);
        } 

        if (message==null || message.equals("")) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("message", "Please enter your message : )" , null));
            errorList.put("message", toErrors);
        }


        if (errorList.isEmpty()) {
            return null;
        } else {
            return errorList;
        }
    }

    /**
     * Find last 24h flashs sorted per tribe
     * @return
     */
    public static HashMap<Tribu,  List<Flash>> getLastMessages(User user) {
        long nowMinus24Hrs = System.currentTimeMillis() -24*(1000*60*60);

        String sql = "SELECT flash.id AS flashId "
                + "FROM flash "
                + "WHERE token IS NULL "
                + "AND EXISTS (SELECT * FROM followed_thread, thread WHERE user_id = :userId AND thread_id = thread.id AND thread.tribu_id = flash.tribu_id)"
                + "AND to_number(date_post, '999999999999999') > :nowMinus24Hrs ";

        sql += " GROUP BY flash.id ORDER by flash.date_post DESC";

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("nowMinus24Hrs", nowMinus24Hrs);
        sqlQuery.setParameter("userId", user.id);

        List<SqlRow> sqlRows = sqlQuery.findList();

        HashMap<Tribu, List<Flash>> result = new HashMap<Tribu,  List<Flash>>();
        for (SqlRow sqlRow : sqlRows) {
            Flash flash = Flash.findById(sqlRow.getString("flashId"));
            if(flash.member==null || flash.member.blocked==null || !flash.member.blocked) {

                List<Flash> tribeFlashes = result.get(flash.tribu);
                if(tribeFlashes==null){
                    tribeFlashes = new ArrayList<>();
                    result.put(flash.tribu, tribeFlashes);
                }
                if(tribeFlashes.size()<4) {
                    if (flash.picture != null) {
                        tribeFlashes.add(0, flash);
                    } else {
                        tribeFlashes.add(flash);
                    }
                }
            }
        }

        return result;
    }

    public static Flash findSame(String mail, String message, Tribu tribu) {
        for(Flash flash: Flash.findAllEvenNotValidated(tribu)) {

            if (flash.mail.equals(mail) && flash.message.equals(message) && flash.tribu.equals(tribu)) {
                return flash;
            }
        }
        return null;
    }

    public static Flash getLastMessage(User user) {
        return finder.where()
                .eq("flasher_id", user.id)
                .orderBy("date_post desc")
                .findList().iterator().next();
    }

    public static boolean flashExistWithPicture(Picture picto) {
        return !finder.where().eq("picture_id", picto.id).findList().isEmpty();
    }


    public String getDate() {
        Date date = new Date(Long.valueOf(this.date_post.trim()));
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dat = dateFormat.format(date);
        return dat;
    }

    public static List<Flash> findFlashesOfUser(User user) {
        return finder.where().eq("flasher_id", user.id).findList();
    }
}