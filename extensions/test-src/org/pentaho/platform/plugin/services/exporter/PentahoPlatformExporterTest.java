package org.pentaho.platform.plugin.services.exporter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.mockito.ArgumentCaptor;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.scheduler2.versionchecker.EmbeddedVersionCheckSystemListener;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.security.policy.rolebased.RoleBindingStruct;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class PentahoPlatformExporterTest {

  PentahoPlatformExporter exporterSpy;
  PentahoPlatformExporter exporter;
  IUnifiedRepository repo;
  IScheduler scheduler;
  IPentahoSession session;

  @Before
  public void setUp() throws Exception {
    repo = mock( IUnifiedRepository.class );
    scheduler = mock( IScheduler.class );
    session = mock( IPentahoSession.class );
    PentahoSessionHolder.setSession( session );
    exporterSpy = spy( new PentahoPlatformExporter( repo ) );
    exporterSpy.setScheduler( scheduler );
    doReturn( "session name" ).when( session ).getName();
  exporter = new PentahoPlatformExporter( repo );
  }

  @After
  public void tearDown() {
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testExportSchedules() throws Exception {
    List<Job> jobs = new ArrayList<>();
    ComplexJobTrigger trigger = mock( ComplexJobTrigger.class );
    JobTrigger unknownTrigger = mock( JobTrigger.class );

    Job job1 = mock( Job.class );
    Job job2 = mock( Job.class );
    Job job3 = mock( Job.class );
    jobs.add( job1 );
    jobs.add( job2 );
    jobs.add( job3 );

    when( scheduler.getJobs( null ) ).thenReturn( jobs );
    when( job1.getJobName() ).thenReturn( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME );
    when( job2.getJobName() ).thenReturn( "job 2" );
    when( job2.getJobTrigger() ).thenReturn( trigger );
    when( job3.getJobName() ).thenReturn( "job 3" );
    when( job3.getJobTrigger() ).thenReturn( unknownTrigger );

    exporterSpy.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 1, exporterSpy.getExportManifest().getScheduleList().size() );
  }
  @Test
  public void testExportSchedules_SchedulereThrowsException() throws Exception {
    when( scheduler.getJobs( null ) ).thenThrow( new SchedulerException( "bad" ) );

    exporterSpy.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 0, exporterSpy.getExportManifest().getScheduleList().size() );
  }

  @Test
  public void testExportUsersAndRoles() {
    IUserRoleDao mockDao = mock( IUserRoleDao.class );
    PentahoSystem.registerObject( mockDao );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = mock( IRoleAuthorizationPolicyRoleBindingDao.class );
    PentahoSystem.registerObject( roleBindingDao );

    String tenantPath = "path";
    when( session.getAttribute( IPentahoSession.TENANT_ID_KEY ) ).thenReturn( tenantPath );

    List<IPentahoUser> userList = new ArrayList<IPentahoUser>();
    IPentahoUser user = new PentahoUser( "testUser" );
    IPentahoRole role = new PentahoRole( "testRole" );
    userList.add( user );
    when( mockDao.getUsers( any( ITenant.class ) ) ).thenReturn( userList );

    List<IPentahoRole> roleList = new ArrayList<IPentahoRole>();
    roleList.add( role );
    when( mockDao.getRoles() ).thenReturn( roleList );

    Map<String, List<String>> map = new HashMap<String, List<String>>();
    List<String> permissions = new ArrayList<String>();
    permissions.add( "read" );
    map.put( "testRole", permissions );
    RoleBindingStruct struct = mock( RoleBindingStruct.class );
    struct.bindingMap = map;
    when( roleBindingDao.getRoleBindingStruct( anyString() ) ).thenReturn( struct );

    ArgumentCaptor<UserExport> userCaptor = ArgumentCaptor.forClass( UserExport.class );
    ArgumentCaptor<RoleExport> roleCaptor = ArgumentCaptor.forClass( RoleExport.class );
    ExportManifest manifest = mock( ExportManifest.class );
    exporter.setExportManifest( manifest );

    exporter.exportUsersAndRoles();

    verify( manifest ).addUserExport( userCaptor.capture() );
    verify( manifest ).addRoleExport( roleCaptor.capture() );

    UserExport userExport = userCaptor.getValue();
    assertEquals( "testUser", userExport.getUsername() );
    RoleExport roleExport = roleCaptor.getValue();
    assertEquals( "testRole", roleExport.getRolename() );
  }

  @Test
  public void testExportMetadata_noModels() throws Exception {
    IMetadataDomainRepository mdr = mock( IMetadataDomainRepository.class );
    exporter.setMetadataDomainRepository( mdr );

    exporter.exportMetadataModels();
    assertEquals( 0, exporter.getExportManifest().getMetadataList().size() );
  }

  @Test
  public void testExportMetadata() throws Exception {
    IMetadataDomainRepository mdr = mock( IMetadataDomainRepository.class );

    Set<String> domainIds = new HashSet<>();
    domainIds.add( "test1" );

    when( mdr.getDomainIds() ).thenReturn( domainIds );
    exporter.setMetadataDomainRepository( mdr );
    exporter.zos = mock( ZipOutputStream.class );

    Map<String, InputStream> inputMap = new HashMap<>();
    InputStream is = mock( InputStream.class );
    when( is.read( any( (new byte[]{}).getClass() ) ) ).thenReturn( -1 );
    inputMap.put( "test1", is );

    doReturn( inputMap ).when( exporter ).getDomainFilesData( "test1" );

    exporter.exportMetadataModels();
    assertEquals( 1, exporter.getExportManifest().getMetadataList().size() );

    assertEquals( "test1", exporter.getExportManifest().getMetadataList().get( 0 ).getDomainId() );
    assertEquals( PentahoPlatformExporter.METADATA_PATH_IN_ZIP + "test1.xmi",
      exporter.getExportManifest().getMetadataList().get( 0 ).getFile() );
  }

  @Test
  public void testExportDatasources() throws Exception {

    IDatasourceMgmtService svc = mock( IDatasourceMgmtService.class );
    exporter.setDatasourceMgmtService( svc );

    List<IDatabaseConnection> datasources = new ArrayList<>();
    IDatabaseConnection conn = mock( DatabaseConnection.class );
    IDatabaseConnection icon = mock( IDatabaseConnection.class );
    datasources.add( conn );
    datasources.add( icon );

    when( svc.getDatasources() ).thenReturn( datasources );

    exporter.exportDatasources();

    assertEquals( 1, exporter.getExportManifest().getDatasourceList().size() );
    assertEquals( conn, exporter.getExportManifest().getDatasourceList().get( 0 ) );
  }
}