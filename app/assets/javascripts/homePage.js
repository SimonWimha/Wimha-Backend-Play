$(document).ready(function() {
	flippingTotem();

	(function() {
        var requestAnimationFrame = window.requestAnimationFrame || window.mozRequestAnimationFrame || window.webkitRequestAnimationFrame || window.msRequestAnimationFrame ||
        function(callback) {
            window.setTimeout(callback, 1000 / 60);
        };
        window.requestAnimationFrame = requestAnimationFrame;

        $('.js_playVideo').on('click',function(){
            $(this).hide();
            $('.js_playVideoText').hide();
            $('.js_christmasVideo').removeClass('hide');
        });
    })();
});

function flippingTotem(){
	i = 0; 
	$('.js_flipTotem').mouseover(function(){
		i = i+1;
		if (i == 3){
			i = 0;
			$(this).addClass('animated flipOutY');
			setTimeout(
			  function() 
			  {
			    $('.js_flipTotem').addClass('hide');
			    $('.js_flipTotem').removeClass('animated flipOutY');
			    $('.js_backTotem').removeClass('hide');
			  }, 1000);
			setTimeout(function(){
				$('.js_backTotem').addClass('hide');
				$('.js_flipTotem').removeClass('hide');
			},7000);
		}
	});
}