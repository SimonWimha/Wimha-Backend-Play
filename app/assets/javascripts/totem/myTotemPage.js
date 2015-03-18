$(document).ready(function() {
    initFeed();
    initFavoriteTab();
    initFBShare();
    initBtnDeleteFlash();
    displayQuestion();
    showFavoriteModal();
    initFavoriteSubmitBtn();
});

function displayQuestion(){
    var $question = $(".question");
    var $numWords = $question.text().length;

    if (($numWords >= 1) && ($numWords < 140)) {
        $question.css("font-size", "50px");
    }
    else if (($numWords >= 140) && ($numWords < 220)) {
        $question.css("font-size", "40px");
    }  
    else if (($numWords >= 220)) {
        $question.css("font-size", "30px");
    }  
}

function initFBShare(){
    $('.js_fbShare').on('click', function(){

        FB.ui({
          method: 'share',
          href: $(this).data('href')
        }, function(response){
            if (response && !response.error_code) {
            }
        });
    });
}


//to know if we are already waiting for an ajax request
var fetchingPage=false;

/**
 * Touchy infinite scrolling with two infinite tabs
 */
function initFeed(){

    $(window).bind('scroll', function(){
        //initialize infinite scrolling (dynamic data loading) on the container
        var a= $(window).scrollTop() +  $(window).height();
        var b = $(document).height()-500;
        if (a > b){
            getFeedNextPage();
        }

        //display button to scroll to if needed
        if($(this).scrollTop() != 0) {
            $('#toTop').fadeIn();
        } else {
            $('#toTop').fadeOut();
        }
    });

    $('#toTop').click(function() {
        $('body,html').animate({scrollTop:0},800);
        return false;
    });
}

var timers = new Array();
var noMoreFavorite = false;
var noMore = false;
function getFeedNextPage() {

    if(fetchingPage){
        return false;
    }
    fetchingPage=true;


    var container = $(".js_feed_container:not(.hidden)");
    var pagePrec=parseInt(container.data("page"));
    var pageSuiv=pagePrec+1;

    var favorite = container.hasClass("js_containerFavorite");
    if(favorite && noMoreFavorite){
        fetchingPage=false;
        return;
    }
    if(!favorite && noMore){
        fetchingPage=false;
        return;
    }
    container.append('<div class="row scrolLoader"><i class="fa fa-spinner fa-lg fa-spin"></i>' + Messages('status.loading') + '</div>');

    if(container.length > 0){
        $.ajax({
            url : "/getMyTPPage/"+pageSuiv+"?totemName="+container.data("totem_name")+"&favoriteOnly="+favorite,
            success:function(data) {
                container.find(".js_endOfList:last").before(data.html);
                container.data("page", pageSuiv);

                fetchingPage=false;

                for(timer in timers){
                    clearInterval(timer);
                }

                timers.push(setTimeout(function(){
                    $('.scrolLoader').remove();
                },3000));

                centerPictures();
                initClicPicture();
                initBtnLikeFlash();
                initBtnDeleteFlash();
                initFavoriteSubmitBtn();

                //Init facebook button
                initFBShare();

            },
            error:function(data, textStatus){
                fetchingPage=false;

                if(textStatus == 'timeout'){
                    $('.scrolLoader').html(Messages('error.server.connexion'));
                    loggr("error timeout ajax "+version, "error scrollFeed", data);
                }else if(data.status==404){
                    $('.scrolLoader').html(Messages('error.internet.connexion'));
                    loggr("no web "+version, "trace scrollFeed");
                }else if(data.status==400){
                    $('.scrolLoader').html(Messages('myTP.noMoreNews'));
                    if(favorite){
                        noMoreFavorite=true;
                    }else{
                        noMore=true;
                    }
                }else{
                    $('.scrolLoader').html(Messages('error.bug'));
                    loggr("error ajax"+version, "error scrollFeed", data);
                }
                timers.push(setTimeout(function(){
                    $('.scrolLoader').remove();
                },5000));

            }
        });
    }else{
        fetchingPage=false;
    }


}

/**
 * Totem Admin can mark favorite any flash (will appear in the tab favorite)
 */
var divToAddToFavorites;
function showFavoriteModal(){
    $('#favorite-modal').on('shown.bs.modal', function (e) {
        var favoriteBtn = $(e.relatedTarget);
        var flasherName = favoriteBtn.data('flasher-name');
        $('.flasherName').html(flasherName);

        //Copy infos of the button to the modal (to be send in ajax)
        var btnsConfirmation = $('.js_favoriteBtnWithEmail,.js_favoriteBtnNoEmail');
        btnsConfirmation.data('flash_id', favoriteBtn.data('flash_id'));
        btnsConfirmation.data('id_to_remove', favoriteBtn.data('id_to_remove'));
        divToAddToFavorites = favoriteBtn.parent().closest("li").clone();

        //Enable the button only after typing
        $(".js_favorite_mail_text").keydown(function() {
            $(this).closest('#favorite-modal').find('.js_favoriteBtnWithEmail').prop({
              disabled: false
            });
        });
    });
}

function initFavoriteTab(){
    $(".js_tabBtn").on("click", function(){
        $(".js_tabBtn").toggleClass("active");
        $(".js_feed_container").toggleClass("hidden");
    });
}

function initFavoriteSubmitBtn(){
    $(".js_favoriteBtnWithEmail, .js_favoriteBtnNoEmail,.js_unfavoriteBtn").on("click", function(){
        var btn=$(this);
        var flashId=btn.data("flash_id");
        var data;

        var value="true";
        var btnText=Messages("button.remove.favorite");
        if(btn.hasClass("js_unfavoriteBtn")){
            value="false";
            btnText = Messages("button.favorite");
        }else{
           data = { text : $('.js_favorite_mail_text').val() };
        }


        $.ajax({
            url : "/favorite/"+flashId+"/"+value,
            method : 'POST',
            data : data,
            success:function() {
                btn.text(btnText);
                if(btn.hasClass("js_unfavoriteBtn")){
                    $(".js_containerFavorite").find("#"+btn.data("id_to_remove")).remove();
                }else{
                    $(".js_containerFavorite").find(".js_endOfList:last").before(divToAddToFavorites);
                    $(".emptyTimeline").remove();
                }
            },
            error:function(data){
                btn.after(Messages('error.server.connexion'));
                loggr("error timeout ajax ", "error favorite", data);
            }
        });
    });
}

/**
 * Totem admin can delete any flash
 */
function initBtnDeleteFlash(){

    $(".timeline-panel")
	.mouseover(function(){
		$(this).find(".js_delete_button").removeClass('hide');
	})
	.mouseout(function() {
		if(!$(this).find(".dropdown").hasClass('open')){
			$(this).find(".js_delete_button").addClass('hide');
		}
  	});


    $('.js_delete_flash').on('click', function(){
    	var btn=$(this);

        $.ajax({
            url: '/delete_flash_owner/'+btn.data("flash-id"),
            type: 'GET',
            success: function () {
                btn.closest(".js_removed_when_delete").remove();
            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error deleteFlash");
		        }else if(data.status==404){
		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web "+version, "trace deleteFlash");
		        }else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error deleteFlash");
		        }

            },
            timeout: 10000
        });
    });


    $('.js_block_member_flash').on('click', function(){
    	var btn=$(this);

        $.ajax({
            url: '/block_member/'+btn.data("flash-id"),
            type: 'GET',
            success: function () {
                btn.closest(".js_removed_when_delete").animate({opacity: 0.25});
            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax "+version, "error deleteFlash");
		        }else if(data.status==404){
		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web "+version, "trace deleteFlash");
		        }else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax"+version, "error deleteFlash");
		        }

            },
            timeout: 10000
        });
    });
}
