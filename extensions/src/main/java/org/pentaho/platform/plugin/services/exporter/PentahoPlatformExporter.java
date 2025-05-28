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


package org.pentaho.platform.plugin.services.exporter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.api.util.IPentahoPlatformExporter;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importexport.DatabaseConnectionConverter;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository.solution.filebased.MondrianVfs;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PentahoPlatformExporter extends ZipExportProcessor implements IPentahoPlatformExporter {

  private static final Logger log = LoggerFactory.getLogger( PentahoPlatformExporter.class );

  public static final String ROOT = "/";
  public static final String DATA_SOURCES_PATH_IN_ZIP = "_datasources/";
  public static final String METADATA_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "metadata/";
  public static final String ANALYSIS_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "analysis/";
  public static final String METASTORE = "metastore";
  public static final String METASTORE_BACKUP_EXT = ".mzip";

  protected ZipOutputStream zos;

  private IScheduler scheduler;
  private IMetadataDomainRepository metadataDomainRepository;
  private IDatasourceMgmtService datasourceMgmtService;
  private IMondrianCatalogService mondrianCatalogService;
  private MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;
  private IMetaStore metastore;
  private IUserSettingService userSettingService;

  private final List<IExportHelper> exportHelpers = new ArrayList<>();

  public PentahoPlatformExporter( IUnifiedRepository repository ) {
    super( ROOT, repository, true );
    setUnifiedRepository( repository );
    addExportHandler( new DefaultExportHandler() );
  }

  public File performExport() throws ExportException, IOException {
    return this.performExport( null );
  }

  public void addExportHelper( IExportHelper helper ) {
    exportHelpers.add( helper );
  }

  public void runExportHelpers() {
    for ( IExportHelper helper : exportHelpers ) {
      try {
        helper.doExport( this );
      } catch ( ExportException exportException ) {
        getRepositoryExportLogger().error( "Error performing backup of component [ " + helper.getName() + " ] Cause [ " + exportException.getLocalizedMessage() + " ]" );
      }
    }
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  @Override
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_PROCESS" ) );
    // always export root
    exportRepositoryFile = getUnifiedRepository().getFile( ROOT );

    // create temp file
    File exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    zos = new ZipOutputStream( new FileOutputStream( exportFile ) );

    try {
      exportFileContent( exportRepositoryFile );
    } catch ( ExportException | IOException exception ) {
      getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_EXPORT_FILE_CONTENT", exception.getLocalizedMessage() ) );
    }

    exportDatasources();
    exportMondrianSchemas();
    exportMetadataModels();
    runExportHelpers();
    exportUsersAndRoles();
    exportMetastore();

    if ( this.withManifest ) {
      // write manifest to zip output stream
      ZipEntry entry = new ZipEntry( EXPORT_MANIFEST_FILENAME );
      zos.putNextEntry( entry );

      // pass output stream to manifest class for writing
      try {
        getExportManifest().toXml( zos );
      } catch ( Exception e ) {
        // todo: add to messages.properties
        getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_GENERATING_EXPORT_XML" ) );
      }

      zos.closeEntry();
    }

    zos.close();

    // clean up
    initManifest();
    zos = null;

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_PROCESS" ) );

    return exportFile;
  }

  protected void exportDatasources() {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_JDBC_DATASOURCE" ) );
    // get all connection to export
    int successfulExportJDBCDSCount = 0;
    int databaseConnectionsSize = 0;
    try {
      List<IDatabaseConnection> databaseConnections = getDatasourceMgmtService().getDatasources();
      if ( databaseConnections != null ) {
        databaseConnectionsSize = databaseConnections.size();
        getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_JDBC_DATASOURCE_TO_EXPORT", databaseConnectionsSize ) );
      }
      for ( IDatabaseConnection datasource : databaseConnections ) {
        if ( datasource instanceof org.pentaho.database.model.DatabaseConnection ) {
          getRepositoryExportLogger().debug( "Starting to perform backup of datasource [ " + datasource.getName() + " ]" );
          getExportManifest().addDatasource( DatabaseConnectionConverter.model2export( datasource ) );
          getRepositoryExportLogger().debug( "Finished performing backup of datasource [ " + datasource.getName() + " ]" );
          successfulExportJDBCDSCount++;
        }
      }
    } catch ( DatasourceMgmtServiceException e ) {
      getRepositoryExportLogger().warn( "Unable to retrieve JDBC datasource(s). Cause [" + e.getMessage() + " ]" );
      getRepositoryExportLogger().debug( "Unable to retrieve JDBC datasource(s). Cause [" + e.getMessage() + " ]", e );
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_JDBC_DATASOURCE_EXPORT_COUNT", successfulExportJDBCDSCount, databaseConnectionsSize ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_JDBC_DATASOURCE" ) );
  }

  protected void exportMetadataModels() {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_METADATA" ) );
    int successfulExportMetadataDSCount = 0;
    int metadataDSSize = 0;
    // get all of the metadata models
    Set<String> domainIds = getMetadataDomainRepository().getDomainIds();
    if ( domainIds != null ) {
      metadataDSSize = domainIds.size();
      getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_METADATA_DATASOURCE_TO_EXPORT", metadataDSSize ) );
    }

    for ( String domainId : domainIds ) {
      // get all of the files for this model
      Map<String, InputStream> domainFilesData = getDomainFilesData( domainId );
      getRepositoryExportLogger().debug( "Starting to backup metadata datasource [ " + domainId + " ]" );
      for ( String fileName : domainFilesData.keySet() ) {
        getRepositoryExportLogger().trace( "Adding metadata file [ " + fileName + " ]" );
        // write the file to the zip
        String metadataFilePath = METADATA_PATH_IN_ZIP + fileName;
        if ( !metadataFilePath.endsWith( ".xmi" ) ) {
          metadataFilePath += ".xmi";
        }
        String metadataZipEntryName = metadataFilePath;
        if ( this.withManifest ) {
          metadataZipEntryName = ExportFileNameEncoder.encodeZipPathName( metadataZipEntryName );
        }
        ZipEntry zipEntry = new ZipEntry( metadataZipEntryName );
        InputStream inputStream = domainFilesData.get( fileName );

        try {
          zos.putNextEntry( zipEntry );
          IOUtils.copy( inputStream, zos );

          // add the info to the exportManifest
          ExportManifestMetadata metadata = new ExportManifestMetadata();
          metadata.setDomainId( domainId );
          metadata.setFile( metadataFilePath );
          getExportManifest().addMetadata( metadata );
          successfulExportMetadataDSCount++;
        } catch ( IOException e ) {
          getRepositoryExportLogger().warn( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_METADATA_DATASOURCE_EXPORT", e.getMessage() ), e );
        } finally {
          IOUtils.closeQuietly( inputStream );
          try {
            zos.closeEntry();
          } catch ( IOException e ) {
            // can't close the entry of input stream
          }
        }
        getRepositoryExportLogger().trace( "Successfully added metadata file [ " + fileName + " ] to the manifest" );
      }
      getRepositoryExportLogger().debug( "Successfully perform backup of metadata datasource [ " + domainId + " ]" );
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_METADATA_DATASOURCE_EXPORT_COUNT", successfulExportMetadataDSCount, metadataDSSize ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_METADATA" ) );
  }

  protected void exportMondrianSchemas() {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_MONDRIAN_DATASOURCE" ) );
    // Get the mondrian catalogs available in the repo
    int successfulExportMondrianDSCount = 0;
    int mondrianDSSize = 0;
    List<MondrianCatalog> catalogs = getMondrianCatalogService().listCatalogs( getSession(), false );
    if ( catalogs != null ) {
      mondrianDSSize = catalogs.size();
      getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_MONDRIAN_DATASOURCE_TO_EXPORT", mondrianDSSize ) );
    }
    for ( MondrianCatalog catalog : catalogs ) {
      getRepositoryExportLogger().debug( "Starting to perform backup mondrian datasource [ " + catalog.getName() + " ]" );
      // get the files for this catalog
      Map<String, InputStream> files = getMondrianCatalogRepositoryHelper().getMondrianSchemaFiles( catalog.getName() );

      ExportManifestMondrian mondrian = new ExportManifestMondrian();
      for ( String fileName : files.keySet() ) {
        getRepositoryExportLogger().trace( "Starting to add filename [ " + fileName + " ] with datasource [" + catalog.getName() + " ] to the bundle" );

        // write the file to the zip
        String path = ANALYSIS_PATH_IN_ZIP + catalog.getName() + "/" + fileName;
        ZipEntry zipEntry = new ZipEntry( new ZipEntry( ExportFileNameEncoder.encodeZipPathName( path ) ) );
        InputStream inputStream = files.get( fileName );

        // ignore *.annotated.xml files, they are not needed
        if ( fileName.equals( "schema.annotated.xml" ) ) {
          // these types of files only exist for contextual export of a data source (from the UI) to later be imported in.
          // However, in the case of backup/restore we don't need these since we'll be using the annotations.xml file along
          // with the original schema xml file to re-generate the model properly
          continue;
        } else if ( MondrianVfs.ANNOTATIONS_XML.equals( fileName ) ) {
          // annotations.xml should be written to the zip file and referenced in the export manifest entry for the
          // related mondrian model
          mondrian.setAnnotationsFile( path );
        } else {
          // must be a true mondrian model
          mondrian.setCatalogName( catalog.getName() );
          boolean xmlaEnabled = parseXmlaEnabled( catalog.getDataSourceInfo() );
          mondrian.setXmlaEnabled( xmlaEnabled );
          mondrian.setFile( path );
          Parameters mondrianParameters = new Parameters();
          mondrianParameters.put( "Provider", "mondrian" );
          //DataSource can be escaped
          mondrianParameters.put( "DataSource", StringEscapeUtils.unescapeXml( catalog.getJndi() ) );
          mondrianParameters.put( "EnableXmla", Boolean.toString( xmlaEnabled ) );

          StreamSupport.stream( catalog.getConnectProperties().spliterator(), false )
              .filter( p -> !mondrianParameters.containsKey( p.getKey() ) )
              //if value is escaped it should be unescaped to avoid double escape after export in xml file, because
              //marshaller executes escaping as well
              .forEach( p -> mondrianParameters.put( p.getKey(), StringEscapeUtils.unescapeXml( p.getValue() ) ) );

          mondrian.setParameters( mondrianParameters );
        }

        try {
          zos.putNextEntry( zipEntry );
          IOUtils.copy( inputStream, zos );
        } catch ( IOException e ) {
          getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_MONDRIAN_DATASOURCE_EXPORT" ) );
        } finally {
          IOUtils.closeQuietly( inputStream );
          try {
            zos.closeEntry();
          } catch ( IOException e ) {
            // can't close the entry of input stream
          }
        }
      }
      if ( mondrian.getCatalogName() != null && mondrian.getFile() != null ) {
        getExportManifest().addMondrian( mondrian );
        getRepositoryExportLogger().debug( "Successfully added filename [ " + mondrian.getFile() + " ] with catalog [" + mondrian.getCatalogName() + " ] to the bundle" );
        successfulExportMondrianDSCount++;
      }
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_MONDRIAN_DATASOURCE_EXPORT_COUNT", successfulExportMondrianDSCount, mondrianDSSize ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_MONDRIAN_DATASOURCE" ) );
  }

  protected boolean parseXmlaEnabled( String dataSourceInfo ) {
    String key = "EnableXmla=";
    int pos = dataSourceInfo.indexOf( key );
    if ( pos == -1 ) {
      // if not specified, assume false
      return false;
    }
    int end = dataSourceInfo.indexOf( ";", pos ) > -1 ? dataSourceInfo.indexOf( ";", pos ) : dataSourceInfo.length();
    String xmlaEnabled = dataSourceInfo.substring( pos + key.length(), end );
    return Boolean.parseBoolean( xmlaEnabled.replace( "\"", "" ) );
  }

  protected void exportUsersAndRoles() {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_USER" ) );
    int successfulExportUsers = 0;
    int usersSize = 0;

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    UserDetailsService userDetailsService = PentahoSystem.get( UserDetailsService.class );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
        IRoleAuthorizationPolicyRoleBindingDao.class );
    ITenant tenant = TenantUtils.getCurrentTenant();

    //  get the user settings for this user
    IUserSettingService service = getUserSettingService();

    //User Export
    List<String> userList = userRoleListService.getAllUsers( tenant );
    if ( userList != null ) {
      usersSize = userList.size();
      getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_USER_TO_EXPORT", usersSize ) );
    }
    for ( String user : userList ) {
      getRepositoryExportLogger().debug( "Starting backup of user [ " + user + " ] " );
      UserExport userExport = new UserExport();
      userExport.setUsername( user );
      userExport.setPassword( userDetailsService.loadUserByUsername( user ).getPassword() );

      for ( String role : userRoleListService.getRolesForUser( tenant, user ) ) {
        getRepositoryExportLogger().trace( "user [ " + user + " ] has an associated role [ " + role + " ]" );
        userExport.setRole( role );
      }

      if ( service != null && service instanceof IAnyUserSettingService ) {
        getRepositoryExportLogger().debug( "Starting backup of user specific settings for user [ " + user + " ] " );
        IAnyUserSettingService userSettings = (IAnyUserSettingService) service;
        List<IUserSetting> settings = userSettings.getUserSettings( user );
        if ( settings != null ) {
          for ( IUserSetting setting : settings ) {
            getRepositoryExportLogger().debug( "Adding user specific setting [ "
                + setting.getSettingName() + " ] with value [ " + setting.getSettingValue() + " ] to backup" );
            userExport.addUserSetting( new ExportManifestUserSetting( setting ) );
            getRepositoryExportLogger().debug( "Successfully added user specific setting [ "
                + setting.getSettingName() + " ] with value [ " + setting.getSettingValue() + " ] to backup" );
          }
        }
        getRepositoryExportLogger().debug( "Finished backup of user specific settings for user [ " + user + " ] " );
      }

      this.getExportManifest().addUserExport( userExport );
      successfulExportUsers++;
      getRepositoryExportLogger().debug( "Successfully perform backup of user [ " + user + " ] " );
    }

    // export the global user settings
    if ( service != null ) {
      getRepositoryExportLogger().debug( "Starting backup of global user settings" );
      List<IUserSetting> globalUserSettings = service.getGlobalUserSettings();
      if ( globalUserSettings != null ) {
        for ( IUserSetting setting : globalUserSettings ) {
          getExportManifest().addGlobalUserSetting( new ExportManifestUserSetting( setting ) );
        }
      }
      getRepositoryExportLogger().debug( "Finished backup of global user settings" );
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_USER_EXPORT_COUNT", successfulExportUsers, usersSize ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_USER" ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_ROLE" ) );
    int successfulExportRoles = 0;
    int rolesSize = 0;

    //RoleExport
    List<String> roles = userRoleListService.getAllRoles();
    if ( roles != null ) {
      rolesSize = roles.size();
      getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_ROLE_TO_EXPORT", rolesSize ) );
    }
    for ( String role : roles ) {
      getRepositoryExportLogger().debug( "Starting backup of role [ " + role + " ] " );
      RoleExport roleExport = new RoleExport();
      roleExport.setRolename( role );
      roleExport.setPermission( roleBindingDao.getRoleBindingStruct( null ).bindingMap.get( role ) );
      exportManifest.addRoleExport( roleExport );
      successfulExportRoles++;
      getRepositoryExportLogger().debug( "Finished backup of role [ " + role + " ] " );
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_ROLE_EXPORT_COUNT", successfulExportRoles, rolesSize ) );

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_ROLE" ) );
  }

  protected void exportMetastore() throws IOException {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_METASTORE" ) );
    try {
      getRepositoryExportLogger().debug( "Starting to copy metastore to a temp location" );
      Path tempDirectory = Files.createTempDirectory( METASTORE );
      IMetaStore xmlMetaStore = new XmlMetaStore( tempDirectory.toString() );
      MetaStoreUtil.copy( getRepoMetaStore(), xmlMetaStore );
      getRepositoryExportLogger().debug( "Finished to copying metastore to a temp location" );
      getRepositoryExportLogger().debug( "Starting to zip the metastore" );
      File zippedMetastore = Files.createTempFile( METASTORE, EXPORT_TEMP_FILENAME_EXT ).toFile();
      ZipOutputStream zipOutputStream = new ZipOutputStream( new FileOutputStream( zippedMetastore ) );
      zipFolder( tempDirectory.toFile(), zipOutputStream, tempDirectory.toString() );
      zipOutputStream.close();
      getRepositoryExportLogger().debug( "Finished zipping the metastore" );
      // now that we have the zipped content of an xml metastore, we need to write that to the export bundle
      FileInputStream zis = new FileInputStream( zippedMetastore );
      String zipFileLocation = METASTORE + METASTORE_BACKUP_EXT;
      ZipEntry metastoreZipFileZipEntry = new ZipEntry( zipFileLocation );
      getRepositoryExportLogger().debug( "Starting to add the metastore zip to the bundle" );
      zos.putNextEntry( metastoreZipFileZipEntry );
      try {
        IOUtils.copy( zis, zos );
        getRepositoryExportLogger().debug( "Finished adding the metastore zip to the bundle" );
      } finally {
        zis.close();
        zos.closeEntry();
      }
      getRepositoryExportLogger().debug( "Starting to add the metastore to the manifest" );
      // add an ExportManifest entry for the metastore.
      ExportManifestMetaStore exportManifestMetaStore = new ExportManifestMetaStore( zipFileLocation,
          getRepoMetaStore().getName(),
          getRepoMetaStore().getDescription() );

      getExportManifest().setMetaStore( exportManifestMetaStore );

      zippedMetastore.deleteOnExit();
      tempDirectory.toFile().deleteOnExit();
      getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_EXPORT_METASTORE" ) );
    } catch ( Exception e ) {
      getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ) );
      getRepositoryExportLogger().debug( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ), e );
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_METASTORE" ) );
  }

  protected IMetaStore getRepoMetaStore() {
    if ( metastore == null ) {
      try {
        metastore = MetaStoreExportUtil.connectToRepository( null ).getRepositoryMetaStore();
      } catch ( KettleException e ) {
        // can't get the metastore to import into
        getRepositoryExportLogger().debug( "Can't get the metastore to import into" );

      }
    }
    return metastore;
  }

  protected void setRepoMetaStore( IMetaStore metastore ) {
    this.metastore = metastore;
  }

  protected void zipFolder( File file, ZipOutputStream zos, String pathPrefixToRemove ) {
    if ( file.isDirectory() ) {
      File[] listFiles = file.listFiles();
      for ( File listFile : listFiles ) {
        if ( listFile.isDirectory() ) {
          zipFolder( listFile, zos, pathPrefixToRemove );
        } else {
          if ( !pathPrefixToRemove.endsWith( File.separator ) ) {
            pathPrefixToRemove += File.separator;
          }
          String path = listFile.getPath().replace( pathPrefixToRemove, "" );
          ZipEntry entry = new ZipEntry( path );
          FileInputStream fis = null;
          try {
            zos.putNextEntry( entry );
            fis = new FileInputStream( listFile );
            IOUtils.copy( fis, zos );
          } catch ( IOException e ) {
            e.printStackTrace();
          } finally {
            try {
              zos.closeEntry();
            } catch ( IOException e ) {
              e.printStackTrace();
            }
            IOUtils.closeQuietly( fis );
          }
        }
      }
    }
  }

  protected void exportFileContent( RepositoryFile exportRepositoryFile ) throws IOException, ExportException {
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_REPOSITORY_OBJECT" ) );
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
      getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + "] is a folder" );
      getExportManifest().getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );

      // don't zip root folder without name
      if ( !ClientRepositoryPaths.getRootFolderPath().equals( exportRepositoryFile.getPath() ) ) {
        getRepositoryExportLogger().trace( "Adding a name to the root folder" );
        zos.putNextEntry( new ZipEntry( getFixedZipEntryName( exportRepositoryFile, filePath ) ) );
      }
      getRepositoryExportLogger().debug( "Starting recursive backup of a folder [ " + exportRepositoryFile.getName() + " ]" );
      exportDirectory( exportRepositoryFile, zos, filePath );

    } else {
      getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + "] is a file" );
      getExportManifest().getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );

      try {
        getRepositoryExportLogger().debug( "Starting backup of a file [ " + exportRepositoryFile.getName() + " ]" );
        exportFile( exportRepositoryFile, zos, filePath );
      } catch ( ExportException | IOException exception ) {
        getRepositoryExportLogger().error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_EXPORT_REPOSITORY_OBJECT", exportRepositoryFile.getName() ) );
      } finally {
        getRepositoryExportLogger().debug( "Finished the backup of a file [ " + exportRepositoryFile.getName() + " ]" );
      }
    }
    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_REPOSITORY_OBJECT" ) );
  }

  protected Map<String, InputStream> getDomainFilesData( String domainId ) {
    return ( (IPentahoMetadataDomainRepositoryExporter) metadataDomainRepository ).getDomainFilesData( domainId );
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

  public IMetadataDomainRepository getMetadataDomainRepository() {
    if ( metadataDomainRepository == null ) {
      metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, getSession() );
    }
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository( IMetadataDomainRepository metadataDomainRepository ) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public IDatasourceMgmtService getDatasourceMgmtService() {
    if ( datasourceMgmtService == null ) {
      datasourceMgmtService = PentahoSystem.get( IDatasourceMgmtService.class, getSession() );
    }
    return datasourceMgmtService;
  }

  public void setDatasourceMgmtService( IDatasourceMgmtService datasourceMgmtService ) {
    this.datasourceMgmtService = datasourceMgmtService;
  }

  public MondrianCatalogRepositoryHelper getMondrianCatalogRepositoryHelper() {
    if ( this.mondrianCatalogRepositoryHelper == null ) {
      mondrianCatalogRepositoryHelper = new MondrianCatalogRepositoryHelper( getUnifiedRepository() );
    }
    return mondrianCatalogRepositoryHelper;
  }

  public void setMondrianCatalogRepositoryHelper(
      MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper ) {
    this.mondrianCatalogRepositoryHelper = mondrianCatalogRepositoryHelper;
  }

  public IMondrianCatalogService getMondrianCatalogService() {
    if ( mondrianCatalogService == null ) {
      mondrianCatalogService = PentahoSystem.get( IMondrianCatalogService.class, getSession() );
    }
    return mondrianCatalogService;
  }

  public void setMondrianCatalogService(
      IMondrianCatalogService mondrianCatalogService ) {
    this.mondrianCatalogService = mondrianCatalogService;
  }

  public IUserSettingService getUserSettingService() {
    if ( userSettingService == null ) {
      userSettingService = PentahoSystem.get( IUserSettingService.class, getSession() );
    }
    return userSettingService;
  }

  public void setUserSettingService( IUserSettingService userSettingService ) {
    this.userSettingService = userSettingService;
  }

  @Override
  protected boolean isExportCandidate( String path ) {
    if ( path == null ) {
      return false;
    }

    String etc = ClientRepositoryPaths.getEtcFolderPath();

    // we need to include the etc/operation_mart folder and sub folders
    // but NOT any other folders in /etc

    if ( path.startsWith( etc ) ) {
      // might need to export it...
      String etc_operations_mart = etc + RepositoryFile.SEPARATOR + "operations_mart";
      if ( path.equals( etc ) ) {
        return true;
      } else {
        return path.startsWith( etc_operations_mart );
      }
    }
    return true;
  }

  public ZipOutputStream getZipStream() {
    return zos;
  }
}
