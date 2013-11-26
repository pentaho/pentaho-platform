/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.File;

@SuppressWarnings( "serial" )
/**
 * This servlet is the traffic cop for GWT services core to the BIServer.  See pentahoServices.spring.xml for bean
 * definitions referenced by this servlet.
 */
public class GwtRpcProxyServlet extends AbstractGwtRpcProxyServlet {

  @Override
  protected Object resolveDispatchTarget( String servletContextPath ) {
    ApplicationContext beanFactory = getAppContext();
    if ( servletContextPath.startsWith( "/" ) ) { //$NON-NLS-1$
      servletContextPath = servletContextPath.substring( 1 );
    }
    String beanId = servletContextPath.replaceAll( "/", "-" ); //$NON-NLS-1$ //$NON-NLS-2$

    if ( !beanFactory.containsBean( beanId ) ) {
      throw new GwtRpcProxyException( Messages.getInstance().getErrorString(
          "GwtRpcProxyServlet.ERROR_0001_NO_BEAN_FOUND_FOR_SERVICE", beanId, servletContextPath ) ); //$NON-NLS-1$
    }
    Object target = beanFactory.getBean( beanId );
    return target;
  }

  protected ApplicationContext getAppContext() {
    WebApplicationContext parent = WebApplicationContextUtils.getRequiredWebApplicationContext( getServletContext() );

    ConfigurableWebApplicationContext wac = new XmlWebApplicationContext() {
      @Override
      protected Resource getResourceByPath( String path ) {
        return new FileSystemResource( new File( path ) );
      }
    };
    wac.setParent( parent );
    wac.setServletContext( getServletContext() );
    wac.setServletConfig( getServletConfig() );
    wac.setNamespace( getServletName() );
    String springFile =
        PentahoSystem.getApplicationContext()
            .getSolutionPath( "system" + File.separator + "pentahoServices.spring.xml" ); //$NON-NLS-1$ //$NON-NLS-2$
    wac.setConfigLocations( new String[] { springFile } );
    wac.refresh();

    return wac;
  }

}
