<%@ 
	page language="java" 
	import="java.util.ArrayList,
			org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.util.VersionHelper,
			org.pentaho.platform.api.ui.INavigationComponent,
			org.pentaho.platform.uifoundation.component.HtmlComponent,
			org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.uifoundation.chart.ChartHelper,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder" %><%

/*
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
 * @created Jul 23, 2005 
 * @author James Dixon
 * 
 */
 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();
	ArrayList messages; 
	

	IPentahoSession userSession = PentahoSessionHolder.getSession();

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template-home.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
			intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
			footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" );
	}

%>

	<%= intro %>
	

	<%@page%>
<center>
	<div valign="top" align="center">

<%  
	String location = request.getParameter("page");
	if (location == null){
		location = "samples/launch/launch.htm";
	}
	messages = new ArrayList();
	HtmlComponent html = new HtmlComponent( HtmlComponent.TYPE_SOLUTION_FILE, location, Messages.getInstance().getString("UI.USER_OFFLINE"), null, messages);
	html.validate( userSession, null ); %>

	<%=  html.getContent( "text/html" ) %>		

	</div>

  </center>

	<%= footer %>
	
