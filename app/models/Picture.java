package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;
import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.Url;
import models.totem.Tribu;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.db.ebean.Model;
import models.util.FileExtensionConverter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.Callable;



@Entity
public class Picture extends Model {

    static String cloudinaryRepositoryName = play.Play.application().configuration().getString("cloudinary.repositoryName");
    static String cloudinarySecretKey = play.Play.application().configuration().getString("cloudinary.secretKey");
    public static String cloudinaryApiKey = play.Play.application().configuration().getString("cloudinary.apiKey");
    static Cloudinary cloudinary = new Cloudinary("cloudinary://"+cloudinaryApiKey+":"+cloudinarySecretKey+"@"+cloudinaryRepositoryName);

	@Id
    public UUID id;

	@Column
	private String contentType;

	@Column
    public String version;

    public Picture(){}

    public Picture(final String contentType, final String version) {
        this.contentType = contentType;
        this.version = version;
    }

	public UUID getId() {
		return this.id;
	}

	public void setContentType(String ct){
		this.contentType=ct;
	}

	public String getVersion(){
		return this.version;
	}

	public void setVersion(String v){
		this.version=v;
	}


	private static Finder<UUID, Picture> finder = new Finder<UUID, Picture>(
			UUID.class, Picture.class);

	public static Picture findById(final String id) {
		//Logger.info("Get picture => " + id);
		if (id == null) {
			return null;
		}
		final UUID uuid = UUID.fromString(id);

		try {
			// use some cache for pictures
			return Cache.getOrElse("picture" + uuid, new Callable<Picture>() {
				@Override
				public Picture call() throws Exception {
					return finder.byId(uuid);
				}
			}, 3600);
		} catch (Exception e) {
			Logger.error("Unable to retrieve picture from cache", e);
		}
		return finder.byId(uuid);
	}

    /******* PICTURE OPERATIONS *********/
    public static Picture save(final File file, final String contentType) {
        Picture result = new Picture();
        result.setContentType(contentType);
        result.save();
        Map params = Cloudinary.asMap("public_id", result.getId().toString());
        Map uploadResult = null;

        try {
            uploadResult = cloudinary.uploader().upload(file, params);
        } catch (Exception e) {
            Logger.error("Error uploading picture : cloudinary API exception : ", e);
        }

        try{
            if(uploadResult!=null) {
                result.setVersion(uploadResult.get("version").toString());
            }
            result.save();
            Logger.info("Just published a picture on Cloudinary with id " + result.id.toString());
        }catch(Exception e){
            Logger.error("Error handling upload result : ", e);
        }
        return result;
    }

	public static void updatePicture(UUID id, final File file, final String contentType){
		Picture currentPicture = findById(id.toString());
		if (currentPicture != null){
			try {
				cloudinary.uploader().destroy(id.toString(), Cloudinary.emptyMap());
				
				Map params = Cloudinary.asMap(
					"public_id", id.toString(),
					"invalidate", true
				);
				Map uploadResult = cloudinary.uploader().upload(file, params);
                String version = uploadResult.get("version").toString();
                Logger.info("Just updated a picture on Cloudinary with id " + id.toString() + " " + version);

                String sql = "UPDATE picture "
                        + "SET version = '"+version+"' "
                        + ", content_type = '"+contentType+"' "
                        + "WHERE id = '" + id+"'";

                SqlUpdate sqlQuery = Ebean.createSqlUpdate(sql);
                sqlQuery.execute();

			} catch (Exception e) {
				Logger.error("Unable to post Picture", e);
			}
		}
	}

    public void delete() {
		try {
			cloudinary.uploader().destroy(id.toString(), Cloudinary.emptyMap());
		} catch (Exception e) {
			Logger.error("Unable to delete picture from Cloudinary", e);
		}
	}

    /******* PICTURE URLS *********/
    public String extension(){
		FileExtensionConverter converter = new FileExtensionConverter();
        String res=converter.ToExtensionType(contentType);
        if(StringUtils.isEmpty(res)){
            return "jpg";
        }else {
            return converter.ToExtensionType(contentType);
        }
	}

    private Url url_version(){
        if(StringUtils.isNotBlank(version) && !"null".equals(version)){
            return cloudinary.url().version(version).secure(true);
        }else{
            return cloudinary.url().secure(true);
        }
    }

    private Url url_versionNoSSL(){
        if(StringUtils.isNotBlank(version) && !"null".equals(version)){
            return cloudinary.url().version(version).secure(false);
        }else{
            return cloudinary.url().secure(false);
        }
    }

    public String url(){
		return url_version().generate(this.getId() + "." + extension());
	}

    public String urlNoSSL(){
        return url_versionNoSSL().generate(this.getId() + "." + extension());
    }

    /******* PICTURE URLS WITH TRANSFORMATION*********/
    public String url_h(int h){
		return url_version().transformation(new Transformation().height(h)).generate(this.getId() + "." + extension());
	}

	public String url_w(int w){
		return url_version().transformation(new Transformation().width(w)).generate(this.getId() + "." + extension());
	}

    public String url_a(){
        if(version!=null) {
            return "http://res.cloudinary.com/cloudinarywimha/image/upload/a_exif/v" + version + "/" + id + "." + extension();
        }else{
            return "http://res.cloudinary.com/cloudinarywimha/image/upload/a_exif/" + id + "." + extension();
        }
    }

	public String transformation(final Integer width, final Integer height, final String transformation){
		return url_version().transformation(new Transformation().height(height).width(width).crop("fill")).generate(this.getId() + "." + extension());
	}

	public String imageWithBG(){
		return "http://res.cloudinary.com/cloudinarywimha/image/upload/c_pad,co_rgb:000,g_center,h_630,pg_1/c_scale,h_630,u_picjumbo_com_IMG_1776_q86dw4,w_1200/v"+version+"/"+id+"."+extension();
	}

    public String imageTotemBG(){
        return "http://res.cloudinary.com/cloudinarywimha/image/upload/e_colorize:60,w_1600,h_1280,c_fill/v"+version+"/"+id+"."+extension();
    }

    //Android background smaller because bitmap overflow
    public String imageTotemBGAndroid(){
        return "http://res.cloudinary.com/cloudinarywimha/image/upload/e_colorize:60,w_1250,h_1000,c_fill/v"+version+"/"+id+"."+extension();
    }

    public String imageFlashForFeed(){
        return "http://res.cloudinary.com/cloudinarywimha/image/upload/e_colorize:15,w_400,c_fill/v"+version+"/"+id+"."+extension();
    }

    public String imageTotemBG_blured(){
        return "http://res.cloudinary.com/cloudinarywimha/image/upload/e_blur:700,w_1600,h_1280,c_fill/v"+version+"/"+id+"."+extension();
    }

	public String imageWithTotem(final Tribu tribu){
		return "http://res.cloudinary.com/cloudinarywimha/image/upload/bo_10px_solid_rgb:fff,c_fill,g_faces:center,h_630,w_1200/c_scale,g_south_east,l_"+ tribu.picture.getId()+",w_150"+checkVersion()+"/"+id+"."+extension();
	}

    public String checkVersion() {
        if(StringUtils.isNotBlank(version) && !"null".equals(version)){
            return "/v"+version;
        }else{
            return "";
        }
    }

    public String imageTweet(final Tribu tribu) {
        return "http://res.cloudinary.com/cloudinarywimha/image/upload/bo_10px_solid_rgb:fff,c_fill,g_faces:center,h_220,w_440/c_scale,g_south_east,l_"+ tribu.picture.getId()+",w_70/v"+checkVersion()+"/"+id+"."+extension();
    }

    /******* PICTURE DOWNLOAD *********/
    public static Picture fetchPictureFromUrl(final String url) {
        if (url!=null) {
            try {
                File file=new File("/tmp/"+new Date().getTime());
                FileUtils.copyURLToFile(new URL(url), file);
                String contentType = new javax.activation.MimetypesFileTypeMap().getContentType(file);
                long fileSize = file.length();
                Logger.info(contentType+fileSize);
                if(fileSize==0 || fileSize > 700000) {
                    Logger.error(url + " : " + fileSize+ "ko");
                    return null;
                }
                if("image/gif".equals(contentType) || "image/jpeg".equals(contentType) || "image/png".equals(contentType)){
                    return Picture.save(file, contentType);
                }
                if("application/octet-stream".equals(contentType)){
                    contentType="image/jpeg";
                    return Picture.save(file, contentType);
                }
            } catch (IOException e) {
                Logger.error("Exception fetchPictureFromUrl", e);
            }

        }

        return null;
    }

    public static File fetchFacebookPictureFromUser(final User user, final String idFb) {

        if (user != null && idFb != null) {
            Logger.info("Fetching "+user.id+ " " +idFb);

                String imgPath = "https://graph.facebook.com/" + idFb + "/picture?width=500";
                InputStream picture2AsStream = null;
                byte[] bytes = null;
                String contentType = null;
                try {
                    URL oracle = new URL(imgPath);
                    picture2AsStream  = oracle.openStream();
                    HttpURLConnection connection = (HttpURLConnection)  oracle.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.connect();
                    contentType = connection.getContentType();
                }
                catch (MalformedURLException ex) {
                    Logger.info(ex.getMessage() + "error type MalformedURLException");
                }
                catch (IOException ex) {
                    Logger.info(ex.getMessage() + "error type IOException");
                }
                if (picture2AsStream != null) {
                    try {
                        bytes = IOUtils.toByteArray(picture2AsStream);
                        int fileSize = bytes.length;
                        Logger.info("file size "+fileSize);

                        if(("image/gif".equals(contentType) || "image/jpeg".equals(contentType) || "image/png".equals(contentType)) && (fileSize < 700000)) {
                            File tmp=new File("/tmp/fbpic");
                            FileUtils.writeByteArrayToFile(tmp, bytes);
                            return tmp;
                        }

                    } catch (IOException e) {
                        Logger.error(e.getMessage());
                    }
                } else {
                    Logger.warn("Image not found " + imgPath);
                }
            }
        return null;

    }

    /******** HELPERS  ********/
    public static String getApiKey(){
        return cloudinaryApiKey;
    }

    public static File getFile(final String url) {
        if (url!=null) {
            try {
                File file=new File("/tmp/"+new Date().getTime());
                FileUtils.copyURLToFile(new URL(url), file);
                return file;
            } catch (IOException e) {
                Logger.error("Unable to copy file in tmp", e);
            }

        }

        return null;
    }

    public static String generateSignature(String newId, String date_post) {
        Map<String, Object> options=new HashMap<>();
        Map<String, Object> params=new HashMap<>();
        params.put("public_id", newId);
        params.put("timestamp", date_post);
        cloudinary.signRequest(params, options);
        return (String) params.get("signature");
    }
}