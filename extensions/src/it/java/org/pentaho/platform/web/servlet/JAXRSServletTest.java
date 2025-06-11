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

import org.glassfish.jersey.servlet.WebServletConfig;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.context.ConfigurableApplicationContext;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import org.glassfish.jersey.servlet.ServletContainer;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by Dmitriy Stepanov on 28.03.18.
 */
public class JAXRSServletTest {

  protected static final String URL = "/url";

  class JAXRSServletT extends JAXRSServlet {
    protected WebServletConfig createWebConfig( ServletContainer container ) {
      return new WebServletConfig( container );
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
    jaxrsServlet = Mockito.spy( new JAXRSServletT() );
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
      Mockito.doNothing().when( jaxrsServlet ).service( request, response );
      jaxrsServlet.service( request, response );
      verify( jaxrsServlet ).service( eq( request ), eq( response ) );

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
    doReturn( classes ).when( resourceConfig ).getClasses();
    doReturn( context ).when( jaxrsServlet ).getAppContext();
    doReturn( Collections.emptyEnumeration() ).when( request ).getHeaderNames();
    try {
      jaxrsServlet.init();
      jaxrsServlet.service( request, response );
      verify( jaxrsServlet ).service( eq( request ), eq( response ) );
      verify( response ).setStatus( 404 );

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
      } ).when( jaxrsServlet ).service( eq( request ), eq( response ) );
  }
}
