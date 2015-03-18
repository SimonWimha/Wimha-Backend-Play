import models.PendingImageUpload;
import models.User;
import models.totem.Member;
import models.totem.Tribu;
import org.fest.assertions.Assertions;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import play.Logger;
import play.libs.F;
import play.libs.WS;
import play.test.TestBrowser;

import java.io.File;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AGenMemberTest {

    static String name ="JulienTestInitial";
    static String mail = "jderenty@gmail.com";
    static String message ="Cecie est un flash loggué de test, avec photo";
    static String path;
    static String target;
    static String memberId;

//    @Test
    public void runInBrowser() {
        running(testServer(3333), HTMLUNIT, new F.Callback<TestBrowser>() {
            public void invoke(TestBrowser browser) {
                browser.goTo("http://localhost:3333");
            }
        });
    }

    @Test
    public void aCreateMember() {
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response scanResponse = WS.url("http://localhost:3333/genid/")
                        .setQueryParameter("nb", "1")
                        .setHeader("Content_Type", "multipart/form-data")
                        .post(new File("/Users/juliend/Downloads/picture.png")).get(5000);
                Assertions.assertThat(scanResponse.getStatus()).isEqualTo(OK);

                String responseText = scanResponse.getBody();

                String selector="http://www.wimh.it";
                int index=responseText.indexOf(selector)+selector.length();
                path=responseText.substring(index, index+10);
            }
        });

    }

    @Test
    public void bTestRedirection() {
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response redirectionResponse = WS.url("http://localhost:3333" + path).setFollowRedirects(false).get().get(5000);
                Logger.debug("body" + redirectionResponse.getBody());
                target = redirectionResponse.getHeader("location");

                Logger.debug("target "+target);

            }
        });

    }


    @Test
    public void cTestInitMember() {
        running(testServer(3333), new Runnable() {
            public void run() {
                Logger.debug("target "+target);

                int index=target.indexOf("/id")+4;
                memberId=target.substring(index);
                Logger.debug("id "+memberId);

                Tribu tribu= Member.findById(memberId).tribu;
                tribu.picture=Tribu.findByName("Dreamjobs").picture;
                Logger.debug(tribu.picture+"");
                tribu.save();

                WS.Response webserviceResponse = WS.url("http://localhost:3333/android/id/"+memberId).get().get(5000);
                Logger.debug(webserviceResponse.getBody());


            }
        });

    }

    @Test
    public void dTestWebService() {
        running(testServer(3333), new Runnable() {
            public void run() {

                int index=target.indexOf("/id")+4;
                String tribuId=target.substring(index);
                Logger.debug("id "+tribuId);

                WS.Response webserviceResponse = WS.url("http://localhost:3333/android/id/"+tribuId).get().get(5000);
                Logger.debug(webserviceResponse.getBody());

                assertThat("{\"initialized\":\"false\",\"picture\":\"https://res.cloudinary.com/cloudinarywimha/image/upload/h_100/v1386235707/e05b6d08-5f77-4516-bf22-7af1ab35eab5.png\"}").isEqualTo(webserviceResponse.getBody());
            }
        });

    }

//
//    static String name ="JulienTestInitial";
//    static String mail = "jderenty@gmail.com";
    static String question ="Question de test unitaire ?";
    public static String totemName="UnitTestTot"+Math.round(Math.random()*1000);


    @Test
    public void eTestInit() {
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response responseRegister = WS.url("http://localhost:3333/newtotem/")
                        .setQueryParameter("id", AGenMemberTest.memberId)
                        .setQueryParameter("question", question)
                        .setQueryParameter("name", totemName)
                        .setQueryParameter("owner_name",name)
                        .setQueryParameter("owner_mail", mail)
                        .setQueryParameter("description", "")
                        .setQueryParameter("lang", "fr")
                        .post("").get(5000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);
                Assertions.assertThat(responseRegister.asJson().has("error")).isFalse();
            }
        });
    }

    @Test
    public void fTestValidation() {
        running(testServer(3333), new Runnable() {
            public void run() {
                Tribu tribu=Member.findById(AGenMemberTest.memberId).tribu;

                WS.Response responseRegister = WS.url("http://localhost:3333/newtotem/validate/" + tribu.id +"/" + tribu.token).get().get(5000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);

            }
        });

    }

    @Test
    public void gTestScan() {
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response responseRegister = WS.url("http://localhost:3333/android/id/" + AGenMemberTest.memberId)
                        .get().get(5000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);
                Logger.debug(responseRegister.getBody());
            }
        });
    }
//
//    static String name ="JulienTestInitial";
//    static String mail = "jderenty@gmail.com";
//    static String message ="Cecie est un flash loggué de test, avec photo";
    private static String signature;

    private static void randomUserAndMessage(){
        name="JulienTest"+Math.round(Math.random()*1000);
        mail=name+"@mailinator.com";
        message="Message de "+name;
    }

    @Test
    public void hTestFlashNeedPictureLog() {
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response responseRegister = WS.url("http://localhost:3333/submit/")
                        .setQueryParameter("member_id", AGenMemberTest.memberId)
                        .setQueryParameter("totem_name", totemName)
                        .setQueryParameter("lat", "43")
                        .setQueryParameter("lon", "2")
                        .setQueryParameter("message",message)
                        .setQueryParameter("name",name)
                        .setQueryParameter("mail", mail)
                        .setQueryParameter("logged", "true")
                        .setQueryParameter("post_fb", "true")
                        .setQueryParameter("fbid",  User.findUserByEmail("julien@wimha.com").idFb)
                        .setQueryParameter("need_picture_id", "true")
                        .setHeader("Content_Type", "multipart/form-data")
                        .post("").get(5000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);

                signature=responseRegister.asJson().path("cloudinary_signature").asText();
            }
        });

    }

    @Test
    public void iTestFlashNeedPictureInvite() {
        randomUserAndMessage();

        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response responseRegister = WS.url("http://localhost:3333/submit/")
                        .setQueryParameter("member_id", AGenMemberTest.memberId)
                        .setQueryParameter("totem_name", totemName)
                        .setQueryParameter("lat", "43")
                        .setQueryParameter("lon", "2")
                        .setQueryParameter("message",message)
                        .setQueryParameter("name",name)
                        .setQueryParameter("mail", mail)
                        .setQueryParameter("need_picture_id", "true")
                        .setHeader("Content_Type", "multipart/form-data")
                        .post("").get(5000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);

            }
        });
    }

    @Test
    public void jTestPushSignature(){
        running(testServer(3333), new Runnable() {
            public void run() {

                WS.Response responseRegister = WS.url("http://localhost:3333/update_version/")
                        .setQueryParameter("signature", signature)
                        .setQueryParameter("version", "123456789")
                        .setQueryParameter("content_type", "image/jpeg")
                        .get().get(10000);

                int statusRegister=responseRegister.getStatus();
                Assertions.assertThat(statusRegister).isEqualTo(OK);

                PendingImageUpload pendingImageUpload=PendingImageUpload.findBySignature(signature);
                Assertions.assertThat(pendingImageUpload.ok).isEqualTo(true);
            }
        });

    }



}