<%@ page language="java" 
	import="org.pentaho.platform.engine.core.system.PentahoSystem,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.util.xml.XmlHelper,
           	org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.engine.services.SolutionURIResolver,
			org.pentaho.platform.util.web.SimpleUrlFactory,
			org.pentaho.platform.engine.core.solution.SimpleParameterProvider,
			org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
			javax.xml.transform.TransformerException,
      org.pentaho.platform.engine.core.system.PentahoSessionHolder,
      org.pentaho.platform.api.ui.INavigationComponent" %><%

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

	IPentahoSession userSession = PentahoSessionHolder.getSession();

	StringBuffer sb = new StringBuffer();

	String header = Messages.getInstance().getString( "UI.USER_ADMIN_INTRO" ); //$NON-NLS-1$
	String admin = getAdminLinks( userSession );
	String publish = getPublisherContent( userSession );

	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template.html", "", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
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

	<%@page import="java.util.ArrayList"%>
<%@page import="org.dom4j.Document"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.pentaho.platform.api.ui.INavigationComponent;"%>
<table class='content_table' border='0' cellpadding='0' cellspacing='0' height='100%''>
		<tr>
			<td colspan='2' class='content_pagehead'>
				<%= header %>
			</td>
		</tr>
		<tr>
			<td class='contentcell_half_right' width='50%'>
				<%= admin %>
				<%= publish %>
			</td>
		</tr>
	</table>

<%= footer %>
<%!private final String getAdminLinks( IPentahoSession userSession ) {
	        SimpleParameterProvider parameters = new SimpleParameterProvider();
        	parameters.setParameter( "solution", "admin" );
		String navigateUrl = PentahoSystem.getApplicationContext().getBaseUrl() + "/Navigate?";
		SimpleUrlFactory urlFactory = new SimpleUrlFactory( navigateUrl );
		ArrayList messages = new ArrayList();
		INavigationComponent navigate = PentahoSystem.get(INavigationComponent.class, userSession);
		navigate.setHrefUrl(PentahoSystem.getApplicationContext().getBaseUrl());
		navigate.setOnClick("");
		navigate.setSolutionParamName("solution");
		navigate.setPathParamName("path");
		navigate.setAllowNavigation( new Boolean(false) );
		navigate.setOptions("");
		navigate.setUrlFactory(urlFactory);
		navigate.setMessages(messages);
		// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
		navigate.validate( userSession, null );
		navigate.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, parameters ); //$NON-NLS-1$
		navigate.setXsl( "text/html", "admin-mini.xsl" );
		String content = navigate.getContent( "text/html" ); //$NON-NLS-1$
		return content;
	}

	private final String getPublisherContent( IPentahoSession userSession ) {
		Document publishersDocument = PentahoSystem.getPublishersDocument();
		if( publishersDocument != null ) {
			HashMap parameters = new HashMap();
			try
			{
				StringBuffer sb = XmlHelper.transformXml( "publishers-mini.xsl", null, publishersDocument.asXML(), parameters, new SolutionURIResolver(userSession) ); //$NON-NLS-1$
				return sb.toString();
			} catch (TransformerException e )
			{
				return Messages.getInstance().getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$
			}
		}
		return Messages.getInstance().getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$

	}%>

