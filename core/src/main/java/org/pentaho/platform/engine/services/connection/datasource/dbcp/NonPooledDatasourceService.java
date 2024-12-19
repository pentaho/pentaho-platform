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
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
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
  public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection )
    throws DBDatasourceServiceException {
    return PooledDatasourceHelper.convert( databaseConnection );
  }


}
