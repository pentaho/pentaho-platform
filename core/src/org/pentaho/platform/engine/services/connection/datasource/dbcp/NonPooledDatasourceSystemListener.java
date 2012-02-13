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
 * Copyright 2006 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Jul 07, 2008 
 * @author rmansoor
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.logging.Logger;

public class NonPooledDatasourceSystemListener implements IPentahoSystemListener {
  public static final String DATASOURCE_REGION = "DATASOURCE";//$NON-NLS-1$
  public boolean startup(final IPentahoSession session) {
    /*try {*/
      ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
      Logger.debug(this, "NonPooledDatasourceSystemListener: called for startup"); //$NON-NLS-1$
      boolean cachingAvailable = cacheManager != null && cacheManager.cacheEnabled();
      /*IDatasourceMgmtService datasourceMgmtSvc = (IDatasourceMgmtService) PentahoSystem.get(IDatasourceMgmtService.class,session);
      if(cachingAvailable) {
        if(!cacheManager.cacheEnabled(IDBDatasourceService.JDBC_DATASOURCE)) {
          cacheManager.addCacheRegion(IDBDatasourceService.JDBC_DATASOURCE);
        }
      }
      List<DatabaseMeta> datasources = datasourceMgmtSvc.getDatasources();
      for (DatabaseMeta datasource : datasources) {
        Logger.debug(this, "(storing DataSource under key \"" + IDBDatasourceService.JDBC_DATASOURCE //$NON-NLS-1$
            + datasource.getName() + "\")"); //$NON-NLS-1$
        cacheManager.putInRegionCache(IDBDatasourceService.JDBC_DATASOURCE, datasource.getName(), PooledDatasourceHelper.convert(datasource));
       }
      Logger.debug(this, "NonPooledDatasourceSystemListener: done with init"); //$NON-NLS-1$*/
      return true;
    /*} catch (DatasourceMgmtServiceException dmse) {
      Logger.error(this, Messages.getInstance().getErrorString(
          "NonPooledDatasourceSystemListener.ERROR_0002_UNABLE_TO_GET_DATASOURCE",NonPooledDatasourceSystemListener.class.getName()), dmse); //$NON-NLS-1$
      return false;        
    }*/
  }

  public void shutdown() {
    ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
    Logger.debug(this, "NonPooledDatasourceSystemListener: called for shutdown"); //$NON-NLS-1$
    // Cleaning cache for datasources
    cacheManager.removeRegionCache(IDBDatasourceService.JDBC_DATASOURCE);      
    
    Logger.debug(this, "NonPooledDatasourceSystemListener: completed shutdown"); //$NON-NLS-1$
    return;
  }

}
