package org.pentaho.platform.plugin.services.exporter;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.scheduler2.versionchecker.EmbeddedVersionCheckSystemListener;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;

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
    exporter = spy( new PentahoPlatformExporter( repo ) );
    exporter.setScheduler( scheduler );
    doReturn( "session name" ).when( session ).getName();
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

    exporter.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 1, exporter.getExportManifest().getScheduleList().size() );
  }
  @Test
  public void testExportSchedules_SchedulereThrowsException() throws Exception {
    when( scheduler.getJobs( null ) ).thenThrow( new SchedulerException( "bad" ) );

    exporter.exportSchedules();

    verify( scheduler ).getJobs( null );
    assertEquals( 0, exporter.getExportManifest().getScheduleList().size() );
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