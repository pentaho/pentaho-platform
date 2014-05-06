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
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.sql.DataSource;

public class NonPooledDatasourceService extends BaseDatasourceService {

  @Override
  protected DataSource retrieve( String dsName ) throws DBDatasourceServiceException {
    DataSource ds = null;
    try {
      IDatasourceMgmtService datasourceMgmtSvc = getDatasourceMgmtService();
      IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName( dsName );
      if ( databaseConnection != null ) {
        ds = resolveDatabaseConnection( databaseConnection );
        if ( ds != null ) {
          cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, ds );
        }
      } else {
        throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
            "DatasourceService.ERROR_0002_UNABLE_TO_GET_DATASOURCE" ) ); //$NON-NLS-1$
      }
    } catch ( DatasourceMgmtServiceException daoe ) {
      throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
          "DatasourceService.ERROR_0002_UNABLE_TO_GET_DATASOURCE" ), daoe ); //$NON-NLS-1$
    }
    return ds;
  }


  @Override
  public String getDSBoundName( final String dsName ) throws DBDatasourceServiceException {
    return dsName;
  }

  @Override
  public String getDSUnboundName( final String dsName ) {
    return dsName;
  }

  @Override
  protected DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    return PooledDatasourceHelper.convert( databaseConnection );
  }


}
