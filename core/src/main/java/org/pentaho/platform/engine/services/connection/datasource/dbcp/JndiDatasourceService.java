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

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.data.IJndiDatasourceService;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.sql.DataSource;

public class JndiDatasourceService extends BaseDatasourceService implements IJndiDatasourceService {

  @Override
  protected DataSource retrieve( String dsName ) throws DBDatasourceServiceException {
    DataSource ds;
    try {
      ds =  getJndiDataSource( dsName );
      if ( ds != null ) {
        cacheManager.putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, dsName, ds );
      }
    } catch ( DBDatasourceServiceException dse ) {
      throw new DBDatasourceServiceException( Messages.getInstance().getErrorString(
          "DatasourceService.ERROR_0003_UNABLE_TO_GET_JNDI_DATASOURCE" ), dse ); //$NON-NLS-1$
    }

    return ds;
  }

}
