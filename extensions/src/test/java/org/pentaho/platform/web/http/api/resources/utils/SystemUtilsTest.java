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

package org.pentaho.platform.web.http.api.resources.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class SystemUtilsTest {

  private static final String USER = "testUser";

  private static final String USER_HOME_FOLDER = "/home/" + USER;

  private IPentahoObjectFactory pentahoObjectFactory;

  private ITenantedPrincipleNameResolver resolver;

  @Mock
  private IUnifiedRepository repo;

  @Mock
  private IUserRoleListService userRoleListService;

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcherTest() );
  }

  private static class AnyClassMatcherTest implements ArgumentMatcher<Class<?>> {
    @Override
    public boolean matches( final Class<?> arg ) {
      return true;
    }
  }

  @Before
  public void setUp() throws ObjectFactoryException {
    PentahoSystem.init();
    ITenant tenant = mock( ITenant.class );

    resolver = mock( ITenantedPrincipleNameResolver.class );
    doReturn( tenant ).when( resolver ).getTenant( nullable( String.class ) );
    doReturn( USER ).when( resolver ).getPrincipleName( nullable( String.class ) );
    pentahoObjectFactory = mock( IPentahoObjectFactory.class );
    when( pentahoObjectFactory.objectDefined( nullable( String.class ) ) ).thenReturn( true );
    when( pentahoObjectFactory.get( anyClass(), nullable( String.class ), any( IPentahoSession.class ) ) ).thenAnswer(
      invocation -> {
        if ( invocation.getArguments()[0].equals( ITenantedPrincipleNameResolver.class ) ) {
          return resolver;
        }
        return null;
      } );

    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    PentahoSystem.registerObject( userRoleListService );
    PentahoSystem.registerObject( repo );

    IPentahoSession session = mock( IPentahoSession.class );
    doReturn( "sampleSession" ).when( session ).getName();
    PentahoSessionHolder.setSession( session );
  }

  @After
  public void tearDown() {
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.shutdown();
  }

  @Test
  public void testCanAdminister_NoRepositoryReadAction() {
    PentahoSystem.registerObject( mock( IAuthorizationPolicy.class ) );

    assertFalse( SystemUtils.canAdminister() );
  }

  @Test
  public void testCanAdminister_NoRepositoryCreateAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canAdminister() );
  }

  @Test
  public void testCanAdminister_NoAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canAdminister() );
  }


  @Test
  public void testCanAdminister() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canAdminister() );
  }

  @Test
  public void testCanUpload_HomeFolder_NoRepositoryReadAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_HomeFolder_NoRepositoryCreateAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_NoRepositoryFilePermissionWrite() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    doReturn( false ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.WRITE ) );

    assertFalse( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_NoFileFound() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( null ).when( repo ).getFile( any( String.class ) );

    assertFalse( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_NotAFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( false ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    assertFalse( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_HasAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.WRITE ) );

    assertTrue( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_HasPublishAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( PublishAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.WRITE ) );

    assertTrue( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_EmptyUploadDir() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canUpload( "" ) );
  }

  @Test
  public void testCanUpload_EmptyUploadDir_HasAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canUpload( "" ) );
  }

  @Test
  public void testCanUpload_EmptyUploadDir_HasPublishAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( PublishAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canUpload( "" ) );
  }

  @Test
  public void testCanUpload_NotInHomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    doReturn( true ).when( repo ).hasAccess( "/public/test", EnumSet.of( RepositoryFilePermission.WRITE ) );

    assertFalse( SystemUtils.canUpload( "/public/test" ) );
  }

  @Test
  public void testCanUpload_HomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    RepositoryFile file = mock( RepositoryFile.class );
    doReturn( true ).when( file ).isFolder();
    doReturn( file ).when( repo ).getFile( any( String.class ) );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.WRITE ) );

    assertTrue( SystemUtils.canUpload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanUpload_ignoreUploadDirExistenceChecks_HasAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canUpload( "", true ) );
  }

  @Test
  public void testCanUpload_ignoreUploadDirExistenceChecks_HasPublishActions() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( PublishAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canUpload( "", true ) );
  }

  @Test
  public void testCanUpload_ignoreUploadDirExistenceChecks_HomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canUpload( USER_HOME_FOLDER, true ) );
  }

  @Test
  public void testCanDownload_NoRepositoryReadAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canDownload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanDownload_NoRepositoryCreateAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canDownload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanDownload_NoRepositoryFilePermissionRead() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( false ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.READ ) );

    assertFalse( SystemUtils.canDownload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanDownload_HasAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.READ ) );

    assertTrue( SystemUtils.canDownload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testCanDownload_HasDownloadRole() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.READ ) );

    doReturn( List.of( "DownloadRole" ) ).when( userRoleListService ).getRolesForUser( any(), any() );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<JcrTenantUtils> jcrTenantUtils = Mockito.mockStatic( JcrTenantUtils.class ) ) {
      jcrTenantUtils.when( JcrTenantUtils::getUserNameUtils ).thenReturn( resolver );

      pentahoSystem.when( () -> PentahoSystem.get( ITenantedPrincipleNameResolver.class ) ).thenReturn( resolver );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( pentahoObjectFactory );
      pentahoSystem.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( mockAuthPolicy );
      pentahoSystem.when( () -> PentahoSystem.get( IUserRoleListService.class ) ).thenReturn( userRoleListService );
      pentahoSystem.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( repo );
      pentahoSystem.when( PentahoSystem::getDownloadRolesList ).thenReturn( List.of( "DownloadRole" ) );

      assertTrue( SystemUtils.canDownload( USER_HOME_FOLDER ) );
    }
  }

  @Test
  public void testCanDownload_EmptyPath() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.canDownload( "" ) );
  }

  @Test
  public void testCanDownload_EmptyPath_HasAdministerSecurityAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.canDownload( "" ) );
  }



  @Test
  public void testCanDownload_EmptyPath_HasDownloadRole() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( AdministerSecurityAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( List.of( "DownloadRole" ) ).when( userRoleListService ).getRolesForUser( any(), any() );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<JcrTenantUtils> jcrTenantUtils = Mockito.mockStatic( JcrTenantUtils.class ) ) {
      jcrTenantUtils.when( JcrTenantUtils::getUserNameUtils ).thenReturn( resolver );

      pentahoSystem.when( () -> PentahoSystem.get( ITenantedPrincipleNameResolver.class ) ).thenReturn( resolver );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( pentahoObjectFactory );
      pentahoSystem.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( mockAuthPolicy );
      pentahoSystem.when( () -> PentahoSystem.get( IUserRoleListService.class ) ).thenReturn( userRoleListService );
      pentahoSystem.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( repo );
      pentahoSystem.when( PentahoSystem::getDownloadRolesList ).thenReturn( List.of( "DownloadRole" ) );

      assertTrue( SystemUtils.canDownload( "" ) );
    }
  }

  @Test
  public void testCanDownload_NotInHomeFolder() {
    // no administer security action nor download role
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( true ).when( repo ).hasAccess( "/public/test", EnumSet.of( RepositoryFilePermission.READ ) );

    assertFalse( SystemUtils.canDownload( "/public/test" ) );
  }

  @Test
  public void testCanDownload_HomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    doReturn( true ).when( repo ).hasAccess( USER_HOME_FOLDER, EnumSet.of( RepositoryFilePermission.READ ) );

    assertTrue( SystemUtils.canDownload( USER_HOME_FOLDER ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_Null() {
    assertFalse( SystemUtils.validateAccessToHomeFolder( null ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_Empty() {
    assertFalse( SystemUtils.validateAccessToHomeFolder( "" ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_NoRepositoryReadAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_NoRepositoryCreateAction() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( false ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_HomeFolderNotFound() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );

    try ( MockedStatic<PentahoSystem> pentahoSystem = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<JcrTenantUtils> jcrTenantUtils = Mockito.mockStatic( JcrTenantUtils.class );
          MockedStatic<ServerRepositoryPaths> serverRepositoryPaths = Mockito.mockStatic( ServerRepositoryPaths.class ) ) {
      jcrTenantUtils.when( JcrTenantUtils::getUserNameUtils ).thenReturn( resolver );
      serverRepositoryPaths.when( () -> ServerRepositoryPaths.getUserHomeFolderPath( any(), any() ) ).thenReturn( null );
      // getTenantRootFolderPath
      pentahoSystem.when( () -> PentahoSystem.get( ITenantedPrincipleNameResolver.class ) ).thenReturn( resolver );
      pentahoSystem.when( PentahoSystem::getObjectFactory ).thenReturn( pentahoObjectFactory );
      pentahoSystem.when( () -> PentahoSystem.get( IAuthorizationPolicy.class ) ).thenReturn( mockAuthPolicy );
      pentahoSystem.when( () -> PentahoSystem.get( IUserRoleListService.class ) ).thenReturn( userRoleListService );
      pentahoSystem.when( () -> PentahoSystem.get( IUnifiedRepository.class ) ).thenReturn( repo );
      pentahoSystem.when( PentahoSystem::getDownloadRolesList ).thenReturn( List.of( "DownloadRole" ) );

      assertFalse( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER ) );
    }
  }

  @Test
  public void testValidateAccessToHomeFolder_NotInHomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    // subfolder structure in admin home folder matches the user home folder, must return false
    assertFalse( SystemUtils.validateAccessToHomeFolder( "/home/admin/pentaho/tenant0" + USER_HOME_FOLDER ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_FolderNameNormalization() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertFalse( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER + "2" ) );
  }

  @Test
  public void testValidateAccessToHomeFolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER ) );
  }

  @Test
  public void testValidateAccessToHomeFolder_Subfolder() {
    IAuthorizationPolicy mockAuthPolicy = mock( IAuthorizationPolicy.class );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryReadAction.NAME );
    doReturn( true ).when( mockAuthPolicy ).isAllowed( RepositoryCreateAction.NAME );
    PentahoSystem.registerObject( mockAuthPolicy );

    assertTrue( SystemUtils.validateAccessToHomeFolder( USER_HOME_FOLDER + "/subfolder" ) );
  }
}
