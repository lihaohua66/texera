var main = function() {
  $('.btn').click(function() {
    var col = $('.word').css('color');
    if (col==='rgb(0, 0, 255)'){
    	$('.word').css('color','#ffff00');
    }else if (col==='rgb(225, 225, 0)'){
      $('.word').css('color','#0000ff');
    }else{
      $('.word').css('color','#0000ff');
    }
  });
}

$(document).ready(main);