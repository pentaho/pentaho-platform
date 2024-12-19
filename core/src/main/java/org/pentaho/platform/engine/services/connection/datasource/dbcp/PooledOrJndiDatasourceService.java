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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.kettle.PoolingManagedDataSource;

import javax.sql.DataSource;

public class PooledOrJndiDatasourceService extends NonPooledOrJndiDatasourceService {

    @Override
    public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
            throws DBDatasourceServiceException {
        synchronized (this) {
            ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
            Object fromCache = cacheManager.getFromRegionCache( IDBDatasourceService.JDBC_DATASOURCE, databaseConnection.getName() );
            if ( !( fromCache instanceof PoolingManagedDataSource ) ) {
                cacheManager.removeFromRegionCache( IDBDatasourceService.JDBC_DATASOURCE, databaseConnection.getName() );
            } else {
                PoolingManagedDataSource cachedDataSource = (PoolingManagedDataSource) fromCache;
                if ( !cachedDataSource.isExpired() ) {
                    // Check expired
                    if ( cachedDataSource.hasSameConfig( databaseConnection.calculateHash() ) ) {
                        return cachedDataSource;
                    }
                    if ( cachedDataSource.isInUse() ) {
                        cachedDataSource.expire();
                    }
                    cacheManager.removeFromRegionCache( IDBDatasourceService.JDBC_DATASOURCE, databaseConnection.getName() );
                }
            }
            return PooledDatasourceHelper.setupPooledDataSource( databaseConnection );
        }
    }

}
