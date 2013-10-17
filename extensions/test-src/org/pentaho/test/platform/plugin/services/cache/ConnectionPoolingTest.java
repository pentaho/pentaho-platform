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

import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceSystemListener;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

//import java.sql.Connection;
//
//import javax.sql.DataSource;
//import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceService;

@SuppressWarnings( "nls" )
public class ConnectionPoolingTest extends BaseTest {
  PooledDatasourceSystemListener listener;
  StandaloneSession session;

  private static final String SOLUTION_PATH = "test-src/cache-solution";
  private static final String ALT_SOLUTION_PATH = "test-src/cache-solution";
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
