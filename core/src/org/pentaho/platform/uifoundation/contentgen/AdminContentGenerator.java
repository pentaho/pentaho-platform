/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * 
 * Copyright 2008 Pentaho Corporation.  All rights reserved. 
 * 
 */
package org.pentaho.platform.uifoundation.contentgen;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.SolutionURIResolver;
import org.pentaho.platform.uifoundation.messages.Messages;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.util.xml.XmlHelper;

public class AdminContentGenerator extends BaseXmlContentGenerator {

	private static final long serialVersionUID = 2272261269875005948L;

	@Override
	public Log getLogger() {
		return LogFactory.getLog(AdminContentGenerator.class);
	}

	@Override
	public String getContent() throws Exception {

		String header = Messages.getInstance().getString( "UI.USER_ADMIN_INTRO" ); //$NON-NLS-1$
		String admin = getAdminLinks( userSession );
		String publish = getPublisherContent( userSession );
		
		// worth putting this table into a template?
		
		StringBuilder sb = new StringBuilder();
		sb.append( "<table class='content_table' border='0' cellpadding='0' cellspacing='0' height='100%''>\n" );//$NON-NLS-1$
		sb.append( 		"<tr>\n" );//$NON-NLS-1$
		sb.append( 			"<td colspan='2' class='content_pagehead'>\n" );//$NON-NLS-1$
		sb.append( header );
		sb.append( 			"</td>\n" );//$NON-NLS-1$
		sb.append( 		"</tr>\n" );//$NON-NLS-1$
		sb.append( 		"<tr>\n" );//$NON-NLS-1$
		sb.append( 			"<td class='contentcell_half_right' width='50%'>\n" );//$NON-NLS-1$
		sb.append( admin );
		sb.append( publish );
		sb.append( 			"</td>\n" );//$NON-NLS-1$
		sb.append( 		"</tr>\n" );//$NON-NLS-1$
		sb.append( "</table>\n" );//$NON-NLS-1$
		
		return sb.toString(); 
	}

	private final String getAdminLinks( IPentahoSession userSession ) {
        SimpleParameterProvider parameters = new SimpleParameterProvider();
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    String contextPath = requestContext.getContextPath();        
    	parameters.setParameter( "solution", "admin" ); //$NON-NLS-1$ //$NON-NLS-2$
	String navigateUrl = contextPath + "Navigate?"; //$NON-NLS-1$
	SimpleUrlFactory urlFactory = new SimpleUrlFactory( navigateUrl );
	ArrayList messages = new ArrayList();
	INavigationComponent navigate = PentahoSystem.get(INavigationComponent.class, userSession);
	navigate.setHrefUrl(contextPath);//$NON-NLS-1$
	navigate.setOnClick(""); //$NON-NLS-1$
	navigate.setSolutionParamName("solution"); //$NON-NLS-1$
	navigate.setPathParamName("path"); //$NON-NLS-1$
	navigate.setAllowNavigation( new Boolean(false) );
	navigate.setOptions(""); //$NON-NLS-1$
	navigate.setUrlFactory(urlFactory);
	navigate.setMessages(messages);
	// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
	navigate.validate( userSession, null );
	navigate.setParameterProvider( IParameterProvider.SCOPE_REQUEST, parameters );
	navigate.setXsl( "text/html", "admin-mini.xsl" ); //$NON-NLS-1$ //$NON-NLS-2$
	String content = navigate.getContent( "text/html" ); //$NON-NLS-1$
	return content;
}

private final String getPublisherContent( IPentahoSession userSession ) {
	Document publishersDocument = PentahoSystem.getPublishersDocument();
	if( publishersDocument != null ) {
		HashMap parameters = new HashMap();
		try
		{
			StringBuffer sb = XmlHelper.transformXml( "publishers-mini.xsl", null, publishersDocument.asXML(), parameters, new SolutionURIResolver() ); //$NON-NLS-1$
			return sb.toString();
		} catch (TransformerException e )
		{
			return Messages.getInstance().getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$
		}
	}
	return Messages.getInstance().getErrorString( "PUBLISHERS.ERROR_0001_PUBLISHERS_ERROR" ); //$NON-NLS-1$

}

}
