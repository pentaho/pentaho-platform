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

package org.pentaho.platform.plugin.services.exporter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.stores.xml.XmlMetaStore;
import org.pentaho.metastore.util.MetaStoreUtil;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.usersettings.IAnyUserSettingService;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.ExportException;
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
import org.pentaho.platform.scheduler2.versionchecker.EmbeddedVersionCheckSystemListener;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.JobScheduleRequest;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PentahoPlatformExporter extends ZipExportProcessor {

  private static final Logger log = LoggerFactory.getLogger( PentahoPlatformExporter.class );

  public static final String ROOT = "/";
  public static final String DATA_SOURCES_PATH_IN_ZIP = "_datasources/";
  public static final String METADATA_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "metadata/";
  public static final String ANALYSIS_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "analysis/";
  public static final String CONNECTIONS_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "connections/";
  public static final String METASTORE = "metastore";
  public static final String METASTORE_BACKUP_EXT = ".mzip";

  private File exportFile;
  protected ZipOutputStream zos;

  private IScheduler scheduler;
  private IMetadataDomainRepository metadataDomainRepository;
  private IDatasourceMgmtService datasourceMgmtService;
  private IMondrianCatalogService mondrianCatalogService;
  private MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper;
  private IMetaStore metastore;
  private IUserSettingService userSettingService;

  public PentahoPlatformExporter( IUnifiedRepository repository ) {
    super( ROOT, repository, true );
    setUnifiedRepository( repository );
    addExportHandler( new DefaultExportHandler() );
  }

  public File performExport() throws ExportException, IOException {
    return this.performExport( null );
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  @Override
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {

    // always export root
    exportRepositoryFile = getUnifiedRepository().getFile( ROOT );

    // create temp file
    exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    zos = new ZipOutputStream( new FileOutputStream( exportFile ) );

    exportFileContent( exportRepositoryFile );
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
        getExportManifest().toXml( zos );
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

  protected void exportDatasources() {
    log.debug( "export datasources" );
    // get all connection to export
    try {
      List<IDatabaseConnection> datasources = getDatasourceMgmtService().getDatasources();
      for ( IDatabaseConnection datasource : datasources ) {
        if ( datasource instanceof DatabaseConnection ) {
          getExportManifest().addDatasource( (DatabaseConnection) datasource );
        }
      }
    } catch ( DatasourceMgmtServiceException e ) {
      log.warn( e.getMessage(), e );
    }
  }

  protected void exportMetadataModels() {
    log.debug( "export metadata models" );

    // get all of the metadata models
    Set<String> domainIds = getMetadataDomainRepository().getDomainIds();

    for ( String domainId : domainIds ) {
      // get all of the files for this model
      Map<String, InputStream> domainFilesData = getDomainFilesData( domainId );

      for ( String fileName : domainFilesData.keySet() ) {
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

        } catch ( IOException e ) {
          log.warn( e.getMessage(), e );
        } finally {
          IOUtils.closeQuietly( inputStream );
          try {
            zos.closeEntry();
          } catch ( IOException e ) {
            // can't close the entry of input stream
          }
        }
      }
    }
  }

  protected void exportMondrianSchemas() {
    log.debug( "export mondrian schemas" );

    // Get the mondrian catalogs available in the repo
    List<MondrianCatalog> catalogs = getMondrianCatalogService().listCatalogs( getSession(), false );
    for ( MondrianCatalog catalog : catalogs ) {

      // get the files for this catalog
      Map<String, InputStream> files = getMondrianCatalogRepositoryHelper().getModrianSchemaFiles( catalog.getName() );

      ExportManifestMondrian mondrian = new ExportManifestMondrian();
      for ( String fileName : files.keySet() ) {

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
          log.warn( e.getMessage(), e );
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
      }
    }
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
    return xmlaEnabled == null ? false : Boolean.parseBoolean( xmlaEnabled.replace( "\"", "" ) );
  }

  protected void exportSchedules() {
    log.debug( "export schedules" );
    try {
      List<Job> jobs = getScheduler().getJobs( null );
      for ( Job job : jobs ) {
        if ( job.getJobName().equals( EmbeddedVersionCheckSystemListener.VERSION_CHECK_JOBNAME ) ) {
          // don't bother exporting the Version Checker schedule, it gets created automatically on server start
          // if it doesn't exist and fails if you try to import it due to a null ActionClass
          continue;
        }
        try {
          JobScheduleRequest scheduleRequest = ScheduleExportUtil.createJobScheduleRequest( job );
          getExportManifest().addSchedule( scheduleRequest );
        } catch ( IllegalArgumentException e ) {
          log.warn( e.getMessage(), e );
        }
      }
    } catch ( SchedulerException e ) {
      log.error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR_EXPORTING_JOBS" ), e );
    }
  }

  protected void exportUsersAndRoles() {
    log.debug( "export users & roles" );

    IUserRoleListService userRoleListService = PentahoSystem.get( IUserRoleListService.class );
    UserDetailsService userDetailsService = PentahoSystem.get( UserDetailsService.class );

    IRoleAuthorizationPolicyRoleBindingDao roleBindingDao = PentahoSystem.get(
      IRoleAuthorizationPolicyRoleBindingDao.class );
    ITenant tenant = TenantUtils.getCurrentTenant();

    //  get the user settings for this user
    IUserSettingService service = getUserSettingService();

    //User Export
    List<String> userList = userRoleListService.getAllUsers( tenant );
    for ( String user : userList ) {
      UserExport userExport = new UserExport();
      userExport.setUsername( user );
      userExport.setPassword( userDetailsService.loadUserByUsername( user ).getPassword() );

      for ( String role : userRoleListService.getRolesForUser( tenant, user ) ) {
        userExport.setRole( role );
      }

      if ( service != null && service instanceof IAnyUserSettingService ) {
        IAnyUserSettingService userSettings = (IAnyUserSettingService) service;
        List<IUserSetting> settings = userSettings.getUserSettings( user );
        if ( settings != null ) {
          for ( IUserSetting setting : settings ) {
            userExport.addUserSetting( new ExportManifestUserSetting( setting ) );
          }
        }
      }

      this.getExportManifest().addUserExport( userExport );
    }

    // export the global user settings
    if ( service != null ) {
      List<IUserSetting> globalUserSettings = service.getGlobalUserSettings();
      if ( globalUserSettings != null ) {
        for ( IUserSetting setting : globalUserSettings ) {
          getExportManifest().addGlobalUserSetting( new ExportManifestUserSetting( setting ) );
        }
      }
    }

    //RoleExport
    List<String> roles = userRoleListService.getAllRoles();
    for ( String role : roles ) {
      RoleExport roleExport = new RoleExport();
      roleExport.setRolename( role );
      roleExport.setPermission( roleBindingDao.getRoleBindingStruct( null ).bindingMap.get( role ) );
      exportManifest.addRoleExport( roleExport );
    }
  }

  protected void exportMetastore() throws IOException {
    log.debug( "export the metastore" );
    try {
      Path tempDirectory = Files.createTempDirectory( METASTORE );
      IMetaStore xmlMetaStore = new XmlMetaStore( tempDirectory.toString() );
      MetaStoreUtil.copy( getRepoMetaStore(), xmlMetaStore );

      File zippedMetastore = Files.createTempFile( METASTORE, EXPORT_TEMP_FILENAME_EXT ).toFile();
      ZipOutputStream zipOutputStream = new ZipOutputStream( new FileOutputStream( zippedMetastore ) );
      zipFolder( tempDirectory.toFile(), zipOutputStream, tempDirectory.toString() );
      zipOutputStream.close();

      // now that we have the zipped content of an xml metastore, we need to write that to the export bundle
      FileInputStream zis = new FileInputStream( zippedMetastore );
      String zipFileLocation = METASTORE + METASTORE_BACKUP_EXT;
      ZipEntry metastoreZipFileZipEntry = new ZipEntry( zipFileLocation );
      zos.putNextEntry( metastoreZipFileZipEntry );
      try {
        IOUtils.copy( zis, zos );
      } catch ( IOException e ) {
        throw e;
      } finally {
        zis.close();
        zos.closeEntry();
      }

      // add an ExportManifest entry for the metastore.
      ExportManifestMetaStore exportManifestMetaStore = new ExportManifestMetaStore( zipFileLocation,
        getRepoMetaStore().getName(),
        getRepoMetaStore().getDescription() );

      getExportManifest().setMetaStore( exportManifestMetaStore );

      zippedMetastore.deleteOnExit();
      tempDirectory.toFile().deleteOnExit();

    } catch ( Exception e ) {
      log.error( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ) );
      log.debug( Messages.getInstance().getString( "PentahoPlatformExporter.ERROR.ExportingMetaStore" ), e );
    }
  }

  protected IMetaStore getRepoMetaStore() {
    if ( metastore == null ) {
      try {
        metastore = MetaStoreExportUtil.connectToRepository( null ).getMetaStore();
      } catch ( KettleException e ) {
        // can't get the metastore to import into
        log.debug( "Can't get the metastore to import into" );
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
      getExportManifest().getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );

      // don't zip root folder without name
      if ( !ClientRepositoryPaths.getRootFolderPath().equals( exportRepositoryFile.getPath() ) ) {
        zos.putNextEntry( new ZipEntry( getFixedZipEntryName( exportRepositoryFile, filePath ) ) );
      }
      exportDirectory( exportRepositoryFile, zos, filePath );

    } else {
      getExportManifest().getManifestInformation().setRootFolder( path.substring( 0, path.lastIndexOf( "/" ) + 1 ) );
      exportFile( exportRepositoryFile, zos, filePath );
    }
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
      } else if ( path.startsWith( etc_operations_mart ) ) {
        return true;
      } else {
        return false;
      }
    }
    return true;
  }

}
