/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.connections.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatcher;

public class SQLConnectionTest {
  private Connection nativeConnection = mock( Connection.class );
  private IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class );
  private IDBDatasourceService datasourceService = mock( IDBDatasourceService.class );
  private DataSource dataSource = mock( DataSource.class );

  @Before
  public void setUp() throws  ObjectFactoryException, DBDatasourceServiceException, SQLException {
    doReturn( dataSource ).when( datasourceService ).getDataSource( nullable( String.class ) );
    doReturn( nativeConnection ).when( dataSource ).getConnection();
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), nullable( String.class ), nullable( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IDBDatasourceService.class ) ) {
              return datasourceService;
            }
            return null;
          }
        } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.clearObjectFactory();
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher implements ArgumentMatcher<Class<?>> {

    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }

  @Test
  public void testConnect() {
    SQLConnection sqlc = spy( new SQLConnection() );
    Properties props = new Properties();

    props = new Properties();
    props.put( IPentahoConnection.JNDI_NAME_KEY, "test" );
    assertTrue( "JNDI Test", sqlc.connect( props ) );

    props = new Properties();
    doNothing().when( sqlc ).close();
    doNothing().when( sqlc ).init( nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ) );
    assertTrue( "NonPool Test", sqlc.connect( props ) );

    doNothing().when( sqlc ).initDataSource( nullable( IDatabaseConnection.class ) );

    props.put( IPentahoConnection.CONNECTION_NAME, "test" );
    assertTrue( "Pool Test", sqlc.connect( props ) );
  }
}
