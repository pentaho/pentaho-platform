/*!
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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
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
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerResource;

import mockit.Deencapsulation;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;

@RunWith( JMockit.class )
public class SolutionImportHandlerTest {

  private SolutionImportHandler importHandler;

  private IUserRoleDao userRoleDao;
  private IUnifiedRepository repository;
  private IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;
  private SolutionFileImportHelper solutionHelper;

  @Before
  public void setUp() throws Exception {
    userRoleDao = mockToPentahoSystem( IUserRoleDao.class );
    repository = mockToPentahoSystem( IUnifiedRepository.class );
    roleAuthorizationPolicyRoleBindingDao = mockToPentahoSystem( IRoleAuthorizationPolicyRoleBindingDao.class );

    List<IMimeType> mimeTypes = new ArrayList<>();
    importHandler = new SolutionImportHandler( mimeTypes );

    solutionHelper = Mockito.mock( SolutionFileImportHelper.class );
    Deencapsulation.setField( importHandler, "solutionHelper", solutionHelper );
  }

  private <T> T mockToPentahoSystem( Class<T> cl ) {
    T t = Mockito.mock( cl );
    PentahoSystem.registerObject( t );
    return t;
  }

  @Test
  public void testImportUsers_oneUserManyRoles() throws Exception {
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

    Mockito.verify( userRoleDao ).createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    Mockito.verify( userRoleDao, Mockito.never() )
      .setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() )
      .setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testImportUsers_manyUserManyRoles() throws Exception {
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

    Mockito.verify( userRoleDao ).createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) );

    Mockito.verify( userRoleDao ).createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "the dude" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    Mockito.verify( userRoleDao, Mockito.never() )
      .setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() )
      .setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testImportUsers_userAlreadyExists() throws Exception {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setPassword( "password" );
    users.add( user );
    String[] strings = {};

    Mockito.when( userRoleDao.createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( true );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

    Assert.assertEquals( 1, rolesToUsers.size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    Mockito.verify( userRoleDao ).createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    Mockito.verify( userRoleDao )
      .setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao ).setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testImportUsers_userAlreadyExists_overwriteFalse() throws Exception {
    List<UserExport> users = new ArrayList<>();
    UserExport user = new UserExport();
    user.setUsername( "scrum master" );
    user.setRole( "coder" );
    user.setPassword( "password" );
    users.add( user );
    String[] strings = {};

    Mockito.when( userRoleDao.createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( false );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

    Assert.assertEquals( 1, rolesToUsers.size() );
    Assert.assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    Mockito.verify( userRoleDao ).createUser(
      Mockito.any( ITenant.class ),
      Mockito.eq( "scrum master" ),
      Mockito.anyString(),
      Mockito.anyString(),
      Mockito.any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    Mockito.verify( userRoleDao, Mockito.never() )
      .setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() )
      .setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testImportRoles() throws Exception {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<String>();

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

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.anyString(),
      Mockito.any( userStrings.getClass() ) );
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao )
      .setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ),
        Mockito.eq( permissions ) );
  }

  @Test
  public void testImportRoles_roleAlreadyExists() throws Exception {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<String>();

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

    Mockito.when( userRoleDao.createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(),
      Mockito.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( true );
    importHandler.importRoles( roles, roleToUserMap );

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(),
      Mockito.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao )
      .setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.eq(
        permissions ) );

  }

  @Test
  public void testImportRoles_roleAlreadyExists_overwriteFalse() throws Exception {
    String roleName = "ADMIN";
    List<String> permissions = new ArrayList<String>();

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

    Mockito.when( userRoleDao.createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(),
      Mockito.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( false );
    importHandler.importRoles( roles, roleToUserMap );

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(),
      Mockito.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao, Mockito.never() )
      .setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.eq(
        permissions ) );

  }

  @Test
  public void testImportMetaStore() throws Exception {
    ExportManifest manifest = Mockito.spy( new ExportManifest() );
    String path = "/path/to/file.zip";
    ExportManifestMetaStore manifestMetaStore = new ExportManifestMetaStore( path,
      "metastore",
      "description of the metastore" );
    importHandler.cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();

    Mockito.when( manifest.getMetaStore() ).thenReturn( manifestMetaStore );

    importHandler.importMetaStore( manifest, true );
    Assert.assertEquals( 1, importHandler.cachedImports.size() );
    Assert.assertTrue( importHandler.cachedImports.get( path ) != null );
  }

  @Test
  public void testImportMetaStore_nullMetastoreManifest() throws Exception {
    ExportManifest manifest = Mockito.spy( new ExportManifest() );

    importHandler.cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();
    importHandler.importMetaStore( manifest, true );
    Assert.assertEquals( 0, importHandler.cachedImports.size() );
  }

  @Test
  public void testImportUserSettings() throws Exception {
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    IAnyUserSettingService userSettingService = Mockito.mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    importHandler.setOverwriteFile( true );

    importHandler.importUserSettings( user );
    Mockito.verify( userSettingService ).setUserSetting( "pentaho", "theme", "crystal" );
    Mockito.verify( userSettingService ).setUserSetting( "pentaho", "language", "en_US" );
  }

  @Test
  public void testImportUserSettings_NoOverwrite() throws Exception {
    UserExport user = new UserExport();
    user.setUsername( "pentaho" );
    user.addUserSetting( new ExportManifestUserSetting( "theme", "crystal" ) );
    user.addUserSetting( new ExportManifestUserSetting( "language", "en_US" ) );
    IAnyUserSettingService userSettingService = Mockito.mock( IAnyUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    importHandler.setOverwriteFile( false );

    IUserSetting existingSetting = Mockito.mock( IUserSetting.class );
    Mockito.when( userSettingService.getUserSetting( "pentaho", "theme", null ) ).thenReturn( existingSetting );
    Mockito.when( userSettingService.getUserSetting( "pentaho", "language", null ) ).thenReturn( null );

    importHandler.importUserSettings( user );
    Mockito.verify( userSettingService, Mockito.never() ).setUserSetting( "pentaho", "theme", "crystal" );
    Mockito.verify( userSettingService ).setUserSetting( "pentaho", "language", "en_US" );
    Mockito.verify( userSettingService ).getUserSetting( "pentaho", "theme", null );
    Mockito.verify( userSettingService ).getUserSetting( "pentaho", "language", null );
  }

  @Test
  public void testImportGlobalUserSetting() throws Exception {
    importHandler.setOverwriteFile( true );
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = Mockito.mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    importHandler.importGlobalUserSettings( settings );

    Mockito.verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    Mockito.verify( userSettingService ).setGlobalUserSetting( "showHiddenFiles", "false" );
    Mockito.verify( userSettingService, Mockito.never() )
      .getGlobalUserSetting( Mockito.anyString(), Mockito.anyString() );
  }

  @Test
  public void testImportGlobalUserSetting_noOverwrite() throws Exception {
    importHandler.setOverwriteFile( false );
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = Mockito.mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );
    IUserSetting setting = Mockito.mock( IUserSetting.class );
    Mockito.when( userSettingService.getGlobalUserSetting( "language", null ) ).thenReturn( null );
    Mockito.when( userSettingService.getGlobalUserSetting( "showHiddenFiles", null ) ).thenReturn( setting );

    importHandler.importGlobalUserSettings( settings );

    Mockito.verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    Mockito.verify( userSettingService, Mockito.never() )
      .setGlobalUserSetting( Mockito.eq( "showHiddenFiles" ), Mockito.anyString() );
    Mockito.verify( userSettingService ).getGlobalUserSetting( "language", null );
    Mockito.verify( userSettingService ).getGlobalUserSetting( "showHiddenFiles", null );

  }

  @Test
  public void testImportSchedules() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = Mockito.spy( new JobScheduleRequest() );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = Mockito.spy( importHandler );
    Response response = Mockito.mock( Response.class );
    Mockito.when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    Mockito.when( response.getEntity() ).thenReturn( "job id" );

    Mockito.doReturn( response ).when( spyHandler )
      .createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.eq( scheduleRequest ) );

    mockSchedulerPause();

    spyHandler.importSchedules( schedules );

    Mockito.verify( spyHandler )
      .createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.eq( scheduleRequest ) );
    Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
  }

  private void mockSchedulerPause() {
    SchedulerResource schedulerResource = new SchedulerResource();
    new NonStrictExpectations( SchedulerResource.class ) {
      {
        schedulerResource.pause();
        times = 1;

        schedulerResource.start();
        times = 1;

        schedulerResource.getAllJobs();
        result = null;
      }
    };
  }

  @Test
  public void testImportSchedules_FailsToCreateSchedule() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = Mockito.spy( new JobScheduleRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduledTransform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduledTransform*" );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = Mockito.spy( importHandler );

    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.eq( scheduleRequest ) );

    mockSchedulerPause();

    spyHandler.importSchedules( schedules );
    Assert.assertEquals( 0, ImportSession.getSession().getImportedScheduleJobIds().size() );
  }

  @Test
  public void testImportSchedules_FailsToCreateScheduleWithSpace() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = Mockito.spy( new JobScheduleRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduled Transform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduled Transform*" );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = Mockito.spy( importHandler );

    ScheduleRequestMatcher throwMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.argThat( throwMatcher ) );

    Response response = Mockito.mock( Response.class );
    Mockito.when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    Mockito.when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    Mockito.doReturn( response ).when( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ),
      Mockito.argThat( goodMatcher ) );

    mockSchedulerPause();

    spyHandler.importSchedules( schedules );
    Mockito.verify( spyHandler, Mockito.times( 2 ) ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.any( JobScheduleRequest.class ) );
    Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
  }

  @Test
  public void testImportSchedules_FailsToCreateScheduleWithSpaceOnWindows() throws Exception {
    String sep = File.separator;
    System.setProperty( "file.separator", "\\" );
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = Mockito.spy( new JobScheduleRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduled Transform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduled Transform*" );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = Mockito.spy( importHandler );

    ScheduleRequestMatcher throwMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.argThat( throwMatcher ) );

    Response response = Mockito.mock( Response.class );
    Mockito.when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    Mockito.when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher =
      new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    Mockito.doReturn( response ).when( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ),
      Mockito.argThat( goodMatcher ) );

    mockSchedulerPause();

    spyHandler.importSchedules( schedules );
    Mockito.verify( spyHandler, Mockito.times( 2 ) )
      .createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.any( JobScheduleRequest.class ) );
    Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
    System.setProperty( "file.separator", sep );
  }

  private class ScheduleRequestMatcher extends ArgumentMatcher<JobScheduleRequest> {
    private String input;
    private String output;

    public ScheduleRequestMatcher( String input, String output ) {
      this.input = input;
      this.output = output;
    }

    @Override public boolean matches( Object argument ) {
      JobScheduleRequest jsr = (JobScheduleRequest) argument;
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
    Mockito.when( repository.getFile( "/BASE_PATH/SUB_PATH/FILE_NAME" ) ).thenReturn( expectedFile );

    RepositoryFile actualFile = Deencapsulation.invoke( importHandler, "getFile", importBundle, fileBundle );

    Assert.assertEquals( expectedFile, actualFile );
  }

  @Test
  public void testIsFileHidden() {
    ManifestFile manifestFile = Mockito.mock( ManifestFile.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "FILE_NAME" ).hidden( true ).build();

    Mockito.when( manifestFile.isFileHidden() ).thenReturn( true );
    Assert.assertTrue( runIsFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    Mockito.when( manifestFile.isFileHidden() ).thenReturn( false );
    Assert.assertFalse( runIsFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    Mockito.when( manifestFile.isFileHidden() ).thenReturn( null );
    Assert.assertTrue( runIsFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    repoFile = new RepositoryFile.Builder( "FILE_NAME" ).hidden( false ).build();
    Assert.assertFalse( runIsFileHidden( repoFile, manifestFile, "SOURCE_PATH" ) );

    Mockito.when( solutionHelper.isInHiddenList( "SOURCE_PATH" ) ).thenReturn( true );
    Assert.assertTrue( runIsFileHidden( null, manifestFile, "SOURCE_PATH" ) );

    Mockito.when( solutionHelper.isInHiddenList( "SOURCE_PATH" ) ).thenReturn( false );
    Assert.assertEquals( RepositoryFile.HIDDEN_BY_DEFAULT, runIsFileHidden( null, manifestFile, "SOURCE_PATH" ) );
  }

  private Boolean runIsFileHidden( RepositoryFile file, ManifestFile manifestFile, String sourcePath ) {
    return Deencapsulation.invoke( importHandler, "isFileHidden", file == null ? RepositoryFile.class : file,
        manifestFile, sourcePath );
  }

  @Test
  public void testIsSchedulable() {
    ManifestFile manifestFile = Mockito.mock( ManifestFile.class );
    RepositoryFile repoFile = new RepositoryFile.Builder( "FILE_NAME" ).schedulable( true ).build();

    Mockito.when( manifestFile.isFileSchedulable() ).thenReturn( true );
    Assert.assertTrue( runIsSchedulable( repoFile, manifestFile ) );

    Mockito.when( manifestFile.isFileSchedulable() ).thenReturn( false );
    Assert.assertFalse( runIsSchedulable( repoFile, manifestFile ) );

    Mockito.when( manifestFile.isFileSchedulable() ).thenReturn( null );
    Assert.assertTrue( runIsSchedulable( repoFile, manifestFile ) );

    Assert.assertEquals( RepositoryFile.SCHEDULABLE_BY_DEFAULT, runIsSchedulable( null, manifestFile ) );
  }

  @Test
  public void testFileIsScheduleInputSource() {
    ExportManifest manifest = Mockito.mock( ExportManifest.class );
    SolutionImportHandler spyHandler = Mockito.spy( importHandler );
    List<JobScheduleRequest> scheduleRequests = new ArrayList<>();
    for ( int i = 0 ; i < 10; i++ ) {
      JobScheduleRequest jobScheduleRequest = new JobScheduleRequest();
      jobScheduleRequest.setInputFile( "/public/test/file" + i );
      scheduleRequests.add( jobScheduleRequest );
    }
    Assert.assertFalse( spyHandler.fileIsScheduleInputSource( manifest, null ) );

    Mockito.when( manifest.getScheduleList() ).thenReturn( scheduleRequests );

    Assert.assertFalse( spyHandler.fileIsScheduleInputSource( manifest, "/public/file" ) );
    Assert.assertTrue( spyHandler.fileIsScheduleInputSource( manifest, "/public/test/file3" ) );
    Assert.assertTrue( spyHandler.fileIsScheduleInputSource( manifest, "public/test/file3" ) );
  }

  private Boolean runIsSchedulable( RepositoryFile file, ManifestFile manifestFile ) {
    return Deencapsulation.invoke( importHandler, "isSchedulable", file == null ? RepositoryFile.class : file,
        manifestFile );
  }

  @After
  public void tearDown() throws Exception {
    ImportSession.getSession().getImportedScheduleJobIds().clear();
    PentahoSystem.clearObjectFactory();
  }
}
