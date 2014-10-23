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
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.apache.commons.pool.ObjectPool;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import javax.sql.DataSource;
import java.util.List;

public class PooledDatasourceSystemListener extends NonPooledDatasourceSystemListener {

  @Override
  protected DataSource getDataSource( IDatabaseConnection connection )  {

    DataSource ds = null;
    try {
      if (!connection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {
      ds =  PooledDatasourceHelper.setupPooledDataSource( connection );
      } else {
        ds = PooledDatasourceHelper.getJndiDataSource( connection.getDatabaseName() );
      }
    } catch ( DBDatasourceServiceException e ) {

      Logger.error( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.ERROR_0003_UNABLE_TO_POOL_DATASOURCE", connection.getName(),
          e.getMessage() ) ); //$NON-NLS-1$
    }

    return ds;

  }

  @Override
  protected ICacheManager addCacheRegions( ) {

    ICacheManager cacheManager = super.addCacheRegions();

    if ( !cacheManager.cacheEnabled( IDBDatasourceService.JDBC_POOL ) ) {
      cacheManager.addCacheRegion( IDBDatasourceService.JDBC_POOL );
    }

    return cacheManager;
  }

  @SuppressWarnings( "unchecked" )
  public void shutdown() {

    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );

    List<ObjectPool> objectPools = null;
    objectPools = (List<ObjectPool>) cacheManager.getAllValuesFromRegionCache( IDBDatasourceService.JDBC_POOL );

    Logger.debug( this, "DatasourceSystemListener: Called for shutdown ..." ); //$NON-NLS-1$

    try {
      if ( objectPools != null ) {
        for ( ObjectPool objectPool : objectPools ) {
          if ( null != objectPool ) {
            objectPool.clear();
          }
        }
      }
    } catch ( Throwable ignored ) {

      Logger.error( this, "Failed to clear connection pool: " + ignored.getMessage(), ignored ); //$NON-NLS-1$

    }

    cacheManager.removeRegionCache( IDBDatasourceService.JDBC_POOL );
    cacheManager.removeRegionCache( IDBDatasourceService.JDBC_DATASOURCE );

    Logger.debug( this, "DatasourceSystemListener: Completed shutdown." ); //$NON-NLS-1$
  }

}
