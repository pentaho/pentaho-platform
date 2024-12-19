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


package org.pentaho.test.platform.plugin.services.cache;

import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

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
public class DatasourceSystemListenerWithoutCacheManagerInstantiatedIT extends BaseTest {

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution2";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution2";
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
