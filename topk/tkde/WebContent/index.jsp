<%@page import="java.sql.Connection"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Exampler Trajectory Query</title>
	<link href="css/bootstrap.min.css" rel="stylesheet" />
    <link href="css/main.css" rel="stylesheet" />
    <link href="css/ie10-viewport-bug-workaround.css" rel="stylesheet" />
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?v=3.exp&libraries=visualization,drawing,places&key=AIzaSyDVW4z98LfwZZ7uRj2wIXZ-Cq8-P4OwERc"></script>
    <script src="js/ie-emulation-modes-warning.js"></script>
    <script src="js/ie10-viewport-bug-workaround.js"></script>
    <script type="text/javascript">
    	var directionsService = new google.maps.DirectionsService;
        var directionsDisplay = new google.maps.DirectionsRenderer({suppressMarkers: true});
        
        var map = null;
        var data = [];
        var tmarkers = [];
        var gmarkers = [];
        var trips = [];
        var trajectory = null;
        function loadMap() {
          var latlng = new google.maps.LatLng( 40.69084335918964, -74.0287627838552 );
          //var latlng = new google.maps.LatLng( 15.208997961132809, 145.84631393165625 );
          //var latlng = new google.maps.LatLng( 59.722069245257984, 18.1702883541584 );
          
          var myOptions = {
            zoom: 10,
            center: latlng,
            mapTypeId: google.maps.MapTypeId.Terrian
          //  mapTypeId: google.maps.MapTypeId.SATELLITE
          };
          
          map = new google.maps.Map(document.getElementById("map_container"),myOptions);

          google.maps.event.addListener(map, 'click', function( event ){
              
            data.push(event.latLng);
            marker = new google.maps.Marker({
                    position: new google.maps.LatLng(event.latLng.lat(), event.latLng.lng()),
                    map: map
            });
            tmarkers.push(marker);
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
            $("#trajectory").append('<div class="bs-callout bs-callout-danger"><h6>Lat: '+event.latLng.lat()+' Lng: '+event.latLng.lng()+' <span id="remove" class="glyphicon glyphicon-remove pull-right"> </span></h6><input type="text" class="form-control input-sm" placeholder="Activity"> </div>')
            var loc = $("#location").val();
            $("#location").val(loc +event.latLng.lat()+','+event.latLng.lng()+',');
          });                
               
          $("#location").val("");
          /*
          $.ajax({
    		  type: "GET",
            url: 'PlaceServlet',
            data: "trip=31",
            complete: function(data){
          		
          	   	output = data.responseText;
          	   	var points =  output.split(",");
          	  	var i = 0;
          	  	//alert(points.length)
          	   	while(i < points.length)  {
          	   		//alert(points[i]+" " + points[i+1]);
	          	   	marker = new google.maps.Marker({
	        	    	position: new google.maps.LatLng(points[i], points[i+1]),
	    	        	map: map,
	    	        	title: points[i] + " " + points[i+1],
	    	        	icon: 'http://maps.google.com/mapfiles/ms/icons/blue-dot.png'
	    	      	});
	          	   	i+=2;
                } 
          	   	
            }			  
        });*/
          
        $.ajax({
	  		  type: "POST",
	          url: 'DistributionServlet',
	          complete: function(data){
	        		
	        	   	output = data.responseText;
	        	   	var points =  JSON.parse(output);
	        	  	for (i = 0; i < points.length; i+=5) {
	        	  		var rectangle = new google.maps.Rectangle({
	        	  			strokeWeight: 0,
	        	  			fillColor: points[i+4],
	        	            fillOpacity: 0.5,
	        	            map: map,
	        	            bounds: {
	        	              north: points[i+2],
	        	              south: points[i],
	        	              east: points[i+3],
	        	              west: points[i+1]
	        	            }
	        	      	});	
					}
	        	   	
	          }			  
	      });
        
        $.ajax({
  		  type: "GET",
          url: 'PointServlet',
          complete: function(data){
        		
        	   	output = data.responseText;
        	   	var points =  JSON.parse(output);
        	  	for (i = 0; i < points.length; i=i+2) {
        	  		
	          	  	marker = new google.maps.Marker({
	        	    	position: new google.maps.LatLng(points[i], points[i+1]),
	    	        	map: map,
	    	        	title: points[i]+","+points[i+1],
	    	        	icon: 'http://maps.google.com/mapfiles/ms/icons/green-dot.png'
	    	      	});
				}
        	   	
          }			  
      });
        
        marker = new google.maps.Marker({
	    	position: new google.maps.LatLng(40.499528884887695,-72.99986267089844),
        	map: map,
        	title: 'Center',
        	icon: 'http://maps.google.com/mapfiles/ms/icons/red-dot.png'
      	});
        
        /*
        arr1 = [
40.592812759722676, -74.0157680521952, 40.709479716693785, -73.99192427750677
    		];
        for (i = 0; i < arr1.length; i=i+2) {
        	marker = new google.maps.Marker({
    	    	position: new google.maps.LatLng(arr1[i],arr1[i+1]),
            	map: map,
            	title: 'Center',
            	icon: 'http://maps.google.com/mapfiles/ms/icons/yellow-dot.png'
          	});
        }
		*/
     }
     function drawCell(x1, y1, x2, y2) {
     	var wdata = [];	  
     	wdata.push(new google.maps.LatLng(x1, y1));
        	wdata.push(new google.maps.LatLng(x1 , y2));
        	wdata.push(new google.maps.LatLng(x2 , y2));
        	wdata.push(new google.maps.LatLng(x2 , y1));
        	wdata.push(new google.maps.LatLng(x1 , y1));
         var wpolyline = new google.maps.Polyline({
             path: wdata,
             strokeColor: '#0000FF',
             strokeOpacity: 1.0,
             strokeWeight: 1,
             editable: false
         });
         wpolyline.setMap(map); 
         wdata.length = 0;
       }
    </script>
    <script src="js/main.js"></script>
        
<link rel="icon" type="image/x-icon" href="favicon.ico" />
</head>
<body onload="loadMap()">
	<nav class="navbar navbar-default navbar-fixed-top">
      <div class="container">
        <div class="navbar-header">
          <a class="navbar-brand" href="#">Activity Trajectory Search System</a>
        </div>
      </div>
    </nav>
    <div class="container">
        <div class="row">
            <div class="col-sm-3 col-md-3 sidebar">
                
                <h5>Most Related Trajectories</h5>
                <hr id="hr">
                <div id="top-k">
                    <img src="images/4.jpg" class="img-thumbnail" width="100%">
                    <img src="images/6.jpg" class="img-thumbnail" width="100%">
                </div>                     
            </div>
            <div class="col-sm-6 thumbnail">
                
                <div id="map_container"></div>
            </div>
            <div class="col-sm-3">
	            <div id="marker">    
	            
	            </div>
	            <div class="clearfix"></div>
	            <div id="trajectory">
	                
	            </div>
	            <div id="info">
	                
	            </div>
	            <div class="col-sm-12">
	            	<label class="radio-inline"><input type="radio" value="1" id="alg" name="optradio">SRA</label>
					<label class="radio-inline"><input type="radio" value="2" id="alg" name="optradio">SGRA</label>
	                <input type="hidden" id="location" value=""/>
	                <button type="button" id="search" class="btn btn-sm btn-primary indent">Search</button>
	                <button type="button" id="find" class="btn btn-sm btn-primary indent">Find</button>
	            </div>                
            </div>            
        </div>
        <div class="row">
        	<div class="col-sm-12" id="response"></div>
        </div>
    </div>    
    
    <hr>
    
    <div class="container">
    			<%    
    				/*
    				MyLucene5 lucene = new MyLucene5();
            		String dir = "C:\\Users\\Home\\workspace\\tkde\\index";
            		/*
            		Dataset ds = new Dataset("root", "");
            		Connection conn = ds.Connect();
            		QuadTree qTree = ds.createQuadTreeFromData(conn, 1024);
            		ds.createHash(conn, qTree, dir);
            		*/
            		//out.print(lucene.search("spencer", dir));
            		
            	%> 
        <footer>
            <p class="pull-right"><a href="#">Back to top</a></p>
            <p>&copy; 2016, RMIT. </p>
        </footer>
    </div>
</body>
</html>