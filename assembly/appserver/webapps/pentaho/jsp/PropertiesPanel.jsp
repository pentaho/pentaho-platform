
<%@ page language="java" 
	import="java.util.ArrayList,
	java.util.List,
	org.pentaho.platform.util.web.SimpleUrlFactory,org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.VersionHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.uifoundation.component.xml.PropertiesPanelUIComponent,
	org.pentaho.platform.web.jsp.messages.Messages,
  org.pentaho.platform.engine.core.system.PentahoSessionHolder"
%>
<%--
 * Copyright 2006 - 2010 Pentaho Corporation.  All rights reserved. 
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
--%>
<%
	PentahoSystem.systemEntryPoint();
	String content = null;
	IPentahoSession userSession = null;
	try {
		response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
		String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
	
		userSession = PentahoSessionHolder.getSession();
		HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
		HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );
		
		String thisUrl = baseUrl; //+ "./PropertiesEditor?"; //$NON-NLS-1$
		SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
		List messages = new ArrayList();
		
		PropertiesPanelUIComponent propsPanel = new PropertiesPanelUIComponent( urlFactory, messages, userSession );
		propsPanel.validate( userSession, null );
		propsPanel.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters );
		propsPanel.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters );
		content = propsPanel.getContent( "text/html" ); //$NON-NLS-1$
	} finally {
    	PentahoSystem.systemExitPoint();      
    }
%>
<html>
<head>
    <link href="/pentaho-style/styles-new.css" rel="stylesheet" type="text/css" />
</head>

<body class="" dir="{text-direction}">

	<%= content %>
</body>
</html>