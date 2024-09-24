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

package org.pentaho.platform.web.http.api.resources;

import org.junit.*;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import org.junit.runner.RunWith;

import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SystemRefreshResourceTest {

  private static final IOlapService olapService = mock( IOlapService.class );
  private static final IPentahoSession session = mock( IPentahoSession.class );
  private final SystemRefreshResource resource = spy( new SystemRefreshResource() );

  private static MockedStatic<PentahoSessionHolder> pentahoSessionHolderMock;
  private static MockedStatic<PentahoSystem> pentahoSystemMock;
  private static MockedStatic<SystemUtils> systemUtilsMock;

  @BeforeClass
  public static void beforeAll() {
    pentahoSessionHolderMock = mockStatic( PentahoSessionHolder.class );
    pentahoSystemMock = mockStatic( PentahoSystem.class );
    systemUtilsMock = mockStatic( SystemUtils.class );

    pentahoSessionHolderMock.when( PentahoSessionHolder::getSession ).thenAnswer( invocationOnMock -> session );
    pentahoSystemMock.when( () -> PentahoSystem.get( IOlapService.class, "IOlapService", session ) ).thenAnswer( invocationOnMock -> olapService );
  }

  @AfterClass
  public static void afterAll() {
    pentahoSessionHolderMock.close();
    pentahoSystemMock.close();
    systemUtilsMock.close();
  }

  @After
  public void afterEach() {
    reset( olapService );
    reset( session );
  }

  @Test
  public void flushMondrianSchemaCacheNotAdmin() {
    when( SystemUtils.canAdminister() ).thenReturn( false );

    Response response = resource.flushMondrianSchemaCache( "schemaX" );
    assertEquals( UNAUTHORIZED.getStatusCode(), response.getStatus() );
  }

  @Test
  public void flushMondrianSchemaCacheIsAdmin() {
    when( SystemUtils.canAdminister() ).thenReturn( true );

    Response response = resource.flushMondrianSchemaCache( "schemaX" );

    assertEquals( OK.getStatusCode(), response.getStatus() );
    verify( olapService, times( 1 ) ).flush( session, "schemaX" );
  }

  @Test
  public void flushMondrianSchemaCacheError() {
    when( SystemUtils.canAdminister() ).thenReturn( true );
    doThrow( IOlapServiceException.class ).when( olapService ).flush( session, "schemaX" );

    try {
      resource.flushMondrianSchemaCache( "schemaX" );
      fail();
    } catch ( IOlapServiceException e ) {
      verify( olapService, times( 1 ) ).flush( session, "schemaX" );
    }
  }
}
