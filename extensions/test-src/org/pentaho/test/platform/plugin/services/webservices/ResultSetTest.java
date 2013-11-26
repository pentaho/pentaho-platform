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
