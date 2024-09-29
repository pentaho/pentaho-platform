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

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.engine.security.userroledao.AlreadyExistsException;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IUserRoleDao;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IJobRequest;
import org.pentaho.platform.api.scheduler2.IJobScheduleParam;
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
import java.io.Serializable;
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

  private static final String RESERVEDMAPKEY_LINEAGE_ID = "lineage-id";

  private static final String XMI_EXTENSION = ".xmi";

  private static final String EXPORT_MANIFEST_XML_FILE = "exportManifest.xml";
  private static final String DOMAIN_ID = "domain-id";
  private static final String UTF_8 = StandardCharsets.UTF_8.name();

  private IUnifiedRepository repository; // TODO inject via Spring
  protected Map<String, RepositoryFileImportBundle.Builder> cachedImports;
  private SolutionFileImportHelper solutionHelper;
  private List<IMimeType> mimeTypes;
  private boolean overwriteFile;
  private List<IRepositoryFileBundle> files;

  public SolutionImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
    repository = PentahoSystem.get( IUnifiedRepository.class );
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

    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    if ( !processZip( bundle.getInputStream() ) ) {
      // Something went wrong, do not proceed!
      return;
    }

    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    setOverwriteFile( bundle.overwriteInRepository() );

    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    cachedImports = new HashMap<>();

    //Process Manifest Settings
    ExportManifest manifest = getImportSession().getManifest();
    String manifestVersion = null;
    // Process Metadata
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();

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

      // Add DB Connections
      List<org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection> datasourceList = manifest.getDatasourceList();
      if ( datasourceList != null ) {
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
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      }
    }

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
        byte[] bytes = IOUtils.toByteArray( fileBundle.getInputStream() );
        RepositoryFileImportBundle.Builder builder = cachedImports.get( repositoryFilePath );
        builder.input( new ByteArrayInputStream( bytes ) );

        importer.importFile( build( builder ) );
        continue;
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
            .getString( "SolutionImportHandler.SkipLocaleFile",  repositoryFilePath ) );
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
      importer.importFile( platformImportBundle );

      if ( bundleInputStream != null ) {
        bundleInputStream.close();
        bundleInputStream = null;
      }
    }

    if ( manifest != null ) {
      importSchedules( manifest.getScheduleList() );
    }

    // Process locale files.
    localeFilesProcessor.processLocaleFiles( importer );
  }

  List<IJob> getAllJobs( ISchedulerResource schedulerResource ) {
    return schedulerResource.getJobsList();
  }

  private RepositoryFile getFile( IPlatformImportBundle importBundle, IRepositoryFileBundle fileBundle ) {
    String repositoryFilePath =
      repositoryPathConcat( importBundle.getPath(), fileBundle.getPath(), fileBundle.getFile().getName() );
    return repository.getFile( repositoryFilePath );
  }

  protected void importSchedules( List<IJobScheduleRequest> scheduleList ) throws PlatformImportException {
    if ( CollectionUtils.isNotEmpty( scheduleList ) ) {
      IScheduler scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$
      ISchedulerResource schedulerResource = scheduler.createSchedulerResource();
      schedulerResource.pause();
      for ( IJobScheduleRequest jobScheduleRequest : scheduleList ) {

        boolean jobExists = false;

        List<IJob> jobs = getAllJobs( schedulerResource );
        if ( jobs != null ) {

          //paramRequest to map<String, Serializable>
          Map<String, Serializable> mapParamsRequest = new HashMap<>();
          for ( IJobScheduleParam paramRequest : jobScheduleRequest.getJobParameters() ) {
            mapParamsRequest.put( paramRequest.getName(), paramRequest.getValue() );
          }

          for ( IJob job : jobs ) {

            if ( ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID ) != null )
              && ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID )
              .equals( job.getJobParams().get( RESERVEDMAPKEY_LINEAGE_ID ) ) ) ) {
              jobExists = true;
            }

            if ( overwriteFile && jobExists ) {
              IJobRequest jobRequest = scheduler.createJobRequest();
              jobRequest.setJobId( job.getJobId() );
              schedulerResource.removeJob( jobRequest );
              jobExists = false;
              break;
            }
          }
        }

        if ( !jobExists ) {
          try {
            Response response = createSchedulerJob( schedulerResource, jobScheduleRequest );
            if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
              if ( response.getEntity() != null ) {
                // get the schedule job id from the response and add it to the import session
                ImportSession.getSession().addImportedScheduleJobId( response.getEntity().toString() );
              }
            }
          } catch ( Exception e ) {
            // there is a scenario where if the file scheduled has a space in the file name, that it won't work. the
            // di server

            // replaces spaces with underscores and the export mechanism can't determine if it needs this to happen
            // or not
            // so, if we failed to import and there is a space in the path, try again but this time with replacing
            // the space(s)
            if ( jobScheduleRequest.getInputFile().contains( " " ) || jobScheduleRequest.getOutputFile()
              .contains( " " ) ) {
              getLogger().info( Messages.getInstance()
                .getString( "SolutionImportHandler.SchedulesWithSpaces", jobScheduleRequest.getInputFile() ) );
              File inFile = new File( jobScheduleRequest.getInputFile() );
              File outFile = new File( jobScheduleRequest.getOutputFile() );
              String inputFileName = inFile.getParent() + RepositoryFile.SEPARATOR
                + inFile.getName().replace( " ", "_" );
              String outputFileName = outFile.getParent() + RepositoryFile.SEPARATOR
                + outFile.getName().replace( " ", "_" );
              jobScheduleRequest.setInputFile( inputFileName );
              jobScheduleRequest.setOutputFile( outputFileName );
              try {
                if ( !File.separator.equals( RepositoryFile.SEPARATOR ) ) {
                  // on windows systems, the backslashes will result in the file not being found in the repository
                  jobScheduleRequest.setInputFile( inputFileName.replace( File.separator, RepositoryFile.SEPARATOR ) );
                  jobScheduleRequest
                    .setOutputFile( outputFileName.replace( File.separator, RepositoryFile.SEPARATOR ) );
                }
                Response response = createSchedulerJob( schedulerResource, jobScheduleRequest );
                if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                  if ( response.getEntity() != null ) {
                    // get the schedule job id from the response and add it to the import session
                    ImportSession.getSession().addImportedScheduleJobId( response.getEntity().toString() );
                  }
                }
              } catch ( Exception ex ) {
                // log it and keep going. we should stop processing all schedules just because one fails.
                getLogger().error( Messages.getInstance()
                  .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", e.getMessage() ), ex );
              }
            } else {
              // log it and keep going. we should stop processing all schedules just because one fails.
              getLogger().error( Messages.getInstance()
                .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", e.getMessage() ) );
            }
          }
        } else {
          getLogger().info( Messages.getInstance()
            .getString( "DefaultImportHandler.ERROR_0009_OVERWRITE_CONTENT", jobScheduleRequest.toString() ) );
        }
      }
      schedulerResource.start();
    }
  }

  protected void importMetaStore( ExportManifestMetaStore manifestMetaStore, boolean overwrite ) {
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

    if ( users != null && roleDao != null ) {
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
          roleDao.createUser( tenant, user.getUsername(), password, null, userRoles );
        } catch ( AlreadyExistsException e ) {
          // it's ok if the user already exists, it is probably a default user
          getLogger().info( Messages.getInstance().getString( "USER.Already.Exists", user.getUsername() ) );

          try {
            if ( isOverwriteFile() ) {
              // set the roles, maybe they changed
              roleDao.setUserRoles( tenant, user.getUsername(), userRoles );

              // set the password just in case it changed
              roleDao.setPassword( tenant, user.getUsername(), password );
            }
          } catch ( Exception ex ) {
            // couldn't set the roles or password either
            getLogger().debug( Messages.getInstance()
              .getString( "ERROR.OverridingExistingUser", user.getUsername() ), ex );
          }
        } catch ( Exception e ) {
          getLogger().error( Messages.getInstance()
            .getString( "ERROR.OverridingExistingUser", user.getUsername() ), e );
        }
        importUserSettings( user );
      }
    }
    return roleToUserMap;
  }

  protected void importGlobalUserSettings( List<ExportManifestUserSetting> globalSettings ) {
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    if ( settingService != null ) {
      for ( ExportManifestUserSetting globalSetting : globalSettings ) {
        if ( isOverwriteFile() ) {
          settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
        } else {
          IUserSetting userSetting = settingService.getGlobalUserSetting( globalSetting.getName(), null );
          if ( userSetting == null ) {
            settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
          }
        }
      }
    }
  }

  protected void importUserSettings( UserExport user ) {
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    IAnyUserSettingService userSettingService = null;
    if ( settingService != null && settingService instanceof IAnyUserSettingService ) {
      userSettingService = (IAnyUserSettingService) settingService;
    }

    if ( userSettingService != null ) {
      List<ExportManifestUserSetting> exportedSettings = user.getUserSettings();
      try {
        for ( ExportManifestUserSetting exportedSetting : exportedSettings ) {
          if ( isOverwriteFile() ) {
            userSettingService.setUserSetting( user.getUsername(),
              exportedSetting.getName(), exportedSetting.getValue() );
          } else {
            // see if it's there first before we set this setting
            IUserSetting userSetting =
              userSettingService.getUserSetting( user.getUsername(), exportedSetting.getName(), null );
            if ( userSetting == null ) {
              // only set it if we didn't find that it exists already
              userSettingService.setUserSetting( user.getUsername(),
                exportedSetting.getName(), exportedSetting.getValue() );
            }
          }
        }
      } catch ( SecurityException e ) {
        String errorMsg = Messages.getInstance().getString( "ERROR.ImportingUserSetting", user.getUsername() );
        getLogger().error( errorMsg );
        getLogger().debug( errorMsg, e );
      }
    }
  }

  protected void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap ) {
    if ( roles != null ) {
      IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
      ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
        IRoleAuthorizationPolicyRoleBindingDao.class );

      Set<String> existingRoles = new HashSet<>();

      for ( RoleExport role : roles ) {
        getLogger().debug( Messages.getInstance().getString( "ROLE.importing", role.getRolename() ) );
        try {
          List<String> users = roleToUserMap.get( role.getRolename() );
          String[] userarray = users == null ? new String[] {} : users.toArray( new String[] {} );
          IPentahoRole role1 = roleDao.createRole( tenant, role.getRolename(), null, userarray );
        } catch ( AlreadyExistsException e ) {
          existingRoles.add( role.getRolename() );
          // it's ok if the role already exists, it is probably a default role
          getLogger().info( Messages.getInstance().getString( "ROLE.Already.Exists", role.getRolename() ) );
        }
        try {
          if ( existingRoles.contains( role.getRolename() ) ) {
            //Only update an existing role if the overwrite flag is set
            if ( isOverwriteFile() ) {
              roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
            }
          } else {
            //Always write a roles permissions that were not previously existing
            roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
          }
        } catch ( Exception e ) {
          getLogger().info( Messages.getInstance()
            .getString( "ERROR.SettingRolePermissions", role.getRolename() ), e );
        }
      }
    }
  }

  /**
   * <p>Import the Metadata</p>
   *
   * @param metadataList metadata to be imported
   * @param preserveDsw  whether or not to preserve DSW settings
   */
  protected void importMetadata( List<ExportManifestMetadata> metadataList, boolean preserveDsw ) {
    if ( null != metadataList ) {
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {
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
      }
    }
  }

  protected void importMondrian( List<ExportManifestMondrian> mondrianList ) {
    if ( null != mondrianList ) {
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {

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
      }
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
    try ( ZipInputStream zipInputStream = new ZipInputStream( inputStream ) ) {
      FileService fileService = new FileService();
      ZipEntry entry = zipInputStream.getNextEntry();
      while ( entry != null ) {
        final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
        getLogger().trace( Messages.getInstance().getString( "ZIPFILE.ProcessingEntry", entryName ) );
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
  public Response createSchedulerJob( ISchedulerResource scheduler, IJobScheduleRequest jobScheduleRequest )
    throws IOException {
    Response rs = scheduler != null ? (Response) scheduler.createJob( jobScheduleRequest ) : null;
    if ( jobScheduleRequest.getJobState() != JobState.NORMAL ) {
      IJobRequest jobRequest = PentahoSystem.get( IScheduler.class, "IScheduler2", null ).createJobRequest();
      jobRequest.setJobId( rs.getEntity().toString() );
      scheduler.pauseJob( jobRequest );
    }
    return rs;
  }

  public boolean isOverwriteFile() {
    return overwriteFile;
  }

  public void setOverwriteFile( boolean overwriteFile ) {
    this.overwriteFile = overwriteFile;
  }
}
