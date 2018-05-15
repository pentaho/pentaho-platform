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
import org.pentaho.platform.cache.MockPlatformCache;

import javax.sql.DataSource;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;


/**
 * Created by gmoran on 5/2/14.
 */
@RunWith(Parameterized.class)
public class JNDIDatasourceServiceTest {

  private BaseDatasourceService service;
  private BaseDatasourceService spyService;
  private String dsName;

  @Parameterized.Parameters
  public static Collection services() {
    return Arrays.asList( new Object[][] {
        { new JndiDatasourceService(), "test0" },
        { new NonPooledOrJndiDatasourceService(), "test1" },
        { new PooledOrJndiDatasourceService(), "test2" }
    } );
  }

  @Before
  public void setUp(){

    IDatabaseConnection mockConnection = mock(IDatabaseConnection.class);

    // Set it up - this is a JNDI connection
    when( mockConnection.getAccessType()).thenReturn( DatabaseAccessType.JNDI );
    when( mockConnection.getDatabaseName()).thenReturn( dsName );

    DataSource mockDs = mock(DataSource.class);
    IDatasourceMgmtService mockMgmtService = mock(IDatasourceMgmtService.class);

    spyService = spy(service );

    try {
      when( mockMgmtService.getDatasourceByName( dsName ) ).thenReturn( mockConnection );
    } catch ( DatasourceMgmtServiceException e ) {
      e.printStackTrace();
    }

    try {
      doReturn(mockDs).when( spyService ).getJndiDataSource( dsName );
    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

    doReturn( mockMgmtService ).when( spyService ).getDatasourceMgmtService();

    service.clearCache();

  }

  public JNDIDatasourceServiceTest( BaseDatasourceService service, String name ){
    this.service = service;
    this.service.setCacheManager( new MockPlatformCache() );
    this.dsName = name;
  }

  @Test
  public void testJndiServices( ){


    try {

      // Now make sure that the JNDI service method gets called; if not,
      // fail the test
      spyService.getDataSource( dsName );
      verify(spyService).getJndiDataSource( dsName );

    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

  }

  @Test
  public void testCachedJndiServices( ){

    try {

      // Now make sure that the JNDI service method gets called the first time,
      // but the second time it should retrieve from cache.
      spyService.getDataSource( dsName );
      spyService.getDataSource( dsName );
      verify(spyService, Mockito.times( 1 )).getJndiDataSource( dsName );

    } catch ( DBDatasourceServiceException e ) {
      e.printStackTrace();
    }

  }

}
