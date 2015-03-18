package controllers;

import controllers.authentication.WimhaSecured;
import models.User;
import models.totem.Tribu;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.List;

/**
 * Created by julien on 18/03/14.
 * Used to save lang changes in the cookie and in DB
 */
public class Lang extends Controller {


    /**
     * change lang on the homepage = change in the cookie at least + in DB if user logged in
     * @param locale
     * @return
     */
    public static Result changeLocaleHomePage(String locale){

        //origin url to reload the page
        String[] referrers = ctx().request().headers().get(REFERER);

        String referrer="/";
        if(referrers!=null && referrers.length>0){
            referrer=referrers[0];
        }

        //change also in DB
        User user = WimhaSecured.getCurrentUser(ctx());
        if(user!=null){
            user.setLang(locale);
        }

        //set the cookie
        changeLang(locale);

        return redirect(referrer); // TODO Check if the lang is handled by the application
    }


    /**
     * Retrieves browser or cookie value of the language
     * @return
     */
    public static String getLangRequest(){

        //else, if already set: find in cookie
        Http.Cookie cookie = ctx().request().cookie("PLAY_LANG");
        if(cookie!=null){
            return codeToGoodDisplay(cookie.value());

        //else, find in navigator
        } else{
            return codeToGoodDisplay(getBestLang(ctx().request().acceptLanguages()));
        }
    }

    /**
     * Formats lang code to display to user
     * @param code
     * @return
     */
    private static String codeToGoodDisplay(String code) {
        if(code==null){
            return "En";
        }
        if("en".equals(code) || "en-EN".equals(code)){
            return "En";
        }else if("fr".equals(code) || "fr-FR".equals(code)){
            return "Fr";
        }
        return "En";
    }

    public static String getBestLang(List<play.i18n.Lang> requestedLanguages){
        for (play.i18n.Lang lang : requestedLanguages) {
            String conf = play.Play.application().configuration().getString("application.langs");
            if (conf.indexOf(lang.code())>0) {
                return lang.code();
            }
        }
        return "En-en";
    }



}
