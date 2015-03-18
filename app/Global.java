import controllers.authentication.WimhaSecured;
import models.User;
import models.notification.DelayedMailNotification;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Seconds;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.Akka;
import play.libs.F;
import play.mvc.*;
import scala.concurrent.duration.Duration;
import services.MailService;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class Global extends GlobalSettings {

    boolean stopping=false;
    @Override
    public void onStart(final Application app) {

        Akka.system().scheduler().scheduleOnce(
                Duration.create(secondsUntilCertainDayHour(1, 9), TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!stopping && !controllers.Application.VAL && play.Play.isProd()) {
                                MailService.ambassadorMail();
                            }
                        } catch (Exception e) {
                            Logger.error("Exception sending ambassador mail", e);
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        Akka.system().scheduler().schedule(
                Duration.create(5, TimeUnit.SECONDS),
                Duration.create(5, TimeUnit.SECONDS),
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DelayedMailNotification.send();
                        } catch (Exception e) {
                            Logger.error("Exception sending delayed notif", e);
                        }
                    }
                },
                Akka.system().dispatcher()
        );

        int seconds=nextExecutionInSeconds(17, 00);
        Akka.system().scheduler().schedule(
                Duration.create(seconds, TimeUnit.SECONDS),
                Duration.create(24, TimeUnit.HOURS),
                new Runnable() {
                    @Override
                    public void run() {

                        //avoid undesired executions when stopping play
                        if(!stopping && !controllers.Application.VAL && play.Play.isProd()){
                            try{
                                Logger.info("EVERY DAY AT 19:00 ---  sending daily digest  " + System.currentTimeMillis());
                                MailService.dailyDigestScript();

                            }catch(Exception e){
                                Logger.error("Exception sending daily digest", e);
                            }
                        }
                    }
                },
                Akka.system().dispatcher()
        );
    }

    @Override
    public void onStop(final Application app) {
        stopping = true;
    }

    public static int nextExecutionInSeconds(int hour, int minute){
        return Seconds.secondsBetween(
                new DateTime(),
                nextExecution(hour, minute)
        ).getSeconds();
    }

    public static DateTime nextExecution(int hour, int minute){
        DateTime next = new DateTime()
                .withHourOfDay(hour)
                .withMinuteOfHour(minute)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        return (next.isBeforeNow())
                ? next.plusHours(24)
                : next;
    }


	@Override
    public F.Promise<Result> onHandlerNotFound(Http.RequestHeader request) {
		if (play.Play.isDev()) {
			return super.onHandlerNotFound(request);
		} else {
        	return F.Promise.pure((Result) Controller.notFound(views.html.error_pages.notFound.render()));
		}
	}

	@Override
	public F.Promise<Result> onError(final Http.RequestHeader arg0, final Throwable arg1) {
        if (play.Play.isDev()) {
			return super.onError(arg0, arg1);
		} else {
			return F.Promise.pure((Result) Controller.internalServerError(views.html.error_pages.maintenance.render(null)));
		}
	}


    @Override
    public Action onRequest(final Http.Request request, Method actionMethod) {
        String ua=request.getHeader("User-Agent");

        if(ua==null){
            return super.onRequest(request,actionMethod);
        }

        ua=ua.toLowerCase();

        //contains http or https
        String header=request.getHeader("x-forwarded-proto");
        boolean https = header !=null && "https".equals(header) || request.host().endsWith("9443");
        boolean homepage = ("/".equals(request.path().toLowerCase()));
        String noelHost = "noel-dev.wimha.com:9443";

        User user = WimhaSecured.getCurrentUser(request);

        //case dev: no redirection.
        if (play.Play.isDev() || controllers.Application.VAL || ua.contains("androidapp"))
        {
            return super.onRequest(request,actionMethod);
        }

        //case old flash urls :
        if("totem.wimha.com".equals(request.host()))
        {
            return new Action.Simple() {
                @Override
                public F.Promise<Result> call(Http.Context ctx) throws Throwable {
                    return F.Promise.pure(redirect(controllers.Application.SERVER_URL + request.path()));
                }
            };
        }

        //case Noel page force https and if https : display home page noel
        else if(request.host().contains(noelHost))
        {

            return new Action.Simple() {
                @Override
                public F.Promise<Result> call(Http.Context ctx) throws Throwable {
                    return F.Promise.pure(redirect("http://www.wimha.com" + request.path() + params(request)));
                }
            };

        }

        return super.onRequest(request,actionMethod);
    }

    private String params(Http.Request request) {
        String res = "?";
        for (String key : request.queryString().keySet()) {
            res+=key+"="+request.getQueryString(key)+"&";
        }
        return res;
    }

    public static long secondsUntilCertainDayHour(int day, int hour) {
        DateTime now = new DateTime(DateTimeZone.UTC);
        DateTime nextDay = now.withDayOfWeek(day);
        DateTime next = nextDay.withHourOfDay(hour).withMinuteOfHour(0);
        if (now.isAfter(next)) {
            next = next.plusWeeks(1);
        }
        long seconds = new org.joda.time.Duration(new DateTime(), next).getStandardSeconds();
        Logger.info("Seconds until next day " + day + " at " + hour + " : " + seconds);
        return seconds;
    }


    public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{GzipFilter.class};
    }
}
