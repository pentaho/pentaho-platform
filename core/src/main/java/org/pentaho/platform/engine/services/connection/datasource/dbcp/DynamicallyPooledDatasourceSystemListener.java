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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class DynamicallyPooledDatasourceSystemListener extends PooledDatasourceSystemListener {

  private IDBDatasourceService datasourceService;

  public IDBDatasourceService getDatasourceService() {
    if ( datasourceService == null ) {
      datasourceService = PentahoSystem.get( IDBDatasourceService.class, null );
    }
    return datasourceService;
  }

  @Override
  protected DataSource getDataSource( IDatabaseConnection connection ) {
    DataSource ds = null;
    try {
      ds = getDatasourceService().getDataSource( connection.getName() );
    } catch ( DBDatasourceServiceException | DriverNotInitializedException e ) {
      Logger.error( this, Messages.getInstance()
          .getErrorString( "DatasourceSystemListener.ERROR_0003_UNABLE_TO_POOL_DATASOURCE", connection.getName(), e.getMessage() ) ); //$NON-NLS-1$
    }
    return ds;
  }
}
