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

import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.modules.misc.datafactory.StaticDataFactory;

import javax.swing.table.TableModel;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * This needs the latest CVS version of JFreeReport (0.8.7-5-cvs)...
 * 
 * @author Thomas Morgner
 */
public class PentahoDataFactory extends StaticDataFactory {
  private static final long serialVersionUID = 1235223223457803299L;
  private ClassLoader classLoader;

  public PentahoDataFactory( final ClassLoader classLoader ) {
    this.classLoader = classLoader;
  }

  @Override
  public ClassLoader getClassLoader() {
    return classLoader;
  }

  @Override
  public TableModel queryData( final String string, final DataRow dataRow ) throws ReportDataFactoryException {
    final TableModel tableModel = super.queryData( string, dataRow );

    try {
      final Class cls = tableModel.getClass();
      final Map map = new HashMap();

      String[] columnNames = dataRow.getColumnNames();
      for ( String columnName : columnNames ) {
        map.put( columnName, dataRow.get( columnName ) );
      }

      final Object[] args = { map };
      final Class[] argt = { Map.class };
      final Method theMethod = cls.getMethod( "setParameters", argt ); //$NON-NLS-1$
      if ( theMethod != null ) {
        theMethod.invoke( tableModel, args );
      }
    } catch ( Exception ignored ) {
      // Method does not exist... ok, ignore it.
    }

    return tableModel;
  }

}
