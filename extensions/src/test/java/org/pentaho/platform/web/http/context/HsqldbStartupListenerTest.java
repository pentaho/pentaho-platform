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
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

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

  @Test
  public void testContextInitialized_withInMemoryDatabases() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata,hibernate@mem:hibernate,quartz@mem:quartz" );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( null );

    // Don't start the actual server, just verify config is set
    startupListener.contextInitialized( contextEvent );
    // Server startup will fail in unit tests since we're not actually running HSQLDB
    // but the listener should still attempt to configure it
  }

  @Test
  public void testGetDatabaseConfiguration() throws Exception {
    // Test parsing of database configuration string from web.xml
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata,hibernate@mem:hibernate,quartz@mem:quartz" );

    startupListener.contextInitialized( contextEvent );
    // If no exception, parsing was successful
  }

  @Test
  public void testScriptLoadingWithNullPath() throws Exception {
    // Test graceful handling when hsqldb-init-script is null
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata" );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( null );

    // Should not throw exception when script path is null
    startupListener.contextInitialized( contextEvent );
  }

  private void writeScriptFile( File file, String content ) throws IOException {
    file.getParentFile().mkdirs();
    try ( FileWriter writer = new FileWriter( file ) ) {
      writer.write( content );
    }
  }
}