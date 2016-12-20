/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatcher;

public class SQLConnectionTest {
  private Connection nativeConnection = mock( Connection.class );
  private IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class );
  private IDBDatasourceService datasourceService = mock( IDBDatasourceService.class );
  private DataSource dataSource = mock( DataSource.class );

  @Before
  public void setUp() throws  ObjectFactoryException, DBDatasourceServiceException, SQLException {
    doReturn( dataSource ).when( datasourceService ).getDataSource( anyString() );
    doReturn( nativeConnection ).when( dataSource ).getConnection();
    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
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

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {

    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }

  @Test
  public void testConnect() throws Exception {
    SQLConnection sqlc = spy( new SQLConnection() );
    Properties props = new Properties();

    props = new Properties();
    props.put( IPentahoConnection.JNDI_NAME_KEY, "test" );
    assertTrue( "JNDI Test", sqlc.connect( props ) );

    props = new Properties();
    doNothing().when( sqlc ).close();
    doNothing().when( sqlc ).init( anyString(), anyString(), anyString(), anyString() );
    assertTrue( "NonPool Test", sqlc.connect( props ) );

    doNothing().when( sqlc ).initDataSource( any( IDatabaseConnection.class ) );

    props.put( IPentahoConnection.CONNECTION_NAME, "test" );
    assertTrue( "Pool Test", sqlc.connect( props ) );
  }
}
