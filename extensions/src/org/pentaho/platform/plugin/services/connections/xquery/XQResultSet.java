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

package org.pentaho.platform.plugin.services.connections.xquery;

import net.sf.saxon.om.Axis;
import net.sf.saxon.om.AxisIterator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.tinytree.TinyNodeImpl;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.Value;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPeekable;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class XQResultSet implements IPentahoResultSet, IPeekable {

  protected static final Log logger = LogFactory.getLog( XQResultSet.class );

  protected XQueryExpression exp = null;

  protected DynamicQueryContext dynamicContext = null;

  protected XQMetaData metaData = null;

  protected static final String DELIM = ", "; //$NON-NLS-1$

  protected static final String EMPTY_STR = ""; //$NON-NLS-1$

  protected Object[] peekRow;

  Iterator iter = null;

  protected String[] columnTypes = null;

  protected XQConnection connection;

  private List evaluatedList;

  /**
   * @param exp
   * @param dynamicContext
   * @param columnTypes
   * @throws XPathException
   */
  public XQResultSet( final XQConnection xqConnection, final XQueryExpression exp,
      final DynamicQueryContext dynamicContext, final String[] columnTypes ) throws XPathException {
    super();
    this.columnTypes = columnTypes;
    this.exp = exp;
    this.dynamicContext = dynamicContext;
    this.connection = xqConnection;
    init();
  }

  protected void init() throws XPathException {
    if ( evaluatedList == null ) {
      evaluatedList = evaluate();
    }
    if ( this.metaData == null ) {
      iter = evaluatedList.iterator();
      this.metaData = new XQMetaData( connection, iter );
    }
    iter = evaluatedList.iterator();
  }

  protected List evaluate() throws XPathException {
    SequenceIterator sequenceiterator = exp.iterator( dynamicContext );
    List rtn = new ArrayList( 100 );
    int rowCount = 0;
    int maxRows = ( this.connection != null ) ? this.connection.getMaxRows() : -1;
    Item item = null;
    while ( ( item = sequenceiterator.next() ) != null ) {
      if ( ( item == null ) ) {
        break;
      }
      rowCount++;
      if ( ( maxRows >= 0 ) && ( rowCount > maxRows ) ) {
        break;
      }
      rtn.add( Value.convertToJava( item ) );
    }
    return rtn;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getMetaData()
   */
  public IPentahoMetaData getMetaData() {
    return metaData;
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

    // Create a map of the headers and assign empty string to them
    OrderedMap resultList = new ListOrderedMap();
    for ( int i = 0; i < metaData.getColumnCount(); i++ ) {
      resultList.put( metaData.getColumnHeaders()[0][i], XQResultSet.EMPTY_STR );
    }
    // Get the next row of data
    if ( iter.hasNext() ) {
      Object o = iter.next();
      decodeNode( o, resultList );
    }
    // get the values
    Object[] currentRow = new Object[resultList.size()];
    Iterator keyIter = resultList.keySet().iterator();
    int i = 0;
    while ( keyIter.hasNext() ) {
      currentRow[i] = resultList.get( keyIter.next() );
      i++;
    }
    // if all the values are the empty string then we're done.
    boolean done = true;
    for ( Object element : currentRow ) {
      if ( !( "".equals( element ) ) ) { //$NON-NLS-1$
        done = false;
      }
    }
    if ( done ) {
      return null;
    }
    return currentRow;
  }

  protected void decodeNode( final Object obj, final Map retValue ) {
    if ( obj instanceof TinyNodeImpl ) {
      AxisIterator aIter = ( (TinyNodeImpl) obj ).iterateAxis( Axis.DESCENDANT );
      Object descendent = aIter.next();
      boolean processedChildren = false;
      int columnIndex = 0;
      while ( descendent != null ) {
        if ( ( descendent instanceof TinyNodeImpl ) && ( ( (TinyNodeImpl) descendent )
          .getNodeKind() == Type.ELEMENT ) ) {
          TinyNodeImpl descNode = (TinyNodeImpl) descendent;
          Object value = retValue.get( descNode.getDisplayName() );
          if ( value == null ) {
            value = XQResultSet.EMPTY_STR;
          }
          if ( !( XQResultSet.EMPTY_STR.equals( value ) ) ) {
            value = value.toString() + XQResultSet.DELIM;
          }
          value = value.toString() + descNode.getStringValue();
          if ( ( value != null )
              && !value.equals( "" ) && ( columnTypes != null ) && ( columnIndex >= 0 )
            && ( columnIndex < columnTypes.length ) ) { //$NON-NLS-1$
            String columnType = columnTypes[columnIndex].trim();
            if ( columnType.equals( "java.math.BigDecimal" ) ) { //$NON-NLS-1$
              value = new BigDecimal( value.toString() );
            } else if ( columnType.equals( "java.sql.Timestamp" ) ) { //$NON-NLS-1$
              value = new Timestamp( Long.parseLong( value.toString() ) );
            } else if ( columnType.equals( "java.sql.Date" ) ) { //$NON-NLS-1$
              value = new Date( Long.parseLong( value.toString() ) );
            } else if ( columnType.equals( "java.lang.Integer" ) ) { //$NON-NLS-1$
              value = new Integer( Integer.parseInt( value.toString() ) );
            } else if ( columnType.equals( "java.lang.Double" ) ) { //$NON-NLS-1$
              value = new Double( Double.parseDouble( value.toString() ) );
            } else if ( columnType.equals( "java.lang.Long" ) ) { //$NON-NLS-1$
              value = new Long( Long.parseLong( value.toString() ) );
            }
          }
          retValue.put( descNode.getDisplayName(), value );
          processedChildren = true;
          columnIndex++;
        }
        descendent = aIter.next();
      }
      if ( !processedChildren ) {
        Object key = ( (TinyNodeImpl) obj ).getDisplayName();
        Object value = ( (TinyNodeImpl) obj ).getStringValue();
        retValue.put( key, value );
      }
    } else {
      retValue.put( XQMetaData.DEFAULT_COLUMN_NAME, obj.toString() );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#close()
   */
  public void close() {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#closeConnection()
   */
  public void closeConnection() {
    // TODO Auto-generated method stub
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#isScrollable()
   */
  public boolean isScrollable() {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getValueAt(int, int)
   */
  public Object getValueAt( final int row, final int column ) {
    Object[] rowarr = getDataRow( row );
    if ( ( rowarr != null ) && ( column >= 0 ) && ( column < rowarr.length ) ) {
      return rowarr[column];
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getRowCount()
   */
  public int getRowCount() {
    return metaData.getRowCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getColumnCount()
   */
  public int getColumnCount() {
    return metaData.getColumnCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.core.runtime.IDisposable#dispose()
   */
  public void dispose() {
    // TODO Auto-generated method stub
  }

  public IPentahoResultSet memoryCopy() {
    try {
      IPentahoMetaData metadata = getMetaData();
      Object[][] columnHeaders = metadata.getColumnHeaders();
      MemoryMetaData cachedMetaData = new MemoryMetaData( columnHeaders, null );
      // set column types of cachedMetaData
      String[] columnTypeClones = new String[columnTypes.length];
      System.arraycopy( columnTypes, 0, columnTypeClones, 0, columnTypes.length );
      cachedMetaData.setColumnTypes( columnTypeClones );

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
      init();
    } catch ( XPathException e ) {
      XQResultSet.logger.error( "Cannot initialize XQResultSet", e );
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoResultSet#getDataColumn(int)
   * 
   * NOTE: calling this will move the cursor to the top of the result stack
   */
  public Object[] getDataColumn( final int column ) {
    if ( column >= getColumnCount() ) {
      return null;
    }
    beforeFirst(); // go to top just in case we called this after some
    // next()s
    Object[] result = new Object[getRowCount()];
    int rowIndex = 0;
    Object[] rowData = next();
    while ( rowData != null ) {
      result[rowIndex] = rowData[column];
      rowIndex++;
      rowData = next();
    }
    beforeFirst();
    return result;
  }

  public Object[] getDataRow( final int row ) {
    beforeFirst(); // go to top
    int count = 0;
    while ( count++ < row ) {
      next();
    }
    Object[] dataRow = next();
    beforeFirst();
    return dataRow;
  }
}
