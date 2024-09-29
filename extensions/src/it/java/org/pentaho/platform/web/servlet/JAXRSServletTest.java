/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License, version 2 as published by the Free Software Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, you can obtain
 * a copy at http://www.gnu.org/licenses/gpl-2.0.html or from the Free Software Foundation, Inc.,  51 Franklin
 * Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * Copyright 2006 - 2021 Hitachi Vantara.  All rights reserved.
 *
 */

package org.pentaho.platform.web.servlet;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.spi.MessageBodyWorkers;
import com.sun.jersey.spi.container.WebApplication;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.spi.container.servlet.WebServletConfig;
import com.sun.jersey.spi.spring.container.servlet.SpringServlet;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.pentaho.platform.api.util.LogUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by Dmitriy Stepanov on 28.03.18.
 */
@RunWith( PowerMockRunner.class )
@PowerMockIgnore( "jdk.internal.reflect.*" )
@PrepareForTest( { SpringServlet.class, ServletContainer.class, JAXRSServlet.class } )
public class JAXRSServletTest {

  protected static final String URL = "/url";

  class JAXRSServletT extends JAXRSServlet {
    @Override protected void callSuperInitiate( ResourceConfig rc, WebApplication wa ) {
      if ( callSuperIterate.get() ) {
        super.callSuperInitiate( rc, wa );
      }
    }

    protected void setBackWardSendErrorCompatible( Boolean flag ) {
      SendEmptyEntityForServicesFlag = !flag;
    }
  }

  private AtomicBoolean callSuperIterate = new AtomicBoolean( false );
  private JAXRSServletT jaxrsServlet;

  @Before
  public void setUp() throws Exception {
    callSuperIterate.set( false );
    jaxrsServlet = PowerMockito.spy( new JAXRSServletT() );
  }

  @Test
  public void getContextTest() {
    Mockito.doReturn( null ).when( jaxrsServlet ).getAppContext();
    jaxrsServlet.getContext();
    verify( jaxrsServlet ).getAppContext();
  }

  @Test
  public void serviceTest() {
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    when( request.getMethod() ).thenReturn( "POST" );
    try {
      Mockito.doNothing().when( (SpringServlet) jaxrsServlet ).service( request, response );
      jaxrsServlet.service( request, response );
      verify( (SpringServlet) jaxrsServlet ).service( eq( request ), eq( response ) );

      when( request.getMethod() ).thenReturn( JAXRSServlet.GET );

      String MIME_TYPE = "MIME_TYPE";
      String NOT_MIME_TYPE = "NOT_MIME_TYPE";

      when( request.getHeader( JAXRSServlet.ACCEPT ) ).thenReturn( NOT_MIME_TYPE );
      when( request.getParameter( JAXRSServlet.MIME_TYPE ) ).thenReturn( null );
      final AtomicBoolean fail = new AtomicBoolean( false );
      checkMime( request, response, MIME_TYPE, fail );
      assertFalse( "Mime type was incorrectly changed", fail.get() );


      when( request.getParameter( JAXRSServlet.MIME_TYPE ) ).thenReturn( JAXRSServlet.MIME_TYPE );
      checkMime( request, response, NOT_MIME_TYPE, fail );
      assertFalse( "Mime type was incorrectly changed", fail.get() );
    } catch ( ServletException e ) {
      e.printStackTrace();
      fail( "ServletException appeared" );
    } catch ( IOException e ) {
      e.printStackTrace();
      fail( "IOException appeared" );
    }
  }

  @Path( "url" )
  public static class ResourseClass {

    AtomicBoolean b = new AtomicBoolean( true );

    @POST
    public void postSuccessThenFail() {
      if ( b.get() ) {
        b.set( false );
      } else {
        b.set( true );
        throw new RuntimeException( "failed" );
      }

    }
  }


  @Test
  public void serviceTestWriter() throws ServletException {
    callSuperIterate.set( true );
    HttpServletRequest request = mock( HttpServletRequest.class );
    HttpServletResponse response = mock( HttpServletResponse.class );
    ResourceConfig resourceConfig = mock( ResourceConfig.class );
    String contextPath = "/context";
    String servletPath = "/servlet";
    StringBuffer requestUrl = new StringBuffer( contextPath + servletPath + URL );
    ServletConfig servletConfig = mock( ServletConfig.class );
    WebServletConfig webServletConfig = mock( WebServletConfig.class );
    ServletContext servletContext = mock( ServletContext.class );
    ConfigurableApplicationContext context = mock( ConfigurableApplicationContext.class );
    String[] beans = {};
    HashSet<Class> classes = new HashSet<>();
    classes.add( ResourseClass.class );

    when( request.getMethod() ).thenReturn( "POST" );
    when( request.getRequestURL() ).thenReturn( requestUrl );
    when( request.getRequestURI() ).thenReturn( URL );
    when( request.getContextPath() ).thenReturn( contextPath );
    when( request.getServletPath() ).thenReturn( servletPath );
    doReturn( webServletConfig ).when( jaxrsServlet ).createWebConfig( jaxrsServlet );
    doReturn( servletConfig ).when( jaxrsServlet ).getServletConfig();
    doReturn( context ).when( jaxrsServlet ).getContext();
    doReturn( beans ).when( context ).getBeanNamesForType( (Class<?>) any() );
    doReturn( Collections.emptyEnumeration() ).when( webServletConfig ).getInitParameterNames();
    doReturn( null ).when( servletContext ).getAttribute( "javax.enterprise.inject.spi.BeanManager" );
    doReturn( servletContext ).when( webServletConfig ).getServletContext();
    doReturn( resourceConfig ).when( webServletConfig ).getDefaultResourceConfig( any() );
    doReturn( classes ).when( resourceConfig ).getRootResourceClasses();
    doReturn( context ).when( jaxrsServlet ).getAppContext();
    doReturn( Collections.emptyEnumeration() ).when( request ).getHeaderNames();
    try {
      jaxrsServlet.init();
      jaxrsServlet.service( request, response );
      verify( (SpringServlet) jaxrsServlet ).service( eq( request ), eq( response ) );
      verify( response ).setStatus( 404, "Not Found" );

      jaxrsServlet.setBackWardSendErrorCompatible( true );
      jaxrsServlet.service( request, response );
      verify( response, never() ).setStatus( 404 );
      verify( response, never() ).sendError( 404 );

    } catch ( ServletException e ) {
      e.printStackTrace();
      fail( "ServletException appeared" );
    } catch ( IOException e ) {
      e.printStackTrace();
      fail( "IOException appeared" );
    }
  }

  private void checkMime( HttpServletRequest request, HttpServletResponse response, String NOT_MIME_TYPE,
                          AtomicBoolean fail ) throws ServletException, IOException {
    doAnswer(
      (Answer<Object>) invocation -> {
        Object[] arguments = invocation.getArguments();
        HttpServletRequest req = (HttpServletRequest) arguments[ 2 ];
        if ( req.getHeader( JAXRSServlet.ACCEPT ).equals( NOT_MIME_TYPE ) ) {
          fail.set( true );
        }
        return null;
      } ).when( (SpringServlet) jaxrsServlet ).service( eq( request ), eq( response ) );
  }

  @Test
  public void initiateTest() throws Exception {
    ResourceConfig rc = mock( ResourceConfig.class );
    HashMap<String, Boolean> features = new HashMap<>();
    when( rc.getFeatures() ).thenReturn( features );
    WebApplication wa = mock( WebApplication.class );
    when( jaxrsServlet.getServletConfig() ).thenReturn( mock( ServletConfig.class ) );
    MessageBodyWorkers messageBodyWorkers = mock( MessageBodyWorkers.class );
    doReturn( messageBodyWorkers ).when( wa ).getMessageBodyWorkers();
    doReturn( null ).when( messageBodyWorkers ).getWriters( MediaType.WILDCARD_TYPE );
    doReturn( mock( ConfigurableApplicationContext.class ) ).when( jaxrsServlet ).getAppContext();
    PowerMockito.doNothing().when( jaxrsServlet, "callSuperInitiate", any(), any() );
    setDebugLogLevel();
    jaxrsServlet.initiate( rc, wa );
    verify( wa ).getMessageBodyWorkers();
    Boolean condition = features.get( ResourceConfig.FEATURE_TRACE );
    if ( condition == null ) {
      condition = false;
    }
    assertTrue( condition );
    condition = features.get( ResourceConfig.FEATURE_TRACE_PER_REQUEST );
    if ( condition == null ) {
      condition = false;
    }
    assertTrue( condition );
  }

  private void setDebugLogLevel() {
    // Try java.util.logging as backend
    java.util.logging.Logger.getLogger( JAXRSServlet.class.getName() ).setLevel( Level.ALL );

    // Try Log4J as backend
    LogUtil.setLevel(LogManager.getLogger(JAXRSServlet.class), org.apache.logging.log4j.Level.DEBUG);
  }

  @Test
  public void initTest() throws Exception {
    ServletConfig servletConfig = mock( ServletConfig.class );
    WebServletConfig webServletConfig = mock( WebServletConfig.class );
    ServletContext servletContext = mock( ServletContext.class );
    doReturn( webServletConfig ).when( jaxrsServlet ).createWebConfig( jaxrsServlet );
    doReturn( servletConfig ).when( jaxrsServlet ).getServletConfig();
    doReturn( Collections.emptyEnumeration() ).when( webServletConfig ).getInitParameterNames();
    doReturn( null ).when( servletContext ).getAttribute( "javax.enterprise.inject.spi.BeanManager" );
    doReturn( servletContext ).when( webServletConfig ).getServletContext();
    doReturn( mock( ConfigurableApplicationContext.class ) ).when( jaxrsServlet ).getAppContext();

    try {
      jaxrsServlet.init();
    } catch ( ServletException e ) {
      e.printStackTrace();
      fail( "ServletException appeared" );
    }
    Field web = ServletContainer.class.getDeclaredField( "webComponent" );
    web.setAccessible( true );
    Object obj = web.get( jaxrsServlet );
    assertTrue( obj.getClass().getCanonicalName().contains( this.getClass().getPackage().getName() ) );
    assertFalse( obj.getClass().getCanonicalName().contains( ServletContainer.class.getPackage().getName() ) );
  }
}
