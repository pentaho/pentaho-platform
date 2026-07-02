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
