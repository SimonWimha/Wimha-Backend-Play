package controllers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.authentication.Helper;
import controllers.authentication.WimhaSecured;
import models.Picture;
import models.Url;
import models.User;
import models.totem.Member;
import models.totem.Tribu;
import models.util.IdentifierCodec;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.DynamicForm;
import play.data.Form;
import play.i18n.Messages;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import services.MailService;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static play.data.Form.form;


public class NewTotem extends Controller {


    public static Result genPublicId(){
        User user = WimhaSecured.getCurrentUser(ctx());
        return ok(views.html.genPublicId.render(user));
    }




    public static Result genPublicIdSubmit(){
        User user = WimhaSecured.getCurrentUser(ctx());
        DynamicForm requestParameters = play.data.Form.form().bindFromRequest();

        ObjectNode json = Json.newObject();

        int nb=-1;
        try{
            nb=Integer.valueOf(requestParameters.get("nb"));
        }catch(Exception e){}

        if(nb<=0){
            ObjectNode error = json.putObject("error");
            error.put("nb", Messages.get("create.error.nb"));
            return ok(json);
        }

        String tribe_name=requestParameters.get("tribe_name");
        if(StringUtils.isBlank(tribe_name)){
            ObjectNode error = json.putObject("error");
            error.put("tribe_name", Messages.get("create.error.tribe_name_null"));
            return ok(json);
        }

        if(Tribu.findByName(tribe_name)!=null){
            ObjectNode error = json.putObject("error");
            error.put("tribe_name", Messages.get("create.error.tribe_name_exists"));
            return ok(json);
        }

        String question=requestParameters.get("question");
        if(StringUtils.isBlank(question)){
            ObjectNode error = json.putObject("error");
            error.put("question", Messages.get("create.error.question"));
            return ok(json);
        }

        if(user==null){
            String user_name=requestParameters.get("user_name");
            if(StringUtils.isBlank(user_name)){
                ObjectNode error = json.putObject("error");
                error.put("user_name", Messages.get("create.error.user_name_null"));
                return ok(json);
            }

            String email=requestParameters.get("email");
            if(StringUtils.isBlank(email)){
                ObjectNode error = json.putObject("error");
                error.put("email", Messages.get("create.error.email_null"));
                return ok(json);
            }

            user = User.getOrCreateUser(user_name, email, null, false).user;
        }



        Picture picture = Picture.findById("212534d8-2190-40cc-bd84-6d1f426b7234");
        MultipartFormData body = request().body().asMultipartFormData();
        if(body!=null){
            FilePart pictureFilePart = body.getFile("picture");
            if (pictureFilePart != null) {

                String contentType = pictureFilePart.getContentType();
                File file = pictureFilePart.getFile();
                try{
                    picture = Picture.save(file, contentType);
                }catch(Exception e){
                    Logger.error("Exception saving picture for new totem", e);
                }

            }
        }

        Tribu tribu = new Tribu();
        tribu.birthdate=new Date();
        tribu.owner_name=user.firstname;
        tribu.question=question;
        tribu.owner_mail=user.email;
        tribu.name=tribe_name;
        tribu.daily_digest=true;
        tribu.owner = user;
        tribu.picture = picture ;
        tribu.save();

        ArrayNode urls = json.putArray("urls");

        for(int i=0;i<nb;i++){
            Member member = new Member();
            member.tribu=tribu;
            member.save();

            Url url=new Url();
            url.member = member;
            url.save();

            url.shortId = IdentifierCodec.encode(url.id);
            url.save();

            urls.add("http://www.wimh.it/r/" + url.shortId);
        }


        return ok(json);
    }


    public static Result generateNewId() {
       	return ok(views.html.admin.generate_id.render());
    }

    /**
     * To generate totems (before initialisation)
     * @return
     */
    public static Result generateNewIdSubmit() {

		DynamicForm requestParameters = play.data.Form.form().bindFromRequest();
		int nb = Integer.valueOf(requestParameters.get("nb"));

		if(nb<=0){
			return badRequest("mauvais nombre");
		}

		Picture picture=null;
        MultipartFormData body = request().body().asMultipartFormData();
        if(body!=null){
	        FilePart pictureFilePart = body.getFile("picture");
	        if (pictureFilePart != null) {

                String fileName = pictureFilePart.getFilename();
	            String contentType = pictureFilePart.getContentType(); 
	            File file = pictureFilePart.getFile();
	            try{
		            picture = Picture.save(file, contentType);
	            }catch(Exception e){
                    Logger.error("Exception saving picture for new totem", e);
	            }

	        }
		}

        String tribuId = requestParameters.get("tribuId");
        Tribu tribu=null;
        if(StringUtils.isNotEmpty(tribuId)){
            tribu=Tribu.findById(tribuId);
        }else{
//            if(picture==null){
//                return ok("les members n'ont pas été générés car l'image n'a pas été récupérée. Réessayez ou contactez Julien");
//            }
            tribu = new Tribu();
            tribu.token=UUID.randomUUID();
            tribu.picture=picture;
            tribu.save();
        }

        List<String> listShortUrl=new ArrayList<>();
        List<String> listMemberIdUrl=new ArrayList<>();
		for(int i=0;i<nb;i++){
            Member member = new Member();
            member.tribu=tribu;
            member.save();

            Url url=new Url();
            url.member = member;
            url.save();

            url.shortId = IdentifierCodec.encode(url.id);
            url.save();

            listShortUrl.add("http://www.wimh.it/r/" + url.shortId);
            listMemberIdUrl.add("http://www.wimha.com/id/" + url.member.id);
        }

        return ok(views.html.admin.generate_id_submit.render(listShortUrl,listMemberIdUrl));
    }

    public static Result submitForm() {
    	Logger.info("Creating totem");
		final Form<Tribu> filledForm = new Form<Tribu>(Tribu.class).bindFromRequest();

		if (filledForm.hasErrors() && !play.Play.isTest()) {
			ObjectNode error = Json.newObject();
			error.put("error", filledForm.errorsAsJson());
            Logger.info("form errors in init" + error.asText());
			return ok(error);
		} else {
            String id=filledForm.data().get("id");
            Member member=Member.findById(id);
            Tribu tribu;
            if(member!=null){
                tribu=member.tribu;
            }else{
                tribu=Tribu.findById(id);
            }
            if(member==null && tribu==null){
                Logger.error("[Init] Member or tribu doesnt exist");
                return badRequest();
            }
            tribu.birthdate=new Date();
            tribu.owner_name=filledForm.data().get("owner_name");
            tribu.question=filledForm.data().get("question");
            tribu.owner_mail=filledForm.data().get("owner_mail");
            tribu.name=filledForm.data().get("name");
            String desc = filledForm.data().get("description");
            if(StringUtils.isNotBlank(desc) && !desc.contains("<NewTotem")) {
                tribu.description = desc;
            }
            tribu.token = UUID.randomUUID();
            tribu.daily_digest=true;

            User.CreationUserResult infos=User.getOrCreateUser(tribu.owner_name,
                    tribu.owner_mail,
                    "lsjf5jd8hdkqz1",
                    false
            );

            tribu.owner = infos.user;
            tribu.save();


			Logger.info("totem created, sending mail token + feedback");
			MailService.totemValidation(tribu);

			ObjectNode jsonResult = Json.newObject();
            jsonResult.put("totem_name", tribu.name);
            jsonResult.put("token", tribu.token+"");
			return ok(jsonResult);
		}
	}

	public static Result validateToken(final String id,final String token){

		final Tribu tribu = Tribu.findByToken(token);
		if(tribu ==null){
			Tribu otherTribu = Tribu.findById(id);
			if(otherTribu !=null){
                Helper.addSession(otherTribu.owner);
                return redirect(controllers.routes.Application.myTotemPage(otherTribu.name));
			}else{
				return badRequest(views.html.error_pages.maintenance.render(null));
			}
		}else if(!tribu.token.toString().equals(token)){
			Logger.error("trying to validate totem id:"+id+ " with wrong token :"+token);
			return badRequest(views.html.error_pages.maintenance.render(null));
		}else{

            User user = tribu.owner;

			models.comment.Thread thread = new models.comment.Thread(tribu);
			thread.save();
			models.comment.FollowedThread.subscribeUser(user,thread);

			tribu.token=null;
			tribu.update();

            //make user connected
            Helper.addSession(user);

            flash("validated","totem");
			return redirect(controllers.routes.Application.myTotemPage(tribu.name));
		}
    }

    public static Result reSendMail(String totemName){
        final DynamicForm form = form().bindFromRequest();
        final String owner_mail = form.get("owner_mail");
        final Tribu tribu = Tribu.findByName(totemName);
        tribu.owner_mail=owner_mail;
        tribu.save();
        MailService.totemValidation(tribu);

        return ok("mail envoyé");
    }
    public static Result reSendMailId(String id){
        final DynamicForm form = form().bindFromRequest();
        final String owner_mail = form.get("owner_mail");

        final Tribu tribu;
        Member m=Member.findById(id);
        if(m!=null){
            tribu=m.tribu;
        }else{
            tribu=Tribu.findById(id);
        }

        tribu.owner_mail=owner_mail;
        tribu.save();
        MailService.totemValidation(tribu);

        return ok().as("application/json");
    }

 	/**
	 * Retrieves totems as a rss feed
	 * @param  page       [description]
	 * @param  totem_name [description]
	 * @return            [description]
	 */
   	public static Result rss() {
   		List<Tribu> res= Tribu.findAll();
		return ok(views.xml.rss_totems.render(res));	
	}

    public static Result myTotems(String email){
        if(StringUtils.isBlank(email)) {
            return badRequest();
        }

        User user = User.findUserByEmail(email);
        if(user==null){
            return notFound();
        }

        ObjectNode json = Json.newObject();
        ArrayNode totems = json.putArray("totems");

        for(Member member :  Member.findUserMemberOfTribe(user.id.toString())){
            Tribu tribu = member.tribu;
            ObjectNode jsonTotem = Json.newObject();

            jsonTotem.put("uid", member.id+"");
            if (tribu.name == null) {
                jsonTotem.put("initialized", "false");
                jsonTotem.put("img", tribu.picture.urlNoSSL());
            } else {
                jsonTotem.put("initialized", "true");
                jsonTotem.put("img", tribu.picture.urlNoSSL());
                jsonTotem.put("question", tribu.question);
                jsonTotem.put("totemName", tribu.name);
                if(tribu.backgroundPicture!=null) {
                    jsonTotem.put("totemBackground", tribu.backgroundPicture.imageTotemBG());
                }
            }

            totems.add(jsonTotem);
        }
        json.put("totems", totems);

        return ok(json);
    }
}
