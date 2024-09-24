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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith( MockitoJUnitRunner.class )
public class RepositoryResourceTest {

  private final IUnifiedRepository repository = mock( IUnifiedRepository.class );

  @Test
  public void doExecuteDefaultNotFound() throws Exception {
    try ( MockedStatic<FileResource> fileResource = Mockito.mockStatic( FileResource.class ) ) {
      fileResource.when( () -> FileResource.idToPath( nullable( String.class ) ) ).thenCallRealMethod();
      fileResource.when( FileResource::getRepository ).thenReturn( repository );

      doReturn( null ).when( repository ).getFile( "/home/admin/comments.wcdf" );
      Response response = new RepositoryResource().doExecuteDefault( ":home:admin:comments.wcdf" );

      assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }
  }
}
