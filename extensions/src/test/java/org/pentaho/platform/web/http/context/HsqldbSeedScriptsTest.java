/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.web.http.context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that the HSQLDB seed scripts shipped in assemblies/pentaho-data load into an empty database, using the
 * same loader as {@link HsqldbStartupListener}, without unexpected failures. The listener only loads a seed into an
 * empty database once, so any statement that fails on that first load leaves the sample data permanently incomplete.
 */
@RunWith( Parameterized.class )
public class HsqldbSeedScriptsTest {

  private static final File SEED_DIR = new File( "../assemblies/pentaho-data/src/main/resources/data/hsqldb" );
  private static final Pattern INSERT_TABLE = Pattern.compile( "^INSERT INTO ((?:\\w+\\.)?\\w+)\\s*VALUES", Pattern.CASE_INSENSITIVE );

  // Harmless failures: the database already has SA and PUBLIC, and SCALE is not settable on HSQLDB 2.7.4
  private static final List<String> SAMPLEDATA_EXPECTED_FAILURES = Arrays.asList(
    "SET FILES SCALE 1;",
    "CREATE USER SA PASSWORD '';",
    "CREATE SCHEMA PUBLIC AUTHORIZATION DBA;" );

  @Parameter( 0 )
  public String dbName;

  @Parameter( 1 )
  public List<String> expectedFailures;

  @Parameters( name = "{0}.script" )
  public static Collection<Object[]> seeds() {
    return Arrays.asList( new Object[][] {
      { "sampledata", SAMPLEDATA_EXPECTED_FAILURES },
      { "hibernate", Collections.emptyList() },
      { "quartz", Collections.emptyList() },
      { "jackrabbit", Collections.emptyList() } } );
  }

  @Test
  public void testSeedLoadsIntoEmptyDatabaseWithoutUnexpectedFailures() throws Exception {
    File script = new File( SEED_DIR, dbName + ".script" );
    assertTrue( "Seed script not found: " + script.getAbsolutePath(), script.isFile() );

    try ( Connection conn = DriverManager.getConnection( "jdbc:hsqldb:mem:seedtest_" + dbName + "_" + System.nanoTime(), "sa", "" );
          Statement stmt = conn.createStatement() ) {
      List<String> failures = loadWithListener( dbName, script, stmt );

      List<String> unexpected = failures.stream()
        .filter( failure -> expectedFailures.stream().noneMatch( sql -> failure.startsWith( sql + " -> " ) ) )
        .collect( Collectors.toList() );
      assertEquals( "Unexpected failures loading " + script.getName() + ":\n" + String.join( "\n", unexpected ),
        Collections.emptyList(), unexpected );

      // Table names are used as written in the script, so they resolve the same way in this session
      for ( Map.Entry<String, Integer> expected : countInsertsPerTable( script ).entrySet() ) {
        try ( ResultSet rs = stmt.executeQuery( "SELECT COUNT(*) FROM " + expected.getKey() ) ) {
          assertTrue( rs.next() );
          assertEquals( "Rows loaded into " + expected.getKey() + " from " + script.getName(),
            expected.getValue().intValue(), rs.getInt( 1 ) );
        }
      }
      stmt.execute( "SHUTDOWN" );
    }
  }

  /**
   * Runs the script through the listener's own (private) loader, so statement splitting matches production. The
   * listener swallows SQL errors, so the Statement is wrapped to record each failed statement as "sql -> error".
   */
  private static List<String> loadWithListener( String dbName, File script, Statement stmt ) throws Exception {
    List<String> failures = new ArrayList<>();
    Statement recordingStmt = (Statement) Proxy.newProxyInstance( Statement.class.getClassLoader(),
      new Class<?>[] { Statement.class }, ( proxy, method, args ) -> {
        try {
          return method.invoke( stmt, args );
        } catch ( InvocationTargetException e ) {
          if ( "execute".equals( method.getName() ) && e.getCause() instanceof SQLException ) {
            failures.add( args[ 0 ] + " -> " + e.getCause().getMessage() );
          }
          throw e.getCause();
        }
      } );

    Method loader = HsqldbStartupListener.class.getDeclaredMethod( "executeSqlStatements", String.class, File.class,
      Statement.class );
    loader.setAccessible( true );
    try {
      loader.invoke( new HsqldbStartupListener(), dbName, script, recordingStmt );
    } catch ( InvocationTargetException e ) {
      throw (Exception) e.getCause();
    }
    return failures;
  }

  private static Map<String, Integer> countInsertsPerTable( File script ) throws Exception {
    Map<String, Integer> counts = new TreeMap<>();
    for ( String line : Files.readAllLines( script.toPath(), StandardCharsets.UTF_8 ) ) {
      Matcher m = INSERT_TABLE.matcher( line.trim() );
      if ( m.find() ) {
        counts.merge( m.group( 1 ).toUpperCase(), 1, Integer::sum );
      }
    }
    return counts;
  }
}
