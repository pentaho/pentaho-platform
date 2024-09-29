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

package org.pentaho.platform.repository2.unified;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import javax.jcr.security.Privilege;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IRepositoryVersionManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAce;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryAccessDeniedException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryException;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.sample.SampleRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryDumpToFile;
import org.pentaho.platform.repository2.unified.jcr.JcrRepositoryFileUtils;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.security.policy.rolebased.actions.SchedulerAction;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and {@link org.pentaho.platform.api.engine.IAuthorizationPolicy IAuthorizationPolicy} fully configured
 * behind Spring Security's method security and Spring's transaction interceptor.
 *
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The
 * application context config files are listed in the ContextConfiguration annotation. By implementing
 * {@link org.springframework.context.ApplicationContextAware ApplicationContextAware}, this unit test can access various beans defined in the application context,
 * including the bean under test.
 * </p>
 *
 * This is part of tests for Authorization and ACL check purposes
 *
 * @author mlowery
 * @author Aliaksei_Haidukou
 */
@RunWith( SpringJUnit4ClassRunner.class )
@SuppressWarnings( "nls" )
public class DefaultUnifiedRepositoryAuthorizationIT extends DefaultUnifiedRepositoryBase {

  @Before
  public void setup() {
    IRepositoryVersionManager mockRepositoryVersionManager = mock( IRepositoryVersionManager.class );
    when( mockRepositoryVersionManager.isVersioningEnabled( anyString() ) ).thenReturn( true );
    when( mockRepositoryVersionManager.isVersionCommentEnabled( anyString() ) ).thenReturn( false );
    JcrRepositoryFileUtils.setRepositoryVersionManager( mockRepositoryVersionManager );
  }

  /**
   * This test method depends on {@code DefaultRepositoryEventHandler} behavior.
   */
  @Test
  public void testOnNewUser() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAdminRoleName } );
    logout();

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    RepositoryFile suzyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) );
    assertNotNull( suzyHomeFolder );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath() ) );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath() ) );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath() ) );
    final String suzyFolderPath = ServerRepositoryPaths.getUserHomeFolderPath( tenantAcme, USERNAME_SUZY );
    assertNotNull( SimpleJcrTestUtils.getItem( testJcrTemplate, suzyFolderPath ) );
  }

  /**
   * This test method depends on {@code DefaultBackingRepositoryLifecycleManager} behavior.
   */
  @Test
  public void testAclsOnDefaultFolders() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final RepositoryFileSid suzySid = new RepositoryFileSid( USERNAME_SUZY, RepositoryFileSid.Type.USER );
    final RepositoryFileSid acmeAuthenticatedAuthoritySid =
        new RepositoryFileSid( tenantAuthenticatedRoleName, RepositoryFileSid.Type.ROLE );
    final RepositoryFileSid sysAdminSid = new RepositoryFileSid( sysAdminUserName, RepositoryFileSid.Type.USER );
    final RepositoryFileSid tenantAdminSid = new RepositoryFileSid( USERNAME_ADMIN, RepositoryFileSid.Type.USER );
    final RepositoryFileSid tenantCreatorSid = new RepositoryFileSid( sysAdminUserName, RepositoryFileSid.Type.USER );

    RepositoryFile file = tenantManager.getTenantRootFolder( tenantAcme );
    String tenantRootFolderAbsPath = pathConversionHelper.relToAbs( file.getPath() );
    // pentaho root folder
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_READ ) );
    // TODO mlowery possible issue
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_WRITE ) );
    // TODO mlowery possible issue
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_READ_ACCESS_CONTROL ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath, Privilege.JCR_WRITE ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, tenantRootFolderAbsPath,
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // tenant root folder
    // there is no ace that gives authenticated acme users access to /pentaho/acme; it's in logic on the server
    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getRootFolderPath() )
        .getId() ).isEntriesInheriting() );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getRootFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .isEntriesInheriting() );

    // tenant public folder
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    // tenant home folder
    assertFalse( repo.getAcl( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() )
        .getId() ).isEntriesInheriting() );
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getHomeFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantHomeFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    Serializable fileId = repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ).getId();
    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantEtcFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantEtcFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );

    // suzy home folder
    assertEquals( suzySid, repo.getAcl(
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ).getId() ).getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY ), Privilege.JCR_ALL ) );

    // tenant etc/pdi folder
    final String pdiPath = ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + "pdi";
    assertTrue( repo.getAcl( repo.getFile( pdiPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( pdiPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( pdiPath ).getId() ).getOwner() );

    // tenant etc/databases folder
    final String databasesPath = pdiPath + RepositoryFile.SEPARATOR + "databases";
    assertTrue( repo.getAcl( repo.getFile( databasesPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( databasesPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( databasesPath ).getId() ).getOwner() );

    // tenant etc/slaveServers folder
    final String slaveServersPath = pdiPath + RepositoryFile.SEPARATOR + "slaveServers";
    assertTrue( repo.getAcl( repo.getFile( slaveServersPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( slaveServersPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( slaveServersPath ).getId() ).getOwner() );

    // tenant etc/clusterSchemas folder
    final String clusterSchemasPath = pdiPath + RepositoryFile.SEPARATOR + "clusterSchemas";
    assertTrue( repo.getAcl( repo.getFile( clusterSchemasPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( clusterSchemasPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( clusterSchemasPath ).getId() ).getOwner() );

    // tenant etc/partitionSchemas folder
    final String partitionSchemasPath = pdiPath + RepositoryFile.SEPARATOR + "partitionSchemas";
    assertTrue( repo.getAcl( repo.getFile( partitionSchemasPath ).getId() ).isEntriesInheriting() );
    assertLocalAclEmpty( repo.getFile( partitionSchemasPath ) );
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( partitionSchemasPath ).getId() ).getOwner() );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getUserHomeFolderPath(
        tenantAcme, USERNAME_SUZY ), Privilege.JCR_WRITE ) );

    assertLocalAceExists( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ), acmeAuthenticatedAuthoritySid,
        EnumSet.of( RepositoryFilePermission.READ ) );

    // Test admin access ot tenant public folder
    assertEquals( tenantCreatorSid, repo.getAcl( repo.getFile( ClientRepositoryPaths.getPublicFolderPath() ).getId() )
        .getOwner() );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_READ_ACCESS_CONTROL ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_ADD_CHILD_NODES ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_MODIFY_PROPERTIES ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_NODE_TYPE_MANAGEMENT ) );
    assertTrue( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths.getTenantPublicFolderPath(),
        Privilege.JCR_MODIFY_ACCESS_CONTROL ) );

  }

  @Test
  public void testGetFileAccessDenied() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    assertNotNull( tiffanyHomeFolder );
    assertNotNull( repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" ).folder( true )
        .build(), null ) );
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    final String acmeTenantRootFolderPath = ClientRepositoryPaths.getRootFolderPath();
    final String homeFolderPath = ClientRepositoryPaths.getHomeFolderPath();
    final String tiffanyFolderPath = homeFolderPath + "/" + USERNAME_TIFFANY;
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "c:/build/testrepo_7", JcrRepositoryDumpToFile.Mode.CUSTOM );
    // dumpToFile.execute();
    // read access for suzy on home
    assertNotNull( repo.getFile( homeFolderPath ) );
    // no read access for suzy on tiffany's folder
    assertNull( repo.getFile( tiffanyFolderPath ) );
    // no read access for suzy on subfolder of tiffany's folder
    final String tiffanySubFolderPath = tiffanyFolderPath + "/test";
    assertNull( repo.getFile( tiffanySubFolderPath ) );
    // make sure Pat can't see acme folder (pat is in the duff tenant)
    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertNull( SimpleJcrTestUtils
        .getItem( testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath( tenantAcme ) ) );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantRootFolderPath( tenantAcme ), Privilege.JCR_READ ) );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantRootFolderPath( tenantAcme ), Privilege.JCR_READ_ACCESS_CONTROL ) );
  }

  @Test
  public void testGetFileAdmin() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" ).folder( true ).build(), null );
    RepositoryFileAcl acl = repo.getAcl( tiffanyHomeFolder.getId() );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) ) );
    assertNotNull( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) + "/test" ) );
  }

  @Test
  public void testStopThenStartInheriting() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile tiffanyHomeFolder = repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_TIFFANY ) );
    RepositoryFile testFolder =
        repo.createFolder( tiffanyHomeFolder.getId(), new RepositoryFile.Builder( "test" )
            .folder( true ).build(), null );
    RepositoryFileAcl acl = repo.getAcl( testFolder.getId() );
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertFalse( updatedAcl.isEntriesInheriting() );
    updatedAcl = new RepositoryFileAcl.Builder( updatedAcl ).entriesInheriting( true ).build();
    updatedAcl = repo.updateAcl( updatedAcl );
    assertTrue( updatedAcl.isEntriesInheriting() );
  }

  /**
   * While they may be filtered from the version history, we still must be able to fetch acl-only changes.
   */
  @Test
  public void testGetAclOnlyVersion() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();

    final String fileName = "helloworld.sample";
    RepositoryFile newFile =
        createSampleFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ), fileName, "blah", false, 123,
            true );
    assertEquals( 1, repo.getVersionSummaries( newFile.getId() ).size() );
    RepositoryFileAcl acl = repo.getAcl( newFile.getId() );
    // no change; just want to create a new version
    RepositoryFileAcl updatedAcl = new RepositoryFileAcl.Builder( acl ).build();
    repo.updateAcl( updatedAcl );
    assertEquals( 2, repo.getVersionSummaries( newFile.getId() ).size() );
    assertNotNull( repo.getVersionSummary( newFile.getId(), "1.1" ) );
  }

  @Test( expected = UnifiedRepositoryAccessDeniedException.class )
  public void testCreateFolderAccessDenied() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder = repo.getFile( ClientRepositoryPaths.getRootFolderPath() );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).build();
    repo.createFolder( parentFolder.getId(), newFolder, null );
  }

  @Test
  public void testOwnership() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // Suzy gives Tiffany all rights to her home folder
    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder =
        repo.getFile( parentFolderPath );
    RepositoryFileAcl parentAcl = repo.getAcl( parentFolder.getId() );
    RepositoryFileAcl newParentAcl =
        new RepositoryFileAcl.Builder( parentAcl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).build();
    repo.updateAcl( newParentAcl );

    //suzy now creates a new folder inside of her home folder
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    final String testFolderPath =
        parentFolderPath + RepositoryFile.SEPARATOR + "test";
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), repo.getAcl( newFolder.getId() ).getOwner() );

    // tiffany will set acl removing suzy's rights to this folder
    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFileAcl testFolderAcl = repo.getAcl( newFolder.getId() );
    //do a new Ace List filtering suzy's rights out
    List<RepositoryFileAce> newAceList = new ArrayList<RepositoryFileAce>();
    for ( RepositoryFileAce ace : newParentAcl.getAces() ) {
      if ( !ace.getSid().getName().equals( USERNAME_SUZY ) ) {
        newAceList.add( ace );
      }
    }
    RepositoryFileAcl newTestAcl =
        new RepositoryFileAcl.Builder( testFolderAcl ).aces( newAceList ).build();
    repo.updateAcl( newTestAcl );
    // but suzy is still the owner--she should be able to "acl" herself back into the folder
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( testFolderPath ) );
    // tiffany still have permissions
    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertNotNull( repo.getFile( testFolderPath ) );
  }

  @Test
  public void testGetAcl() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );

    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    assertEquals( true, acl.isEntriesInheriting() );
    assertEquals( new RepositoryFileSid( USERNAME_SUZY ), acl.getOwner() );
    assertEquals( newFolder.getId(), acl.getId() );
    assertTrue( acl.getAces().isEmpty() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER, RepositoryFilePermission.READ ).entriesInheriting( true ).build();
    RepositoryFileAcl fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is true, ace addition should not have taken
    assertTrue( fetchedAcl.getAces().isEmpty() );
    newAcl =
        new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
            RepositoryFileSid.Type.USER, RepositoryFilePermission.READ ).build(); // calling ace sets
    // entriesInheriting to false
    fetchedAcl = repo.updateAcl( newAcl );
    // since isEntriesInheriting is false, ace addition should have taken
    assertFalse( fetchedAcl.getAces().isEmpty() );
  }

  @Test
  public void testGetAcl2() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) ),
            RepositoryFilePermission.ALL ).build();
    repo.updateAcl( newAcl );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( 1, fetchedAcl.getAces().size() );
  }

  @Test
  public void testHasAccess() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( repo.hasAccess( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession()
        .getName() ), EnumSet.of( RepositoryFilePermission.READ ) ) );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertFalse( SimpleJcrTestUtils.hasPrivileges( testJcrTemplate, ServerRepositoryPaths
        .getTenantPublicFolderPath( tenantAcme ), Privilege.JCR_READ ) );

    // false is returned if path does not exist
    assertFalse( repo.hasAccess( ClientRepositoryPaths.getRootFolderPath() + "doesnotexist", EnumSet
        .of( RepositoryFilePermission.READ ) ) );
  }

  @Test
  public void testGetEffectiveAces() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile acmePublicFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    List<RepositoryFileAce> expectedEffectiveAces1 = repo.getEffectiveAces( acmePublicFolder.getId() );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( acmePublicFolder.getId(), newFolder, null );
    assertEquals( expectedEffectiveAces1, repo.getEffectiveAces( newFolder.getId() ) );

    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );
    RepositoryFileAcl newAcl =
        new RepositoryFileAcl.Builder( acl ).entriesInheriting( false ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) ),
            RepositoryFilePermission.ALL ).ace(
            new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) ),
            RepositoryFilePermission.READ ).build();
    repo.updateAcl( newAcl );

    List<RepositoryFileAce> expectedEffectiveAces2 = new ArrayList<RepositoryFileAce>();
    expectedEffectiveAces2.add( new RepositoryFileAce( new RepositoryFileSid( USERNAME_SUZY ), EnumSet
        .of( RepositoryFilePermission.ALL ) ) );
    expectedEffectiveAces2.add( new RepositoryFileAce( new RepositoryFileSid( USERNAME_TIFFANY ), EnumSet
        .of( RepositoryFilePermission.READ ) ) );
    assertEquals( expectedEffectiveAces2, repo.getEffectiveAces( newFolder.getId() ) );

    assertEquals( expectedEffectiveAces2, repo.getEffectiveAces( newFolder.getId(), false ) );

    assertEquals( expectedEffectiveAces1, repo.getEffectiveAces( newFolder.getId(), true ) );
  }

  @Test
  public void testUpdateAcl() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );
    RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acl );
    RepositoryFileSid tiffanySid = new RepositoryFileSid(
        userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) );
    newAclBuilder.owner( tiffanySid );
    repo.updateAcl( newAclBuilder.build() );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( new RepositoryFileSid( USERNAME_TIFFANY ), fetchedAcl.getOwner() );
  }

  @Test
  public void testCreateFolderWithAcl() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    RepositoryFileSid tiffanySid = new RepositoryFileSid(
        userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ) );
    RepositoryFileSid suzySid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    // tiffany owns it but suzy is creating it
    RepositoryFileAcl.Builder aclBuilder = new RepositoryFileAcl.Builder( tiffanySid );
    // need this to be able to fetch acl as suzy
    aclBuilder.ace( suzySid, RepositoryFilePermission.READ );
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, aclBuilder.build(), null );
    RepositoryFileAcl fetchedAcl = repo.getAcl( newFolder.getId() );
    assertEquals( new RepositoryFileSid( USERNAME_TIFFANY ), fetchedAcl.getOwner() );
    assertLocalAceExists( newFolder, new RepositoryFileSid( USERNAME_SUZY ),
        EnumSet.of( RepositoryFilePermission.READ ) );
  }

  /**
   * Tests parent ACL's contribution to decision. // This test is bogus, it doesn't actually try the delete
   */
  @Test
  public void testDeleteInheritingFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    defaultBackingRepositoryLifecycleManager.newTenant();
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile newFile =
        createSampleFile( repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY ) ).getPath(),
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl acl =
        new RepositoryFileAcl.Builder( newFile.getId(), userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ),
            RepositoryFileSid.Type.USER ).entriesInheriting( false ).build();
    repo.updateAcl( acl );
  }

  /**
   * Tests deleting a file when no delete permission is given to the role
   */
  @Test
  public void testDeleteWhenNoDeletePermissionOnFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();
    RepositoryFile publicFolderFile =
        createSampleFile( repo.getFile(
                ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) ).getPath(),
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new RepositoryFileAcl.Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), RepositoryFileSid.Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ, RepositoryFilePermission.WRITE ).build();
    repo.updateAcl( publicFolderFileAcl );

    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAuthenticatedRoleName } );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( publicFolderFile.getId(), null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( publicFolderFile.getId(), null );
      assertTrue( true );
    } catch ( UnifiedRepositoryException e ) {
      fail();
    }
  }

  /**
   * Tests deleting a file when no delete permission is given to the role
   */
  @Test
  public void testWriteWhenNoWritePermissionOnFile() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    defaultBackingRepositoryLifecycleManager.newTenant();
    RepositoryFile publicFolderFile =
        createSampleFile( repo.getFile(
                ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) ).getPath(),
            "helloworld.sample", "ddfdf", false, 83 );
    RepositoryFileAcl publicFolderFileAcl =
        new RepositoryFileAcl.Builder( publicFolderFile.getId(), userNameUtils.getPrincipleId( tenantAcme,
            USERNAME_ADMIN ), RepositoryFileSid.Type.USER ).entriesInheriting( false ).ace(
            new RepositoryFileSid( roleNameUtils.getPrincipleId( tenantAcme, tenantAuthenticatedRoleName ),
                RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ ).build();
    repo.updateAcl( publicFolderFileAcl );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAuthenticatedRoleName } );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String modSampleString = "Ciao World!";
    final boolean modSampleBoolean = true;
    final int modSampleInteger = 99;

    final SampleRepositoryFileData modContent =
        new SampleRepositoryFileData( modSampleString, modSampleBoolean, modSampleInteger );

    try {

      repo.updateFile( publicFolderFile, modContent, null );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    try {
      repo.updateFile( publicFolderFile, modContent, null );
      assertTrue( true );
    } catch ( UnifiedRepositoryException e ) {
      fail();
    }
  }

  /**
   * Tests Updating the ACL when no GRANT_PERMISSION is assigned
   *
   */
  @Test
  public void testUpdatingPermissionWhenNoGrantPermissionOnFile() throws Exception {

    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", new String[] { tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_TIFFANY, PASSWORD, "", new String[] { tenantAuthenticatedRoleName } );

    defaultBackingRepositoryLifecycleManager.newTenant();

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFolder = new RepositoryFile.Builder( "test" ).folder( true ).versioned( true ).build();
    newFolder = repo.createFolder( parentFolder.getId(), newFolder, null );

    RepositoryFileAcl acls = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_TIFFANY ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.READ );
    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_TIFFANY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    RepositoryFileAcl newAcl = repo.getAcl( newFolder.getId() );

    RepositoryFileAcl.Builder anotherNewAclBuilder = new RepositoryFileAcl.Builder( newAcl );
    anotherNewAclBuilder.ace( new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme,
            tenantAuthenticatedRoleName ), RepositoryFileSid.Type.ROLE ), RepositoryFilePermission.READ,
        RepositoryFilePermission.WRITE, RepositoryFilePermission.DELETE );

    try {
      repo.updateAcl( anotherNewAclBuilder.build() );
      fail();
    } catch ( UnifiedRepositoryException e ) {
      assertNotNull( e );
    }

  }

  @Test
  public void testDeleteSid() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantDuff, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    IPentahoUser userGeorge = userRoleDao.createUser( tenantDuff, USERNAME_GEORGE, PASSWORD, "", null );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );

    login( USERNAME_GEORGE, tenantDuff, new String[] { tenantAuthenticatedRoleName } );

    RepositoryFile parentFolder =
        repo.getFile( ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) );
    RepositoryFile newFile = createSampleFile( parentFolder.getPath(), "hello.xaction", "", false, 2, false );
    RepositoryFileAcl acls = repo.getAcl( newFile.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantDuff, USERNAME_PAT ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL );
    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );

    userRoleDao.deleteUser( userGeorge );
    // TestPrincipalProvider.enableGeorgeAndDuff(false); simulate delete of george who is owner and explicitly in
    // ACE
    RepositoryFile fetchedFile = repo.getFileById( newFile.getId() );
    assertEquals( USERNAME_GEORGE, repo.getAcl( fetchedFile.getId() ).getOwner().getName() );
    assertEquals( RepositoryFileSid.Type.USER, repo.getAcl( fetchedFile.getId() ).getOwner().getType() );

    RepositoryFileAcl updatedAcl = repo.getAcl( newFile.getId() );

    boolean foundGeorge = false;

    for ( RepositoryFileAce ace : updatedAcl.getAces() ) {
      if ( USERNAME_GEORGE.equals( ace.getSid().getName() ) ) {
        foundGeorge = true;
      }
    }
  }

  @Test
  public void testAdminCreate() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    final String expectedName = "helloworld.sample";
    final String sampleString = "Ciao World!";
    final boolean sampleBoolean = true;
    final int sampleInteger = 99;
    final String parentFolderPath = ClientRepositoryPaths.getUserHomeFolderPath( USERNAME_SUZY );
    RepositoryFile newFile =
        createSampleFile( parentFolderPath, expectedName, sampleString, sampleBoolean, sampleInteger );
    RepositoryFileAcl acls = repo.getAcl( newFile.getId() );

    RepositoryFileAcl.Builder newAclBuilder = new RepositoryFileAcl.Builder( acls );
    newAclBuilder.entriesInheriting( false ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ),
        RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL );
    repo.updateAcl( newAclBuilder.build() );

    // newFile = repo.getFile(newFile.getPath());
    JcrRepositoryDumpToFile dumpToFile =
        new JcrRepositoryDumpToFile( testJcrTemplate, jcrTransactionTemplate, repositoryAdminUsername,
            "dumpTestAdminCreate", JcrRepositoryDumpToFile.Mode.CUSTOM );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    try {
      repo.deleteFile( newFile.getId(), null );
    } finally {
      dumpToFile.execute();
    }
  }

  @Test( expected = AccessDeniedException.class )
  public void testRoleAuthorizationPolicyAdministerSecurityAccessDenied() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    roleBindingDao.setRoleBindings( AUTHENTICATED_ROLE_NAME, Arrays.asList( RepositoryReadAction.NAME ) );
  }

  @Test
  public void testRoleAuthorizationPolicyNoBoundLogicalRoles() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );

    // List could come back in any order so check elements individually
    List<String> list = roleBindingDao.getBoundLogicalRoleNames( Arrays.asList( AUTHENTICATED_ROLE_NAME, "ceo" ) );
    assertEquals( 3, list.size() );
    findInList( RepositoryReadAction.NAME, list );
    findInList( SchedulerAction.NAME, list );
    findInList( RepositoryCreateAction.NAME, list );
  }

  private void findInList( String name, List<String> list ) {
    for ( String listName : list ) {
      if ( name.equals( listName ) ) {
        return;
      }
    }
    fail( "One of the 3 roles in the role list did not match" );
  }

  @Test
  public void testRoleAuthorizationPolicyGetAllowedActions() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    // test with null namespace
    List<String> allowedActions = authorizationPolicy.getAllowedActions( null );

    assertEquals( 3, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    // test with explicit namespace
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_REPOSITORY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );

    // test with scheduler
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SCHEDULER );
    assertEquals( 1, allowedActions.size() );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    // test with bogus namespace
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_DOESNOTEXIST );
    assertEquals( 0, allowedActions.size() );

    // login with pat (in tenant duff); pat is granted "Authenticated" so he is allowed
    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    allowedActions = authorizationPolicy.getAllowedActions( null );
    assertEquals( 3, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_REPOSITORY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( RepositoryReadAction.NAME ) );
    assertTrue( allowedActions.contains( RepositoryCreateAction.NAME ) );
    // test with scheduler
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SCHEDULER );
    assertEquals( 1, allowedActions.size() );
    assertTrue( allowedActions.contains( SchedulerAction.NAME ) );
    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_SECURITY );
    assertEquals( 2, allowedActions.size() );
    assertTrue( allowedActions.contains( AdministerSecurityAction.NAME ) );

    allowedActions = authorizationPolicy.getAllowedActions( NAMESPACE_PENTAHO );
    assertEquals( 5, allowedActions.size() );
  }

  @Test
  public void testRoleAuthorizationPolicyTenants() throws Exception {
    ITenant tenantAcme = null;
    List<String> origLogicalRoles = null;
    try {
      loginAsSysTenantAdmin();
      origLogicalRoles =
          roleBindingDao.getBoundLogicalRoleNames( Arrays.asList( "acme_Authenticated" ) );
      tenantAcme =
          tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
              ANONYMOUS_ROLE_NAME );
      userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
      ITenant tenantDuff =
          tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
              ANONYMOUS_ROLE_NAME );
      userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
      userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );
      assertEquals( 5, authorizationPolicy.getAllowedActions( null ).size() );

      login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 3, authorizationPolicy.getAllowedActions( null ).size() );

      // login with admin (in tenant acme)
      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      roleBindingDao
          .setRoleBindings( tenantAuthenticatedRoleName, Arrays.asList( RepositoryReadAction.NAME,
              RepositoryCreateAction.NAME, SchedulerAction.NAME, AdministerSecurityAction.NAME ) );
      assertEquals( 5, authorizationPolicy.getAllowedActions( null ).size() );

      // login with pat (in tenant duff)
      login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 3, authorizationPolicy.getAllowedActions( null ).size() );

      // login with suzy again (in tenant acme); expect additional action for suzy
      login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
      assertEquals( 4, authorizationPolicy.getAllowedActions( null ).size() );
    } finally {
      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      // must do it this way in order to reset the cache
      roleBindingDao.setRoleBindings( tenantAuthenticatedRoleName, origLogicalRoles );
    }
  }

  @Test
  public void testRoleAuthorizationPolicyIsAllowed() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );
    ITenant tenantDuff =
        tenantManager.createTenant( systemTenant, TENANT_ID_DUFF, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantDuff, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );
    userRoleDao.createUser( tenantDuff, USERNAME_PAT, PASSWORD, "", null );

    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertFalse( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );

    login( USERNAME_PAT, tenantDuff, new String[] { tenantAuthenticatedRoleName } );
    assertTrue( authorizationPolicy.isAllowed( RepositoryReadAction.NAME ) );
    assertTrue( authorizationPolicy.isAllowed( RepositoryCreateAction.NAME ) );
    assertFalse( authorizationPolicy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  @Test
  public void testRoleAuthorizationPolicyRemoveImmutableBinding() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      roleBindingDao.setRoleBindings( tenantAdminRoleName, Arrays.asList( RepositoryReadAction.NAME,
          RepositoryCreateAction.NAME ) );
      fail();
    } catch ( Exception e ) {
      //ignored
    }
  }

  @Test
  // @Test(expected = AccessDeniedException.class)
  public
    void testRoleAuthorizationPolicyGetRoleBindingStructAccessDenied() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    // login with user that is not allowed to "administer security"
    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
  }

  /**
   * Please keep this test the last of the testRoleAuthorizationPolicy* since it adds a binding that cannot be
   * deleted, only set to no associated logical roles.
   */
  @Test
  public void testRoleAuthorizationPolicyGetRoleBindingStruct() throws Exception {
    ITenant tenantAcme = null;
    loginAsSysTenantAdmin();
    tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    RoleBindingStruct struct = roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
    assertNotNull( struct );
    assertNotNull( struct.bindingMap );
    assertEquals( 3, struct.bindingMap.size() );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME, AdministerSecurityAction.NAME, PublishAction.NAME } ), struct.bindingMap
        .get( superAdminRoleName ) );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME, AdministerSecurityAction.NAME, PublishAction.NAME } ), struct.bindingMap
        .get( tenantAdminRoleName ) );
    assertEquals( Arrays.asList( new String[] { RepositoryReadAction.NAME, RepositoryCreateAction.NAME,
      SchedulerAction.NAME } ), struct.bindingMap.get( tenantAuthenticatedRoleName ) );
    roleBindingDao.setRoleBindings( "whatever", Arrays.asList( "org.pentaho.p1.reader" ) );

    struct = roleBindingDao.getRoleBindingStruct( Locale.getDefault().toString() );
    assertEquals( 4, struct.bindingMap.size() );
    assertEquals( Arrays.asList( new String[] { "org.pentaho.p1.reader" } ), struct.bindingMap.get( "whatever" ) );

    assertNotNull( struct.logicalRoleNameMap );
    assertEquals( 5, struct.logicalRoleNameMap.size() );
    assertEquals( "Create Content", struct.logicalRoleNameMap.get( RepositoryCreateAction.NAME ) );

    assertNotNull( struct.immutableRoles );
    assertEquals( 2, struct.immutableRoles.size() );
    assertTrue( struct.immutableRoles.contains( superAdminRoleName ) );
    assertTrue( struct.immutableRoles.contains( tenantAdminRoleName ) );
  }

  @Test
  public void testDeleteInheritingFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );

    // Try an inheriting folder delete
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      RepositoryFile newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder" ).folder( true ).build(),
              null, null );

      RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

      RepositoryFileAcl newAcl =
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFolder.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }

    // Now try one not inheriting
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      RepositoryFile newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder2" ).folder( true ).build(),
              null, null );

      RepositoryFileAcl acl = repo.getAcl( newFolder.getId() );

      RepositoryFileAcl newAcl =
          new RepositoryFileAcl.Builder( acl ).clearAces().ace(
              userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ), RepositoryFileSid.Type.USER,
              RepositoryFilePermission.ALL ).entriesInheriting( false ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFolder.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }
  }

  @Test
  public void testDeleteInheritingFile2() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );

    RepositoryFile newFolder = null;
    // Try an inheriting file delete
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {
      newFolder =
          repo.createFolder( parentFolder.getId(), new RepositoryFile.Builder( "testFolder" ).folder( true ).build(),
              null, null );

      RepositoryFile newFile =
          repo.createFile( newFolder.getId(), new RepositoryFile.Builder( "testFile" ).folder( false ).build(),
              content, null );

      RepositoryFileAcl acl = repo.getAcl( newFile.getId() );

      RepositoryFileAcl newAcl =
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( true ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFile.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }

    // Now try one not inheriting
    //CHECKSTYLE IGNORE AvoidNestedBlocks FOR NEXT 3 LINES
    {

      RepositoryFile newFile =
          repo.createFile( newFolder.getId(), new RepositoryFile.Builder( "testFile" ).folder( false ).build(),
              content, null );

      RepositoryFileAcl acl = repo.getAcl( newFile.getId() );

      RepositoryFileAcl newAcl =
          new RepositoryFileAcl.Builder( acl ).ace( userNameUtils.getPrincipleId( tenantAcme, USERNAME_ADMIN ),
              RepositoryFileSid.Type.USER, RepositoryFilePermission.ALL ).entriesInheriting( false ).build();
      repo.updateAcl( newAcl );

      login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
      try {
        repo.deleteFile( newFile.getId(), null );
      } catch ( Exception e ) {
        e.printStackTrace();
        fail();
      }
    }
  }

  @Test
  public void testInheritingNodeRemoval() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    final String parentFolderPath = ClientRepositoryPaths.getPublicFolderPath();
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );

    DataNode node = new DataNode( "kdjd" );
    node.setProperty( "ddf", "ljsdfkjsdkf" );
    DataNode newChild1 = node.addNode( "herfkmdx" );

    NodeRepositoryFileData data = new NodeRepositoryFileData( node );
    RepositoryFile repoFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( "test" ).build(), data, null );
    RepositoryFileAcl acl = repo.getAcl( repoFile.getId() );

    RepositoryFileSid suzySid = new RepositoryFileSid( userNameUtils.getPrincipleId( tenantAcme, USERNAME_SUZY ) );
    RepositoryFileAcl.Builder newAclBuilder =
        new RepositoryFileAcl.Builder( acl ).ace( suzySid, EnumSet.of( RepositoryFilePermission.READ,
            RepositoryFilePermission.WRITE ) );

    repo.updateAcl( newAclBuilder.build() );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );
    repoFile = repo.getFile( repoFile.getPath() );

    node = new DataNode( "kdjd" );
    node.setProperty( "foo", "bar" );
    newChild1 = node.addNode( "sdfsdf" );

    data = new NodeRepositoryFileData( node );
    repo.updateFile( repoFile, data, "testUpdate" );

  }

  @Test
  public void testDeleteUsersFolder() throws Exception {
    loginAsSysTenantAdmin();
    ITenant tenantAcme =
        tenantManager.createTenant( systemTenant, TENANT_ID_ACME, tenantAdminRoleName, tenantAuthenticatedRoleName,
            ANONYMOUS_ROLE_NAME );
    userRoleDao.createUser( tenantAcme, USERNAME_ADMIN, PASSWORD, "", new String[] { tenantAdminRoleName } );

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    userRoleDao.createUser( tenantAcme, USERNAME_SUZY, PASSWORD, "", null );

    login( USERNAME_SUZY, tenantAcme, new String[] { tenantAuthenticatedRoleName } );

    final String parentFolderPath =
        ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() );
    RepositoryFile parentFolder = repo.getFile( parentFolderPath );
    final String dataString = "Hello World!";
    final String encoding = "UTF-8";
    byte[] data = dataString.getBytes( encoding );
    ByteArrayInputStream dataStream = new ByteArrayInputStream( data );
    final String mimeType = "text/plain";
    final String fileName = "helloworld.xaction";

    final SimpleRepositoryFileData content = new SimpleRepositoryFileData( dataStream, encoding, mimeType );
    RepositoryFile newFile =
        repo.createFile( parentFolder.getId(), new RepositoryFile.Builder( fileName ).build(), content, null );
    final String filePath = parentFolderPath + RepositoryFile.SEPARATOR + fileName;

    login( USERNAME_ADMIN, tenantAcme, new String[] { tenantAdminRoleName, tenantAuthenticatedRoleName } );
    try {
      repo.deleteFile( repo.getFile( parentFolderPath ).getId(), null );
    } catch ( Exception e ) {
      e.printStackTrace();
      fail();
    }

  }

  private void assertLocalAclEmpty( final RepositoryFile file ) {
    RepositoryFileAcl acl = repo.getAcl( file.getId() );
    assertTrue( acl.getAces().size() == 0 );
  }

  private void assertLocalAceExists( final RepositoryFile file, final RepositoryFileSid sid,
                                     final EnumSet<RepositoryFilePermission> permissions ) {
    RepositoryFileAcl acl = repo.getAcl( file.getId() );

    List<RepositoryFileAce> aces = acl.getAces();
    for ( RepositoryFileAce ace : aces ) {
      if ( sid.equals( ace.getSid() ) && permissions.equals( ace.getPermissions() ) ) {
        return;
      }
    }
    fail();
  }
}
