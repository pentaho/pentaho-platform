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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

import javax.sql.DataSource;
import java.util.List;

public class NonPooledDatasourceSystemListener implements IPentahoSystemListener {

  public boolean startup( final IPentahoSession session ) {
    try {

      Logger.debug( this, "DatasourceSystemListener: called for startup ..." ); //$NON-NLS-1$

      ICacheManager cacheManager = addCacheRegions();

      IDatasourceMgmtService datasourceMgmtSvc =
          (IDatasourceMgmtService) PentahoSystem.getObjectFactory().get( IDatasourceMgmtService.class, session );

      List<IDatabaseConnection> databaseConnections = datasourceMgmtSvc.getDatasources();

      String dsName = "";
      DataSource ds = null;

      for ( IDatabaseConnection databaseConnection : databaseConnections ) {

        if ( databaseConnection != null ) {

          Logger.debug( this, "  Setting up datasource - " + databaseConnection ); //$NON-NLS-1$

          try {
            ds = getDataSource( databaseConnection );
          } catch ( DBDatasourceServiceException e ) {
            Logger.error(this, "Error retrieving DataSource", e );
            continue;
          }
          dsName = databaseConnection.getName();

          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, ds );

          Logger.debug( this, "(Storing datasource under key \"" + IDBDatasourceService.JDBC_DATASOURCE //$NON-NLS-1$
              + dsName + "\")" ); //$NON-NLS-1$
        }

      }

      Logger.debug( this, "DatasourceSystemListener: Completed startup." ); //$NON-NLS-1$

      return true;

    } catch ( ObjectFactoryException objface ) {

      Logger.error( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.ERROR_0001_UNABLE_TO_INSTANTIATE_OBJECT" ), objface ); //$NON-NLS-1$

      return false;

    } catch ( DatasourceMgmtServiceException dmse ) {

      Logger.error( this, Messages.getInstance().getErrorString(
        "DatasourceSystemListener.ERROR_0002_UNABLE_TO_GET_DATASOURCE" ), dmse ); //$NON-NLS-1$

      return false;
    }
  }

  protected DataSource getDataSource( IDatabaseConnection connection ) throws DBDatasourceServiceException {

    return PooledDatasourceHelper.convert( connection );

  }

  protected ICacheManager addCacheRegions( ) {

    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );

    Logger.debug( this, "Adding caching regions ..." ); //$NON-NLS-1$

    if ( !cacheManager.cacheEnabled( IDBDatasourceService.JDBC_DATASOURCE ) ) {
      cacheManager.addCacheRegion( IDBDatasourceService.JDBC_DATASOURCE );
    }

    return cacheManager;
  }


  public void shutdown() {

    ICacheManager cacheManager = PentahoSystem.getCacheManager( null );

    Logger.debug( this, "DatasourceSystemListener: Called for shutdown ..." ); //$NON-NLS-1$

    cacheManager.removeRegionCache( IDBDatasourceService.JDBC_DATASOURCE );

    Logger.debug( this, "DatasourceSystemListener: Completed shutdown." ); //$NON-NLS-1$

  }

}
