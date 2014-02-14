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

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * This should only be used by a plugin in the plugin.spring.xml file to initialize a Jersey. The presence of this
 * servlet in the spring file will make it possible to write JAX-RS POJOs in your plugin.
 * 
 * @author Aaron Phillips
 */
public class JAXRSServlet extends SpringServlet {

  private static final long serialVersionUID = 457538570048660945L;

  private static final Log logger = LogFactory.getLog( JAXRSServlet.class );
  public static final String MIME_TYPE = "mime-type";
  public static final String GET_HEADERS = "getHeaders";
  public static final String ACCEPT = "accept";
  public static final String GET = "GET";

  @Override
  protected ConfigurableApplicationContext getContext() {
    return (ConfigurableApplicationContext) getAppContext();
  }

  @Override
  public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException {

    if ( logger.isDebugEnabled() ) {
      logger.debug( "servicing request for resource " + request.getPathInfo() ); //$NON-NLS-1$
    }

    if ( request.getMethod().equals( GET ) ) {
      // Extension to allow accept type override from mime-type query param
      final String mimeType = request.getParameter( MIME_TYPE );
      if ( mimeType != null ) {
        final HttpServletRequest originalRequest = request;

        request =
            (HttpServletRequest) Proxy.newProxyInstance( getClass().getClassLoader(),
                new Class[] { HttpServletRequest.class }, new InvocationHandler() {
                  public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {
                    if ( method.getName().equals( GET_HEADERS ) && args.length > 0 && args[0].equals( ACCEPT ) ) {
                      return new Enumeration() {
                        boolean hasMore = true;

                        @Override
                        public boolean hasMoreElements() {
                          return hasMore;
                        }

                        @Override
                        public Object nextElement() {
                          hasMore = false;
                          return mimeType;
                        }
                      };
                    }
                    return method.invoke( originalRequest, args );
                  }
                } );
      }
    }
    super.service( request, response );
  }

  @Override
  public void service( ServletRequest req, ServletResponse res ) throws ServletException, IOException {
    super.service( req, res );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected void initiate( ResourceConfig rc, WebApplication wa ) {
    if ( logger.isDebugEnabled() ) {
      rc.getFeatures().put( ResourceConfig.FEATURE_TRACE, true );
      rc.getFeatures().put( ResourceConfig.FEATURE_TRACE_PER_REQUEST, true );
    }
    super.initiate( rc, wa );
    if ( logger.isDebugEnabled() ) {
      Map<MediaType, List<MessageBodyWriter>> writers = wa.getMessageBodyWorkers()
        .getWriters( MediaType.WILDCARD_TYPE );
      logger.debug( "Writers: " + writers ); //$NON-NLS-1$
    }
  }

  protected ApplicationContext getAppContext() {

    ConfigurableWebApplicationContext wac = new XmlWebApplicationContext() {
      @Override
      protected Resource getResourceByPath( String path ) {
        return new FileSystemResource( new File( path ) );
      }
    };

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
