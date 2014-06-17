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

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.util.logging.Logger;

public class DynamicallyPooledDatasourceSystemListener extends PooledDatasourceSystemListener {

  private IDBDatasourceService datasourceService;
  public IDBDatasourceService getDatasourceService() {
    if( datasourceService == null ){
      datasourceService = PentahoSystem.get(
        IDBDatasourceService.class, null );
    }
    return datasourceService;
  }


  @Override
  protected DataSource getDataSource( IDatabaseConnection connection ) {

    DataSource ds = null;
    try {
      ds = connection.isUsingConnectionPool() ? PooledDatasourceHelper
          .setupPooledDataSource( connection ) : getDatasourceService().getDataSource( connection.getName() );;

    } catch ( DBDatasourceServiceException e ) {

      Logger.error( this, Messages.getInstance().getErrorString(
          "DatasourceSystemListener.ERROR_0003_UNABLE_TO_POOL_DATASOURCE", connection.getName(),
          e.getMessage() ) ); //$NON-NLS-1$

    }

    return ds;

  }

}
