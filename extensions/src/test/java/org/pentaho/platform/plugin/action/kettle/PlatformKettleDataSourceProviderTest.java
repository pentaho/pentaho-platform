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
 * Copyright (c) 2021-2022 Hitachi Vantara..  All rights reserved.
 */


package org.pentaho.platform.plugin.action.kettle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.platform.api.data.IPooledDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;


@RunWith( PowerMockRunner.class )
@PrepareForTest( { PlatformKettleDataSourceProvider.class, PentahoSystem.class } )
public class PlatformKettleDataSourceProviderTest {

  @Test
  public void testInvalidateNamedDataSource() throws Exception {
    String namedDataSource = UUID.randomUUID().toString();

    // Mock objects
    IPooledDatasourceService service = mock( IPooledDatasourceService.class );
    DataSource dataSource = mock( DataSource.class );
    DatabaseDialectService mockDatabaseDialectService = Mockito.mock( DatabaseDialectService.class );
    when( mockDatabaseDialectService.getDatabaseTypes() ).thenReturn( new ArrayList<>() );
    mockStatic( PentahoSystem.class );
    when( PentahoSystem.get( eq( IDatabaseDialectService.class ) ) ).thenReturn( mockDatabaseDialectService );
    DataSourceProviderInterface dsp = mock( PlatformKettleDataSourceProvider.class );
    doReturn( service ).when( dsp, "getService", IPooledDatasourceService.class );
    when( service.getDataSource( namedDataSource ) ).thenReturn( dataSource );
    when( dsp.invalidateNamedDataSource( namedDataSource, DataSourceProviderInterface.DatasourceType.POOLED ) )
      .thenCallRealMethod();

    dsp.invalidateNamedDataSource( namedDataSource, DataSourceProviderInterface.DatasourceType.POOLED );
    Mockito.verify( service, Mockito.times( 1 ) ).clearDataSource( namedDataSource );
  }
}
