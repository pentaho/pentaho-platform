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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *  
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;

public class NonPooledOrJndiDatasourceService extends BaseDatasourceService {
  /**
   * Since JNDI is supported different ways in different app servers, it's
   * nearly impossible to have a ubiquitous way to look up a datasource. This
   * method is intended to hide all the lookups that may be required to find a
   * jndi name.
   * 
   * @param dsName
   *            The Datasource name
   * @return DataSource if there is one bound in JNDI
   * @throws NamingException
   */
  public DataSource getDataSource(String dsName) throws DBDatasourceServiceException {
    DataSource dataSource = null;
    Object foundDs = null;
    if(!cacheManager.cacheEnabled(IDBDatasourceService.JDBC_DATASOURCE)) {
        cacheManager.addCacheRegion(IDBDatasourceService.JDBC_DATASOURCE);
      }
      foundDs = cacheManager.getFromRegionCache(IDBDatasourceService.JDBC_DATASOURCE,dsName);
    if (foundDs != null) {
      return (DataSource) foundDs;
    }
    try {
      IDatasourceMgmtService datasourceMgmtSvc = (IDatasourceMgmtService) PentahoSystem.get(IDatasourceMgmtService.class,PentahoSessionHolder.getSession()); 
      IDatabaseConnection databaseConnection = datasourceMgmtSvc.getDatasourceByName(dsName);
      // Look in the database for the datasource
      if(databaseConnection != null) {
        dataSource = PooledDatasourceHelper.convert(databaseConnection);
        cacheManager.putInRegionCache(IDBDatasourceService.JDBC_DATASOURCE,dsName, (DataSource) dataSource);  
      } else {
        // Database does not have the datasource, look in jndi now
        dataSource = getJndiDataSource(dsName);
      }
    } catch (DatasourceMgmtServiceException daoe) {
      try {
        return getJndiDataSource(dsName);  
      }
      catch(DBDatasourceServiceException dse) {
       throw new DBDatasourceServiceException(Messages.getInstance().getErrorString("NonPooledOrJndiDatasourceService.ERROR_0003_UNABLE_TO_GET_JNDI_DATASOURCE") ,dse);  //$NON-NLS-1$
      }

    }
    return dataSource;
  }
}
