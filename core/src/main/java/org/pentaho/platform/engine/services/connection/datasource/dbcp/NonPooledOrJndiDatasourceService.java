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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.sql.DataSource;

public class NonPooledOrJndiDatasourceService extends BaseDatasourceService {

  private static final Log log = LogFactory.getLog( NonPooledOrJndiDatasourceService.class );

  @Override
  protected DataSource retrieve( String dsName ) throws DBDatasourceServiceException {
    DataSource ds = null;

    try {
      IDatasourceMgmtService datasourceMgmtSvc = getDatasourceMgmtService();
      IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName( dsName );

      if ( databaseConnection != null && !databaseConnection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {
        ds = resolveDatabaseConnection( databaseConnection );
        // Database does not have the datasource, look in jndi now
      } else {
        try {
          ds = getJndiDataSource( dsName );
        } catch ( DBDatasourceServiceException e ) {
          //Ignore, Maybe jndi name is specified as database name in the connection
        }
      }
      if ( ds == null && databaseConnection != null ) {
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
  public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection  )
    throws DBDatasourceServiceException {
    return PooledDatasourceHelper.convert( databaseConnection );
  }

}
