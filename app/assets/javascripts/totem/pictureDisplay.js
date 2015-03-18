
/**
 * Anything about well display pictures in feeds (my totem page, social feed)
 */

 $(document).ready(function() {
	centerPictures();
	initClicPicture();
	bgBlured();
	totemBlockBlured();
	bg_upload();
});

function centerPictures(){
    $(".js_flashPicture").one('load', function() {
        centerPicture($(this));
    }).each(function() {
      if(this.complete) $(this).load();
    });
}

function centerPicture(picture){
	var	pictureWidth=picture.width();
	var pictureHeight=picture.height();
	if(pictureHeight > pictureWidth){
		if(picture.parent().hasClass("closed")){
			picture.css('margin-top',-pictureHeight / 3);
			picture.addClass('pointer');
		}
	}
}

function initClicPicture(){
	$(".timeline-heading").off();
	$(".timeline-heading").on("click",function(){
		if ($(this).hasClass("closed")) {
			$(this).removeClass("closed");
			$(this).find(".js_flashPicture").css('margin-top',0);
		}else{
			$(this).addClass("closed");
			centerPicture($(this).find(".js_flashPicture"));
		}
	})
}

function bgBlured() {
	$(window).scroll(function() {
	    if ($(window).width() < 991){
	    	var s = $(window).scrollTop(),
	    	opacityVal = (s / 1600);
	    }else{
	    	var s = $(window).scrollTop(),
	    	opacityVal = (s / 800);
	    }
	    $('.js_blur').css('opacity', opacityVal);
	});
}

function totemBlockBlured() { // pmax is the padding-bottom on scroll-top, it has to be the same in the CSS
	$(window).scroll(function() {
	     if ($(window).width() < 480){
	    	var s = $(window).scrollTop()
	    	var smax = 1700
	    	var pmin = 10 
	    	var pmax = 40,
	    	opacityVal = ((smax - s)/smax);
	    	padding = ((s - (pmax*smax)/(pmax-pmin))*((pmin-pmax)/smax));
	     }else{
	    	var s = $(window).scrollTop()
	    	var smax = 800
	    	var pmin = 10 
	    	var pmax = 170,
	    	opacityVal = ((smax - s)/smax);
	    	padding = ((s - (pmax*smax)/(pmax-pmin))*((pmin-pmax)/smax));
	     }
	    $('.js_blockBlur').css('opacity', opacityVal);
	    $('.js_blockBlur').css('padding-bottom',padding);
	});
}

function bg_upload(){
	$('.js_bg_upload').click(function(){
   	    document.getElementById("upfile").click();
   	});

    $("#upfile").change(function(){
        $("#bg_pic_form").submit();
    });
}