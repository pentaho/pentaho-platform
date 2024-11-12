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
  public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    return databaseConnection.isUsingConnectionPool()
          ? getPooledDatasourceService().resolveDatabaseConnection( databaseConnection )
          : getNonPooledDatasourceService().getDataSource( databaseConnection.getName() );
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
