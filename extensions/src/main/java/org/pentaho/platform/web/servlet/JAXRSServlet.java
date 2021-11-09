/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.sun.jersey.api.container.ContainerException;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.ThreadLocalInvoker;
import com.sun.jersey.server.probes.UriRuleProbeProvider;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseWriter;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebComponent;
import com.sun.jersey.spi.container.servlet.WebConfig;
import com.sun.jersey.spi.container.servlet.WebServletConfig;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.objfac.spring.PentahoBeanScopeValidatorPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.security.Principal;
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
  public static final String SET_STATUS = "setStatus";
  public static final String ACCEPT = "accept";
  public static final String GET = "GET";
  protected boolean SendEmptyEntityForServicesFlag;

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

  @SuppressWarnings( "unchecked" )
  @Override
  protected void initiate( ResourceConfig rc, WebApplication wa ) {
    if ( logger.isDebugEnabled() ) {
      rc.getFeatures().put( ResourceConfig.FEATURE_TRACE, true );
      rc.getFeatures().put( ResourceConfig.FEATURE_TRACE_PER_REQUEST, true );
    }
    callSuperInitiate( rc, wa );
    if ( logger.isDebugEnabled() ) {
      MessageBodyWorkers messageBodyWorkers = wa.getMessageBodyWorkers();
      Map<MediaType, List<MessageBodyWriter>> writers = messageBodyWorkers == null ? null
        : messageBodyWorkers.getWriters( MediaType.WILDCARD_TYPE );
      logger.debug( "Writers: " + writers ); //$NON-NLS-1$
    }
  }

  @VisibleForTesting
  protected void callSuperInitiate( ResourceConfig rc, WebApplication wa ) {
    super.initiate( rc, wa );
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

  @Override public void init() throws ServletException {
    SendEmptyEntityForServicesFlag =
      Boolean.parseBoolean( PentahoSystem.getSystemSetting( "pentaho.xml", "set-empty-entity-rest-services", "true" ) );
    Application app = (Application) getPrivate( "app", ServletContainer.class, this );
    WebComponent component;
    if ( app == null ) {
      component = new InternalWebComponent();
    } else {
      component = new InternalWebComponent( app );
    }
    setPrivate( "webComponent", ServletContainer.class, this, component, false );
    WebServletConfig webConfig = createWebConfig( this );
    component.init( webConfig );

  }

  @VisibleForTesting
  protected WebServletConfig createWebConfig( ServletContainer container ) {
    return new WebServletConfig( container );
  }

  class InternalWebComponent extends WebComponent {

    InternalWebComponent() {
    }

    InternalWebComponent( Application app ) {
      super( app );
    }

    @Override
    protected WebApplication create() {
      return JAXRSServlet.this.create();
    }

    @Override
    protected void configure( WebConfig wc, ResourceConfig rc, WebApplication wa ) {
      super.configure( wc, rc, wa );

      JAXRSServlet.this.configure( wc, rc, wa );
    }

    @Override
    protected void initiate( ResourceConfig rc, WebApplication wa ) {
      JAXRSServlet.this.initiate( rc, wa );
    }

    @Override
    protected ResourceConfig getDefaultResourceConfig( Map<String, Object> props,
                                                       WebConfig wc ) throws ServletException {
      return JAXRSServlet.this.getDefaultResourceConfig( props, wc );
    }

    @Override
    public int service( URI baseUri, URI requestUri, HttpServletRequest request, HttpServletResponse response )
      throws ServletException, IOException {
      // Copy the application field to local instance to ensure that the
      // currently loaded web application is used to process
      // request
      final WebApplication _application = (WebApplication) getPrivate( "application", WebComponent.class, this );

      final ContainerRequest cRequest = createRequest(
        _application,
        request,
        baseUri,
        requestUri );

      cRequest.setSecurityContext( new SecurityContext() {
        public Principal getUserPrincipal() {
          return request.getUserPrincipal();
        }

        public boolean isUserInRole( String role ) {
          return request.isUserInRole( role );
        }

        public boolean isSecure() {
          return request.isSecure();
        }

        public String getAuthenticationScheme() {
          return request.getAuthType();
        }
      } );

      try {
        // Check if any servlet filters have consumed a request entity
        // of the media type application/x-www-form-urlencoded
        // This can happen if a filter calls request.getParameter(...)
        runFilterFormParameters( request, cRequest );

        UriRuleProbeProvider.requestStart( requestUri );

        runRequestInvokerSet( request );
        runResponseInvokerSet( response );

        final BackwardCompatibleWriter w = new BackwardCompatibleWriter( response );
        _application.handleRequest( cRequest, w );
        return w.cResponse.getStatus();
      } catch ( WebApplicationException ex ) {
        final Response exResponse = ex.getResponse();
        final String entity = exResponse.getEntity() != null ? exResponse.getEntity().toString() : null;

        response.sendError( exResponse.getStatus(), entity );

        return exResponse.getStatus();
      } catch ( AccessDeniedException ex ) {
        response.sendError( Response.Status.FORBIDDEN.getStatusCode() );
        return Response.Status.FORBIDDEN.getStatusCode();
      } catch ( AuthenticationException ex ) {
        response.sendError( Response.Status.FORBIDDEN.getStatusCode() );
        return Response.Status.FORBIDDEN.getStatusCode();
      } catch ( MappableContainerException ex ) {
        runTraceOnException( cRequest, response );
        throw new ServletException( ex.getCause() );
      } catch ( ContainerException ex ) {
        runTraceOnException( cRequest, response );
        throw new ServletException( ex );
      } catch ( RuntimeException ex ) {
        runTraceOnException( cRequest, response );
        throw ex;
      } finally {
        UriRuleProbeProvider.requestEnd();
        runRequestInvokerSet( null );
        runResponseInvokerSet( null );
      }
    }

    private void runTraceOnException( ContainerRequest cRequest, HttpServletResponse response )
      throws ServletException {
      runPrivate( "traceOnException", WebComponent.class,
        new Class[] { ContainerRequest.class, HttpServletResponse.class }, this, cRequest, response );
    }

    private void runFilterFormParameters( HttpServletRequest request, ContainerRequest cRequest )
      throws ServletException {
      runPrivate( "filterFormParameters", WebComponent.class,
        new Class[] { HttpServletRequest.class, ContainerRequest.class }, this, request, cRequest );
    }

    private void runRequestInvokerSet( HttpServletRequest request )
      throws ServletException {
      ( (ThreadLocalInvoker<HttpServletRequest>) getPrivate( "requestInvoker", WebComponent.class, this ) )
        .set( request );
    }

    private void runResponseInvokerSet( HttpServletResponse response )
      throws ServletException {
      ( (ThreadLocalInvoker<HttpServletResponse>) getPrivate( "responseInvoker", WebComponent.class, this ) )
        .set( response );
    }
  }

  private static Object getPrivate( String nameField, Class classOfField, Object fromObj ) throws ServletException {
    Field field = null;
    try {
      field = classOfField.getDeclaredField( nameField );
      field.setAccessible( true );
      return field.get( fromObj );
    } catch ( Exception e ) {
      throw new ServletException( e );
    }
  }

  private static void setPrivate( String nameField, Class classOfField, Object fromObj, Object value,
                                  boolean finalFlag )
    throws ServletException {
    Field field = null;
    try {
      field = classOfField.getDeclaredField( nameField );
      field.setAccessible( true );
      if ( finalFlag ) {
        Field modifiersField = Field.class.getDeclaredField( "modifiers" );
        modifiersField.setAccessible( true );
        modifiersField.setInt( field, field.getModifiers() & ~Modifier.FINAL );
      }
      field.set( fromObj, value );
    } catch ( Exception e ) {
      throw new ServletException( e );
    }
  }

  private static void runPrivate( String methodName, Class classOfField, Class<?>[] parameterTypes, Object fromObj,
                                  Object... args )
    throws ServletException {
    try {
      Method m = classOfField.getDeclaredMethod( methodName, parameterTypes );
      m.setAccessible( true );
      m.invoke( fromObj, args );
    } catch ( Exception e ) {
      throw new ServletException( e );
    }
  }


  private class BackwardCompatibleWriter extends OutputStream implements ContainerResponseWriter {
    final HttpServletResponse response;

    ContainerResponse cResponse;

    long contentLength;

    OutputStream out;

    boolean statusAndHeadersWritten = false;

    BackwardCompatibleWriter( HttpServletResponse response ) {
      this.response = response;
    }

    public OutputStream writeStatusAndHeaders( long contentLength,
                                               ContainerResponse cResponse ) throws IOException {
      this.contentLength = contentLength;
      this.cResponse = cResponse;
      this.statusAndHeadersWritten = false;
      return this;
    }

    public void finish() throws IOException {
      if ( statusAndHeadersWritten ) {
        return;
      }

      // Note that the writing of headers MUST be performed before
      // the invocation of sendError as on some Servlet implementations
      // modification of the response headers will have no effect
      // after the invocation of sendError.
      writeHeaders();


      writeStatus();
    }

    private void writeStatus() throws IOException {
      Response.StatusType statusType = cResponse.getStatusType();
      final String reasonPhrase = statusType.getReasonPhrase();

      if ( !SendEmptyEntityForServicesFlag && ( statusType.getFamily().equals( Response.Status.Family.CLIENT_ERROR )
        || statusType.getFamily().equals( Response.Status.Family.SERVER_ERROR ) ) ) {
        if ( reasonPhrase == null || reasonPhrase.isEmpty() ) {
          response.sendError( cResponse.getStatus() );
        } else {
          response.sendError( cResponse.getStatus(), reasonPhrase );
        }
      } else {
        if ( reasonPhrase != null ) {
          response.setStatus( statusType.getStatusCode(), reasonPhrase );
        } else {
          response.setStatus( statusType.getStatusCode() );
        }
      }
    }


    public void write( int b ) throws IOException {
      initiate();
      out.write( b );
    }

    @Override
    public void write( byte[] b ) throws IOException {
      if ( b.length > 0 ) {
        initiate();
        out.write( b );
      }
    }

    @Override
    public void write( byte[] b, int off, int len ) throws IOException {
      if ( len > 0 ) {
        initiate();
        out.write( b, off, len );
      }
    }

    @Override
    public void flush() throws IOException {
      writeStatusAndHeaders();
      if ( out != null ) {
        out.flush();
      }
    }

    @Override
    public void close() throws IOException {
      initiate();
      out.close();
    }

    void initiate() throws IOException {
      if ( out == null ) {
        writeStatusAndHeaders();
        out = response.getOutputStream();
      }
    }

    void writeStatusAndHeaders() throws IOException {
      if ( statusAndHeadersWritten ) {
        return;
      }

      writeHeaders();
      writeStatus();
      statusAndHeadersWritten = true;
    }

    void writeHeaders() {
      if ( contentLength != -1 && contentLength < Integer.MAX_VALUE ) {
        response.setContentLength( (int) contentLength );
      }

      MultivaluedMap<String, Object> headers = cResponse.getHttpHeaders();
      for ( Map.Entry<String, List<Object>> e : headers.entrySet() ) {
        for ( Object v : e.getValue() ) {
          response.addHeader( e.getKey(), ContainerResponse.getHeaderValue( v ) );
        }
      }
    }
  }
}
