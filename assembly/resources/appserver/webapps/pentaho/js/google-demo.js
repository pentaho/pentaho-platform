
var map;
	var redicon;
	var yellowicon;
	var greenicon;
	var topThreshold = 100000;
	var bottomThreshold = 50000;
	var points = new Array();
	
	var icon;

 var geocoder = new GClientGeocoder();


icon = new GIcon();
icon.image = "http://labs.google.com/ridefinder/images/mm_20_red.png";
icon.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
icon.iconSize = new GSize(12, 20);
icon.shadowSize = new GSize(22, 20);
icon.iconAnchor = new GPoint(6, 20);
icon.infoWindowAnchor = new GPoint(5, 1);
redicon = icon;

icon = new GIcon();
icon.image = "http://labs.google.com/ridefinder/images/mm_20_yellow.png";
icon.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
icon.iconSize = new GSize(12, 20);
icon.shadowSize = new GSize(22, 20);
icon.iconAnchor = new GPoint(6, 20);
icon.infoWindowAnchor = new GPoint(5, 1);
yellowicon = icon;

icon = new GIcon();
icon.image = "http://labs.google.com/ridefinder/images/mm_20_green.png";
icon.shadow = "http://labs.google.com/ridefinder/images/mm_20_shadow.png";
icon.iconSize = new GSize(12, 20);
icon.shadowSize = new GSize(22, 20);
icon.iconAnchor = new GPoint(6, 20);
icon.infoWindowAnchor = new GPoint(5, 1);
greenicon = icon;

    function load() {
      if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("map"));
        map.setCenter(new GLatLng(37.4419, -95), 4);
		map.addControl(new GSmallMapControl());
		map.addControl(new GMapTypeControl());
		addPoints();
	  }
	}


	function customerClick( ) {
		
		pentahoAction( "steel-wheels", "google", "dial.xaction", 
				new Array( 
					new Array( "customer", currentRecord[7] ),
					new Array( "value", currentRecord[4] ),
					new Array( "max", 200000 ),
					new Array( "low", bottomThreshold ),
					new Array( "high", topThreshold )
				), 
				'updateInfoWindow'
		);
		
	}

	function updateInfoWindow( content ) {
		currentMarker.openInfoWindowHtml("<table border='0' width='375' cellpadding='0' cellspacing='0'><tr><td rowspan='2' height='125' width='250' style='xborder-right:1px solid #bbbbbb'><table><tr><td nowrap>Customer:</td><td nowrap>" + currentRecord[7] + "</td></tr><tr><td nowrap>Name:</td><td nowrap>" + currentRecord[3] + "</td></tr><tr><td nowrap>Location:</td><td nowrap>"+currentRecord[2]+"</td></tr><tr><td nowrap>Current Sales:</td><td nowrap>"+currentRecord[4]+"</td></tr></table></td><td colspan='2' valign='top' width='125'>"+content+"</td></tr><tr><td>0</td><td style='text-align:right'>200,000</td></tr></table>");
		pentahoAction( "steel-wheels", "google", "chart.xaction", 
				new Array( new Array( "customer", currentRecord[7] ) ), 
				 'updateProductMix'
		);
	}

	function updateProductMix( content ) {
		document.getElementById( 'details-div' ).style.display='block';
		document.getElementById( 'details-cell1' ).innerHTML=content;
		pentahoAction( "steel-wheels", "google", "customer_details.xaction", 
				new Array( new Array( "customer", currentRecord[7] ) ), 
				 'updateHistory'
		);
	}

	function updateHistory( content ) {
		document.getElementById( 'details-div' ).style.display='block';
		document.getElementById( 'details-cell2' ).innerHTML=content;
	}

function showAddress(address, name, custNum, value, selected) {
	geocoder.getLatLng(
		address,
		function(point) {
			if (!point) {
//				alert(address + " not found");
			} else {
				var record = new Array( null, point, address, name, value, selected, null, custNum );
				points.push( record );
				showMarker( null, null, record );
			}
		}
	);
}

	function showMarker( oldMarker, oldIcon, record ) {
			var icon;
			var value = record[4];
			var point = record[1];
			if( value < bottomThreshold ) {
				icon = redicon;
			}
			else if( value > topThreshold ) {
				icon = greenicon;
			} else {
				icon = yellowicon;
			}
			if( icon == oldIcon ) {
				// this marker has not changed so return the old one
				return oldMarker;
			}
			record[5] = icon;
			// this marker has changed so remove it
			if ( oldMarker ) {
				map.removeOverlay( oldMarker );
			}
			// create a new marker with a click listener
			var marker = new GMarker(point, icon);
			map.addOverlay(marker);
			GEvent.addListener(marker, "click", function() {
				infoWindow( marker, record );
			});
			GEvent.addListener(marker, "hide", function() {
				currentMarker = null;
				alert( 1 );
			});
			return marker;
	}

	var currentMarker = null;
	var currentRecord = null;
	

	function infoWindow( marker, record ) {
		currentMarker = marker;
		currentRecord = record;
		customerClick( );
	}

	function update(topChange) {
		// repaint all of the points using the 
		var n = points.length;
		
		var idx1 = document.getElementById('topthreshold').selectedIndex;
		var idx2 = document.getElementById('bottomthreshold').selectedIndex;
		if( idx1 < idx2 ) {
			if( topChange ) {
				document.getElementById('bottomthreshold').selectedIndex = idx1;
			} else {
				document.getElementById('topthreshold').selectedIndex = idx2;
			}
		}
		
		topThreshold = document.getElementById('topthreshold').value;
		bottomThreshold = document.getElementById('bottomthreshold').value;
		for( idx=0; idx<n; idx++ ) {
			var marker = points[idx][0];
			var icon = points[idx][5];
			points[idx][0] = showMarker( marker, icon, points[idx] ); 
		}
		
		if( currentRecord ) {
			pentahoAction( "steel-wheels", "google", "dial.xaction", 
				new Array( 
					new Array( "customer", name ),
					new Array( "value", currentRecord[4] ),
					new Array( "max", 200000 ),
					new Array( "low", bottomThreshold ),
					new Array( "high", topThreshold )
				), 
				'updateInfoWindow'
			);
		}

		
	}
