/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.servlet;

import jakarta.inject.Provider;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Context;
import com.google.common.annotations.VisibleForTesting;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.servlet.ServletContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.jersey.servlet.WebConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PentahoBeanScopeValidatorPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
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
public class JAXRSServlet extends ServletContainer {

  private static final long serialVersionUID = 457538570048660945L;

  private static final Log logger = LogFactory.getLog( JAXRSServlet.class );
  public static final String MIME_TYPE = "mime-type";
  public static final String GET_HEADERS = "getHeaders";
  public static final String SET_STATUS = "setStatus";
  public static final String ACCEPT = "accept";
  public static final String GET = "GET";
  protected boolean SendEmptyEntityForServicesFlag;

  @Context
  private Provider<MessageBodyWorkers> provider;

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
                if ( method.getName().equals( GET_HEADERS ) && args.length > 0 && args[ 0 ].equals( ACCEPT ) ) {
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

  @Override
  public void init() throws ServletException {
    super.init();
    if ( logger.isDebugEnabled() ) {
      MessageBodyWorkers messageBodyWorkers = provider.get();
      Map<MediaType, List<MessageBodyWriter>> writers = messageBodyWorkers == null ? null
        : messageBodyWorkers.getWriters( MediaType.WILDCARD_TYPE );
      logger.debug( "Writers: " + writers ); //$NON-NLS-1$
    }
  }

  @VisibleForTesting
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
    wac.addBeanFactoryPostProcessor( new PentahoBeanScopeValidatorPostProcessor() );
    wac.refresh();

    return wac;
  }
}
