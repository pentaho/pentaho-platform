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
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    // Create a spy of SQLConnection to verify method calls
    SQLConnection sqlc = spy( new SQLConnection() );

    // Mock the logger to avoid actual logging
    ILogger logger = mock( ILogger.class );
    doNothing().when( logger ).error( anyString(), any( Throwable.class ) );
    sqlc.logger = logger;

    // Test connection using JNDI name
    Properties props = new Properties();
    props.put( IPentahoConnection.JNDI_NAME_KEY, "test" );
    assertTrue( "JNDI Test", sqlc.connect( props ) );
    verify( sqlc ).initWithJNDI( "test" );
    assertTrue( sqlc.initialized() );

    // Test connection without JNDI name
    props = new Properties();
    doNothing().when( sqlc ).close();
    doNothing().when( sqlc ).init( nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ) );
    assertTrue( "NonPool Test", sqlc.connect( props ) );
    verify( sqlc ).init( nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ) );
    assertTrue( sqlc.initialized() );

    // Test connection with a mock IDatabaseConnection
    IDatabaseConnection mockDatabaseConnection = mock( IDatabaseConnection.class );
    doNothing().when( sqlc ).initDataSource( eq( mockDatabaseConnection ), eq( false ) );

    // Initialize the data source and test connection
    props.put( IPentahoConnection.CONNECTION_NAME, "test" );
    sqlc.initDataSource( mockDatabaseConnection, false );
    assertTrue( "Pool Test", sqlc.connect( props ) );
    verify( sqlc ).initDataSource( eq( mockDatabaseConnection ), eq( false ) );
    assertTrue( sqlc.initialized() );
  }
}
