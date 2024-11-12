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
import org.pentaho.platform.engine.services.connection.datasource.dbcp.NonPooledDatasourceService;

import javax.sql.DataSource;
import java.sql.Connection;

@SuppressWarnings( "nls" )
public class NonPooledDatasourceServiceTest extends TestCase {

  public void testGetDatasource() {
    Connection connection = null;
    try {
      NonPooledDatasourceService service = new NonPooledDatasourceService();
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
