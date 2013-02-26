

<%@ page language="java" 
	import="java.util.ArrayList,
	org.pentaho.platform.util.web.SimpleUrlFactory,
	org.pentaho.platform.web.jsp.messages.Messages,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.uifoundation.chart.PieDatasetChartComponent,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.api.engine.IUITemplater,
	org.pentaho.platform.util.logging.SimpleLogger,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.commons.connection.IPentahoConnection,
	org.pentaho.commons.connection.IPentahoResultSet,
	org.pentaho.commons.connection.PentahoDataTransmuter,
	org.pentaho.platform.engine.services.connection.PentahoConnectionFactory,
  org.pentaho.platform.engine.core.system.PentahoSessionHolder,
  org.pentaho.platform.api.engine.IMessageFormatter"
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
	String thisUrl = baseUrl + "./PieChart?"; //$NON-NLS-1$

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();

	PieDatasetChartComponent pieChart = new PieDatasetChartComponent( 2, "/public/pentaho-solutions/bi-developers/charts/pieChart.xml", 600, 400, urlFactory, messages ); //$NON-NLS-1$
	String intro = ""; //$NON-NLS-1$
	String footer = ""; //$NON-NLS-1$
	String content = ""; //$NON-NLS-1$
	
    IPentahoConnection connection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, "SampleData", userSession, userSession); //$NON-NLS-1$ 
    try {
	    String query = "SELECT DEPARTMENT, ACTUAL FROM QUADRANT_ACTUALS ORDER BY DEPARTMENT"; //$NON-NLS-1$
	    IPentahoResultSet results = connection.executeQuery(query);
	    try {
		    Integer[] columnsToInclude = new Integer[] {new Integer(1)};
		    IPentahoResultSet r2 = PentahoDataTransmuter.transmute(results, new Integer(0), null, null, columnsToInclude, true);
		    
		    pieChart.setValues(r2);
			pieChart.setTitle( Messages.getInstance().getString( "PIECHART.TEST_PIE_CHAR" )); //$NON-NLS-1$
			pieChart.validate( userSession, null );
			
			pieChart.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
			pieChart.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
			
			content = pieChart.getContent( "text/html" ); //$NON-NLS-1$
			if( content == null ) {
				StringBuffer buffer = new StringBuffer();
				PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", Messages.getInstance().getString("PIECHART.DISPLAY_ERROR"), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
				content = buffer.toString();
			}
		
			IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
			if( templater != null ) {
				String sections[] = templater.breakTemplate( "template-document.html", Messages.getInstance().getString("PIECHART.USER_SAMPLES"), userSession ); //$NON-NLS-1$ //$NON-NLS-2$
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

%>
<%= intro %>
<%= content %>