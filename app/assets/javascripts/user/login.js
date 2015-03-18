$(document).ready(function() {
	initLoginBtn();
	initCheckMail();
	initResetBtn();
	initResetSubmitBtn();
	initSubmitBtn();
	initGiveMailBtn();
	cleanInputOnFocus();
	backToLogin();
	
	$('input').bind('keypress', function(e) {
	   if( e.which === 13 )
	       return false;
	});
});

var btnLogin = $('.js_btnLogin'),
	btnResetPwd = $('.resetContainer .js_resetPasswordSend');

// to display feedbacks
var	buttonCopy = btnLogin.html(),
	errorMessage = btnLogin.data('error-message'),
	sendingMessage = btnLogin.data('sending-message'),
	okMessage = btnLogin.data('ok-message'),
	sendingPassword = btnResetPwd.data('sending-message');

function cleanInputOnFocus() {
	$("input").focus(function(){$('.login-error-message').css('display','none')});
}

function timerErrorBtn(){
	setTimeout(function(){
		btnLogin.html(buttonCopy);
		btnLogin.width('auto');
	},2000);
}
function timerErrorBtnReset(){
	setTimeout(function(){
		btnResetPwd.html(buttonCopy);
		btnResetPwd.width('auto');
	},2000);
}

function initLoginBtn(){
	$("header").find('.js_headerBrand').removeClass('col-md-6');
	$("header").find('.js_headerBrand').addClass('col-md-12');
	$("header").find('.js_headerBtn').hide();
}

function initCheckMail(){
	$("#contact-mail").keyup(checkMail);
	$("#contact-mail").blur(checkMail);
}

function checkMail(){
	var reg = new RegExp('^[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*@[a-z0-9]+([_|\.|-]{1}[a-z0-9]+)*[\.]{1}[a-z]{2,6}$', 'i');
	$(this).val($(this).val().replace(/\s/g, ''));
    $(this).val($(this).val().toLowerCase());

	if(reg.test($(this).val())){
		$.ajax({
			url: "/checkMail/"+$(this).val(),
			type: "GET",
			success: function(data) {
				if(data.exist==="true"){
					if(data.type==="fbc"){
						$(".js_onlyWhenNew").hide();							
						$(".js_onlyWhenExists").hide();							
						$(".js_email_error").show();
						$(".js_email_error").addClass("login-error-message");
						$(".js_email_error").html(Messages('error.login.fbc'));
					}else{
						$(".js_onlyWhenNew").hide();							
						$(".js_onlyWhenExists").show();
					}
				}else{
					$(".js_onlyWhenExists").hide();
					$(".js_onlyWhenNew").show();
				}
			},
			error: function() {
				loggr("error checking mail", "error login");
			}
		});
	}
}

function initResetBtn(){
    $('.js_resetPassword').on('click', function(){
    	$('.resetContainer').show();
    	$('.loginContainer').hide();
        $('.resetContainer').find("#reset-mail").val($("#contact-mail").val().toLowerCase());
    });
}

function backToLogin(){
	$('.js_backToLogin').on('click',function(){
    	$('.resetContainer').hide();
    	$('.loginContainer').show();
    	$('.passwordInput').show();
        $('.contact-form').find("#contact-mail").val($("#reset-mail").val().toLowerCase());
	});
}

function initResetSubmitBtn(){
    $('.js_resetPasswordSend').on('click', function(){
        btnResetPwd.html('<i class="fa fa-refresh fa-spin"></i>'+sendingPassword);

        var emailReset = $("#reset-mail").val();
        
		var re = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
		   
	    if(re.test(emailReset)){
	        $.ajax({
	            url: '/sendmail/',
	            type: 'POST',
	            data : {email: $("#reset-mail").val()},
	            //add beforesend handler to validate or something
	            //beforeSend: functionname,
	            success: function () {
                    $(".resetContainer").find(".success-message").show();
                    btnResetPwd.html('<i class="fa fa-check"></i>'+okMessage);
                    setInterval(function(){
                        $('.js_btnSwitch').removeClass('js_resetPasswordSend');
                        $('.js_btnSwitch').addClass('js_backToLogin');
                        $('.js_btnSwitch').html(Messages("button.login"));
                        $('.js_btnSwitch').unbind();
                        backToLogin();						
                    },2000);
	            },
	            error: function(){
                    btnResetPwd.html('<i class="fa fa-times"></i>'+ Messages('error.login.noAccount'));
	            },
	            timeout: 10000
	        });
		}else{
			$('.js_emailReset_error').show();
			btnResetPwd.html(errorMessage);
			timerErrorBtnReset();
		}
    });
}

function initSubmitBtn(){
    $('.js_login').on('click', function(){
        $("#contact-mail").val($("#contact-mail").val().toLowerCase());

		btnLogin.html('<i class="fa fa-refresh fa-spin"></i>'+sendingMessage);

    	//log beginning of the flash
		loggr("trying login or register "+$("#contact-mail").val(), "created login");


    	// send form
        $.ajax({
            url: '/register/',
            type: 'POST',
            data : $(".contact-form").serializeArray(),
            //add beforesend handler to validate or something
            //beforeSend: functionname,
            success: function (data) {
    			$('.login-error-message').hide();

	         		//erros from validation form
	            	if(data.error!=null){

							var errorFound = false;
							if(data.error["user.firstname"]){
								errorFound=true;
								loggr("error name", "trace login");
								$(".js_name_error").show();
							}
							if(data.error["user.email"]){
								errorFound=true;
								loggr("error mail", "trace login");
								console.log(data.error["user.email"]);
								$(".js_email_error").show();
								$(".js_email_error").html(data.error["user.email"]);
							}
							if(data.error.password){
								errorFound=true;
								loggr("error password", "trace login");
								console.log("show");
								$(".js_error_pw").show();
								$(".js_error_pw").html(data.error.password);
							}

							if(!errorFound){
								loggr("error unknow callback submit", "error login", data);
								$(".btn-login").after("<p class='error'>" + Messages('error.bug') + "</p>");
							}
							btnLogin.html(errorMessage);
							timerErrorBtn();
					} else{

					    if(data.type ==="login"){
					    	loggr("successful login ", "created login");
							$(".success-message.login").show();
							timerErrorBtn();

					    }else{
					    	loggr("successful register ", "created login");
							$(".success-message.register").show();
							btnLogin.html('<i class="fa fa-check"></i>'+okMessage);

					    }

						var url = $("#user_form").data("originalurl");
						setTimeout(function(){
							if(url !== ""){
								location.href=url;
							}else{
								location.reload();
							}
						},2000);

					}


            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	$(".btn-login").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error login");

		        }else{
					$(".btn-login").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error login");
		        }
		        btnLogin.html(errorMessage);
				timerErrorBtn();
            },
            timeout: 10000
        });
    });
}


function initGiveMailBtn(){
    var btnLogin = $('.js_give_mail');
    btnLogin.on('click', function(){
        $("#giveEmail").val($("#giveEmail").val().toLowerCase());

	    sendingMessage = btnLogin.data('sending-message');
		btnLogin.html('<i class="fa fa-refresh fa-spin"></i>'+sendingMessage);

    	//log beginning of the flash
		loggr("trying login or register "+$("#contact-mail").val(), "created login");

    	// send form
        $.ajax({
            url: '/givemail/',
            type: 'POST',
            data : $(".contact-form").serializeArray(),
            //add beforesend handler to validate or something
            //beforeSend: functionname,
            success: function (data) {
    			$('.login-error-message').hide();

                //erros from validation form
                if(data.error!=null){
                        var errorFound = false;
                        if(data.error["user.email"]){
                            errorFound=true;
                            loggr("error mail", "trace login");
                            console.log(data.error["user.email"]);
                            $(".js_email_error").show();
                            $(".js_email_error").html(data.error["user.email"]);
                        }

                        if(!errorFound){
                            loggr("error unknow callback submit", "error login", data);
                            $(".btn-login").after("<p class='error'>" + Messages('error.bug') + "</p>");
                        }
                        btnLogin.html(errorMessage);
                        timerErrorBtn();
                } else{


                    var url = $("#missing_email").data("originalurl");
                    setTimeout(function(){
                        if(url !== ""){
                            location.href=url;
                        }else{
                            location.reload();
                        }
                    },2000);

                }


            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	$(".btn-login").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error login");

		        }else{
					$(".btn-login").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error login");
		        }
		        btnLogin.html(errorMessage);
				timerErrorBtn();
            },
            timeout: 10000
        });
    });
}

function getCookie(cname)
{
var name = cname + "=";
var ca = document.cookie.split(';');
for(var i=0; i<ca.length; i++) 
  {
  var c = ca[i].trim();
  if (c.indexOf(name)==0) return c.substring(name.length,c.length);
  }
return "";
}

/***************  Facebook connect  ***********************/

var btnFBC = $(".js_fbc");

window.fbAsyncInit = function() {
    FB.init({
        appId      : '137474053062029',
        cookie     : true,  // enable cookies to allow the server to access
                            // the session
        xfbml      : true,  // parse social plugins on this page
        version    : 'v2.1' // use version 2.1
    });


    $('.js_fbc').on('click', function(){
		btnFBC.html('<i class="fa fa-refresh fa-spin"></i>'+sendingMessage);

        FB.login(function(response) {
          if (response.status === 'connected') {

            var token=response.authResponse.accessToken;

            FB.api('/me', function(response) {
                if(response.email != undefined){
                    doFacebookConnect(response.name, response.email, response.id, token);
                }else{
                    doFacebookConnect(response.name, "", response.id, token);
                }
            });

          } else if (response.status === 'not_authorized') {
            // The person is logged into Facebook, but not your app.
            console.log(JSON.stringify(response));
		    btnFBC.html('<i class="fa fa-times"></i>'+'You must accept Wimha app on Facebook :)');

          } else {
            // The person is not logged into Facebook, so we're not sure if
            // they are logged into this app or not.
            console.log(JSON.stringify(response));
		    btnFBC.html('<i class="fa fa-times"></i>'+'You must accept :)');

          }
        });

    });
};

function doFacebookConnect(name, email, fbId, fbToken){
    var originUrl = $("#user_form").data("originalurl");

    // send form
    $.ajax({
        url: '/doFacebookConnect/',
        type: 'POST',
        data : {name:name, email:email, fbId:fbId, fbToken:fbToken},
        success: function(data) {
            if(data.hasEmail==="true"){

                setTimeout(function(){
                    btnFBC.html('<i class="fa fa-check"></i>'+okMessage);

                    if(originUrl !== ""){
                        location.href=originUrl;
                    }else{
                        location.reload();
                    }
                },2000);

            }else{
                location.href="/missingEmail/"+encodeURIComponent(originUrl);
            }

        },
        error: function(data, textStatus){
            if(textStatus == 'timeout'){
                $(".js_fbc").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
                loggr("error timeout ajax ", "error fbc");

            }else{
                $(".js_fbc").after("<p class='error'>" + Messages('error.bug') + "</p>");
                loggr("error ajax", "error fbc");
            }
            $(".js_fbc").html(errorMessage);
            timerErrorBtn();
        },
        timeout: 10000
    });

}
