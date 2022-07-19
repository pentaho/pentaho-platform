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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.engine.services.connection.datasource.dbcp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;

import javax.sql.DataSource;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;


/**
 * Created by gmoran on 5/2/14.
 */
@RunWith( Parameterized.class )
public class DynamicConnectionDatasourceServiceTest {

  private BaseDatasourceService spyService;
  private BaseDatasourceService pooledSpyService;
  private BaseDatasourceService nonPooledSpyService;
  private IDatabaseConnection mockConnection;
  private String dsName;

  @Parameterized.Parameters
  public static Collection<Object[]> services() {
    return Arrays.asList( new Object[][] {
      { new PooledDatasourceService(), new NonPooledDatasourceService(), "test1" },
      { new PooledOrJndiDatasourceService(), new NonPooledOrJndiDatasourceService(), "test3" }
    } );
  }

  @Before
  public void setUp() {

    mockConnection = mock( IDatabaseConnection.class );

    // Set it up - this is a NATIVE connection
    dsName = mockConnection.getName();
    when( mockConnection.getAccessType() ).thenReturn( DatabaseAccessType.NATIVE );
    when( mockConnection.getDatabaseName() ).thenReturn( dsName );

    DataSource mockDs = mock( DataSource.class );
    IDatasourceMgmtService mockMgmtService = mock( IDatasourceMgmtService.class );

    DynamicallyPooledOrJndiDatasourceService dynamic = new DynamicallyPooledOrJndiDatasourceService();
    dynamic.setNonPooledDatasourceService( nonPooledSpyService );
    dynamic.setPooledDatasourceService( pooledSpyService );
    spyService = spy(dynamic );

    try {
      when( mockMgmtService.getDatasourceByName( dsName ) ).thenReturn( mockConnection );
    } catch ( DatasourceMgmtServiceException e ) {
      e.printStackTrace();
    }

    try {
      doReturn( mockDs ).when( nonPooledSpyService ).resolveDatabaseConnection( mockConnection );
      doReturn( mockDs ).when( pooledSpyService ).resolveDatabaseConnection( mockConnection );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

    doReturn( mockMgmtService ).when( nonPooledSpyService ).getDatasourceMgmtService();
    doReturn( mockMgmtService ).when( pooledSpyService ).getDatasourceMgmtService();
    doReturn( mockMgmtService ).when( spyService ).getDatasourceMgmtService();

    spyService.clearCache();

  }

  public DynamicConnectionDatasourceServiceTest( BaseDatasourceService pooled, BaseDatasourceService nonpooled, String name ) {
    this.pooledSpyService = spy( pooled );
    this.nonPooledSpyService = spy( nonpooled );
    this.dsName = name;
  }

  @Test
  public void testUsePoolingConnectionServices( ) {
    try {
      when( mockConnection.isUsingConnectionPool() ).thenReturn( true );
      spyService.getDataSource( dsName );
      verify( pooledSpyService ).resolveDatabaseConnection( mockConnection );
      verify( nonPooledSpyService, Mockito.never() ).resolveDatabaseConnection( mockConnection );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void testUseNonPoolingConnectionServices( ) {
    try {
      when( mockConnection.isUsingConnectionPool() ).thenReturn( false );
      spyService.getDataSource( dsName );
      verify( nonPooledSpyService ).resolveDatabaseConnection( mockConnection );
      verify( pooledSpyService, Mockito.never() ).resolveDatabaseConnection( mockConnection );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }
  }

  @Test
  public void testCacheClearinginDynamicConnectionServices( ) {
    try {
      when( mockConnection.isUsingConnectionPool() ).thenReturn( true );
      spyService.getDataSource( dsName );
      spyService.clearCache();
      spyService.getDataSource( dsName );
      verify( pooledSpyService, Mockito.times( 2 ) ).resolveDatabaseConnection( mockConnection );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }
  }

}
