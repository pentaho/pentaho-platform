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
