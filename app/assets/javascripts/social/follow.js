$(document).ready(function() {
	initBtnFollow();
});

function initBtnFollow(){

    $('.js_follow').on('click', function(){
    var btn=$(this);
  
	        $.ajax({
	            url: '/subscribeuser/thread/'+btn.data("thread-id"),
	            type: 'GET',
	            success: function () {
	            	var count_elt = btn.closest(".followButton").parent().find(".js_count");
	            	btn.html(btn.data("message-unfollow"));
					var count = parseInt(count_elt.text());
					console.log(count);
					count_elt.html(count+1);
					btn.removeClass("js_follow");
					btn.addClass("js_unfollow");
					$(".js_follow,.js_unfollow").unbind();
                    setTimeout(function(){
                        initBtnFollow();
                    },1000);

                    followFb(btn.data("totem_name"));

	            },
	            error: function(data, textStatus){
			        if(textStatus == 'timeout'){
			        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
						loggr("error timeout ajax ", "error follow");
				    }else if(data.status==404){
			        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
						loggr("error no web ", "trace follow");
			        }else{
						btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
			        	loggr("error ajax", "error follow");
			        	console.log(data);
			        }

	            },
	            timeout: 10000
	        });
    });

    $('.js_unfollow').on('click', function(){
    var btn=$(this);
  
	        $.ajax({
	            url: '/unsubscribeuser/thread/'+btn.data("thread-id"),
	            type: 'GET',
	            success: function () {
	            	var count_elt = btn.closest(".followButton").parent().find(".js_count");
	            	btn.html(btn.data("message-follow"));
					var count = parseInt(count_elt.text());
					count_elt.html(count-1);
					btn.removeClass("js_unfollow");
					btn.addClass("js_follow");
					$(".js_follow,.js_unfollow").unbind();
                    setTimeout(function(){
                        initBtnFollow();
                    },1000);
	            },
	            error: function(data, textStatus){
			        if(textStatus == 'timeout'){
			        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
						loggr("error timeout ajax ", "error follow");
				    }else if(data.status==404){
			        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
						loggr("error no web ", "trace follow");
			        }else{
						btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
			        	loggr("error ajax", "error follow");
			        	console.log(data);
			        }

	            },
	            timeout: 10000
	        });
    });
}