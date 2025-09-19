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


package org.pentaho.platform.plugin.services.connections.xquery;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.type.Type;
import org.pentaho.commons.connection.AbstractPentahoMetaData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wseyler
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class XQMetaData extends AbstractPentahoMetaData {
  public static final String DEFAULT_COLUMN_NAME = "Default Column"; //$NON-NLS-1$

  private Object[][] columnHeaders;

  private Object[][] rowHeaders;

  private XQConnection connection;

  int rowCount = 0;

  public XQMetaData( final XQConnection xqConnection, final Iterator iter ) {
    this.connection = xqConnection;
    List headers = new ArrayList();
    while ( iter.hasNext() ) {
      rowCount++;
      Object obj = iter.next();
      if ( obj instanceof NodeInfo ) {
        boolean processedChildren = false;
        AxisIterator aIter = ( (NodeInfo) obj ).iterateAxis( AxisInfo.DESCENDANT );
        Object descendent = aIter.next();
        while ( descendent != null ) {
          if ( ( descendent instanceof NodeInfo ) && ( ( (NodeInfo) descendent )
            .getNodeKind() == Type.ELEMENT ) ) {
            NodeInfo descNode = (NodeInfo) descendent;
            processedChildren = true;
            if ( !headers.contains( descNode.getDisplayName() ) ) {
              headers.add( descNode.getDisplayName() );
            }
          }
          descendent = aIter.next();
        }
        if ( !processedChildren ) {
          Object value = ( (NodeInfo) obj ).getDisplayName();
          if ( !headers.contains( value ) ) {
            headers.add( value );
          }
        }
      }
    }
    if ( headers.size() > 0 ) {
      columnHeaders = new Object[1][headers.size()];
      Iterator headerIter = headers.iterator();
      int i = 0;
      while ( headerIter.hasNext() ) {
        columnHeaders[0][i] = headerIter.next();
        i++;
      }
    } else {
      columnHeaders = new Object[1][1];
      columnHeaders[0][0] = XQMetaData.DEFAULT_COLUMN_NAME;
    }

  }

  public int getRowCount() {
    return rowCount;
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
}
