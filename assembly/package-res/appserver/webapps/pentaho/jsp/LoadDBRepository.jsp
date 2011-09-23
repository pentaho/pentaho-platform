
<%@ page language="java" 
	import="
	java.util.ArrayList,
	org.pentaho.platform.util.web.SimpleUrlFactory,
	org.pentaho.platform.uifoundation.component.xml.LoadDBRepositoryUIComponent,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.VersionHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.api.engine.IUITemplater,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.web.jsp.messages.Messages,
	org.pentaho.platform.engine.security.SecurityHelper,
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
 * 
 */
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();

     IPentahoSession userSession = PentahoSessionHolder.getSession();
	HttpRequestParameterProvider requestParameters = new HttpRequestParameterProvider( request );
	HttpSessionParameterProvider sessionParameters = new HttpSessionParameterProvider( userSession );
	
	String thisUrl = baseUrl + "./LoadDBRepository?"; //$NON-NLS-1$
	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();
	
	LoadDBRepositoryUIComponent repository = new LoadDBRepositoryUIComponent( urlFactory, messages, userSession );
	repository.validate( userSession, null );
	repository.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters );
	repository.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters );
	
	String content = repository.getContent( "text/html" ); //$NON-NLS-1$
	if (content == null) {
		content = "BIG ERROR -- SHOULD NOT SEE THIS!!!";
	}
	
	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( "template-document.html",  "DB Load Utility", userSession ); //$NON-NLS-1$ //$NON-NLS-2$
		if( sections != null && sections.length > 0 ) {
	intro = sections[0];
		}
		if( sections != null && sections.length > 1 ) {
	footer = sections[1];
		}
	} else {
		intro = Messages.getInstance().getString( "UI.ERROR_0002_BAD_TEMPLATE_OBJECT" ); //$NON-NLS-1$
	}

	if( !SecurityHelper.isPentahoAdministrator(userSession) ) {
%>
	<%= intro %>
	<%= Messages.getInstance().getString( "UI.USER_PERMISSION_DENIED" ) %>
	<%= footer %>
<%
		return;
	}

%>

	<%= intro %>
	<%= content %>
	<%= footer %>
