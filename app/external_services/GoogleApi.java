package external_services;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.totem.Flash;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.libs.F;
import play.libs.Json;
import play.libs.ws.*;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * Created by julien on 28/05/14.
 */
public class GoogleApi extends Controller {


    public static Flash geocoding(Flash flash, String city, CharSequence address, String country, String countryCode) {
        if (StringUtils.isEmpty(city) || StringUtils.isEmpty(address) || StringUtils.isEmpty(country)) {
            try {
                addressSubmit(flash);
            } catch (Exception e) {
                Logger.error("Exception in geocoding call",e);
                flash.city = flash.lat;
                flash.country = flash.lon;
            }
        } else {
            flash.city = city;
            flash.country = country;
            flash.country_code = countryCode;
        }
        return flash;
    }


    /**
     * Fetch the adress of coordinates at the end of flash
     *
     * @param  lat [description]
     * @param  lon [description]
     * @return     [description]
     */
    public static void addressSubmit(final Flash flash) throws Exception{

        try{
            String lat=flash.getLat();
            String lon=flash.getLon();

            WS.url("http://dev.virtualearth.net/REST/v1/Locations/"+lat+","+lon)
                    .setQueryParameter("o", "json")
                    .setQueryParameter("key", play.Play.application().configuration().getString("bing.geocode_key"))
                    .get()
                    .map(
                            new F.Function<WSResponse, Result>() {
                                public Result apply(WSResponse response) throws Exception {
                                    JsonNode responseJson = null;
                                    try {
                                        responseJson = response.asJson();
                                    } catch (Exception e) {
                                        Logger.error("couldn't parse geocoding response as json");
                                        throw e;
                                    }
                                    if (!"ValidCredentials".equals(responseJson.get("authenticationResultCode").asText())) {
                                        Logger.error("bad response of geocoding api : " + responseJson);
                                    }

                                    try {
                                        JsonNode root = responseJson.path("resourceSets").get(0).path("resources").get(0).get("address");
                                        String addr = root.get("formattedAddress").toString();
                                        String city = root.get("locality").toString();
                                        String country = root.get("countryRegion").toString();
                                        String countryCode = "";

                                        flash.address = addr;
                                        flash.city = city;
                                        flash.country = country;
                                        flash.country_code = countryCode;
                                    }catch(Exception e){
                                        Logger.error("Exception in WS call to geocoding during submit", e);
                                        Logger.error(responseJson.asText());
                                        addressSubmitWithGoogle(flash);
                                    }
                                    return ok();
                                }
                            }
                    ).get(2000);
        }catch(Exception e){
            Logger.error("Exception in WS call to geocoding during submit", e);
            addressSubmitWithGoogle(flash);
        }
    }


    /**
     * Fetch the adress of coordinates at the end of flash
     *
     * @param  lat [description]
     * @param  lon [description]
     * @return     [description]
     */
    public static void addressSubmitWithGoogle(final Flash flash) throws Exception{

        try{
            String lat=flash.getLat();
            String lon=flash.getLon();

            WS.url("https://maps.googleapis.com/maps/api/geocode/json")
                    .setQueryParameter("latlng", lat + "," + lon)
                    .setQueryParameter("key", play.Play.application().configuration().getString("google.geocode_key"))
                    .setQueryParameter("sensor", "false")
                    .post("content")
                    .map(
                            new F.Function<WSResponse, Result>() {
                                public Result apply(WSResponse response) throws Exception {
                                    JsonNode responseJson = null;
                                    try {
                                        responseJson = response.asJson();
                                    } catch (Exception e) {
                                        Logger.error("couldn't parse geocoding response as json");
                                        throw e;
                                    }
                                    if (!"OK".equals(responseJson.get("status").asText())) {
                                        Logger.error("bad response of geocoding api : " + responseJson);
                                    }

                                    JsonNode root = responseJson.path("results").get(0);
                                    String addr = root.get("formatted_address").toString();

                                    String city = "";
                                    String country = "";
                                    String countryCode = "";
                                    for (JsonNode node : root.path("address_components")) {

                                        for (Iterator it = node.get("types").elements(); it.hasNext(); ) {
                                            JsonNode itNode = (JsonNode) it.next();
                                            if ("locality".equals(itNode.asText())) {
                                                city = node.get("long_name").asText();
                                            }
                                        }

                                        for (Iterator it = node.get("types").elements(); it.hasNext(); ) {
                                            JsonNode itNode = (JsonNode) it.next();
                                            if ("country".equals(itNode.asText())) {
                                                country = node.get("long_name").asText();
                                                countryCode = node.get("short_name").asText();
                                            }
                                        }
                                    }

                                    flash.address = addr;
                                    flash.city = city;
                                    flash.country = country;
                                    flash.country_code = countryCode;

                                    return ok();
                                }
                            }
                    ).get(2000);
        }catch(Exception e){
            Logger.error("Exception in WS call to geocoding during submit", e);
            flash.address=flash.lat + " , " + flash.lon;
        }
    }

}
