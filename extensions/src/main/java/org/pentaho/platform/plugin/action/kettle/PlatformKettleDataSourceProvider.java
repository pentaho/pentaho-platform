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

package org.pentaho.platform.plugin.action.kettle;

import org.pentaho.di.core.database.DataSourceNamingException;
import org.pentaho.di.core.database.DataSourceProviderFactory;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.data.IJndiDatasourceService;
import org.pentaho.platform.api.data.IPooledDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.sql.DataSource;

public class PlatformKettleDataSourceProvider implements DataSourceProviderInterface {

  protected static final PlatformKettleDataSourceProvider instance = new PlatformKettleDataSourceProvider();

  private PlatformKettleDataSourceProvider() {
    // Private constructor
  }

  public DataSourceProviderInterface getInstance() {
    return instance;
  }

  protected static void hookupProvider() {
    DataSourceProviderFactory.setDataSourceProviderInterface( instance );
  }

  protected <T extends IDBDatasourceService> DataSource getNamedDataSourceFromService(
    Class<T> dataSourceServiceInterface, String dataSourceName ) throws DataSourceNamingException {
    T datasourceService = PentahoSystem.get( dataSourceServiceInterface, null );

    IDBDatasourceService service =
      ( datasourceService == null ) ? PentahoSystem.get( IDBDatasourceService.class, null ) : datasourceService;

    if ( service != null ) {
      try {
        return service.getDataSource( dataSourceName );
      } catch ( DBDatasourceServiceException ex ) {
        throw new DataSourceNamingException( ex );
      }
    }
    return null;
  }

  public DataSource getNamedDataSource( String dataSourceName ) throws DataSourceNamingException {
    return getNamedDataSourceFromService( IDBDatasourceService.class, dataSourceName );
  }

  @Override
  public DataSource getNamedDataSource( String dataSourceName, DataSourceProviderInterface.DatasourceType type )
    throws DataSourceNamingException {
    if ( type != null ) {
      switch( type ) {
        case JNDI:
          return getNamedDataSourceFromService( IJndiDatasourceService.class, dataSourceName );
        case POOLED:
          return getNamedDataSourceFromService( IPooledDatasourceService.class, dataSourceName );
      }
    }
    throw new DataSourceNamingException(
      String.format( "Unknown data source type [%s] for named data source [%s]", type, dataSourceName ) );
  }
}
