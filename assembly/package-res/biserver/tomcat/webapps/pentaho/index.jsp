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
            org.owasp.esapi.ESAPI,
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
          //Check for deep linking by fetching the name and startup-url values from URL query parameters
          String name = (String) request.getAttribute("name");
          String startupUrl = (String) request.getAttribute("startup-url");
          if (startupUrl != null && name != null){
            //Sanitize the values assigned
            mobileRedirect += "?name=" + ESAPI.encoder().encodeForJavaScript(name) + "&startup-url=" + ESAPI.encoder().encodeForJavaScript(startupUrl);
          }
			    %>
			    <script type="text/javascript">
			  	  if(typeof window.top.PentahoMobile != "undefined"){
			  		  window.top.location.reload();
			  	  } else {
              var tag = document.createElement('META');
              tag.setAttribute('HTTP-EQUIV', 'refresh');
              tag.setAttribute('CONTENT', '0;URL=<%=mobileRedirect%>');
              document.getElementsByTagName('HEAD')[0].appendChild(tag);
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
