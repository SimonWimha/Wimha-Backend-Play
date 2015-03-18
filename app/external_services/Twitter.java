package external_services;

import models.Picture;
import models.totem.Flash;
import models.totem.Tribu;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import twitter4j.StatusUpdate;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.File;

/**
 * Created by wimha on 01/07/2014.
 */
public class Twitter {

    public String twitterConsumerKey;
    public String twitterConsumerSecret;
    public String twitterAccessToken;
    public String twitterAccessTokenSecret;
    public String picTweet;
    public String tweet;

    public Twitter(Flash flash, Form<Flash> filledForm){
        Tribu tribu= flash.tribu;
        String totemName=tribu.name;

        twitterConsumerKey = filledForm.data().get("twitter_consumer_key");
        twitterConsumerSecret = filledForm.data().get("twitter_consumer_secret");
        twitterAccessToken = filledForm.data().get("twitter_access_token");
        twitterAccessTokenSecret = filledForm.data().get("twitter_access_token_secret");

        //Wimha hashtags
        String afterTweet = "... #"+totemName + " #totem @wimha ";

        //Tribu hashtag
        if(StringUtils.isNotBlank(tribu.owner_vcard_twitter)){
            afterTweet += "@"+tribu.owner_vcard_twitter;
        }

        //Tribu page link
        afterTweet += " http://www.wimha.com/myTotemPage/"+totemName;

        //reduce message length
        int total = 139;
        int tweetSize = total-afterTweet.length();
        tweet = StringUtils.abbreviate(flash.message, tweetSize) + afterTweet;

        picTweet = flash.picture!=null ? flash.picture.imageTweet(tribu) : null ;

    }

    public void tweet(){

            Twitter.callApi(tweet,
                    twitterConsumerKey,
                    twitterConsumerSecret,
                    twitterAccessToken,
                    twitterAccessTokenSecret,
                    picTweet);

    }

    public static void callApi(String msg, String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, String url) {

        if(msg==null
            || consumerKey == null
            || consumerSecret ==null
            || accessToken == null
            || accessTokenSecret == null){
            return;
        }else {
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setDebugEnabled(true)
                    .setOAuthConsumerKey(consumerKey)
                    .setOAuthConsumerSecret(consumerSecret)
                    .setOAuthAccessToken(accessToken)
                    .setOAuthAccessTokenSecret(accessTokenSecret);
            TwitterFactory tf = new TwitterFactory(cb.build());

            twitter4j.Twitter twitter = tf.getInstance();

            try {
                StatusUpdate status = new StatusUpdate(msg);
                if(StringUtils.isNotEmpty(url)) {
                    File file=Picture.getFile(url);
                    status.setMedia(file);
                }
                twitter.updateStatus(status);


            } catch (Exception e) {
                Logger.error("Exception from tweet library "+msg, e);
            }
        }
    }
}
