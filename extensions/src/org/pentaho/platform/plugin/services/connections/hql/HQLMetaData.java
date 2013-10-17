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

package org.pentaho.platform.plugin.services.connections.hql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.type.Type;
import org.pentaho.commons.connection.AbstractPentahoMetaData;

import java.util.List;

/**
 * @author mdamour
 * 
 *         TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class HQLMetaData extends AbstractPentahoMetaData {
  private Object[][] columnHeaders;

  protected static final Log logger = LogFactory.getLog( HQLMetaData.class );

  private HQLResultSet resultSet;

  private String[] columnNames;

  public HQLMetaData( final List data, final HQLResultSet resultSet, final String[] columnNames,
      final Type[] colummTypes ) {
    this.resultSet = resultSet;
    this.columnNames = columnNames;
    getColumnHeaders();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
   * 
   * In the case of HQL data there is only 1 row
   */
  @Override
  public Object[][] getColumnHeaders() {
    if ( columnHeaders == null ) {
      try {
        int rowCount = 1;
        int columnCount = resultSet.getColumnCount();
        Object[][] result = new Object[rowCount][columnCount];
        for ( int column = 0; column < columnCount; column++ ) {
          try {
            result[0][column] = columnNames[column];
          } catch ( Exception e ) {
            //ignore
          }
        }
        this.columnHeaders = result;
      } catch ( Exception e ) {
        HQLMetaData.logger.error( null, e );
      }
    }
    return columnHeaders;
  }

  @Override
  public int getColumnCount() {
    try {
      return resultSet.getColumnCount();
    } catch ( Exception ex ) {
      HQLMetaData.logger.error( null, ex );
    }
    // TODO: Ripple the exception out of this package
    return -1;
  }

  @Override
  public Object[][] getRowHeaders() {
    return null;
  }
}
