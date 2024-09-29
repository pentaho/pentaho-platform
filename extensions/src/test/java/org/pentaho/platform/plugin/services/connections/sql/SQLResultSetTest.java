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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.connections.sql;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.metadata.query.model.util.QueryModelMetaData;

public class SQLResultSetTest {

  private final Object[][] columns = new Object[][]{ { "column1", "column2" } };

  private SQLConnection nativeConnection;

  private ResultSet nativeResultSet;

  @Before
  public void setUp() throws SQLException {
    ResultSetMetaData metaData = mock( ResultSetMetaData.class );
    when( metaData.getColumnCount() ).thenReturn( columns[0].length );
    when( metaData.getColumnLabel( anyInt() ) ).thenAnswer( new Answer<String>() {

      //offset for compatibility with #ResultSetMetaData
      private int offset = 1;

      @Override
      public String answer( InvocationOnMock invocation ) throws Throwable {
        return (String) columns[0][ (Integer) invocation.getArguments()[0] - offset];
      }
    } );

    nativeConnection = mock( SQLConnection.class );
    nativeResultSet = mock( ResultSet.class );
    when( nativeResultSet.getMetaData() ).thenReturn( metaData );
  }

  @Test
  public void testGetValueAt() throws SQLException {
    Object value = "testObject";
    //more than 1 for peek next row
    int rowCount = 2;
    when( nativeResultSet.getObject( anyInt() ) ).thenReturn( value );
    when( nativeResultSet.getRow() ).thenReturn( rowCount ).thenReturn( rowCount - 1 );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( value, resultSet.getValueAt( 0, 0 ) );
  }

  @Test
  public void testGetValueAtNullResultSet() throws SQLException {
    SQLResultSet resultSet = new SQLResultSet( null, nativeConnection );
    assertNull( resultSet.getValueAt( 0, 0 ) );
  }

  @Test
  public void testDispose() throws SQLException {
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    resultSet.dispose();
    verify( nativeResultSet, times( 1 ) ).close();
    verify( nativeConnection, times( 1 ) ).close();
  }

  @Test
  public void testPeek() throws SQLException {
    Object value = "testObject";
    when( nativeResultSet.getObject( anyInt() ) ).thenReturn( value );
    when( nativeResultSet.next() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    Object[] objects = resultSet.peek();
    for ( int i = 0; i < objects.length; i++ ) {
      assertEquals( value, objects[i] );
    }
  }

  @Test
  public void testNext() throws SQLException {
    Object value = "testObject";
    when( nativeResultSet.getObject( anyInt() ) ).thenReturn( value );
    when( nativeResultSet.next() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    Object[] objects = resultSet.next();
    for ( int i = 0; i < objects.length; i++ ) {
      assertEquals( value, objects[i] );
    }
  }

  @Test
  public void testGetDataColumn() throws SQLException {
    Object value = "testObject";
    int rowCount = 1;
    when( nativeResultSet.getObject( anyInt() ) ).thenReturn( value );
    when( nativeResultSet.getRow() ).thenReturn( rowCount );
    //we should read last row when native result set row count equals SQLresultSetCount
    when( nativeResultSet.last() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( value, resultSet.getDataColumn( 0 )[0] );
  }

  @Test
  public void testGetDataRow() throws SQLException {
    Object value = "testObject";
    int rowCount = 1;
    when( nativeResultSet.getObject( anyInt() ) ).thenReturn( value );
    when( nativeResultSet.getRow() ).thenReturn( rowCount );
    //we should read last row when native result set row count equals SQLresultSetCount
    when( nativeResultSet.last() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( value, resultSet.getDataRow( 0 )[0] );
  }

  @Test
  public void testGetColumnCountNativeNull() {
    SQLResultSet resultSet = new SQLResultSet( null, nativeConnection );
    assertEquals( 0, resultSet.getColumnCount() );
  }

  @Test
  public void testGetColumnCount() {
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( columns[0].length, resultSet.getColumnCount() );
  }

  @Test
  public void testGetRowCount() {
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( 0, resultSet.getRowCount() );
  }

  @Test
  public void testGetRowCountResultSetReturn0() throws SQLException {
    int rowCount = 0;
    when( nativeResultSet.getRow() ).thenReturn( rowCount );
    //we should read last row when native result set row count equals SQLresultSetCount
    when( nativeResultSet.last() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( rowCount, resultSet.getRowCount() );
  }

  @Test
  public void testGetRowCountResultSetReturnRow() throws SQLException {
    int rowCount = 1;
    when( nativeResultSet.getRow() ).thenReturn( rowCount );
    //we should read last row when native result set row count equals SQLresultSetCount
    when( nativeResultSet.last() ).thenReturn( true );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    assertEquals( rowCount, resultSet.getRowCount() );
  }

  @Test
  public void testGetRowCountNonScrolableNullResultSet() {
    SQLResultSet resultSet = new SQLResultSet( null, nativeConnection );
    assertEquals( -1, resultSet.getRowCount() );
  }

  @Test
  public void testMemoryCopy() throws SQLException {
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    IPentahoResultSet  memoryCopyResultSet = resultSet.memoryCopy();

    assertEquals( columns[0].length, memoryCopyResultSet.getColumnCount() );
    assertTrue( Arrays.equals( columns[0], memoryCopyResultSet.getMetaData().getColumnHeaders()[0] ) );

    verify( nativeResultSet, times( 1 ) ).close();
  }

  @Test
  public void testMemoryCopy_setMetadaQuery() throws SQLException {
    IPentahoMetaData metadata = new QueryModelMetaData( mock( QueryModelMetaData.class ) );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    resultSet.setMetaData( metadata );
    IPentahoResultSet  memoryCopyResultSet = resultSet.memoryCopy();

    assertEquals( metadata.getClass(), memoryCopyResultSet.getMetaData().getClass() );
  }

  @Test
  public void testMemoryCopy_setMetadaMemory() throws SQLException {
    IPentahoMetaData metadata = new MemoryMetaData( mock( MemoryMetaData.class ) );
    SQLResultSet resultSet = new SQLResultSet( nativeResultSet, nativeConnection );
    resultSet.setMetaData( metadata );
    IPentahoResultSet  memoryCopyResultSet = resultSet.memoryCopy();

    assertEquals( metadata.getClass(), memoryCopyResultSet.getMetaData().getClass() );
  }

}
