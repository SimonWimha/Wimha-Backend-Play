package models;

import play.api.PlayException;
import play.api.libs.Crypto;
import play.data.validation.Constraints.Required;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Password {

	@Required
	@Column
	public String encryptedPassword;

	public Password(final String password) {
		encryptedPassword = encrypPassword(password);
	}

	public static String encrypPassword(final String password) {
		if (password == null || password.trim().isEmpty()) {
			return null;
		}
		try {
			// by default, the secret key comes from the application.conf file
			// (under application.secret entry)
			return Crypto.sign(password);
		} catch (PlayException e) {
			// if the key is not specified in conf, we use a hard coded key
			return Crypto.sign(password,
					new String("our secret key is wimha").getBytes());
		}

	}
}
