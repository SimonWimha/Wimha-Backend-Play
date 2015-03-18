package models.totem;

import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name="member")
public class Member extends Model {

	@Id
    public UUID id;

    @ManyToOne
    public User user;

    @ManyToOne
    public Tribu tribu;

    @Column
    public Boolean blocked;

    protected static Finder<UUID, Member> finder = new Finder<UUID, Member>(UUID.class, Member.class);


    public static void fill(String member_id, Flash flash) {
        Member member = Member.findById(member_id);
        if(member==null){
            Logger.warn("Member null but tribe has no members");
            if(!flash.tribu.members.isEmpty()) {
                Logger.error("Member null");
            }
            return;
        }
        if(member.tribu==null){
            Logger.error("Member "+member.id+" has no tribe");
            return;
        }
        if(!member.tribu.equals(flash.tribu)) {
            Logger.error("A member got to flash another tribe");
            Logger.error("Member id : "+member.id + " member tribe id : " +member.tribu.id );
            Logger.error("Flashing tribu :"+ flash.tribu.id );
            return;
        }

        Logger.info("Setting user field of member id : "+member.id);
        if(member.user==null) {
            Logger.info("Initializing member with user : "+ flash.flasher.id);
            member.user = flash.flasher;
            member.save();
        }else{
            Logger.info("Member already initialized with user : "+member.user.id);
        }

    }

    public static Member findById(String member_id) {
        if(StringUtils.isNotBlank(member_id)){
            return finder.byId(UUID.fromString(member_id));
        }
        return null;
    }

    public static List<Member> findUserMemberOfTribe(String userId){
        return finder.where().eq("user_id", userId).findList();
    }
}