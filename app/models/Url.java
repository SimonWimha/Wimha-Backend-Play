package models;

import models.totem.Member;
import models.totem.Tribu;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by juliend on 12/08/14.
 */
@Entity
@Table(name="url")
public class Url extends Model{

    @Id
    public Long id;

    @Column
    public String shortId;

    @Column
    public long type;

    @ManyToOne
    public Tribu tribu;

    @OneToOne
    public Member member;

    @Column
    public String targetUrl;


    protected static Model.Finder<Long, Url> finder = new Model.Finder<Long, Url>(Long.class, Url.class);


    public static Tribu findTribuByShortId(String shortId){
        return finder.where().eq("short_id", shortId).findUnique().tribu;
    }

    public static Member findMemberByShortId(String shortId) {
        return finder.where().eq("short_id", shortId).findUnique().member;
    }

}
