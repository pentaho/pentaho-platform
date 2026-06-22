/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.plugin.services.exporter;

import org.apache.commons.io.IOUtils;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.util.IPentahoPlatformExporter;
import org.pentaho.platform.plugin.services.exporter.helper.DatasourcesExportHelper;
import org.pentaho.platform.plugin.services.exporter.helper.MetadataExportHelper;
import org.pentaho.platform.plugin.services.exporter.helper.MetastoreExportHelper;
import org.pentaho.platform.plugin.services.exporter.helper.MondrianExportHelper;
import org.pentaho.platform.plugin.services.exporter.helper.RepositoryContentExportHelper;
import org.pentaho.platform.plugin.services.exporter.helper.UsersAndRolesExportHelper;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.BackupInventory;
import org.pentaho.platform.plugin.services.importexport.InventoryLogger;
import org.pentaho.platform.plugin.services.importexport.ImportExportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetricsCollector;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PentahoPlatformExporter extends ZipExportProcessor implements IPentahoPlatformExporter {

  private static final Logger log = LoggerFactory.getLogger( PentahoPlatformExporter.class );

  public static final String ROOT = "/";
  public static final String DATA_SOURCES_PATH_IN_ZIP = "_datasources/";
  public static final String METADATA_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "metadata/";
  public static final String ANALYSIS_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "analysis/";
  public static final String CONNECTIONS_PATH_IN_ZIP = DATA_SOURCES_PATH_IN_ZIP + "connections/";
  public static final String METASTORE = "metastore";
  public static final String METASTORE_BACKUP_EXT = ".mzip";

  protected ZipOutputStream zos;

  private IMetaStore metastore;
  private ComponentConfig componentConfig;
  private BackupInventory backupInventory;
  private InventoryLogger inventoryLogger;
  private ImportExportLogger importExportLogger;
  private ImportExportMetricsCollector metricsCollector;
  private ImportExportMetrics exportMetrics;  // New comprehensive metrics collector
  private int exportedFileCount = 0;  // Track total files exported
  private int exportedFolderCount = 0;  // Track total folders exported
  private long exportStartTime = 0;  // Track export start time for duration calculation

  private List<IExportHelper> exportHelpers = new ArrayList<>();

  public PentahoPlatformExporter( IUnifiedRepository repository ) {
    super( ROOT, repository, true );
    setUnifiedRepository( repository );
    addExportHandler( new DefaultExportHandler() );
    
    // Register built-in export helpers
    registerBuiltInExportHelpers( repository );
  }

  /**
   * Register built-in export helpers for standard components.
   * These helpers provide profile-based filtering for selective exports.
   */
  protected void registerBuiltInExportHelpers( IUnifiedRepository repository ) {
    // Register helpers in order of typical export flow
    addExportHelper( new RepositoryContentExportHelper(  ) );
    addExportHelper( new DatasourcesExportHelper(  ) );
    addExportHelper( new MetadataExportHelper( ) );
    addExportHelper( new MondrianExportHelper( ) );
    addExportHelper( new UsersAndRolesExportHelper( ) );
    addExportHelper( new MetastoreExportHelper( ) );
  }

  // ========== Public Delegation Methods for Export Helpers ==========



  public File performExport() throws ExportException, IOException {
    if ( componentConfig == null ) {
      componentConfig = ComponentConfig.fullSystem();
    }
    return this.performExport( null );
  }

  /**
   * Perform selective export based on component configuration
   */
  public File performSelectiveExport( RepositoryFile exportRepositoryFile, ComponentConfig config )
    throws ExportException, IOException {
    this.componentConfig = config;
    getRepositoryExportLogger().info( "Starting selective export: " + config.toString() );
    return this.performExport( exportRepositoryFile );
  }

  /**
   * Perform selective export of root directory
   */
  public File performSelectiveExport( ComponentConfig config ) throws ExportException, IOException {
    return performSelectiveExport( null, config );
  }

  public void addExportHelper( IExportHelper helper ) {
    exportHelpers.add( helper );
  }

  /**
   * Get the list of registered export helpers.
   * @return list of IExportHelper instances
   */
  public List<IExportHelper> getExportHelpers() {
    return exportHelpers;
  }

  // ========== Public Accessors for Helper Use ==========

  /**
   * Public accessor for metrics collector.
   */
  public ImportExportMetricsCollector getMetricsCollector() {
    return metricsCollector;
  }

  /**
   * Public accessor for export metrics.
   */
  public ImportExportMetrics getExportMetrics() {
    return exportMetrics;
  }

  /**
   * Public accessor for import/export logger.
   */
  public ImportExportLogger getImportExportLogger() {
    return importExportLogger;
  }

  /**
   * Public accessor for inventory logger.
   */
  public InventoryLogger getInventoryLogger() {
    return inventoryLogger;
  }

  /**
   * Public accessor for backup inventory.
   */
  public BackupInventory getBackupInventory() {
    return backupInventory;
  }

  /**
   * Public accessor for metastore.
   * Note: Metastore initialization/lazy-loading is now managed by MetastoreExportHelper.
   * This accessor is maintained for backward compatibility and for sharing the instance
   * across helpers if needed.
   */
  public IMetaStore getMetastore() {
    return metastore;
  }

  // ========== Run All Export Helpers ==========
  /**
   * Run all registered export helpers.
   * Each helper is responsible for checking its own configuration and deciding whether to execute.
   * This removes coupling between the exporter and individual helper implementations.
   */
  public void runAllExportHelpers() {
    for ( IExportHelper helper : exportHelpers ) {
      try {
        getRepositoryExportLogger().debug( "Running export helper: " + helper.getName() );
        // Each helper checks its own configuration in doExport() before executing
        helper.doExport( this );
      } catch ( ExportException exportException ) {
        getRepositoryExportLogger().error( "Error in export helper [ " + helper.getName() + " ] Cause [ " + exportException.getLocalizedMessage() + " ]" );
      } catch ( Exception e ) {
        getRepositoryExportLogger().error( "Unexpected error in export helper [ " + helper.getName() + " ]: " + e.getMessage(), e );
      }
    }
  }

  /**
   * Export a specific file from the repository to the export bundle.
   * Used by export helpers to export files referenced by other components (e.g., files referenced by schedules).
   * 
   * @param repositoryFilePath the repository path of the file to export
   * @throws ExportException if the file cannot be exported
   */
  public void exportFileByPath( String repositoryFilePath ) throws ExportException {
    if ( repositoryFilePath == null || repositoryFilePath.trim().isEmpty() ) {
      throw new ExportException( "Repository file path cannot be null or empty" );
    }
    
    try {
      IUnifiedRepository repository = getUnifiedRepository();
      if ( repository == null ) {
        throw new ExportException( "Unable to access unified repository" );
      }
      
      RepositoryFile file = repository.getFile( repositoryFilePath );
      if ( file == null ) {
        throw new ExportException( "File not found in repository: " + repositoryFilePath );
      }
      
      getRepositoryExportLogger().debug( "Exporting dependency file: " + repositoryFilePath );
      exportFileContent( file );
      getRepositoryExportLogger().debug( "Successfully exported dependency file: " + repositoryFilePath );
    } catch ( IOException e ) {
      throw new ExportException( "Error exporting file [ " + repositoryFilePath + " ]: " + e.getMessage(), e );
    }
  }

  /**
   * Performs the export process, returns a zip File object
   *
   * @throws ExportException indicates an error in import processing
   */
  @Override
  public File performExport( RepositoryFile exportRepositoryFile ) throws ExportException, IOException {

    // Initialize component config if not set (backward compatibility)
    if ( componentConfig == null ) {
      componentConfig = ComponentConfig.fullSystem();
    }

    // LOG COMPONENT CONFIG AT START
    getRepositoryExportLogger().info( "========== COMPONENT CONFIG AT EXPORT START ==========" );
    getRepositoryExportLogger().info( "  Content: " + componentConfig.isIncludeContent() );
    getRepositoryExportLogger().info( "  Users: " + componentConfig.isIncludeUsers() );
    getRepositoryExportLogger().info( "  Datasources: " + componentConfig.isIncludeDatasources() );
    getRepositoryExportLogger().info( "  Mondrian: " + componentConfig.isIncludeMondrian() );
    getRepositoryExportLogger().info( "  Metastore: " + componentConfig.isIncludeMetastore() );
    getRepositoryExportLogger().info( "  Schedules: " + componentConfig.isIncludeSchedules() );
    getRepositoryExportLogger().info( "  UserSettings: " + componentConfig.isIncludeUserSettings() );
    getRepositoryExportLogger().info( "  Generated Content: " + componentConfig.isIncludeGeneratedContent() );
    getRepositoryExportLogger().info( "=====================================================" );

    // Reset export counters
    resetExportCounters();

    // Initialize new logging framework
    metricsCollector = new ImportExportMetricsCollector();
    importExportLogger = new ImportExportLogger();
    exportMetrics = new ImportExportMetrics( ImportExportMetrics.OperationType.BACKUP );
    exportStartTime = System.currentTimeMillis();  // Track start time for duration calculation

    // Log backup start with config
    importExportLogger.logBackupStart( componentConfig );

    // Initialize backup inventory tracking (legacy)
    backupInventory = new BackupInventory("BACKUP");
    inventoryLogger = new InventoryLogger(getRepositoryExportLogger(), backupInventory, true);

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_PROCESS" ) );
    // always export root
    exportRepositoryFile = getUnifiedRepository().getFile( ROOT );

    // create temp file
    File exportFile = File.createTempFile( EXPORT_TEMP_FILENAME_PREFIX, EXPORT_TEMP_FILENAME_EXT );
    exportFile.deleteOnExit();

    zos = new ZipOutputStream( new FileOutputStream( exportFile ) );

    // Initialize rootFolder in manifest at the start, before any helpers run
    // This ensures it's always available, even if RepositoryContentExportHelper is skipped
    String rootFolderPath = this.path;
    if ( rootFolderPath != null && rootFolderPath.length() > 0 ) {
      int lastSlashIndex = rootFolderPath.lastIndexOf( "/" );
      if ( lastSlashIndex >= 0 ) {
        rootFolderPath = rootFolderPath.substring( 0, lastSlashIndex + 1 );
      } else {
        rootFolderPath = "/";
      }
    } else {
      rootFolderPath = "/";
    }
    getExportManifest().getManifestInformation().setRootFolder( rootFolderPath );

    // Run all export helpers
    try {
      runAllExportHelpers();
    } catch ( Exception e ) {
      getRepositoryExportLogger().error( "Error running export helpers: " + e.getMessage(), e );
    }

    if ( this.withManifest ) {
      // write manifest to zip output stream
      ZipEntry entry = new ZipEntry( EXPORT_MANIFEST_FILENAME );
      zos.putNextEntry( entry );
      trackFileAdded( EXPORT_MANIFEST_FILENAME );

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

    // Update inventory with file/folder statistics before logging
    if ( backupInventory != null ) {
      backupInventory.setExportFileStats( exportedFileCount, exportedFolderCount );
    }

    // Log final inventory report (legacy)
    if ( inventoryLogger != null ) {
      inventoryLogger.logOperationComplete();
    }

    // Log consolidated metrics summary (new framework)
    if ( metricsCollector != null ) {
      metricsCollector.printConsolidatedSummary();
    }
    
    // Log comprehensive export metrics report
    if ( exportMetrics != null ) {
      long endTime = System.currentTimeMillis();
      long duration = endTime - getStartTime();
      
      getRepositoryExportLogger().info( "" );
      getRepositoryExportLogger().info( "================================================================================" );
      getRepositoryExportLogger().info( "                    BACKUP OPERATION SUMMARY" );
      getRepositoryExportLogger().info( "================================================================================" );
      getRepositoryExportLogger().info( "Duration: " + formatDuration( duration ) );
      getRepositoryExportLogger().info( "" );
      getRepositoryExportLogger().info( exportMetrics.generateDetailedReport() );
      getRepositoryExportLogger().info( "" );
    }

    // Log file count statistics
    getRepositoryExportLogger().info( "Export Summary Statistics:" );
    getRepositoryExportLogger().info( "  Total Files Exported: " + exportedFileCount );
    getRepositoryExportLogger().info( "  Total Folders Exported: " + exportedFolderCount );
    getRepositoryExportLogger().info( "  Total Items Exported: " + getTotalExportedCount() );

    // clean up
    initManifest();
    zos = null;

    getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_PROCESS" ) );

    return exportFile;
  }

  public boolean parseXmlaEnabled( String dataSourceInfo ) {
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

  /**
   * Export a single user and their roles.
   * This method is required by the IPentahoPlatformExporter interface.
   * Delegates to UsersAndRolesExportHelper to perform the actual export.
   * 
   * @param username the username to export
   * @return true if the user was successfully exported, false otherwise
   */
  @Override
  public boolean exportUserAndRole( String username ) {
    if ( username == null || username.trim().isEmpty() ) {
      return false;
    }
    
    // Find and delegate to the UsersAndRolesExportHelper
    for ( IExportHelper helper : exportHelpers ) {
      if ( helper instanceof UsersAndRolesExportHelper ) {
        try {
          UsersAndRolesExportHelper usersHelper = (UsersAndRolesExportHelper) helper;
          return usersHelper.exportUserAndRole( username, this );
        } catch ( Exception e ) {
          getRepositoryExportLogger().error( "Error exporting user [ " + username + " ]: " + e.getMessage(), e );
          return false;
        }
      }
    }
    
    getRepositoryExportLogger().warn( "UsersAndRolesExportHelper not found in registered export helpers" );
    return false;
  }

  /**
   * Export file/folder content from the repository.
   * This method is called by exportFileByPath() and is required for backward compatibility.
   * The actual export logic has been moved to RepositoryContentExportHelper.
   * 
   * @param exportRepositoryFile the repository file to export
   * @throws IOException if I/O error occurs
   * @throws ExportException if export error occurs
   */
  protected void exportFileContent( RepositoryFile exportRepositoryFile ) throws IOException, ExportException {
    if ( exportRepositoryFile == null ) {
      throw new FileNotFoundException( "Repository file not found" );
    }
    
    getRepositoryExportLogger().debug( "Exporting file content for: " + exportRepositoryFile.getPath() );
    
    try {
      // Get the base path for export
      String filePath = new File( this.path ).getParent();
      if ( filePath == null ) {
        filePath = "/";
      }

      if ( exportRepositoryFile.isFolder() ) {
        getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + " ] is a folder" );
        // Delegate to parent class for folder export (exists in ZipExportProcessor)
        exportFile( exportRepositoryFile, zos, filePath );
      } else {
        getRepositoryExportLogger().trace( "Repository object [ " + exportRepositoryFile.getName() + " ] is a file" );
        // Delegate to parent class for file export (exists in ZipExportProcessor)
        exportFile( exportRepositoryFile, zos, filePath );
      }
    } catch ( ExportException | IOException e ) {
      getRepositoryExportLogger().error( "Error exporting file [ " + exportRepositoryFile.getName() + " ]: " + e.getMessage(), e );
      throw e;
    }
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
            trackFileAdded( path );
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







  public Object getComponentConfig() {
    return componentConfig;
  }

  public void setComponentConfig( ComponentConfig componentConfig ) {
    this.componentConfig = componentConfig;
  }

  /**
   * Public accessor for the current export path
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Public accessor for the ZIP output stream
   */
  public ZipOutputStream getZipOutputStream() {
    return this.zos;
  }

  /**
   * Public accessor for setting metastore.
   * Note: Metastore initialization/lazy-loading is now managed by MetastoreExportHelper.
   * This setter is maintained for backward compatibility and testing purposes.
   * Changes made here will be reflected across all helpers that use getMetastore().
   */
  public void setMetastore( IMetaStore metastore ) {
    this.metastore = metastore;
  }

  /**
   * Public accessor for withManifest flag
   */
  public boolean isWithManifest() {
    return this.withManifest;
  }

  /**
   * Public accessor for addToManifest from parent class
   */
  public void addToManifest( RepositoryFile repositoryFile ) throws ExportException {
    super.addToManifest( repositoryFile );
  }

  /**
   * Public accessor for getSession from parent class
   */
  public IPentahoSession getPublicSession() {
    return getSession();
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

  /**
   * Track file being added to ZIP export
   */
  public void trackFileAdded( String zipPath ) {
    exportedFileCount++;
    getRepositoryExportLogger().debug( "Added to ZIP [" + exportedFileCount + "]: " + zipPath );
  }

  /**
   * Track folder being added to ZIP export
   */
  public void trackFolderAdded( String zipPath ) {
    exportedFolderCount++;
    getRepositoryExportLogger().debug( "Added folder to ZIP [" + exportedFolderCount + "]: " + zipPath );
  }

  /**
   * Get total files exported
   */
  public int getExportedFileCount() {
    return exportedFileCount;
  }

  /**
   * Get total folders exported
   */
  public int getExportedFolderCount() {
    return exportedFolderCount;
  }

  /**
   * Get total items exported
   */
  public int getTotalExportedCount() {
    return exportedFileCount + exportedFolderCount;
  }

  /**
   * Reset export counters
   */
  private void resetExportCounters() {
    exportedFileCount = 0;
    exportedFolderCount = 0;
  }

  public ZipOutputStream getZipStream() {
    return zos;
  }

  /**
   * Public accessor for fixed zip entry name.
   * Used by export helpers to get properly formatted zip paths.
   */
  public String getFixedZipEntryName( RepositoryFile repositoryFile, String filePath ) {
    return super.getFixedZipEntryName( repositoryFile, filePath );
  }

  /**
   * Override from ZipExportProcessor to implement generated content filtering.
   * Checks if a file is marked as generated content (has lineage-id metadata)
   * and should be excluded based on the component configuration.
   */
  @Override
  protected boolean shouldSkipGeneratedContent( RepositoryFile repositoryFile ) {
    // Skip filtering if no component config, or if content not included, or if generated content is included
    if ( componentConfig == null || !componentConfig.isIncludeContent() || componentConfig.isIncludeGeneratedContent() ) {
      return false;
    }
    
    // Now check if the file is marked as generated content by looking for lineage-id metadata
    try {
      IUnifiedRepository repo = getUnifiedRepository();
      if ( repo != null && repositoryFile != null && repositoryFile.getId() != null ) {
        java.util.Map<String, Serializable> metadata = repo.getFileMetadata( repositoryFile.getId() );
        if ( metadata != null && metadata.containsKey( "lineage-id" ) ) {
          getRepositoryExportLogger().debug( "Skipping generated content file: " + repositoryFile.getPath() );
          return true;  // This is generated content, skip it
        }
      }
    } catch ( Exception e ) {
      getRepositoryExportLogger().warn( "Error checking file metadata for generated content: " + e.getMessage(), e );
    }
    
    return false;  // Not generated content, don't skip
  }

  /**
   * Get the export start time
   */
  protected long getStartTime() {
    return exportStartTime;
  }

  /**
   * Format duration in milliseconds to human-readable format
   */
  private String formatDuration( long millis ) {
    if ( millis < 0 ) return "0ms";
    long seconds = ( millis / 1000 ) % 60;
    long minutes = ( millis / ( 1000 * 60 ) ) % 60;
    long hours = millis / ( 1000 * 60 * 60 );
    StringBuilder result = new StringBuilder();
    if ( hours > 0 ) {
      result.append( hours ).append( "h " );
    }
    if ( minutes > 0 || hours > 0 ) {
      result.append( minutes ).append( "m " );
    }
    result.append( seconds ).append( "s" );
    return result.toString();
  }

  // ========== DEPRECATED STUB METHODS FOR BACKWARD COMPATIBILITY AND TESTING ==========
  // These methods have been moved to their respective helpers and are provided here
  // only for backward compatibility and to allow existing tests to compile.
  // DO NOT USE IN NEW CODE - these will be removed in a future version.
  
  /**
   * @deprecated Use RepositoryContentExportHelper instead
   */
  @Deprecated
  protected void exportFolderHierarchyWithMetadata( RepositoryFile file, ZipOutputStream zos, String path ) {
    getRepositoryExportLogger().warn( "exportFolderHierarchyWithMetadata() is deprecated. This method has been moved to RepositoryContentExportHelper." );
  }

  /**
   * @deprecated Use RepositoryContentExportHelper instead
   */
  @Deprecated
  protected void exportFolderAcls( RepositoryFile folder ) {
    getRepositoryExportLogger().warn( "exportFolderAcls() is deprecated. This method has been moved to RepositoryContentExportHelper." );
  }

  /**
   * @deprecated Use UsersAndRolesExportHelper instead
   */
  @Deprecated
  protected void exportUsersAndRoles() {
    getRepositoryExportLogger().warn( "exportUsersAndRoles() is deprecated. This method has been moved to UsersAndRolesExportHelper." );
  }

  /**
   * @deprecated Use MetadataExportHelper instead
   */
  @Deprecated
  protected void exportMetadataModels() {
    getRepositoryExportLogger().warn( "exportMetadataModels() is deprecated. This method has been moved to MetadataExportHelper." );
  }

  /**
   * @deprecated Use MetadataExportHelper instead
   */
  @Deprecated
  public Map<String, InputStream> getDomainFilesData( String domainId ) {
    getRepositoryExportLogger().warn( "getDomainFilesData() is deprecated. This method has been moved to MetadataExportHelper." );
    return new java.util.HashMap<>();
  }

  /**
   * @deprecated Use MetadataExportHelper instead
   */
  @Deprecated
  public void setMetadataDomainRepository( IMetadataDomainRepository metadataDomainRepository ) {
    getRepositoryExportLogger().warn( "setMetadataDomainRepository() is deprecated. This method has been moved to MetadataExportHelper." );
  }

  /**
   * @deprecated Use DatasourcesExportHelper instead
   */
  @Deprecated
  protected void exportDatasources() {
    getRepositoryExportLogger().warn( "exportDatasources() is deprecated. This method has been moved to DatasourcesExportHelper." );
  }

  /**
   * @deprecated Use DatasourcesExportHelper instead
   */
  @Deprecated
  public void setDatasourceMgmtService( IDatasourceMgmtService datasourceMgmtService ) {
    getRepositoryExportLogger().warn( "setDatasourceMgmtService() is deprecated. This method has been moved to DatasourcesExportHelper." );
  }

  /**
   * @deprecated Use MondrianExportHelper instead
   */
  @Deprecated
  protected void exportMondrianSchemas() {
    getRepositoryExportLogger().warn( "exportMondrianSchemas() is deprecated. This method has been moved to MondrianExportHelper." );
  }

  /**
   * @deprecated Use MondrianExportHelper instead
   */
  @Deprecated
  public void setMondrianCatalogRepositoryHelper( MondrianCatalogRepositoryHelper mondrianCatalogRepositoryHelper ) {
    getRepositoryExportLogger().warn( "setMondrianCatalogRepositoryHelper() is deprecated. This method has been moved to MondrianExportHelper." );
  }

  /**
   * @deprecated Use MetastoreExportHelper instead
   */
  @Deprecated
  protected void exportMetastore() {
    getRepositoryExportLogger().warn( "exportMetastore() is deprecated. This method has been moved to MetastoreExportHelper." );
  }

}
