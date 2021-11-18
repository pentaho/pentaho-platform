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

package org.pentaho.platform.web.http.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class HsqldbStartupListenerTest {

  HsqldbStartupListener startupListener;

  @Mock ServletContextEvent contextEvent;
  @Mock ServletContext context;
  @Mock HsqlDatabaseStarterBean starterBean;

  @Before
  public void setUp() throws Exception {
    startupListener = new HsqldbStartupListener();
    when( contextEvent.getServletContext() ).thenReturn( context );
  }

  @Test
  public void testContextDestroyed_nullStarterBean() throws Exception {
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenReturn( null );
    startupListener.contextDestroyed( contextEvent );
  }

  @Test
  public void testContextDestroyed() throws Exception {
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenReturn( starterBean );

    startupListener.contextDestroyed( contextEvent );
    verify( starterBean ).stop();
  }

  @Test
  public void testContextInitialized_nullPort() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( null );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized_gracefullyHandleInvalidPort() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "eighty-eighty" );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized_nullAllowPortFailover() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9999" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( null );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9999" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) ).thenReturn( "test@localhost" );
    startupListener.contextInitialized( contextEvent );
    verify( context ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
    startupListener.contextDestroyed( contextEvent );
  }
}
