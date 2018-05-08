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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.olap.IOlapService;
import org.pentaho.platform.plugin.action.olap.IOlapServiceException;

import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSessionHolder.class, PentahoSystem.class, SystemUtils.class } )
public class SystemRefreshResourceTest {

  private IOlapService olapService = mock( IOlapService.class );
  private IPentahoSession session = mock( IPentahoSession.class );
  private SystemRefreshResource resource = spy( new SystemRefreshResource() );

  @Before
  public void setup() {
    mockStatic( PentahoSessionHolder.class );
    mockStatic( PentahoSystem.class );
    mockStatic( SystemUtils.class );

    when( PentahoSessionHolder.getSession() ).thenReturn( session );
    when( PentahoSystem.get( IOlapService.class, "IOlapService", session ) ).thenReturn( olapService );
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
      Response response = resource.flushMondrianSchemaCache( "schemaX" );
      fail();
    } catch ( IOlapServiceException e ) {
      verify( olapService, times( 1 ) ).flush( session, "schemaX" );
    }
  }
}
