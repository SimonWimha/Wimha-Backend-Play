//import models.PendingImageUpload;
//import models.User;
//import models.totem.Position;
//import models.totem.Tribu;
//import org.apache.http.entity.mime.MultipartEntity;
//import org.apache.http.entity.mime.content.FileBody;
//import org.fest.assertions.Assertions;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//import play.libs.WS;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//
//import static play.test.Helpers.*;
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class FlashTotemTest {
//
//    static String name ="JulienTestInitial";
//    static String mail = "jderenty@gmail.com";
//    static String message ="Cecie est un flash loggué de test, avec photo";
//    private static String signature;
//
//    private static void randomUserAndMessage(){
//        name="JulienTest"+Math.round(Math.random()*1000);
//        mail=name+"@mailinator.com";
//        message="Message de "+name;
//    }
//
//    @Test
//    public void aTestFlashNeedPictureLog() {
//        running(testServer(3333), new Runnable() {
//            public void run() {
//
//                WS.Response responseRegister = WS.url("http://localhost:3333/submit/")
//                        .setQueryParameter("totem.id", "99ad219a-582e-4e40-b3aa-e4d1549a60bd")
//                        .setQueryParameter("totem_name", BInitTribuTest.totemName)
//                        .setQueryParameter("lat", "43")
//                        .setQueryParameter("lon", "2")
//                        .setQueryParameter("message",message)
//                        .setQueryParameter("name",name)
//                        .setQueryParameter("mail", mail)
//                        .setQueryParameter("logged", "true")
//                        .setQueryParameter("post_fb", "true")
//                        .setQueryParameter("fbid",  User.findUserByEmail("julien@wimha.com").idFb)
//                        .setQueryParameter("need_picture_id", "true")
//                        .setHeader("Content_Type", "multipart/form-data")
//                        .post("").get(5000);
//
//                int statusRegister=responseRegister.getStatus();
//                Assertions.assertThat(statusRegister).isEqualTo(OK);
//
//                signature=responseRegister.asJson().path("cloudinary_signature").asText();
//            }
//        });
//
//    }
//
//    @Test
//    public void bTestFlashNeedPictureInvite() {
//        randomUserAndMessage();
//
//        running(testServer(3333), new Runnable() {
//            public void run() {
//
//                WS.Response responseRegister = WS.url("http://localhost:3333/submit/")
//                        .setQueryParameter("totem.id", "99ad219a-582e-4e40-b3aa-e4d1549a60bd")
//                        .setQueryParameter("totem_name", BInitTribuTest.totemName)
//                        .setQueryParameter("lat", "43")
//                        .setQueryParameter("lon", "2")
//                        .setQueryParameter("message",message)
//                        .setQueryParameter("name",name)
//                        .setQueryParameter("mail", mail)
//                        .setQueryParameter("need_picture_id", "true")
//                        .setHeader("Content_Type", "multipart/form-data")
//                        .post("").get(5000);
//
//                int statusRegister=responseRegister.getStatus();
//                Assertions.assertThat(statusRegister).isEqualTo(OK);
//
//            }
//        });
//    }
//
//    @Test
//    public void cTestFlashNeedPictureOldImageUpload() {
//        randomUserAndMessage();
//
//        running(testServer(3333), new Runnable() {
//            public void run() {
//                MultipartEntity entity = new MultipartEntity();
//                entity.addPart("picture", new FileBody(new File("/Users/juliend/Downloads/picture.png")));
//                ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
//
//                try {
//                    entity.writeTo(outputstream);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                WS.Response responseRegister = WS.url("http://localhost:3333/submit/")
//                        .setQueryParameter("totem.id", "99ad219a-582e-4e40-b3aa-e4d1549a60bd")
//                        .setQueryParameter("totem_name", BInitTribuTest.totemName)
//                        .setQueryParameter("lat", "43")
//                        .setQueryParameter("lon", "2")
//                        .setQueryParameter("message",message)
//                        .setQueryParameter("name",name)
//                        .setQueryParameter("mail", mail)
//                        .setQueryParameter("logged", "false")
//                        .setHeader("Content_Type", "multipart/form-data")
//                        .post(outputstream.toString()).get(5000);
//
//                int statusRegister=responseRegister.getStatus();
//                Assertions.assertThat(statusRegister).isEqualTo(OK);
//                Tribu totun=Tribu.findByName(BInitTribuTest.totemName);
//                Position flash=Position.findSame(mail, message, totun);
//                Assertions.assertThat(flash.picture).isNotNull();
//            }
//        });
//
//    }
//
//    @Test
//    public void dTestPushSignature(){
//        running(testServer(3333), new Runnable() {
//            public void run() {
//
//                WS.Response responseRegister = WS.url("http://localhost:3333/update_version/")
//                        .setQueryParameter("signature", signature)
//                        .setQueryParameter("version", "123456789")
//                        .setQueryParameter("content_type", "image/jpeg")
//                        .get().get(5000);
//
//                int statusRegister=responseRegister.getStatus();
//                Assertions.assertThat(statusRegister).isEqualTo(OK);
//
//                PendingImageUpload pendingImageUpload=PendingImageUpload.findBySignature(signature);
//                Assertions.assertThat(pendingImageUpload.ok).isEqualTo(true);
//            }
//        });
//
//    }
//
//}