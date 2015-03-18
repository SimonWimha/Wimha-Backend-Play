$(document).ready(function() {
	initSubmitBtn();
});
var version = "v17";

function initSubmitBtn(){
    $('.js_btnSubmit').on('click', function(){

		var btn=$(this);
		btn.html('<i class="fa fa-refresh fa-spin"></i>'+btn.data("sending-message"));

    	//log beginning of the flash
		loggr("resetting pw "+version, "created resetpw");

    	// send form
        $.ajax({
            url: url,
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

						if(data.error.newPassword){
							errorFound=true;
							loggr("error password"+version, "trace resetpw");
							$(".js_error_pw").show();
							$(".js_error_pw").html(data.error.newPassword);
						}
						if(data.error.confirmNewPassword){
							errorFound=true;
							loggr("error password"+version, "trace resetpw");
							$(".js_error_pw_confirm").show();
							$(".js_error_pw_confirm").html(data.error.password);
						}
                        if(data.error.user){
                            errorFound=true;
                            loggr("error already resetted"+version, "trace resetpw");
							$(".js_btnSubmit").after("<p class='error'>" + data.error.user + "</p>");
                            setTimeout(function(){
                                location.href="/";
                            },2000);

                        }
						
						if(!errorFound){
							loggr("error unknow callback submit"+version, "error resetpw");
							$(".js_btnSubmit").after("<p class='error'>" + Messages('error.bug') + "</p>");
						}
						btn.html('<i class="fa fa-times"></i>'+btn.data("error-message"));
						timerErrorBtn(btn);
				} else{
			    	loggr("success "+version, "created resetpw");
					timerErrorBtn(btn);
					btn.html('<i class="fa fa-check"></i>'+btn.data("ok-message"));
					setTimeout(function(){
					    location.href="/";
					},2000);
				}
			
				
            },
            error: function(data, textStatus){
            console.log(data);
		        if(textStatus == 'timeout'){
		        	$(".js_btnSubmit").after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error resetpw");

		        }else{
					$(".js_btnSubmit").after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error resetpw");
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