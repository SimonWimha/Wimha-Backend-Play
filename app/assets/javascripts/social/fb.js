$(document).ready(function() {
    initPostSettings();
});

/**
 * In settings page, totem owner can activate autopost of his totem's flashes on facebook
 */
function initPostSettings(){
	$('input').on('switch-change',function(){

		var btn=$(this);
		loggr("changing postfb for totem "+totem_name, "created postfb");
		var url;
		var totem_name=btn.data("totem_name");
		if(btn.is(':checked')){
            url= "/facebook/post/"+totem_name;
		}else{
            url= "/facebook/nopost/"+totem_name;
        }

		$.ajax({
			url: url,
			type: 'GET',
			success: function () {
				$('.error').remove();
				loggr("success changed  " , "created postfb");
			},
			error: function(data, textStatus){
				if(textStatus == 'timeout'){
					btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error postfb");
				}else if(data.status==404){
					btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web ", "trace postfb");
				}else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
					loggr("error ajax", "error postfb");
				}
			},
			timeout: 10000
		});
	});
}


/**
 * Any logged user can like any totem which will like also the page on facebook
 */
function likeFb(totem_name){
        loggr("posting like fb "+totem_name+ " " , "created likefb");

        FB.getLoginStatus(function(response) {
            if (response.status === 'connected') {
                FB.api(
                    "/me/og.likes",
                    "POST",
                    {
                            "object": "http://www.wimha.com/myTotemPage/"+totem_name

                    },
                    function (response) {
                    console.log(response);
                      if (response && !response.error) {
                            loggr("success like fb ", "created likefb");
                      }
                    }
                );

            } else if (response.status === 'not_authorized') {
                loggr("error like fb not authorized ", "trace likefb");
            } else {
                loggr("error like fb not connected ", "trace likefb");
            }
        });
}

/**
 * Any logged user can like any flash which will like also the page on facebook
 */
function likeFbFlash(flashId){
        loggr("posting like fb "+flashId, "created likefb");

        FB.getLoginStatus(function(response) {
            if (response.status === 'connected') {
                FB.api(
                    "/me/og.likes",
                    "POST",
                    {
                            "object": "http://www.wimha.com/commentPage/"+flashId

                    },
                    function (response) {
                    console.log(response);
                      if (response && !response.error) {
                            loggr("success like fb ", "created likefb");
                      }
                    }
                );

            } else if (response.status === 'not_authorized') {
                loggr("error like fb not authorized ", "trace likefb");
            } else {
                loggr("error like fb not connected ", "trace likefb");
            }
        });
}

/**
 * Any logged user can follow any totem which will appear on facebook as an open graph action
 * TODO : Create the opengraph action and submit it for review : not working now !
 */
function followFb(totem_name){
        loggr("posting follow fb "+totem_name+ " " , "created likefb");
        FB.getLoginStatus(function(response) {
            if (response.status === 'connected') {
                FB.api(
                  'me/wimhapp:follow_the_totem',
                  'post',
                  {
                    totem: "http://www.wimha.com/myTotemPage/"+totem_name
                  },
                  function(response) {
                  console.log(response);
                        loggr("success follow fb ", "created likefb");
                  }
                );

            } else if (response.status === 'not_authorized') {
                loggr("error follow fb not authorized ", "trace likefb");
            } else {
                loggr("error follow fb not connected ", "trace likefb");
            }
        });
}



