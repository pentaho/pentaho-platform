<%@ 
	page language="java" 
	import="
			java.util.ArrayList,
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
	String path = request.getContextPath();
     IPentahoSession userSession = PentahoSessionHolder.getSession();

%>
	
<%
	// See if we have a 'territory' parameter
	String territory = request.getParameter("territory");
	// See if we have a 'productline' parameter
	String productline = request.getParameter("productline");

	// Create the title for the top of the page
	String title = "Top Ten Customers";
	if( territory == null && productline != null) {
		title = "Top Ten for " + productline;
	} 
	else if ( territory != null && productline == null) {
		title = "Top Ten for " + territory;
	}
	else if ( territory == null && productline == null) {
		title = "Top Ten Customers";
	}
	else  {
		title = "Top Ten for " + territory + ", " + productline;
	}
	
	String pie1 = "";
	String pie2 = "";
	String chart = "";

	SimpleParameterProvider parameters = new SimpleParameterProvider();
	parameters.setParameter( "drill-url", "PreviousHome?territory={territory}" );
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-width", "350"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "200"); //$NON-NLS-1$ //$NON-NLS-2$
	StringBuffer content = new StringBuffer(); 
	ArrayList messages = new ArrayList();
	ChartHelper.doPieChart( "/public/pentaho-solutions/steel-wheels/homeDashboard/territory.widget.xml", parameters, content, userSession, messages, null ); 

	pie1 = content.toString();
	 
	parameters = new SimpleParameterProvider();

	if( territory == null ) {
	parameters.setParameter( "drill-url", "PreviousHome?productline={productline}" );
	} else {
	parameters.setParameter( "drill-url", "PreviousHome?territory="+territory+"&amp;productline={productline}" );
	}
	
	parameters.setParameter( "territory", territory );
	parameters.setParameter( "productline", productline );
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "inner-param", "productline"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-width", "350"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "200"); //$NON-NLS-1$ //$NON-NLS-2$
	content = new StringBuffer(); 
	messages = new ArrayList();
    ChartHelper.doPieChart( "/public/pentaho-solutions/steel-wheels/homeDashboard/productline.widget.xml", parameters, content, userSession, messages, null ); 
	pie2 = content.toString();
	
	parameters = new SimpleParameterProvider();
	parameters.setParameter( "image-width", "400"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "image-height", "400"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "territory", territory );
	parameters.setParameter( "productline", productline );  			
	parameters.setParameter( "inner-param", "territory"); //$NON-NLS-1$ //$NON-NLS-2$
	parameters.setParameter( "inner-param", "productline"); //$NON-NLS-1$ //$NON-NLS-2$

	content = new StringBuffer(); 
	messages = new ArrayList();
	ChartHelper.doChart( "/public/pentaho-solutions/steel-wheels/homeDashboard/customer.widget.xml", parameters, content, userSession, messages, null ); 
	chart = content.toString();

	%>

<html>
	<head>
		<title>Steel Wheels - Top Ten</title>
	</head>
	<body>
  	<table  background="/sw-style/active/logo_backup.png">
  		<tr>
  			<td width="750" height="40" align="right" valign="middle" style="font-family:Arial;font-weight:bold" border="0"/><%= title %></td>
  		</tr>		
  	</table>	
  		<table class="homeDashboard" cellpadding="0" cellspacing="0" border="0" >
			<tr>
				<td valign="top" align="center"><%= pie1 %></td>
				<td rowspan="2" valign="top">
					<%= chart %>
				</td>
			</tr>
			<tr>
				<td valign="top" align="center">
					<%= pie2 %>
				</td>
			</tr>
 		</table>
</body>
</html>	
	
