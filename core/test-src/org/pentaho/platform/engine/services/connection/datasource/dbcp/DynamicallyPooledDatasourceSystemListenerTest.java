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
 *
 * Copyright 2006 - 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import javax.sql.DataSource;

import org.junit.Test;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.engine.services.MockDataSourceService;

public class DynamicallyPooledDatasourceSystemListenerTest {

  private static final String CONNECTION_NAME = "TEST CONNECTION";

  @Test
  public void testGetDataSource() {
    IDatabaseConnection connection = mock( IDatabaseConnection.class );
    when( connection.getName() ).thenReturn( CONNECTION_NAME );

    DynamicallyPooledDatasourceSystemListener listener = spy( new DynamicallyPooledDatasourceSystemListener() );
    when( listener.getDatasourceService() ).thenReturn( new MockDataSourceService( false ) );
    DataSource ds = listener.getDataSource( connection );
    assertNotNull( ds );
  }

}
