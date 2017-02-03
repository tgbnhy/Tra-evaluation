/*  
$(document).ready(function(){
  $('#search').click(function(){// it should be load
	  
      var ne = map.getBounds().getNorthEast();
      var sw = map.getBounds().getSouthWest();
      
      $.ajax({
		type: "GET",
        url: 'PlaceServlet',
        data: "lat1="+sw.lat()+"&lng1="+sw.lng()+"&lat2="+ne.lat()+"&lng2="+ne.lng(),
        complete: function(data){
      	   	var output = data.responseText;
      	  	var array = output.split(',');
      	  	$("#marker").html('<button class="btn btn-primary" type="button">'+
      	  		  'Result count <span class="badge">' + parseInt(array.length/2) + '</span></button>');
      	  	var j=0;
              while(j < (array.length - 1))  {
              	var x1 = array[j];
          		var y1 = array[j+1];
          		
          		drawCell(x1, y1, parseFloat(x1)+0.0002, parseFloat(y1)+0.0004);	
          		j += 2;
              }         
                 
        }			  
      });
  });	 
});
*/

$(document).ready(function(){
  
  $('#search').click(function(){// it should be load
	  $.ajax({
		type: "POST",
        url: 'PlaceServlet',
        data: "locs="+$("#location").val()+"&algorithm="+$('input[name=optradio]:checked').val(),
        complete: function(data){
        	var output = data.responseText;
      		$("#top-k").html(output);
        }			  
      });
  });	 
});

$(document).ready(function(){
	  
	  $('#find').click(function(){// it should be load
		  $.ajax({
			type: "POST",
	        url: 'NeighborServlet',
	        data: "locs="+$("#location").val(),
	        complete: function(data){
	        	var output = data.responseText;
	        	var points =  JSON.parse(output);
	    	  	for (i = 0; i < points.length; i=i+2) {
	    	  		
	          	  	marker = new google.maps.Marker({
	        	    	position: new google.maps.LatLng(points[i], points[i+1]),
	    	        	map: map,
	    	        	title: points[i]+","+points[i+1],
	    	        	icon: 'http://maps.google.com/mapfiles/ms/icons/yellow-dot.png'
	    	      	});
				}
	        }			  
	      });
	  });	 
	});

$(document).ready(function(){
	$('#top-k').on('click', 'img', function (){
	  $.ajax({
          method: "GET",
          url: 'PlaceServlet',
          data: "trip="+$(this).attr("trip"),
          complete: function(output){
              var words = [];
              var i;
              var data = [];
              
              var points = output.responseText.split(",");
              
              for(k=0; k<gmarkers.length; k++){
                gmarkers[k].setMap(null);
              }
              gmarkers = [];
              
              for(k=0; k<trips.length; k++){
                  trips[k].setMap(null);
              }
              trips = [];
              for (i = 0; i < points.length; i += 2) {
                var marker;
              
                marker = new google.maps.Marker({
                      position: new google.maps.LatLng(points[i], points[i+1]),
                      map: map,
                      title: 'Point',
                      icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
                });
                
                gmarkers.push(marker);
                data.push(new google.maps.LatLng(points[i] , points[i+1]));
              }              
              
              if(data.length > 1){
                var polyline = new google.maps.Polyline({
                    path: data,
                    strokeColor: '#0000FF',
                    strokeOpacity: 1.0,
                    strokeWeight: 2,
                    editable: false
                });
                polyline.setMap(map);
                trips.push(polyline);
              }  
          }
      });   
      
  });
});

$(document).ready(function(){
  $('#trajectory').on('click', '#remove', function (){
      
      $(this).parent().parent().remove();
      var info = $(this).parent().html();
      var lat = info.split(" ")[1];
      var lng = info.split(" ")[3];
      var locs = $("#location").val();
      $("#location").val(locs.replace(lat+','+lng+',', ''));
      //alert($("#location").val());
      var tmp = []; 
      trajectory.setMap(null);
      for(var i = 0; i < data.length; i++) {
          if(data[i].lat() != lat || data[i].lng() != lng){
            tmp.push(data[i]);
          }
          
      }
      for(var i = 0; i < tmarkers.length; i++) {
          if(tmarkers[i].getPosition().lat() == lat && tmarkers[i].getPosition().lng() == lng){
            tmarkers[i].setMap(null);
          } 
      }
      data = tmp;
      if(data.length > 1){
        if(trajectory != null){
            trajectory.setMap(null);
        }
        trajectory = new google.maps.Polyline({
            path: data,
            strokeColor: '#FF0000',
            strokeOpacity: 1.0,
            strokeWeight: 2,
            editable: false
        });
        trajectory.setMap(map);
      }
    
    
  });
  
});
