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

package org.pentaho.test.platform.plugin.services.connections;

import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

//import org.pentaho.platform.api.engine.IRuntimeContext;
//import org.pentaho.platform.plugin.services.messages.Messages;
//import org.pentaho.platform.util.xml.dom4j.DataGrid;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
@SuppressWarnings( "nls" )
public class ConnectionIT extends BaseTest {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/connections-solution";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/connections-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }

  }

  public Map getRequiredListeners() {
    Map listeners = super.getRequiredListeners();
    listeners.put( "mondrian", "mondrian" ); //$NON-NLS-1$ //$NON-NLS-2$
    return listeners;
  }

  // public void testSQLConnection() {
  // startTest();
  //        OutputStream outputStream = this.getOutputStream("ConnectionTest.testSQLConnection", ".csv"); //$NON-NLS-1$ //$NON-NLS-2$
  // IPentahoSession session = new StandaloneSession("Admin");
  //        IPentahoConnection connection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, session, this); //$NON-NLS-1$
  // try {
  //            IPentahoResultSet results = connection.executeQuery("select * from DEPARTMENT_MANAGERS"); //$NON-NLS-1$
  // Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
  // for (int row = 0; row < columnHeaders.length; row++) {
  // for (int col = 0; col < columnHeaders[0].length; col++) {
  // outputStream.write(columnHeaders[row][col].toString().getBytes());
  //                    outputStream.write(",".getBytes()); //$NON-NLS-1$
  // }
  //                outputStream.write("\n".getBytes()); //$NON-NLS-1$
  // }
  // Object[] row = results.next();
  // while (row != null) {
  // for (int i = 0; i < row.length; i++) {
  // outputStream.write(row[i].toString().getBytes());
  //                    outputStream.write(",".getBytes()); //$NON-NLS-1$
  // }
  //                outputStream.write("\n".getBytes()); //$NON-NLS-1$
  // row = results.next();
  // }
  //
  // } catch (Exception e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // connection.close();
  // finishTest();
  // }

  /*
   * public void testConnectionWithPropertyName() { startTest(); OutputStream outputStream =
   * this.getOutputStream("ConnectionTest.testConnectionWithPropertyName", ".csv"); //$NON-NLS-1$ //$NON-NLS-2$
   * 
   * IPentahoConnection connection = PentahoConnectionFactory.getConnection("datasource", this); //$NON-NLS-1$ // @TODO
   * Need to know how to use this getConnection method. Where does this property name comes from
   * 
   * try { IPentahoResultSet results = connection.executeQuery("select * from DEPARTMENT_MANAGERS"); //$NON-NLS-1$
   * Object[][] columnHeaders = results.getMetaData().getColumnHeaders(); for (int row = 0; row < columnHeaders.length;
   * row++) { for (int col = 0; col < columnHeaders[0].length; col++) {
   * outputStream.write(columnHeaders[row][col].toString().getBytes()); outputStream.write(",".getBytes());
   * //$NON-NLS-1$ } outputStream.write("\n".getBytes()); //$NON-NLS-1$ } Object[] row = results.next(); while (row !=
   * null) { for (int i = 0; i < row.length; i++) { outputStream.write(row[i].toString().getBytes());
   * outputStream.write(",".getBytes()); //$NON-NLS-1$ } outputStream.write("\n".getBytes()); //$NON-NLS-1$ row =
   * results.next(); }
   * 
   * } catch (Exception e) { // TODO Auto-generated catch block e.printStackTrace(); } connection.close(); finishTest();
   * }
   */

  public void testMDXConnectionWithPropertiesFile() {
    startTest();
    OutputStream outputStream = this.getOutputStream( "ConnectionTest.testMDXConnectionWithPropertiesFile", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    IPentahoSession session = new StandaloneSession( "Admin" );
    Properties mdxProperties = new Properties();
    try {
      mdxProperties.load( new FileInputStream( "mdxConnection.properties" ) ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    IPentahoConnection connection =
        PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE, mdxProperties, session, this );
    // @TODO Need to know how to use this getConnection method. Where does this property name comes from

    try {
      IPentahoResultSet results = connection.executeQuery( "select * from DEPARTMENT_MANAGERS" ); //$NON-NLS-1$
      Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
      for ( int row = 0; row < columnHeaders.length; row++ ) {
        for ( int col = 0; col < columnHeaders[0].length; col++ ) {
          outputStream.write( columnHeaders[row][col].toString().getBytes() );
          outputStream.write( ",".getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( "\n".getBytes() ); //$NON-NLS-1$
      }
      Object[] row = results.next();
      while ( row != null ) {
        for ( int i = 0; i < row.length; i++ ) {
          outputStream.write( row[i].toString().getBytes() );
          outputStream.write( ",".getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( "\n".getBytes() ); //$NON-NLS-1$
        row = results.next();
      }

    } catch ( Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    connection.close();
    finishTest();
  }

  public void testSQLConnectionWithAllInfo() {
    startTest();
    IPentahoSession session = new StandaloneSession( "Admin" );
    OutputStream outputStream = this.getOutputStream( "ConnectionTest.testConnectionWithPropertyName", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    File file =
        new File( PentahoSystem.getApplicationContext().getSolutionPath( "test/datasources/SampleData.mondrian.xml" ) ); //$NON-NLS-1$
    IPentahoConnection connection =
        PentahoConnectionFactory
            .getConnection(
              IPentahoConnection.MDX_DATASOURCE,
              "jdbc:hsqldb:hsql://localhost:9001/sampledata; Catalog=" + file.toURI().toString(), "mondrian", "sa", "",
              session, this ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    try {
      IPentahoResultSet results = connection.executeQuery( "select * from DEPARTMENT_MANAGERS" ); //$NON-NLS-1$
      Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
      for ( int row = 0; row < columnHeaders.length; row++ ) {
        for ( int col = 0; col < columnHeaders[0].length; col++ ) {
          outputStream.write( columnHeaders[row][col].toString().getBytes() );
          outputStream.write( ",".getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( "\n".getBytes() ); //$NON-NLS-1$
      }
      Object[] row = results.next();
      while ( row != null ) {
        for ( int i = 0; i < row.length; i++ ) {
          outputStream.write( row[i].toString().getBytes() );
          outputStream.write( ",".getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( "\n".getBytes() ); //$NON-NLS-1$
        row = results.next();
      }

    } catch ( Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    connection.close();
    finishTest();
  }

  /**
     * 
     */
  @SuppressWarnings( "deprecation" )
  public void testMDXConnection() {
    startTest();
    IPentahoSession session = new StandaloneSession( "Admin" );
    OutputStream outputStream = this.getOutputStream( "ConnectionTest.testSQLConnection", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    File file =
        new File( PentahoSystem.getApplicationContext().getSolutionPath( "test/datasources/SampleData.mondrian.xml" ) ); //$NON-NLS-1$
    IPentahoConnection connection =
        PentahoConnectionFactory
            .getConnection(
              IPentahoConnection.MDX_DATASOURCE,
              "jdbc:hsqldb:hsql://localhost:9001/sampledata; Catalog=" + file.toURI().toString(), "mondrian", "sa", "",
              session, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    String query =
        "select {[Measures].[Actual], [Measures].[Budget]} on rows, {[Region].[All Regions]} ON columns from [Quadrant Analysis] WHERE ([Positions].[All Positions])"; //$NON-NLS-1$

    try {
      IPentahoResultSet results = connection.executeQuery( query );
      Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
      for ( int row = columnHeaders.length - 1; row >= 0; row-- ) {
        for ( int col = 0; col < columnHeaders[row].length; col++ ) {
          outputStream.write( ( columnHeaders[row][col] + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( '\n' );
      }

      Object[][] rowHeaders = results.getMetaData().getRowHeaders();
      int rowIdx = 0;

      Object[] row = results.next();
      while ( row != null ) {
        for ( int colIdx = rowHeaders[rowIdx].length - 1; colIdx >= 0; colIdx-- ) {
          outputStream.write( ( rowHeaders[rowIdx][colIdx].toString() + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        for ( int colIdx = 0; colIdx < row.length; colIdx++ ) {
          outputStream.write( ( row[colIdx] + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( '\n' );
        row = results.next();
        rowIdx++;
      }
      results.close();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
    connection.close();
    finishTest();
  }

  /**
   * This test currently fails, due to Mondrian not understanding the catalog URL. I'm guessing we're not using the
   * latest mondrian libs that support mondrian
   */
  @SuppressWarnings( "deprecation" )
  public void testMDX_VFS_zipped_Schema() {
    startTest();

    // StandaloneSession session = new StandaloneSession(Messages.getString("BaseTest.DEBUG_JUNIT_SESSION")); //$NON-NLS-1$
    // SolutionRepositoryVfs.setSolutionRepository( PentahoSystem.getSolutionRepository( session ) );
    IPentahoSession session = new StandaloneSession( "Admin" );
    OutputStream outputStream = this.getOutputStream( "ConnectionTest.testSQLConnection", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    File file =
        new File( PentahoSystem.getApplicationContext().getSolutionPath( "test/datasources/SampleDataSchema.zip" ) ); //$NON-NLS-1$
    String catalog = "zip:" + file.toURI().toString() + "!/SampleData.mondrian.xml"; //$NON-NLS-1$ //$NON-NLS-2$
    catalog = "solution:/test/datasources/SampleData.mondrian.xml;vfs=true"; //$NON-NLS-1$
    IPentahoConnection connection =
        PentahoConnectionFactory.getConnection( IPentahoConnection.MDX_DATASOURCE,
            "jdbc:hsqldb:hsql://localhost:9001/sampledata; Catalog=" + catalog, "mondrian", "sa", "", session, null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    String query =
        "select {[Measures].[Actual], [Measures].[Budget]} on rows, {[Region].[All Regions]} ON columns from [Quadrant Analysis] WHERE ([Positions].[All Positions])"; //$NON-NLS-1$

    try {
      IPentahoResultSet results = connection.executeQuery( query );
      Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
      for ( int row = columnHeaders.length - 1; row >= 0; row-- ) {
        for ( int col = 0; col < columnHeaders[row].length; col++ ) {
          outputStream.write( ( columnHeaders[row][col] + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( '\n' );
      }

      Object[][] rowHeaders = results.getMetaData().getRowHeaders();
      int rowIdx = 0;

      Object[] row = results.next();
      while ( row != null ) {
        for ( int colIdx = rowHeaders[rowIdx].length - 1; colIdx >= 0; colIdx-- ) {
          outputStream.write( ( rowHeaders[rowIdx][colIdx].toString() + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        for ( int colIdx = 0; colIdx < row.length; colIdx++ ) {
          outputStream.write( ( row[colIdx] + "\t" ).getBytes() ); //$NON-NLS-1$
        }
        outputStream.write( '\n' );
        row = results.next();
        rowIdx++;
      }
      results.close();

    } catch ( Exception e ) {
      e.printStackTrace();
    }
    connection.close();
    finishTest();
  }

  public void testXQueryConnection() {
    startTest();
    IPentahoSession session = new StandaloneSession( "Admin" );
    OutputStream outputStream = this.getOutputStream( "ConnectionTest.testSQLConnection", ".csv" ); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      IPentahoConnection connection =
          PentahoConnectionFactory.getConnection( IPentahoConnection.XML_DATASOURCE, session, this );
      String query =
          "doc(\"" + PentahoSystem.getApplicationContext().getSolutionPath( "samples/datasources/books.xml" ) + "\")/bookstore/book"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
      query = query.replace( '\\', '/' );
      IPentahoResultSet results = connection.executeQuery( query );
      assertNotNull( results );
      Object[][] columnHeaders = results.getMetaData().getColumnHeaders();
      for ( int row = 0; row < columnHeaders.length; row++ ) {
        for ( int col = 0; col < columnHeaders[0].length; col++ ) {
          outputStream.write( columnHeaders[row][col].toString().getBytes() );
          outputStream.write( ',' );
        }
        outputStream.write( '\n' );
      }
      Object[] row = results.next();
      while ( row != null ) {
        for ( int i = 0; i < row.length; i++ ) {
          outputStream.write( row[i].toString().getBytes() );
          outputStream.write( ',' );
        }
        outputStream.write( '\n' );
        row = results.next();
      }

    } catch ( Exception e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finishTest();
  }

  public static void main( String[] args ) {
    ConnectionIT test = new ConnectionIT();
    test.setUp();
    try {
      // test.testMdx();
      // test.testSQLConnection();
      test.testMDXConnection();
      test.testMDX_VFS_zipped_Schema();
      test.testXQueryConnection();
      // test.testXQueryAction();
      // test.testRelationalGrid();
      // test.testConnectionWithPropertyName();
      test.testMDXConnectionWithPropertiesFile();
      test.testSQLConnectionWithAllInfo();

    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
