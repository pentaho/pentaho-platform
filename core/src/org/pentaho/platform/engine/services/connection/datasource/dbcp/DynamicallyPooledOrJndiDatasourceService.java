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
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

public class DynamicallyPooledOrJndiDatasourceService extends BaseDatasourceService {

  private static final Log logger = LogFactory.getLog( PooledOrJndiDatasourceService.class );

  private IDBDatasourceService pooledDatasourceService;
  private IDBDatasourceService nonPooledDatasourceService;

  public DynamicallyPooledOrJndiDatasourceService() {
  }
  
  @Override
  public DataSource getDataSource( String datasource ) throws DBDatasourceServiceException {

    try {

      IDatasourceMgmtService datasourceMgmtSvc =
          (IDatasourceMgmtService) PentahoSystem.get( IDatasourceMgmtService.class, PentahoSessionHolder.getSession() );

      // Look in the database for the datasource
      IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName( datasource );

      if ( databaseConnection != null ) {

        return databaseConnection.isUsingConnectionPool() ? getPooledDatasourceService().getDataSource( datasource )
            : getNonPooledDatasourceService().getDataSource( datasource );
      }

    } catch ( DatasourceMgmtServiceException daoe ) {
      daoe.printStackTrace();
      logger.info( Messages.getInstance().getErrorString(
          "DinamicallyPooledOrJndiDatasourceService.DEBUG_0001_UNABLE_TO_FIND_DATASOURCE_IN_REPOSITORY",
          daoe.getLocalizedMessage() ), daoe );
    }

    return getJndiDataSource( datasource );
  }

  public IDBDatasourceService getPooledDatasourceService() {
    return pooledDatasourceService;
  }

  public void setPooledDatasourceService( IDBDatasourceService pooledDatasourceService ) {
    this.pooledDatasourceService = pooledDatasourceService;
  }

  public IDBDatasourceService getNonPooledDatasourceService() {
    return nonPooledDatasourceService;
  }

  public void setNonPooledDatasourceService( IDBDatasourceService nonPooledDatasourceService ) {
    this.nonPooledDatasourceService = nonPooledDatasourceService;
  }
}
