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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;

public class DynamicallyPooledOrJndiDatasourceService extends NonPooledOrJndiDatasourceService {

  private IDBDatasourceService pooledDatasourceService;
  private IDBDatasourceService nonPooledDatasourceService;

  public DynamicallyPooledOrJndiDatasourceService() {
  }

  @Override
  protected DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    return databaseConnection.isUsingConnectionPool()
          ? getPooledDatasourceService().getDataSource( requestedDatasourceName )
          : getNonPooledDatasourceService().getDataSource( requestedDatasourceName );
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
