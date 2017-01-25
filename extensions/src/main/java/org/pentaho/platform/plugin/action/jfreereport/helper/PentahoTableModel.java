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

package org.pentaho.platform.plugin.action.jfreereport.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IMultiDimensionalMetaData;
import org.pentaho.commons.connection.IMultiDimensionalResultSet;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.util.IVersionHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;

import javax.swing.table.AbstractTableModel;

// import org.jfree.report.modules.misc.tablemodel.TypeMapper;

public class PentahoTableModel extends AbstractTableModel implements CloseableTableModel {
  private static final long serialVersionUID = 696878055074045444L;

  private IPentahoResultSet resultSet;

  private transient Object[][] rowHeaders;

  public PentahoTableModel( final IPentahoResultSet rs ) {
    super();
    resultSet = rs;
  }

  /**
   * returns the logger object
   * 
   * @return log
   */
  public Log getLogger() {
    return LogFactory.getLog( getClass() );
  }

  @SuppressWarnings( "deprecation" )
  public int getColumnCount() {
    if ( resultSet == null ) {
      return 0;
    }

    if ( rowHeaders == null ) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if ( ( rowHeaders != null ) && ( rowHeaders.length > 0 ) ) {
      return rowHeaders[0].length + resultSet.getColumnCount();
    } else {
      return resultSet.getColumnCount();
    }
  }

  @SuppressWarnings( "deprecation" )
  public Object getValueAt( final int rowIndex, int columnIndex ) {
    if ( resultSet == null ) {
      return null;
    }

    if ( rowHeaders == null ) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if ( rowHeaders != null ) {
      if ( columnIndex < rowHeaders[0].length ) {
        return rowHeaders[rowIndex][columnIndex];
      } else {
        columnIndex -= rowHeaders[0].length;
      }
    }

    // catch any exceptions so we don't blow up the entire jfreereport
    Object val = null;
    try {
      val = resultSet.getValueAt( rowIndex, columnIndex );
    } catch ( IndexOutOfBoundsException e1 ) {
      //
      // MB - This isn't an error condition. Indeed, it will happen when there are zero rows of
      // data. So, log an info message and be done with it. We also don't want an if-check to be
      // done on every cell for this boundary case.
      //
      getLogger().info( Messages.getInstance().getErrorString( "PentahoTableModel.ERROR_0001_GET_VALUE_AT" ) ); //$NON-NLS-1$
    } catch ( Throwable t ) {
      IVersionHelper versionHelper = PentahoSystem.get( IVersionHelper.class, null );
      getLogger().error( "Error Start: Pentaho " + versionHelper.getVersionInformation( this.getClass() ) ); //$NON-NLS-1$
      getLogger().error( Messages.getInstance().getErrorString( "PentahoTableModel.ERROR_0001_GET_VALUE_AT" ), t ); //$NON-NLS-1$
      getLogger().error( "Error end:" ); //$NON-NLS-1$
    }

    return val;
  }

  public int getRowCount() {
    if ( resultSet != null ) {
      return resultSet.getRowCount();
    }
    return 0;
  }

  @SuppressWarnings( "deprecation" )
  @Override
  public String getColumnName( int columnNumber ) {
    if ( resultSet == null ) {
      return null;
    }

    // Flatten out the column headers into one column-name
    Object[][] columnHeaders = resultSet.getMetaData().getColumnHeaders();
    if ( rowHeaders == null ) {
      rowHeaders = resultSet.getMetaData().getRowHeaders();
    }

    if ( ( rowHeaders != null ) && ( rowHeaders.length > 0 ) ) {
      if ( columnNumber < rowHeaders[0].length ) {
        if ( resultSet instanceof IMultiDimensionalResultSet ) {
          // If the requested column number is in the range, return it. Otherwise return a blank string
          final String[] columnNames = ( (IMultiDimensionalMetaData) resultSet.getMetaData() ).getRowHeaderNames();
          return ( columnNumber >= 0 && columnNumber < columnNames.length ) ? columnNames[columnNumber] : ""; //$NON-NLS-1$
        }
      } else {
        columnNumber -= rowHeaders[0].length;
      }
    }
    StringBuffer buf = new StringBuffer();
    if ( ( columnHeaders != null ) && ( columnHeaders.length > 0 ) ) {
      for ( int i = 0; i < columnHeaders.length; i++ ) {
        if ( i > 0 ) {
          buf.append( "/" ); //$NON-NLS-1$
        }
        buf.append( columnHeaders[i][columnNumber].toString() );
      }
    }
    return buf.toString();
  }

  public void close() {
    // Close the old result set if needed.
    if ( resultSet != null ) {
      resultSet.closeConnection();
      resultSet.close();
    }
    resultSet = null;
    // JFreeReport wont listen, but it is always good style to comply to
    // the contract ..
    fireTableStructureChanged();
  }
}
