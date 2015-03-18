$(document).ready(function() {
	if(window.location.hash == "") {
		location.href = location.href + '#commentsPage';
	}
	submitComment();
	submitCommentEnter();
	submitCommentButton();
	initBtnDeleteComment();

});

/**
 * Totem owner can delete any comment of a flash
 */
function initBtnDeleteComment(){

    $('.js_delete_button').hover(function(){
        $(this).css("opacity",1);
    },function(){
        $(this).css("opacity",0.2);
    });

    $('.js_delete_comment').on('click', function(){
    	var btn=$(this);

        $.ajax({
            url: '/delete_comment_owner/'+btn.data("comment-id"),
            type: 'GET',
            success: function () {
                btn.closest(".js_removed_when_delete").remove();
            },
            error: function(data, textStatus){
		        if(textStatus == 'timeout'){
		        	btn.after("<p class='error'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error deleteFlash");
		        }else if(data.status==404){
		        	btn.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
					loggr("no web ", "trace deleteFlash");
		        }else{
					btn.after("<p class='error'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error deleteFlash");
		        }

            },
            timeout: 10000
        });
    });
}

/**
 * Any logged user can coment any flash
 */
function submitCommentEnter(){
	$('#userComment').keypress(function(e) {
		if (e.which == 13) {
			console.log("enter !");
			$('.error').remove();
			$('.error-message').addClass("hide");
			submitComment(e);
		}
	});
}

function submitCommentButton(){
	$('.js_postButton').on('click',function(e){
		$('.error').remove();
		$('.error-message').addClass("hide");
		submitComment(e);
	});
}

function submitComment(e){
	var commentText = $("#userComment").val();
	var userName = $(".emptyTemplate").find(".commentName").data('user-Name');
	console.log(userName);
	var threadId = $("#userComment").data('thread-id');
	if(commentText.trim()!==""){
		$('.emptyTemplate').clone().addClass('newComment').removeClass('emptyTemplate').appendTo('.commentBlock');
		$('.newComment:last').find('.commentText').html(commentText);
		$('.newComment').find('.commentName').html(userName);
		$('.newComment').removeClass('hide');
		$('#userComment').val('');
		e.stopPropagation();
		e.preventDefault();
	}

	loggr("commenting "+userName+ " thread id : " +threadId+ " " , "created comment");

	
	if(undefined != commentText && commentText !==""){
		$.ajax({
			url: "/submit_comment/"+threadId,
			type: 'POST',
			data: {message: commentText},
			success: function (data){
				if(data.error!=null && data.error ==="too long"){
					$('.newComment').hide();
		        	$('.inputCommentBlock').find('.js_error_length').removeClass('hide');
		        	
		        	setInterval(function(){
						$('.inputCommentBlock').find('.js_error_length').addClass('hide');
					},3000);

		        	$('#userComment').val(commentText);
				}else{
					loggr("comment success ", "created comment");
				}
			}, 
			error: function (data, textStatus){
		        if(textStatus == 'timeout'){
		        	$('.newComment').after("<p class='error error-happened'>" + Messages('error.server.connexion') + "</p>");
					loggr("error timeout ajax ", "error comment", data);
		        }else if(data.status==404){
		        	$('.newComment').after("<p class='error error-happened row'>" + Messages('error.internet.connexion') + "</p>");
		        	$('.newComment').remove();
		        	$('#userComment').val(commentText);
					loggr("error no web ", "trace comment", data);
		        }else{
					$('.newComment').after("<p class='error error-happened'>" + Messages('error.bug') + "</p>");
		        	loggr("error ajax", "error comment", data);
		        	console.log(data);
		        }						
			},
			timeout: 10000
		});
	}
}