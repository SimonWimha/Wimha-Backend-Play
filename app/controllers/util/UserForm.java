package controllers.util;

import models.User;
import play.Logger;
import play.data.validation.ValidationError;
import play.i18n.Messages;
import models.util.AuthenticationMethod;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserForm {

	@Valid
	public User user;
	
	public String password;

	public Map<String, List<ValidationError>> validate() {

		Map<String, List<ValidationError>> errorList = new HashMap<String, List<ValidationError>>();
		List<ValidationError> emailErrors = new ArrayList<ValidationError>();
		List<ValidationError> passwordErrors = new ArrayList<ValidationError>();

        if (user.email == null || user.email.isEmpty()) {
            emailErrors.add(new ValidationError("email", "Please enter your email ...", null));
            errorList.put("email", emailErrors);
        }else if (User.findUserByEmail(user.email) != null) {
            emailErrors.add(new ValidationError("email", Messages
                    .get("register.error.emailused"), null));
            errorList.put("email", emailErrors);
        }

        if(password == null || password.isEmpty()){
            passwordErrors.add(new ValidationError("password", "Please enter a password", null));
            errorList.put("password", passwordErrors);
        } else if( password.length() < 6){
            passwordErrors.add(new ValidationError("password", "At least 6 characters", null));
            errorList.put("password", passwordErrors);
        }

		return errorList;
	}

}
