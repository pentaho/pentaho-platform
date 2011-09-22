		var img = new Image;
		try {
		    if( img && document.cookie.indexOf( 'img=no' ) == -1 ) {
		    	img.src = "http://sourceforge.net/sflogo.php?group_id=140317&type=2";
			setTimeout( 'checkImage()', 10000 );
		    }
		   } catch (e) { }

		function checkImage() {
			if( !img.complete ) {
				document.cookie += 'img=no';
			}
		}
		
		function winLoaded() {
			try {
				if (TransMenu.isSupported()) {
					TransMenu.initialize();
				}
			} catch (e) {
			}
		}
		
		window.onload =function() {
			init();
		}

		var selectedRow = 0;
		var selectedColumn = 0;

		document.onkeyup = function( event ) {
			var key = 0;
			if ( !event )
			{
				event = window.event;
			}
			if( typeof( event.keyCode ) == 'number'  ) {
				// DOM
				key = event.keyCode;
			} else if( typeof( event.which ) == 'number' ) {
				// NS 4 compatible
				key = event.which;
			} else if( typeof( event.charCode ) == 'number'  ) {
				// also NS 6+, Mozilla 0.9+
				key = event.charCode;
			} else {
				// total failure, we have no way of obtaining the key code
				return;
			}
			if( key == 27 ) {
				hideFly();
			}
			else if( key == 40 ) {
				// /move the selected item down
				if( selectedColumn == 0 ) {
					selectedColumn = 1;
				}
				var newRow = selectedRow+1;
				var oldImg = document.getElementById( 'img-r'+selectedRow+'-c'+selectedColumn);
				var newImg = document.getElementById( 'img-r'+newRow+'-c'+selectedColumn);
				if( newImg ) {
					if( newImg.src.indexOf('folder_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/folder_rollover.png';
					}
					else if( newImg.src.indexOf('file_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/file_rollover.png';
					}
					if( oldImg ) {
						if( oldImg.src.indexOf('folder_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/folder_active.png';
						}
						else if( oldImg.src.indexOf('file_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/file_active.png';
						}
					}
					selectedRow = newRow;
				}
			}
			else if( key == 38 ) {
				// /move the selected item down
				if( selectedColumn == 0 ) {
					selectedColumn = 1;
				}
				var newRow = selectedRow-1;
				var oldImg = document.getElementById( 'img-r'+selectedRow+'-c'+selectedColumn);
				var newImg = document.getElementById( 'img-r'+newRow+'-c'+selectedColumn);
				if( newImg ) {
					if( newImg.src.indexOf('folder_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/folder_rollover.png';
					}
					else if( newImg.src.indexOf('file_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/file_rollover.png';
					}
					if( oldImg ) {
						if( oldImg.src.indexOf('folder_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/folder_active.png';
						}
						else if( oldImg.src.indexOf('file_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/file_active.png';
						}
					}
					selectedRow = newRow;
				}
			}
			else if( key == 39 ) {
				// /move the selected item right
				if( selectedRow == 0 ) {
					selectedRow = 1;
				}
				var newCol = selectedColumn+1;
				var oldImg = document.getElementById( 'img-r'+selectedRow+'-c'+selectedColumn);
				var newImg = document.getElementById( 'img-r'+selectedRow+'-c'+newCol);
				if( newImg ) {
					if( newImg.src.indexOf('folder_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/folder_rollover.png';
					}
					else if( newImg.src.indexOf('file_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/file_rollover.png';
					}
					if( oldImg ) {
						if( oldImg.src.indexOf('folder_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/folder_active.png';
						}
						else if( oldImg.src.indexOf('file_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/file_active.png';
						}
					}
					selectedColumn = newCol;
				}
			}
			else if( key == 37 ) {
				// /move the selected item right
				if( selectedRow == 0 ) {
					selectedRow = 1;
				}
				var newCol = selectedColumn-1;
				var oldImg = document.getElementById( 'img-r'+selectedRow+'-c'+selectedColumn);
				var newImg = document.getElementById( 'img-r'+selectedRow+'-c'+newCol);
				if( newImg ) {
					if( newImg.src.indexOf('folder_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/folder_rollover.png';
					}
					else if( newImg.src.indexOf('file_active.png') > -1 ) {
						newImg.src = '/pentaho-style/images/file_rollover.png';
					}
					if( oldImg ) {
						if( oldImg.src.indexOf('folder_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/folder_active.png';
						}
						else if( oldImg.src.indexOf('file_rollover.png') > -1 ) {
							oldImg.src = '/pentaho-style/images/file_active.png';
						}
					}
					selectedColumn = newCol;
				}
			}
			else if( key == 13 ) {
				// 'click' this
				var anchor = document.getElementById( 'a-r'+selectedRow+'-c'+selectedColumn);
				
				if( anchor ) {
					var href = anchor.href;
					var target = anchor.target;
					if( target == '' || target == '.' ) {
						alert("this has been disabled to prevent possible XSS attach");
						// document.location.href = href;
					} else {
						alert("this has been disabled to prevent possible XSS attach");
						// window.open( href, target );
					}
				}
			}

		}

		function init() {
			// ==========================================================================================
			// if supported, initialize TransMenus
			// ==========================================================================================
			// Check isSupported() so that menus aren't accidentally sent to
			// non-supporting browsers.
			// This is better than server-side checking because it will also
			// catch browsers which would
			// normally support the menus but have javascript disabled.
			//
			// If supported, call initialize() and then hook whatever image
			// rollover code you need to do
			// to the .onactivate and .ondeactivate events for each menu.
			// ==========================================================================================
			try {
			if (TransMenu.isSupported()) {
				TransMenu.initialize();

				// hook all the highlight swapping of the main toolbar to menu
				// activation/deactivation
				// instead of simple rollover to get the effect where the button
				// stays hightlit until
				// the menu is closed.

				if( document.getElementById("home-menu") ) {
					document.getElementById("home-menu").onmouseover = function() {
						ms.hideCurrent();
						this.className = "hover";
					}				
					document.getElementById("home-menu").onmouseout = function() { this.className = ""; }
				}

				if( document.getElementById("admin-menu") ) {
					document.getElementById("admin-menu").onmouseover = function() {
						ms.hideCurrent();
						this.className = "hover";
					}
					document.getElementById("admin-menu").onmouseout = function() { this.className = ""; }
				}
				if( document.getElementById("logout-menu") ) {
					document.getElementById("logout-menu").onmouseover = function() {
						ms.hideCurrent();
						this.className = "hover";
					}
					document.getElementById("logout-menu").onmouseout = function() { this.className = ""; }
				}
				if( menu2 ) {
					menu2.onactivate = function() { document.getElementById("navigate-menu").className = "hover"; };
					menu2.ondeactivate = function() { document.getElementById("navigate-menu").className = ""; };
				}

				if( menu4 ) {
					menu4.onactivate = function() { document.getElementById("about-menu").className = "hover"; };
					menu4.ondeactivate = function() { document.getElementById("about-menu").className = ""; };
				}

				if( topLevelMenuItems ) {
               		var idx=0;
                	for( var idx=0; idx < topLevelMenuItems.length; idx++ ) {
                    	document.getElementById( topLevelMenuItems[idx] ).onmouseover = function() {
                        	ms.hideCurrent();
                        	this.className = "hover";
                    	}				
                    	document.getElementById( topLevelMenuItems[idx] ).onmouseout = function() { this.className = ""; }
                	}
				}

			}
			} catch (e) {
			}
		}


/*******************************************************************************
 * Ajax Includes script- Dynamic Drive DHTML code library (www.dynamicdrive.com)
 * This notice MUST stay intact for legal use Visit Dynamic Drive at
 * http://www.dynamicdrive.com/ for full source code
 ******************************************************************************/

// To include a page, invoke ajaxinclude("afile.htm") in the BODY of page
// Included file MUST be from the same domain as the page displaying it.

var rootdomain="http://"+window.location.hostname

function ajaxinclude(url) {
var page_request = false
if (window.XMLHttpRequest) // if Mozilla, Safari etc
page_request = new XMLHttpRequest()
else if (window.ActiveXObject){ // if IE
try {
page_request = new ActiveXObject("Msxml2.XMLHTTP")
} 
catch (e){
try{
page_request = new ActiveXObject("Microsoft.XMLHTTP")
}
catch (e){}
}
}
else
return false
page_request.open('GET', url, false) // get page synchronously
page_request.send(null)
writecontent(page_request)
}

function writecontent(page_request){
if (window.location.href.indexOf("http")==-1 || page_request.status==200)
document.write(page_request.responseText)
}

function modalWin(URL) {
if (window.showModalDialog) {
window.showModalDialog(URL,"pentahoDialog","dialogWidth:600px;dialogHeight:400px;resizable: yes;status:no;help:no;scroll:no;");
} else { 
window.open(URL,'pentahoDialog','height=400,width=600,toolbar=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=yes,modal=yes');
}
}


<!-- Begin
function popPage(URL) {
window.open(URL);
}
// End -->
 

		
function getDocumentY(e) {
	var posy = 0;
	if (!e) var e = window.event;
	if (e.pageY) 	{
		posy = e.pageY;
	}
	else if (e.clientY) 	{
		posy = e.clientY + document.body.scrollTop
			+ document.documentElement.scrollTop;
	}
	return posy;
}

function getDocumentX(e) {
	var posx = 0;
	if (!e) var e = window.event;
	if (e.pageX) 	{
		posx = e.pageX;
	}
	else if (e.clientX) 	{
		posx = e.clientX + document.body.scrollLeft
			+ document.documentElement.scrollLeft;
	}
	return posx;
}

// This code is necessary for browsers that don't reflect the DOM
// constants (like IE).
if (document.ELEMENT_NODE == null) {
  document.ELEMENT_NODE = 1;
  document.TEXT_NODE = 3;
}

function flyTableSetup( title, desc, iconSrc, target, solution, path, filename, furl, properties, hasModifyAcl ) {
	var titleDiv = document.getElementById("flyTitle1");
	var descDiv = document.getElementById("flyDesc1");
	var img=document.getElementById('flyimg');  
	var actionsDiv = document.getElementById("flyDesc2");
	if( iconSrc && iconSrc != '' ) {
		img.style.display='block';
		img.src=iconSrc;
	} else {
		img.style.display='none';
		img.src='/pentaho-style/images/spacer.gif';
	}

	titleDiv.innerHTML = title;
	descDiv.innerHTML = desc;

	if( !furl || furl == '' ) {
		actionsDiv.innerHTML = "hmm";
		return;
	}
	var actions = getOptions(solution, path, filename, target, filename=='', furl, properties, hasModifyAcl );
	// see template.html for definition and init of isAdmin
	if( isAdmin == 'true' ) {
		actions += "<p/><span style=\"border-bottomx: 1px solid #818f49; padding-bottom: 2px; color: #ef8033; font: normal 1em Tahoma, 'Trebuchet MS', Arial;\">Admin Actions</span>";
		actions += getAdminOptions(solution, path, filename, target, filename=='', furl, properties);
	}

	actionsDiv.innerHTML = actions;
}

var HIDDEN = 1;
var POPPING = 2;
var SHOWN = 3;
var STUCK = 4;
var DYING = 5;
var flyStatus = HIDDEN;
var popTimer = null;
var dieTimer = null;

function hideFly(  ) {
	flyStatus = DYING;
	dieTimer = setTimeout( 'hideFly2()', 300 );
	window.status = "hiding";
	clearTimeout( popTimer );
}

function flyStay() {
	flyStatus = STUCK;
}

function changeFlyTab( tabNo ) {
	try {
		document.getElementById('img-t1').blur(); 
		document.getElementById('img-t2').blur(); 
	} catch (e) {}
	
	if( tabNo == 1 ) {
		document.getElementById('flyTab1').style.display='block'; 
		document.getElementById('flyTab2').style.display='none'; 
		document.getElementById('img-t1').src='/pentaho-style/images/btn_info_active.png'; 
		document.getElementById('img-t2').src='/pentaho-style/images/btn_actions.png'; 
	}
	if( tabNo == 2 ) {
		document.getElementById('flyTab1').style.display='none'; 
		document.getElementById('flyTab2').style.display='block'; 
		document.getElementById('img-t1').src='/pentaho-style/images/btn_info.png'; 
		document.getElementById('img-t2').src='/pentaho-style/images/btn_actions_active.png'; 
	}
}

function hideFly2(  ) {
	if( flyStatus == DYING ) {
		var div=document.getElementById('flydiv'); div.style.top='-1000px';
		document.getElementById('flyTab1').style.display='block'; 
		document.getElementById('flyTab2').style.display='none'; 
		try {
		document.getElementById('img-t1').blur(); 
		document.getElementById('img-t2').blur(); 
		} catch (e) {}
		document.getElementById('img-t1').src='/pentaho-style/images/btn_info_active.png'; 
		document.getElementById('img-t2').src='/pentaho-style/images/btn_actions.png'; 
	}
	flyStatus = HIDDEN;
}

function showFly( obj, event, width, height, title, desc, iconSrc, target, solution, path, filename, furl, properties, hasModifyAcl ) {
	hideFly2();
	var pos = getFlyXY( obj, event, 502, 203); 
	var top=pos[1]; 
	var left=pos[0]; 
	flyTableSetup( title, desc, iconSrc, target, solution, path, filename, furl, properties, hasModifyAcl ); 
	flyStatus = POPPING;
	popTimer = setTimeout( 'showFly2( '+top+', '+left+' )', 500 );
}

function showFly2( top, left, width, height, title, desc, iconSrc ) {

	var div=document.getElementById('flydiv'); 
	div.style.top=''+top+'px'; 
	div.style.left=''+left+'px'; 
	flyStatus = SHOWN;
}


function getFlyXY( obj, event, width, height ) {

	// array contains x, y, and callout corner
	var margin = 5;
	var results = new Array(3);
	results[0] = 10;
	results[1] = 10;
	results[2] = 7;

	var h = 0;
	var w = 0;
	var docX = 0;
	var docY = 0;
	if( obj ) {
		h = obj.offsetHeight;
		w = obj.offsetWidth;
		var o = obj;
		while( o && o != document ) {
			docY += o.offsetTop;
			if( o.scrollTop ) {
				docY -= o.scrollTop;
			}
			o = o.offsetParent;
		}
		docX = obj.offsetLeft+obj.offsetParent.offsetLeft;
		
	} else {
		docX = getDocumentX( event );
		docY = getDocumentY( event );
	}
	var winX = event.clientX;
	var winY = event.clientY;

	var maxX, maxY;
	if (self.innerHeight) // all except Explorer
	{
		maxX = self.innerWidth;
		maxY = self.innerHeight;
	}
	else if (document.documentElement && document.documentElement.clientHeight)
		// Explorer 6 Strict Mode
	{
		maxX = document.documentElement.clientWidth;
		maxY = document.documentElement.clientHeight;
	}
	else if (document.body) // other Explorers
	{
		maxX = document.body.clientWidth;
		maxY = document.body.clientHeight;
	}

	maxY += document.documentElement.scrollTop;

	// calculate the margins all around
	var topGap, leftGap, rightGap, bottomGap;
	var topGap = winY;
	var bottomGap = maxY - winY;
	var leftGap = winX;
	var rightGap = maxX - winX;
	
	// see where this fits best
	// try the default - right and above
	if( topGap > height && rightGap > width ) {
		results[0] = docX + margin;
		results[1] = docY-height - margin;
		results[2] = 11;
	}
	// try left and above
	else if( topGap > height && leftGap > width ) {
		results[0] = docX-width - margin + w;
		results[1] = docY-height - margin;
		results[2] = 7;
	}
	// try right and below
	else if( bottomGap > height && rightGap > width ) {
		results[0] = docX + margin;
		results[1] = docY + margin + h;
		results[2] = 15;
	}
	// try left and below
	else if( bottomGap > height && leftGap > width ) {
		results[0] = docX-width - margin + w;
		results[1] = docY + margin + h;
		results[2] = 7;
	}
	// try top
	else if( topGap > height ) {
		results[0] = docX-width/2;
		results[1] = docY-height - margin;
		results[2] = 1;
	}
	// try left
	else if( leftGap > width ) {
		results[0] = docX-width - margin + w;
		results[1] = docY-height/2;
		results[2] = 13;
	}
	// try bottom
	else if( bottomGap > height ) {
		results[0] = docX-width/2;
		results[1] = docY + margin + h;
		results[2] = 13;
	}
	// try right
	else  {
		results[0] = docX + margin;
		results[1] = docY-height/2;
		results[2] = 13;
	}

	if( results[0] < 5 ) {
		results[0] = 5;
	} 
	else if( (results[0]+width) > maxX ) {
		results[0] = maxX - width - 5;
	}
	
	if( results[1] < 5 ) {
		results[1] = 5;
	} 
	else if( (results[1]+height) > maxY ) {
		results[1] = maxY - height - 5;
	}
	
	
	return results;

}
