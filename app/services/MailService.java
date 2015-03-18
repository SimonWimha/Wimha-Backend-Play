package services;

import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import controllers.Application;
import external_services.Facebook;
import models.User;
import models.comment.Comment;
import models.comment.FollowedThread;
import models.notification.NotificationPreference;
import models.socialAction.LikeFlashAction;
import models.totem.Flash;
import models.totem.Member;
import models.totem.Tribu;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.F;
import play.twirl.api.Html;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

// Code sample from mailjet: http://www.mailjet.com/docs/code/java

public class MailService {

	private static Session mailSession;

	public static Session buildMailService() {
		if (mailSession == null) {

			final String username = Play.application().configuration()
					.getString("mail.username");
			final String password = Play.application().configuration()
					.getString("mail.password");

			Properties props = new Properties();

			props.put("mail.smtp.host", Play.application().configuration()
					.getString("mail.host"));
			props.put("mail.smtp.socketFactory.port", Play.application()
					.configuration().getString("mail.port"));
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.port", Play.application().configuration()
					.getString("mail.port"));

			props.put("mail.smtp.auth", "true");

			if (play.Play.isProd()) {		
				mailSession = Session.getDefaultInstance(props,
						new javax.mail.Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(username,
										password);
							}
						});
			} else {
				mailSession = Session.getInstance(props,
						new javax.mail.Authenticator() {
							@Override
							protected PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(username,
										password);
							}
						});
			}
		}

		return mailSession;
	}

	public static void sendMail(final MimeMessage message)
	{

        //Send picture to cloudinary then send notifications.
        F.Promise.promise(
                new F.Function0<Void>() {
                    public Void apply() {
                        try {
                            boolean forceGmail = false;
                            if (forceGmail || !play.Play.isProd() || controllers.Application.VAL == true) {
                                MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
                                mail.setSubject(message.getSubject());
                                mail.setRecipient(message.getRecipients(Message.RecipientType.TO)[0].toString());
                                mail.setFrom(message.getFrom()[0].toString());
                                mail.sendHtml(message.getContent().toString());
                            } else {
                                Transport.send(message);
                            }
                            Logger.info("Sent mail notif to " + message.getRecipients(Message.RecipientType.TO)[0].toString());
                        }catch(Exception e){

                        }
                        return null;
                    }
                }
        ).map(new F.Function<Void, Object>() {
            @Override
            public Object apply(Void aVoid) throws Throwable {

                return null;
            }
        });

	}

	public static void resetPassword(final User user, String password)
	{
			Html html = views.html.emails.resetPassword.render(user,password);
			Session session = buildMailService();

			String from = "Wimha" + "<admin@wimha.com>";

			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("X-Mailjet-Campaign", "reset-password");
				message.setHeader("Content-Type", "text/html; charset=UTF-8");
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(user.email));
				message.setSubject(Messages.get("email.object.reset"),"utf-8");
				message.setContent(html.body(), "text/html");

					sendMail(message);


			} catch (MessagingException e) {
				Logger.error("Error sending reset password mail", e);
			}
		
	}


	public static void confirmationMail(final User user, final String password)
	{
			Html html = views.html.emails.inlined.ValidationAccount.render(user, password);
			Session session = buildMailService();

			String from = "Wimha" + "<totem@wimha.com>";

			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("X-Mailjet-Campaign", "register-email");
				message.setHeader("Content-Type", "text/html; charset=UTF-8");
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(user.email));
				message.setSubject(Messages.get("email.object.confirmationAccount"),"utf-8");
				message.setContent(html.body(), "text/html");

					sendMail(message);


			} catch (MessagingException e) {
                Logger.error("Error sending confirmationMail", e);
			}
	}

	public static void totemValidation(final Tribu tribu) {

		Html html = views.html.emails.inlined.ValidationTotem.render(tribu.name, tribu.owner_name, tribu);
		Session session = buildMailService();

		String from = "Wimha" + "<totem@wimha.com>";

		try {
			MimeMessage message = new MimeMessage(session);
			message.setHeader("X-Mailjet-Campaign", "new-totem");
			message.setHeader("Content-Type", "text/html; charset=UTF-8");
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(tribu.owner_mail));
			message.setRecipients(Message.RecipientType.BCC,
					InternetAddress.parse("bertran@wimha.com"));
			message.setSubject(Messages.get("email.object.confirmationTotem"),"utf-8");
			message.setContent(html.body(), "text/html");

				sendMail(message);


		} catch (MessagingException e) {
			Logger.error("Error sending totem validation mail", e);
		}
	}


	public static void flashValidation(final Flash flash) {

		Html html = views.html.emails.inlined.ValidationFlash.render(flash.getTribu().name, flash.name, flash);
		Session session = buildMailService();

		String from = "Wimha" + "<totem@wimha.com>";

		try {
			MimeMessage message = new MimeMessage(session);
			message.setHeader("X-Mailjet-Campaign", "flash-totem");
			message.setHeader("Content-Type", "text/html; charset=UTF-8");
			message.setFrom(new InternetAddress(from));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(flash.mail));
			message.setSubject(Messages.get("email.object.confirmationFlash"),"utf-8");
			message.setContent(html.body(), "text/html");

			
				sendMail(message);


		} catch (MessagingException e) {
            Logger.error("Error sending flash validation mail", e);
		}
	}

    public static void loggedFlashValidation(Flash flash) {
        Html html = views.html.emails.inlined.ValidationLoggedFlash.render(flash);

        Session session = buildMailService();

        String from = "Wimha" + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "flash-totem");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(flash.mail));
            message.setSubject(Messages.get("email.object.firstFlash"),"utf-8");
            message.setContent(html.body(), "text/html");


            sendMail(message);


        } catch (MessagingException e) {
            Logger.error("Error sending first flash validation mail", e);
        }
    }


	public static void newFlash(final models.comment.FollowedThread fThread, final Flash flash) {
			Tribu tribu = flash.tribu;
			Html html = views.html.emails.inlined.NewFlashNotif.render(fThread, flash, tribu);
			Session session = buildMailService();

			String from = tribu.name + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "totem-"+ tribu.name);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(fThread.getUser().email));
            message.setSubject(Messages.get("email.object.comment.TOTEM"),"utf-8");
            message.setContent(html.body(), "text/html; charset=UTF-8");


            sendMail(message);


        } catch (MessagingException e) {
            Logger.error("Error new flash notif mail", e);
        }
    }


    public static void newFollower(final User user, final User secondUser, Tribu tribu) {
        Html html = views.html.emails.inlined.NewFollowers.render(user, secondUser, tribu);
        Session session = buildMailService();

        String from = tribu.name + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "new-follower-totem-"+ tribu.name);
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(user.email));
				message.setSubject(Messages.get("email.object.newFollower"),"utf-8");
				message.setContent(html.body(), "text/html");

				
					sendMail(message);


			} catch (MessagingException e) {
                Logger.error("Error new follower mail", e);
			}
	}

	public static void newLikeTotem(final User user, Tribu tribu) {
			Html html = views.html.emails.inlined.NewLike.render(user, tribu.getOwner(), tribu, null);
			Session session = buildMailService();

			String from = tribu.name + "<totem@wimha.com>";

			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("X-Mailjet-Campaign", "new-liker-totem-"+ tribu.name);
				message.setHeader("Content-Type", "text/html; charset=UTF-8");
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(tribu.getOwner().email));
				message.setSubject(Messages.get("email.object.newLike"),"utf-8");
				message.setContent(html.body(), "text/html");

				
					sendMail(message);


			} catch (MessagingException e) {
                Logger.error("Error sending new like totem mail", e);
			}
	}

	public static void newLikeFlash(final User user, Flash flash) {
			Tribu tribu = flash.tribu;
			Html html = views.html.emails.inlined.NewLike.render(user, flash.getFlasher(), tribu, flash);
			Session session = buildMailService();

			String from = tribu.name + "<totem@wimha.com>";

			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("X-Mailjet-Campaign", "new-follower-totem-"+ tribu.name);
				message.setHeader("Content-Type", "text/html; charset=UTF-8");
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(flash.getFlasher().email));
				message.setSubject(Messages.get("email.object.newLike"),"utf-8");
				message.setContent(html.body(), "text/html");

				
					sendMail(message);


			} catch (MessagingException e) {
                Logger.error("Error sending new like flash mail", e);
			}
	}


	public static void commentFlash(final models.comment.FollowedThread fThread, 
		final models.comment.Comment comment, final Flash flash, final Tribu tribu) {
			TypeComment typeComment = getTypeComment(fThread, comment, tribu);
			Html html = views.html.emails.inlined.NewCommentNotif.render(fThread, flash.flasher, comment, tribu, typeComment);
			Session session = buildMailService();

			String from = tribu.name + "<totem@wimha.com>";

			try {
				MimeMessage message = new MimeMessage(session);
				message.setHeader("X-Mailjet-Campaign", "comment-totem-"+ tribu.name);
				message.setHeader("MIME-Version", "1.0");
				message.setHeader("Content-Type", "text/html; charset=UTF-8");
				message.setFrom(new InternetAddress(from));
				message.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(fThread.getUser().email));
				message.setSubject(Messages.get("email.object.comment.FLASH."+typeComment),"utf-8");
				message.setContent(html.body(), "text/html; charset=UTF-8");

				
					sendMail(message);


			} catch (MessagingException e) {
                Logger.error("Error sending new coment mail", e);
				throw new RuntimeException(e);
			}
	}

    public static void deletionRequest(Flash flash) {
        Session session = buildMailService();
        Logger.error("deletion request "+ flash.id);
        String from = "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse("simon@wimha.com, julien@wimha.com"));
            message.setSubject("demande de suppression","utf-8");
            message.setContent("le flasheur veut supprimer son message d'id : "+ flash.id, "text/plain; charset=UTF-8");


            sendMail(message);


        } catch (MessagingException e) {
            Logger.error("Error sending deletion request mail", e);
        }

    }

    public static void dailyDigestScript() {
//        ArrayList<User> list=new ArrayList<User>();
//        list.add(User.findUserByEmail("jderenty@gmail.com"));
        for(User user : User.findAll()){
            if(NotificationPreference.isSubscribed(user, NotificationPreference.newFlashOnATotemYouFollow)){
                HashMap<Tribu, List<Flash>> lastMessages = Flash.getLastMessages(user);
                if(!lastMessages.isEmpty()) {
                    dailyDigest(user, lastMessages);
                }
            }
        }
    }

    public static void dailyDigest(User receiver, HashMap<Tribu, List<Flash>> lastMessages) {
		if ( play.Play.isProd() && !Application.VAL)
		{
            int nbNew = 0;
            for(Tribu tribu : lastMessages.keySet()){
                nbNew+=lastMessages.get(tribu).size();
            }

            if (StringUtils.isNotBlank(receiver.idFb)) {
                String template = nbNew + " new flashes on the tribes you follow ! ";
                Facebook.sendNotifToUser(receiver, template, "/myTotemPage/" + lastMessages.keySet().iterator().next().name); //TODO : point to last tribe but ideally : social feed logged
            }

            Logger.info("Sending daily mail to :" + receiver.email);
            sendDailyDigestMail(receiver, lastMessages);

		}
    }


    public static void sendDailyDigestMail(User receiver, HashMap<Tribu, List<Flash>> lastMessages){
        Html html = views.html.emails.inlined.dailyDigestMail.render(receiver, lastMessages);

        Session session = buildMailService();
        String From = "Wimha" + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "dailyDigest");
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(From));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(receiver.email));
            message.setSubject(Messages.get("email.object.dailyDigest"), "utf-8");
            message.setContent(html.body(), "text/html; charset=UTF-8");

            sendMail(message);

        } catch (MessagingException e) {
            Logger.error("Error sending daily digest mail", e);
        }

    }

    public static void blockedMember(Flash flash) {
        Tribu tribu= flash.tribu;
        Member member= flash.member;
        User user=member.user;

        Html html = views.html.emails.inlined.BlockedMember.render(tribu, member.user.firstname);
        Session session = buildMailService();
        String From = tribu.name + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "blockedmember-totem-" + tribu.name);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(From));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(user.email));
            message.setSubject(Messages.get("email.object.blocked"), "utf-8");
            message.setContent(html.body(), "text/html; charset=UTF-8");


            sendMail(message);

        } catch (MessagingException e) {
            Logger.error("Error sending blocked member mail", e);
        }
    }

    public static void markedFavorite(Flash flash, String messageOwner) {
        Tribu tribu= flash.tribu;
        Member member= flash.member;
        User user=flash.flasher;

        Html html = views.html.emails.inlined.favorite_flash.render(tribu, user.firstname, flash, messageOwner);
        Session session = buildMailService();
        String From = tribu.name + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "markedFavorite-totem-" + tribu.name);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(From));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(user.email));
            message.setSubject(Messages.get("email.object.favorite"), "utf-8");
            message.setContent(html.body(), "text/html; charset=UTF-8");


            sendMail(message);

        } catch (MessagingException e) {
            Logger.error("Error sending marked favorite mail", e);
        }
    }

    public static void ambassadorMail() {

        for(Tribu tribu : Tribu.findAll()){
            List<LikeFlashAction.UserLikes> mostLikedUsers = LikeFlashAction.findMostLikedUsers(tribu);
            if(!mostLikedUsers.isEmpty()){
                for(FollowedThread followedThread : FollowedThread.getSubscribedUserByThread(tribu.thread)){
                    sendAmbassadorMail(followedThread.getUser(), tribu, mostLikedUsers);
                }
            }
        }
    }

    private static void sendAmbassadorMail(User recipient, Tribu tribu, List<LikeFlashAction.UserLikes>  userLikes) {

        Html html = views.html.emails.inlined.ambassador_week.render(recipient, tribu, userLikes);

        Session session = buildMailService();
        String From = tribu.name + "<totem@wimha.com>";

        try {
            MimeMessage message = new MimeMessage(session);
            message.setHeader("X-Mailjet-Campaign", "ambassador-totem-" + tribu.name);
            message.setHeader("MIME-Version", "1.0");
            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            message.setFrom(new InternetAddress(From));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient.email));
            message.setSubject(Messages.get("email.object.ambassador"), "utf-8");
            message.setContent(html.body(), "text/html; charset=UTF-8");


            sendMail(message);

        } catch (MessagingException e) {
            Logger.error("Error sending marked ambassador mail", e);
        }
    }

    public static enum TypeComment{
		TO_OWNER,TO_FLASHER,TO_SUBSCRIBED_USER;
	}

	
	public static TypeComment getTypeComment(final FollowedThread followedThread,final Comment comment, Tribu tribu) {
		//why am i receiving a comment
		User receivingUser = followedThread.getUser();
		User sendingUser = comment.getUser();
		Flash flash = followedThread.getThread().flash;

		if(receivingUser.equals(tribu.owner)){
			return TypeComment.TO_OWNER;
		}else if (receivingUser.equals(flash.flasher)){
			return TypeComment.TO_FLASHER;
		}else{
			return TypeComment.TO_SUBSCRIBED_USER;
		}
	} 

}