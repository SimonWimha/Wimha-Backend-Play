$(document).ready(function() {
    initFeed();
    initSuggestions();
    if($(".js_tohideWhenFollowed").length==0){
        $(".js_removedWhenEmpty").remove();
    }
});


/**
 * On feed page you have suggestions of totem to follow
 */
function initSuggestions(){

    $(".js_follow").on("click",function(){

        var btn=$(this);
        refresh();
        btn.closest(".js_tohideWhenFollowed").addClass('disapear');
        setTimeout(function(){
            btn.closest(".js_tohideWhenFollowed").remove();
            
            if($(".js_tohideWhenFollowed").length==0){
                $(".js_removedWhenEmpty").remove();
            }

        },1000);


    });
}


/**
 * Refresh the feed
 */
function refresh(){
    var container = $(".js_feed_container");
    container.html("");
    setTimeout(function(){

        $.ajax({
            url : "/getFeedPage/1",
            success:function(data) {
                container.append(data.html);
                container.data("page", "0");

                fetchingPage=false;
                initBtnLikeFlash();

                for(timer in timers){
                    clearInterval(timer);
                }

                timers.push(setTimeout(function(){
                    $('.scrolLoader').remove();
                },3000));

                initSuggestions();
            },
            error:function(data, textStatus){
            console.log(data + " " + textStatus);
                if(textStatus == 'timeout'){
                    $('.scrolLoader').html(Messages('error.server.connexion'));
                    loggr("error timeout ajax ", "error scrollFeed", data);
                }else if(data.status==404){
                    $('.scrolLoader').html(Messages('error.internet.connexion'));
                    loggr("no web ", "trace scrollFeed");
                }else if(data.status==400){
                    $('.scrolLoader').html(Messages('socialFeed.noMoreNews'));
                }else{
                    $('.scrolLoader').html(Messages('error.bug'));
                    loggr("error ajax", "error scrollFeed", data);
                }
                timers.push(setTimeout(function(){
                    $('.scrolLoader').remove();
                },8000));
            }
        });


    },100);
}

//to know if we are already waiting for an ajax request
var fetchingPage=false;

/**
 * Initialisation of infinite scrolling
 */
function initFeed(){

    $(window).bind('scroll', function(){
        //initialize infinite scrolling (dynamic data loading) on the container
        var a= $(window).scrollTop() +  $(window).height();
        var b = $(document).height()-200;
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
function getFeedNextPage() {

    if(fetchingPage){
        return false;
    }
    fetchingPage=true;


    var container = $(".js_feed_container");
    var pagePrec=parseInt(container.data("page"));
    var pageSuiv=pagePrec+1;

    container.append('<div class="row scrolLoader"><i class="fa fa-spinner fa-lg fa-spin"></i>' + Messages('status.loading') + '</div>');

    $.ajax({
        url : "/getFeedPage/"+pageSuiv,
        success:function(data) {
            container.append(data.html);
            container.data("page", pageSuiv);

            fetchingPage=false;
            initBtnLikeFlash();

            for(timer in timers){
                clearInterval(timer);
            }

            timers.push(setTimeout(function(){
                $('.scrolLoader').remove();
            },3000));

            centerPictures();
            initClicPicture();
        },
        error:function(data, textStatus){
        console.log(data + " " + textStatus);
            if(textStatus == 'timeout'){
                $('.scrolLoader').html(Messages('error.server.connexion'));
                loggr("error timeout ajax ", "error scrollFeed", data);
            }else if(data.status==404){
                $('.scrolLoader').html(Messages('error.internet.connexion'));
                loggr("no web ", "trace scrollFeed");
            }else if(data.status==400){
                $('.scrolLoader').html(Messages('socialFeed.noMoreNews'));
            }else{
                $('.scrolLoader').html(Messages('error.bug'));
                loggr("error ajax", "error scrollFeed", data);
            }
        }
    });


}