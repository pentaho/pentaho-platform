
<%@ page language="java" 
	import="java.util.ArrayList,
	org.pentaho.platform.api.engine.ILogger,
	org.pentaho.platform.util.web.SimpleUrlFactory,
	org.pentaho.platform.engine.core.system.PentahoSystem,
	org.pentaho.platform.uifoundation.component.xml.InputFormComponent,
	org.pentaho.platform.web.http.request.HttpRequestParameterProvider,
	org.pentaho.platform.web.http.session.HttpSessionParameterProvider,
	org.pentaho.platform.api.engine.IPentahoSession,
	org.pentaho.platform.web.http.WebTemplateHelper,
	org.pentaho.platform.util.messages.LocaleHelper,
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
	String hrefUrl = baseUrl + "ViewAction?"; //$NON-NLS-1$
	String onClick = ""; //$NON-NLS-1$
	String thisUrl = baseUrl + "./Navigate?"; //$NON-NLS-1$

	String instanceId = request.getParameter( "instance-id" ); //$NON-NLS-1$
	String solution = request.getParameter( "solution" ); //$NON-NLS-1$
	String path = request.getParameter( "path" ); //$NON-NLS-1$
	String actionName = request.getParameter( "action" ); //$NON-NLS-1$
	String templateName = request.getParameter( "template" ); //$NON-NLS-1$
	String stylesheetName = request.getParameter( "css" ); //$NON-NLS-1$
	
	SimpleUrlFactory urlFactory = new SimpleUrlFactory( thisUrl );

	ArrayList messages = new ArrayList();

	InputFormComponent inputForm = new InputFormComponent( urlFactory, instanceId, templateName, stylesheetName, solution, path, actionName, messages );
	inputForm.setLoggingLevel( ILogger.DEBUG );

	inputForm.setParameterProvider( HttpRequestParameterProvider.SCOPE_REQUEST, requestParameters ); //$NON-NLS-1$
	inputForm.setParameterProvider( HttpSessionParameterProvider.SCOPE_SESSION, sessionParameters ); //$NON-NLS-1$
	String content = null;
	if( inputForm.validate( userSession, null ) ) {
		content = inputForm.getContent( "text/html" ); //$NON-NLS-1$
	}
	if( content == null ) {
		StringBuffer buffer = new StringBuffer();
		PentahoSystem.get(IMessageFormatter.class, userSession).formatErrorMessage( "text/html", "Could not create inbox task display", messages, buffer ); //$NON-NLS-1$
		content = buffer.toString();
	} else {
		out.print( content );
	}
%>
