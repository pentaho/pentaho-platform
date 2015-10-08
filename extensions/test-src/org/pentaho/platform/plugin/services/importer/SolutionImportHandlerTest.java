package org.pentaho.platform.plugin.services.importer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.scheduler2.SchedulerException;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SolutionImportHandlerTest {

  SolutionImportHandler importHandler;
  IUserRoleDao userRoleDao;
  IRoleAuthorizationPolicyRoleBindingDao roleAuthorizationPolicyRoleBindingDao;

  @Before
  public void setUp() throws Exception {
    List<IMimeType> mimeTypes = new ArrayList<>();

    importHandler = new SolutionImportHandler( mimeTypes );
    userRoleDao = mock( IUserRoleDao.class );
    roleAuthorizationPolicyRoleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );

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

    assertEquals( 3, rolesToUsers.size() );
    assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );
    assertEquals( "scrum master", rolesToUsers.get( "product owner" ).get( 0 ) );
    assertEquals( "scrum master", rolesToUsers.get( "cat herder" ).get( 0 ) );

    String[] strings = {};

    verify( userRoleDao ).createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    verify( userRoleDao, never() ).setUserRoles( any( ITenant.class ), anyString(), any( strings.getClass() ) );
    verify( userRoleDao, never() ).setPassword( any( ITenant.class ), anyString(), anyString() );
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

    assertEquals( 4, rolesToUsers.size() );
    assertEquals( 2, rolesToUsers.get( "coder" ).size() );
    assertEquals( 1, rolesToUsers.get( "product owner" ).size() );
    assertEquals( "scrum master", rolesToUsers.get( "product owner" ).get( 0 ) );
    assertEquals( 1, rolesToUsers.get( "cat herder" ).size() );
    assertEquals( "scrum master", rolesToUsers.get( "cat herder" ).get( 0 ) );
    assertEquals( 1, rolesToUsers.get( "awesome" ).size() );
    assertEquals( "the dude", rolesToUsers.get( "awesome" ).get( 0 ) );

    String[] strings = {};

    verify( userRoleDao ).createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) );

    verify( userRoleDao ).createUser(
      any( ITenant.class ),
      eq( "the dude" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) );

    // should not set the password or roles explicitly if the createUser worked
    verify( userRoleDao, never() ).setUserRoles( any( ITenant.class ), anyString(), any( strings.getClass() ) );
    verify( userRoleDao, never() ).setPassword( any( ITenant.class ), anyString(), anyString() );
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

    when( userRoleDao.createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( true );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

    assertEquals( 1, rolesToUsers.size() );
    assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    verify( userRoleDao ).createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    verify( userRoleDao ).setUserRoles( any( ITenant.class ), anyString(), any( strings.getClass() ) );
    verify( userRoleDao ).setPassword( any( ITenant.class ), anyString(), anyString() );
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

    when( userRoleDao.createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) ) ).thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( false );
    Map<String, List<String>> rolesToUsers = importHandler.importUsers( users );

    assertEquals( 1, rolesToUsers.size() );
    assertEquals( "scrum master", rolesToUsers.get( "coder" ).get( 0 ) );

    verify( userRoleDao ).createUser(
      any( ITenant.class ),
      eq( "scrum master" ),
      anyString(),
      anyString(),
      any( strings.getClass() ) );

    // should set the password or roles explicitly if the createUser failed
    verify( userRoleDao, never() ).setUserRoles( any( ITenant.class ), anyString(), any( strings.getClass() ) );
    verify( userRoleDao, never() ).setPassword( any( ITenant.class ), anyString(), anyString() );
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

    verify( userRoleDao ).createRole( any( ITenant.class ), eq( roleName ), anyString(), any( userStrings.getClass() ) );
    verify( roleAuthorizationPolicyRoleBindingDao ).setRoleBindings( any( ITenant.class ), eq( roleName ),
      eq( permissions ) );
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

    when( userRoleDao.createRole( any( ITenant.class ), anyString(), anyString(), any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( true );
    importHandler.importRoles( roles, roleToUserMap );

    verify( userRoleDao ).createRole( any( ITenant.class ), anyString(), anyString(), any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it anyway... they might have changed
    verify( roleAuthorizationPolicyRoleBindingDao ).setRoleBindings( any( ITenant.class ), eq( roleName ), eq(
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

    when( userRoleDao.createRole( any( ITenant.class ), anyString(), anyString(), any( userStrings.getClass() ) ) )
      .thenThrow( new AlreadyExistsException( "already there" ) );

    importHandler.setOverwriteFile( false );
    importHandler.importRoles( roles, roleToUserMap );

    verify( userRoleDao ).createRole( any( ITenant.class ), anyString(), anyString(), any( userStrings.getClass() ) );

    // even if the roles exists, make sure we set the permissions on it anyway... they might have changed
    verify( roleAuthorizationPolicyRoleBindingDao, never() ).setRoleBindings( any( ITenant.class ), eq( roleName ), eq(
      permissions ) );

  }

  @Test
  public void testImportMetaStore() throws Exception {
    ExportManifest manifest = spy( new ExportManifest() );
    String path = "/path/to/file.zip";
    ExportManifestMetaStore manifestMetaStore = new ExportManifestMetaStore( path,
      "metastore",
      "description of the metastore" );
    importHandler.cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();

    when( manifest.getMetaStore() ).thenReturn( manifestMetaStore );

    importHandler.importMetaStore( manifest, true );
    assertEquals( 1, importHandler.cachedImports.size() );
    assertTrue( importHandler.cachedImports.get( path ) != null );
  }

  @Test
  public void testImportMetaStore_nullMetastoreManifest() throws Exception {
    ExportManifest manifest = spy( new ExportManifest() );

    importHandler.cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();
    importHandler.importMetaStore( manifest, true );
    assertEquals( 0, importHandler.cachedImports.size() );
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
  public void testImportUserSettings_NoOverwrite() throws Exception {
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
  public void testImportGlobalUserSetting() throws Exception {
    importHandler.setOverwriteFile( true );
    List<ExportManifestUserSetting> settings = new ArrayList<>();
    settings.add( new ExportManifestUserSetting( "language", "en_US" ) );
    settings.add( new ExportManifestUserSetting( "showHiddenFiles", "false" ) );
    IUserSettingService userSettingService = mock( IUserSettingService.class );
    PentahoSystem.registerObject( userSettingService );

    importHandler.importGlobalUserSettings( settings );

    verify( userSettingService ).setGlobalUserSetting( "language", "en_US" );
    verify( userSettingService ).setGlobalUserSetting( "showHiddenFiles", "false" );
    verify( userSettingService, never() ).getGlobalUserSetting( anyString(), anyString() );
  }

  @Test
  public void testImportGlobalUserSetting_noOverwrite() throws Exception {
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
    verify( userSettingService, never() ).setGlobalUserSetting( eq( "showHiddenFiles" ), anyString() );
    verify( userSettingService ).getGlobalUserSetting( "language", null );
    verify( userSettingService ).getGlobalUserSetting( "showHiddenFiles", null );

  }

  @Test
  public void testImportSchedules() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = spy( new JobScheduleRequest() );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = spy( importHandler );
    Response response = mock ( Response.class );
    when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    when( response.getEntity() ).thenReturn( "job id" );

    doReturn( response ).when( spyHandler ).createSchedulerJob( any( SchedulerResource.class ), eq( scheduleRequest ) );

    spyHandler.importSchedules( schedules );

    verify( spyHandler ).createSchedulerJob( any( SchedulerResource.class ), eq( scheduleRequest ) );
    assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
  }

  @Test
  public void testImportSchedules_FailsToCreateSchedule() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = spy( new JobScheduleRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduledTransform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduledTransform*" );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = spy( importHandler );

    doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      any( SchedulerResource.class ), eq( scheduleRequest ) );

    spyHandler.importSchedules( schedules );
    assertEquals( 0, ImportSession.getSession().getImportedScheduleJobIds().size() );
  }

  @Test
  public void testImportSchedules_FailsToCreateScheduleWithSpace() throws Exception {
    List<JobScheduleRequest> schedules = new ArrayList<>();
    JobScheduleRequest scheduleRequest = spy( new JobScheduleRequest() );
    scheduleRequest.setInputFile( "/home/admin/scheduled Transform.ktr" );
    scheduleRequest.setOutputFile( "/home/admin/scheduled Transform*" );
    schedules.add( scheduleRequest );

    SolutionImportHandler spyHandler = spy( importHandler );

    ScheduleRequestMatcher throwMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled Transform.ktr", "/home/admin/scheduled Transform*" );
    doThrow( new IOException( "error creating schedule" ) ).when( spyHandler ).createSchedulerJob(
      any( SchedulerResource.class ), argThat( throwMatcher ) );

    Response response = mock( Response.class );
    when( response.getStatus() ).thenReturn( Response.Status.OK.getStatusCode() );
    when( response.getEntity() ).thenReturn( "job id" );
    ScheduleRequestMatcher goodMatcher = new ScheduleRequestMatcher( "/home/admin/scheduled_Transform.ktr", "/home/admin/scheduled_Transform*" );
    doReturn( response ).when( spyHandler ).createSchedulerJob( any( SchedulerResource.class ), argThat(
      goodMatcher ) );

    spyHandler.importSchedules( schedules );
    verify( spyHandler, times( 2 ) ).createSchedulerJob( any( SchedulerResource.class ), any( JobScheduleRequest.class ) );
    assertEquals( 1, ImportSession.getSession().getImportedScheduleJobIds().size() );
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
      boolean matchedInput = input.equals( jsr.getInputFile() );
      boolean matchedOutput = output.equals( jsr.getOutputFile() );
      return matchedInput && matchedOutput;
    }
  }

  @After
  public void tearDown() throws Exception {
    ImportSession.getSession().getImportedScheduleJobIds().clear();
    PentahoSystem.clearObjectFactory();
  }
}
