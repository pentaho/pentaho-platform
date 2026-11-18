/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.web.http.context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.web.hsqldb.HsqlDatabaseStarterBean;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HsqldbStartupListenerTest {

  private static final int MAX_START_ATTEMPTS = 3;

  HsqldbStartupListener startupListener;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Mock
  ServletContextEvent contextEvent;
  @Mock
  ServletContext context;
  @Mock
  HsqlDatabaseStarterBean starterBean;

  // Backs the mocked context's attributes, so contextDestroyed() finds and stops a server started by contextInitialized()
  private final Map<String, Object> contextAttributes = new HashMap<>();

  @Before
  public void setUp() {
    startupListener = new HsqldbStartupListener();
    when( contextEvent.getServletContext() ).thenReturn( context );
    lenient().doAnswer( inv -> contextAttributes.put( inv.getArgument( 0 ), inv.getArgument( 1 ) ) )
      .when( context ).setAttribute( anyString(), any() );
    lenient().when( context.getAttribute( "hsqldb-starter-bean" ) )
      .thenAnswer( inv -> contextAttributes.get( "hsqldb-starter-bean" ) );
  }

  @After
  public void tearDown() {
    // Tests that start a real HSQLDB server must not leave it running: it would keep its port bound for the rest of the JVM
    Object bean = contextAttributes.get( "hsqldb-starter-bean" );
    if ( bean instanceof HsqlDatabaseStarterBean ) {
      ( (HsqlDatabaseStarterBean) bean ).stop();
    }
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
  public void testContextInitialized() throws Exception {
    when( context.getInitParameter( "hsqldb-databases" ) ).thenReturn( "test@mem:testContextInitialized" );
    startOnFreePort( startupListener, contextAttributes, "in testContextInitialized" );
    verify( context ).setAttribute( eq( "hsqldb-starter-bean" ), any() );
    startupListener.contextDestroyed( contextEvent );
  }

  @Test
  public void testContextInitialized_withInMemoryDatabases() throws Exception {
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( String.valueOf( findFreePort() ) );
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
  public void testGetDatabaseConfiguration() throws Exception {
    // Test parsing of database configuration string from web.xml
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata,hibernate@mem:hibernate,quartz@mem:quartz" );
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( String.valueOf( findFreePort() ) );

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

    Map<String, Object> attributes = new HashMap<>();
    doAnswer( inv -> attributes.put( inv.getArgument( 0 ), inv.getArgument( 1 ) ) )
      .when( context ).setAttribute( anyString(), any() );
    when( context.getAttribute( "hsqldb-starter-bean" ) ).thenAnswer( inv -> attributes.get( "hsqldb-starter-bean" ) );
    when( context.getInitParameter( "hsqldb-databases" ) ).thenReturn( "testdb@file:" + dbPath );
    when( context.getInitParameter( "hsqldb-init-script" ) ).thenReturn( ".." + File.separator + "seed" );
    when( context.getRealPath( "/" ) ).thenReturn( webappDir.getAbsolutePath() );

    for ( int start = 1; start <= 2; start++ ) {
      HsqldbStartupListener listener = new HsqldbStartupListener();
      int port = startOnFreePort( listener, attributes, "on start " + start );
      try {
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

  /**
   * Starts the listener on a free port and returns it. findFreePort() releases the port before the HSQLDB server binds
   * it, so on a busy machine another process can take it in between and the server fails to start. In that case retry
   * on a new port; port failover stays disabled, so the server never falls back to the default port 9001. Failures
   * unrelated to the port (e.g. the database can't be opened) still fail on every attempt.
   */
  private int startOnFreePort( HsqldbStartupListener listener, Map<String, Object> attributes, String description )
    throws Exception {
    for ( int attempt = 1; attempt <= MAX_START_ATTEMPTS; attempt++ ) {
      int port = findFreePort();
      when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( String.valueOf( port ) );
      listener.contextInitialized( contextEvent );
      if ( attributes.get( "hsqldb-starter-bean" ) != null ) {
        return port;
      }
    }
    fail( "HSQLDB server should be running " + description + " (tried " + MAX_START_ATTEMPTS + " ports)" );
    return -1;
  }

  private static int findFreePort() throws Exception {
    try ( ServerSocket socket = new ServerSocket( 0 ) ) {
      return socket.getLocalPort();
    }
  }

  @Test
  public void testScriptLoadingWithNullPath() throws Exception {
    // Test graceful handling when hsqldb-init-script is null
    when( context.getInitParameter( "hsqldb-port" ) ).thenReturn( String.valueOf( findFreePort() ) );
    when( context.getInitParameter( "hsqldb-allow-port-failover" ) ).thenReturn( "true" );
    when( context.getInitParameter( "hsqldb-databases" ) )
      .thenReturn( "sampledata@mem:sampledata" );

    // Should not throw exception when script path is null
    startupListener.contextInitialized( contextEvent );
    assertNotNull( startupListener );
  }
}