/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.services.runtime;

import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IActionParameter;
import org.pentaho.platform.api.engine.ISelectionMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SelectionMapper implements ISelectionMapper {

  /**
   * Creates a SelectionMapper based on an IPentahoResultSet. If the result set has 1 column, the values and
   * display names will come from that column. If it has more than 1 column, the first column will be used for the
   * values and the second will be used for the display names
   * 
   * @param resultSet
   *          The result set to get the data from
   * @param displayName
   *          The name used to describe the choice for this selection. Usually used as a header
   * @return SelectionMapper if successful or null
   */
  public static SelectionMapper create( final IPentahoResultSet resultSet, final String displayName,
      final String displayStyle ) {
    return ( SelectionMapper.create( resultSet, 1, -1, displayName, displayStyle ) );
  }

  /**
   * Creates a SelectionMapper based on an IPentahoResultSet. The columns to use for the values and display names
   * are passed in as column names.
   * 
   * @param resultSet
   *          The result set to get the data from
   * @param valueColName
   *          The name of the column to use for the values. If null, the first column will be used
   * @param dispColName
   *          The name of the column to use for the display names. If null, the values column will be used
   * @param displayName
   *          The name used to describe the choice for this selection. Usually used as a header
   * @return SelectionMapper if successful or null
   */
  public static SelectionMapper create( final IPentahoResultSet resultSet, final String valueColName,
      final String dispColName, final String displayName, final String displayStyle ) {
    if ( resultSet == null ) {
      return ( null );
    }

    IPentahoMetaData metaData = resultSet.getMetaData();
    if ( ( metaData == null ) || ( metaData.getColumnCount() < 1 ) ) {
      // TODO surface an error
      return ( null );
    }

    int valueColumnNo = ( valueColName == null ) ? 0 : metaData.getColumnIndex( valueColName );
    if ( valueColumnNo < 0 ) {
      // TODO surface an error
      return ( null );
    }

    int dispColumnNo = -1;
    if ( dispColName != null ) {
      dispColumnNo = metaData.getColumnIndex( dispColName );
      if ( dispColumnNo < 0 ) {
        // TODO surface an error
        return ( null );
      }
    }

    return ( SelectionMapper.create( resultSet, ++valueColumnNo, ++dispColumnNo, displayName, displayStyle ) );
  }

  /**
   * Creates a SelectionMapper based on an IPentahoResultSet. The index of the column to use for the values and
   * display names are passed in. The index is 1 based so the first (left most) column is 1.
   * 
   * @param resultSet
   *          The result set to get the data from
   * @param valueColName
   *          The index of the column to use for the values.
   * @param dispColName
   *          The index of the column to use for the display names. If 0 then the valueColumn will be used.
   * @param displayName
   *          The name used to describe the choice for this selection. Usually used as a header
   * @return SelectionMapper if successful or null
   */
  public static SelectionMapper create( final IPentahoResultSet resultSet, int valueColIndex, int dispColIndex,
      final String displayName, final String displayStyle ) {
    --valueColIndex;
    --dispColIndex;

    if ( ( resultSet == null ) || ( valueColIndex < 0 ) ) {
      return ( null );
    }

    IPentahoMetaData metaData = resultSet.getMetaData();
    if ( ( metaData == null ) || ( metaData.getColumnCount() < valueColIndex )
        || ( metaData.getColumnCount() < dispColIndex ) ) {
      return ( null );
    }

    ArrayList<Object> values = new ArrayList<Object>();

    HashMap<Object, Object> displayNames = ( dispColIndex < 0 ) ? null : new HashMap<Object, Object>();
    Object[] row = resultSet.next();
    Object value, name;
    while ( row != null ) {
      value = row[valueColIndex];
      if ( value != null ) {
        value = value.toString();
        values.add( value );
        if ( displayNames != null ) {
          name = row[dispColIndex];
          displayNames.put( value, ( name != null ) ? name.toString() : value );
        }
      }
      row = resultSet.next();
    }
    // close the result set so we can loop through it again later if we need to
    resultSet.close();

    return ( new SelectionMapper( values, displayNames, displayName, displayStyle ) );
  }

  /**
   * Creates a SelectionMapper based on an IActionParameter. The columns to use for the values and display names
   * are passed in as column names.
   * 
   * @param actionParam
   *          The ActionParameter to get the data from
   * @param valueColName
   *          The name of the column to use for the values. If null, the first column will be used
   * @param dispColName
   *          The name of the column to use for the display names. If null, the values column will be used
   * @param displayName
   *          The name used to describe the choice for this selection. Usually used as a header
   * @return SelectionMapper if successful or null
   */
  public static SelectionMapper create( final IActionParameter actionParam, final String valueColName,
      final String dispColName, final String displayName, final String displayStyle ) {
    if ( actionParam == null ) {
      return ( null );
    }

    Object value = actionParam.getValue();
    if ( value instanceof IPentahoResultSet ) {
      return ( SelectionMapper.create( (IPentahoResultSet) value, valueColName, dispColName,
        displayName, displayStyle ) );
    } else if ( "property-map-list".equals( actionParam.getType() ) ) { //$NON-NLS-1$
      return ( SelectionMapper.createFromPropMapList( (List) value, valueColName, dispColName, displayName,
          displayStyle ) );
    } else if ( value instanceof List ) {
      return ( new SelectionMapper( (List) value, null, displayName, displayStyle ) );
    }

    return ( null );
  }

  /**
   * Creates a SelectionMapper based on a pentaho property map list. The index of the column to use for the values
   * and display names are passed in. The index is 1 based so the first (left most) column is 1.
   * 
   * @param resultSet
   *          The result set to get the data from
   * @param valueColName
   *          The index of the column to use for the values.
   * @param dispColName
   *          The index of the column to use for the display names. If 0 then the valueColumn will be used.
   * @param displayName
   *          The name used to describe the choice for this selection. Usually used as a header
   * @return SelectionMapper if successful or null
   */
  public static SelectionMapper createFromPropMapList( final List aList, final String valueColName,
      final String dispColName, final String displayName, final String displayStyle ) {
    if ( aList == null ) {
      return ( null );
    }

    ArrayList<String> selValues = new ArrayList<String>();
    HashMap<String, String> dispMap = new HashMap<String, String>();
    String val, disp;
    for ( Iterator it = aList.iterator(); it.hasNext(); ) {
      try {
        Map hm = (Map) it.next();
        val = hm.get( valueColName ).toString();
        if ( val != null ) {
          selValues.add( val );
        }
        disp = hm.get( dispColName ).toString();
        if ( disp != null ) {
          dispMap.put( val, disp );
        }
      } catch ( Exception ignore ) {
        //ignore
      }
    }

    return ( new SelectionMapper( selValues, dispMap, displayName, displayStyle ) );
  }

  Map selNames;

  List selValues;

  String displayName, displayStyle;

  private SelectionMapper( final List selValues, final Map selNames,
                           final String displayName, final String displayStyle ) {
    this.displayName = ( displayName != null ) ? displayName : ""; //$NON-NLS-1$
    this.selNames = selNames;
    this.selValues = ( selValues != null ) ? selValues : new ArrayList();
    this.displayStyle = displayStyle;
  }

  public String getDisplayStyle() {
    return displayStyle;
  }

  public String getSelectionDisplayName() {
    return ( displayName );
  }

  public String getSelectionNameForValue( final String val ) {
    Object rtn = null;
    if ( selNames != null ) {
      rtn = selNames.get( val );
    }
    return ( ( rtn == null ) ? val : rtn.toString() );
  }

  public List getSelectionValues() {
    return ( selValues );
  }

  public Map getSelectionNameMap() {
    return ( selNames );
  }

  public boolean hasValue( final String value ) {
    return ( selValues.contains( value ) );
  }

  public int selectionCount() {
    return ( selValues.size() );
  }

  public String getValueAt( final int index ) {
    return ( selValues.get( index ).toString() );
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer( "Display Name: " ).append( getSelectionDisplayName() ).append( " [" ); //$NON-NLS-1$ //$NON-NLS-2$
    for ( Iterator it = selValues.iterator(); it.hasNext(); ) {
      String value = it.next().toString();
      sb.append( " [" ).append( value ).append( " : " ).append( getSelectionNameForValue( value ) ).append( "] " ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
    }
    sb.append( "]" ); //$NON-NLS-1$
    return ( sb.toString() );
  }

}
