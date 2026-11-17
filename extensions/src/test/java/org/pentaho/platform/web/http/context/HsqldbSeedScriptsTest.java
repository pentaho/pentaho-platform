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

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
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
import java.sql.DatabaseMetaData;
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
 * sampledata.script must also open as a native HSQLDB database: the installer reuses it for the design tools, and the
 * same file is shipped by pentaho-kettle, pentaho-metadata-editor(-ee), pentaho-reporting and pentaho-reportdesigner-ee.
 */
@RunWith( Parameterized.class )
public class HsqldbSeedScriptsTest {

  private static final File SEED_DIR = new File( "../assemblies/pentaho-data/src/main/resources/data/hsqldb" );
  private static final Pattern INSERT_TABLE = Pattern.compile( "^INSERT INTO ((?:\\w+\\.)?\\w+)\\s*VALUES", Pattern.CASE_INSENSITIVE );

  // Harmless failures: a new database already has the SA user and the PUBLIC schema
  private static final List<String> SAMPLEDATA_EXPECTED_FAILURES = Arrays.asList(
    "CREATE USER SA PASSWORD '';",
    "CREATE SCHEMA PUBLIC AUTHORIZATION DBA;" );

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Parameter( 0 )
  public String dbName;

  @Parameter( 1 )
  public List<String> expectedFailures;

  /**
   * Whether the seed must also open as a native HSQLDB database. The installer copies sampledata.script/.properties to
   * sampledatapme, sampledatapdi and sampledatareportdesigner, which Metadata Editor, PDI and Report Designer open with
   * jdbc:hsqldb:file:..., so HSQLDB restores the script itself and rejects DDL after the first INSERT.
   */
  @Parameter( 2 )
  public boolean openedNatively;

  @Parameters( name = "{0}.script" )
  public static Collection<Object[]> seeds() {
    return Arrays.asList( new Object[][] {
      { "sampledata", SAMPLEDATA_EXPECTED_FAILURES, true },
      { "hibernate", Collections.emptyList(), false },
      { "quartz", Collections.emptyList(), false },
      { "jackrabbit", Collections.emptyList(), false } } );
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

      assertRowCountsMatchInserts( script, stmt );
      assertForeignKeysHold( conn );
      stmt.execute( "SHUTDOWN" );
    }
  }

  @Test
  public void testSeedOpensAsNativeDatabase() throws Exception {
    Assume.assumeTrue( dbName + ".script is only loaded by HsqldbStartupListener", openedNatively );
    File dir = tempFolder.newFolder();
    Files.copy( new File( SEED_DIR, dbName + ".script" ).toPath(), new File( dir, dbName + ".script" ).toPath() );
    Files.copy( new File( SEED_DIR, dbName + ".properties" ).toPath(), new File( dir, dbName + ".properties" ).toPath() );

    // Same URL form the installer writes for the design tools' SampleData JNDI connection. HSQLDB rewrites the script
    // when it closes the database, so the second open reads HSQLDB's own output (this needs a valid SET FILES SCALE)
    String url = "jdbc:hsqldb:file:" + new File( dir, dbName ).getAbsolutePath() + ";ifexists=true";
    for ( int open = 1; open <= 2; open++ ) {
      try ( Connection conn = DriverManager.getConnection( url, "sa", "" );
            Statement stmt = conn.createStatement() ) {
        assertRowCountsMatchInserts( new File( SEED_DIR, dbName + ".script" ), stmt );
        assertForeignKeysHold( conn );
        stmt.execute( "SHUTDOWN" );
      }
    }
  }

  private static void assertRowCountsMatchInserts( File script, Statement stmt ) throws Exception {
    // Table names are used as written in the script, so they resolve the same way in this session
    for ( Map.Entry<String, Integer> expected : countInsertsPerTable( script ).entrySet() ) {
      try ( ResultSet rs = stmt.executeQuery( "SELECT COUNT(*) FROM " + expected.getKey() ) ) {
        assertTrue( rs.next() );
        assertEquals( "Rows loaded into " + expected.getKey() + " from " + script.getName(),
          expected.getValue().intValue(), rs.getInt( 1 ) );
      }
    }
  }

  /**
   * Fails if any row references a missing parent row. Foreign keys are read from the JDBC metadata, so this covers
   * every constraint declared by the seed, including multi-column ones.
   */
  private static void assertForeignKeysHold( Connection conn ) throws SQLException {
    DatabaseMetaData md = conn.getMetaData();
    List<String[]> tables = new ArrayList<>();
    try ( ResultSet rs = md.getTables( null, null, "%", new String[] { "TABLE" } ) ) {
      while ( rs.next() ) {
        tables.add( new String[] { rs.getString( "TABLE_SCHEM" ), rs.getString( "TABLE_NAME" ) } );
      }
    }
    for ( String[] table : tables ) {
      // Per FK: child columns that must be non-null to be checked (MATCH SIMPLE), and the join to the parent row
      Map<String, StringBuilder> notNullByFk = new TreeMap<>();
      Map<String, StringBuilder> joinByFk = new TreeMap<>();
      Map<String, String> parentByFk = new TreeMap<>();
      try ( ResultSet rs = md.getImportedKeys( null, table[ 0 ], table[ 1 ] ) ) {
        while ( rs.next() ) {
          String fk = rs.getString( "FK_NAME" );
          parentByFk.put( fk, quote( rs.getString( "PKTABLE_SCHEM" ) ) + "." + quote( rs.getString( "PKTABLE_NAME" ) ) );
          String childCol = "c." + quote( rs.getString( "FKCOLUMN_NAME" ) );
          notNullByFk.computeIfAbsent( fk, k -> new StringBuilder( "1 = 1" ) ).append( " AND " ).append( childCol )
            .append( " IS NOT NULL" );
          joinByFk.computeIfAbsent( fk, k -> new StringBuilder( "1 = 1" ) ).append( " AND p." )
            .append( quote( rs.getString( "PKCOLUMN_NAME" ) ) ).append( " = " ).append( childCol );
        }
      }
      for ( Map.Entry<String, StringBuilder> fk : joinByFk.entrySet() ) {
        String orphans = "SELECT COUNT(*) FROM " + quote( table[ 0 ] ) + "." + quote( table[ 1 ] ) + " c WHERE "
          + notNullByFk.get( fk.getKey() ) + " AND NOT EXISTS (SELECT 1 FROM " + parentByFk.get( fk.getKey() ) + " p WHERE "
          + fk.getValue() + ")";
        try ( Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery( orphans ) ) {
          assertTrue( rs.next() );
          assertEquals( "Rows in " + table[ 1 ] + " violating " + fk.getKey(), 0, rs.getInt( 1 ) );
        }
      }
    }
  }

  private static String quote( String identifier ) {
    return "\"" + identifier.replace( "\"", "\"\"" ) + "\"";
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
