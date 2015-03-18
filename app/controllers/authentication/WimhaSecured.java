package controllers.authentication;

import com.fasterxml.jackson.databind.node.ObjectNode;
import models.Cookie;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Http.Context;
import play.mvc.Result;
import play.mvc.With;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

//import services.PgSqlAuthenticatorStore;

public class WimhaSecured extends Action<WimhaSecured.Secured> {



	@Override
	public Promise<Result> call(final Context ctx) throws Throwable {
		try {
			// I don't understand why the ctx is not set in the Http.Context
			// thread local variable.
			// I'm setting it by hand so I can retrieve the i18n messages and
			// currentUser() can work.
			// will find out later why this is working this way, if you know why
			// this is not set let me know :)
			Http.Context.current.set(ctx);
			User user = getCurrentUser(ctx);

			if (user == null) {
				if (Logger.isDebugEnabled()) {
					Logger.debug("Anonymous user trying to access : "
							+ ctx.request().uri());
				}
				if (configuration.isApiClient()) {
                    return Promise.pure(forbidden(forbiddenJson()));
				} else {
					return Promise.pure(redirect(controllers.authentication.routes.Helper.login("%2f")));
				}
			} else {
				// ctx.args.put(SecureSocial.USER_KEY, user);
				return delegate.call(ctx);
			}
		} finally {
			// leave it null as it was before, just in case.H
			Http.Context.current.set(null);
		}
	}

	/**
	 * Generates the json required for API calls.
	 *
	 * @return
	 */
	private static ObjectNode forbiddenJson() {
		ObjectNode result = Json.newObject();
		result.put("error", "Credentials required");
		return result;
	}

    @With(WimhaSecured.class)
	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Secured {
		/**
		 * Specifies wether the action handles an API call or not. Default is
		 * false.
		 *
		 * @return
		 */
		boolean isApiClient() default false;
	}

    public static User getCurrentUser(final Context ctx) {
        return getCurrentUser(ctx.request());
    }

    public static User getCurrentUser(Http.Request request) {

        String userId=userIdFromCookie(request);
        User user = (User) Cache.get("getCurrentUser"+userId);
        if(user!=null) {
            return user;
        }
        user = userFromCookieUserId(userId);

        // return the user found plus updating his language and fb picture
        if(user!=null){
            if(user.email!=null && !user.email.isEmpty()){
                Cache.set("getCurrentUser"+userId, user, 60);
                return user;
            }
        }
        return null;
    }

    public static User getCurrentUserEvenUnregistered(final Context ctx) {
        String userId=userIdFromCookie(ctx.request());
        User user = userFromCookieUserId(userId);

        if(user!=null){
            return user;
        }
        return null;
    }

    /**
     * Find the user thanks to the user id stored in the cookie
     * The user id can be an UUID or a facebook id
     * @param userId
     * @return
     */
    private static User userFromCookieUserId(String userId) {
        // Retrieve the user from the user id
        User user;

        //1. trying to find first a UUID
        if (userId == null || userId.contains("-")) {
            user = User.findById(userId);

        } else { //2. supposing this is a fb id, the user id is a UUID generated from the fbId

            Long idFromFacebookAsLong = Long.valueOf(userId);
            UUID uuid = new UUID(idFromFacebookAsLong, idFromFacebookAsLong);

            //3. trying to find a id generated from a fb id
            user = User.findById(uuid.toString());
            if(user==null){
                //trying to find a user with his fb id in the authenticator
                user = User.findByFbId(userId);
            }
        }
        return user;

    }


    /**
     * Retrieve the user id saved in the cookie
     * @param request
     * @return
     */
    private static String userIdFromCookie(Http.RequestHeader request) {

        //Retrieve the user id from the cookie
        //1.Get the cookie
        Http.Cookie cookie = request.cookies().get(Cookie.cookieName);
        if(cookie!=null){
            String idInSession = cookie.value();

            if(StringUtils.isNotBlank(idInSession)) {

                // todo with play 2.3 :  check length and retry ( too long value, couldn't be in cache (max 250 chars) )
                String userId;// = (String) Cache.get("userIdFromCookie"+idInSession);
//                if(userId!=null){
//                    return userId;
//                }

                userId = models.Cookie.fromRequest(idInSession);
//                Cache.set("userIdFromCookie"+idInSession, userId);

                return userId;
            }

        }

        return null;

    }


}
