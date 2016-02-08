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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerResource;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;

public class SolutionImportHandlerTest {

  SolutionImportHandler importHandler;
  IUserRoleDao userRoleDao;
  IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;

  @Before
  public void setUp() throws Exception {
    List<IMimeType> mimeTypes = new ArrayList<>();

    importHandler = new SolutionImportHandler( mimeTypes );
    userRoleDao = Mockito.mock( IUserRoleDao.class );
    roleAuthorizationPolicyRoleBindingDao = Mockito.mock( IRoleAuthorizationPolicyRoleBindingDao.class );

    PentahoSystem.registerObject( userRoleDao );
    PentahoSystem.registerObject( roleAuthorizationPolicyRoleBindingDao );
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
    Mockito.verify( userRoleDao, Mockito.never() ).setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() ).setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
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
    Mockito.verify( userRoleDao, Mockito.never() ).setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() ).setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
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
    Mockito.verify( userRoleDao ).setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
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
    Mockito.verify( userRoleDao, Mockito.never() ).setUserRoles( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.any( strings.getClass() ) );
    Mockito.verify( userRoleDao, Mockito.never() ).setPassword( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString() );
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

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.anyString(), Mockito.any( userStrings.getClass() ) );
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao ).setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ),
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

    Mockito.when( userRoleDao.createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(), Mockito.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( true );
    importHandler.importRoles( roles, roleToUserMap );

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(), Mockito.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao ).setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.eq(
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

    Mockito.when( userRoleDao.createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(), Mockito.any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( false );
    importHandler.importRoles( roles, roleToUserMap );

    Mockito.verify( userRoleDao ).createRole( Mockito.any( ITenant.class ), Mockito.anyString(), Mockito.anyString(), Mockito.any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it Mockito.anyway... they might have changed
    Mockito.verify( roleAuthorizationPolicyRoleBindingDao, Mockito.never() ).setRoleBindings( Mockito.any( ITenant.class ), Mockito.eq( roleName ), Mockito.eq(
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
    Mockito.verify( userSettingService, Mockito.never() ).getGlobalUserSetting( Mockito.anyString(), Mockito.anyString() );
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
    Mockito.verify( userSettingService, Mockito.never() ).setGlobalUserSetting( Mockito.eq( "showHiddenFiles" ), Mockito.anyString() );
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

    Mockito.doReturn( response ).when( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.eq( scheduleRequest ) );

    spyHandler.importSchedules( schedules );

    Mockito.verify( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.eq( scheduleRequest ) );
    Assert.assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
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

    ScheduleRequestMatcher throwMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.argThat( throwMatcher ) );

    Response response = Mockito.mock( Response.class );
    Mockito.when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    Mockito.when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    Mockito.doReturn( response ).when( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ),
      Mockito.argThat( goodMatcher ) );

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

    ScheduleRequestMatcher throwMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    Mockito.doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      Mockito.any( SchedulerResource.class ), Mockito.argThat( throwMatcher ) );

    Response response = Mockito.mock( Response.class );
    Mockito.when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    Mockito.when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    Mockito.doReturn( response ).when( spyHandler ).createSchedulerJob( Mockito.any( SchedulerResource.class ),
      Mockito.argThat( goodMatcher ) );

    spyHandler.importSchedules( schedules );
    Mockito.verify( spyHandler, Mockito.times( 2 ) ).createSchedulerJob( Mockito.any( SchedulerResource.class ), Mockito.any( JobScheduleRequest.class ) );
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

  @After
  public void tearDown() throws Exception {
    ImportSession.getSession().getImportedScheduleJobIds().clear();
    PentahoSystem.clearObjectFactory();
  }
}
