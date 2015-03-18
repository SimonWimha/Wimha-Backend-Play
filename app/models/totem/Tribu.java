package models.totem;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import models.Picture;
import models.User;
import models.comment.Thread;
import play.data.validation.ValidationError;
import play.db.ebean.Model;
import play.i18n.Messages;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Pattern;

@Entity
@Table(name="tribu")
public class Tribu extends Model {

	@Id
    public UUID id;

    @Column
    public UUID token;

    @ManyToOne
    public User owner;

    @Column(unique=true)
    public String name;

    @Column
    public String owner_mail;

    @Column
    public String owner_name;

    @OneToOne
    public Picture picture;

    @OneToOne
    public Picture backgroundPicture;

    @OneToOne(mappedBy = "tribu")
    public Thread thread;

    @Column
    public String question;
    
    @Column
    public String description;

    @Column
    public Date birthdate;

    @Column
    public String owner_vcard_email;

    @Column
    public String owner_vcard_facebook;

    @Column
    public String owner_vcard_twitter;

    @Column
    public Boolean post_fb;

    @Column
    public Boolean daily_digest;

    @OneToMany
    public List<Flash> flashs;

    @OneToMany
    public List<Member> members;

    protected static Finder<UUID, Tribu> finder = new Finder<UUID, Tribu>(UUID.class, Tribu.class);

    public Tribu(){
        this.daily_digest=true;
    }

    public boolean isEmptyVCard(){
        if(this.owner_vcard_twitter !=null && !this.owner_vcard_twitter.isEmpty()){
            return false;
        }
        if(this.owner_vcard_facebook !=null && !this.owner_vcard_facebook.isEmpty()){
            return false;
        }
        if(this.owner_vcard_email !=null && !this.owner_vcard_email.isEmpty()){
            return false;
        }
        return true;
    }

    public Tribu(String name, String owner_mail, String question) {
        this.name = name;
        this.owner_mail = owner_mail;
        this.question = question;
        this.birthdate = new Date();
        this.daily_digest=true;
    }

    public User getOwner(){
        return this.owner;
    }

    public models.comment.Thread getThread(){
        if(this.thread==null){
            this.thread=new Thread(this);
            this.thread.save();
            this.save();
        }
        return this.thread;

    }

    public static Tribu findByName(final String name){
        String sql = "select id, name from tribu where name ilike :name";

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("name", name);

        // execute the query returning a List of MapBean objects
        List<SqlRow> list = sqlQuery.findList();

        return list.isEmpty() ? null : Tribu.findById(list.get(0).getString("id"));//finder.where().eq("name", name).findUnique();
    }
    
    public static List<Tribu> findAll(){
        return finder.all();
    }

    public static List<Tribu> findAllActivated(){
        return finder.where().isNotNull("owner_id").findList();

    }
   
    public static List<Tribu> findByOwner(User owner){
        if (owner==null) {
            return new ArrayList<Tribu>();
        }
        List<Tribu> res=finder.where().eq("owner_id", owner.id).findList();
        if(res!=null){
            return res;
        }else{
            return new ArrayList<Tribu>();
        }
    }

    public static Tribu findById(final String id){
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

    public static Tribu findByToken(final String token){
        if (token == null) {
            return null;
        }
        try {
            return finder.where().eq("token",UUID.fromString(token)).findUnique();
        } catch (IllegalArgumentException e) {
            // malformed UUID
            return null;
        }
    }


    public static List<Tribu> getDailyDigestTotems() {
        return finder.where().eq("daily_digest",true).findList();
    }

    public void setId(UUID id){
     this.id=id;
    }

    @Override
    public String toString(){
        return name;
    }


    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

    private static final Pattern pseudo = Pattern.compile("[A-Za-z]+");

    public Map<String, List<ValidationError>> validate() {

        Map<String, List<ValidationError>> errorList = new HashMap<String, List<ValidationError>>();

        if (owner_mail==null || owner_mail.equals("")) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("owner_mail", Messages.get("error.email.empty") , null));
            errorList.put("owner_mail", toErrors);
        } else if (!rfc2822.matcher(owner_mail).matches()) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("owner_mail", Messages.get("error.email.invalid") , null));
            errorList.put("owner_mail", toErrors);
        } 
        if (name==null || name.isEmpty()) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("name", Messages.get("error.totemName.empty") , null));
            errorList.put("name", toErrors);
        }  else if (findByName(name)!=null) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("name", Messages.get("error.totemName.alreadyTaken") , null));
            errorList.put("name", toErrors);
        } else if (!pseudo.matcher(name).matches()){
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("name", Messages.get("error.totemName.invalid") , null));
            errorList.put("name", toErrors);
        }
        if (question==null || question.trim().isEmpty()) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("question", Messages.get("error.totemQuestion.empty") , null));
            errorList.put("question", toErrors);
        } 
        if (owner_name==null || owner_name.isEmpty()) {
            List<ValidationError> toErrors = new ArrayList<ValidationError>();
            toErrors.add(new ValidationError("owner_name", Messages.get("error.name.empty") , null));
            errorList.put("owner_name", toErrors);
        }   

        if (errorList.isEmpty()) {
            return null;
        } else {
            return errorList;
        }
    }

}