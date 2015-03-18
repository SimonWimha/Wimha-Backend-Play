package models.socialAction;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import models.User;
import models.totem.Flash;
import models.totem.Tribu;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Controller;
import play.Logger;
import play.cache.Cache;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@DiscriminatorValue("LikeFlash")
public class LikeFlashAction extends LikeAction implements Serializable {

    @ManyToOne
    public Flash flash;

    public LikeFlashAction(final User liker, final Flash flash) {
        super(liker);
        this.flash=flash;
    }

    protected static Finder<Long, LikeFlashAction> finder = new Finder<Long, LikeFlashAction>(
            Long.class, LikeFlashAction.class);


    public static String cacheKeyLikeList(Flash flash){
        if(flash!=null) {
            return "likeListFlash" + flash.id;
        }
        return null;
    }

    public static String cacheKeyAlreadyLike(User user,Flash flash){
        if(flash!=null) {
            return "alreadyLikeFlash" + flash.id+","+user.id;
        }
        return null;
    }

    public static LikeFlashAction alreadyLiked(final User thanker, Flash flash) {
        List<LikeFlashAction> cacheValue = (List<LikeFlashAction>) Cache.get(cacheKeyAlreadyLike(thanker,flash));
        if(cacheValue==null) {
            Cache.set(cacheKeyAlreadyLike(thanker,flash), null);
            cacheValue = finder.where().eq("friend_id", thanker.id)
                    .eq("flash", flash)
                    .findList();
        }

        if(cacheValue.size()>0){
            return cacheValue.get(0);
        }else{
            return null;
        }
    }

    public static LikeFlashAction like(final User thanker, Flash flash) {
        LikeFlashAction res=new LikeFlashAction(thanker, flash);
        res.save();
        Cache.remove(cacheKeyAlreadyLike(thanker,flash));
        Cache.remove(cacheKeyLikeList(flash));
        return res;
    }

    public static void unlike(User user, String flash_id) {
        List<LikeFlashAction> list=finder.where().eq("friend_id", user.id)
                .eq("flash_id", flash_id)
                .findList();
        for(LikeFlashAction like : list){
            Cache.remove(cacheKeyAlreadyLike(user,like.flash));
            Cache.remove(cacheKeyLikeList(like.flash));
            like.delete();
        }
    }

    public static List<LikeFlashAction> findLikesForAFlash(final Flash flash) {
        List<LikeFlashAction> cacheValue = (List<LikeFlashAction>) Cache.get(cacheKeyLikeList(flash));

        if(cacheValue!=null){
            return cacheValue;
        }

        cacheValue = finder.where().eq("flash_id", flash.id).findList();
        Cache.set(cacheKeyLikeList(flash), cacheValue);

        return cacheValue;
    }

    public static List<UserLikes> findMostLikedUsers(Tribu tribu){
        DateTime today = new DateTime( DateTimeZone.UTC ).withTimeAtStartOfDay();
        DateTime endMonday = today.withDayOfWeek(1);
        DateTime beginMonday = endMonday.minusWeeks(1);

        long begin = beginMonday.getMillis();
        long end = endMonday.getMillis();

        String sql = "SELECT wimha_user.id AS userId, count(*) AS likesNB "
                + "FROM wimha_user, social_action, flash "
                + "WHERE flash.tribu_id = :totem_id "
                + "AND social_action.flash_id = flash.id "
                + "AND flash.flasher_id = wimha_user.id "
                + "AND wimha_user.picto_id IS NOT NULL "
                + "AND to_number(date_post, '999999999999999') BETWEEN :begin AND :end";

        sql += " GROUP BY wimha_user.id ORDER by likesNB DESC LIMIT 6";

        SqlQuery sqlQuery = Ebean.createSqlQuery(sql);
        sqlQuery.setParameter("totem_id", tribu.id);
        sqlQuery.setParameter("begin", begin);
        sqlQuery.setParameter("end", end);

        List<UserLikes> result = new ArrayList<>();

        List<SqlRow> sqlRows = sqlQuery.findList();
        if(sqlRows.size()<2){
            return result;
        }

        for (SqlRow sqlRow : sqlRows) {
            User user = User.findById(sqlRow.getString("userId"));
            UserLikes userLikes = new UserLikes();
            userLikes.user=user;
            userLikes.nbLikes= Integer.parseInt(sqlRow.getString("likesNb"));
            result.add(userLikes);
        }

        return result;
    }

    public static class UserLikes {
        public User user;
        public int nbLikes;
    }

}