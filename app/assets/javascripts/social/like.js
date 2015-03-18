$(document).ready(function() {
	initBtnLikeFlash();
	initBtnLikeTotem();

});

var sending=false;

function initBtnLikeFlash(){

     $('.js_like').on('click', function(){
     	var btn=$(this);
        if(btn.hasClass('js_feed')){
            btn.html(btn.data("message-unlike"));
        }else{
            btn.html('<i class="fa fa-heart"></i>');
        }

         if(sending){
             return false;
         }
         sending=true;
         $.ajax({
             url: '/like_flash/'+btn.data("flash-id"),
             type: 'GET',
             success: function () {
             sending=false;
             	var count_elt = btn.parent().parent().find(".js_count");
 				var count = parseInt(count_elt.text());
 				console.log(count);
 				count_elt.html(count+1);
 				btn.removeClass("js_like");
 				btn.addClass("js_unlike");
 				$(".js_like,.js_unlike").off();

                 setInterval(function(){
                     initBtnLikeFlash();
                 },2000);

                //Ass user pic in myTP on the flash picture
                btn.closest("li").find(".js_likeList").append($(".js_userPicLike").html());

                likeFbFlash(btn.data("flash-id"));

             },
             error: function(data, textStatus){
 		        if(textStatus == 'timeout'){
 		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
 					loggr("error timeout ajax ", "error likeFlash");
 		        }else if(data.status==404){
 		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
 					loggr("no web ", "trace likeFlash");
 		        }else{
 					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
 		        	loggr("error ajax", "error likeFlash");
 		        	console.log(data);
 		        }

             },
             timeout: 10000
         });
     });

     $('.js_unlike').on('click', function(){
     	var btn=$(this);

        if(btn.hasClass('js_feed')){
            btn.html(btn.data("message-like"));
        }else{
            btn.html('<i class="fa fa-heart-o"></i>');
        }

         if(sending){
             return false;
         }
         sending=true;
         $.ajax({
             url: '/unlike_flash/'+btn.data("flash-id"),
             type: 'GET',
             success: function () {
             sending=false;
             	var count_elt = btn.parent().parent().find(".js_count");
 				var count = parseInt(count_elt.text());
 				count_elt.html(count-1);
 				btn.removeClass("js_unlike");
 				btn.addClass("js_like");
 				$(".js_like,.js_unlike").off();

                 setInterval(function(){
                     initBtnLikeFlash();
                 },2000);
             },
             error: function(data, textStatus){
 		        if(textStatus == 'timeout'){
 		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
 					loggr("error timeout ajax ", "error unlikeFlash", data);
 		        }else if(data.status==404){
 		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
 					loggr("no web ", "trace unlikeFlash");
 		        }else{
 					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
 		        	loggr("error ajax", "error unikeFlash", data);
 		        }
             },
             timeout: 10000
         });
     });
 }
function initBtnLikeTotem(){

    $('.js_like_totem').on('click', function(){
    	var btn=$(this);
	    btn.html(btn.data("message-unlike"));
        if(sending){
            return false;
        }
        sending=true;
        $.ajax({
            url: '/like_totem/'+btn.data("totem-id"),
            type: 'GET',
            success: function () {
            sending=false;
            	var count_elt = $(".js_count_like_totem");
				var count = parseInt(count_elt.text());
				console.log(count);
				count_elt.html(count+1);
				btn.removeClass("js_like_totem");
				btn.addClass("js_unlike_totem");
				$(".js_like_totem,.js_unlike_totem").off();
                setInterval(function(){
				    initBtnLikeTotem();
                },2000);

                likeFb(btn.data("totem_name"));
           },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error likeTotem", data);
		        }else if(data.status==404){
		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web ", "trace likeTotem");
		        }else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error likeTotem", data);
		        }

            },
            timeout: 10000
        });
    });

    $('.js_unlike_totem').on('click', function(){
    	var btn=$(this);
        btn.html(btn.data("message-like"));
        if(sending){
            return false;
        }
        sending=true;
        $.ajax({
            url: '/unlike_totem/'+btn.data("totem-id"),
            type: 'GET',
            success: function () {
                sending=false;
            	var count_elt = $(".js_count_like_totem");
            	btn.html(btn.data("message-like"));
				var count = parseInt(count_elt.text());
				count_elt.html(count-1);
				btn.removeClass("js_unlike_totem");
				btn.addClass("js_like_totem");
				$(".js_like_totem,.js_unlike_totem").off();
                setInterval(function(){
				    initBtnLikeTotem();
                },2000);
            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error unlikeTotem", data);
		        }else if(data.status==404){
		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web ", "trace unlikeTotem");
		        }else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error unlikeTotem", data);
		        }
            },
            timeout: 10000
        });
    });
}