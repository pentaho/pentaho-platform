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

<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@ page language="java"
    import="
            org.pentaho.platform.engine.core.system.PentahoSystem,
            org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.api.engine.IPluginManager,
            java.util.List,
            org.pentaho.platform.engine.core.system.PentahoSessionHolder,
            java.util.ResourceBundle,
            java.net.URLClassLoader, java.net.URL"
%>
			
<html>
  <head>
  <% 
  URLClassLoader loader = new URLClassLoader( new URL[] { application.getResource( "/mantle/messages/" ) } );
  ResourceBundle properties = ResourceBundle.getBundle( "mantleMessages", request.getLocale(), loader );
%>
    
    <title><%= properties.getString("pentahoBATitle") %></title>    

    <script type="text/javascript" src="webcontext.js"></script>

	<%
		boolean haveMobileRedirect = false;		
		String ua = request.getHeader("User-Agent").toLowerCase();
		if (!"desktop".equalsIgnoreCase(request.getParameter("mode"))) {		
		  if (ua.contains("ipad") || ua.contains("ipod") || ua.contains("iphone") || ua.contains("android") || "mobile".equalsIgnoreCase(request.getParameter("mode"))) {		
		    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); 
		    List<String> pluginIds = pluginManager.getRegisteredPlugins();
		    for (String id : pluginIds) {
		      String mobileRedirect = (String)pluginManager.getPluginSetting(id, "mobile-redirect", null);
		      if (mobileRedirect != null) {
		        // we have a mobile redirect
			    haveMobileRedirect = true;
			    %>
			    <script type="text/javascript">
            //Get URL parameters
            var getParams = document.URL.split("?");
            var params = '';

            //If there are no GET parameters on the URL leave the params object empty so that the check for
            //a startup report setting is conducted
            if (getParams.length > 1) {
		          params = '?' + getParams[1];
            }
			  	  if(typeof window.top.PentahoMobile != "undefined"){
			  		  window.top.location.reload();
			  	  } else {
			  		  document.write('<META HTTP-EQUIV="refresh" CONTENT="0;URL=<%=mobileRedirect%>' + params + '">');
			  	  }
			    </script>
			    <%
			    break;
		      }
		    }
		  }
		  if (!haveMobileRedirect) {
			  %>
			  <META HTTP-EQUIV="refresh" CONTENT="0;URL=./Home">
			  <%
		  }
		}
	%>
	
  </head>
  <body>
  </body>
</html>
