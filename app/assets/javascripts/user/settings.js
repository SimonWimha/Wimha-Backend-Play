$(document).ready(function() {
	initSubmitBtn();
	initSubmitBtnTotem();
	initSendNotif();

	$('.js_profilPicUpload').click(function(){
	    document.getElementById("upfile").click();
	});
	
	$("#upfile").change(function(){
		$("#pic_form").submit();
	});
	cleanInputOnFocus();
	pictureMaxSize();
	switchNotif();
});
var version = "v17";

function pictureMaxSize(){
	$('.js_profilPicUpload')
		.mouseover(function(){
			$('.js_pictureMaxSize').css('opacity',1);})
		.mouseout(function(){
			$('.js_pictureMaxSize').css('opacity',0);})
}

function cleanInputOnFocus() {
	$("input").focus(function(){$('.error-message').css('display','none')});
	$("input").focus(function(){$('.error-textarea').css('display','none')});
}

function initSubmitBtnTotem(){
    $('.js_submitTotem').on('click', function(){
  		var btn=$(this);

     	$('.error-message,.error,.js_totem_question_error').hide();
		btn.html('<i class="fa fa-refresh fa-spin"></i>'+btn.data("sending-message"));

 		var totemForm=btn.closest(".totemForm");

	   	// send form
        $.ajax({
            url: '/settingsPageTotem/',
            type: 'POST',
            data : btn.closest(".totemForm").serializeArray(),
            //add beforesend handler to validate or something
            //beforeSend: functionname,
            success: function (data) {
            	console.log(data);
            	if(data.error!=null){
	            	if(data.error==="questionEmpty"){
						loggr("totem question empty "+version, "trace", data);
	            		totemForm.find(".js_totem_question_error").show();
	            		btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
						timerErrorBtn(btn);    
					}
					if(data.error==="emailInvalid"){
	            		loggr("email vcard invalid "+version, "trace", data);
	            		totemForm.find(".js_totem_ownerEmail_error").show();
	            		btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
						timerErrorBtn(btn); 						
					}        		
            	}else{
			    	loggr("success "+version, "created settings");
					timerErrorBtn(btn);
					btn.html('<i class="fa fa-check"></i>'+btn.data("ok-message"));
            	}
            },
            error: function(data, textStatus){
            	if(textStatus == 'timeout'){
		        	$(".js_submitTotem").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error settings", data);

		        }else{
					$(".js_submitTotem").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error settings", data);
		        }
		        btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
				timerErrorBtn(btn);
            },
            timeout: 10000
        });
    });
}

function initSendNotif(){
    $('.js_submitNotif').on('click', function(){
  		var btn=$(this);

     	$('.error-message,.error,.js_totem_question_error').hide();
		btn.html('<i class="fa fa-refresh fa-spin"></i>'+btn.data("sending-message"));

 		var totemForm=btn.closest(".totemForm");

	   	// send form
        $.ajax({
            url: '/settingsPageTotem/notif/',
            type: 'POST',
            data : btn.closest(".totemForm").serializeArray(),
            //add beforesend handler to validate or something
            //beforeSend: functionname,
            success: function (data) {
            	console.log(data);
            	if(data.error!=null){
	            	if(data.error==="questionEmpty"){
						loggr("totem question empty "+version, "trace", data);
	            		totemForm.find(".js_totem_question_error").show();
	            		btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
					}
                    if(data.error==="alreadysent"){
                        loggr("totem notif already sent "+version, "trace", data);
		                btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
					    btn.after('<p class="centered">' + Messages('settings.totem.nottif.alreadysent')+'</p>')
                    }
            	}else{
			    	loggr("success "+version, "created settings");
					btn.html('<i class="fa fa-check"></i>'+btn.data("ok-message"));
					btn.after('<p class="centered">' + data.cpt + ' ' + Messages('settings.totem.nottif.sent')+'</p>')
            	}
            },
            error: function(data, textStatus){
            	if(textStatus == 'timeout'){
		        	$(".js_submitTotem").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error settings", data);

		        }else{
					$(".js_submitTotem").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error settings", data);
		        }
		        btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
            },
            timeout: 10000
        });
    });
}

function initSubmitBtn(){
    $('.js_btnSubmit').on('click', function(){

        $("#contact-mail").val($("#contact-mail").val().toLowerCase());
		var btn=$(this);
		btn.html('<i class="fa fa-refresh fa-spin"></i>'+btn.data("sending-message"));

    	//log beginning of the flash
		loggr("modifying settings "+$("#contact-mail").val()+" "+version, "created settings");

    	// send form
        $.ajax({
            url: '/settingsPage/',
            type: 'POST',
            data : $(".contact-form").serializeArray(),
            //add beforesend handler to validate or something
            //beforeSend: functionname,
            success: function (data) {
    			$('.error-message').hide();
    			console.log(data);


         		//erros from validation form
            	if(data.error!=null){
					
						var errorFound = false;
						if(data.error.firstName){
							errorFound=true;
							loggr("error name"+version, "trace settings");
							$(".js_name_error").show();
						}
						if(data.error.email){
							errorFound=true;
							loggr("error mail"+version, "trace settings");
							console.log(data.error["user.email"]);
							$(".js_email_error").show();
							$(".js_email_error").html(data.error.email);
						}
						if(data.error.newPassword){
							errorFound=true;
							loggr("error password"+version, "trace settings");
							console.log("show");
							$(".js_error_pw").show();
							$(".js_error_pw").html(data.error.newPassword);
						}
						if(data.error.confirmNewPassword){
							errorFound=true;
							loggr("error password"+version, "trace settings");
							console.log("show");
							$(".js_error_pw_confirm").show();
							$(".js_error_pw_confirm").html(data.error.password);
						}
						
						if(!errorFound){
							loggr("error unknow callback submit"+version, "error settings");
							$(".js_btnSubmit").after("<p class='error'>" + Messages('error.bug') + "</p>");
						}
						btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
						timerErrorBtn(btn);
				} else{
					console.log("success");
			    	loggr("success "+version, "created settings");
					timerErrorBtn(btn);
					btn.html('<i class="fa fa-check"></i>'+btn.data("ok-message"));
				}
			
				
            },
            error: function(data, textStatus){
            console.log(data);
		        if(textStatus == 'timeout'){
		        	$(".js_btnSubmit").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error settings");

		        }else{
					$(".js_btnSubmit").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error settings");
		        }
		        btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
				timerErrorBtn(btn);
            },
            timeout: 10000
        });
    });
}


function timerErrorBtn(btn){
	setTimeout(function(){
		btn.html('Save');
		btn.width('auto');
	},2000);
}

function switchNotif(){
	$('.js_displayCheckboxNotif').focus(function(){
		$(this).closest("form").find('.notifModifQuestion').removeClass('hide');
	});

	$('.notifModifQuestion').find("input").on('switch-change',function(){
		if($(this).is(':checked')){
            $(this).val("true");
            console.log("true");
		}else{
            $(this).val("false");
            console.log("false");
		}
    });
}



