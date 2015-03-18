package controllers.util;

import models.Password;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import models.util.AuthenticationMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ModifyAccountFormTotem {

	public String userId;

	@Required
	public String firstName;

	@Email
	public String email;

	public String newPassword;
	public String confirmNewPassword;


	public ModifyAccountFormTotem() {
	}

	public ModifyAccountFormTotem(final User user) {

		this.firstName = user.firstname;
		this.email = user.email;
	}

	

    private static final Pattern rfc2822 = Pattern.compile(
            "^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$"
    );

	public Map<String, List<ValidationError>> validate() {
		User user = User.findById(userId);

		Map<String, List<ValidationError>> errorList = new HashMap<String, List<ValidationError>>();
		
		List<ValidationError> emailErrors = new ArrayList<ValidationError>();
		if (email.isEmpty()) {
			emailErrors.add(new ValidationError("email", Messages
					.get("error.email.empty"), null));
		} else if (!rfc2822.matcher(email).matches()) {
            emailErrors.add(new ValidationError("email", Messages
					.get("error.email.invalid"), null));
        } else if (!user.email.equals(email)
				&& User.findUserByEmail(email) != null) {
			emailErrors.add(new ValidationError("email", Messages
					.get("error.email.alreadyUsed"), null));
		}
        if(!emailErrors.isEmpty()){
            errorList.put("email", emailErrors);
        }


		if (user.authenticationMethod == AuthenticationMethod.USERNAME_PASSWORD) {

			// if we want to change the password
			if (StringUtils.isNotEmpty(this.newPassword)
					|| !StringUtils.isNotEmpty(this.confirmNewPassword)) {

				List<ValidationError> newPasswordErrors = new ArrayList<ValidationError>();


				// if new passwords don't match
				if (!this.newPassword.equals(this.confirmNewPassword)) {
					newPasswordErrors
							.add(new ValidationError("newPassword", Messages
									.get("error.password.noMatch"), null));
					errorList.put("newPassword", newPasswordErrors);
				}

				// else if password length < 6 characters
				else if (this.newPassword.length() < 6) {
					newPasswordErrors
							.add(new ValidationError("newPassword", Messages
									.get("error.password.length"), null));
					errorList.put("newPassword", newPasswordErrors);
				}
			}
		}
		if (errorList.isEmpty()) {
			return null;
		} else {
			return errorList;
		}
	}


	public static void update(final User userToUpdate,	final ModifyAccountFormTotem form) {
		userToUpdate.firstname = form.firstName;
		if (userToUpdate.authenticationMethod
				.equals(AuthenticationMethod.USERNAME_PASSWORD)) {
			userToUpdate.email = form.email;
			if (!form.newPassword.equals("")) {
				userToUpdate.password = new Password(form.newPassword);
			}
		}

		// update in database
        userToUpdate.save();
	}
}
