<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core'%>
<%@ page language="java"
    import="
            org.pentaho.platform.engine.core.system.PentahoSystem,
            org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.api.engine.IPluginManager,
            java.util.List,
            org.pentaho.platform.engine.core.system.PentahoSessionHolder"
%>
			
<html>
  <head>
    
    <title>Pentaho Business Analytics</title>    

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
			  	  if(typeof window.top.PentahoMobile != "undefined"){
			  		  window.top.location.reload();
			  	  } else {
			  		  document.write('<META HTTP-EQUIV="refresh" CONTENT="0;URL=<%=mobileRedirect%>">');
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