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

import mondrian.olap.Axis;
import mondrian.olap.Hierarchy;
import mondrian.olap.Member;
import mondrian.olap.Result;
import org.pentaho.commons.connection.AbstractPentahoMetaData;
import org.pentaho.commons.connection.IMultiDimensionalMetaData;
import org.pentaho.commons.connection.MetaDataUtil;

import java.util.List;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class MDXMetaData extends AbstractPentahoMetaData implements IMultiDimensionalMetaData {
  private static final int AXIS_COLUMN = 0;

  private Object[][] columnHeaders;

  private Object[][] rowHeaders;

  private String[] columnNames;

  private String[] columnNamesFlattened;

  protected String columnNameFormatStr; // format mask to use to generate the columnNames

  private static final int AXIS_ROW = 1;

  Result nativeResultSet = null;

  private boolean useExtendedColumnNames = false;

  public MDXMetaData() {
    super();
  }

  /**
   * @param connection
   */
  public MDXMetaData( final Result nativeResultSet, boolean useExtendedColumnNames ) {
    super();
    this.useExtendedColumnNames = useExtendedColumnNames;
    this.nativeResultSet = nativeResultSet;
    columnHeaders = createColumnHeaders();
    rowHeaders = createRowHeaders();
    columnNames = createColumnNames();
  }

  /**
   * @param connection
   */
  public MDXMetaData( final Result nativeResultSet ) {
    this( nativeResultSet, false );
  }

  protected Object[][] createColumnHeaders() {
    int rowCount = 0;
    int colCount = 0;
    Object[][] result = null;

    Axis[] axes = nativeResultSet.getAxes();
    // Another IndexOOB Fix
    if ( ( axes.length <= MDXMetaData.AXIS_COLUMN ) || ( axes[MDXMetaData.AXIS_COLUMN] == null ) ) {
      return new Object[0][0];
    }

    List positions = axes[MDXMetaData.AXIS_COLUMN].getPositions();
    if ( useExtendedColumnNames ) {
      if ( positions != null && positions.size() > 0 ) {
        rowCount = ( (List) positions.get( 0 ) ).size();
        colCount = positions.size();
      }
      result = new Object[rowCount][colCount];
      for ( int c = 0; c < colCount; c++ ) {
        List members = (List) positions.get( c );
        Member member = null;
        for ( int r = 0; r < rowCount; r++ ) {
          member = (Member) members.get( r );
          result[r][c] = member.getCaption();
        }
      }
    } else {
      if ( ( positions != null ) && ( positions.size() > 0 ) ) {
        rowCount = ( (List) positions.get( 0 ) ).size() + 1;
        colCount = positions.size();
      }
      result = new Object[rowCount][colCount];
      for ( int c = 0; c < colCount; c++ ) {
        List members = (List) positions.get( c );
        Member member = null;
        for ( int r = 0; r < rowCount - 1; r++ ) {
          member = (Member) members.get( r );
          result[r][c] = member.getCaption();
        }
        result[rowCount - 1][c] = member.getHierarchy().getCaption();
      }
    }
    return result;
  }

  protected Object[][] createRowHeaders() {
    int rowCount = 0;
    int colCount = 0;
    Object[][] result = null;
    Axis[] axes = nativeResultSet.getAxes();
    // Another IndexOOB Fix
    if ( ( axes.length <= MDXMetaData.AXIS_ROW ) || ( axes[MDXMetaData.AXIS_ROW] == null ) ) {
      return new Object[0][0];
    }

    List positions = axes[MDXMetaData.AXIS_ROW].getPositions();

    if ( useExtendedColumnNames ) {
      if ( positions != null && positions.size() > 0 ) {
        rowCount = positions.size();
        colCount = ( (List) positions.get( 0 ) ).size();
      }
      result = new Object[rowCount][colCount];
      for ( int r = 0; r < rowCount; r++ ) {
        List members = (List) positions.get( r );
        Member member = null;
        for ( int c = 0; c < colCount; c++ ) {
          member = (Member) members.get( c );
          result[r][c] = member.getCaption();
        }
      }
    } else {
      if ( ( positions != null ) && ( positions.size() > 0 ) ) {
        rowCount = positions.size();
        colCount = ( (List) positions.get( 0 ) ).size() + 1;
      }
      result = new Object[rowCount][colCount];
      for ( int r = 0; r < rowCount; r++ ) {
        List members = (List) positions.get( r );
        Member member = null;
        for ( int c = 0; c < colCount - 1; c++ ) {
          member = (Member) members.get( c );
          result[r][c] = member.getCaption();
        }
        result[r][colCount - 1] = member.getHierarchy().getCaption();
      }
    }
    return result;
  }

  /**
   * Flattens the row headers into column names (where the useful columns have useful names and the unuseful columns
   * have unusful names).
   * 
   * @return the row headers in a String array
   */
  protected String[] createColumnNames() {
    String[] colNames = null;

    if ( nativeResultSet != null ) {

      // HACK for BISERVER-2640; need backward compatibility to old format of column
      // names, yet with the old format cross joins will have problems (BISERVER-1266).

      Axis[] axes = nativeResultSet.getAxes();
      // Another IndexOOB Fix
      if ( ( axes.length <= MDXMetaData.AXIS_ROW ) || ( axes[MDXMetaData.AXIS_ROW] == null ) ) {
        return new String[0]; // no rows...
      }
      List positions = axes[MDXMetaData.AXIS_ROW].getPositions();

      if ( useExtendedColumnNames ) {
        if ( ( this.rowHeaders.length > 0 ) && ( positions != null ) && ( positions.size() > 0 ) ) {
          colNames = new String[this.rowHeaders[0].length];

          // Flatten out the column headers into one column-name
          for ( int i = 0; i < colNames.length; ++i ) {
            Member member = (Member) ( (List) positions.get( 0 ) ).get( i );
            colNames[i] =
                "[" + member.getDimension().getName() + "].[" + member.getHierarchy().getName() + "].["
                    + member.getLevel().getName() + "]";
          }
        } else {
          colNames = new String[0];
        }
      } else {
        if ( ( positions != null ) && ( positions.size() > 0 ) ) {
          colNames = new String[getColumnCount()];

          // Flatten out the column headers into one column-name
          for ( int i = 0; i < colNames.length; ++i ) {
            if ( i < ( (List) positions.get( 0 ) ).size() ) {
              Member member = (Member) ( (List) positions.get( 0 ) ).get( i );
              Hierarchy hierarchy = member.getHierarchy();
              colNames[i] = hierarchy.getCaption();
            } else {
              colNames[i] =
                  ( (Member) ( (List) positions.get( 0 ) ).get( ( (List) positions.get( 0 ) ).size() - 1 ) )
                      .getHierarchy().getName()
                      + "{" + i + "}"; //$NON-NLS-1$ //$NON-NLS-2$
            }
          }
        } else {
          colNames = new String[0];
        }
      }
    }

    return colNames;
  }

  public String getColumnName( final int columnNumber ) {
    return ( ( columnNames != null ) && ( columnNumber >= 0 ) && ( columnNumber < columnNames.length )
        ? columnNames[columnNumber] : "" ); //$NON-NLS-1$
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnCount()
   */
  @Override
  public int getColumnCount() {
    Axis[] axes = nativeResultSet.getAxes();
    // Another IndexOOB Fix
    if ( ( axes.length <= MDXMetaData.AXIS_COLUMN ) || ( axes[MDXMetaData.AXIS_COLUMN] == null ) ) {
      return 0; // no rows...
    } else {
      List positions = axes[MDXMetaData.AXIS_COLUMN].getPositions();
      return positions != null ? positions.size() : 0;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   */
  @Override
  public Object[][] getColumnHeaders() {
    return columnHeaders;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getRowHeaders()
   */
  @Override
  public Object[][] getRowHeaders() {
    return rowHeaders;
  }

  protected void setColumnHeaders( Object[][] columnHeaders ) {
    this.columnHeaders = columnHeaders;
  }

  protected void setRowHeaders( Object[][] rowHeaders ) {
    this.rowHeaders = rowHeaders;
  }

  public String[] getRowHeaderNames() {
    return columnNames;
  }

  public void setColumnNameFormat( String formatStr ) {
    this.columnNameFormatStr = formatStr;
  }

  public void generateColumnNames() {
    String[] rowHeaderNames = getRowHeaderNames();
    columnNamesFlattened =
        MetaDataUtil.generateColumnNames( columnHeaders, rowHeaders, rowHeaderNames, columnNameFormatStr );
  }

  public String[] getFlattenedColumnNames() {
    if ( columnNamesFlattened == null ) {
      generateColumnNames();
    }
    return columnNamesFlattened;
  }

}
