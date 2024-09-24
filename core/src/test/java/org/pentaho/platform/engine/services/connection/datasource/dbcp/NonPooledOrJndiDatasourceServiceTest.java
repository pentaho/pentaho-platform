/*!
 *
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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.nullable;


public class NonPooledOrJndiDatasourceServiceTest {

  String testName = "testName";
  NonPooledOrJndiDatasourceService service;
  IDatasourceMgmtService mgmtService;
  IDatabaseConnection connection;
  ICacheManager cacheManager;
  DataSource jndiDataSource;
  DataSource databaseConnectionDataSource;


  @Before
  public void init() {
    mgmtService = mock( IDatasourceMgmtService.class );
    connection = mock( IDatabaseConnection.class );
    cacheManager = mock( ICacheManager.class );
    jndiDataSource = mock( DataSource.class );
    databaseConnectionDataSource = mock( DataSource.class );
    service = spy( getPreparedService( mgmtService, cacheManager, jndiDataSource, databaseConnectionDataSource ) );
  }

  @Test
  public void testRetrieveJNDIConnection_1() throws Exception {
    when( mgmtService.getDatasourceByName( testName ) ).thenReturn( connection );
    when( connection.getAccessType() ).thenReturn( DatabaseAccessType.JNDI );
    service.retrieve( testName );
    verify( service ).getJndiDataSource( testName );
    verify( cacheManager ).putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, testName, jndiDataSource );
  }

  @Test
  public void testRetrieveJNDIConnection_2() throws Exception {
    when( mgmtService.getDatasourceByName( testName ) ).thenReturn( null );
    service.retrieve( testName );
    verify( service ).getJndiDataSource( testName );
    verify( cacheManager ).putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, testName, jndiDataSource );
  }

  @Test
  public void testRetrieveJNDIConnection_3() throws Exception {
    service = spy( getPreparedService( mgmtService, cacheManager, null, null ) );
    when( mgmtService.getDatasourceByName( testName ) ).thenReturn( connection );
    when( connection.getAccessType() ).thenReturn( DatabaseAccessType.JNDI );
    service.retrieve( testName );
    verify( service, times( 2 ) ).getJndiDataSource( nullable( String.class ) );
    verify( cacheManager, never() ).putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, testName, jndiDataSource );
  }

  @Test
  public void testRetrieveJNDIConnection_4() throws Exception {
    service = spy( getPreparedService( mgmtService, cacheManager, null, null ) );
    when( mgmtService.getDatasourceByName( testName ) ).thenReturn( connection );
    when( service.getJndiDataSource( testName ) ).thenThrow( DBDatasourceServiceException.class ).thenCallRealMethod();
    when( connection.getAccessType() ).thenReturn( DatabaseAccessType.JNDI );
    service.retrieve( testName );
    verify( service, times( 2 ) ).getJndiDataSource( nullable( String.class ) );
    verify( cacheManager, never() ).putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, testName, jndiDataSource );
  }

  @Test
  public void testRetrieveDatabaseConnection() throws Exception {
    when( mgmtService.getDatasourceByName( testName ) ).thenReturn( connection );
    when( connection.getAccessType() ).thenReturn( DatabaseAccessType.ODBC );
    service.retrieve( testName );
    verify( service ).resolveDatabaseConnection( connection );
    verify( cacheManager ).putInRegionCache( IDBDatasourceService.JDBC_DATASOURCE, testName, databaseConnectionDataSource );
  }

  private NonPooledOrJndiDatasourceService getPreparedService( IDatasourceMgmtService mgmtService, ICacheManager iCacheManager,
                                                               DataSource jndiDataSource, DataSource databaseConnectionDataSource ) {
    return new NonPooledOrJndiDatasourceService() {
      @Override
      public IDatasourceMgmtService getDatasourceMgmtService() {
        return mgmtService;
      }

      @Override
      public ICacheManager getCacheManager() {
        return iCacheManager;
      }

      @Override
      protected DataSource getJndiDataSource( String dsName ) throws DBDatasourceServiceException {
        return jndiDataSource;
      }

      @Override
      public DataSource resolveDatabaseConnection( IDatabaseConnection databaseConnection ) throws DBDatasourceServiceException {
        return databaseConnectionDataSource;
      }
    };
  }
}
