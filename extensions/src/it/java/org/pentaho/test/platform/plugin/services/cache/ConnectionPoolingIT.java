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

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.pentaho.test.platform.utils.TestResourceLocation;

import java.io.File;

//import java.sql.Connection;
//
//import javax.sql.DataSource;
//import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceService;

@SuppressWarnings( "nls" )
public class ConnectionPoolingIT extends BaseTest {
  PooledDatasourceSystemListener listener;
  StandaloneSession session;

  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution";
  private static final String ALT_SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/cache-solution";
  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml";

  @Override
  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH );
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH );
      return ALT_SOLUTION_PATH;
    }
  }

  public void testDummyTest() {
    // TODO: remove once tests pass
  }

}
