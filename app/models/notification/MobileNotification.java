package models.notification;

import play.db.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by juliend on 24/09/14.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class MobileNotification  extends Model {

    @Id
    public UUID id;

    @Column
    public Date sentDate;

    public UUID getId(){
        return this.id;
    }

    public void setId(UUID id){
        this.id = id;
    }

    protected static Finder<UUID, MobileNotification> finder = new Finder<UUID, MobileNotification>(UUID.class, MobileNotification.class);


}
