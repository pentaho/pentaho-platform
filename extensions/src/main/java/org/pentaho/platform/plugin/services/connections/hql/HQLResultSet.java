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

package org.pentaho.platform.plugin.services.connections.hql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.util.List;

/**
 * @author mdamour
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class HQLResultSet implements IPentahoResultSet, IPeekable {

  private static final int COUNT_NEVER_OBTAINED = -2;

  private int rowCount = HQLResultSet.COUNT_NEVER_OBTAINED;

  private int columnCount = HQLResultSet.COUNT_NEVER_OBTAINED;

  private static final Log log = LogFactory.getLog( HQLResultSet.class );

  private IPentahoMetaData metadata;

  private List nativeResultSet = null;

  protected Object[] peekRow;

  private int index = 0;

  /**
   * 
   */
  public HQLResultSet( final List list, final String[] columnNames, final Type[] columnTypes ) {
    super();
    try {
      nativeResultSet = list;
      metadata = new HQLMetaData( list, this, columnNames, columnTypes );
    } catch ( Exception e ) {
      // TODO Auto-generated catch block
      HQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0004_GET_METADATA" ), e ); //$NON-NLS-1$
      // e.printStackTrace();
      throw new RuntimeException( e );
    }
  }

  public void setMetaData( final IPentahoMetaData metadata ) {
    this.metadata = metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getMetaData()
   */
  public IPentahoMetaData getMetaData() {
    return metadata;
  }

  public Object[] peek() {

    if ( peekRow == null ) {
      peekRow = next();
    }
    return peekRow;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#next() returns null if no more rows
   */
  public Object[] next() {
    if ( peekRow != null ) {
      Object[] row = peekRow;
      peekRow = null;
      return row;
    }
    try {
      if ( index < nativeResultSet.size() ) {
        Object row = nativeResultSet.get( index++ );
        if ( row instanceof Object[] ) {
          return (Object[]) row;
        } else {
          Object[] newRow = new Object[1];
          newRow[0] = row;
          return newRow;
        }
      }
    } catch ( Exception e ) {
      HQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0005_NEXT" ), e ); //$NON-NLS-1$
    }
    return null;
  }

  public void closeConnection() {
    close();
  }

  public void close() {
  }

  public void dispose() {
    closeConnection();
  }

  public boolean isScrollable() {
    return false;
  }

  /**
   * Returns the column count from the result set.
   * 
   * @return the column count.
   */
  public int getColumnCount() {
    if ( columnCount != HQLResultSet.COUNT_NEVER_OBTAINED ) {
      // We have already calculated column count, return what we have.
      return columnCount;
    }
    if ( nativeResultSet.size() > 0 ) {
      Object row = nativeResultSet.get( 0 );
      if ( row instanceof Object[] ) {
        columnCount = ( (Object[]) row ).length;
      } else {
        columnCount = 1;
      }
      return columnCount;
    }
    return 0;
  }

  /**
   * Get a rowCount from the resultset. If the resultset
   * 
   * @return the row count.
   */
  public int getRowCount() {
    if ( rowCount != HQLResultSet.COUNT_NEVER_OBTAINED ) {
      // We have already calculated rowcount, return what we have
      return rowCount;
    }
    rowCount = nativeResultSet.size();
    return rowCount;
  }

  /**
   * Returns the value of the specified row and the specified column from within the resultset.
   * 
   * @param row
   *          the row index.
   * @param column
   *          the column index.
   * @return the value.
   */
  public Object getValueAt( final int row, final int column ) {
    if ( row < nativeResultSet.size() ) {
      Object rowObj = nativeResultSet.get( row );
      if ( rowObj instanceof Object[] ) {
        return ( (Object[]) rowObj )[column];
      } else {
        return rowObj;
      }
    }
    return null;
  }

  public IPentahoResultSet memoryCopy() {
    try {
      IPentahoMetaData localMetadata = getMetaData();
      Object[][] columnHeaders = localMetadata.getColumnHeaders();
      MemoryMetaData cachedMetaData = new MemoryMetaData( columnHeaders, null );
      MemoryResultSet cachedResultSet = new MemoryResultSet( cachedMetaData );
      Object[] rowObjects = next();
      while ( rowObjects != null ) {
        cachedResultSet.addRow( rowObjects );
        rowObjects = next();
      }
      return cachedResultSet;
    } finally {
      close();
    }
  }

  public void beforeFirst() {
    try {
      index = 0;
    } catch ( Exception e ) {
      HQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0003_BEFORE_FIRST" ), e ); //$NON-NLS-1$
    }
  }

  public Object[] getDataColumn( final int column ) {
    Object[] result = null;
    result = new Object[getRowCount()];
    for ( int row = 0; row < result.length; row++ ) {
      result[row] = getValueAt( row, column );
    }
    return result;
  }

  public Object[] getDataRow( final int row ) {
    Object[] rowData = new Object[this.getColumnCount()];
    for ( int column = 0; column < rowData.length; column++ ) {
      rowData[column] = getValueAt( row, column );
    }
    return rowData;
  }
}
