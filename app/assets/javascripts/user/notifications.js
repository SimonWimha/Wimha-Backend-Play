$(document).ready(function() {
	initBtns();
});
var version = "v17";

function initBtns(){
	$('input').on('switch-change',function(){

		var btn=$(this);
		loggr("changing settings ", "created notifications");
		var url;
		var group=btn.data("group");
		if(btn.is(':checked')){
			url='/subscribeNotification/';
		}else{
			url='/unsubscribeNotification/';
		}
		console.log(url+group);
		$.ajax({
			url: url+group,
			type: 'GET',
			success: function (data) {
				$('.error').remove();
				loggr("success changed "+ group+ " " +version, "created notifications");
			},
			error: function(data, textStatus){
				if(textStatus == 'timeout'){
					btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error notifications");
				}else if(data.status==404){
					btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web "+version, "trace notifications");
				}else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
					loggr("error ajax"+version, "error notifications");
				}
			},
			timeout: 10000
		});
	});
}

