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

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.logging.Logger;

public class PooledDatasourceHelper {

  public static PoolingDataSource setupPooledDataSource(IDatasource datasource) throws DatasourceServiceException{
	 try {
	    PoolingDataSource poolingDataSource = null;
	    ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
	    // Read default connecion pooling parameter
      String maxdleConn = PentahoSystem.getSystemSetting("dbcp-defaults/max-idle-conn", null);  //$NON-NLS-1$ 
      String minIdleConn = PentahoSystem.getSystemSetting("dbcp-defaults/min-idle-conn", null);  //$NON-NLS-1$    
	    String maxActConn = PentahoSystem.getSystemSetting("dbcp-defaults/max-act-conn", null);  //$NON-NLS-1$
	    String numIdleConn = PentahoSystem.getSystemSetting("dbcp-defaults/num-idle-conn", null);  //$NON-NLS-1$
	    String validQuery = null;
	    String whenExhaustedAction = PentahoSystem.getSystemSetting("dbcp-defaults/when-exhausted-action", null);  //$NON-NLS-1$
	    String wait = PentahoSystem.getSystemSetting("dbcp-defaults/wait", null);  //$NON-NLS-1$
	    String testWhileIdleValue = PentahoSystem.getSystemSetting("dbcp-defaults/test-while-idle", null);  //$NON-NLS-1$
	    String testOnBorrowValue = PentahoSystem.getSystemSetting("dbcp-defaults/test-on-borrow", null);  //$NON-NLS-1$
	    String testOnReturnValue = PentahoSystem.getSystemSetting("dbcp-defaults/test-on-return", null);  //$NON-NLS-1$
	    boolean testWhileIdle = !StringUtil.isEmpty(testWhileIdleValue) ? Boolean.parseBoolean(testWhileIdleValue) : false;
	    boolean testOnBorrow = !StringUtil.isEmpty(testOnBorrowValue) ? Boolean.parseBoolean(testOnBorrowValue) : false;
	    boolean testOnReturn = !StringUtil.isEmpty(testOnReturnValue) ? Boolean.parseBoolean(testOnReturnValue) : false;
	    int maxActiveConnection = -1;
	    int numIdleConnection = -1;
	    long waitTime = -1;
	    byte whenExhaustedActionType = -1;
	    int minIdleConnection =  !StringUtil.isEmpty(minIdleConn) ? Integer.parseInt(minIdleConn) : -1;
	    int maxIdleConnection =  !StringUtil.isEmpty(maxdleConn) ? Integer.parseInt(maxdleConn) : -1;

	    if(datasource.getMaxActConn() >0) {
	      maxActiveConnection = datasource.getMaxActConn();
	    } else  {
	      if(!StringUtil.isEmpty(maxActConn)) {
	         maxActiveConnection = Integer.parseInt(maxActConn);
	      }
	    }
      if(datasource.getIdleConn() >0) {
        numIdleConnection = datasource.getIdleConn();
      } else  {
        if(!StringUtil.isEmpty(numIdleConn)) {
          numIdleConnection = Integer.parseInt(numIdleConn);
        }
      }
      if(datasource.getWait() >0) {
        waitTime = datasource.getWait();
      } else  {
        if(!StringUtil.isEmpty(wait)) {
          waitTime = Long.parseLong(wait);
        }
      }
      if(!StringUtil.isEmpty(datasource.getQuery())) {
        validQuery = datasource.getQuery();
      }
      if(!StringUtil.isEmpty(whenExhaustedAction)) {
        whenExhaustedActionType = Byte.parseByte(whenExhaustedAction);
      } else {
        whenExhaustedActionType = GenericObjectPool.WHEN_EXHAUSTED_BLOCK;
      }
	    poolingDataSource = new PoolingDataSource();
	    Class.forName(datasource.getDriverClass());
	    // As the name says, this is a generic pool; it returns  basic Object-class objects.
	    final GenericObjectPool pool = new GenericObjectPool(null);
      pool.setWhenExhaustedAction(whenExhaustedActionType);  
      
      // Tuning the connection pool
      pool.setMaxActive(maxActiveConnection);
      pool.setMaxIdle(maxIdleConnection);
      pool.setMaxWait(waitTime);
      pool.setMinIdle(minIdleConnection);
      pool.setTestWhileIdle(testWhileIdle);
      pool.setTestOnReturn(testOnReturn);
      pool.setTestOnBorrow(testOnBorrow);
      pool.setTestWhileIdle(testWhileIdle);
	    /*
	    ConnectionFactory creates connections on behalf of the pool.
	    Here, we use the DriverManagerConnectionFactory because that essentially
	    uses DriverManager as the source of connections.
	    */
	    ConnectionFactory factory = new DriverManagerConnectionFactory(datasource.getUrl(), datasource.getUserName(),
	        datasource.getPassword());

	    /*
	    Puts pool-specific wrappers on factory connections.  For clarification:
	    "[PoolableConnection]Factory," not "Poolable[ConnectionFactory]."
	    */
      PoolableConnectionFactory pcf = new PoolableConnectionFactory(factory, // ConnectionFactory
	        pool, // ObjectPool
	        null, // KeyedObjectPoolFactory
	        validQuery, // String (validation query)
	        false, // boolean (default to read-only?)
	        true // boolean (default to auto-commit statements?)
	    );

	    /*
	    initialize the pool to X connections
	    */
	    Logger.debug(PooledDatasourceHelper.class, "Pool defaults to " + maxActiveConnection + " max active/" + maxIdleConnection + "max idle"  + "with " + waitTime + "wait time"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	        + " idle connections."); //$NON-NLS-1$
	    
	     for (int i = 0; i < maxIdleConnection; ++i) {
	      pool.addObject();
	     }
	     Logger.debug(PooledDatasourceHelper.class, "Pool now has " + pool.getNumActive() + " active/" + pool.getNumIdle() + " idle connections."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	    /*
	    All of this is wrapped in a DataSource, which client code should
	    already know how to handle (since it's the same class of object
	    they'd fetch via the container's JNDI tree
	    */
	    poolingDataSource.setPool(pool);

	    // store the pool, so we can get to it later
      cacheManager.putInRegionCache(IDatasourceService.JDBC_POOL, datasource.getName(), pool);
	    return (poolingDataSource);
	  } catch(Exception e) {
	      throw new DatasourceServiceException(e);
	  }
  }
}
