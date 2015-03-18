$(document).ready(function() {
    var ouibounceCookie=$.cookie("ouibounceHasbeenDisplayed");
    if(typeof ouibounceCookie == 'undefined' || undefined == ouibounceCookie ||  ouibounceCookie === 'false'){
        ouibounceModal();
    }
});
var ouibounceModalObject
//https://github.com/carlsednaoui/ouibounce#sensitivity
function ouibounceModal(){
	ouibounceModalObject = ouibounce(document.getElementById('ouibounce-modal'), {callback: function() {
	    $.cookie("ouibounceHasbeenDisplayed","true", { expires: 90,path: '/' });
	}
	});

	$('.js_ouibounce_close_modal').on('click',function(){
		$('#ouibounce-modal').hide();
	})
	$('.js_ouibounce_yes').on('click',function(){
		$('.js_ouibounce_text').html(Messages('hp.modal.second')+ '</br>' + Messages('hp.modal.second.bis'));
		$('.js_ouibounce_yes').hide();
		$('.js_ouibounce_close_modal').hide();
		$('.js_footer').css('text-align','center');
		$('.fb-like').removeClass('hide');
	})
}
// when a user hits the like btn from the ouibounce modal
function likedBtnok(){
	$('#ouibounce-modal').modal('hide');
	$('#ouibounce-modal-result').modal();
}