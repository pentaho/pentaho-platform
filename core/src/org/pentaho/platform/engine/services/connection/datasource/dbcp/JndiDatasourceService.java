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

import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.engine.services.messages.Messages;

import javax.sql.DataSource;

public class JndiDatasourceService extends BaseDatasourceService {

  @Override
  protected DataSource retrieve( String dsName ) throws DBDatasourceServiceException {

    DataSource ds = null;
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
