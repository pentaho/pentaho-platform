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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.sql.DataSource;

public class NonPooledOrJndiDatasourceService extends BaseDatasourceService {

  private static final Log log = LogFactory.getLog( NonPooledOrJndiDatasourceService.class );

  String requestedDatasourceName = null;

  @Override
  protected DataSource retrieve( String dsName ) throws DBDatasourceServiceException {
    DataSource ds = null;
    requestedDatasourceName = dsName;

    try {
      IDatasourceMgmtService datasourceMgmtSvc = getDatasourceMgmtService();
      IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName( dsName );

      if ( databaseConnection != null && !databaseConnection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {
        ds = resolveDatabaseConnection( databaseConnection );
        // Database does not have the datasource, look in jndi now
      } else if ( databaseConnection == null ) {
        ds = getJndiDataSource( dsName );
      } else {
        ds = getJndiDataSource( databaseConnection.getDatabaseName() );
      }
      // if the resulting datasource is not null then store it in the cache
      if ( ds != null ) {
        cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, ds );
      }
    } catch ( DatasourceMgmtServiceException daoe ) {
      log.debug( Messages.getInstance().getErrorString(
          "DatasourceService.DEBUG_0001_UNABLE_TO_FIND_DATASOURCE_IN_REPOSITORY",
          daoe.getLocalizedMessage() ), daoe );
      try {
        return getJndiDataSource( dsName );
      } catch ( DBDatasourceServiceException dse ) {
        throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
            "DatasourceService.ERROR_0003_UNABLE_TO_GET_JNDI_DATASOURCE" ), dse ); //$NON-NLS-1$
      }

    }
    return ds;
  }

  @Override
  protected DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection  )
    throws DBDatasourceServiceException {
    return PooledDatasourceHelper.convert( databaseConnection );
  }

}
