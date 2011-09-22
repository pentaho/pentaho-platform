
<%@page import="org.pentaho.platform.api.engine.IMessageFormatter"%><%@ page language="java" 
	import="java.util.ArrayList,
	org.pentaho.platform.util.web.SimpleUrlFactory,
    org.pentaho.platform.web.jsp.messages.Messages,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.uifoundation.chart.CategoryDatasetChartComponent,
	org.pentaho.platform.uifoundation.chart.JFreeChartEngine,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
    org.pentaho.platform.api.engine.IUITemplater,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.commons.connection.IPentahoConnection,
	org.pentaho.commons.connection.IPentahoResultSet,
	org.pentaho.platform.engine.services.connection.PentahoConnectionFactory,
	org.pentaho.platform.util.logging.SimpleLogger,
    org.pentaho.platform.engine.core.system.PentahoSessionHolder"
	 %><%
	
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
*/

	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

     IPentahoSession userSession = PentahoSessionHolder.getSession();
	HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );
	int chartType = (int)requestParameters.getLongParameter("ChartType", JFreeChartEngine.UNDEFINED_CHART_TYPE); //$NON-NLS-1$
	String chartDefinitionPath = requestParameters.getStringParameter("ChartDefinitionPath", null); //$NON-NLS-1$
	
	String thisUrl = baseUrl + "Chart?"; //$NON-NLS-1$
	String intro = ""; //$NON-NLS-1$
	String footer = ""; //$NON-NLS-1$
	String content = ""; //$NON-NLS-1$

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();
	CategoryDatasetChartComponent barChart = new CategoryDatasetChartComponent( chartType, chartDefinitionPath, 600, 400, urlFactory, messages );

    IPentahoConnection connection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, "SampleData", userSession, userSession); //$NON-NLS-1$
    try {
	    String query = "select department, actual, budget, variance from QUADRANT_ACTUALS"; //$NON-NLS-1$
	
	    IPentahoResultSet results = connection.executeQuery(query);
	    try {
		    
		    barChart.setValues(results);
			barChart.validate( userSession, null );
			
			barChart.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
			barChart.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
			
			content = barChart.getContent( "text/html" ); //$NON-NLS-1$
			if( content == null ) {
				StringBuffer buffer = new StringBuffer();		
				PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", Messages.getInstance().getErrorString( "CHART.DISPLAY_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
				content = buffer.toString();
			}
		
			IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
			if( templater != null ) {
				String sections[] = templater.breakTemplate( "template-document.html", Messages.getInstance().getString( "CHART.USER_SAMPLES" ), userSession ); //$NON-NLS-1$ //$NON-NLS-2$
				if( sections != null && sections.length > 0 ) {
					intro = sections[0];
				}
				if( sections != null && sections.length > 1 ) {
					footer = sections[1];
				}
			} else {
				intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" ); //$NON-NLS-1$
			}
	    } finally {
	    	results.close();
	    }
    } finally {
    	connection.close();
    }
%><%= intro %>
<%= content %>