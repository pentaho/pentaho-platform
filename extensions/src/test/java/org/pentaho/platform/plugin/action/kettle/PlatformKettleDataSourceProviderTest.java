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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.di.core.database.DataSourceProviderInterface;
import org.pentaho.platform.api.data.IPooledDatasourceService;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.sql.DataSource;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class PlatformKettleDataSourceProviderTest {

  @Test
  public void testInvalidateNamedDataSource() throws Exception {
    IPooledDatasourceService service = mock( IPooledDatasourceService.class );
    DataSource dataSource = mock( DataSource.class );
    String namedDataSource = UUID.randomUUID().toString();
    when( service.getDataSource( namedDataSource ) ).thenReturn( dataSource );
    DatabaseDialectService mockDatabaseDialectService = mock( DatabaseDialectService.class );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class ) ) {
      pentahoSystem.when( () -> PentahoSystem.get( eq( IPooledDatasourceService.class ), nullable( IPentahoSession.class) ) ).thenReturn( service );
      pentahoSystem.when( () -> PentahoSystem.get( eq( IDatabaseDialectService.class ) ) ).thenReturn( mockDatabaseDialectService );
      DataSourceProviderInterface dsp = mock( PlatformKettleDataSourceProvider.class );
      when( dsp.invalidateNamedDataSource( namedDataSource, DataSourceProviderInterface.DatasourceType.POOLED ) )
        .thenCallRealMethod();
      dsp.invalidateNamedDataSource( namedDataSource, DataSourceProviderInterface.DatasourceType.POOLED );
      Mockito.verify( service, Mockito.times( 1 ) ).clearDataSource( namedDataSource );
    }
  }
}
