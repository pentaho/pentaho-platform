<%--
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
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
--%>

<%@ page
    import="org.jboss.portal.theme.LayoutConstants,
            org.jboss.portal.theme.page.PageResult"%>
<%@ page import="org.jboss.portal.server.PortalConstants"%>
<%@ taglib uri="/WEB-INF/theme/portal-layout.tld" prefix="p" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
   boolean maximized = false;
   PageResult pageResult = (PageResult)request.getAttribute(LayoutConstants.ATTR_PAGE);
   if ("maximized".equals(pageResult.getLayoutState()))
   {
       maximized = true;
   }

%><title>2.6.1</title>
   <meta http-equiv="Content-Type" content="text/html;"/>
   <!-- to correct the unsightly Flash of Unstyled Content. -->
   <script type="text/javascript">

	var initFunctions = new Array();

	function PageLoad() {
		for( i=0; i<initFunctions.length; i++ ) {
			eval( initFunctions[i] );
		}
	}

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

   </script>
   <!-- inject the theme; default to the Nphalanx theme if nothing is selected for the portal or the page -->
   <p:theme themeName='Nphalanx'/>
   <!-- insert header content that was possibly set by portlets on the page -->
   <p:headerContent/>
   <script type="text/javascript" language="javascript" src="/pentaho-portal-layout/packed.js"></script>

</head>

<body id="body" onload="PageLoad()">
<p:region regionName='dashboardnav' regionID='dashboardnav'/>
	<table border="0" cellpadding="0" cellspacing="0" width="100%" style="padding:0;margin:20px 0px 0px 0px;">
		<tr valign="middle">
			<td>
				<div class="portalLogo" width="100%">
					<div class="sampleHeader" onClick="window.location='/portal/portal/pentaho'">
						<br/>Pentaho Business Intelligence Platform
						<br/>Portal Demo&nbsp;
					</div>
				</div>
			</td>
		</tr>
	</table>

	<table border="0" cellpadding="0" cellspacing="0" width="100%" style="padding:0">
		<tr valign="middle">
			<td width="100%" class="pageMenuContainer">
                  <%-- called pnavigation so as not to conflict with region name used by PageCustomizerInterceptor --%>
                  <p:region regionName='pnavigation' regionID='pnavigation'/>
			</td>

		</tr>
	</table>
<p/>
<div id="portal-container">
   <div id="sizer">
      <div id="expander">
         <div id="content-container">
            <!-- insert the content of the 'left' region of the page, and assign the css selector id 'regionA' -->
            <% if (maximized) { %>
              <table border="0"><tr><td valign="top">
                <p:region regionName='maximized' regionID='regionB'/>
              </td></tr></table>
            <% } else { %>
            <table border="0"><tr><td style="vertical-align:top;">
            <p:region regionName='left' regionID='regionA'/>
            </td>
            <td valign="top">
            <!-- insert the content of the 'center' region of the page, and assign the css selector id 'regionB' -->
            <p:region regionName='center' regionID='regionB'/>
            </td></tr></table>
            <% } %>
            <hr class="cleaner"/>

            <div id="footer-container" class="portal-copyright">Powered by <a class="portal-copyright"
                                                                              href="http://www.jboss.com/products/jbossportal">JBoss
               Portal</a><br/>
               <span id="ThemeBy"></span>
            </div>
         </div>
      </div>
   </div>
</div>
</body>
</html>

