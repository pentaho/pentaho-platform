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

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doReturn;

@RunWith( MockitoJUnitRunner.class )
public class UIServletTest {

  private UIServlet uiServlet;
  @Mock HttpServletRequest request;
  @Mock HttpServletResponse response;
  @Mock ServletOutputStream responseOutputStream;
  @Mock HttpSession session;
  @Mock Log logger;

  @Before
  public void setUp() throws Exception {
    uiServlet = spy( new UIServlet() {
      @Override
      public Log getLogger() {
        return logger;
      }
    } );
    doNothing().when( logger ).fatal( nullable( String.class ), any() );
    doNothing().when( uiServlet ).formatErrorMessage( nullable( IPentahoSession.class ), nullable( StringBuffer.class ), nullable( String.class ) );
  }

  @Test
  public void testDoGet() throws Exception {
    doReturn( responseOutputStream ).when( response ).getOutputStream();
    doReturn( session ).when( request ).getSession();

    doReturn( "application/json" ).when( request ).getParameter( "type" );
    doReturn( "/><system.alert(\"hi!\")/><" ).doReturn( "UIServlet" ).when( request ).getParameter( "component" );
    uiServlet.doGet( request, response );
    verify( response ).setContentType( "text/html" );
    verify( uiServlet ).getComponent( "/&gt;&lt;system.alert(&quot;hi!&quot;)/&gt;&lt;" );

    uiServlet.doGet( request, response );
    verify( uiServlet ).getComponent( "UIServlet" );
  }
}
