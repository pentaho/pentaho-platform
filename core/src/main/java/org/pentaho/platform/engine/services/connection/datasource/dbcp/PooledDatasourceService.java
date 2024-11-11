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
import org.pentaho.platform.api.data.IPooledDatasourceService;

import javax.sql.DataSource;

public class PooledDatasourceService extends NonPooledDatasourceService implements IPooledDatasourceService {

  @Override
  public String getDSBoundName( final String dsName ) throws DBDatasourceServiceException {
    return dsName;
  }

  @Override
  public String getDSUnboundName( final String dsName ) {
    return dsName;
  }

  @Override
  public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    return PooledDatasourceHelper.setupPooledDataSource( databaseConnection );
  }
}
