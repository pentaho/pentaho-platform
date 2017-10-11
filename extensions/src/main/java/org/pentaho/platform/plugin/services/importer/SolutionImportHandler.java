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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.Response;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.Job.JobState;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
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
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.JobRequest;
import org.pentaho.platform.web.http.api.resources.JobScheduleParam;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
import org.pentaho.platform.web.http.api.resources.SchedulerResource;

public class SolutionImportHandler implements IPlatformImportHandler {

  private static final Log log = LogFactory.getLog( SolutionImportHandler.class );

  public static final String RESERVEDMAPKEY_LINEAGE_ID = "lineage-id";

  private static final String XMI_EXTENSION = ".xmi";

  private static final String sep = ";";

  private IUnifiedRepository repository; // TODO inject via Spring
  protected Map<String, RepositoryFileImportBundle.Builder> cachedImports;
  private SolutionFileImportHelper solutionHelper;
  private List<IMimeType> mimeTypes;
  private boolean overwriteFile;

  public SolutionImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
    repository = PentahoSystem.get( IUnifiedRepository.class );
  }

  public ImportSession getImportSession() {
    return ImportSession.getSession();
  }

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
    DomainAlreadyExistsException, DomainStorageException, IOException {

    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;
    ZipInputStream zipImportStream = new ZipInputStream( bundle.getInputStream() );
    SolutionRepositoryImportSource importSource = new SolutionRepositoryImportSource( zipImportStream );
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    setOverwriteFile( bundle.overwriteInRepository() );
    // importSession.set(ImportSession.getSession());

    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    cachedImports = new HashMap<String, RepositoryFileImportBundle.Builder>();

    //Process Manifest Settings
    ExportManifest manifest = getImportSession().getManifest();
    String manifestVersion = null;
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();
    }
    // Process Metadata
    if ( manifest != null ) {

      // import the users
      Map<String, List<String>> roleToUserMap = importUsers( manifest.getUserExports() );
      // import the roles
      importRoles( manifest.getRoleExports(), roleToUserMap );

      List<ExportManifestMetadata> metadataList = manifest.getMetadataList();
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {

        String domainId = exportManifestMetadata.getDomainId();
        if ( domainId != null && !domainId.endsWith( XMI_EXTENSION ) ) {
          domainId = domainId + XMI_EXTENSION;
        }
        boolean overWriteInRepository = isOverwriteFile();
        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( "UTF-8" )
                .hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT )
              // let the parent bundle control whether or not to preserve DSW settings
              .preserveDsw( bundle.isPreserveDsw() )
              .overwriteFile( overWriteInRepository )
              .mime( "text/xmi+xml" )
              .withParam( "domain-id", domainId );

        cachedImports.put( exportManifestMetadata.getFile(), bundleBuilder );

      }

      // Process Mondrian
      List<ExportManifestMondrian> mondrianList = manifest.getMondrianList();
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {

        String catName = exportManifestMondrian.getCatalogName();
        Parameters parametersMap = exportManifestMondrian.getParameters();
        StringBuilder parametersStr = new StringBuilder();
        for ( String s : parametersMap.keySet() ) {
          parametersStr.append( s ).append( "=" ).append( parametersMap.get( s ) ).append( sep );
        }

        RepositoryFileImportBundle.Builder bundleBuilder =
            new RepositoryFileImportBundle.Builder().charSet( "UTF_8" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT )
                .schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT ).name( catName ).overwriteFile(
              isOverwriteFile() ).mime( "application/vnd.pentaho.mondrian+xml" )
                .withParam( "parameters", parametersStr.toString() ).withParam( "domain-id", catName ); // TODO: this is
        // definitely
        // named wrong
        // at the very
        // least.
        // pass as param if not in parameters string
        String xmlaEnabled = "" + exportManifestMondrian.isXmlaEnabled();
        bundleBuilder.withParam( "EnableXmla", xmlaEnabled );

        cachedImports.put( exportManifestMondrian.getFile(), bundleBuilder );

        String annotationsFile = exportManifestMondrian.getAnnotationsFile();
        if ( annotationsFile != null ) {
          RepositoryFileImportBundle.Builder annotationsBundle =
              new RepositoryFileImportBundle.Builder().path( MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
                  + RepositoryFile.SEPARATOR + catName ).name( "annotations.xml" ).charSet( "UTF_8" ).overwriteFile(
                      isOverwriteFile() ).mime( "text/xml" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable(
                          RepositoryFile.SCHEDULABLE_BY_DEFAULT ).withParam( "domain-id", catName );
          cachedImports.put( annotationsFile, annotationsBundle );

        }
      }
    }

    importMetaStore( manifest, bundle.overwriteInRepository() );

    for ( IRepositoryFileBundle fileBundle : importSource.getFiles() ) {
      String fileName = fileBundle.getFile().getName();
      String actualFilePath = fileBundle.getPath();
      if ( manifestVersion != null ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }
      String repositoryFilePath =
        RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      if ( this.cachedImports.containsKey( repositoryFilePath ) ) {

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
          log.trace( "Skipping [" + repositoryFilePath + "], it is a locale property file" );
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

      bundleBuilder.charSet( bundle.getCharset() );
      bundleBuilder.overwriteFile( bundle.overwriteInRepository() );
      bundleBuilder.applyAclSettings( bundle.isApplyAclSettings() );
      bundleBuilder.retainOwnership( bundle.isRetainOwnership() );
      bundleBuilder.overwriteAclSettings( bundle.isOverwriteAclSettings() );
      bundleBuilder.acl( getImportSession().processAclForFile( sourcePath ) );

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

      // Add Hitachi Vantara Connections
      List<org.pentaho.database.model.DatabaseConnection> datasourceList = manifest.getDatasourceList();
      if ( datasourceList != null ) {
        IDatasourceMgmtService datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class );
        for ( org.pentaho.database.model.DatabaseConnection databaseConnection : datasourceList ) {
          if ( databaseConnection.getDatabaseType() == null ) {
            // don't try to import the connection if there is no type it will cause an error
            // However, if this is the DI Server, and the connection is defined in a ktr, it will import automatically
            log.warn(
              "Can't import connection " + databaseConnection.getName() + " because it doesn't have a databaseType" );
            continue;
          }
          try {
            IDatabaseConnection existingDBConnection =
              datasourceMgmtSvc.getDatasourceByName( databaseConnection.getName() );
            if ( existingDBConnection != null && existingDBConnection.getName() != null ) {
              if ( isOverwriteFile() ) {
                databaseConnection.setId( existingDBConnection.getId() );
                datasourceMgmtSvc.updateDatasourceByName( databaseConnection.getName(), databaseConnection );
              }
            } else {
              datasourceMgmtSvc.createDatasource( databaseConnection );
            }
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        }
      }
    }
    // Process locale files.
    localeFilesProcessor.processLocaleFiles( importer );
  }

  List<Job> getAllJobs( SchedulerResource schedulerResource ) {
    return schedulerResource.getAllJobs();
  }

  private RepositoryFile getFile( IPlatformImportBundle importBundle, IRepositoryFileBundle fileBundle ) {
    String repositoryFilePath =
        repositoryPathConcat( importBundle.getPath(), fileBundle.getPath(), fileBundle.getFile().getName() );
    return repository.getFile( repositoryFilePath );
  }

  protected void importSchedules( List<JobScheduleRequest> scheduleList ) throws PlatformImportException {
    if ( CollectionUtils.isNotEmpty( scheduleList ) ) {
      SchedulerResource schedulerResource = new SchedulerResource();
      schedulerResource.pause();
      for ( JobScheduleRequest jobScheduleRequest : scheduleList ) {

        boolean jobExists = false;

        List<Job> jobs = getAllJobs( schedulerResource );
        if ( jobs != null ) {

          //paramRequest to map<String, Serializable>
          Map<String, Serializable> mapParamsRequest = new HashMap<>();
          for ( JobScheduleParam paramRequest : jobScheduleRequest.getJobParameters() ) {
            mapParamsRequest.put( paramRequest.getName(), paramRequest.getValue() );
          }

          for ( Job job : jobs ) {

            if ( ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID ) != null )
              && ( mapParamsRequest.get( RESERVEDMAPKEY_LINEAGE_ID )
              .equals( job.getJobParams().get( RESERVEDMAPKEY_LINEAGE_ID ) ) ) ) {
              jobExists = true;
            }

            if ( overwriteFile && jobExists ) {
              JobRequest jobRequest = new JobRequest();
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
              log.info( "Could not import schedule, attempting to replace spaces with underscores and retrying: "
                + jobScheduleRequest.getInputFile() );
              File inFile = new File( jobScheduleRequest.getInputFile() );
              File outFile = new File( jobScheduleRequest.getOutputFile() );
              String inputFileName = inFile.getParent() + RepositoryFile.SEPARATOR
                + inFile.getName().replaceAll( " ", "_" );
              String outputFileName = outFile.getParent() + RepositoryFile.SEPARATOR
                + outFile.getName().replaceAll( " ", "_" );
              jobScheduleRequest.setInputFile( inputFileName );
              jobScheduleRequest.setOutputFile( outputFileName );
              try {
                if ( File.separator != RepositoryFile.SEPARATOR ) {
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
                log.error( Messages.getInstance()
                  .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", e.getMessage() ), ex );
              }
            } else {
              // log it and keep going. we should stop processing all schedules just because one fails.
              log.error( Messages.getInstance()
                .getString( "SolutionImportHandler.ERROR_0001_ERROR_CREATING_SCHEDULE", e.getMessage() ) );
            }
          }
        } else {
          log.info( Messages.getInstance()
            .getString( "DefaultImportHandler.ERROR_0009_OVERWRITE_CONTENT", jobScheduleRequest.toString() ) );
        }
      }
      schedulerResource.start();
    }
  }

  protected void importMetaStore( ExportManifest manifest, boolean overwrite ) {
    // get the metastore
    if ( manifest != null ) {
      ExportManifestMetaStore manifestMetaStore = manifest.getMetaStore();
      if ( manifestMetaStore != null ) {
        // get the zipped metastore from the export bundle
        RepositoryFileImportBundle.Builder bundleBuilder =
          new RepositoryFileImportBundle.Builder()
            .path( manifestMetaStore.getFile() )
            .name( manifestMetaStore.getName() )
            .withParam( "description", manifestMetaStore.getDescription() )
            .charSet( "UTF-8" )
            .overwriteFile( overwrite )
            .mime( "application/vnd.pentaho.metastore" );

        cachedImports.put( manifestMetaStore.getFile(), bundleBuilder );
      }
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
        log.debug( "Importing user: " + user.getUsername() );

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
          log.info( Messages.getInstance().getString( "USER.Already.Exists", user.getUsername() ) );

          try {
            if ( isOverwriteFile() ) {
              // set the roles, maybe they changed
              roleDao.setUserRoles( tenant, user.getUsername(), userRoles );

              // set the password just in case it changed
              roleDao.setPassword( tenant, user.getUsername(), password );
            }
          } catch ( Exception ex ) {
            // couldn't set the roles or password either
            log.debug( "Failed to set roles or password for existing user on import", ex );
          }
        } catch ( Exception e ) {
          log.error( Messages.getInstance().getString( "ERROR.CreatingUser", user.getUsername() ) );
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
        log.error( Messages.getInstance().getString( "ERROR.ImportingUserSetting", user.getUsername() ) );
        log.debug( Messages.getInstance().getString( "ERROR.ImportingUserSetting", user.getUsername() ), e );
      }
    }
  }

  protected void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap ) {
    IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
    ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
      IRoleAuthorizationPolicyRoleBindingDao.class );

    Set<String> existingRoles = new HashSet<>();
    if ( roles != null ) {
      for ( RoleExport role : roles ) {
        log.debug( "Importing role: " + role.getRolename() );
        try {
          List<String> users = roleToUserMap.get( role.getRolename() );
          String[] userarray = users == null ? new String[] {} : users.toArray( new String[] {} );
          IPentahoRole role1 = roleDao.createRole( tenant, role.getRolename(), null, userarray );
        } catch ( AlreadyExistsException e ) {
          existingRoles.add( role.getRolename() );
          // it's ok if the role already exists, it is probably a default role
          log.info( Messages.getInstance().getString( "ROLE.Already.Exists", role.getRolename() ) );
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
          log.info( Messages.getInstance().getString( "ERROR.SettingRolePermissions", role.getRolename() ), e );
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
      log.warn( "File [" + sourcePath + "] doesn't have schedulable permission ( isSchedulable = false) "
              + "but there are some schedule(s) in import bundle which refers the file " );
      log.warn( "Assigning 'isSchedulable=true' permission for file [" + sourcePath + "] ... " );
    }

    return isSchedulable;
  }

  private boolean isFileHidden( RepositoryFile file, ManifestFile manifestFile, String sourcePath ) {
    Boolean result = manifestFile.isFileHidden();
    if ( result != null ) {
      return result; // file absent or must receive a new setting and the setting is exist
    }
    if ( file != null ) {
      return file.isHidden(); // old setting
    }
    result = solutionHelper.isInHiddenList( sourcePath );
    if ( result ) {
      return true;
    }
    return RepositoryFile.HIDDEN_BY_DEFAULT; // default setting of type
  }

  private boolean isSchedulable( RepositoryFile file, ManifestFile manifestFile ) {
    Boolean result = manifestFile.isFileSchedulable();
    if ( result != null ) {
      return result; // file absent or must receive a new setting and the setting is exist
    }
    if ( file != null ) {
      return file.isSchedulable(); // old setting
    }
    return RepositoryFile.SCHEDULABLE_BY_DEFAULT; // default setting of type
  }

  // private boolean isSystemDir( final String[] split, final int index ) {
  // return ( split != null && index < split.length && ( StringUtils.equals( split[index], "system" ) || StringUtils
  // .equals( split[index], "admin" ) ) );
  // }

  private String repositoryPathConcat( String path, String... subPaths ) {
    for ( String subPath : subPaths ) {
      path = RepositoryFilenameUtils.concat( path, subPath );
    }
    return path;
  }

  class SolutionRepositoryImportSource {
    private ZipInputStream zipInputStream;
    private List<IRepositoryFileBundle> files;

    public SolutionRepositoryImportSource( final ZipInputStream zipInputStream ) {
      this.zipInputStream = zipInputStream;
      this.files = new ArrayList<IRepositoryFileBundle>();
      initialize();
    }

    protected void initialize() {
      try {
        ZipEntry entry = zipInputStream.getNextEntry();
        while ( entry != null ) {
          final String entryName = RepositoryFilenameUtils.separatorsToRepository( entry.getName() );
          File tempFile = null;
          boolean isDir = entry.isDirectory();
          if ( !isDir ) {
            if ( !solutionHelper.isInApprovedExtensionList( entryName ) ) {
              zipInputStream.closeEntry();
              entry = zipInputStream.getNextEntry();
              continue;
            }
            tempFile = File.createTempFile( "zip", null );
            tempFile.deleteOnExit();
            FileOutputStream fos = new FileOutputStream( tempFile );
            IOUtils.copy( zipInputStream, fos );
            fos.close();
          }
          File file = new File( entryName );
          RepositoryFile repoFile =
            new RepositoryFile.Builder( file.getName() ).folder( isDir ).hidden( false ).build();
          String parentDir =
            new File( entryName ).getParent() == null ? RepositoryFile.SEPARATOR : new File( entryName ).getParent()
              + RepositoryFile.SEPARATOR;
          IRepositoryFileBundle repoFileBundle =
            new RepositoryFileBundle( repoFile, null, parentDir, tempFile, "UTF-8", null );

          if ( file.getName().equals( "exportManifest.xml" ) ) {
            initializeAclManifest( repoFileBundle );
          } else {
            files.add( repoFileBundle );
          }
          zipInputStream.closeEntry();
          entry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
      } catch ( IOException exception ) {
        final String errorMessage = Messages.getInstance().getErrorString( "", exception.getLocalizedMessage() );
        log.trace( errorMessage );
      }
    }

    private void initializeAclManifest( IRepositoryFileBundle file ) {
      try {
        byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
        ByteArrayInputStream in = new ByteArrayInputStream( bytes );
        getImportSession().setManifest( ExportManifest.fromXml( in ) );
      } catch ( Exception e ) {
        log.trace( e );
      }
    }

    public List<IRepositoryFileBundle> getFiles() {
      return this.files;
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
  public Response createSchedulerJob( SchedulerResource scheduler, JobScheduleRequest jobScheduleRequest )
    throws IOException {
    Response rs = scheduler != null ? scheduler.createJob( jobScheduleRequest ) : null;
    if ( jobScheduleRequest.getJobState() != JobState.NORMAL ) {
      JobRequest jobRequest = new JobRequest();
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
