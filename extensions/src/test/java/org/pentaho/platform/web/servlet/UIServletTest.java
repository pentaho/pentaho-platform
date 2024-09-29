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
 * Copyright (c) 2021 Hitachi Vantara.  All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
