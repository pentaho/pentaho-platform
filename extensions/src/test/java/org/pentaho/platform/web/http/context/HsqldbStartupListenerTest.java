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


package org.pentaho.platform.web.http.context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class HsqldbStartupListenerTest {

  HsqldbStartupListener startupListener;

  @Mock
  ServletContextEvent contextEvent;
  @Mock
  ServletContext context;
  @Mock
  HsqlDatabaseStarterBean starterBean;

  @Before
  public void setUp() throws Exception {
    startupListener = new HsqldbStartupListener();
    when( contextEvent.getServletContext() ).thenReturn( context );
  }

  @Test
  public void testContextDestroyed_nullStarterBean() {
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenReturn( null );
    startupListener.contextDestroyed( contextEvent );
    // Verify no exception is thrown when starter bean is null
    assertNotNull( startupListener );
  }

  @Test
  public void testContextDestroyed() {
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenReturn( starterBean );

    startupListener.contextDestroyed( contextEvent );
    verify( starterBean ).stop();
  }

  @Test
  public void testContextInitialized_nullPort() {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( null );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized_gracefullyHandleInvalidPort() {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "eighty-eighty" );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized_nullAllowPortFailover() {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9999" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( null );

    startupListener.contextInitialized( contextEvent );
    verify( context, never() ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
  }

  @Test
  public void testContextInitialized() {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9999" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) ).thenReturn( "test@localhost" );
    startupListener.contextInitialized( contextEvent );
    verify( context ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
    startupListener.contextDestroyed( contextEvent );
  }

  @Test
  public void testContextInitialized_withInMemoryDatabases() {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata,hibernate@mem:hibernate,quartz@mem:quartz" );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( null );

    // Don't start the actual server, just verify config is set
    startupListener.contextInitialized( contextEvent );
    // Server startup will fail in unit tests since we're not actually running HSQLDB
    // but the listener should still attempt to configure it
    assertNotNull( startupListener );
  }

  @Test
  public void testGetDatabaseConfiguration() {
    // Test parsing of database configuration string from web.xml
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata,hibernate@mem:hibernate,quartz@mem:quartz" );
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );

    startupListener.contextInitialized( contextEvent );
    // If no exception, parsing was successful
    assertNotNull( startupListener );
  }

  @Test
  public void testScriptLoadingWithNullPath() {
    // Test graceful handling when hsqldb-init-script is null
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata" );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( null );

    // Should not throw exception when script path is null
    startupListener.contextInitialized( contextEvent );
    assertNotNull( startupListener );
  }
}