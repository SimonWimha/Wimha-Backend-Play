package controllers.flash;

import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.Helpers;
import models.totem.Member;
import models.totem.Tribu;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import static play.data.Form.form;

public class Scan extends Controller {

    /**
     * Displays the welcome screen and
     * message creation form
     * @param  totemId [description]
     * @return            [description]
     */
    public static Result index(final String totemId) {
        Member member=Member.findById(totemId);
        Tribu tribu;
        if(member!=null){
            tribu = member.tribu;
        }else{
            tribu = Tribu.findById(totemId);
        }
        if(tribu==null){
            return ok(views.html.error_pages.maintenance.render("This totem does not exist"));
        }

        return ok(views.html.flashPages.index.render(tribu.name));

    }

    /**
     * Sends json to android to tell if terminal should display init  or flash
     * @param  member_id [description]
     * @return            [description]
     */
    public static Result androidIndex(final String member_id) {
        Member member=Member.findById(member_id);
        Tribu tribu;
        if(member!=null){
            tribu = member.tribu;
        }else{
            tribu = Tribu.findById(member_id);
        }
        //Sending json result to javascript
        ObjectNode jsonResult = Json.newObject();
        if(tribu.name==null || tribu.name.isEmpty()){
            jsonResult.put("initialized","false");
            jsonResult.put("picture", tribu.picture.url_h(300));

        }else if(tribu.token!=null){
            jsonResult.put("initialized","pending");
        }else{
            jsonResult.put("initialized", "true");
            jsonResult.put("picture", tribu.picture.url_h(300));
            jsonResult.put("question", tribu.question);
            jsonResult.put("totemName", tribu.name);
            if(tribu.backgroundPicture!=null) {
                if(Helpers.iOSorAndroid()== Helpers.PhoneType.android) {
                    jsonResult.put("totemBackground", tribu.backgroundPicture.imageTotemBGAndroid());
                }else{
                    jsonResult.put("totemBackground", tribu.backgroundPicture.imageTotemBG());
                }
            }
            jsonResult.put("tribuName",tribu.name);
            if(member!=null) {
                jsonResult.put("memberInitialized", (member.user != null) ? "true" : "false");
            }
        }
        return ok(jsonResult);
    }


}
