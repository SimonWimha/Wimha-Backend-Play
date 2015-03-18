package models.notification;

import models.totem.Tribu;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * Store a notification send for question update
 * From a tribu
 * To all its members
 * Created by juliend on 24/09/14.
 */
@Entity
@DiscriminatorValue("MobileNotificationNewFlash")
public class MobileNotificationNewFlash extends MobileNotification {

    @ManyToOne
    public Tribu tribu;

    public static void add(Tribu tribu) {
        MobileNotificationNewFlash mobileNotificationQuestion = new MobileNotificationNewFlash();
        mobileNotificationQuestion.sentDate = new Date();
        mobileNotificationQuestion.tribu = tribu;
        mobileNotificationQuestion.save();
    }

    public static boolean alreadySentToday(Tribu tribu){
        LocalDate now = new LocalDate();
        LocalDate yesterday = now.minus(Period.hours(5));
        Date afterDate = yesterday.toDate();

        return finder.where().gt("sent_date", afterDate).eq("tribu_id", tribu.id).findList().size()>0;
    }
}