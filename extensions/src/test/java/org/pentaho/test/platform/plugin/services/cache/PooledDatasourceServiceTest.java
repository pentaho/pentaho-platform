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

import junit.framework.TestCase;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceService;

import javax.sql.DataSource;
import java.sql.Connection;

@SuppressWarnings( "nls" )
public class PooledDatasourceServiceTest extends TestCase {

  public void setUp() {
    // super.setUp();
    //  StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(TestSettings.SOLUTION_PATH, ""); //$NON-NLS-1$
    // PentahoSystem.init(applicationContext, getRequiredListeners());

  }

  public void testConnectionPoolWhenExhausted() {
    // startTest();
    //   info("Testing Connecton Pooling"); //$NON-NLS-1$
    Connection connection = null;
    try {
      PooledDatasourceService service = new PooledDatasourceService();
      DataSource ds = service.getDataSource( "SampleData" );
      for ( int i = 0; i < 10; i++ ) {
        connection = ds.getConnection();
        System.out.println( "Got the " + ( i + 1 ) + " Connection" );
      }
      fail( "Not expected to reach here" );
    } catch ( Exception e ) {
      assertTrue( "Expected the exception to be thrown", true );
      e.printStackTrace();
    } finally {
      try {
        connection.close();
      } catch ( Exception ee ) {
        ee.printStackTrace();
      }
    }

  }

}
