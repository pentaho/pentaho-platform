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


package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;

/**
 * NonPooledDatasourceSystemListener tests
 * 
 * @author Yury Bakhmutski
 * @see NonPooledDatasourceSystemListener
 */
public class NonPooledDatasourceSystemListenerTest {

  @Mock
  NonPooledDatasourceSystemListener nonPooledDatasourceSystemListenerSpy;
  @Mock
  ICacheManager ICacheManagerMock;

  @Before
  public void init() {
    MockitoAnnotations.initMocks( this );
  }

  @Test
  public void testStartupIsSetupDataSourceForConnectionCalled() throws ObjectFactoryException,
    DatasourceMgmtServiceException {

    stubGetListOfDatabaseConnectionsMethod( DatabaseAccessType.NATIVE );
    stubIsPortUsedByServerMethod( false );
    // to avoid NullPointerException
    stubAddCacheRegionsMethod();

    // call real methods
    callRealSetupDataSourceForConnection();
    callRealStartup();

    // calling testing method
    callStartup();

    // check if setupDataSourceForConnection was called
    isSetupDataSourceForConnectionWasCalled( 1 );
  }


  @Test
  public void testStartupJNDINotCalledIsPortUsedByServer() throws ObjectFactoryException,
    DatasourceMgmtServiceException {

    stubGetListOfDatabaseConnectionsMethod( DatabaseAccessType.JNDI );
    stubIsPortUsedByServerMethod( false );
    // to avoid NullPointerException
    stubAddCacheRegionsMethod();

    // call real methods
    callRealSetupDataSourceForConnection();
    callRealStartup();

    // calling testing method
    callStartup();

    // check isPortUsedByServer wasn't called
    verify( nonPooledDatasourceSystemListenerSpy, never() ).isPortUsedByServer( any( IDatabaseConnection.class ) );
  }

  @Test
  public void testStartupIsSetupDataSourceForConnectionNotCalled() throws ObjectFactoryException,
    DatasourceMgmtServiceException {

    stubGetListOfDatabaseConnectionsMethod( DatabaseAccessType.NATIVE );
    stubIsPortUsedByServerMethod( true );
    // to avoid NullPointerException
    stubAddCacheRegionsMethod();

    // call real methods
    callRealSetupDataSourceForConnection();
    callRealStartup();

    // calling testing method
    callStartup();

    // check if setupDataSourceForConnection wasn't called
    isSetupDataSourceForConnectionWasCalled( 0 );
  }

  @Test
  public void testStartupIsSetupDataSourceForConnectionCalledForNativeNotForJNDI() throws ObjectFactoryException,
      DatasourceMgmtServiceException {

    stubGetListOfDatabaseConnectionsMethod( DatabaseAccessType.NATIVE, DatabaseAccessType.JNDI );
    stubIsPortUsedByServerMethod( false );
    // to avoid NullPointerException
    stubAddCacheRegionsMethod();

    // call real methods
    callRealSetupDataSourceForConnection();
    callRealStartup();

    // calling testing method
    callStartup();

    // check if setupDataSourceForConnection was called 1 time for the non-jndi ds
    isSetupDataSourceForConnectionWasCalled( 1 );

    // check that isPortUsedByServer was called for 1 time for the non-jndi ds
    isPortUsedByServerCalled( 1 );

    // check how putInRegionCache was called was called 1 time per data source for the non-jndi ds
    putInRegionCacheWasCalled( 1 );
  }

  private void stubIsPortUsedByServerMethod( boolean returnedValue ) {
    when( nonPooledDatasourceSystemListenerSpy.isPortUsedByServer( any( IDatabaseConnection.class ) ) ).thenReturn(
        returnedValue );
  }

  private void stubAddCacheRegionsMethod() {
    when( nonPooledDatasourceSystemListenerSpy.addCacheRegions() ).thenReturn( ICacheManagerMock );
  }

  private void stubGetListOfDatabaseConnectionsMethod( DatabaseAccessType... databaseAccessTypes )
    throws ObjectFactoryException, DatasourceMgmtServiceException {

    List<IDatabaseConnection> databaseConnections = new LinkedList<>();
    for ( DatabaseAccessType databaseAccessType : databaseAccessTypes ) {
      IDatabaseConnection connection = mock( DatabaseConnection.class );
      when( connection.getAccessType() ).thenReturn( databaseAccessType );
      when( connection.getName() ).thenReturn( "" );
      databaseConnections.add( connection );
    }

    when( nonPooledDatasourceSystemListenerSpy.getListOfDatabaseConnections( any( IPentahoSession.class ) ) )
        .thenReturn( databaseConnections );
  }

  private void callStartup() {
    nonPooledDatasourceSystemListenerSpy.startup( mock( IPentahoSession.class ) );
  }

  private void callRealStartup() {
    doCallRealMethod().when( nonPooledDatasourceSystemListenerSpy ).startup( any( IPentahoSession.class ) );
  }

  private void callRealSetupDataSourceForConnection() {
    doCallRealMethod().when( nonPooledDatasourceSystemListenerSpy ).setupDataSourceForConnection(
        any( IDatabaseConnection.class ) );
  }

  private void isSetupDataSourceForConnectionWasCalled( int wishedTimes ) {
    switch ( wishedTimes ) {
      case 0:
        verify( nonPooledDatasourceSystemListenerSpy, never() ).setupDataSourceForConnection(
            any( IDatabaseConnection.class ) );
        break;
      case 1:
        verify( nonPooledDatasourceSystemListenerSpy ).setupDataSourceForConnection( any( IDatabaseConnection.class ) );
        break;
      default:
        verify( nonPooledDatasourceSystemListenerSpy, times( wishedTimes ) ).setupDataSourceForConnection(
            any( IDatabaseConnection.class ) );
        break;
    }
  }

  private void putInRegionCacheWasCalled( int wishedTimes ) {
    verify( ICacheManagerMock, times( wishedTimes ) ).putInRegionCache( eq( IDBDatasourceService.JDBC_DATASOURCE ), nullable( String.class ),
        any() );
  }

  private void isPortUsedByServerCalled( int wishedTimes ) {
    verify( nonPooledDatasourceSystemListenerSpy, times( wishedTimes ) ).isPortUsedByServer( any( IDatabaseConnection.class ) );
  }
}
