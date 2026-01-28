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


package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.ICronJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobScheduleParam;
import org.pentaho.platform.api.scheduler2.IJobScheduleRequest;
import org.pentaho.platform.api.scheduler2.ISimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.JobState;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSession.ManifestFile;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SolutionImportHandlerTest {

  private SolutionImportHandler importHandler;

  private IUserRoleDao userRoleDao;
  private IUnifiedRepository repository;
  private IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;
  private IPlatformMimeResolver mockMimeResolver;

  @Before
  public void setUp() throws Exception {
    userRoleDao = mockToPentahoSystem( IUserRoleDao.class );
    repository = mockToPentahoSystem( IUnifiedRepository.class );
    roleAuthorizationPolicyRoleBindingDao = mockToPentahoSystem( IRoleAuthorizationPolicyRoleBindingDao.class );

    List<IMimeType> mimeTypes = new ArrayList<>();
    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = Mockito.mockStatic( PentahoSystem.class ) ) {
      mockMimeResolver = mock( IPlatformMimeResolver.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( IPlatformMimeResolver.class ) )
        .thenReturn( mockMimeResolver );
      importHandler = spy( new SolutionImportHandler( mimeTypes ) );
    }

    when( importHandler.getImportSession() ).thenReturn( mock( ImportSession.class ) );
    when( importHandler.getLogger() ).thenReturn( mock( Log.class ) );
  }

  private <T> T mockToPentahoSystem( Class<T> cl ) {
    T t = mock( cl );
    PentahoSystem.registerObject( t );
    return t;
  }

  @Test
  public void testImportUsers_oneUserManyRoles() {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setRole( "product owner" );
    user.setRole( "cat herder" );
    user.setPassword( "password" );
    users.add( user );

    var importState = new SolutionImportHandler.ImportState();
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users, importState );

    Assert.assertEquals( 3, rolesToUsers.size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "product owner" ).get( 0 ) );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "cat herder" ).get( 0 ) );

    String[] strings = {};

    verify( userRoleDao ).createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    verify( userRoleDao, never() )
      .setUserRoles( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.any( strings.getClass() ) );
    verify( userRoleDao, never() )
      .setPassword( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportUsers_manyUserManyRoles() {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setRole( "product owner" );
    user.setRole( "cat herder" );
    user.setPassword( "password" );
    users.add( user );

    UserExport user2 = new UserExport();
    user2.setUsername( "the dude" );
    user2.setRole( "coder" );
    user2.setRole( "awesome" );
    user2.setPassword( "password" );
    users.add( user2 );

    var importState = new SolutionImportHandler.ImportState();
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users, importState );

    Assert.assertEquals( 4, rolesToUsers.size() );
    Assert.assertEquals( 2, rolesToUsers.get( "coder" ).size() );
    Assert.assertEquals( 1, rolesToUsers.get( "product owner" ).size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "product owner" ).get( 0 ) );
    Assert.assertEquals( 1, rolesToUsers.get( "cat herder" ).size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "cat herder" ).get( 0 ) );
    Assert.assertEquals( 1, rolesToUsers.get( "awesome" ).size() );
    Assert.assertEquals( "the dude", rolesToUsers.get( "awesome" ).get( 0 ) );

    String[] strings = {};

    verify( userRoleDao ).createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) );

    verify( userRoleDao ).createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "the dude" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    verify( userRoleDao, never() )
      .setUserRoles( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.any( strings.getClass() ) );
    verify( userRoleDao, never() )
      .setPassword( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportUsers_userAlreadyExists() {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setPassword( "password" );
    users.add( user );
    String[] strings = {};

    when( userRoleDao.createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = true;
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users, importState );

    Assert.assertEquals( 1, rolesToUsers.size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    verify( userRoleDao ).createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    verify( userRoleDao )
      .setUserRoles( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.any( strings.getClass() ) );
    verify( userRoleDao ).setPassword( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportUsers_userAlreadyExists_overwriteFalse() {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setPassword( "password" );
    users.add( user );
    String[] strings = {};

    when( userRoleDao.createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = false;
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users, importState );

    Assert.assertEquals( 1, rolesToUsers.size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    verify( userRoleDao ).createUser(
      ArgumentMatchers.any( ITenant.class ),
      ArgumentMatchers.eq( "scrum master" ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    verify( userRoleDao, never() )
      .setUserRoles( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.any( strings.getClass() ) );
    verify( userRoleDao, never() )
      .setPassword( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportRoles() {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<>();

    RoleExport role = new RoleExport();
    role.setRolename( roleName );
    role.setPermission( permissions );

    List<RoleExport> roles = new ArrayList<>();
    roles.add( role );

    Map<String, List<String>> roleToUserMap = new HashMap<>();
    final List<String> adminUsers = new ArrayList<>();
    adminUsers.add( "admin" );
    adminUsers.add( "root" );
    roleToUserMap.put( roleName, adminUsers );

    String[] userStrings = adminUsers.toArray( new String[] {} );

    var importState = new SolutionImportHandler.ImportState();
    importHandler.importRoles( roles, roleToUserMap, importState );

    verify( userRoleDao ).createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.eq( roleName ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) );
    verify( roleAuthorizationPolicyRoleBindingDao )
      .setRoleBindings( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.eq( roleName ),
        ArgumentMatchers.eq( permissions ) );
  }

  @Test
  public void testImportRoles_roleAlreadyExists() {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<>();

    RoleExport role = new RoleExport();
    role.setRolename( roleName );
    role.setPermission( permissions );

    List<RoleExport> roles = new ArrayList<>();
    roles.add( role );

    Map<String, List<String>> roleToUserMap = new HashMap<>();
    final List<String> adminUsers = new ArrayList<>();
    adminUsers.add( "admin" );
    adminUsers.add( "root" );
    roleToUserMap.put( roleName, adminUsers );

    String[] userStrings = adminUsers.toArray( new String[] {} );

    when( userRoleDao.createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = true;
    importHandler.importRoles( roles, roleToUserMap, importState );

    verify( userRoleDao ).createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    verify( roleAuthorizationPolicyRoleBindingDao )
      .setRoleBindings( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.eq( roleName ), ArgumentMatchers.eq(
        permissions ) );

  }

  @Test
  public void testImportRoles_roleAlreadyExists_overwriteFalse() {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<>();

    RoleExport role = new RoleExport();
    role.setRolename( roleName );
    role.setPermission( permissions );

    List<RoleExport> roles = new ArrayList<>();
    roles.add( role );

    Map<String, List<String>> roleToUserMap = new HashMap<>();
    final List<String> adminUsers = new ArrayList<>();
    adminUsers.add( "admin" );
    adminUsers.add( "root" );
    roleToUserMap.put( roleName, adminUsers );

    String[] userStrings = adminUsers.toArray( new String[] {} );

    when( userRoleDao.createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = false;
    importHandler.importRoles( roles, roleToUserMap, importState );

    verify( userRoleDao ).createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    verify( roleAuthorizationPolicyRoleBindingDao, never() )
      .setRoleBindings( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.eq( roleName ), ArgumentMatchers.eq(
        permissions ) );

  }

  @Test
  public void testImportMetaStore() {
    var importState = new SolutionImportHandler.ImportState();
    String path = "/path/to/file.zip";
    ExportManifestMetaStore manifestMetaStore = new ExportManifestMetaStore( path,
      "metastore",
      "description of the metastore" );

    importHandler.importMetaStore( manifestMetaStore, true, importState );
    Assert.assertEquals( 1, importState.cachedImports.size() );
    Assert.assertNotNull( importState.cachedImports.get( path ) );
  }

  @Test
  public void testImportMetaStore_nullMetastoreManifest() {
    ExportManifest manifest = spy( new ExportManifest() );
    var importState = new SolutionImportHandler.ImportState();

    importHandler.importMetaStore( manifest.getMetaStore(), true, importState );
    Assert.assertEquals( 0, importState.cachedImports.size() );
  }

  @Test
  public void testImportUserSettings() throws Exception {
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = true;
    importHandler.importUserSettings( user, importState );
    verify( userSettingService ).setUserSetting( "pentaho", "theme", "crystal" );
    verify( userSettingService ).setUserSetting( "pentaho", "language", "en_US" );
  }

  @Test
  public void testImportUserSettings_NoOverwrite() {
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = false;

    IUserSetting existingSetting = mock( IUserSetting.class );
    when( userSettingService.getUserSetting( "pentaho", "theme", null ) ).thenReturn( existingSetting );
    when( userSettingService.getUserSetting( "pentaho", "language", null ) ).thenReturn( null );

    importHandler.importUserSettings( user, importState );
    verify( userSettingService, never() ).setUserSetting( "pentaho", "theme", "crystal" );
    verify( userSettingService ).setUserSetting( "pentaho", "language", "en_US" );
    verify( userSettingService ).getUserSetting( "pentaho", "theme", null );
    verify( userSettingService ).getUserSetting( "pentaho", "language", null );
  }

  @Test
  public void testImportGlobalUserSetting() {
    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = true;
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    importHandler.importGlobalUserSettings( settings, importState );

    verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    verify( userSettingService ).setGlobalUserSetting( "showHiddenFiles", "false" );
    verify( userSettingService, never() )
      .getGlobalUserSetting( ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportGlobalUserSetting_noOverwrite() {
    var importState = new SolutionImportHandler.ImportState();
    importState.overwriteFile = false;
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    IUserSetting setting = mock( IUserSetting.class );
    when( userSettingService.getGlobalUserSetting( "language", null ) ).thenReturn( null );
    when( userSettingService.getGlobalUserSetting( "showHiddenFiles", null ) ).thenReturn( setting );

    importHandler.importGlobalUserSettings( settings, importState );

    verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    verify( userSettingService, never() )
      .setGlobalUserSetting( ArgumentMatchers.eq( "showHiddenFiles" ), ArgumentMatchers.nullable( String.class ) );
    verify( userSettingService ).getGlobalUserSetting( "language", null );
    verify( userSettingService ).getGlobalUserSetting( "showHiddenFiles", null );

  }

  @Test
  public void testGetFile() {
    RepositoryFileImportBundle importBundle = new RepositoryFileImportBundle();
    importBundle.setPath( "/BASE_PATH/" );

    RepositoryFile repoFile = new RepositoryFile.Builder( "FILE_NAME" ).build();
    IRepositoryFileBundle fileBundle = new RepositoryFileBundle( repoFile, null, "parentDir", null, "UTF-8", null );
    fileBundle.setPath( "SUB_PATH/" );

    RepositoryFile expectedFile = new RepositoryFile.Builder( "EXPECTED_FILE" ).build();
    when( repository.getFile( "/BASE_PATH/SUB_PATH/FILE_NAME" ) ).thenReturn( expectedFile );
  }

  @Test
  public void testIsFileHidden() {
    IMimeType hiddenMime = mock( IMimeType.class );
    IMimeType visibleMime = mock( IMimeType.class );
    when( hiddenMime.isHidden() ).thenReturn( true );
    when( visibleMime.isHidden() ).thenReturn( false );
    ManifestFile manifestFile = mock( ManifestFile.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "FILE_NAME" ).hidden( true ).build();

    when( manifestFile.isFileHidden() ).thenReturn( true );
    Assert.assertTrue( importHandler.isFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    when( manifestFile.isFileHidden() ).thenReturn( false );
    Assert.assertFalse( importHandler.isFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    when( manifestFile.isFileHidden() ).thenReturn( null );
    Assert.assertTrue( importHandler.isFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    repoFile = new RepositoryFile.Builder( "FILE_NAME" ).hidden( false ).build();
    Assert.assertFalse( importHandler.isFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    when( mockMimeResolver.resolveMimeTypeForFileName( "SOURCE_PATH" ) ).thenReturn( hiddenMime );
    Assert.assertTrue( importHandler.isFileHidden( null, manifestFile, "SOURCE_PATH" ) );

    when( mockMimeResolver.resolveMimeTypeForFileName( "SOURCE_PATH" ) ).thenReturn( visibleMime );
    Assert.assertEquals( RepositoryFile.HIDDEN_BY_DEFAULT, importHandler.isFileHidden( null, manifestFile, "SOURCE_PATH" ) );
  }

  @Test
  public void testIsSchedulable() {
    ManifestFile manifestFile = mock( ManifestFile.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "FILE_NAME" ).schedulable( true ).build();

    when( manifestFile.isFileSchedulable() ).thenReturn( true );
    Assert.assertTrue( importHandler.isSchedulable( repoFile, manifestFile ) );

    when( manifestFile.isFileSchedulable() ).thenReturn( false );
    Assert.assertFalse( importHandler.isSchedulable( repoFile, manifestFile ) );

    when( manifestFile.isFileSchedulable() ).thenReturn( null );
    Assert.assertTrue( importHandler.isSchedulable( repoFile, manifestFile ) );

    Assert.assertEquals( RepositoryFile.SCHEDULABLE_BY_DEFAULT, importHandler.isSchedulable( null, manifestFile ) );
  }

  @Test
  public void testFileIsScheduleInputSource() {
    ExportManifest manifest = mock( ExportManifest.class );
    List<IJobScheduleRequest> scheduleRequests = new ArrayList<>();
    for ( int i = 0; i < 10; i++ ) {
      IJobScheduleRequest jobScheduleRequest = new FakeJobSchedluerRequest();
      jobScheduleRequest.setInputFile( "/public/test/file" + i );
      scheduleRequests.add( jobScheduleRequest );
    }
    Assert.assertFalse( importHandler.fileIsScheduleInputSource( manifest, null ) );

    when( manifest.getScheduleList() ).thenReturn( scheduleRequests );

    Assert.assertFalse( importHandler.fileIsScheduleInputSource( manifest, "/public/file" ) );
    Assert.assertTrue( importHandler.fileIsScheduleInputSource( manifest, "/public/test/file3" ) );
    Assert.assertTrue( importHandler.fileIsScheduleInputSource( manifest, "public/test/file3" ) );
  }
  @After
  public void tearDown() throws Exception {
    ImportSession.getSession().getImportedScheduleJobIds().clear();
    PentahoSystem.clearObjectFactory();
  }
  private class FakeJobSchedluerRequest implements IJobScheduleRequest {
    private String inputFile;
    @Override public void setJobId( String jobId ) {

    }

    @Override public String getJobId() {
      return null;
    }

    @Override public void setJobName( String jobName ) {

    }

    @Override public void setDuration( long duration ) {

    }

    @Override public void setJobState( JobState state ) {

    }

    @Override public void setInputFile( String inputFilePath ) {
      inputFile = inputFilePath;
    }

    @Override public void setOutputFile( String outputFilePath ) {

    }

    @Override public Map<String, String> getPdiParameters() {
      return null;
    }

    @Override public void setPdiParameters( Map<String, String> stringStringHashMap ) {

    }

    @Override public void setActionClass( String value ) {

    }

    @Override public String getActionClass() {
      return null;
    }

    @Override public void setTimeZone( String value ) {

    }

    @Override public String getTimeZone() {
      return null;
    }

    @Override public void setSimpleJobTrigger( ISimpleJobTrigger jobTrigger ) {

    }

    @Override public ISimpleJobTrigger getSimpleJobTrigger() {
      return null;
    }

    @Override public void setCronJobTrigger( ICronJobTrigger cron ) {

    }

    @Override public String getInputFile() {
      return inputFile;
    }

    @Override public String getJobName() {
      return null;
    }

    @Override public String getOutputFile() {
      return null;
    }

    @Override public List<IJobScheduleParam> getJobParameters() {
      return null;
    }

    @Override public void setJobParameters( List<IJobScheduleParam> parameters ) {

    }

    @Override public long getDuration() {
      return 0;
    }

    @Override public JobState getJobState() {
      return null;
    }

    @Override public ICronJobTrigger getCronJobTrigger() {
      return null;
    }
  }
}
