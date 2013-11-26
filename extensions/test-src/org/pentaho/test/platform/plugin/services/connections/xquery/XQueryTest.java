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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.connections.xquery;

import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.plugin.services.connections.xquery.XQConnection;
import org.pentaho.platform.plugin.services.connections.xquery.XQResultSet;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

@SuppressWarnings( { "all" } )
public class XQueryTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-src/connections-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/connections-solution";
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

  public void testQuery1() throws Exception {

    XQConnection connection = new XQConnection();
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    assertTrue( "result set is wrong type", data instanceof XQResultSet );

    assertFalse( "Should not be scrollable", data.isScrollable() );

    assertEquals( "row count is wrong", 4, data.getRowCount() );
    assertEquals( "column count is wrong", 4, data.getColumnCount() );
    assertEquals( "column header is wrong", "title", data.getMetaData().getColumnHeaders()[0][0] );
    assertEquals( "column header is wrong", "author", data.getMetaData().getColumnHeaders()[0][1] );
    assertEquals( "column header is wrong", "year", data.getMetaData().getColumnHeaders()[0][2] );
    assertEquals( "column header is wrong", "price", data.getMetaData().getColumnHeaders()[0][3] );

    // these don't do much but they should not cause errors
    data.close();
    data.closeConnection();
  }

  public void testGetDataRow() throws Exception {
    XQConnection connection = new XQConnection();
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    Object[] row = data.getDataRow( 1 );
    assertEquals( "Harry Potter", row[0] );
    assertEquals( "J K. Rowling", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "29.99", row[3] );

    row = data.getDataRow( 3 );
    assertEquals( "Learning XML", row[0] );
    assertEquals( "Erik T. Ray", row[1] );
    assertEquals( "2003", row[2] );
    assertEquals( "39.95", row[3] );

    row = data.getDataRow( 99 );
    assertNull( row );
  }

  public void testGetDataColumn() throws Exception {
    XQConnection connection = new XQConnection();
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    Object[] col = data.getDataColumn( 2 );
    assertEquals( "row count is wrong", 4, col.length );

    assertEquals( "2005", col[0] );
    assertEquals( "2005", col[1] );
    assertEquals( "2003", col[2] );
    assertEquals( "2003", col[3] );

    col = data.getDataColumn( 99 );
    assertNull( col );
  }

  public void testRowLimit() throws Exception {

    XQConnection connection = new XQConnection();
    connection.setMaxRows( 2 );
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    assertTrue( "result set is wrong type", data instanceof XQResultSet );

    assertEquals( "row count is wrong", 2, data.getRowCount() );
    assertEquals( "column header is wrong", "title", data.getMetaData().getColumnHeaders()[0][0] );
    assertEquals( "column header is wrong", "author", data.getMetaData().getColumnHeaders()[0][1] );
    assertEquals( "column header is wrong", "year", data.getMetaData().getColumnHeaders()[0][2] );
    assertEquals( "column header is wrong", "price", data.getMetaData().getColumnHeaders()[0][3] );

    Object[] row = data.next();
    assertEquals( "Everyday Italian", row[0] );
    assertEquals( "Giada De Laurentiis", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "30.00", row[3] );

    row = data.next();
    assertEquals( "Harry Potter", row[0] );
    assertEquals( "J K. Rowling", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "29.99", row[3] );

    row = data.next();
    assertNull( row );
  }

  public void testValueAt() throws Exception {

    XQConnection connection = new XQConnection();
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    assertEquals( "2005", data.getValueAt( 0, 2 ) );
    assertEquals( "Everyday Italian", data.getValueAt( 0, 0 ) );

    assertEquals( "J K. Rowling", data.getValueAt( 1, 1 ) );
    assertEquals( "29.99", data.getValueAt( 1, 3 ) );

    assertNull( data.getValueAt( -1, -1 ) );
    assertNull( data.getValueAt( 99, 0 ) );
    assertNull( data.getValueAt( 0, 99 ) );
  }

  public void testPeek() throws Exception {

    XQConnection connection = new XQConnection();
    IPentahoResultSet data = connection.executeQuery( "doc(\"" + SOLUTION_PATH
      + "/xquery/books.xml\")/bookstore/book" );
    assertNotNull( "result set is null", data );

    assertTrue( "result set is wrong type", data instanceof XQResultSet );

    assertEquals( "row count is wrong", 4, data.getRowCount() );
    assertEquals( "column header is wrong", "title", data.getMetaData().getColumnHeaders()[0][0] );
    assertEquals( "column header is wrong", "author", data.getMetaData().getColumnHeaders()[0][1] );
    assertEquals( "column header is wrong", "year", data.getMetaData().getColumnHeaders()[0][2] );
    assertEquals( "column header is wrong", "price", data.getMetaData().getColumnHeaders()[0][3] );

    assertTrue( "result set is not peekable", data instanceof IPeekable );

    IPeekable peekable = (IPeekable) data;

    Object[] row = peekable.peek();
    assertEquals( "Everyday Italian", row[0] );
    assertEquals( "Giada De Laurentiis", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "30.00", row[3] );

    row = peekable.peek();
    assertEquals( "Everyday Italian", row[0] );

    row = peekable.peek();
    assertEquals( "Everyday Italian", row[0] );

    row = peekable.peek();
    assertEquals( "Everyday Italian", row[0] );

    row = data.next();
    assertEquals( "Everyday Italian", row[0] );
    assertEquals( "Giada De Laurentiis", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "30.00", row[3] );

    row = peekable.peek();
    assertEquals( "Harry Potter", row[0] );
    assertEquals( "J K. Rowling", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "29.99", row[3] );

    row = peekable.peek();
    assertEquals( "Harry Potter", row[0] );
    assertEquals( "J K. Rowling", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "29.99", row[3] );

    row = data.next();
    assertEquals( "Harry Potter", row[0] );
    assertEquals( "J K. Rowling", row[1] );
    assertEquals( "2005", row[2] );
    assertEquals( "29.99", row[3] );

    row = peekable.peek();
    assertNotNull( row );

    row = data.next();
    assertNotNull( row );

    row = peekable.peek();
    assertNotNull( row );

    row = data.next();
    assertNotNull( row );

    row = peekable.peek();
    assertNull( row );

    row = data.next();
    assertNull( row );

    row = peekable.peek();
    assertNull( row );

  }

  /*
   * public void testAddRow() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet dataSet = new MemoryResultSet( metadata ); JavaScriptResultSet data = new JavaScriptResultSet( );
   * data.setResultSet(dataSet);
   * 
   * data.addRow( new Object[] {"a", new Integer(1) } ); data.addRow( new Object[] {"b", new Integer(2) } );
   * data.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * assertEquals( 3, data.getRowCount() ); assertEquals( "a", data.getValueAt(0, 0) ); assertEquals( 1,
   * data.getValueAt(0, 1) ); assertEquals( "b", data.getValueAt(1, 0) ); assertEquals( 2, data.getValueAt(1, 1) );
   * assertEquals( "c", data.getValueAt(2, 0) ); assertEquals( 3, data.getValueAt(2, 1) );
   * 
   * }
   * 
   * public void testIterators() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet dataSet = new MemoryResultSet( metadata ); JavaScriptResultSet data = new JavaScriptResultSet( );
   * data.setResultSet(dataSet);
   * 
   * data.addRow( new Object[] {"a", new Integer(1) } ); data.addRow( new Object[] {"b", new Integer(2) } );
   * data.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * assertEquals( 3, data.getRowCount() ); assertEquals( "a", data.getValueAt(0, 0) ); assertEquals( 1,
   * data.getValueAt(0, 1) ); assertEquals( "b", data.getValueAt(1, 0) ); assertEquals( 2, data.getValueAt(1, 1) );
   * assertEquals( "c", data.getValueAt(2, 0) ); assertEquals( 3, data.getValueAt(2, 1) );
   * 
   * assertEquals( "a", data.next()[0] ); assertEquals( "b", data.next()[0] ); assertEquals( "c", data.next()[0] );
   * assertNull( data.next() );
   * 
   * data.beforeFirst();
   * 
   * assertEquals( "a", data.next()[0] ); assertEquals( "b", data.next()[0] ); assertEquals( "c", data.next()[0] );
   * assertNull( data.next() );
   * 
   * data.close();
   * 
   * assertEquals( "a", data.next()[0] ); assertEquals( "b", data.next()[0] ); assertEquals( "c", data.next()[0] );
   * assertNull( data.next() );
   * 
   * data.closeConnection();
   * 
   * assertEquals( "a", data.next()[0] ); assertEquals( "b", data.next()[0] ); assertEquals( "c", data.next()[0] );
   * assertNull( data.next() );
   * 
   * data.dispose();
   * 
   * assertEquals( "a", data.next()[0] ); assertEquals( "b", data.next()[0] ); assertEquals( "c", data.next()[0] );
   * assertNull( data.next() );
   * 
   * }
   * 
   * public void testGetDataColumn() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet dataSet = new MemoryResultSet( metadata ); JavaScriptResultSet data = new JavaScriptResultSet( );
   * data.setResultSet(dataSet);
   * 
   * data.addRow( new Object[] {"a", new Integer(1) } ); data.addRow( new Object[] {"b", new Integer(2) } );
   * data.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * Object col[] = data.getDataColumn( 0 ); assertEquals( 3, col.length ); assertEquals( "a", col[0] ); assertEquals(
   * "b", col[1] ); assertEquals( "c", col[2] );
   * 
   * col = data.getDataColumn( 1 ); assertEquals( 3, col.length ); assertEquals( 1, col[0] ); assertEquals( 2, col[1] );
   * assertEquals( 3, col[2] ); }
   * 
   * public void testGetDataRow() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet dataSet = new MemoryResultSet( metadata ); JavaScriptResultSet data = new JavaScriptResultSet( );
   * data.setResultSet(dataSet);
   * 
   * data.addRow( new Object[] {"a", new Integer(1) } ); data.addRow( new Object[] {"b", new Integer(2) } );
   * data.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * Object row[] = data.getDataRow( 0 ); assertEquals( 2, row.length ); assertEquals( "a", row[0] ); assertEquals( 1,
   * row[1] ); row = data.getDataRow( 1 ); assertEquals( "b", row[0] ); assertEquals( 2, row[1] ); row =
   * data.getDataRow( 2 ); assertEquals( "c", row[0] ); assertEquals( 3, row[1] );
   * 
   * assertNull( data.getDataRow( 99 )); }
   * 
   * public void testPeek() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet dataSet = new MemoryResultSet( metadata ); JavaScriptResultSet data = new JavaScriptResultSet( );
   * data.setResultSet(dataSet);
   * 
   * data.addRow( new Object[] {"a", new Integer(1) } ); data.addRow( new Object[] {"b", new Integer(2) } );
   * data.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * Object row[] = data.peek( ); assertEquals( "a", row[0] ); assertEquals( 1, row[1] );
   * 
   * row = data.peek( ); assertEquals( "a", row[0] ); assertEquals( 1, row[1] );
   * 
   * row = data.peek( ); assertEquals( "a", row[0] ); assertEquals( 1, row[1] );
   * 
   * row = data.next(); assertEquals( "a", row[0] ); assertEquals( 1, row[1] );
   * 
   * row = data.peek( ); assertEquals( "b", row[0] ); assertEquals( 2, row[1] );
   * 
   * row = data.peek( ); assertEquals( "b", row[0] ); assertEquals( 2, row[1] );
   * 
   * row = data.next(); assertEquals( "b", row[0] ); assertEquals( 2, row[1] );
   * 
   * row = data.peek( ); assertEquals( "c", row[0] ); assertEquals( 3, row[1] );
   * 
   * row = data.peek( ); assertEquals( "c", row[0] ); assertEquals( 3, row[1] );
   * 
   * row = data.next( ); assertEquals( "c", row[0] ); assertEquals( 3, row[1] );
   * 
   * row = data.peek( ); assertNull( row );
   * 
   * row = data.peek( ); assertNull( row );
   * 
   * row = data.peek( ); assertNull( row );
   * 
   * row = data.next( ); assertNull( row );
   * 
   * data.beforeFirst(); row = data.peek( ); assertEquals( "a", row[0] ); assertEquals( 1, row[1] );
   * 
   * }
   * 
   * public void testCopy() {
   * 
   * MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );
   * 
   * MemoryResultSet data1 = new MemoryResultSet( metadata );
   * 
   * data1.addRow( new Object[] {"a", new Integer(1) } ); data1.addRow( new Object[] {"b", new Integer(2) } );
   * data1.addRow( new Object[] {"c", new Integer(3) } );
   * 
   * MemoryResultSet data = (MemoryResultSet) data1.memoryCopy();
   * 
   * assertNotNull( data.getMetaData() ); assertNotNull( data.getMetaData().getColumnHeaders() ); assertNull(
   * data.getMetaData().getRowHeaders() );
   * 
   * assertEquals( 2, data.getMetaData().getColumnCount() ); assertEquals( 1,
   * data.getMetaData().getColumnHeaders().length ); assertEquals( 2, data.getMetaData().getColumnHeaders()[0].length );
   * assertEquals( "col1", data.getMetaData().getColumnHeaders()[0][0] ); assertEquals( "col2",
   * data.getMetaData().getColumnHeaders()[0][1] );
   * 
   * assertEquals( 3, data.getRowCount() ); assertEquals( "a", data.getValueAt(0, 0) ); assertEquals( 1,
   * data.getValueAt(0, 1) ); assertEquals( "b", data.getValueAt(1, 0) ); assertEquals( 2, data.getValueAt(1, 1) );
   * assertEquals( "c", data.getValueAt(2, 0) ); assertEquals( 3, data.getValueAt(2, 1) );
   * 
   * }
   * 
   * // public void testRSCompareNotOK5() // { // startTest(); // IRuntimeContext context = run("samples", "rules",
   * "ResultSetCompareTest_error5.xaction"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //
   * assertEquals(context.getStatus(), IRuntimeContext.RUNTIME_STATUS_FAILURE); // // finishTest(); // // } public
   * static void main(String[] args) { XQueryTest test = new XQueryTest(); try { test.setUp(); //
   * test.testRSCompareOK(); // test.testRSCompareNotOK1(); // test.testRSCompareNotOK2(); test.testRSCompareNotOK3();
   * test.testRSCompareNotOK4(); // test.testRSCompareNotOK5(); } finally { test.tearDown(); BaseTest.shutdown(); } }
   */
}
