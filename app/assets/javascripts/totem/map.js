/*** Map variable initialized when gmaps script loaded ****/
var map;
var mapOptions;


/**** Async loading of google maps then infobox.js ****/
function loadMapsAPI() {
    addScript( 'https://maps.googleapis.com/maps/api/js?key=AIzaSyD_nYQ4Vk57N-4BOjY7eJSEX-BQq7Fvorg&sensor=false&callback=mapsApiReady' );
}

function mapsApiReady() {
    addScript( 'https://google-maps-utility-library-v3.googlecode.com/svn/trunk/infobox/src/infobox.js', infoboxReady );
    console.log('maps done');
}

function infoboxReady() {
    addScript( 'https://google-maps-utility-library-v3.googlecode.com/svn/trunk/markerclusterer/src/markerclusterer.js', allScriptsReady );
    console.log('infobox done');
}


/**** In parallel : fetch markers ****/
$(document).ready(function() {
    loadMapsAPI();
    getMap();
});

function getMap(){
    var container = $(".js_mapData");
    $.ajax({
        url: '/mapData/'+container.data("tribu_name"),
        type: 'GET',
        success: function (data) {
            $(".coord").remove();
            container.append(data);
        },
        error: function(data, textStatus){
            if(textStatus == 'timeout'){
                container.after("<p class='error'>" + Messages('error.internet.connexion') + "</p>");
                loggr("no web ", "trace map");
            }
        },
        timeout: 15000
    });
}

/**** When scripts and markers are ready, we can initialize markers *****/
function allScriptsReady(){
    console.log('clusters done');
    mapOptions = {
      center: new google.maps.LatLng(43,1),
      zoom: 5,
      zoomControl: true,
      zoomControlOptions: {
          style: google.maps.ZoomControlStyle.DEFAULT
      },
      disableDoubleClickZoom: true,
      mapTypeControl: false,
      scaleControl: false,
      scrollwheel: false,
      streetViewControl: false,
      draggable : true,
      overviewMapControl: false,
      mapTypeId: google.maps.MapTypeId.ROADMAP,
      styles: [{"featureType":"water","stylers":[{"visibility":"on"},{"color":"#acbcc9"}]},{"featureType":"landscape","stylers":[{"color":"#f2e5d4"}]},{"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#c5c6c6"}]},{"featureType":"road.arterial","elementType":"geometry","stylers":[{"color":"#e4d7c6"}]},{"featureType":"road.local","elementType":"geometry","stylers":[{"color":"#fbfaf7"}]},{"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#c5dac6"}]},{"featureType":"administrative","stylers":[{"visibility":"on"},{"lightness":33}]},{"featureType":"road"},{"featureType":"poi.park","elementType":"labels","stylers":[{"visibility":"on"},{"lightness":20}]},{},{"featureType":"road","stylers":[{"lightness":20}]}]
    };
    map = new google.maps.Map(document.getElementById("map-canvas"),mapOptions);

    waitForMarkers();
}

function waitForMarkers(){
    setTimeout(function(){
    console.log("check " + $('.coord').length);
        if( $('.coord').length != 0 ){
            initMarkers();
        }else{
            waitForMarkers();
        }
    }, 1000);
}

/**
 * displays infowindows on coordinates on the map
 * @return {[type]} [description]
 */
function initMarkers(){
  var markers=[];
  var infoboxes=[];
  var lastmessage_time=0;
  var lastmessage;
  var lastmessage_infobox;
  var anchor;

	$(".coord").each(function(){
		var lat=$(this).attr("data-lat");
		var lon=$(this).attr("data-lon");
		var name=$(this).attr("data-name");
		var msg=$(this).attr("data-msg");
		var time=$(this).attr("data-timestamp"); //Make sure to delete it if we don't use it anymore
		var realDate =$(this).attr("data-realDate");
		var adress =$(this).attr("data-adress");
		var myLatlng = new google.maps.LatLng(lat,lon);
    var picture = $(this).attr("data-picture");

  if(picture!=null){
    var contentString = '<div id="infobox" class="pic"><div id="content">'+
            '<div style="position:relative;"><img src="'+picture+'" style="width:100%;"><h3 id="firstHeading" class="firstHeading" style="position:absolute;bottom:0px;left:10px;">'+name+'</h3></div><img class="leftPointer" src="http://res.cloudinary.com/cloudinarywimha/image/upload/v1414447760/Static%20Pictures/tipbox.png">'+
            '<div class="flashInfo"><p style="font-size:12px;margin:5px 0px"><span>' +realDate+ '</span> - <span style="font-size:12px;">' +adress+ '</span></p>'+
            '<div id="bodyContent">'+
              '<h4 class="addEllipsis">"'+' '+msg+' '+'"</h4>'+
            '</div></div>' +
          '</div>';
    }else{
    var contentString = '<div id="infobox" class="noPic"><div id="content"><img class="leftPointer" src="http://res.cloudinary.com/cloudinarywimha/image/upload/v1414448319/Static%20Pictures/tipboxblack.png"><div class="flashInfo">'+
            '<h3 id="firstHeading" class="firstHeading" style="margin:10px 0 0 0;">'+name+'</h3>'+
            '<p style="font-size:12px;margin:5px 0px"><span>' +realDate+ '</span> - <span style="font-size:12px;">' +adress+ '</span></p>'+
            '<div id="bodyContent">'+
              '<h4>"'+' '+msg+' '+'"</h4>'+
            '</div>' +
          '</div></div></div>';
    }

  
     var infobox = new InfoBox(
     {
       content: contentString,
       disableAutoPan: false,
       maxWidth: 150,
       pixelOffset: new google.maps.Size(25, -140),
       zIndex: null,
       boxStyle: {
                   opacity: 1,
                   width: "280px"
           },
       closeBoxURL: "http://res.cloudinary.com/cloudinarywimha/image/upload/h_15,w_15/v1414418528/Static%20Pictures/croos.png",
       infoBoxClearance: new google.maps.Size(1, 1)
     }
     );
     infoboxes.push(infobox);

//  if(picture!=null){
//    var contentString = '<div id="content">'+
//            '<img src="'+picture+'" style="width:100%;">'+
//            '<h3 id="firstHeading" class="firstHeading" style="margin:10px 0 0 0;">'+name+'</h3>'+
//            '<p style="font-size:12px;margin:5px 0px"><span>' +realDate+ '</span> - <span style="font-size:12px;">' +adress+ '</span></p>'+
//            '<div id="bodyContent">'+
//              '<p>"'+msg+'"</p>'+
//            '</div>' +
//          '</div>';
//    }else{
//    var contentString = '<div id="content">'+
//            '<h3 id="firstHeading" class="firstHeading" style="margin:10px 0 0 0;">'+name+'</h3>'+
//            '<p style="font-size:12px;margin:5px 0px"><span>' +realDate+ '</span> - <span style="font-size:12px;">' +adress+ '</span></p>'+
//            '<div id="bodyContent">'+
//              '<p>"'+msg+'"</p>'+
//            '</div>' +
//          '</div>';
//    }
//    var infobox = new google.maps.InfoWindow({
//       content: contentString,
//       maxWidth : 250
//    });
//    infoboxes.push(infobox);

    var marker = new google.maps.Marker({
          icon: $('#totemPins').attr('src'),
          animation: google.maps.Animation.DROP,
          position: myLatlng,
          map: map,
          visible: true
    });
    markers.push(marker);

    google.maps.event.addListener(marker, 'click', function() {
        $.each(infoboxes,function(index,infoboxe){
          infoboxe.close();
        });
        infobox.open(map,marker);
        // Once the infobox is fully loaded, add your events here
        infobox.addListener('domready', function() {
            // Set up the ellipsis for text
            $('.addEllipsis').addClass('messagePreview');
        });
    });

    if (window.location.hash === ('#'+ time)) {
      infobox.open(map,marker);
      // Once the infobox is fully loaded, add your events here
      infobox.addListener('domready', function() {
          // Set up the ellipsis for text
          $('.addEllipsis').addClass('messagePreview');
      });
      map.setZoom(10);
      var center = new google.maps.LatLng(
        marker.position.lat()+3,
        marker.position.lng()
      );
      center.lb+=3;
      map.panTo(center);
      anchor=marker;
    }

    if(lastmessage_time<time){
      lastmessage_time=time;
      lastmessage=marker;
      lastmessage_infobox=infobox;
    }

  });

  if(anchor===undefined && lastmessage!==undefined){
    var center = new google.maps.LatLng(
        lastmessage.position.lat()+3,
        lastmessage.position.lng()
    );
    //center.lb+=3;
    map.panTo(center);
    lastmessage_infobox.open(map,lastmessage);
  }

//  var clusterStyles = [
//    {
//      opt_textColor: 'white',
//      url: '../images/map-marker.png',
//      height: 50,
//      width: 50
//    }
//  ];

  var mcOptions = {
    gridSize: 50,
    //styles: clusterStyles,
    maxZoom: 15
  };

  new MarkerClusterer(map,markers,mcOptions);

}

/** Helper **/
function addScript( url, callback ) {
    var script = document.createElement( 'script' );
    if( callback ) script.onload = callback;
    script.type = 'text/javascript';
    script.src = url;
    document.body.appendChild( script );
}