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

package org.pentaho.platform.plugin.services.connections.mondrian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import mondrian.olap.Axis;
import mondrian.olap.Dimension;
import mondrian.olap.Hierarchy;
import mondrian.olap.Level;
import mondrian.olap.Member;
import mondrian.olap.Result;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class MDXMetaDataTest {

  private static final int COLUMN_SIZE = 3;
  private static final int ROW_SIZE = 2;

  private static final String HIERARCHY_NAME = "hierarchy";
  private static final String LEVEL_NAME = "level";
  private static final String DIMENSION_NAME = "dimension";
  private static final String MEMBER_NAME = "member name";
  private static final String MEMBER_CAPTION = "member caption";

  @Test
  public void testMetadataFromEmptyAxis() {
    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( new Axis[0] );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    checkEmptyResult( metadata );
  }

  @Test
  public void testMetadataFromNullAxis() {
    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( new Axis[] { null, null } );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    checkEmptyResult( metadata );
  }

  @Test
  public void testMetadataForNullPositions() {
    Axis axColumn = mockAxis( null );
    Axis axRow = mockAxis( null );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    checkEmptyResult( metadata );
  }

  @Test
  public void testMetadataForEmptyPositions() {
    Axis axColumn = mockAxis( ListUtils.EMPTY_LIST );
    Axis axRow = mockAxis( ListUtils.EMPTY_LIST );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    checkEmptyResult( metadata );
  }

  @Test
  @SuppressWarnings( { "rawtypes", "unchecked" } )
  public void testMetadataExtendedColumnNames() {
    List positions = mockPositions( COLUMN_SIZE, ROW_SIZE );

    Axis axColumn = mockAxis( positions );
    Axis axRow = mockAxis( positions );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet, true );

    checkColumnHeaders( positions, metadata.getColumnHeaders() );
    checkRowHeaders( positions, metadata.getRowHeaders() );
    checkRowHeaderNames( positions, metadata.getRowHeaderNames() );
  }

  @Test
  @SuppressWarnings( { "rawtypes", "unchecked" } )
  public void testMetadataWithoutExtendedColumnNames() {
    List positions = mockPositions( COLUMN_SIZE, ROW_SIZE );

    Axis axColumn = mockAxis( positions );
    Axis axRow = mockAxis( positions );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );
    Object[][] columnHeaders = metadata.getColumnHeaders();

    checkExtendedColumnHeaders( positions, columnHeaders );
    checkExtendedRowHeaders( positions, metadata.getRowHeaders() );
    checkExtendedRowHeaderNames( positions, metadata.getRowHeaderNames() );
  }

  @Test
  public void testGetEmptyColumnName() {
    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( new Axis[0] );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    String columnName = metadata.getColumnName( -1 );
    assertEquals( StringUtils.EMPTY, columnName );

    columnName = metadata.getColumnName( metadata.getRowHeaderNames().length + 1 );
    assertEquals( StringUtils.EMPTY, columnName );
  }

  @Test
  @SuppressWarnings( "rawtypes" )
  public void testGetColumnName() {
    List positions = mockPositions( COLUMN_SIZE, ROW_SIZE );

    Axis axColumn = mockAxis( positions );
    Axis axRow = mockAxis( positions );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    String columnName = metadata.getColumnName( 0 );
    assertEquals( metadata.getRowHeaderNames()[0], columnName );
  }

  @Test
  @SuppressWarnings( "rawtypes" )
  public void testGetColumnCount() {
    List positions = mockPositions( COLUMN_SIZE, ROW_SIZE );

    Axis axColumn = mockAxis( positions );
    Axis axRow = mockAxis( positions );
    Axis[] axes = new Axis[] { axColumn, axRow };

    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( axes );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    int columnCount = metadata.getColumnCount();
    assertEquals( positions.size(), columnCount );
  }

  @Test
  public void testGetZeroColumnCount() {
    Result nativeResultSet = mock( Result.class );
    when( nativeResultSet.getAxes() ).thenReturn( new Axis[0] );

    MDXMetaData metadata = new MDXMetaData( nativeResultSet );

    int columnCount = metadata.getColumnCount();
    assertEquals( 0, columnCount );

    when( nativeResultSet.getAxes() ).thenReturn( new Axis[] { null } );

    metadata = new MDXMetaData( nativeResultSet );

    columnCount = metadata.getColumnCount();
    assertEquals( 0, columnCount );

    Axis axColumn = mockAxis( null );
    when( nativeResultSet.getAxes() ).thenReturn( new Axis[] { axColumn } );

    metadata = new MDXMetaData( nativeResultSet );

    columnCount = metadata.getColumnCount();
    assertEquals( 0, columnCount );
  }

  private void checkEmptyResult( MDXMetaData metadata ) {
    assertEquals( 0, metadata.getColumnHeaders().length );
    assertEquals( 0, metadata.getRowHeaders().length );
    assertEquals( 0, metadata.getRowHeaderNames().length );
  }

  @SuppressWarnings( "unchecked" )
  private void checkColumnHeaders( List<Member> positions, Object[][] columnHeaders ) {
    assertNotNull( columnHeaders );
    assertTrue( columnHeaders.length > 0 );
    for ( int i = 0; i < columnHeaders.length; i++ ) {
      for ( int j = 0; j < columnHeaders[i].length; j++ ) {
        List<Member> row = (List<Member>) positions.get( j );
        Member memberExpected = (Member) row.get( i );
        assertEquals( memberExpected.getCaption(), columnHeaders[i][j] );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void checkExtendedColumnHeaders( List<Member> positions, Object[][] columnHeaders ) {
    assertNotNull( columnHeaders );
    assertTrue( columnHeaders.length > 0 );
    for ( int i = 0; i < columnHeaders.length; i++ ) {
      for ( int j = 0; j < columnHeaders[i].length; j++ ) {
        List<Member> row = (List<Member>) positions.get( j );
        String expectedValue;
        if ( ( i == columnHeaders.length - 1 ) ) {
          Member memberExpected = (Member) row.get( i - 1 );
          expectedValue = memberExpected.getHierarchy().getCaption();
        } else {
          Member memberExpected = (Member) row.get( i );
          expectedValue = memberExpected.getCaption();
        }
        assertEquals( expectedValue, columnHeaders[i][j] );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void checkRowHeaders( List<Member> positions, Object[][] rowHeaders ) {
    assertNotNull( rowHeaders );
    assertTrue( rowHeaders.length > 0 );
    for ( int i = 0; i < rowHeaders.length; i++ ) {
      for ( int j = 0; j < rowHeaders[i].length; j++ ) {
        List<Member> row = (List<Member>) positions.get( i );
        Member memberExpected = (Member) row.get( j );
        assertEquals( memberExpected.getCaption(), rowHeaders[i][j] );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void checkExtendedRowHeaders( List<Member> positions, Object[][] rowHeaders ) {
    assertNotNull( rowHeaders );
    assertTrue( rowHeaders.length > 0 );
    for ( int i = 0; i < rowHeaders.length; i++ ) {
      for ( int j = 0; j < rowHeaders[i].length; j++ ) {
        List<Member> row = (List<Member>) positions.get( i );
        String expectedValue;
        if ( ( j == rowHeaders.length - 1 ) ) {
          Member memberExpected = (Member) row.get( j - 1 );
          expectedValue = memberExpected.getHierarchy().getCaption();
        } else {
          Member memberExpected = (Member) row.get( j );
          expectedValue = memberExpected.getCaption();
        }
        assertEquals( expectedValue, rowHeaders[i][j] );
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private void checkRowHeaderNames( List<Member> positions, String[] names ) {
    assertNotNull( names );
    assertTrue( names.length > 0 );
    for ( int i = 0; i < names.length; i++ ) {
      List<Member> row = (List<Member>) positions.get( 0 );
      Member member = (Member) row.get( i );
      String expectedName =
        "[" + member.getDimension().getName() + "].[" + member.getHierarchy().getName() + "].["
          + member.getLevel().getName() + "]";
      assertEquals( expectedName, names[i] );
    }
  }

  @SuppressWarnings( "unchecked" )
  private void checkExtendedRowHeaderNames( List<Member> positions, String[] names ) {
    assertNotNull( names );
    assertTrue( names.length > 0 );
    for ( int i = 0; i < names.length; i++ ) {
      String expectedValue;
      List<Member> row = (List<Member>) positions.get( 0 );
      if ( ( i == row.size() ) ) {
        Member member = (Member) row.get( row.size() - 1 );
        expectedValue = member.getHierarchy().getName() + "{" + i + "}";
      } else {
        Member member = (Member) row.get( i );
        expectedValue = member.getHierarchy().getCaption();
      }
      assertEquals( expectedValue, names[i] );
    }
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  private Axis mockAxis( List positions ) {
    Axis ax = mock( Axis.class );
    when( ax.getPositions() ).thenReturn( positions );
    return ax;
  }

  private Hierarchy mockHierarchy( String name, String caption ) {
    String hierarchyCaption = HIERARCHY_NAME + caption;
    String hierarchyName = HIERARCHY_NAME + name;
    Hierarchy hierarchy = mock( Hierarchy.class );
    when( hierarchy.getCaption() ).thenReturn( hierarchyCaption );
    when( hierarchy.getName() ).thenReturn( hierarchyName );
    return hierarchy;
  }

  private Level mockLevel( String name ) {
    Level level = mock( Level.class );
    when( level.getName() ).thenReturn( LEVEL_NAME + name );
    return level;
  }

  private Dimension mockDimension( String name ) {
    Dimension dimension = mock( Dimension.class );
    when( dimension.getName() ).thenReturn( DIMENSION_NAME + name );
    return dimension;
  }

  private Member mockMember( String caption, String name ) {
    Level level = mockLevel( name );
    Dimension dimension = mockDimension( name );
    Hierarchy hierarchy = mockHierarchy( name, caption );

    Member member = mock( Member.class );
    when( member.getCaption() ).thenReturn( caption );
    when( member.getHierarchy() ).thenReturn( hierarchy );
    when( member.getDimension() ).thenReturn( dimension );
    when( member.getLevel() ).thenReturn( level );
    return member;
  }

  private List<Member> mockRow( int rowSize, String additionalName ) {
    List<Member> rows = new ArrayList<Member>( rowSize );
    for ( int i = 0; i < rowSize; i++ ) {
      rows.add( mockMember( MEMBER_CAPTION + i + additionalName, MEMBER_NAME + i + additionalName ) );
    }
    return rows;
  }

  private List<List<Member>> mockPositions( int positionsSize, int rowSize ) {
    List<List<Member>> positions = new ArrayList<List<Member>>( positionsSize );
    for ( int i = 0; i < positionsSize; i++ ) {
      positions.add( mockRow( rowSize, String.valueOf( i ) ) );
    }
    return positions;
  }
}
