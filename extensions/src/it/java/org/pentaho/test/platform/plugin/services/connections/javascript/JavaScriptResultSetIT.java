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


package org.pentaho.test.platform.plugin.services.connections.javascript;

import java.io.File;

import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.IRuntimeContext;
import org.pentaho.platform.plugin.services.connections.javascript.JavaScriptResultSet;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

@SuppressWarnings( { "all" } )
public class JavaScriptResultSetIT extends BaseTest {
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

  public void testRSCompareOK() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetTest.xaction" );
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() );
    IActionParameter rtn = context.getOutputParameter( "COMPARERESULT" );
    assertNotNull( rtn );
    String compareResult = rtn.getStringValue();
    assertEquals( compareResult, "No Mismatches" );
    finishTest();
  }

  public void testRSCompareNotOK1() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetCompareTest_error1.xaction" );
    assertEquals( IRuntimeContext.RUNTIME_STATUS_SUCCESS, context.getStatus() );
    finishTest();
  }

  public void testRSCompareNotOK2() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetCompareTest_error2.xaction" );
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
    finishTest();
  }

  public void testRSCompareNotOK3() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetCompareTest_error3.xaction" );
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );

    finishTest();

  }

  public void testRSCompareNotOK4() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetCompareTest_error4.xaction" );
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_CONTEXT_VALIDATE_FAIL );

    finishTest();

  }

  public void testAddRow() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet dataSet = new MemoryResultSet( metadata );
    JavaScriptResultSet data = new JavaScriptResultSet();
    data.setResultSet( dataSet );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    assertEquals( 3, data.getRowCount() );
    assertEquals( "a", data.getValueAt( 0, 0 ) );
    assertEquals( 1, data.getValueAt( 0, 1 ) );
    assertEquals( "b", data.getValueAt( 1, 0 ) );
    assertEquals( 2, data.getValueAt( 1, 1 ) );
    assertEquals( "c", data.getValueAt( 2, 0 ) );
    assertEquals( 3, data.getValueAt( 2, 1 ) );

  }

  public void testIterators() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet dataSet = new MemoryResultSet( metadata );
    JavaScriptResultSet data = new JavaScriptResultSet();
    data.setResultSet( dataSet );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    assertEquals( 3, data.getRowCount() );
    assertEquals( "a", data.getValueAt( 0, 0 ) );
    assertEquals( 1, data.getValueAt( 0, 1 ) );
    assertEquals( "b", data.getValueAt( 1, 0 ) );
    assertEquals( 2, data.getValueAt( 1, 1 ) );
    assertEquals( "c", data.getValueAt( 2, 0 ) );
    assertEquals( 3, data.getValueAt( 2, 1 ) );

    assertEquals( "a", data.next()[0] );
    assertEquals( "b", data.next()[0] );
    assertEquals( "c", data.next()[0] );
    assertNull( data.next() );

    data.beforeFirst();

    assertEquals( "a", data.next()[0] );
    assertEquals( "b", data.next()[0] );
    assertEquals( "c", data.next()[0] );
    assertNull( data.next() );

    data.close();

    assertEquals( "a", data.next()[0] );
    assertEquals( "b", data.next()[0] );
    assertEquals( "c", data.next()[0] );
    assertNull( data.next() );

    data.closeConnection();

    assertEquals( "a", data.next()[0] );
    assertEquals( "b", data.next()[0] );
    assertEquals( "c", data.next()[0] );
    assertNull( data.next() );

    data.dispose();

    assertEquals( "a", data.next()[0] );
    assertEquals( "b", data.next()[0] );
    assertEquals( "c", data.next()[0] );
    assertNull( data.next() );

  }

  public void testGetDataColumn() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet dataSet = new MemoryResultSet( metadata );
    JavaScriptResultSet data = new JavaScriptResultSet();
    data.setResultSet( dataSet );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    Object[] col = data.getDataColumn( 0 );
    assertEquals( 3, col.length );
    assertEquals( "a", col[0] );
    assertEquals( "b", col[1] );
    assertEquals( "c", col[2] );

    col = data.getDataColumn( 1 );
    assertEquals( 3, col.length );
    assertEquals( 1, col[0] );
    assertEquals( 2, col[1] );
    assertEquals( 3, col[2] );
  }

  public void testGetDataRow() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet dataSet = new MemoryResultSet( metadata );
    JavaScriptResultSet data = new JavaScriptResultSet();
    data.setResultSet( dataSet );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    Object[] row = data.getDataRow( 0 );
    assertEquals( 2, row.length );
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );
    row = data.getDataRow( 1 );
    assertEquals( "b", row[0] );
    assertEquals( 2, row[1] );
    row = data.getDataRow( 2 );
    assertEquals( "c", row[0] );
    assertEquals( 3, row[1] );

    assertNull( data.getDataRow( 99 ) );
  }

  public void testPeek() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet dataSet = new MemoryResultSet( metadata );
    JavaScriptResultSet data = new JavaScriptResultSet();
    data.setResultSet( dataSet );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    Object[] row = data.peek();
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );

    row = data.peek();
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );

    row = data.peek();
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );

    row = data.next();
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );

    row = data.peek();
    assertEquals( "b", row[0] );
    assertEquals( 2, row[1] );

    row = data.peek();
    assertEquals( "b", row[0] );
    assertEquals( 2, row[1] );

    row = data.next();
    assertEquals( "b", row[0] );
    assertEquals( 2, row[1] );

    row = data.peek();
    assertEquals( "c", row[0] );
    assertEquals( 3, row[1] );

    row = data.peek();
    assertEquals( "c", row[0] );
    assertEquals( 3, row[1] );

    row = data.next();
    assertEquals( "c", row[0] );
    assertEquals( 3, row[1] );

    row = data.peek();
    assertNull( row );

    row = data.peek();
    assertNull( row );

    row = data.peek();
    assertNull( row );

    row = data.next();
    assertNull( row );

    data.beforeFirst();
    row = data.peek();
    assertEquals( "a", row[0] );
    assertEquals( 1, row[1] );

  }

  public void testCopy() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    MemoryResultSet data1 = new MemoryResultSet( metadata );

    data1.addRow( new Object[] { "a", new Integer( 1 ) } );
    data1.addRow( new Object[] { "b", new Integer( 2 ) } );
    data1.addRow( new Object[] { "c", new Integer( 3 ) } );

    MemoryResultSet data = (MemoryResultSet) data1.memoryCopy();

    assertNotNull( data.getMetaData() );
    assertNotNull( data.getMetaData().getColumnHeaders() );
    assertNull( data.getMetaData().getRowHeaders() );

    assertEquals( 2, data.getMetaData().getColumnCount() );
    assertEquals( 1, data.getMetaData().getColumnHeaders().length );
    assertEquals( 2, data.getMetaData().getColumnHeaders()[0].length );
    assertEquals( "col1", data.getMetaData().getColumnHeaders()[0][0] );
    assertEquals( "col2", data.getMetaData().getColumnHeaders()[0][1] );

    assertEquals( 3, data.getRowCount() );
    assertEquals( "a", data.getValueAt( 0, 0 ) );
    assertEquals( 1, data.getValueAt( 0, 1 ) );
    assertEquals( "b", data.getValueAt( 1, 0 ) );
    assertEquals( 2, data.getValueAt( 1, 1 ) );
    assertEquals( "c", data.getValueAt( 2, 0 ) );
    assertEquals( 3, data.getValueAt( 2, 1 ) );

  }

  public void testRSCompareNotOK5() {
    startTest();
    IRuntimeContext context = run( "/samples/rules/ResultSetCompareTest_error5.xaction" );
    assertEquals( context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE );
    finishTest();
  }

  public static void main( String[] args ) {
    JavaScriptResultSetIT test = new JavaScriptResultSetIT();
    try {
      test.setUp();
      test.testRSCompareOK();
      test.testRSCompareNotOK1();
      test.testRSCompareNotOK2();
      test.testRSCompareNotOK3();
      test.testRSCompareNotOK4();
      test.testRSCompareNotOK5();
    } finally {
      test.tearDown();
      BaseTest.shutdown();
    }
  }
}
