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

package org.pentaho.platform.plugin.services.connections.mondrian;

import mondrian.olap.Connection;
import mondrian.olap.Result;
import org.pentaho.commons.connection.IMultiDimensionalResultSet;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class MDXResultSet implements IPentahoResultSet, IPeekable, IMultiDimensionalResultSet {

  private Result nativeResultSet;

  private Connection nativeConnection;

  private int rowIndex = 0;

  private MDXMetaData mdxMetaData = null;

  private Object[] peekRow;

  private boolean formattedCellValues = false; // should we return formatted or plain cell values

  public MDXResultSet() {

  }

  /**
   * @param useExtendedColumnNames
   *          if true, columnNames will follow the format: "[dimension_name].[hierarchy_name].[level_name]" otherwise
   *          the format for column names will be: "hierarchy_name{column_number}"
   * 
   *          Implemented as a flag to allow reports prior to platform version 2.1 (Liberty) to continue to execute as
   *          expected with the short column names, but if the developer sets the extendedColumnNames flag to true, can
   *          overcome the bug in BISERVER-1266.
   * 
   * @param returnNullCells
   *          if true, returns null instead of 0.000000012345. This is configurable for backwards compatibility
   * 
   */
  public MDXResultSet( final Result nativeResultSet, final Connection nativeConnection,
                       boolean useExtendedColumnNames ) {
    super();
    this.nativeResultSet = nativeResultSet;
    this.nativeConnection = nativeConnection;

    mdxMetaData = new MDXMetaData( this.nativeResultSet, useExtendedColumnNames );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getMetaData()
   */
  public IPentahoMetaData getMetaData() {
    return mdxMetaData;
  }

  public Object[] peekRowHeaders() {
    int peekRowNo = rowIndex;
    if ( peekRowNo < getRowCount() ) {
      return mdxMetaData.getRowHeaders()[peekRowNo];
    }
    return null;
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
   * @see org.pentaho.connection.IPentahoResultSet#next()
   */
  public Object[] next() {

    if ( peekRow != null ) {
      Object[] row = peekRow;
      peekRow = null;
      return row;
    }
    Object[] currentRow = null;
    int columnCount = getColumnCount();
    if ( rowIndex < getRowCount() ) {
      currentRow = new Object[columnCount];
      for ( int i = 0; i < columnCount; i++ ) {
        currentRow[i] = getValueAt( rowIndex, i );
      }
      rowIndex++;
    }
    return currentRow;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#close()
   */
  public void close() {
    nativeResultSet.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#closeConnection()
   */
  public void closeConnection() {
    nativeResultSet.close();
    nativeConnection.close();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.runtime.IDisposable#dispose()
   */
  public void dispose() {
    closeConnection();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#isScrollable()
   */
  public boolean isScrollable() {
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getValueAt(int, int)
   */
  public Object getValueAt( final int row, final int column ) {
    int[] key = new int[2];
    key[0] = column;
    key[1] = row;
    if ( formattedCellValues ) {
      return nativeResultSet.getCell( key ).getFormattedValue();
    } else {
      return nativeResultSet.getCell( key ).getValue();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getRowCount()
   */
  public int getRowCount() {
    return mdxMetaData.getRowHeaders().length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getColumnCount()
   */
  public int getColumnCount() {
    return mdxMetaData.getColumnCount();
  }

  public IPentahoResultSet memoryCopy() {
    try {
      IPentahoMetaData metadata = getMetaData();
      Object[][] columnHeaders = metadata.getColumnHeaders();
      Object[][] rowHeaders = metadata.getRowHeaders();

      MemoryMetaData cachedMetaData = new MemoryMetaData( columnHeaders, rowHeaders );
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
    rowIndex = 0;
  }

  public Object[] getDataColumn( final int column ) {
    int oldIndex = rowIndex; // save our current iteration location

    beforeFirst();
    Object[] result = new Object[getRowCount()];
    int index = 0;
    Object[] rowData = next();
    while ( rowData != null ) {
      result[index] = rowData[column];
      index++;
      rowData = next();
    }

    rowIndex = oldIndex; // restore the old iteration location

    return result;
  }

  public Object[] getDataRow( final int row ) {
    int oldIndex = rowIndex; // save our current iteration location

    rowIndex = row;
    Object[] rowData = next();
    rowIndex = oldIndex;

    return rowData;
  }

  protected Result getNativeResultSet() {
    return nativeResultSet;
  }

  protected void setNativeResultSet( Result nativeResultSet ) {
    this.nativeResultSet = nativeResultSet;
  }

  protected Connection getNativeConnection() {
    return nativeConnection;
  }

  protected void setNativeConnection( Connection nativeConnection ) {
    this.nativeConnection = nativeConnection;
  }

  protected MDXMetaData getMdxMetaData() {
    return mdxMetaData;
  }

  protected void setMdxMetaData( MDXMetaData mdxMetaData ) {
    this.mdxMetaData = mdxMetaData;
  }

  protected Object[] getPeekRow() {
    return peekRow;
  }

  protected void setPeekRow( Object[] peekRow ) {
    this.peekRow = peekRow;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public Object[] nextFlattened() {
    Object[][] rowHeaders = mdxMetaData.getRowHeaders();
    if ( rowHeaders == null ) {
      // we have no row headers so we can call the regular next()
      return next();
    }
    // get the row
    Object[] row = next();
    if ( row == null ) {
      // we have got to the end
      return null;
    }
    // do we have row headers to return also?
    if ( rowIndex <= rowHeaders.length ) {
      // pull out the right row headers
      Object[] rowHeads = rowHeaders[rowIndex - 1];
      // create the flattened row
      Object[] flatRow = new Object[rowHeads.length + row.length];
      // copy in the row headers and row objects
      System.arraycopy( rowHeads, 0, flatRow, 0, rowHeads.length );
      System.arraycopy( row, 0, flatRow, rowHeads.length, row.length );
      return flatRow;
    }
    return row;
  }

  public Object[] peekFlattened() {
    Object[][] rowHeaders = mdxMetaData.getRowHeaders();
    if ( rowHeaders == null ) {
      // we have no row headers so we can call the regular peek()
      return peek();
    }
    // get the row
    Object[] row = peek();
    if ( row == null ) {
      // we have got to the end
      return null;
    }
    // do we have row headers to return also?
    if ( rowIndex <= rowHeaders.length ) {
      // pull out the right row headers
      Object[] rowHeads = rowHeaders[rowIndex - 1];
      // create the flattened row
      Object[] flatRow = new Object[rowHeads.length + row.length];
      // copy in the row headers and row objects
      System.arraycopy( rowHeads, 0, flatRow, 0, rowHeads.length );
      System.arraycopy( row, 0, flatRow, rowHeads.length, row.length );
      return flatRow;
    }
    return row;
  }

  /**
   * Sets the 'formatted cell values' flag. If this flag is set calls to getValueAt (and methods like next() and peek()
   * that use getValueAt) returns the formatted value of the cell instead of the plain number. BISERVER-3543
   * 
   * @param formattedCellValues
   */
  public void setFormattedCellValues( boolean formattedCellValues ) {
    this.formattedCellValues = formattedCellValues;
  }

}
