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

package org.pentaho.platform.web.http.api.resources.services;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.io.InputStream;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class RepositoryPublishServiceTest {

  private RepositoryPublishService repositoryPublishService;
  private RepositoryFileImportBundle mockRepositoryFileImportBundle;

  @Before
  public void setUp() {
    repositoryPublishService = spy( new RepositoryPublishService() );
    repositoryPublishService.platformImporter = mock( IPlatformImporter.class );
    repositoryPublishService.policy = mock( IAuthorizationPolicy.class );
    mockRepositoryFileImportBundle = mock( RepositoryFileImportBundle.class );
  }

  @After
  public void cleanup() {
    repositoryPublishService = null;
  }

  @Test
  public void testWriteFile() throws Exception {
    String pathId = "path:to:file";
    InputStream stubInputStream = IOUtils.toInputStream( "some test data for my input stream" );
    Boolean overwriteFile = true;

    doReturn( true ).when( repositoryPublishService.policy ).isAllowed( nullable( String.class ) );
    doReturn( repositoryPublishService.policy ).when( repositoryPublishService ).getPolicy();

    doReturn( mockRepositoryFileImportBundle ).when( repositoryPublishService ).buildBundle( pathId, stubInputStream, overwriteFile );

    repositoryPublishService.writeFile( pathId, stubInputStream, overwriteFile );

    verify( repositoryPublishService.platformImporter, times( 1 ) ).importFile( mockRepositoryFileImportBundle );
  }

  @Test
  public void testWriteFileException() throws Exception {
    String pathId = "path:to:file";
    InputStream stubInputStream = IOUtils.toInputStream( "some test data for my input stream" );
    Boolean overwriteFile = true;

    doReturn( mockRepositoryFileImportBundle ).when( repositoryPublishService ).buildBundle( pathId, stubInputStream, overwriteFile );

    /*
     * Test 1
     */
    doReturn( false ).when( repositoryPublishService.policy ).isAllowed( nullable( String.class ) );
    doReturn( repositoryPublishService.policy ).when( repositoryPublishService ).getPolicy();

    try {
      repositoryPublishService.writeFile( pathId, stubInputStream, overwriteFile );
      fail();
    } catch ( PentahoAccessControlException e ) {
      // Expected
    } catch ( Throwable t ) {
      fail();
    }

    /*
     * Test 2
     */
    doReturn( true ).when( repositoryPublishService.policy ).isAllowed( nullable( String.class ) );
    doThrow( new PlatformImportException( "" ) ).when( repositoryPublishService.platformImporter ).importFile( mockRepositoryFileImportBundle );
    try {
      repositoryPublishService.writeFile( pathId, stubInputStream, overwriteFile );
      fail();
    } catch ( PlatformImportException e ) {
      // Expected
    } catch ( Exception e ) {
      fail();
    }

    /*
     * Test 3
     */
    doReturn( true ).when( repositoryPublishService.policy ).isAllowed( nullable( String.class ) );
    doThrow( new InternalError() ).when( repositoryPublishService.platformImporter ).
        importFile( mockRepositoryFileImportBundle );
    try {
      repositoryPublishService.writeFile( pathId, stubInputStream, overwriteFile );
      fail();
    } catch ( PlatformImportException e ) {
      fail();
    } catch ( InternalError e ) {
      // Expected
    }
  }

  @Test( expected = PentahoAccessControlException.class )
  public void prohibitedForAdministerSecurity() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( RepositoryReadAction.NAME ) ).thenReturn( true );
    when( policy.isAllowed( RepositoryCreateAction.NAME ) ).thenReturn( true );
    when( policy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( true );
    when( policy.isAllowed( PublishAction.NAME ) ).thenReturn( false );
    repositoryPublishService.validateAccess();
  }

  @Test
  public void permittedForPublisher() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = mock( IAuthorizationPolicy.class );
    when( policy.isAllowed( PublishAction.NAME ) ).thenReturn( true );
    doReturn( policy ).when( repositoryPublishService ).getPolicy();
    repositoryPublishService.validateAccess();
  }
}
