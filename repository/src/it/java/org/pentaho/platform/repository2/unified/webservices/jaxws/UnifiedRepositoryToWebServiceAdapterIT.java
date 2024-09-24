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

package org.pentaho.platform.repository2.unified.webservices.jaxws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Class Description
 * 
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class UnifiedRepositoryToWebServiceAdapterIT extends DefaultUnifiedRepositoryBase {
  private UnifiedRepositoryToWebServiceAdapter adapter;
  public static final String MAIN_TENANT_1 = "maintenant1";

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    IUnifiedRepositoryJaxwsWebService repositoryWS = new DefaultUnifiedRepositoryJaxwsWebService( repo );
    adapter = new UnifiedRepositoryToWebServiceAdapter( repositoryWS );
  }

  @Test
  public void testFileMetadata() throws Exception {
    login( sysAdminUserName, systemTenant, new String[] { tenantAdminRoleName,
      tenantAuthenticatedRoleName } );
    ITenant mainTenant_1 =
        tenantManager.createTenant( systemTenant, MAIN_TENANT_1, tenantAdminRoleName,
            tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( mainTenant_1, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    login( USERNAME_ADMIN, mainTenant_1, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFile file = repo.getFile( ClientRepositoryPaths.getPublicFolderPath() );
    final RepositoryFile testfile =
        repo.createFile( file.getId(), new RepositoryFile.Builder( "testfile" ).build(),
            new SimpleRepositoryFileData( new ByteArrayInputStream( "test".getBytes() ), "UTF-8",
              "text/plain" ), null );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository is setup correctly
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repo.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 1, fileMetadata.size() );
    }

    final Map<String, Serializable> metadata = new HashMap<String, Serializable>();
    metadata.put( "sample key", "sample value" );
    metadata.put( "complex key?", "\"an even more 'complex' value\"! {and them some}" );

    adapter.setFileMetadata( testfile.getId(), metadata );
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure the repository sees the metadata
      assertNotNull( testfile );
      assertNotNull( testfile.getId() );
      final Map<String, Serializable> fileMetadata = repo.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
    }
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      // Make sure we can get the same metadata back via the web service
      final Map<String, Serializable> fileMetadata = adapter.getFileMetadata( testfile.getId() );
      assertNotNull( fileMetadata );
      assertEquals( 2, fileMetadata.size() );
      assertTrue( StringUtils.equals( "sample value", (String) fileMetadata.get( "sample key" ) ) );
      assertTrue( StringUtils.equals( "\"an even more 'complex' value\"! {and them some}", (String) fileMetadata
          .get( "complex key?" ) ) );
    }

    cleanupUserAndRoles( mainTenant_1 );
  }

}
