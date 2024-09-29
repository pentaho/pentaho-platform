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

package org.pentaho.platform.plugin.services.importer;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
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
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISchedulerResource;
import org.pentaho.platform.api.scheduler2.ISimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.JobState;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
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

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
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

    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

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

    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

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

    importHandler.setOverwriteFile( true );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

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

    importHandler.setOverwriteFile( false );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

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

    importHandler.importRoles( roles, roleToUserMap );

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

    importHandler.setOverwriteFile( true );
    importHandler.importRoles( roles, roleToUserMap );

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

    importHandler.setOverwriteFile( false );
    importHandler.importRoles( roles, roleToUserMap );

    verify( userRoleDao ).createRole( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ),
      ArgumentMatchers.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    verify( roleAuthorizationPolicyRoleBindingDao, never() )
      .setRoleBindings( ArgumentMatchers.any( ITenant.class ), ArgumentMatchers.eq( roleName ), ArgumentMatchers.eq(
        permissions ) );

  }

  @Test
  public void testImportMetaStore() {
    String path = "/path/to/file.zip";
    ExportManifestMetaStore manifestMetaStore = new ExportManifestMetaStore( path,
      "metastore",
      "description of the metastore" );
    importHandler.cachedImports = new HashMap<>();

    importHandler.importMetaStore( manifestMetaStore, true );
    Assert.assertEquals( 1, importHandler.cachedImports.size() );
    Assert.assertNotNull( importHandler.cachedImports.get( path ) );
  }

  @Test
  public void testImportMetaStore_nullMetastoreManifest() {
    ExportManifest manifest = spy( new ExportManifest() );

    importHandler.cachedImports = new HashMap<>();
    importHandler.importMetaStore( manifest.getMetaStore(), true );
    Assert.assertEquals( 0, importHandler.cachedImports.size() );
  }

  @Test
  public void testImportUserSettings() throws Exception {
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    IAnyUserSettingService userSettingService = mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    importHandler.setOverwriteFile( true );

    importHandler.importUserSettings( user );
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
    importHandler.setOverwriteFile( false );

    IUserSetting existingSetting = mock( IUserSetting.class );
    when( userSettingService.getUserSetting( "pentaho", "theme", null ) ).thenReturn( existingSetting );
    when( userSettingService.getUserSetting( "pentaho", "language", null ) ).thenReturn( null );

    importHandler.importUserSettings( user );
    verify( userSettingService, never() ).setUserSetting( "pentaho", "theme", "crystal" );
    verify( userSettingService ).setUserSetting( "pentaho", "language", "en_US" );
    verify( userSettingService ).getUserSetting( "pentaho", "theme", null );
    verify( userSettingService ).getUserSetting( "pentaho", "language", null );
  }

  @Test
  public void testImportGlobalUserSetting() {
    importHandler.setOverwriteFile( true );
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    importHandler.importGlobalUserSettings( settings );

    verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    verify( userSettingService ).setGlobalUserSetting( "showHiddenFiles", "false" );
    verify( userSettingService, never() )
      .getGlobalUserSetting( ArgumentMatchers.nullable( String.class ), ArgumentMatchers.nullable( String.class ) );
  }

  @Test
  public void testImportGlobalUserSetting_noOverwrite() {
    importHandler.setOverwriteFile( false );
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    IUserSetting setting = mock( IUserSetting.class );
    when( userSettingService.getGlobalUserSetting( "language", null ) ).thenReturn( null );
    when( userSettingService.getGlobalUserSetting( "showHiddenFiles", null ) ).thenReturn( setting );

    importHandler.importGlobalUserSettings( settings );

    verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    verify( userSettingService, never() )
      .setGlobalUserSetting( ArgumentMatchers.eq( "showHiddenFiles" ), ArgumentMatchers.nullable( String.class ) );
    verify( userSettingService ).getGlobalUserSetting( "language", null );
    verify( userSettingService ).getGlobalUserSetting( "showHiddenFiles", null );

  }

  @Test
  @Ignore
  public void testImportSchedules() throws Exception {
    List<IJobScheduleRequest> schedules = new ArrayList<>();
    IJobScheduleRequest scheduleRequest = Mockito.spy( new FakeJobSchedluerRequest() );
    schedules.add( scheduleRequest );

    Response response = mock( Response.class );
    when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    when( response.getEntity() ).thenReturn( "job id" );

    doReturn( response ).when( importHandler )
      .createSchedulerJob( ArgumentMatchers.any( ISchedulerResource.class ), ArgumentMatchers.eq( scheduleRequest ) );

    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = Mockito.mockStatic( PentahoSessionHolder.class ) ) {
      IAuthorizationPolicy iAuthorizationPolicyMock = mock( IAuthorizationPolicy.class );
      IScheduler iSchedulerMock = mock( IScheduler.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IAuthorizationPolicy.class ) ) )
        .thenReturn( iAuthorizationPolicyMock );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IScheduler.class ), ArgumentMatchers.anyString(), ArgumentMatchers.eq( null ) ) )
        .thenReturn( iSchedulerMock );
      when( iSchedulerMock.getStatus() ).thenReturn( mock( IScheduler.SchedulerStatus.class ) );
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession )
        .thenReturn( mock( IPentahoSession.class ) );

      importHandler.importSchedules( schedules );

      verify( importHandler )
        .createSchedulerJob( ArgumentMatchers.any( ISchedulerResource.class ), ArgumentMatchers.eq( scheduleRequest ) );
      Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
    }
  }

  @Test
  @Ignore
  public void testImportSchedules_FailsToCreateSchedule() throws Exception {
    List<IJobScheduleRequest> schedules = new ArrayList<>();
    IJobScheduleRequest scheduleRequest = Mockito.spy( new FakeJobSchedluerRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduledTransform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduledTransform*" );
    schedules.add( scheduleRequest );

    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( importHandler ).createSchedulerJob(
      ArgumentMatchers.any( ISchedulerResource.class ), ArgumentMatchers.eq( scheduleRequest ) );

    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = Mockito.mockStatic( PentahoSessionHolder.class ) ) {
      IAuthorizationPolicy iAuthorizationPolicyMock = mock( IAuthorizationPolicy.class );
      IScheduler iSchedulerMock = mock( IScheduler.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IAuthorizationPolicy.class ) ) )
        .thenReturn( iAuthorizationPolicyMock );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IScheduler.class ), ArgumentMatchers.anyString(), ArgumentMatchers.eq( null ) ) )
        .thenReturn( iSchedulerMock );
      when( iSchedulerMock.getStatus() ).thenReturn( mock( IScheduler.SchedulerStatus.class ) );
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession )
        .thenReturn( mock( IPentahoSession.class ) );

      importHandler.importSchedules( schedules );
      Assert.assertEquals( 0, ImportSession.getSession().getImportedScheduleJobIds().size() );
    }
  }

  @Test
  @Ignore
  public void testImportSchedules_FailsToCreateScheduleWithSpace() throws Exception {
    List<IJobScheduleRequest> schedules = new ArrayList<>();
    IJobScheduleRequest scheduleRequest = Mockito.spy( new FakeJobSchedluerRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduled Transform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduled Transform*" );
    schedules.add( scheduleRequest );

    ScheduleRequestMatcher throwMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( importHandler ).createSchedulerJob(
      ArgumentMatchers.any( ISchedulerResource.class ), ArgumentMatchers.argThat( throwMatcher ) );

    Response response = mock( Response.class );
    when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    doReturn( response ).when( importHandler ).createSchedulerJob( ArgumentMatchers.any( ISchedulerResource.class ),
      ArgumentMatchers.argThat( goodMatcher ) );

    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = Mockito.mockStatic( PentahoSystem.class );
    MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = Mockito.mockStatic( PentahoSessionHolder.class ) ) {
      IAuthorizationPolicy iAuthorizationPolicyMock = mock( IAuthorizationPolicy.class );
      IScheduler iSchedulerMock = mock( IScheduler.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IAuthorizationPolicy.class ) ) ).thenReturn( iAuthorizationPolicyMock );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IScheduler.class ), ArgumentMatchers.anyString(), ArgumentMatchers.eq( null ) ) )
        .thenReturn( iSchedulerMock );
      when( iSchedulerMock.getStatus() ).thenReturn( mock( IScheduler.SchedulerStatus.class ) );
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession ).thenReturn( mock( IPentahoSession.class ) );
      importHandler.importSchedules( schedules );
      verify( importHandler, times( 2 ) ).createSchedulerJob(
        ArgumentMatchers.any( ISchedulerResource.class ), ArgumentMatchers.any( IJobScheduleRequest.class ) );
      Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
    }
  }

  @Test
  @Ignore
  public void testImportSchedules_FailsToCreateScheduleWithSpaceOnWindows() throws Exception {
    String sep = File.separator;
    System.setProperty( "file.separator", "\\" );
    List<IJobScheduleRequest> schedules = new ArrayList<>();
    IJobScheduleRequest scheduleRequest = Mockito.spy( new FakeJobSchedluerRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduled Transform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduled Transform*" );
    schedules.add( scheduleRequest );

    ScheduleRequestMatcher throwMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( importHandler ).createSchedulerJob(
      ArgumentMatchers.nullable( ISchedulerResource.class ), ArgumentMatchers.argThat( throwMatcher ) );

    Response response = mock( Response.class );
    when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    doReturn( response ).when( importHandler ).createSchedulerJob( ArgumentMatchers.nullable( ISchedulerResource.class ),
      ArgumentMatchers.argThat( goodMatcher ) );

    try ( MockedStatic<PentahoSystem> pentahoSystemMockedStatic = Mockito.mockStatic( PentahoSystem.class );
          MockedStatic<PentahoSessionHolder> pentahoSessionHolderMockedStatic = Mockito.mockStatic( PentahoSessionHolder.class ) ) {
      IAuthorizationPolicy iAuthorizationPolicyMock = mock( IAuthorizationPolicy.class );
      IScheduler iSchedulerMock = mock( IScheduler.class );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IAuthorizationPolicy.class ) ) )
        .thenReturn( iAuthorizationPolicyMock );
      pentahoSystemMockedStatic.when( () -> PentahoSystem.get( ArgumentMatchers.eq( IScheduler.class ), ArgumentMatchers.anyString(), ArgumentMatchers.eq( null ) ) )
        .thenReturn( iSchedulerMock );
      when( iSchedulerMock.getStatus() ).thenReturn( mock( IScheduler.SchedulerStatus.class ) );
      pentahoSessionHolderMockedStatic.when( PentahoSessionHolder::getSession )
        .thenReturn( mock( IPentahoSession.class ) );

      importHandler.importSchedules( schedules );
      verify( importHandler, times( 2 ) )
        .createSchedulerJob( ArgumentMatchers.nullable( ISchedulerResource.class ), ArgumentMatchers.nullable( IJobScheduleRequest.class ) );
      Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
      System.setProperty( "file.separator", sep );
    }
  }

  private static class ScheduleRequestMatcher implements ArgumentMatcher<IJobScheduleRequest> {
    private final String input;
    private final String output;

    public ScheduleRequestMatcher( String input, String output ) {
      this.input = input;
      this.output = output;
    }

    @Override public boolean matches( IJobScheduleRequest jsr ) {
      boolean matchedInput = input.equals( FilenameUtils.separatorsToUnix( jsr.getInputFile() ) );
      boolean matchedOutput = output.equals( FilenameUtils.separatorsToUnix( jsr.getOutputFile() ) );
      return matchedInput && matchedOutput;
    }
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
