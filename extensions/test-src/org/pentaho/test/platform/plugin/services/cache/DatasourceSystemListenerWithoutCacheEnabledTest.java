/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.test.platform.plugin.services.cache;

import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

//import java.sql.Connection;
//import java.util.List;
//
//import org.apache.commons.dbcp.PoolingDataSource;
//import org.pentaho.platform.api.data.IDBDatasourceService;
//import org.pentaho.platform.api.engine.ICacheManager;
//import org.pentaho.platform.engine.core.system.PentahoSystem;
//import org.pentaho.platform.engine.core.system.SimpleMapCacheManager;
//import org.pentaho.platform.engine.core.system.StandaloneSession;
//import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;

@SuppressWarnings( "nls" )
public class DatasourceSystemListenerWithoutCacheEnabledTest extends BaseTest {

  private static final String SOLUTION_PATH = "test-src/cache-solution1";
  private static final String ALT_SOLUTION_PATH = "test-src/cache-solution1";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";
  // private static final String DEFAULT_SPRING_CONFIG_FILE_NAME = "pentahoObjects.spring.xml";
  final String SYSTEM_FOLDER = "/system";

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      return SOLUTION_PATH;
    } else {
      return ALT_SOLUTION_PATH;
    }
  }

  // public void testListener() {
  // Connection connection = null;
  // PoolingDataSource ds = null;
  // try {
  // ICacheManager simpleMapCacheManager = SimpleMapCacheManager.getInstance();
  // StandaloneSession session = new StandaloneSession("TestSession");
  // ICacheManager cacheManager = PentahoSystem.getCacheManager(null);
  // PooledDatasourceSystemListener listener = new PooledDatasourceSystemListener();
  // listener.startup(session);
  // boolean cachingAvailable = cacheManager != null && cacheManager.cacheEnabled();
  // List datasourceList = null;
  // List poolsList = null;
  // if(cachingAvailable) {
  // datasourceList = cacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_DATASOURCE);
  // poolsList = cacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_POOL);
  // } else {
  // datasourceList = simpleMapCacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_DATASOURCE);
  // poolsList = simpleMapCacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_POOL);
  // }
  // assertNotNull(datasourceList);
  // assertNotNull(poolsList);
  // assertTrue("Size is not zero", datasourceList.size() > 0);
  // assertTrue("Size is not zero", poolsList.size() > 0);
  // listener.shutdown();
  // if(cachingAvailable) {
  // datasourceList = cacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_DATASOURCE);
  // poolsList = cacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_POOL);
  // } else {
  // datasourceList = simpleMapCacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_DATASOURCE);
  // poolsList = simpleMapCacheManager.getAllValuesFromRegionCache(IDBDatasourceService.JDBC_POOL);
  // }
  // assertSame(0, datasourceList.size());
  // assertSame(0, poolsList.size());
  // } catch (Exception e) {
  // fail("Not Expected the exception to be thrown");
  // e.printStackTrace();
  // }
  // }

  public void testDummyTest() {
    // TODO: remove once tests pass
  }
}
