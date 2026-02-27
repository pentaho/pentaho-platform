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
import org.pentaho.platform.api.scheduler2.ISchedulerResource;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.services.importexport.DatabaseConnectionConverter;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSession.ManifestFile;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
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

  IRepositoryImportLogger logger = new Log4JRepositoryImportLogger();
  private IUnifiedRepository repository;
  private List<IMimeType> mimeTypes;
  private SolutionFileImportHelper solutionHelper;

  @Deprecated
  /** @deprecated This may hold the wrong value if there is a concurrent import. Do not use */
  public boolean overwriteFile;

  @VisibleForTesting
  protected static class ImportState {
    /** whether the import was partially successful. */
    protected boolean partialImport;
    protected Map<String, RepositoryFileImportBundle.Builder> cachedImports = new HashMap<>();
    protected boolean overwriteFile;
    protected List<IRepositoryFileBundle> files = new ArrayList<>();
    protected boolean isPerformingRestore;
  }

  private List<IImportHelper> importHelpers = new ArrayList<>();

  public SolutionImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
    repository = PentahoSystem.get( IUnifiedRepository.class );
  }

  public void addImportHelper( IImportHelper helper ) {
    importHelpers.add( helper );
  }


  private void runImportHelpers( IImportHelper.ImportContext context )  {
    for ( IImportHelper helper : importHelpers ) {
      try {
        helper.doImport( context );
      } catch ( ImportException exportException ) {
        logger.error( "Error performing backup of component [ " + helper.getName() + " ] Cause [ " + exportException.getLocalizedMessage() + " ]" );
        System.out.println( exportException.getLocalizedMessage() );
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
    var importState = new ImportState();
    IPlatformImporter platformImporter = PentahoSystem.get( IPlatformImporter.class );
    importState.isPerformingRestore = platformImporter.getRepositoryImportLogger().isPerformingRestore();
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_PROCESS" ) );
    }

    // Processing file
    if ( importState.isPerformingRestore ) {
      getLogger().debug( " Start:  pre processing files and folder from the bundle" );
    }
    if ( !processZip( bundle.getInputStream(), importState ) ) {
      // Something went wrong, do not proceed!
      return;
    }
    if ( importState.isPerformingRestore ) {
      getLogger().debug( " End:  pre processing files and folder from the bundle" );
    }
    importState.overwriteFile = bundle.overwriteInRepository();

    //Process Manifest Settings
    ExportManifest manifest = getImportSession().getManifest();
    // Process Metadata
    if ( manifest != null ) {
      Map<String, List<String>> roleToUserMap = importUsers( manifest.getUserExports(), importState );

      importRoles( manifest.getRoleExports(), roleToUserMap, importState );

      importMetadata( manifest.getMetadataList(), bundle.isPreserveDsw(), importState );

      importMondrian( manifest.getMondrianList(), importState );

      importMetaStore( manifest.getMetaStore(), bundle.overwriteInRepository(), importState );

      importJDBCDataSource( manifest, importState );
    }
    importRepositoryFilesAndFolders( manifest, bundle, importState );

    // import schedules and any other imports defined by ImportHelper
    if ( manifest != null ) {
      // to be removed when interfaces are updated
      overwriteFile = bundle.overwriteInRepository();
      runImportHelpers( getImportCtx( importState ) );
    }
    if ( importState.partialImport ) {
      throw new PlatformImportException( "Some files have invalid mime types",
        PlatformImportException.PUBLISH_PARTIAL_UPLOAD );
    }
  }

  private IImportHelper.ImportContext getImportCtx( ImportState state ) {
    return new IImportHelper.ImportContext() {

      @Override
      public Log getLogger() {
        return getImportSession().getLogger();
      }

      @Override
      public boolean isPerformingRestore() {
        return state.isPerformingRestore;
      }

      @Override
      public boolean isOverwriteFile() {
        return state.overwriteFile;
      }
    };
  }

  protected void importRepositoryFilesAndFolders( ExportManifest manifest, IPlatformImportBundle bundle, ImportState importState ) throws IOException {
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_FILEFOLDER" ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_FILEFOLDER", importState.files.size() ) );
    }
    int successfulFilesImportCount = 0;
    String manifestVersion = null;
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();
    }
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;

    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    for ( IRepositoryFileBundle fileBundle : importState.files ) {
      String fileName = fileBundle.getFile().getName();
      String actualFilePath = fileBundle.getPath();
      if ( manifestVersion != null ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }
      String repositoryFilePath =
          RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      var cachedImports = importState.cachedImports;
      if ( cachedImports.containsKey( repositoryFilePath ) ) {
        getLogger().debug( "Repository object with path [ " + repositoryFilePath + " ] found in the cache" );
        byte[] bytes = IOUtils.toByteArray( fileBundle.getInputStream() );
        RepositoryFileImportBundle.Builder builder = cachedImports.get( repositoryFilePath );
        builder.input( new ByteArrayInputStream( bytes ) );

        try {
          importer.importFile( build( builder ) );
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ] from the cache" );
          }
          successfulFilesImportCount++;
          continue;
        } catch ( PlatformImportException e ) {
          if ( importState.isPerformingRestore ) {
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
        if ( importState.isPerformingRestore ) {
          getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ]" );
        }
      } catch ( PlatformImportException e ) {
        if ( importState.isPerformingRestore ) {
          getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", repositoryFilePath, e.getLocalizedMessage() ) );
        }
      }

      if ( bundleInputStream != null ) {
        bundleInputStream.close();
      }
    }

    // Process locale files.
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_LOCALEFILE" ) );
    }
    int successfulLocaleFilesProcessed = 0;
    try {
      successfulLocaleFilesProcessed = localeFilesProcessor.processLocaleFiles( importer );
    } catch ( PlatformImportException e ) {
      if ( importState.isPerformingRestore ) {
        getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_LOCALE_FILE", e.getLocalizedMessage() ) );
      }
    } finally {
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_LOCALEFILE" ) );
      }
    }

    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString(
        "SolutionImportHandler.INFO_SUCCESSFUL_REPOSITORY_IMPORT_COUNT", successfulFilesImportCount
          + successfulLocaleFilesProcessed, importState.files.size() ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_FILEFOLDER" ) );
    }
  }

  protected void importJDBCDataSource( ExportManifest manifest, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_DATASOURCE" ) );
    }
    // Add DB Connections
    List<org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection> datasourceList = manifest.getDatasourceList();
    if ( datasourceList != null ) {
      int successfulDatasourceImportCount = 0;
      if ( importState.isPerformingRestore ) {
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
            if ( importState.overwriteFile ) {
              databaseConnection.setId( existingDBConnection.getId() );
              datasourceMgmtSvc.updateDatasourceByName( databaseConnection.getName(),
                  DatabaseConnectionConverter.export2model( databaseConnection ) );
            }
          } else {
            datasourceMgmtSvc.createDatasource( DatabaseConnectionConverter.export2model( databaseConnection ) );
          }
          successfulDatasourceImportCount++;
        } catch ( Exception e ) {
          if ( importState.isPerformingRestore ) {
            getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_JDBC_DATASOURCE", databaseConnection.getName(), e.getLocalizedMessage() ) );
          }
        }
      }
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_DATASOURCE_IMPORT_COUNT", successfulDatasourceImportCount, datasourceList.size() ) );
      }
    }
    if ( importState.isPerformingRestore ) {
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

  protected void importMetaStore( ExportManifestMetaStore manifestMetaStore, boolean overwrite, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
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

      importState.cachedImports.put( manifestMetaStore.getFile(), bundleBuilder );
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_IMPORT_METASTORE" ) );
      }
    }
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METASTORE" ) );
    }
  }

  /**
   * Imports UserExport objects into the platform as users.
   *
   * @param users
   * @param importState the import state containing cached imports
   * @return A map of role names to list of users in that role
   */
  protected Map<String, List<String>> importUsers( List<UserExport> users, ImportState importState ) {
    Map<String, List<String>> roleToUserMap = new HashMap<>();
    IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
    ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
    int successFullUserImportCount = 0;
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER" ) );
    }
    if ( users != null && roleDao != null ) {
      if ( importState.isPerformingRestore ) {
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
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Restoring user [ " + user.getUsername() + " ] " );
          }
          roleDao.createUser( tenant, user.getUsername(), password, null, userRoles );
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Successfully restored user [ " + user.getUsername() + " ]" );
          }
          successFullUserImportCount++;
        } catch ( AlreadyExistsException e ) {
          // it's ok if the user already exists, it is probably a default user
          getLogger().debug( Messages.getInstance().getString( "USER.Already.Exists", user.getUsername() ) );

          try {
            if ( importState.overwriteFile ) {
              if ( importState.isPerformingRestore ) {
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
        if ( importState.isPerformingRestore ) {
          getLogger().debug( "Restoring user [ " + user.getUsername() + " ] specific settings" );
        }
        importUserSettings( user, importState );
        if ( importState.isPerformingRestore ) {
          getLogger().debug( "Successfully restored user [ " + user.getUsername() + " ] specific settings" );
        }
      }
    }
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_COUNT", successFullUserImportCount, users.size() ) );
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_USER" ) );
    }
    return roleToUserMap;
  }

  protected void importGlobalUserSettings( List<ExportManifestUserSetting> globalSettings, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
      getLogger().debug( "************************[ Start: Restore global user  settings] *************************" );
    }
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    if ( settingService != null ) {
      for ( ExportManifestUserSetting globalSetting : globalSettings ) {
        if ( importState.overwriteFile ) {
          if ( importState.isPerformingRestore ) {
            getLogger().trace( "Overwrite flag is set to true." );
          }
          settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Finished restore of global user setting with name [ " + globalSetting.getName() + " ]" );
          }
        } else {
          if ( importState.isPerformingRestore ) {
            getLogger().trace( "Overwrite flag is set to false." );
          }
          IUserSetting userSetting = settingService.getGlobalUserSetting( globalSetting.getName(), null );
          if ( userSetting == null ) {
            settingService.setGlobalUserSetting( globalSetting.getName(), globalSetting.getValue() );
            if ( importState.isPerformingRestore ) {
              getLogger().debug( "Finished restore of global user setting with name [ " + globalSetting.getName() + " ]" );
            }
          }
        }
      }
    }
    if ( importState.isPerformingRestore ) {
      getLogger().debug( "************************[ End: Restore global user settings] *************************" );
    }
  }

  protected void importUserSettings( UserExport user, ImportState importState ) {
    IUserSettingService settingService = PentahoSystem.get( IUserSettingService.class );
    IAnyUserSettingService userSettingService = null;
    int userSettingsListSize = 0;
    int successfulUserSettingsImportCount = 0;
    if ( settingService != null && settingService instanceof IAnyUserSettingService ) {
      userSettingService = (IAnyUserSettingService) settingService;
    }
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_USER_SETTING" ) );
    }
    if ( userSettingService != null ) {
      List<ExportManifestUserSetting> exportedSettings = user.getUserSettings();
      userSettingsListSize = user.getUserSettings().size();
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_USER_SETTING", userSettingsListSize, user.getUsername() ) );
      }
      try {
        for ( ExportManifestUserSetting exportedSetting : exportedSettings ) {
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Restore user specific setting  [ " + exportedSetting.getName() + " ]" );
          }
          if ( importState.overwriteFile ) {
            if ( importState.isPerformingRestore ) {
              getLogger().debug( "Overwrite is set to true. So restoring setting  [ " + exportedSetting.getName() + " ]" );
            }
            userSettingService.setUserSetting( user.getUsername(),
                exportedSetting.getName(), exportedSetting.getValue() );
            if ( importState.isPerformingRestore ) {
              getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
            }
          } else {
            // see if it's there first before we set this setting
            if ( importState.isPerformingRestore ) {
              getLogger().debug( "Overwrite is set to false. Only restore setting  [ " + exportedSetting.getName() + " ] if is does not exist" );
            }
            IUserSetting userSetting =
                userSettingService.getUserSetting( user.getUsername(), exportedSetting.getName(), null );
            if ( userSetting == null ) {
              // only set it if we didn't find that it exists already
              userSettingService.setUserSetting( user.getUsername(), exportedSetting.getName(), exportedSetting.getValue() );
              if ( importState.isPerformingRestore ) {
                getLogger().debug( "Finished restore of user specific setting with name [ " + exportedSetting.getName() + " ]" );
              }
            }
          }
          successfulUserSettingsImportCount++;
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Successfully restored setting  [ " + exportedSetting.getName() + " ]" );
          }
        }
      } catch ( SecurityException e ) {
        String errorMsg = Messages.getInstance().getString( "ERROR.ImportingUserSetting", user.getUsername() );
        getLogger().error( errorMsg );
        getLogger().debug( errorMsg, e );
      } finally {
        if ( importState.isPerformingRestore ) {
          getLogger().info( Messages.getInstance()
              .getString( "SolutionImportHandler.INFO_SUCCESSFUL_USER_SETTING_IMPORT_COUNT", successfulUserSettingsImportCount, userSettingsListSize ) );
          getLogger().info( Messages.getInstance()
              .getString( "SolutionImportHandler.INFO_END_IMPORT_USER_SETTING" ) );
        }
      }
    }
  }

  protected void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_ROLE" ) );
    }
    if ( roles != null ) {
      IUserRoleDao roleDao = PentahoSystem.get( IUserRoleDao.class );
      ITenant tenant = new Tenant( "/pentaho/" + TenantUtils.getDefaultTenant(), true );
      IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
          IRoleAuthorizationPolicyRoleBindingDao.class );

      Set<String> existingRoles = new HashSet<>();
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_ROLE", roles.size() ) );
      }
      int successFullRoleImportCount = 0;
      for ( RoleExport role : roles ) {
        getLogger().debug( Messages.getInstance().getString( "ROLE.importing", role.getRolename() ) );
        try {
          List<String> users = roleToUserMap.get( role.getRolename() );
          String[] userarray = users == null ? new String[] {} : users.toArray( new String[] {} );
          roleDao.createRole( tenant, role.getRolename(), null, userarray );
          successFullRoleImportCount++;
        } catch ( AlreadyExistsException e ) {
          existingRoles.add( role.getRolename() );
          // it's ok if the role already exists, it is probably a default role
          getLogger().debug( Messages.getInstance().getString( "ROLE.Already.Exists", role.getRolename() ) );
        }
        try {
          if ( existingRoles.contains( role.getRolename() ) ) {
            //Only update an existing role if the overwrite flag is set
            if ( importState.overwriteFile ) {
              if ( importState.isPerformingRestore ) {
                getLogger().debug( "Overwrite is set to true so restoring role [ " + role.getRolename() + "]" );
              }
              roleBindingDao.setRoleBindings( tenant, role.getRolename(), role.getPermissions() );
            }
          } else {
            if ( importState.isPerformingRestore ) {
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
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_ROLE_COUNT", successFullRoleImportCount, roles.size() ) );
      }
    }
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_ROLE" ) );
    }
  }

  /**
   * <p>Import the Metadata</p>
   *
   * @param metadataList metadata to be imported
   * @param preserveDsw  whether or not to preserve DSW settings
   * @param importState  the import state containing cached imports
   */
  protected void importMetadata( List<ExportManifestMetadata> metadataList, boolean preserveDsw, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METADATA_DATASOURCE" ) );
    }
    if ( null != metadataList ) {
      int successfulMetadataModelImport = 0;
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_METADATA_DATASOURCE", metadataList.size() ) );
      }
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {
        if ( importState.isPerformingRestore ) {
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
                .overwriteFile( importState.overwriteFile )
                .mime( "text/xmi+xml" )
                .withParam( DOMAIN_ID, domainId );

        importState.cachedImports.put( exportManifestMetadata.getFile(), bundleBuilder );
        if ( importState.isPerformingRestore ) {
          getLogger().debug( " Successfully restored  [ " + exportManifestMetadata.getDomainId() + " ] datasource" );
        }
        successfulMetadataModelImport++;
      }
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_METADATA_DATASOURCE_COUNT", successfulMetadataModelImport, metadataList.size() ) );
      }
    }
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METADATA_DATASOURCE" ) );
    }
  }

  protected void importMondrian( List<ExportManifestMondrian> mondrianList, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_MONDRIAN_DATASOURCE" ) );
    }
    if ( null != mondrianList ) {
      int successfulMondrianSchemaImport = 0;
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_MONDRIAN_DATASOURCE", mondrianList.size() ) );
      }
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {
        if ( importState.isPerformingRestore ) {
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
                importState.overwriteFile ).mime( "application/vnd.pentaho.mondrian+xml" )
                .withParam( "parameters", parametersStr.toString() )
                .withParam( DOMAIN_ID, catName ); // TODO: this is definitely named wrong at the very least.
        // pass as param if not in parameters string
        String xmlaEnabled = "" + exportManifestMondrian.isXmlaEnabled();
        bundleBuilder.withParam( "EnableXmla", xmlaEnabled );

        importState.cachedImports.put( exportManifestMondrian.getFile(), bundleBuilder );

        String annotationsFile = exportManifestMondrian.getAnnotationsFile();
        if ( annotationsFile != null ) {
          RepositoryFileImportBundle.Builder annotationsBundle =
              new RepositoryFileImportBundle.Builder().path( MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
                  + RepositoryFile.SEPARATOR + catName ).name( "annotations.xml" ).charSet( UTF_8 ).overwriteFile(
                  importState.overwriteFile ).mime( "text/xml" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable(
                  RepositoryFile.SCHEDULABLE_BY_DEFAULT ).withParam( DOMAIN_ID, catName );
          importState.cachedImports.put( annotationsFile, annotationsBundle );
        }
        successfulMondrianSchemaImport++;
        if ( importState.isPerformingRestore ) {
          getLogger().debug( " Successfully restored  [ " + exportManifestMondrian.getCatalogName() + " ] mondrian datasource" );
        }
      }
      if ( importState.isPerformingRestore ) {
        getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_MONDRIAN_DATASOURCE_IMPORT_COUNT", successfulMondrianSchemaImport, mondrianList.size() ) );
      }
    }
    if ( importState.isPerformingRestore ) {
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

  private boolean processZip( InputStream inputStream, ImportState importState ) {
    if ( importState.isPerformingRestore ) {
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
            importState.partialImport = true;
            continue;
          }

          if ( !fileService.isValidFileName( entryName ) ) {
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
          if ( !fileService.isValidFileName( entryName ) ) {
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
          if ( importState.isPerformingRestore ) {
            getLogger().debug( "Adding file " + repoFile.getName() + " to list for later processing " );
          }
          importState.files.add( repoFileBundle );
        }
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
    } catch ( IOException | PlatformImportException e ) {
      getLogger().error( Messages.getInstance()
          .getErrorString( "ZIPFILE.ExceptionOccurred", e.getLocalizedMessage() ), e );
      return false;
    }
    if ( importState.isPerformingRestore ) {
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

  @Deprecated
  public boolean isPerformingRestore() {
    return PentahoSystem.get( IPlatformImporter.class ).getRepositoryImportLogger().isPerformingRestore();
  }
}
