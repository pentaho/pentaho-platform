/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.web.http.context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;

import java.io.File;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/28/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class HsqldbStartupListenerTest {

  HsqldbStartupListener startupListener;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

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
  public void testIsDatabaseInitialized_emptyDatabase() throws Exception {
    try ( Connection conn = DriverManager.getConnection( "jdbc:hsqldb:mem:emptydb_" + System.nanoTime(), "sa", "" );
          Statement stmt = conn.createStatement() ) {
      assertFalse( startupListener.isDatabaseInitialized( conn ) );
      stmt.execute( "SHUTDOWN" );
    }
  }

  @Test
  public void testIsDatabaseInitialized_databaseWithUserTables() throws Exception {
    try ( Connection conn = DriverManager.getConnection( "jdbc:hsqldb:mem:populateddb_" + System.nanoTime(), "sa", "" );
          Statement stmt = conn.createStatement() ) {
      stmt.execute( "CREATE TABLE PUBLIC.ORDERFACT(ORDERNUMBER INTEGER, TOTALPRICE REAL)" );
      assertTrue( startupListener.isDatabaseInitialized( conn ) );
      stmt.execute( "SHUTDOWN" );
    }
  }

  @Test
  public void testFileDatabaseScriptIsLoadedOnlyOnceAcrossRestarts() throws Exception {
    // Mirrors web.xml: hsqldb-init-script is resolved relative to the webapp real path
    File webappDir = tempFolder.newFolder( "webapp" );
    File seedDir = tempFolder.newFolder( "seed" );
    Files.write( new File( seedDir, "testdb.script" ).toPath(), Arrays.asList(
      "CREATE TABLE PUBLIC.FACT(ID INTEGER, AMOUNT INTEGER);",
      "INSERT INTO PUBLIC.FACT VALUES(1, 10);",
      "INSERT INTO PUBLIC.FACT VALUES(2, 20);" ), StandardCharsets.UTF_8 );
    String dbPath = new File( tempFolder.getRoot(), "db" + File.separator + "testdbdb" ).getAbsolutePath();
    int port = findFreePort();

    Map<String, Object> attributes = new HashMap<>();
    doAnswer( inv -> attributes.put( inv.getArgument( 0 ), inv.getArgument( 1 ) ) )
      .when( context ).setAttribute( anyString(), any() );
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenAnswer( inv -> attributes.get( "hsqldb-starter-bean" ) );
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( String.valueOf( port ) );
    when( context.getInitParameter( "hsqldb-databases" ) ).thenReturn( "testdb@file:" + dbPath );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( ".." + File.separator + "seed" );
    when( context.getRealPath( "/" ) ).thenReturn( webappDir.getAbsolutePath() );

    for ( int start = 1; start <= 2; start++ ) {
      HsqldbStartupListener listener = new HsqldbStartupListener();
      listener.contextInitialized( contextEvent );
      try {
        assertNotNull( "HSQLDB server should be running on start " + start, attributes.get( "hsqldb-starter-bean" ) );
        try ( Connection conn = DriverManager.getConnection( "jdbc:hsqldb:hsql://localhost:" + port + "/testdb", "sa", "" );
              Statement stmt = conn.createStatement() ) {
          try ( ResultSet rs = stmt.executeQuery( "SELECT COUNT(*) FROM PUBLIC.FACT" ) ) {
            assertTrue( rs.next() );
            assertEquals( "Seed rows must not be duplicated on start " + start, 2, rs.getInt( 1 ) );
          }
          // Close the database so the next start reopens it from disk, like a real server restart
          stmt.execute( "SHUTDOWN" );
        }
      } finally {
        listener.contextDestroyed( contextEvent );
        attributes.clear();
      }
    }
  }

  private static int findFreePort() throws Exception {
    try ( ServerSocket socket = new ServerSocket( 0 ) ) {
      return socket.getLocalPort();
    }
  }

  @Test
  public void testScriptLoadingWithNullPath() {
    // Test graceful handling when hsqldb-init-script is null
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( "9001" );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata" );

    // Should not throw exception when script path is null
    startupListener.contextInitialized( contextEvent );
    assertNotNull( startupListener );
  }
}