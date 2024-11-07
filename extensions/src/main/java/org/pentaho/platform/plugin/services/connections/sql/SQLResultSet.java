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

package org.pentaho.platform.plugin.services.connections.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class SQLResultSet implements IPentahoResultSet, IPeekable {
  ResultSet nativeResultSet = null;

  SQLConnection connection;

  private static final int COUNT_NEVER_OBTAINED = -2;

  private int rowCount = SQLResultSet.COUNT_NEVER_OBTAINED;

  private int columnCount = SQLResultSet.COUNT_NEVER_OBTAINED;

  protected Object[] peekRow;

  private static final Log log = LogFactory.getLog( SQLResultSet.class );

  private IPentahoMetaData metaData;

  /**
   * 
   */
  public SQLResultSet( final ResultSet nativeResultSet, final SQLConnection nativeConnection ) {
    super();
    this.connection = nativeConnection;
    this.nativeResultSet = nativeResultSet;
  }

  public void setMetaData( final IPentahoMetaData metadata ) {
    this.metaData = metadata;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getMetaData()
   */
  public IPentahoMetaData getMetaData() {
    if ( metaData == null ) {
      try {
        metaData = new SQLMetaData( nativeResultSet.getMetaData() );
      } catch ( SQLException e ) {
        // TODO Auto-generated catch block
        SQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0004_GET_METADATA" ), e ); //$NON-NLS-1$
        // log.error(null, e);
        throw new RuntimeException( e );
      }
    }
    return metaData;
  }

  public Object[] peek() {

    if ( peekRow == null ) {
      peekRow = next();
    }
    return peekRow;
  }

  /**
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#next() returns null if no more rows
   * 
   * @throws SQLResultSetException
   */
  public Object[] next() {
    if ( peekRow != null ) {
      Object[] row = peekRow;
      peekRow = null;
      return row;
    }
    try {
      int columns = nativeResultSet.getMetaData().getColumnCount();
      if ( nativeResultSet.next() ) {
        Object[] currentRow = new Object[columns];
        for ( int column = 0; column < columns; column++ ) {
          currentRow[column] = nativeResultSet.getObject( column + 1 );
        }
        return currentRow;
      }
    } catch ( SQLException e ) {
      // TODO surface this error
      SQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0005_NEXT" ), e ); //$NON-NLS-1$
      throw new SQLResultSetException( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0005_NEXT" ), e ); //$NON-NLS-1$
    }
    return null;
  }

  public void closeConnection() {
    close();
    if ( connection != null ) {
      try {
        connection.close();
      } catch ( Exception ignored ) {
        //ignore
      }
    }
    connection = null;
  }

  public void close() {
    if ( nativeResultSet != null ) {
      try {
        nativeResultSet.close();
      } catch ( SQLException e ) {
        // TODO sbarkdull, localize this!
        SQLResultSet.log.warn( Messages.getInstance().getString( "SQLResultSet.WARN_CONNECTION_NOT_CLOSED" ) ); //$NON-NLS-1$
      }
      rowCount = SQLResultSet.COUNT_NEVER_OBTAINED;
    }
    nativeResultSet = null;
  }

  public void dispose() {
    closeConnection();
  }

  public boolean isScrollable() {
    if ( nativeResultSet == null ) {
      return false;
    }

    int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    try {
      resultSetType = nativeResultSet.getType();
    } catch ( SQLException ex ) {
      SQLResultSet.log.warn( Messages.getInstance().getString( "SQLResultSet.WARN_RESULTSET_TYPE_UNDETERMINED" ) ); //$NON-NLS-1$
    }
    if ( resultSetType == ResultSet.TYPE_FORWARD_ONLY ) {
      return false;
    } else {
      return true;
    }
  }

  /**
   * Returns the column count from the result set.
   * 
   * @return the column count.
   */
  public int getColumnCount() {
    if ( columnCount != SQLResultSet.COUNT_NEVER_OBTAINED ) {
      // We have already calculated column count, return what we have.
      return columnCount;
    }
    if ( nativeResultSet == null ) {
      return 0;
    }
    try {
      columnCount = nativeResultSet.getMetaData().getColumnCount();
      return columnCount;
    } catch ( SQLException ex ) {
      // TODO: Surfase this exception.
      SQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0006_GET_COLUMNCOUNT" ), ex ); //$NON-NLS-1$
    }
    return 0;
  }

  /**
   * Get a rowCount from the resultset. If the resultset
   * 
   * @return the row count.
   */
  public int getRowCount() {
    if ( !isScrollable() ) {
      return -1;
    }
    if ( rowCount != SQLResultSet.COUNT_NEVER_OBTAINED ) {
      // We have already calculated rowcount, return what we have
      return rowCount;
    }
    // No resultset
    if ( nativeResultSet == null ) {
      return 0;
    }
    try {
      // Get current row in the resultset
      int curRow = nativeResultSet.getRow();
      try {
        // Seek to the end of the resultset. This could be very
        // bad for performance if the cursor is client-side.
        if ( nativeResultSet.last() ) {
          // Get the rownumber of the last row
          rowCount = nativeResultSet.getRow();
          // Boundary case
          if ( rowCount <= 0 ) {
            rowCount = 0;
          }
        } else {
          // Couldn't seek to last row - Scrollable resultsets not
          // supported?
          // TODO: Possibly throw an exception in this case
          rowCount = 0;
        }
      } finally {
        // There is no row 0 - if the curRow was 0, go to before the
        // first row in the resultset
        if ( curRow == 0 ) {
          nativeResultSet.beforeFirst();
        } else {
          // Go back where we started
          nativeResultSet.absolute( curRow );
        }
      }
    } catch ( SQLException sqle ) {
      SQLResultSet.log.error(
          Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0001_OBTAINING_ROWCOUNT" ), sqle ); //$NON-NLS-1$
      rowCount = 0;
    }
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
    if ( nativeResultSet != null ) {
      try {
        /*
         * Following code courtesy of contribution from Rui Goncalves BISERVER-3213
         */
        int curNativeRow = nativeResultSet.getRow();
        if ( curNativeRow != ( row + 1 ) ) {
          if ( curNativeRow == row ) {
            nativeResultSet.next();
          } else {
            nativeResultSet.absolute( row + 1 );
          }
        }
        return nativeResultSet.getObject( column + 1 );
      } catch ( SQLException ex ) {
        SQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0002_GET_VALUE" ), ex ); //$NON-NLS-1$
        throw new IllegalStateException( Messages.getInstance().getErrorString(
            "SQLResultSet.ERROR_0008_FORWARDED_SQL_MSG" ), ex ); //$NON-NLS-1$
      }
    }
    return null;
  }

  /**
   * <b>Attention: </b> It does not clone data!  It is create the shallow copy of metadata! 
   * It is create the shallow copy of data. You must avoid to use this method. 
   * @return new instance the {@link MemoryResultSet} with same metadata
   */
  public IPentahoResultSet memoryCopy() {
    try {
      // we have the {@link #setMetaData(IPentahoMetaData)} so the metadata can be any 
      // class which implements IPentahoMetaData, we should not lost data from metadata, so we must use metadata from original result set,
      // or clone metadata. The IPentahoMetaData does not implement Cloneable and we unable to clone data. So keep the shallow copy of metadata.
      MemoryResultSet cachedResultSet = new MemoryResultSet( getMetaData() );
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
      if ( nativeResultSet == null ) {
        SQLResultSet.log.error( Messages.getInstance().getErrorString(
          "SQLResultSet.ERROR_0007_BEFORE_FIRST_CONNECTION_CLOSED" ) ); //$NON-NLS-1$
      } else if ( isScrollable() ) {
        nativeResultSet.beforeFirst();
      } else {
        SQLResultSet.log.info( Messages.getInstance().getString(
          "SQLResultSet.INFO_IGNORE_BEFORE_FIRST_RESULTSET_NON_SCROLLABLE" ) );
      }
    } catch ( SQLException e ) {
      SQLResultSet.log.error( Messages.getInstance().getErrorString( "SQLResultSet.ERROR_0003_BEFORE_FIRST" ), e ); //$NON-NLS-1$
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
