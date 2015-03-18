package models;

import external_services.Facebook;
import external_services.Twitter;
import models.totem.Flash;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.UUID;

/**
 * Created by juliend on 19/08/14.
 */
@Entity
public class PendingImageUpload extends Model {

    @Id
    public String signature;

    @OneToOne
    public Flash flash;

    @OneToOne
    public Picture picture;

    @Column
    public boolean logged;

    @Column
    public Boolean ok;

    @Column
    public String email;

    @Column
    public String twitterConsumerKey;

    @Column
    public String twitterConsumerSecret;

    @Column
    public String twitterAccessToken;

    @Column
    public String twitterAccessTokenSecret;

    @Column
    public String imageTweet;

    @Column
    public String tweet;

    @Column
    public String messageFacebookPost;

    @ManyToOne
    public User fbPoster;

    @Column
    public Boolean firstFlash;

    private static Finder<String, PendingImageUpload> finder = new Finder<String, PendingImageUpload>(
            String.class, PendingImageUpload.class);

    public static PendingImageUpload findBySignature(final String signature) {
        return finder.byId(signature);
    }
    public static PendingImageUpload findByFlash(final UUID flashId) {
        return finder.where().eq("flash_id", flashId.toString()).findUnique();
    }


    public static PendingImageUpload put(String signature, Flash flash, Picture flashPic) {
        PendingImageUpload pendingImageUpload= findBySignature(signature);

        if(pendingImageUpload==null){
            pendingImageUpload=new PendingImageUpload();
            pendingImageUpload.signature=signature;
        }

        pendingImageUpload.picture=flashPic;
        pendingImageUpload.flash =flash;
        pendingImageUpload.save();
        Logger.info("[Submit] Put signature "+signature);
        return pendingImageUpload;
    }

    public void storeValidationMail(Boolean logged, String email) {
        Logger.info("[Submit] storeValidationMail "+ this.flash.id.toString() + " " + logged+" " +email);
        this.logged=logged;
        this.email=email;
        this.save();
    }

    public void storeTweet(String tweet, String twitterAccessToken, String twitterAccessTokenSecret, String twitterConsumerKey, String twitterConsumerSecret, String picTweet) {
        this.tweet= tweet;
        this.twitterAccessToken=twitterAccessToken;
        this.twitterAccessTokenSecret=twitterAccessTokenSecret;
        this.twitterConsumerKey=twitterConsumerKey;
        this.twitterConsumerSecret=twitterConsumerSecret;
        this.imageTweet=picTweet;
        this.save();

    }

    public void storeFBPost(String message, User flasher) {
        this.messageFacebookPost=message;
        this.fbPoster=flasher;
        this.save();
    }

    public static PendingImageUpload get(String signature) {
        return findBySignature(signature);

    }

    public static void markOk(String signature) {
        PendingImageUpload pendingImageUpload=findBySignature(signature);
        pendingImageUpload.ok=true;
        pendingImageUpload.save();
    }



    public Flash getFlash() {
        return flash;
    }
}
