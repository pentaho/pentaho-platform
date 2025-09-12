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


package org.pentaho.test.platform.plugin.services.webservices;

import junit.framework.TestCase;
import org.pentaho.commons.connection.marshal.MarshallableColumnNames;
import org.pentaho.commons.connection.marshal.MarshallableColumnTypes;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;

@SuppressWarnings( { "all" } )
public class ResultSetTest extends TestCase {

  public void testWSResultSet1() {

    MemoryMetaData metadata = new MemoryMetaData( new String[][] { { "col1", "col2" } }, null );

    metadata.setColumnTypes( new String[] { "string", "int" } );

    MemoryResultSet data = new MemoryResultSet( metadata );

    data.addRow( new Object[] { "a", new Integer( 1 ) } );
    data.addRow( new Object[] { "b", new Integer( 2 ) } );
    data.addRow( new Object[] { "c", new Integer( 3 ) } );

    MarshallableResultSet result = new MarshallableResultSet();
    result.setResultSet( data );
    validate( result );

  }

  private void validate( MarshallableResultSet result ) {

    MarshallableColumnNames colNames = result.getColumnNames();
    assertNotNull( colNames );
    String[] cols = colNames.getColumnName();
    assertNotNull( cols );
    assertEquals( 2, cols.length );
    assertEquals( "col1", cols[0] );
    assertEquals( "col2", cols[1] );

    MarshallableColumnTypes colTypes = result.getColumnTypes();
    assertNotNull( colTypes );
    String[] types = colTypes.getColumnType();
    assertNotNull( types );
    assertEquals( 2, types.length );
    assertEquals( "string", types[0] );
    assertEquals( "integer", types[1] );

    MarshallableRow[] rows = result.getRows();
    assertNotNull( rows );
    assertEquals( 3, rows.length );

    MarshallableRow row = rows[0];
    assertNotNull( row );
    String[] cells = row.getCell();
    assertEquals( 2, cells.length );
    assertEquals( "a", cells[0] );
    assertEquals( "1", cells[1] );

    row = rows[1];
    assertNotNull( row );
    cells = row.getCell();
    assertEquals( 2, cells.length );
    assertEquals( "b", cells[0] );
    assertEquals( "2", cells[1] );

    row = rows[2];
    assertNotNull( row );
    cells = row.getCell();
    assertEquals( 2, cells.length );
    assertEquals( "c", cells[0] );
    assertEquals( "3", cells[1] );

  }

}
