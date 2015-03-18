$(document).ready(function() {
    initSubmit();

	$('.js_picUpload').click(function(){
	    document.getElementById("upfile").click();
	});

	$("#upfile").change(function(){
        readURL(this);
    });
});

function initSubmit(){
    $(".js_submitBtn").on("click", function(){
        var btn=$(this);
        $('.errorForm').addClass('hidden');

        $.ajax({
            url : "/genPublicIdSubmit/",
            method : 'POST',
            data : $("#tribeForm").serializeArray(),
            success:function(data) {
                    console.log(data);

            	if(data.error!=null){
            	    for(var key in data.error){
                        $('.js_error_'+key).text(data.error[key]);
                        $('.js_error_'+key).removeClass('hidden');
            	    }
            	} else {
                    $('#tribeForm').hide();
                    for(var i=0;i<data.urls.length;i++){
                        $('.js_urls').append('<p style="font-size:20px;"> • '+data.urls[i]+'</p>');
                    }
                    $('.js_row').removeClass('hidden');

                    $('html, body').stop().animate({
                       scrollTop: target.offset().top
                    }, 1000);
                }

            },
            error:function(data){

            }
        });
    });
}

function readURL(input) {

    if (input.files && input.files[0]) {
        var reader = new FileReader();

        reader.onload = function (e) {
            $('#preview').attr('src', e.target.result);
        }

        reader.readAsDataURL(input.files[0]);
    }
}

