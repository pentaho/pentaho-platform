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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.ui.INavigationComponent;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class NavigationContentGenerator extends BaseXmlContentGenerator {

	private static final long serialVersionUID = 2272261269875005948L;

	@Override
	public Log getLogger() {
		return LogFactory.getLog(NavigationContentGenerator.class);
	}

	@Override
	public String getContent() throws Exception {

		String solution = requestParameters.getStringParameter("solution", null); //$NON-NLS-1$
		if( "".equals( solution ) ) { //$NON-NLS-1$
			solution = null;
		}

		INavigationComponent navigate = PentahoSystem.get(INavigationComponent.class, userSession);
    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    String contextPath = requestContext.getContextPath();
		navigate.setHrefUrl(contextPath); //$NON-NLS-1$
		navigate.setOnClick("");//$NON-NLS-1$
		navigate.setSolutionParamName("solution");//$NON-NLS-1$
		navigate.setPathParamName("path");//$NON-NLS-1$
		navigate.setAllowNavigation(new Boolean(solution != null));
		navigate.setOptions("");//$NON-NLS-1$
		navigate.setUrlFactory(urlFactory);
		navigate.setMessages(messages);
		// This line will override the default setting of the navigate component
		// to allow debugging of the generated HTML.
		// navigate.setLoggingLevel( org.pentaho.platform.api.engine.ILogger.DEBUG );
		navigate.validate( userSession, null );
		navigate.setParameterProvider( IParameterProvider.SCOPE_REQUEST, requestParameters );
		navigate.setParameterProvider( IParameterProvider.SCOPE_SESSION, sessionParameters );
		
		String view = requestParameters.getStringParameter("view", null );//$NON-NLS-1$
		if( view != null ) {
			if( "default".equals( view ) ) { //$NON-NLS-1$
				userSession.removeAttribute( "pentaho-ui-folder-style" ); //$NON-NLS-1$
			} else {
				userSession.setAttribute( "pentaho-ui-folder-style", view );
				navigate.setXsl( "text/html", view ); //$NON-NLS-1$
			}
		} else {
			view = (String) userSession.getAttribute( "pentaho-ui-folder-style" );
			if( view != null ) {
				navigate.setXsl( "text/html", view ); //$NON-NLS-1$
			}
		}
		
		return navigate.getContent( "text/html" ); //$NON-NLS-1$
	}


}
