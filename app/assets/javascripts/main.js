$(document).ready(function() {
	$('html,body').scrollTop(0);
	$('.js_signup_redirect').on('click', function(){
		location.href="/login/"+encodeURIComponent(location.href);
	});

	fadingImages();
	chevronAnimated();
	initSideBar();
});


// header

$("#dropdownBtn").on("click",function(){
	var dropDown = $('.dropdownHeader');
	if (dropDown.hasClass("hide")) {
		$('.dropdownLang').addClass('hide');
		dropDown.removeClass('hide');
		$("header").find(".focusedBtn").addClass('open');
	}else{
		dropDown.addClass('hide');
		$("header").find(".focusedBtn").removeClass('open');
	}
});

$("#langBtn").on("click",function(){
	var dropDownLang = $('.dropdownLang');
	if (dropDownLang.hasClass("hide")) {
		$('.dropdownHeader').addClass('hide');
		$("header").find(".focusedBtn").removeClass('open');
		dropDownLang.removeClass('hide');
	}else{
		dropDownLang.addClass('hide');
	}
	
});

$("#moreTotems").on("click",function(){
   $("#hiddenTotems").show();
   $("#moreTotems").hide();
});


// Home Page

function fadingImages(){
	$('.js_changePicture').each(function(){
	    (function($set){
	        setInterval(function(){
	            var $cur = $set.find('.active').removeClass('active');
	            var $next = $cur.next().length?$cur.next():$set.children().eq(0);
	            $next.addClass('active');
	        },3000);
	    })($(this));
	});
}


// MyTP

function chevronAnimated(){
	if($(window).height()>'700'){
		setTimeout(function(){
			$('.fa-chevron-down').removeClass('hide');
			$('.christmasBanner').removeClass('hide');
		}, 3000);
	}

	$('.js_scrollDown').on('click', function(e){
	    e.preventDefault();
	    if($(this).hasClass('js_toMap')){
	    	var target= $('#map-canvas');
	    }else if($(this).hasClass('js_toFollowers')){
	    	var target= $('#followersPage');
	    }else if($(this).hasClass('js_toComments')){
	    	var target= $('#commentsPage');
	    }else{
	    	var target= $('#flash_feed');
	    }
	    $('html, body').stop().animate({
	       scrollTop: target.offset().top
	    }, 1000);
	});
}

// Sidebar menu 

function initSideBar(){
	var menuLeft = document.getElementById( 'cbp-spmenu-s1' ),
		showLeftPush = document.getElementById( 'showLeftPush' ),
		closeMenu = document.getElementById( 'closeSideMenu' ),
		body = document.body;
	if(showLeftPush!=null){
		showLeftPush.onclick = function() {
			classie.toggle( this, 'active' );
			classie.toggle( body, 'cbp-spmenu-push-toright' );
			classie.toggle( menuLeft, 'cbp-spmenu-open' );
			classie.toggle(body, 'noScroll');
			classie.toggle( this, 'hide');
		};
		closeMenu.onclick = function(){
			showLeftPush.click();
		};
	}

}

