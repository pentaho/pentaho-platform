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


package org.pentaho.platform.plugin.services.connections.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.AbstractPentahoMetaData;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class SQLMetaData extends AbstractPentahoMetaData {

  protected static final Log logger = LogFactory.getLog( SQLMetaData.class );

  ResultSetMetaData nativeMetaData = null;

  private Object[][] columnHeaders;

  public SQLMetaData( final ResultSetMetaData nativeMetaData ) {
    this.nativeMetaData = nativeMetaData;
  }

  public int[] getJDBCColumnTypes() throws SQLException {
    if ( nativeMetaData != null ) {
      int colCount = nativeMetaData.getColumnCount();
      if ( colCount > 0 ) {
        int[] rtn = new int[colCount];
        for ( int i = 0; i < colCount; i++ ) {
          rtn[i] = nativeMetaData.getColumnType( i + 1 ); // JDBC is 1-based
        }
        return rtn;
      } else {
        return new int[] {};
      }
    } else {
      throw new IllegalStateException();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   * 
   * In the case of SQL data there is only 1 row
   */
  @Override
  public Object[][] getColumnHeaders() {
    if ( columnHeaders == null ) {
      try {
        int rowCount = 1;
        int columnCount = nativeMetaData.getColumnCount();
        Object[][] result = new Object[rowCount][columnCount];
        for ( int column = 0; column < columnCount; column++ ) {
          result[0][column] = nativeMetaData.getColumnLabel( column + 1 );
        }
        this.columnHeaders = result;
      } catch ( SQLException e ) {
        SQLMetaData.logger.error( null, e );
      }
    }
    return columnHeaders;
  }

  @Override
  public int getColumnCount() {
    try {
      return nativeMetaData.getColumnCount();
    } catch ( SQLException ex ) {
      SQLMetaData.logger.error( null, ex );
    }
    // TODO: Ripple the exception out of this package
    return -1;
  }

  @Override
  public Object[][] getRowHeaders() {
    return null;
  }
}
