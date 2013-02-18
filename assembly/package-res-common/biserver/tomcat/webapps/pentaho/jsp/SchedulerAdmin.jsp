
<%@ page language="java" 
	import="java.util.ArrayList,
	 org.pentaho.platform.util.web.SimpleUrlFactory,
  	 org.pentaho.platform.engine.core.system.PentahoSystem,
  	 org.pentaho.platform.scheduler.SchedulerAdminUIComponent,
  	 org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
  	 org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
  	 org.pentaho.platform.api.engine.IPentahoSession,
  	 org.pentaho.platform.web.http.WebTemplateHelper,
  	 org.pentaho.platform.api.engine.IUITemplater,
  	 org.pentaho.platform.util.messages.LocaleHelper,
  	 org.apache.commons.lang.StringUtils,
  	 org.pentaho.platform.web.jsp.messages.Messages,
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
	String thisUrl = baseUrl + "./SchedulerAdmin?"; //$NON-NLS-1$
	
	String mimeType = request.getParameter( "requestedMimeType" );
	if ( StringUtils.isEmpty( mimeType ) ) {
	  mimeType = "text/html";
	}

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();

	SchedulerAdminUIComponent admin = new SchedulerAdminUIComponent( urlFactory, messages ); //$NON-NLS-1$

	admin.validate( userSession, null );
	
	admin.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
	admin.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
	
	String content = admin.getContent( mimeType );
	if ( "text/html".equals( mimeType ) ) {
		if( content == null ) {
			StringBuffer buffer = new StringBuffer();
			PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", Messages.getInstance().getErrorString( "SCHEDULER_ADMIN.ERROR_0001_DISPLAY_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
			content = buffer.toString();
		}
	
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
	
		// Content had $ signs - the regex stuff messes up with $ and \ so...
	    content = content.replaceAll( "\\\\", "\\\\\\\\" );
	    content = content.replaceAll( "\\$", "\\\\\\$" );
		%><%= intro %>
		<%= content %>
		<%= footer %><%    
	} else {
		if( content == null ) {
		  content = "<error msg='" + Messages.getInstance().getErrorString( "SCHEDULER_ADMIN.ERROR_0001_DISPLAY_ERROR" ) + "'></error>";
		}
		%><%=content%><%  
	}
%>