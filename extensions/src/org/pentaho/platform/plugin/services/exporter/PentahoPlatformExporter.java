package org.pentaho.platform.plugin.services.exporter;

import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.ExportException;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.web.http.api.resources.ComplexJobTriggerProxy;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PentahoPlatformExporter extends ZipExportProcessor {

  private static final Logger log = LoggerFactory.getLogger( PentahoPlatformExporter.class );

  public static final String ROOT = "/";
  private RepositoryFile rootLocation;
  private File exportFile;
  private ZipOutputStream zos;

  private IScheduler scheduler;

  public PentahoPlatformExporter( IUnifiedRepository repository ) {
    super( ROOT, repository, true );
    setUnifiedRepository( repository );
    addExportHandler( new DefaultExportHandler() );
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {

    if ( exportRepositoryFile == null ) {
      exportRepositoryFile = getUnifiedRepository().getFile( ROOT );
    }

    // create temp file
    exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    zos = new ZipOutputStream( new FileOutputStream( exportFile ) );

//    exportFileContent( exportRepositoryFile );
    exportDatasources();
    exportMondrianSchemas();
    exportMetadataModels();
    exportSchedules();
    exportUsersAndRoles();
    exportMetastore();

    if ( this.withManifest ) {
      // write manifest to zip output stream
      ZipEntry entry = new ZipEntry( EXPORT_MANIFEST_FILENAME );
      zos.putNextEntry( entry );

      // pass output stream to manifest class for writing
      try {
        exportManifest.toXml( zos );
      } catch ( Exception e ) {
        // todo: add to messages.properties
        log.error( "Error generating export XML" );
      }

      zos.closeEntry();
    }

    zos.close();

    // clean up
    exportManifest = null;
    zos = null;

    return exportFile;
  }

  private void exportDatasources() {
    log.debug( "export datasources" );
    exportManifest.addDatasource( null );
  }

  private void exportMetadataModels() {
    log.debug( "export metadata models" );
    exportManifest.addMondrian( null );
  }

  private void exportMondrianSchemas() {
    log.debug( "export mondrian schemas" );
    exportManifest.addMetadata( null );
  }

  private void exportSchedules() {
    log.debug( "export schedules" );
    try {
      List<Job> jobs = getScheduler().getJobs( null );
      for ( Job job : jobs ) {
        JobScheduleRequest scheduleRequest = createJobScheduleRequest( job );
        exportManifest.addSchedule( scheduleRequest );
      }
    } catch ( SchedulerException e ) {
      log.debug( "Error exporting schedules", e );
    }
  }

  protected JobScheduleRequest createJobScheduleRequest( Job job ) {
    ComplexJobTriggerConverter converter = new ComplexJobTriggerConverter();
    JobScheduleRequest schedule = new JobScheduleRequest();
    schedule.setJobName( job.getJobName() );
    if ( job.getJobTrigger() instanceof SimpleJobTrigger ) {
      SimpleJobTrigger jobTrigger = (SimpleJobTrigger) job.getJobTrigger();
      schedule.setSimpleJobTrigger( jobTrigger );

    } else if ( job.getJobTrigger() instanceof ComplexJobTrigger ) {
//      ComplexJobTrigger jobTrigger = (ComplexJobTrigger) job.getJobTrigger();
//      converter.setJobTrigger( jobTrigger );
//      ComplexJobTriggerProxy proxy = converter.convertToComplexJobTriggerProxy();
//      schedule.setComplexJobTrigger( proxy );
      ComplexJobTrigger jobTrigger = (ComplexJobTrigger) job.getJobTrigger();
      CronJobTrigger cron = new CronJobTrigger();
      cron.setCronString( jobTrigger.getCronString() );
      schedule.setCronJobTrigger( cron );
    } else if ( job.getJobTrigger() instanceof CronJobTrigger ) {
      CronJobTrigger jobTrigger = (CronJobTrigger) job.getJobTrigger();
      schedule.setCronJobTrigger( jobTrigger );

    } else {
      // don't know what this is, can't export it

    }
    return schedule;
  }

  private void exportUsersAndRoles() {
    log.debug( "export users & roles" );
  }

  private void exportMetastore() {
    log.debug( "export the metastore" );
  }

  protected void exportFileContent( RepositoryFile exportRepositoryFile ) throws IOException, ExportException {
    // get the file path
    String filePath = new File( this.path ).getParent();
    if ( filePath == null ) {
      filePath = "/";
    }

    // send a response right away if not found
    if ( exportRepositoryFile == null ) {
      // todo: add to messages.properties
      throw new FileNotFoundException( "JCR file not found: " + this.path );
    }

    if ( exportRepositoryFile.isFolder() ) { // Handle recursive export
      exportManifest.getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );

      // don't zip root folder without name
      if ( !ClientRepositoryPaths.getRootFolderPath().equals( exportRepositoryFile.getPath() ) ) {
        zos.putNextEntry( new ZipEntry( ExportFileNameEncoder
          .encodeZipPathName( getZipEntryName( exportRepositoryFile, filePath ) ) ) );
      }
      exportDirectory( exportRepositoryFile, zos, filePath );

    } else {
      exportManifest.getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );
      exportFile( exportRepositoryFile, zos, filePath );
    }
  }

  public IScheduler getScheduler() {
    if ( scheduler == null ) {
      scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
    }
    return scheduler;
  }

  public void setScheduler( IScheduler scheduler ) {
    this.scheduler = scheduler;
  }
}
