/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
