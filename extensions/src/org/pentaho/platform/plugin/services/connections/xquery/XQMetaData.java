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
import net.sf.saxon.tinytree.TinyNodeImpl;
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
      if ( obj instanceof TinyNodeImpl ) {
        boolean processedChildren = false;
        AxisIterator aIter = ( (TinyNodeImpl) obj ).iterateAxis( Axis.DESCENDANT );
        Object descendent = aIter.next();
        while ( descendent != null ) {
          if ( ( descendent instanceof TinyNodeImpl ) && ( ( (TinyNodeImpl) descendent )
            .getNodeKind() == Type.ELEMENT ) ) {
            TinyNodeImpl descNode = (TinyNodeImpl) descendent;
            processedChildren = true;
            if ( !headers.contains( descNode.getDisplayName() ) ) {
              headers.add( descNode.getDisplayName() );
            }
          }
          descendent = aIter.next();
        }
        if ( !processedChildren ) {
          Object value = ( (TinyNodeImpl) obj ).getDisplayName();
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
