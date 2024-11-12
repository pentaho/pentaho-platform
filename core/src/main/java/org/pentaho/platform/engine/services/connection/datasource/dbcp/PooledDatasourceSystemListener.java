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


package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.apache.commons.pool2.ObjectPool;
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
