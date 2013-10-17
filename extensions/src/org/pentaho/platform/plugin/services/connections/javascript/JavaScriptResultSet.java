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

package org.pentaho.platform.plugin.services.connections.javascript;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;

import java.text.SimpleDateFormat;

public class JavaScriptResultSet extends ScriptableObject implements IPentahoResultSet, IPeekable {
  /**
   * 
   */
  private static final long serialVersionUID = -2303805979176976941L;

  private IPentahoResultSet results;

  private MemoryResultSet writeableResults;

  protected Object[] peekRow;

  private StringBuffer description;

  public JavaScriptResultSet() {
    description = new StringBuffer();
    results = null;
  }

  public void setResultSet( final IPentahoResultSet pResults ) {
    this.results = pResults;
    if ( results instanceof MemoryResultSet ) {
      writeableResults = (MemoryResultSet) results;
    }
  }

  @Override
  public String getClassName() {
    return "JavaScriptResultSet"; //$NON-NLS-1$
  }

  public static Object jsFunction_getColumnCount( final Context cx, final Scriptable thisObj, final Object[] args,
      final Function funObj ) {
    if ( ( args != null ) && ( args.length > 0 ) ) {
      return null;
    }
    JavaScriptResultSet resultSet = (JavaScriptResultSet) thisObj;
    return new Integer( resultSet.getColumnCount() );
  }

  public static Object jsFunction_getRowCount( final Context cx, final Scriptable thisObj, final Object[] args,
      final Function funObj ) {
    if ( ( args != null ) && ( args.length > 0 ) ) {
      return null;
    }
    JavaScriptResultSet resultSet = (JavaScriptResultSet) thisObj;
    return new Integer( resultSet.getRowCount() );
  }

  public static Object jsFunction_getValueAt( final Context cx, final Scriptable thisObj, final Object[] args,
      final Function funObj ) {
    if ( args == null ) {
      return null;
    }
    if ( args.length < 2 ) {
      return null;
    }
    JavaScriptResultSet resultSet = (JavaScriptResultSet) thisObj;
    int row = 0, column = 0;
    try {
      if ( args[0] instanceof Number ) {
        row = ( (Number) args[0] ).intValue();
      } else if ( args[0] instanceof String ) {
        row = Integer.parseInt( (String) args[0] );
      } else {
        return null;
      }
      if ( args[1] instanceof Number ) {
        column = ( (Number) args[1] ).intValue();
      } else if ( args[1] instanceof String ) {
        column = Integer.parseInt( (String) args[1] );
      } else {
        return null;
      }
    } catch ( Exception e ) {
      return null;
    }
    return resultSet.getValueAt( row, column );
  }

  public static Object jsFunction_setColumnHeaders( final Context cx, final Scriptable thisObj, final Object[] args,
      final Function funObj ) {
    if ( args == null ) {
      return null;
    }
    if ( args.length == 0 ) {
      return null;
    }
    JavaScriptResultSet resultSet = (JavaScriptResultSet) thisObj;
    if ( ( args.length == 1 ) && ( args[0] instanceof NativeArray ) ) {
      NativeArray array = (NativeArray) args[0];
      resultSet.setMetaData( JavaScriptResultSet.createMetadata( array, thisObj ) );
    } else if ( ( args.length == 2 ) && ( args[0] instanceof NativeArray ) && ( args[1] instanceof NativeArray ) ) {
      NativeArray array = (NativeArray) args[0];
      MemoryMetaData metaData = JavaScriptResultSet.createMetadata( array, thisObj );
      // create some metadata objects
      array = (NativeArray) args[1];
      int length = (int) array.getLength();
      String[] columnTypes = new String[length];
      for ( int i = 0; i < length; i++ ) {
        columnTypes[i] = array.get( i, thisObj ).toString();
      }
      metaData.setColumnTypes( columnTypes );
      resultSet.setMetaData( metaData );
    } else {
      int length = args.length;
      String[] columnHeaders = new String[length];
      for ( int i = 0; i < length; i++ ) {
        columnHeaders[i] = args[i].toString();
      }
      MemoryMetaData metaData = new MemoryMetaData( new String[][] { columnHeaders }, null );
      resultSet.setMetaData( metaData );
    }
    return null;
  }

  private static MemoryMetaData createMetadata( final NativeArray array, final Scriptable thisObj ) {
    int length = (int) array.getLength();
    String[] columnHeaders = new String[length];
    for ( int i = 0; i < length; i++ ) {
      columnHeaders[i] = array.get( i, thisObj ).toString();
    }
    return new MemoryMetaData( new String[][] { columnHeaders }, null );
  }

  public static Object jsFunction_addRow( final Context cx, final Scriptable thisObj, final Object[] args,
      final Function funObj ) {
    if ( args == null ) {
      return null;
    }
    if ( args.length == 0 ) {
      return null;
    }
    // TODO support dates
    JavaScriptResultSet resultSet = (JavaScriptResultSet) thisObj;
    if ( ( args.length == 1 ) && ( args[0] instanceof NativeArray ) ) {
      NativeArray array = (NativeArray) args[0];
      int length = (int) array.getLength();
      Object[] row = new Object[length];
      String[] columnTypes = ( (MemoryMetaData) resultSet.getMetaData() ).getColumnTypes();
      for ( int i = 0; i < length; i++ ) {
        Object data = array.get( i, thisObj );
        if ( data == null ) {
          row[i] = null;
        } else if ( columnTypes != null ) {
          if ( data instanceof NativeJavaObject ) {
            // see if we can force a conversion
            Object outputClass = null;
            if ( "string".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
              outputClass = java.lang.String.class;
            } else if ( "date".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
              outputClass = java.util.Date.class;
            } else if ( "int".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
              outputClass = java.lang.Integer.class;
            } else if ( "float".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
              outputClass = java.lang.Float.class;
            } else if ( "double".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
              outputClass = java.lang.Double.class;
            }
            if ( ( NativeJavaObject.canConvert( data, outputClass.getClass() ) ) ) {
              row[i] = Context.jsToJava( data, java.lang.String.class );
            } else {
              row[i] = null;
            }
          }
          if ( "string".equalsIgnoreCase( columnTypes[i] ) ) { //$NON-NLS-1$
            row[i] = data.toString();
          } else if ( "date".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof String ) ) { //$NON-NLS-1$
            SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" ); //$NON-NLS-1$
            try {
              row[i] = format.parse( (String) data );
            } catch ( Throwable t ) {
              row[i] = null;
            }
          } else if ( "int".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof Integer ) ) { //$NON-NLS-1$
            row[i] = data;
          } else if ( "int".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof Double ) ) { //$NON-NLS-1$
            row[i] = new Integer( ( (Double) data ).intValue() );
          } else if ( "int".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof String ) ) { //$NON-NLS-1$
            row[i] = new Integer( (String) data );
          } else if ( "float".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof Double ) ) { //$NON-NLS-1$
            row[i] = data;
          } else if ( "float".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof Integer ) ) { //$NON-NLS-1$
            row[i] = new Double( ( (Integer) data ).floatValue() );
          } else if ( "float".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof String ) ) { //$NON-NLS-1$
            row[i] = new Integer( (String) data );
          } else if ( "double".equalsIgnoreCase( columnTypes[i] ) && ( data instanceof Double ) ) { //$NON-NLS-1$
            row[i] = data;
          }
        } else if ( data instanceof NativeJavaObject ) {
          Object obj = ( (NativeJavaObject) data ).unwrap();
          row[i] = obj;
        } else {
          row[i] = data;
        }
      }
      resultSet.addRow( row );
    } else {
      int length = args.length;
      String[] row = new String[length];
      for ( int i = 0; i < length; i++ ) {
        row[i] = args[i].toString();
      }
      resultSet.addRow( row );
    }
    return null;
  }

  public void setMetaData( final IPentahoMetaData metaData ) {
    results = new MemoryResultSet( metaData );
    writeableResults = (MemoryResultSet) results;
    // this.metaData = metaData;
    // rows = new ArrayList();
  }

  /* IPentahoResultSet methods */
  public void addRow( final String[] row ) {
    if ( writeableResults != null ) {
      writeableResults.addRow( row );
    }
    // rows.add( row );
    if ( description.length() < 100 ) {
      description.append( row );
    }
  }

  public void addRow( final Object[] row ) {
    if ( writeableResults != null ) {
      writeableResults.addRow( row );
    }
    // rows.add( row );
    if ( description.length() < 100 ) {
      description.append( row );
    }
  }

  @Override
  public String toString() {
    if ( results.getMetaData().getColumnHeaders() != null ) {
      return results.getMetaData().getColumnHeaders().toString() + description.toString();
    }
    return description.toString();
  }

  public IPentahoMetaData getMetaData() {
    return results.getMetaData();
  }

  public Object[] peek() {

    if ( peekRow == null ) {
      peekRow = next();
    }
    return peekRow;
  }

  public Object[] next() {
    if ( peekRow != null ) {
      Object[] row = peekRow;
      peekRow = null;
      return row;
    }
    return results.next();
  }

  public void close() {
    // dispose of the iterator so the rows can be iterated again
    results.close();
  }

  public void closeConnection() {
    close();
  }

  public void dispose() {
    close();
  }

  public boolean isScrollable() {
    return true;
  }

  public int getColumnCount() {
    return results.getMetaData().getColumnCount();
  }

  public int getRowCount() {
    return results.getRowCount();
  }

  public Object getValueAt( final int row, final int column ) {
    return results.getValueAt( row, column );
  }

  public IPentahoResultSet memoryCopy() {
    return results.memoryCopy();
  }

  public void beforeFirst() {
    results.beforeFirst();
  }

  public Object[] getDataColumn( final int column ) {
    return results.getDataColumn( column );
  }

  public Object[] getDataRow( final int row ) {
    return results.getDataRow( row );
  }
}
