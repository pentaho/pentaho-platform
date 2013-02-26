
<%@ page language="java"
import="org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.util.web.SimpleUrlFactory,
	java.util.ArrayList,
	org.pentaho.platform.web.refactor.SolutionManagerUIComponent,
	org.pentaho.platform.web.jsp.messages.Messages,
	java.io.IOException,
  org.pentaho.platform.engine.core.system.PentahoSessionHolder,
  org.pentaho.platform.api.engine.IMessageFormatter"%><%

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
	String thisUrl = baseUrl + "./SolutionManager?"; //$NON-NLS-1$

	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );
	ArrayList messages = new ArrayList();

	SolutionManagerUIComponent manager = new SolutionManagerUIComponent( urlFactory, messages, userSession ); //$NON-NLS-1$
	manager.validate( userSession, null );
	
	manager.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
	manager.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
	
	String content = manager.getContent( "text/xml" ); //$NON-NLS-1$
	if( content == null ) {
		StringBuffer buffer = new StringBuffer();
		PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", Messages.getInstance().getErrorString( "SOLUTION_MANAGER.ERROR_0001_DISPLAY_ERROR" ), messages, buffer ); //$NON-NLS-1$ //$NON-NLS-2$
		content = buffer.toString();
	}

	try {
		out.print( content );
	} catch (IOException e) {}
%>