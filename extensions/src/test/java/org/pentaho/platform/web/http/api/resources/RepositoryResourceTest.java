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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith( PowerMockRunner.class )
@PrepareForTest( FileResource.class )
public class RepositoryResourceTest {

  private IUnifiedRepository repository = mock( IUnifiedRepository.class );

  @Before
  public void setup() {
    mockStatic( FileResource.class );
    when( FileResource.idToPath( anyString() ) ).thenCallRealMethod();
    when( FileResource.getRepository() ).thenReturn( repository );
  }

  @Test
  public void doExecuteDefaultNotFound() throws Exception {
    doReturn( null ).when( repository ).getFile( "/home/admin/comments.wcdf" );
    Response response = new RepositoryResource().doExecuteDefault( ":home:admin:comments.wcdf" );

    assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
  }
}
