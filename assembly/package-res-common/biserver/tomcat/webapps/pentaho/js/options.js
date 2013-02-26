/*
 * Copyright 2006 - 2010 Pentaho Corporation. All Rights Reserved.
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
*/
function runInBackground( url, target )
{
	var response = confirm( "Info:  Reports that prompt for parameters are not supported with this feature."
		+ "\nRun in Background may generate content with incorrect results." );

	if ( response )
	{
		url = url + "&background=true";
		if ( target.toLowerCase().indexOf( 'new' ) >= 0 )
		{
			var targetWin = window.open( url );
		}
		else
		{
			window.location = url;
		}
	}
	return undefined;	// forces current page to remain unchanged when target=new
}

function getOptions( solution, path, filename, target, isFolder, furl, properties, hasModifyAcl  ) {

	var actions = "";
	if( filename.indexOf( '.url' ) == (filename.length - 4) ) {
		actions += "<a href=\""+furl+"\" target=\""+target+"\">Run</a>";
		return actions;
	}

	var actions = "";
	if( !isFolder ) {
		var url = "ViewAction?solution=" + solution
		  + "&path=" + path
		  + "&action=" + filename;
		actions += "<a href=\""+url+"\" target=\""+target+"\">Run</a>&nbsp;|&nbsp;";
		actions += "<a href='javascript:runInBackground(\"" + url + "\", \"" + target + "\");'>Background</a>";

		if ( isWaqrAction( filename ) )
		{
			var waqrEditUrl = getWaqrEditUrl( solution, path, filename, furl );
			actions +=  "&nbsp;|&nbsp;<a href=\""+waqrEditUrl+"\" target=\""+target+"\">Open</a>";

			var waqrDeleteUrl = getWaqrDeleteUrl( solution, path, filename );
			actions +=  "&nbsp;|&nbsp;<a href='" + waqrDeleteUrl + "'>Delete</a>";
		}
    actions += getShareOption( solution, path, filename, target, furl, hasModifyAcl );
		
		if( properties.indexOf( "subscribable=true" ) != -1 ) {		
			actions += "<br/><a href='"+url+"&subscribepage=yes' target='"+target+"'>Subscribe</a>";
		}
	}

	return actions;
};

/**
 * replace single quotes with the hexadecimal character reference &apos;
 * @param str String string to be encoded
 */
function encodeSingleQuote( str )
{
	return str.replace( /'/g, "&apos;" );
};

function getWaqrDeleteUrl( solution, path, filename )
{
  var xreportSpecFilename = getBaseWaqrFilename( filename ) + ".xreportspec";
  var url = "javascript:deleteWaqrReport(\"" 
    + encodeSingleQuote( solution ) + "\",\"" 
    + encodeSingleQuote( path ) + "\",\"" 
    + encodeSingleQuote( xreportSpecFilename ) + "\");";
  
	return url;
};

function deleteWaqrReport( solution, path, filename )
{
	var yes = window.confirm( "Please confirm that you want to delete " + filename + ".");
	try {
		if ( yes )
		{
			var component = "deleteWaqrReport";
			var params = {
        solution: solution,
        path: path,
        filename: filename
			};
			var responseMsg = WebServiceProxy.post( WebServiceProxy.ADHOC_WEBSERVICE_URL, component, params, undefined, 'text/xml' );
			if ( undefined !== responseMsg )
  			{
  			var errorMsg = XmlUtil.getErrorMsg( responseMsg );
  			if ( errorMsg )
  			{
  				alert( errorMsg );
  			}
  			else
  			{
  				var statusMsg = XmlUtil.getStatusMsg( responseMsg );
  				if ( statusMsg )
  				{
  					alert( statusMsg );
  				}
  				// refresh the page so that the deleted xaction is no longer there
  				window.location = window.location;
  			}
			}
      // else the user session has expired, WebServiceProxy.post 
      // will take care of prompting the user to login again.
		}
	}
	catch( e )
	{
		alert( e );
	}
};

function getWaqrEditUrl( solution, path, filename, url )
{
	var beginUrl = getProtocolHostPortContextParts( url ) + "/adhoc/waqr.html";
	var baseFilename = getBaseWaqrFilename( filename );
  var queryString = "?solution=" + encodeURIComponent( solution )
    + "&path=" + encodeURIComponent( path )
    + "&filename=" + encodeURIComponent( baseFilename ) + ".xreportspec";
  
	return beginUrl + queryString;
};
function getBaseWaqrFilename( filename )
{
	var matched = filename.match( /(.*)\.xaction/ );	// get everything in the filename except the .xaction extension
	return matched[ 1 ];
}
function getBaseFilename( filename )
{
	var matched = filename.match( /(.*)\.waqr\.xaction/ );	// get everything in the filename except the .waqr.xaction extension
	return matched[ 1 ];
}
function getProtocolHostPortContextParts( strUrl )
{
	var matched = strUrl.match( /(.*)\/ViewAction.*/ );	// get everything before "/ViewAction ..."
	return matched[ 1 ];
}
function isWaqrAction( filename )
{
	return null != filename.match( /.*\.waqr\.xaction/ );	// test to see if filename ends with .waqr.xaction
}

// properties is often "subscribable=false" (or true)
function getAdminOptions( solution, path, filename, target, isFolder, furl, properties) {

	var permUrl = "PropertiesEditor?path=/" + gRepositoryName
    + ( !StringUtils.isEmpty( solution ) ? "/" + solution : "" ) 
    + ( !StringUtils.isEmpty( path ) ? "/" + path : "" ) 
    + ( !StringUtils.isEmpty( filename ) ? "/" + filename : "" )  ;
	var actions = "<br/><br/><a href=\""+permUrl+"\" >Permissions</a>"
	return actions;
}

function getShareOption( solution, path, filename, target, furl, hasModifyAcl )
{
  if( hasModifyAcl )
  {		
    var option = "&nbsp;|&nbsp;<a href='javascript:void(0)'"
      + "onclick='javascript:showShareDialog( event,\"" 
      + encodeSingleQuote( solution ) + "\", \""
      + encodeSingleQuote( path ) + "\", \""
      + encodeSingleQuote( filename )
      + "\");" + "'>Share</a>";
    return option;
  }
  else
  {
    return "";
  }
}

var shareDialog = null;
var shareDialogController = null;
function showShareDialog( event, solution, path, filename )
{
  if ( null == shareDialog )
  {
  	shareDialog = new AclEditorDialog( "aclDialog.dialogId", "modalMaskId" );
    shareDialogController = new AclEditorController( shareDialog );
  }
  shareDialogController.loadPage( solution, path, filename );
  var position = UIUtil.getScrollCoords( { left: event.clientX, top: event.clientY } );
  shareDialog.setPosition( { left: position.left+ "px", top: position.top + "px" } );
  shareDialog.show();
}