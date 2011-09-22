<%@page import="java.util.HashMap"%>
<%@page import="org.dom4j.Document"%>
<%@ page language="java" 
	import="org.pentaho.platform.engine.core.system.PentahoSystem,
	        org.pentaho.platform.engine.core.system.PentahoSessionHolder,
			org.pentaho.platform.api.engine.IPentahoSession,
			org.pentaho.platform.util.xml.XmlHelper,
			org.pentaho.platform.engine.services.SolutionURIResolver,
            org.pentaho.platform.web.jsp.messages.Messages,
			org.pentaho.platform.api.engine.IUITemplater,
			org.pentaho.platform.web.http.WebTemplateHelper,
			org.pentaho.platform.util.messages.LocaleHelper,
			org.pentaho.platform.util.VersionHelper,
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
 * @author Gretchen Moran
 * 
 */
 
	response.setCharacterEncoding(LocaleHelper.getSystemEncoding());
 	String baseUrl = PentahoSystem.getApplicationContext().getBaseUrl();
 
	String path = request.getContextPath();

	IPentahoSession userSession = PentahoSessionHolder.getSession();
	//PPP-2148, BISERVER-3521: have to set the session back to the thread local since J2EE filters may have prematurely cleared it
	PentahoSessionHolder.setSession(userSession);

	String publish = request.getParameter( "publish" ); //$NON-NLS-1$	
	String style = request.getParameter( "style" ); //$NON-NLS-1$	
	String className = request.getParameter( "class" ); //$NON-NLS-1$	
	String message = ""; //$NON-NLS-1$
	String content = ""; //$NON-NLS-1$
	if( "now".equals( publish ) ) { //$NON-NLS-1$
		message = PentahoSystem.publish(userSession, className);
	}
	String templateName="template-dialog.html";
	if( "popup".equals( style ) ) {
		content = message;
	} else {
		Document publishersDocument = PentahoSystem.getPublishersDocument();
		if( publishersDocument != null ) {
			HashMap parameters = new HashMap();
			parameters.put( "message", message ); //$NON-NLS-1$
			StringBuffer sb = XmlHelper.transformXml( "publishers.xsl", null, publishersDocument.asXML(), parameters, new SolutionURIResolver(userSession) ); //$NON-NLS-1$
			if( sb != null ) {
				content = sb.toString();
			} else {
				content = Messages.getInstance().getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$
			}
		}
	}
	
	String intro = "";
	String footer = "";
	IUITemplater templater = PentahoSystem.get(IUITemplater.class, userSession );
	if( templater != null ) {
		String sections[] = templater.breakTemplate( templateName, Messages.getInstance().getString("UI.USER_PUBLISHER_TITLE"), userSession ); //$NON-NLS-1$ //$NON-NLS-2$
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
<%= content %>
<%= footer %>
