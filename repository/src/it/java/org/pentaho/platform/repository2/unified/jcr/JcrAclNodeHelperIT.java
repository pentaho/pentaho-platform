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

package org.pentaho.platform.repository2.unified.jcr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jcr.PathNotFoundException;
import java.util.EnumSet;

import static org.junit.Assert.*;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/repository-test-override.spring.xml" } )
public class JcrAclNodeHelperIT extends DefaultUnifiedRepositoryBase {

  private JcrAclNodeHelperCallTester helper;
  private ITenant defaultTenant;
  private RepositoryFile targetFile;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    createUsers();
    ensurePublicExists();
    loginAsRepositoryAdmin();
    targetFile = createSampleFile( "/public", "test.txt", "test", true, 1 );
    RepositoryFileAcl acl = repo.getAcl( targetFile.getId() );
    RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( false )
        .clearAces()
            .entriesInheriting( false )
        .ace( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE, EnumSet.of( RepositoryFilePermission.READ ) )
        .build();
    repo.updateAcl( newAcl );

    helper = new JcrAclNodeHelperCallTester( repo ); // Subclass for ensuring no redundant calls are made.
    logout();
  }

  private void createUsers() {
    loginAsSysTenantAdmin();

    defaultTenant = tenantManager.createTenant( systemTenant, TenantUtils.getDefaultTenant(), tenantAdminRoleName,
        tenantAuthenticatedRoleName, ANONYMOUS_ROLE_NAME );

    createUser( defaultTenant, singleTenantAdminUserName, PASSWORD, tenantAdminRoleName );
    createUser( defaultTenant, USERNAME_SUZY, PASSWORD, tenantAuthenticatedRoleName );
    createUser( defaultTenant, USERNAME_TIFFANY, PASSWORD, tenantAuthenticatedRoleName );

    logout();
  }

  private void ensurePublicExists() {
    ensureFolderExists( ClientRepositoryPaths.getPublicFolderPath() );
  }

  @After
  public void tearDown() throws Exception {
    loginAsSysTenantAdmin();

    ITenant defaultTenant = tenantManager.getTenant( "/" + ServerRepositoryPaths.getPentahoRootFolderName() + "/"
        + TenantUtils.getDefaultTenant() );
    if ( defaultTenant != null ) {
      cleanupUserAndRoles( defaultTenant );
    }

    super.tearDown();
  }
  


  @Test
  public void visibleForEveryOne() {
    loginAsRepositoryAdmin();
    helper.resetAclNodeCallCounter();
    assertTrue( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );
    // This tests that canAccess doesn't make redundant calls to getAclNode - BISERVER-12780
    assertEquals( 1, helper.getAclNodeCallCounter() );
  }


  @Test
  public void suzycanAccess() {
    makeDsPrivate();
    loginAsSuzy();
    assertTrue( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );
  }

  @Test
  public void tiffanyHasNoAccess() {
    makeDsPrivate();

    loginAsTiffany();
    assertFalse( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );
  }


  @Test
  public void publish() {
    makeDsPrivate();
    helper.removeAclFor( targetFile );

    loginAsTiffany();
    assertTrue( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );
  }

  @Test
  public void aclIsReplaced() throws InterruptedException {
    loginAsRepositoryAdmin();
    RepositoryFileAcl acl = createAclFor( USERNAME_TIFFANY );
    helper.setAclFor( targetFile, acl );

    loginAsTiffany();
    assertTrue( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );

    loginAsSuzy();
    assertFalse( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );

    loginAsRepositoryAdmin();
    acl = createAclFor( USERNAME_SUZY );
    helper.setAclFor( targetFile, acl );

    loginAsSuzy();

    // This is failing most of the time in this integration test. ACL is set properly yet Suzy can still see ACL node.
    // If execution is paused long enough, it does work properly.
    assertTrue( helper.canAccess( targetFile, EnumSet.of( RepositoryFilePermission.READ ) ) );
  }


  @Test
  public void aclNodeIsCreated() {
    makeDsPrivate();

    loginAsRepositoryAdmin();
    assertTrue( "No ACL node was created", !repo.getReferrers( targetFile.getId() ).isEmpty() );
  }

  @Test
  public void aclNodeIsRemoved() {
    makeDsPrivate();

    loginAsRepositoryAdmin();
    helper.setAclFor( targetFile, null );
    assertTrue( "Referrers should be null after ACL delete", repo.getReferrers( targetFile.getId() ).isEmpty() );
  }

  @Test
  public void cannotDeleteTargetWithAclNode() {
    makeDsPrivate();

    loginAsRepositoryAdmin();
    try {
      repo.deleteFile( targetFile.getId(), true, "I cannot be killed" );
      fail("Should have thrown Referential Integrity Exception");
    } catch( Exception e ){
      assertTrue( repo.getFile( targetFile.getPath() ) != null );
    }

  }

  @Test
  public void canDeleteTargetIfAclNodeRemoved() {
    makeDsPrivate();

    loginAsRepositoryAdmin();
    helper.setAclFor( targetFile, null );
    repo.deleteFile( targetFile.getId(), true, "I can be killed" );

    Throwable pathNotFound = null;
    try {
      repo.getFile( targetFile.getPath() );
    } catch ( Throwable e ) {
      while ( e != null ) {
        if (e instanceof PathNotFoundException) {
          pathNotFound = e;
          e = null;
        } else {
          e = e.getCause();
        }
      }
    }
    assertNotNull( pathNotFound );
  }

  @Test
  public void administratorRoleIsAdded() {
    makeDsPrivate();
    loginAsSuzy();

    helper.resetAclNodeCallCounter();
    RepositoryFileAcl aclReturned = helper.getAclFor( targetFile );
    // This tests that getAclFor doesn't make redundant calls to getAclNode - BISERVER-12780
    assertEquals( 1, helper.getAclNodeCallCounter() );
    
    boolean adminPresent = false;

    for( RepositoryFileAce ace : aclReturned.getAces() ){
      if( ace.getSid().getName() == tenantAdminRoleName ) {
        adminPresent = true;
        break;
      }
    }

    assertFalse( adminPresent );

    loginAsRepositoryAdmin();

    aclReturned = helper.getAclFor( targetFile );
    adminPresent = false;

    for( RepositoryFileAce ace : aclReturned.getAces() ){
      if( ace.getSid().getName() == tenantAdminRoleName ) {
        adminPresent = true;
        break;
      }
    }

    assertTrue( adminPresent );
  }


  @Test
  public void getAclFor_Null_ReturnsFalse() throws Exception {
    loginAsRepositoryAdmin();
    assertNull( helper.getAclFor( null ) );
  }

  @Test
  public void canAccess_Null_ReturnsFalse() throws Exception {
    loginAsRepositoryAdmin();
    assertFalse( helper.canAccess( null, EnumSet.of( RepositoryFilePermission.READ ) ) );
  }


  private void makeDsPrivate() {
    loginAsRepositoryAdmin();
    RepositoryFileAcl acl = createAclFor( USERNAME_SUZY );
    helper.setAclFor( targetFile, acl );
    logout();
  }

  private static RepositoryFileAcl createAclFor( String user ) {
    RepositoryFileSid userSid = new RepositoryFileSid( user, RepositoryFileSid.Type.USER );
    return new RepositoryFileAcl.Builder( user )
        .ace( userSid, EnumSet.of( RepositoryFilePermission.ALL ) )
        .entriesInheriting( false )
        .build();
  }


  private RepositoryFile ensureFolderExists( String folderName ) {
    loginAsRepositoryAdmin();
    try {
      RepositoryFile folder = repo.getFile( folderName );
      if ( folder == null ) {
        folder = repo.createFolder( repo.getFile( "/" ).getId(), new RepositoryFile.Builder( folderName ).
            folder( true ).build(), "" );

      }

      RepositoryFileAcl acl = repo.getAcl( folder.getId() );
      RepositoryFileAcl newAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( true )
          .ace( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE, EnumSet.of( RepositoryFilePermission.ALL ) )
          .build();
      repo.updateAcl( newAcl );

      return folder;
    } finally {
      logout();
    }
  }

  private void loginAsSuzy() {
    login( USERNAME_SUZY, defaultTenant, new String[] { tenantAuthenticatedRoleName } );
  }

  private void loginAsTiffany() {
    login( USERNAME_TIFFANY, defaultTenant, new String[] { tenantAuthenticatedRoleName } );
  }
}
