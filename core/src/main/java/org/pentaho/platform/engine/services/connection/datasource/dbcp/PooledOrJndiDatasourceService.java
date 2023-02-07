/*!
 *
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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

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
