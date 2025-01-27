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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IJobRequest;
import org.pentaho.platform.api.scheduler2.IJobScheduleRequest;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISchedulerResource;
import org.pentaho.platform.api.scheduler2.JobState;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.importexport.DatabaseConnectionConverter;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSession.ManifestFile;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SolutionImportHandler implements IPlatformImportHandler {

  private static final String XMI_EXTENSION = ".xmi";

  private static final String EXPORT_MANIFEST_XML_FILE = "exportManifest.xml";
  private static final String DOMAIN_ID = "domain-id";
  private static final String UTF_8 = StandardCharsets.UTF_8.name();

  private IUnifiedRepository repository; // TODO inject via Spring
  protected Map<String, RepositoryFileImportBundle.Builder> cachedImports;
  private SolutionFileImportHelper solutionHelper;
  private List<IMimeType> mimeTypes;
  public boolean overwriteFile;
  private List<IRepositoryFileBundle> files;
  private boolean isPerformingRestore = false;

  private List<IImportHelper> importHelpers = new ArrayList<>();

  public SolutionImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
    repository = PentahoSystem.get( IUnifiedRepository.class );
  }

  public void addImportHelper( IImportHelper helper ) {
    importHelpers.add( helper );
  }

  public void runImportHelpers() {
    for ( IImportHelper helper : importHelpers ) {
      try {
        helper.doImport( this );
      } catch ( ImportException exportException ) {
        // Todo fix this.
        // getRepositoryExportLogger().error( "Error performing backup of component [ " + helper.getName() + " ] Cause [ " + exportException.getLocalizedMessage() + " ]" );
      }
    }
  }
  public ImportSession getImportSession() {
    return ImportSession.getSession();
  }

  public Log getLogger() {
    return getImportSession().getLogger();
  }

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    IPlatformImporter platformImporter = PentahoSystem.get( IPlatformImporter.class );
    isPerformingRestore = platformImporter.getRepositoryImportLogger().isPerformingRestore();
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_PROCESS" ) );
    }

    // Processing file
    if ( isPerformingRestore ) {
      getLogger().debug( " Start:  pre processing files and folder from the bundle" );
    }
    if ( !processZip( bundle.getInputStream() ) ) {
      // Something went wrong, do not proceed!
      return;
    }
    if ( isPerformingRestore ) {
      getLogger().debug( " End:  pre processing files and folder from the bundle" );
    }
    setOverwriteFile( bundle.overwriteInRepository() );
    cachedImports = new HashMap<>();

    //Process Manifest Settings
    ExportManifest manifest = getImportSession().getManifest();
    // Process Metadata
    if ( manifest != null ) {
      // import the users
      Map<String, List<String>> roleToUserMap = importUsers( manifest.getUserExports() );

      // import the roles
      importRoles( manifest.getRoleExports(), roleToUserMap );

      // import the metadata
      importMetadata( manifest.getMetadataList(), bundle.isPreserveDsw() );

      // Process Mondrian
      importMondrian( manifest.getMondrianList() );

      // import the metastore
      importMetaStore( manifest.getMetaStore(), bundle.overwriteInRepository() );

      // import jdbc datasource
      importJDBCDataSource( manifest );
    }
    // import files and folders
    importRepositoryFilesAndFolders( manifest, bundle );

    // import schedules and any other imports defined by ImportHelper
    if ( manifest != null ) {
      runImportHelpers();
//      importSchedules( manifest.getScheduleList() );
    }
  }

  protected void importRepositoryFilesAndFolders( ExportManifest manifest, IPlatformImportBundle bundle ) throws IOException {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_FILEFOLDER" ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_FILEFOLDER", files.size() ) );
    }
    int successfulFilesImportCount = 0;
    String manifestVersion = null;
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();
    }
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;

    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    for ( IRepositoryFileBundle fileBundle : files ) {
      String fileName = fileBundle.getFile().getName();
      String actualFilePath = fileBundle.getPath();
      if ( manifestVersion != null ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }
      String repositoryFilePath =
          RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      if ( cachedImports.containsKey( repositoryFilePath ) ) {
        getLogger().debug( "Repository object with path [ " + repositoryFilePath + " ] found in the cache" );
        byte[] bytes = IOUtils.toByteArray( fileBundle.getInputStream() );
        RepositoryFileImportBundle.Builder builder = cachedImports.get( repositoryFilePath );
        builder.input( new ByteArrayInputStream( bytes ) );

        try {
          importer.importFile( build( builder ) );
          if ( isPerformingRestore ) {
            getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ] from the cache" );
          }
          successfulFilesImportCount++;
          continue;
        } catch ( PlatformImportException e ) {
          if ( isPerformingRestore ) {
            getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", repositoryFilePath, e.getLocalizedMessage() ) );
          }
        }
      }

      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
      InputStream bundleInputStream = null;

      String decodedFilePath = fileBundle.getPath();
      RepositoryFile decodedFile = fileBundle.getFile();
      if ( manifestVersion != null ) {
        decodedFile = new RepositoryFile.Builder( decodedFile ).path( decodedFilePath ).name( fileName ).title( fileName ).build();
        decodedFilePath = ExportFileNameEncoder.decodeZipFileName( fileBundle.getPath() );
      }

      if ( fileBundle.getFile().isFolder() ) {
        bundleBuilder.mime( "text/directory" );
        bundleBuilder.file( decodedFile );
        fileName = repositoryFilePath;
        repositoryFilePath = importBundle.getPath();
      } else {
        byte[] bytes = IOUtils.toByteArray( fileBundle.getInputStream() );
        bundleInputStream = new ByteArrayInputStream( bytes );
        // If is locale file store it for later processing.
        if ( localeFilesProcessor.isLocaleFile( fileBundle, importBundle.getPath(), bytes ) ) {
          getLogger().trace( Messages.getInstance()
              .getString( "SolutionImportHandler.SkipLocaleFile", repositoryFilePath ) );
          continue;
        }
        bundleBuilder.input( bundleInputStream );
        bundleBuilder.mime( solutionHelper.getMime( fileName ) );

        String filePath =
            ( decodedFilePath.equals( "/" ) || decodedFilePath.equals( "\\" ) ) ? "" : decodedFilePath;
        repositoryFilePath = RepositoryFilenameUtils.concat( importBundle.getPath(), filePath );
      }

      bundleBuilder.name( fileName );
      bundleBuilder.path( repositoryFilePath );

      String sourcePath;
      if ( fileBundle.getFile().isFolder() ) {
        sourcePath = fileName;
      } else {
        sourcePath =
            RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );
      }

      //This clause was added for processing ivb files so that it would not try process acls on folders that the user
      //may not have rights to such as /home or /public
      if ( manifest != null && manifest.getExportManifestEntity( sourcePath ) == null && fileBundle.getFile()
          .isFolder() ) {
        continue;
      }

      getImportSession().setCurrentManifestKey( sourcePath );

      bundleBuilder.charSet( bundle.getCharSet() );
      bundleBuilder.overwriteFile( bundle.overwriteInRepository() );
      bundleBuilder.applyAclSettings( bundle.isApplyAclSettings() );
      bundleBuilder.retainOwnership( bundle.isRetainOwnership() );
      bundleBuilder.overwriteAclSettings( bundle.isOverwriteAclSettings() );
      bundleBuilder.acl( getImportSession().processAclForFile( sourcePath ) );
      bundleBuilder.extraMetaData( getImportSession().processExtraMetaDataForFile( sourcePath ) );

      RepositoryFile file = getFile( importBundle, fileBundle );
      ManifestFile manifestFile = getImportSession().getManifestFile( sourcePath, file != null );

      bundleBuilder.hidden( isFileHidden( file, manifestFile, sourcePath ) );
      boolean isSchedulable = isSchedulable( file, manifestFile );

      if ( isSchedulable ) {
        bundleBuilder.schedulable( isSchedulable );
      } else {
        bundleBuilder.schedulable( fileIsScheduleInputSource( manifest, sourcePath ) );
      }

      IPlatformImportBundle platformImportBundle = build( bundleBuilder );
      try {
        importer.importFile( platformImportBundle );
        successfulFilesImportCount++;
        if ( isPerformingRestore ) {
          getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ]" );
        }
      } catch ( PlatformImportException e ) {
        if ( isPerformingRestore ) {
          getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", repositoryFilePath, e.getLocalizedMessage() ) );
        }
      }

      if ( bundleInputStream != null ) {
        bundleInputStream.close();
      }
    }

    // Process locale files.
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_LOCALEFILE" ) );
    }
    int successfulLocaleFilesProcessed = 0;
    try {
      successfulLocaleFilesProcessed = localeFilesProcessor.processLocaleFiles( importer );
    } catch ( PlatformImportException e ) {
      if ( isPerformingRestore ) {
        getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_LOCALE_FILE", e.getLocalizedMessage() ) );
      }
    } finally {
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_LOCALEFILE" ) );
      }
    }

    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_REPOSITORY_IMPORT_COUNT", successfulFilesImportCount + successfulLocaleFilesProcessed, files.size() ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_FILEFOLDER" ) );
    }
  }

  protected void importJDBCDataSource( ExportManifest manifest ) {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_DATASOURCE" ) );
    }
    // Add DB Connections
    List<org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection> datasourceList = manifest.getDatasourceList();
    if ( datasourceList != null ) {
      int successfulDatasourceImportCount = 0;
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_DATASOURCE", datasourceList.size() ) );
      }
      IDatasourceMgmtService datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class );
      for ( org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection databaseConnection : datasourceList ) {
        if ( databaseConnection.getDatabaseType() == null ) {
          // don't try to import the connection if there is no type it will cause an error
          // However, if this is the DI Server, and the connection is defined in a ktr, it will import automatically
          getLogger().warn( Messages.getInstance()
              .getString( "SolutionImportHandler.ConnectionWithoutDatabaseType", databaseConnection.getName() ) );
          continue;
        }
        try {
          IDatabaseConnection existingDBConnection =
              datasourceMgmtSvc.getDatasourceByName( databaseConnection.getName() );
          if ( existingDBConnection != null && existingDBConnection.getName() != null ) {
            if ( isOverwriteFile() ) {
              databaseConnection.setId( existingDBConnection.getId() );
              datasourceMgmtSvc.updateDatasourceByName( databaseConnection.getName(),
                  DatabaseConnectionConverter.export2model( databaseConnection ) );
            }
          } else {
            datasourceMgmtSvc.createDatasource( DatabaseConnectionConverter.export2model( databaseConnection ) );
          }
          successfulDatasourceImportCount++;
        } catch ( Exception e ) {
          if ( isPerformingRestore ) {
            getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_JDBC_DATASOURCE", databaseConnection.getName(), e.getLocalizedMessage() ) );
          }
        }
      }
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_DATASOURCE_IMPORT_COUNT", successfulDatasourceImportCount, datasourceList.size() ) );
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_DATASOURCE" ) );
    }
  }

  List<IJob> getAllJobs( ISchedulerResource schedulerResource ) {
    return schedulerResource.getJobsList();
  }

  private RepositoryFile getFile( IPlatformImportBundle importBundle, IRepositoryFileBundle fileBundle ) {
    String repositoryFilePath =
        repositoryPathConcat( importBundle.getPath(), fileBundle.getPath(), fileBundle.getFile().getName() );
    return repository.getFile( repositoryFilePath );
  }

//  protected void importSchedules( List<IJobScheduleRequest> scheduleList ) throws PlatformImportException {
//    if ( isPerformingRestore ) {
//      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_SCHEDULE" ) );
//    }
//    if ( CollectionUtils.isNotEmpty( scheduleList ) ) {
//      if ( isPerformingRestore ) {
//        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_SCHEDULUE", scheduleList.size() ) );
//      }
//      int successfulScheduleImportCount = 0;
//      IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
//      ISchedulerResource schedulerResource = scheduler.createSchedulerResource();
//      if ( isPerformingRestore ) {
//        getLogger().debug( "Pausing the scheduler before the start of the restore process" );
//      }
//      schedulerResource.pause();
//      if ( isPerformingRestore ) {
//        getLogger().debug( "Successfully paused the scheduler" );
//      }
//      for ( IJobScheduleRequest jobScheduleRequest : scheduleList ) {
//        if ( isPerformingRestore ) {
//          getLogger().debug( "Restoring schedule name [ " + jobScheduleRequest.getJobName() + "] inputFile [ " + jobScheduleRequest.getInputFile() + " ] outputFile [ " + jobScheduleRequest.getOutputFile() + "]" );
//        }
//        boolean jobExists = false;
//
//        List<IJob> jobs = getAllJobs( schedulerResource );
//        if ( jobs != null ) {
//
//          //paramRequest to map<String, Serializable>
//          Map<String, Serializable> mapParamsRequest = new HashMap<>();
//          for ( IJobScheduleParam paramRequest : jobScheduleRequest.getJobParameters() ) {
//            mapParamsRequest.put( paramRequest.getName(), paramRequest.getValue() );
//          }
//
//          // We will check the existing job in the repository. If the job being imported exists, we will remove it from the repository
//          for ( IJob job : jobs ) {
//
//            if ( ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID ) != null )
//                && ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID )
//                .equals( job.getJobParams().get( RESERVEDMAPKEY_LINEAGE_ID ) ) ) ) {
//              jobExists = true;
//            }
//
//            if ( overwriteFile && jobExists ) {
//              if ( isPerformingRestore ) {
//                getLogger().debug( "Schedule  [ " + jobScheduleRequest.getJobName() + "] already exists and overwrite flag is set to true. Removing the job so we can add it again" );
//              }
//              IJobRequest jobRequest = scheduler.createJobRequest();
//              jobRequest.setJobId( job.getJobId() );
//              schedulerResource.removeJob( jobRequest );
//              jobExists = false;
//              break;
//            }
//          }
//        }
//
//        if ( !jobExists ) {
//          try {
//            Response response = createSchedulerJob( schedulerResource, jobScheduleRequest );
//            if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
//              if ( response.getEntity() != null ) {
//                // get the schedule job id from the response and add it to the import session
//                ImportSession.getSession().addImportedScheduleJobId( response.getEntity().toString() );
//                if ( isPerformingRestore ) {
//                  getLogger().debug( "Successfully restored schedule [ " + jobScheduleRequest.getJobName() + " ] " );
//                }
//                successfulScheduleImportCount++;
//              }
//            } else {
//              getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_SCHEDULE", jobScheduleRequest.getJobName(), response.getEntity() != null
//                  ? response.getEntity().toString() : "" ) );
//            }
//          } catch ( Exception e ) {
//            // there is a scenario where if the file scheduled has a space in the file name, that it won't work. the
//            // di server
//
//            // replaces spaces with underscores and the export mechanism can't determine if it needs this to happen
//            // or not
//            // so, if we failed to import and there is a space in the path, try again but this time with replacing
//            // the space(s)
//            if ( jobScheduleRequest.getInputFile().contains( " " ) || jobScheduleRequest.getOutputFile()
//                .contains( " " ) ) {
//              getLogger().debug( Messages.getInstance()
//                  .getString( "SolutionImportHandler.SchedulesWithSpaces", jobScheduleRequest.getInputFile() ) );
//              File inFile = new File( jobScheduleRequest.getInputFile() );
//              File outFile = new File( jobScheduleRequest.getOutputFile() );
//              String inputFileName = inFile.getParent() + RepositoryFile.SEPARATOR
//                  + inFile.getName().replace( " ", "_" );
//              String outputFileName = outFile.getParent() + RepositoryFile.SEPARATOR
//                  + outFile.getName().replace( " ", "_" );
//              jobScheduleRequest.setInputFile( inputFileName );
//              jobScheduleRequest.setOutputFile( outputFileName );
//              try {
//                if ( !File.separator.equals( RepositoryFile.SEPARATOR ) ) {
//                  // on windows systems, the backslashes will result in the file not being found in the repository
//                  jobScheduleRequest.setInputFile( inputFileName.replace( File.separator, RepositoryFile.SEPARATOR ) );
//                  jobScheduleRequest
//                      .setOutputFile( outputFileName.replace( File.separator, RepositoryFile.SEPARATOR ) );
//                }
//                Response response = createSchedulerJob( schedulerResource, jobScheduleRequest );
//                if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
//                  if ( response.getEntity() != null ) {
//                    // get the schedule job id from the response and add it to the import session
//                    ImportSession.getSession().addImportedScheduleJobId( response.getEntity().toString() );
//                    successfulScheduleImportCount++;
//                  }
//                }
//              } catch ( Exception ex ) {
//                // log it and keep going. we shouldn't stop processing all schedules just because one fails.
//                getLogger().error( Messages.getInstance()
//                    .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", "[ " + jobScheduleRequest.getJobName() + " ] cause [ " + ex.getMessage() + " ]" ), ex );
//              }
//            } else {
//              // log it and keep going. we shouldn't stop processing all schedules just because one fails.
//              getLogger().error( Messages.getInstance()
//                  .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", "[ " + jobScheduleRequest.getJobName() + " ]" ) );
//            }
//          }
//        } else {
//          getLogger().info( Messages.getInstance()
//              .getString( "DefaultImportHandler.ERROR_0009_OVERWRITE_CONTENT", jobScheduleRequest.toString() ) );
//        }
//      }
//      if ( isPerformingRestore ) {
//        getLogger().info( Messages.getInstance()
//            .getString( "SolutionImportHandler.INFO_SUCCESSFUL_SCHEDULE_IMPORT_COUNT", successfulScheduleImportCount, scheduleList.size() ) );
//      }
//      schedulerResource.start();
//      if ( isPerformingRestore ) {
//        getLogger().debug( "Successfully started the scheduler" );
//      }
//    }
//    if ( isPerformingRestore ) {
//      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_SCHEDULE" ) );
//    }
//  }

  protected void importMetaStore( ExportManifestMetaStore manifestMetaStore, boolean overwrite ) {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METASTORE" ) );
    }
    if ( manifestMetaStore != null ) {
      // get the zipped metastore from the export bundle
      RepositoryFileImportBundle.Builder bundleBuilder =
          new RepositoryFileImportBundle.Builder()
              .path( manifestMetaStore.getFile() )
              .name( manifestMetaStore.getName() )
              .withParam( "description", manifestMetaStore.getDescription() )
              .charSet( UTF_8 )
              .overwriteFile( overwrite )
              .mime( "application/vnd.pentaho.metastore" );

      cachedImports.put( manifestMetaStore.getFile(), bundleBuilder );
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_IMPORT_METASTORE" ) );
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METASTORE" ) );
    }
  }

  /**
   * Imports UserExport objects into the platform as users.
   *
   * @param users
   * @return A map of role names to list of users in that role
   */
  protected Map<String, List<String>> importUsers( List<UserExport> users ) {
    Map<String, List<String>> roleToUserMap = new HashMap<>();
    IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
    ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
    int successFullUserImportCount = 0;
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER" ) );
    }
    if ( users != null && roleDao != null ) {
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_USER", users.size() ) );
      }
      for ( UserExport user : users ) {
        String password = user.getPassword();
        getLogger().debug( Messages.getInstance().getString( "USER.importing", user.getUsername() ) );

        // map the user to the roles he/she is in
        for ( String role : user.getRoles() ) {
          List<String> userList;
          if ( !roleToUserMap.containsKey( role ) ) {
            userList = new ArrayList<>();
            roleToUserMap.put( role, userList );
          } else {
            userList = roleToUserMap.get( role );
          }
          userList.add( user.getUsername() );
        }

        String[] userRoles = user.getRoles().toArray( new String[] {} );
        try {
          if ( isPerformingRestore ) {
            getLogger().debug( "Restoring user [ " + user.getUsername() + " ] " );
          }
          roleDao.createUser( tenant, user.getUsername(), password, null, userRoles );
          if ( isPerformingRestore ) {
            getLogger().debug( "Successfully restored user [ " + user.getUsername() + " ]" );
          }
          successFullUserImportCount++;
        } catch ( AlreadyExistsException e ) {
          // it's ok if the user already exists, it is probably a default user
          getLogger().debug( Messages.getInstance().getString( "USER.Already.Exists", user.getUsername() ) );

          try {
            if ( isOverwriteFile() ) {
              if ( isPerformingRestore ) {
                getLogger().debug( "Overwrite is set to true. So restoring user [ " + user.getUsername() + " ]" );
              }
              // set the roles, maybe they changed
              roleDao.setUserRoles( tenant, user.getUsername(), userRoles );

              // set the password just in case it changed
              roleDao.setPassword( tenant, user.getUsername(), password );
              successFullUserImportCount++;
            }
          } catch ( Exception ex ) {
            // couldn't set the roles or password either
            getLogger().warn( Messages.getInstance()
                .getString( "ERROR.OverridingExistingUser", user.getUsername() ) );
            getLogger().debug( Messages.getInstance()
                .getString( "ERROR.OverridingExistingUser", user.getUsername() ), ex );
          }
        } catch ( Exception e ) {
          getLogger().debug( Messages.getInstance()
              .getString( "ERROR.OverridingExistingUser", user.getUsername() ), e );
          getLogger().error( Messages.getInstance()
              .getString( "ERROR.OverridingExistingUser", user.getUsername() ) );
        }
        if ( isPerformingRestore ) {
          getLogger().debug( "Restoring user [ " + user.getUsername() + " ] specific settings" );
        }
        importUserSettings( user );
        if ( isPerformingRestore ) {
          getLogger().debug( "Successfully restored user [ " + user.getUsername() + " ] specific settings" );
        }
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_COUNT", successFullUserImportCount, users.size() ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_USER" ) );
    }
    return roleToUserMap;
  }

  protected void importGlobalUserSettings( List<ExportManifestUserSetting> globalSettings ) {
    if ( isPerformingRestore ) {
      getLogger().debug( "************************[ Start: Restore global user  settings] *************************" );
    }
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    if ( settingService != null ) {
      for ( ExportManifestUserSetting globalSetting : globalSettings ) {
        if ( isOverwriteFile() ) {
          if ( isPerformingRestore ) {
            getLogger().trace( "Overwrite flag is set to true." );
          }
          settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
          if ( isPerformingRestore ) {
            getLogger().debug( "Finished restore of global user setting with name [ " + globalSetting.getName() + " ]" );
          }
        } else {
          if ( isPerformingRestore ) {
            getLogger().trace( "Overwrite flag is set to false." );
          }
          IUserSetting userSetting = settingService.getGlobalUserSetting( globalSetting.getName(), null );
          if ( userSetting == null ) {
            settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
            if ( isPerformingRestore ) {
              getLogger().debug( "Finished restore of global user setting with name [ " + globalSetting.getName() + " ]" );
            }
          }
        }
      }
    }
    if ( isPerformingRestore ) {
      getLogger().debug( "************************[ End: Restore global user settings] *************************" );
    }
  }

  protected void importUserSettings( UserExport user ) {
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    IAnyUserSettingService userSettingService = null;
    int userSettingsListSize = 0;
    int successfulUserSettingsImportCount = 0;
    if ( settingService != null && settingService instanceof IAnyUserSettingService ) {
      userSettingService = (IAnyUserSettingService) settingService;
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER_SETTING" ) );
    }
    if ( userSettingService != null ) {
      List<ExportManifestUserSetting> exportedSettings = user.getUserSettings();
      userSettingsListSize = user.getUserSettings().size();
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_USER_SETTING", userSettingsListSize, user.getUsername() ) );
      }
      try {
        for ( ExportManifestUserSetting exportedSetting : exportedSettings ) {
          if ( isPerformingRestore ) {
            getLogger().debug( "Restore user specific setting  [ " + exportedSetting.getName() + " ]" );
          }
          if ( isOverwriteFile() ) {
            if ( isPerformingRestore ) {
              getLogger().debug( "Overwrite is set to true. So restoring setting  [ " + exportedSetting.getName() + " ]" );
            }
            userSettingService.setUserSetting( user.getUsername(),
                exportedSetting.getName(), exportedSetting.getValue() );
            if ( isPerformingRestore ) {
              getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
            }
          } else {
            // see if it's there first before we set this setting
            if ( isPerformingRestore ) {
              getLogger().debug( "Overwrite is set to false. Only restore setting  [ " + exportedSetting.getName() + " ] if is does not exist" );
            }
            IUserSetting userSetting =
                userSettingService.getUserSetting( user.getUsername(), exportedSetting.getName(), null );
            if ( userSetting == null ) {
              // only set it if we didn't find that it exists already
              userSettingService.setUserSetting( user.getUsername(), exportedSetting.getName(), exportedSetting.getValue() );
              if ( isPerformingRestore ) {
                getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
              }
            }
          }
          successfulUserSettingsImportCount++;
          if ( isPerformingRestore ) {
            getLogger().debug( "Successfully restored setting  [ " + exportedSetting.getName() + " ]" );
          }
        }
      } catch ( SecurityException e ) {
        String errorMsg = Messages.getInstance().getString( "ERROR.ImportingUserSetting", user.getUsername() );
        getLogger().error( errorMsg );
        getLogger().debug( errorMsg, e );
      } finally {
        if ( isPerformingRestore ) {
          getLogger().info( Messages.getInstance()
              .getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_SETTING_IMPORT_COUNT", successfulUserSettingsImportCount, userSettingsListSize ) );
          getLogger().info( Messages.getInstance()
              .getString( "SolutionImportHandler.INFO_END_IMPORT_USER_SETTING" ) );
        }
      }
    }
  }

  protected void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap ) {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_ROLE" ) );
    }
    if ( roles != null ) {
      IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
      ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
          IRoleAuthorizationPolicyRoleBindingDao.class );

      Set<String> existingRoles = new HashSet<>();
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_ROLE", roles.size() ) );
      }
      int successFullRoleImportCount = 0;
      for ( RoleExport role : roles ) {
        getLogger().debug( Messages.getInstance().getString( "ROLE.importing", role.getRolename() ) );
        try {
          List<String> users = roleToUserMap.get( role.getRolename() );
          String[] userarray = users == null ? new String[] {} : users.toArray( new String[] {} );
          IPentahoRole role1 = roleDao.createRole( tenant, role.getRolename(), null, userarray );
          successFullRoleImportCount++;
        } catch ( AlreadyExistsException e ) {
          existingRoles.add( role.getRolename() );
          // it's ok if the role already exists, it is probably a default role
          getLogger().debug( Messages.getInstance().getString( "ROLE.Already.Exists", role.getRolename() ) );
        }
        try {
          if ( existingRoles.contains( role.getRolename() ) ) {
            //Only update an existing role if the overwrite flag is set
            if ( isOverwriteFile() ) {
              if ( isPerformingRestore ) {
                getLogger().debug( "Overwrite is set to true so restoring role [ " + role.getRolename() + "]" );
              }
              roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
            }
          } else {
            if ( isPerformingRestore ) {
              getLogger().debug( "Updating the role mapping from runtime roles to logical roles for  [ " + role.getRolename() + "]" );
            }
            //Always write a roles permissions that were not previously existing
            roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
          }
          successFullRoleImportCount++;
        } catch ( Exception e ) {
          getLogger().error( Messages.getInstance()
              .getString( "ERROR.SettingRolePermissions", role.getRolename() ), e );
        }
      }
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_ROLE_COUNT", successFullRoleImportCount, roles.size() ) );
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_ROLE" ) );
    }
  }

  /**
   * <p>Import the Metadata</p>
   *
   * @param metadataList metadata to be imported
   * @param preserveDsw  whether or not to preserve DSW settings
   */
  protected void importMetadata( List<ExportManifestMetadata> metadataList, boolean preserveDsw ) {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METADATA_DATASOURCE" ) );
    }
    if ( null != metadataList ) {
      int successfulMetadataModelImport = 0;
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_METADATA_DATASOURCE", metadataList.size() ) );
      }
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {
        if ( isPerformingRestore ) {
          getLogger().debug( "Restoring  [ " + exportManifestMetadata.getDomainId() + " ] datasource" );
        }
        String domainId = exportManifestMetadata.getDomainId();
        if ( domainId != null && !domainId.endsWith( XMI_EXTENSION ) ) {
          domainId = domainId + XMI_EXTENSION;
        }
        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( UTF_8 )
                .hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT )
                // let the parent bundle control whether or not to preserve DSW settings
                .preserveDsw( preserveDsw )
                .overwriteFile( isOverwriteFile() )
                .mime( "text/xmi+xml" )
                .withParam( DOMAIN_ID, domainId );

        cachedImports.put( exportManifestMetadata.getFile(), bundleBuilder );
        if ( isPerformingRestore ) {
          getLogger().debug( " Successfully restored  [ " + exportManifestMetadata.getDomainId() + " ] datasource" );
        }
        successfulMetadataModelImport++;
      }
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_METDATA_DATASOURCE_COUNT", successfulMetadataModelImport, metadataList.size() ) );
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METADATA_DATASOURCE" ) );
    }
  }

  protected void importMondrian( List<ExportManifestMondrian> mondrianList ) {
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_MONDRIAN_DATASOURCE" ) );
    }
    if ( null != mondrianList ) {
      int successfulMondrianSchemaImport = 0;
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_MONDRIAN_DATASOURCE", mondrianList.size() ) );
      }
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {
        if ( isPerformingRestore ) {
          getLogger().debug( "Restoring  [ " + exportManifestMondrian.getCatalogName() + " ] mondrian datasource" );
        }
        String catName = exportManifestMondrian.getCatalogName();
        Parameters parametersMap = exportManifestMondrian.getParameters();
        StringBuilder parametersStr = new StringBuilder();
        for ( Map.Entry<String, String> e : parametersMap.entrySet() ) {
          parametersStr.append( e.getKey() ).append( '=' ).append( e.getValue() ).append( ';' );
        }

        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( UTF_8 ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT )
                .schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT ).name( catName ).overwriteFile(
                isOverwriteFile() ).mime( "application/vnd.pentaho.mondrian+xml" )
                .withParam( "parameters", parametersStr.toString() )
                .withParam( DOMAIN_ID, catName ); // TODO: this is definitely named wrong at the very least.
        // pass as param if not in parameters string
        String xmlaEnabled = "" + exportManifestMondrian.isXmlaEnabled();
        bundleBuilder.withParam( "EnableXmla", xmlaEnabled );

        cachedImports.put( exportManifestMondrian.getFile(), bundleBuilder );

        String annotationsFile = exportManifestMondrian.getAnnotationsFile();
        if ( annotationsFile != null ) {
          RepositoryFileImportBundle.Builder annotationsBundle =
              new RepositoryFileImportBundle.Builder().path( MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
                  + RepositoryFile.SEPARATOR + catName ).name( "annotations.xml" ).charSet( UTF_8 ).overwriteFile(
                  isOverwriteFile() ).mime( "text/xml" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable(
                  RepositoryFile.SCHEDULABLE_BY_DEFAULT ).withParam( DOMAIN_ID, catName );
          cachedImports.put( annotationsFile, annotationsBundle );
        }
        successfulMondrianSchemaImport++;
        if ( isPerformingRestore ) {
          getLogger().debug( " Successfully restored  [ " + exportManifestMondrian.getCatalogName() + " ] mondrian datasource" );
        }
      }
      if ( isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_MONDRIAN_DATASOURCE_IMPORT_COUNT", successfulMondrianSchemaImport, mondrianList.size() ) );
      }
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_MONDRIAN_DATASOURCE" ) );
    }
  }

  /**
   * See BISERVER-13481 . For backward compatibility we must check if there are any schedules
   * which refers to this file. If yes make this file schedulable
   */
  @VisibleForTesting
  boolean fileIsScheduleInputSource( ExportManifest manifest, String sourcePath ) {
    boolean isSchedulable = false;
    if ( sourcePath != null && manifest != null
        && manifest.getScheduleList() != null ) {
      String path = sourcePath.startsWith( "/" ) ? sourcePath : "/" + sourcePath;
      isSchedulable = manifest.getScheduleList().stream()
          .anyMatch( schedule -> path.equals( schedule.getInputFile() ) );
    }

    if ( isSchedulable ) {
      getLogger().warn( Messages.getInstance()
          .getString( "ERROR.ScheduledWithoutPermission", sourcePath ) );
      getLogger().warn( Messages.getInstance().getString( "SCHEDULE.AssigningPermission", sourcePath ) );
    }

    return isSchedulable;
  }

  @VisibleForTesting
  protected boolean isFileHidden( RepositoryFile file, ManifestFile manifestFile, String sourcePath ) {
    Boolean result = manifestFile.isFileHidden();
    if ( result != null ) {
      return result; // file absent or must receive a new setting and the setting is exist
    }
    if ( file != null ) {
      return file.isHidden(); // old setting
    }
    if ( solutionHelper.isInHiddenList( sourcePath ) ) {
      return true;
    }
    return RepositoryFile.HIDDEN_BY_DEFAULT; // default setting of type
  }

  @VisibleForTesting
  protected boolean isSchedulable( RepositoryFile file, ManifestFile manifestFile ) {
    Boolean result = manifestFile.isFileSchedulable();
    if ( result != null ) {
      return result; // file absent or must receive a new setting and the setting is exist
    }
    if ( file != null ) {
      return file.isSchedulable(); // old setting
    }
    return RepositoryFile.SCHEDULABLE_BY_DEFAULT; // default setting of type
  }

  private String repositoryPathConcat( String path, String... subPaths ) {
    for ( String subPath : subPaths ) {
      path = RepositoryFilenameUtils.concat( path, subPath );
    }
    return path;
  }

  private boolean processZip( InputStream inputStream ) {
    this.files = new ArrayList<>();
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_REPOSITORY_OBJECT" ) );
    }
    try ( ZipInputStream zipInputStream = new ZipInputStream( inputStream ) ) {
      FileService fileService = new FileService();
      ZipEntry entry = zipInputStream.getNextEntry();
      while ( entry != null ) {
        final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
        getLogger().debug( Messages.getInstance().getString( "ZIPFILE.ProcessingEntry", entryName ) );
        final String decodedEntryName = ExportFileNameEncoder.decodeZipFileName( entryName );
        File tempFile = null;
        boolean isDir = entry.isDirectory();
        if ( !isDir ) {
          if ( !solutionHelper.isInApprovedExtensionList( entryName ) ) {
            zipInputStream.closeEntry();
            entry = zipInputStream.getNextEntry();
            continue;
          }

          if ( !fileService.isValidFileName( decodedEntryName ) ) {
            getLogger().error( Messages.getInstance().getString( "DefaultImportHandler.ERROR_0011_INVALID_FILE_NAME", decodedEntryName ) );
            throw new PlatformImportException(
                Messages.getInstance().getString( "DefaultImportHandler.ERROR_0011_INVALID_FILE_NAME",
                    entryName ), PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
          }

          tempFile = File.createTempFile( "zip", null );
          tempFile.deleteOnExit();
          try ( FileOutputStream fos = new FileOutputStream( tempFile ) ) {
            IOUtils.copy( zipInputStream, fos );
          }
        } else {
          if ( !fileService.isValidFileName( decodedEntryName ) ) {
            getLogger().error( Messages.getInstance().getString( "DefaultImportHandler.ERROR_0011_INVALID_FILE_NAME", decodedEntryName ) );
            throw new PlatformImportException(
                Messages.getInstance().getString( "DefaultImportHandler.ERROR_0012_INVALID_FOLDER_NAME",
                    entryName ), PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR );
          }
        }
        File file = new File( entryName );
        RepositoryFile repoFile =
            new RepositoryFile.Builder( file.getName() ).folder( isDir ).hidden( false ).build();
        String parentDir =
            file.getParent() == null ? RepositoryFile.SEPARATOR : file.getParent()
                + RepositoryFile.SEPARATOR;
        IRepositoryFileBundle repoFileBundle =
            new RepositoryFileBundle( repoFile, null, parentDir, tempFile, UTF_8, null );

        if ( EXPORT_MANIFEST_XML_FILE.equals( file.getName() ) ) {
          initializeAclManifest( repoFileBundle );
        } else {
          if ( isPerformingRestore ) {
            getLogger().debug( "Adding file " + repoFile.getName() + " to list for later processing " );
          }
          files.add( repoFileBundle );
        }
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
    } catch ( IOException | PlatformImportException e ) {
      getLogger().error( Messages.getInstance()
          .getErrorString( "ZIPFILE.ExceptionOccurred", e.getLocalizedMessage() ), e );
      return false;
    }
    if ( isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_REPOSITORY_OBJECT" ) );
    }
    return true;
  }

  private void initializeAclManifest( IRepositoryFileBundle file ) {
    try {
      byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
      ByteArrayInputStream in = new ByteArrayInputStream( bytes );
      getImportSession().setManifest( ExportManifest.fromXml( in ) );
    } catch ( Exception e ) {
      getLogger().trace( e );
    }
  }

  @Override
  public List<IMimeType> getMimeTypes() {
    return mimeTypes;
  }

  // handlers that extend this class may override this method and perform operations
  // over the bundle prior to entering its designated importer.importFile()
  public IPlatformImportBundle build( RepositoryFileImportBundle.Builder builder ) {
    return builder != null ? builder.build() : null;
  }

  // handlers that extend this class may override this method and perform operations
  // over the job prior to its creation at scheduler.createJob()
//  public Response createSchedulerJob( ISchedulerResource scheduler, IJobScheduleRequest jobScheduleRequest )
//      throws IOException {
//    Response rs = scheduler != null ? (Response) scheduler.createJob( jobScheduleRequest ) : null;
//    if ( jobScheduleRequest.getJobState() != JobState.NORMAL ) {
//      IJobRequest jobRequest = PentahoSystem.get( IScheduler.class, "IScheduler2", null ).createJobRequest();
//      jobRequest.setJobId( rs.getEntity().toString() );
//      scheduler.pauseJob( jobRequest );
//    }
//    return rs;
//  }

  public boolean isOverwriteFile() {
    return overwriteFile;
  }

  public void setOverwriteFile( boolean overwriteFile ) {
    this.overwriteFile = overwriteFile;
  }

  public boolean isPerformingRestore() {
    return isPerformingRestore;
  }
}
