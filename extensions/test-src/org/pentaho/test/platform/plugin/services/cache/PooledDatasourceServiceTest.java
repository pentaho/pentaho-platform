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
