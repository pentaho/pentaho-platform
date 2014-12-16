package org.pentaho.platform.repository2.unified.jcr;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.pentaho.di.core.util.Assert.assertTrue;
import static org.pentaho.di.core.util.Assert.assertFalse;
import static org.pentaho.platform.repository2.unified.jcr.IAclNodeHelper.DatasourceType.MONDRIAN;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
  "classpath:/repository-test-override.spring.xml" } )
public class JcrAclNodeHelperTest extends DefaultUnifiedRepositoryBase {

  private static final String DS_NAME = "test.txt";

  private JcrAclNodeHelper helper;
  private ITenant defaultTenant;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    createUsers();
    ensurePublicExists();

    helper = new JcrAclNodeHelper( repo, ClientRepositoryPaths.getPublicFolderPath() );
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
  public void jcrAclNodeHelperDefaultLocation() {
    loginAsSysTenantAdmin();

    helper = new JcrAclNodeHelper( repo, null );
    assertEquals( helper.getAclNodeFolder(), ServerRepositoryPaths.getAclNodeFolderPath() );
  }


  @Test
  public void visibleForEveryOne() {
    loginAsSuzy();
    assertTrue( helper.hasAccess( DS_NAME, MONDRIAN ) );
  }


  @Test
  public void suzyHasAccess() {
    makeDsPrivate();

    loginAsSuzy();
    assertTrue( helper.hasAccess( DS_NAME, MONDRIAN ) );
  }

  @Test
  public void tiffanyHasNoAccess() {
    makeDsPrivate();

    loginAsTiffany();
    assertFalse( helper.hasAccess( DS_NAME, MONDRIAN ) );
  }


  @Test
  public void publish() {
    makeDsPrivate();
    helper.publishDatasource( DS_NAME, MONDRIAN );

    loginAsTiffany();
    assertTrue( helper.hasAccess( DS_NAME, MONDRIAN ) );
  }


  @Test
  public void aclNodeIsCreated() {
    makeDsPrivate();

    loginAsSuzy();
    assertNotNull( repo.getFile( helper.getAclNodeFolder() + "/" + MONDRIAN.resolveName( DS_NAME ) ) );

    loginAsRepositoryAdmin();
    assertNotNull(
      repo.getFile( helper.getAclNodeFolder() + "/" + MONDRIAN.resolveName( DS_NAME ) + "/" + "acl.store" ) );
  }

  @Test
  public void aclNodeIsRemoved() {
    makeDsPrivate();

    loginAsRepositoryAdmin();
    helper.setAclFor( DS_NAME, MONDRIAN, null );
    assertNull( repo.getFile( helper.getAclNodeFolder() + "/" + MONDRIAN.resolveName( DS_NAME ) ) );
    assertNull( repo.getFile( helper.getAclNodeFolder() + "/" + MONDRIAN.resolveName( DS_NAME ) + "/" + "acl.store" ) );
  }


  private void makeDsPrivate() {
    loginAsRepositoryAdmin();
    RepositoryFileSid userSid = new RepositoryFileSid( USERNAME_SUZY, RepositoryFileSid.Type.USER );
    RepositoryFileAcl acl = new RepositoryFileAcl.Builder( USERNAME_SUZY ).ace( userSid,
      EnumSet.of( RepositoryFilePermission.ALL ) ).entriesInheriting( false ).build();

    helper.setAclFor( DS_NAME, MONDRIAN, acl );
  }


  private RepositoryFile ensureFolderExists( String folderName ) {
    loginAsRepositoryAdmin();
    try {
      RepositoryFile folder = repo.getFile( folderName );
      if ( folder == null ) {
        folder = repo.createFolder( repo.getFile( "/" ).getId(), new RepositoryFile.Builder( folderName ).
          folder( true ).build(), "" );
      }
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
