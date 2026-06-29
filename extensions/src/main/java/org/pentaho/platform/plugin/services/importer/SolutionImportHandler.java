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


package org.pentaho.platform.plugin.services.importer;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.castor.core.util.Assert;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ExportManifestUserSetting;
import org.pentaho.platform.plugin.services.importexport.IRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.ImportSession;
import org.pentaho.platform.plugin.services.importexport.ImportSession.ManifestFile;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.Log4JRepositoryImportLogger;
import org.pentaho.platform.plugin.services.importexport.RepositoryFileBundle;
import org.pentaho.platform.plugin.services.importexport.RoleExport;
import org.pentaho.platform.plugin.services.importexport.UserExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.importer.helper.UsersAndRolesImportHelper;
import org.pentaho.platform.plugin.services.importer.helper.MetadataImportHelper;
import org.pentaho.platform.plugin.services.importer.helper.MondrianImportHelper;
import org.pentaho.platform.plugin.services.importer.helper.MetastoreImportHelper;
import org.pentaho.platform.plugin.services.importer.helper.JdbcDatasourceImportHelper;
import org.pentaho.platform.plugin.services.importer.helper.RepositoryFilesImportHelper;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SolutionImportHandler implements IPlatformImportHandler {

  private static final String EXPORT_MANIFEST_XML_FILE = "exportManifest.xml";
  private static final String DOMAIN_ID = "domain-id";
  private static final String UTF_8 = StandardCharsets.UTF_8.name();
  private IUnifiedRepository repository; // TODO inject via Spring
  protected Map<String, RepositoryFileImportBundle.Builder> cachedImports;
  private SolutionFileImportHelper solutionHelper;
  private List<IMimeType> mimeTypes;
  private boolean overwriteFile;
  private List<IRepositoryFileBundle> files;
  private boolean isPerformingRestore = false;
  protected ImportExportMetrics metrics;
  protected List<IImportHelper> importHelpers = new ArrayList<>();

  // Static SLF4J logger for pre-context operations (e.g., helper execution)
  private static final Logger STATIC_LOGGER = LoggerFactory.getLogger( SolutionImportHandler.class );

  // Instance logger for post-context operations (requires MDC setup)
  IRepositoryImportLogger logger = new Log4JRepositoryImportLogger();


  public SolutionImportHandler( List<IMimeType> mimeTypes ) {
    this.mimeTypes = mimeTypes;
    this.solutionHelper = new SolutionFileImportHelper();
    repository = PentahoSystem.get( IUnifiedRepository.class );
    initializeBuiltInHelpers();
  }

  /**
   * Initialize built-in import helpers for core content types.
   * These handle users, datasources, metadata, mondrian, metastore, and repository files.
   * Plugins can add additional helpers via addImportHelper().
   */
  private void initializeBuiltInHelpers() {
    // Register built-in content type helpers
    addImportHelper( new UsersAndRolesImportHelper() );
    addImportHelper( new MetadataImportHelper() );
    addImportHelper( new MondrianImportHelper() );
    addImportHelper( new MetastoreImportHelper() );
    addImportHelper( new JdbcDatasourceImportHelper() );
    addImportHelper( new RepositoryFilesImportHelper() );
  }

  public ImportSession getImportSession() {
    return ImportSession.getSession();
  }

  public Log getLogger() {
    return getImportSession().getLogger();
  }

  public void addImportHelper( IImportHelper helper ) {
    importHelpers.add( helper );
  }

  /**
   * Get the cached imports map (for helpers that need to access cached bundles)
   */
  public Map<String, RepositoryFileImportBundle.Builder> getCachedImports() {
    return cachedImports;
  }

  /**
   * Get the files list (for helpers that need to iterate over extracted files)
   */
  public List<IRepositoryFileBundle> getFiles() {
    return files;
  }

  /**
   * Get the metrics collector (for helpers that need to record import metrics)
   */
  public ImportExportMetrics getMetrics() {
    return metrics;
  }

  /**
   * Get the file from the repository using the bundle info
   */
  public RepositoryFile getFile( IPlatformImportBundle importBundle, IRepositoryFileBundle fileBundle ) {
    String repositoryFilePath =
        repositoryPathConcat( importBundle.getPath(), fileBundle.getPath(), fileBundle.getFile().getName() );
    return repository.getFile( repositoryFilePath );
  }

  public void runImportHelpers() {
    int successfulHelpers = 0;
    int totalHelpers = importHelpers.size();
    Object componentOverrides = getImportSession().getComponentOverrides();

    for ( IImportHelper helper : importHelpers ) {
      try {
        // Check if helper should execute for this restore profile
        if ( !helper.shouldExecute( componentOverrides ) ) {
          if ( isPerformingRestore ) {
            STATIC_LOGGER.debug( "Skipping import helper: " + helper.getName() +
              " (not applicable for current restore profile)" );
          }
          continue;
        }

        // Use static SLF4J logger - not Log4JRepositoryImportLogger
        // because job context may not be initialized yet
        STATIC_LOGGER.info( "Running import helper: " + helper.getName() );
        helper.doImport( this );
        successfulHelpers++;
        STATIC_LOGGER.info( "Successfully completed import helper: " + helper.getName() );
      } catch ( Exception e ) {
        STATIC_LOGGER.error( "Import helper " + helper.getName() + " failed: " + e.getMessage(), e );
        // Record failure but continue with next helper
      }
    }

    if ( isPerformingRestore ) {
      STATIC_LOGGER.debug( "Import helpers completed: " + successfulHelpers + "/" + totalHelpers + " successful" );
    }
  }

  @Override
  public void importFile( IPlatformImportBundle bundle ) throws PlatformImportException, DomainIdNullException,
      DomainAlreadyExistsException, DomainStorageException, IOException {
    IPlatformImporter platformImporter = PentahoSystem.get( IPlatformImporter.class );
    files = new ArrayList<>();
    if ( isPerformingRestore() ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_PROCESS" ) );
    }

    // Initialize metrics collector
    metrics = new ImportExportMetrics( ImportExportMetrics.OperationType.RESTORE );

    if ( isPerformingRestore()  ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_PROCESS" ) );
    }
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;

    // Processing file
    if ( isPerformingRestore()  ) {
      getLogger().debug( " Start:  pre processing files and folder from the bundle" );
    }
    if ( !processZip( bundle.getInputStream() ) ) {
      // Something went wrong, do not proceed!
      if ( isPerformingRestore()  ) {
        getLogger().error( "Failed to process ZIP file during restore" );
      }
      return;
    }
    if ( isPerformingRestore()  ) {
      getLogger().debug( " End:  pre processing files and folder from the bundle" );
    }
    setOverwriteFile( bundle.overwriteInRepository() );
    cachedImports = new HashMap<>();

    // Initialize helper settings before running helpers
    ExportManifest manifest = getImportSession().getManifest();
    ComponentConfig componentOverrides = getImportSession().getComponentOverrides();

    if ( isPerformingRestore()  && componentOverrides != null ) {
      getLogger().debug( "Selective restore active with component overrides: Users=" + componentOverrides.isIncludeUsers() +
        ", Content=" + componentOverrides.isIncludeContent() + ", Datasources=" + componentOverrides.isIncludeDatasources() );
    }

    // Configure repository files helper with bundle reference
    for ( IImportHelper helper : importHelpers ) {
      if ( helper instanceof RepositoryFilesImportHelper ) {
        ( (RepositoryFilesImportHelper) helper ).setBundle( bundle );
      }
      if ( helper instanceof MetadataImportHelper ) {
        ( (MetadataImportHelper) helper ).setPreserveDsw( bundle.isPreserveDsw() );
      }
      if ( helper instanceof MetastoreImportHelper ) {
        ( (MetastoreImportHelper) helper ).setOverwriteFile( bundle.overwriteInRepository() );
      }
    }

    // Run all import helpers (core + plugins)
    // Each helper handles its own profile filtering via shouldExecute()
    if ( !importHelpers.isEmpty() ) {
      try {
        runImportHelpers();
      } catch ( Exception e ) {
        if ( isPerformingRestore()  ) {
          getLogger().error( "Failed to run import helpers: " + e.getMessage() );
          getLogger().debug( "Import helpers error", e );
        }
      }
    }

    // Output metrics report
    if ( isPerformingRestore()  && metrics != null ) {
      getLogger().info( metrics.generateDetailedReport() );
    }
  }

  /**
   * Normalize a repository path for consistent comparison:
   * - Ensures leading forward slash
   * - Converts backslashes to forward slashes
   * - Decodes URL-encoded characters (e.g., %28 → (, %29 → ), %20 → space)
   * - Normalizes space encoding (both spaces and + are treated equivalently)
   * - Handles URL encoding inconsistencies
   *
   * @param path the path to normalize
   * @return normalized path
   */
  public String normalizePath( String path ) {
    if ( path == null ) {
      return "";
    }

    String normalized = path;

    // 1. CRITICAL: Convert + to space FIRST (before handling %2B)
    // This handles form URL encoding where + = space
    // MUST be done before %2B conversion to avoid corrupting actual plus characters
    normalized = normalized.replace( "+", " " );

    // 2. URL-decode common characters that might be encoded
    // Handle parentheses: %28 = (, %29 = )
    normalized = normalized.replace( "%28", "(" );
    normalized = normalized.replace( "%29", ")" );
    // Handle spaces: %20 = space
    normalized = normalized.replace( "%20", " " );
    // Handle other common encoded chars
    normalized = normalized.replace( "%5B", "[" );  // [
    normalized = normalized.replace( "%5D", "]" );  // ]
    normalized = normalized.replace( "%26", "&" );  // &
    normalized = normalized.replace( "%2B", "+" );  // + (actual plus character)

    // 3. Convert backslashes to forward slashes
    normalized = normalized.replace( File.separator, RepositoryFile.SEPARATOR );
    normalized = normalized.replace( "\\", RepositoryFile.SEPARATOR );

    // 4. Ensure leading forward slash
    if ( !normalized.startsWith( RepositoryFile.SEPARATOR ) ) {
      normalized = RepositoryFile.SEPARATOR + normalized;
    }

    // 5. Normalize multiple spaces to single space
    normalized = normalized.replaceAll( "\\s+", " " );
    return normalized;
  }

  /**
   * Import a schedule dependency file from the backup bundle.
   * Uses BACKWARDS COMPATIBLE matching: first tries with URL decoding (new exports),
   * then tries without decoding (old exports with literal spaces).
   * This ensures both old and new backup formats are supported.
   *
   * @param filePath the repository path of the file to import (e.g., "/Reports/SalesReport.prpt")
   * @return true if successfully imported, false if not found or failed
   */
  public boolean importFileFromBundle( String filePath ) {
    if ( filePath == null || filePath.trim().isEmpty() ) {
      return false;
    }

    if ( isPerformingRestore ) {
      getLogger().debug( "Attempting to import file from bundle: [ " + filePath + " ]" );
    }

    try {
      String normalizedSearchPath = normalizePath( filePath );

      // BACKWARDS COMPATIBLE APPROACH: Try multiple matching strategies
      // Strategy 1: Try matching with URL decoding (new exports with + and %XX)
      IRepositoryFileBundle foundBundle = findFileInBundle( normalizedSearchPath, true );
      if ( foundBundle != null ) {
        return importFileFromFoundBundle( foundBundle, normalizedSearchPath );
      }

      // Strategy 2: Try matching WITHOUT URL decoding (old exports with literal spaces)
      // This handles old backups that may have been created before URL encoding was used
      foundBundle = findFileInBundle( normalizedSearchPath, false );
      if ( foundBundle != null ) {
        return importFileFromFoundBundle( foundBundle, normalizedSearchPath );
      }

      // File not found in bundle using any strategy
      if ( isPerformingRestore ) {
        getLogger().warn( "File not found in bundle: [ " + filePath + " ] using any matching strategy" );
      }
      return false;

    } catch ( Exception e ) {
      getLogger().error( "Error importing file from bundle [ " + filePath + " ]: " + e.getMessage() );
      return false;
    }
  }

  /**
   * Search for a file in the bundle list using specified matching strategy.
   *
   * @param normalizedSearchPath the normalized search path
   * @param shouldDecode whether to URL-decode ZIP entry names
   * @return the matching IRepositoryFileBundle, or null if not found
   */
  private IRepositoryFileBundle findFileInBundle( String normalizedSearchPath, boolean shouldDecode ) {
    for ( IRepositoryFileBundle fileBundle : files ) {
      String fileName = fileBundle.getFile().getName();
      String actualFilePath = fileBundle.getPath();

      // Apply decoding strategy if requested
      if ( shouldDecode ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }

      String repositoryFilePath = RepositoryFilenameUtils.concat(
          PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      // Normalize the bundle path for comparison
      String normalizedBundlePath = normalizePath( repositoryFilePath );

      if ( isPerformingRestore ) {
        getLogger().debug( "Strategy[decode=" + shouldDecode + "]: Comparing search [ " + normalizedSearchPath
            + " ] with bundle [ " + normalizedBundlePath + " ]" );
      }

      if ( normalizedSearchPath.equalsIgnoreCase( normalizedBundlePath ) ) {
        if ( isPerformingRestore ) {
          getLogger().debug( "Found file in bundle using strategy[decode=" + shouldDecode + "]: [ "
              + repositoryFilePath + " ]" );
        }
        return fileBundle;
      }
    }
    return null;
  }

  /**
   * Import a found file bundle from the ZIP archive.
   *
   * @param fileBundle the file bundle to import
   * @param normalizedSearchPath the normalized search path (for logging)
   * @return true if successfully imported, false otherwise
   */
  private boolean importFileFromFoundBundle( IRepositoryFileBundle fileBundle, String normalizedSearchPath ) {
    try {
      RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder();
      bundleBuilder.input( fileBundle.getInputStream() );
      bundleBuilder.file( fileBundle.getFile() );
      Assert.notNull( fileBundle.getFile(), "File object should not be null" );
      bundleBuilder.name( fileBundle.getFile().getName() );
      bundleBuilder.path( fileBundle.getPath() );
      bundleBuilder.overwriteFile( overwriteFile );
      bundleBuilder.retainOwnership( true );
      bundleBuilder.charSet( fileBundle.getCharset() );
      IPlatformMimeResolver mimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );
      String mimeTypeFromFile = mimeResolver.resolveMimeForFileName( fileBundle.getFile().getName() );
      bundleBuilder.mime( mimeTypeFromFile );
      IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
      if ( importer != null ) {
        importer.importFile( build( bundleBuilder ) );
        if ( isPerformingRestore ) {
          getLogger().debug( "Successfully imported file from bundle: [ " + normalizedSearchPath + " ]" );
        }
        return true;
      }
    } catch ( Exception e ) {
      getLogger().error( "Failed to import file from bundle [ " + normalizedSearchPath + " ]: " + e.getMessage() );
      return false;
    }
    return false;
  }
  /**
   * Import global user settings into the platform.
   * Delegates to UsersAndRolesImportHelper
   */
  protected void importGlobalUserSettings( List<ExportManifestUserSetting> globalSettings ) {
    // Find and delegate to the UsersAndRolesImportHelper
    for ( IImportHelper helper : importHelpers ) {
      if ( helper instanceof UsersAndRolesImportHelper ) {
        try {
          ( (UsersAndRolesImportHelper) helper ).importGlobalUserSettings( globalSettings, this );
          return;
        } catch ( Exception e ) {
          getLogger().error( "Failed to import global user settings via helper: " + e.getMessage() );
          throw new RuntimeException( "Global user settings import failed", e );
        }
      }
    }
    getLogger().warn( "UsersAndRolesImportHelper not found - cannot import global user settings" );
  }

  /**
   * See BISERVER-13481 . For backward compatibility we must check if there are any schedules
   * which refers to this file. If yes make this file schedulable
   */
  @VisibleForTesting
  public boolean fileIsScheduleInputSource( ExportManifest manifest, String sourcePath ) {
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
  public boolean isFileHidden( RepositoryFile file, ManifestFile manifestFile, String sourcePath ) {
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
  public boolean isSchedulable( RepositoryFile file, ManifestFile manifestFile ) {
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
    if ( isPerformingRestore() ) {
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
            // TODO Find out what does this line do importState.setPartialImport( true );
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
          if ( isPerformingRestore() ) {
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
    if ( isPerformingRestore() ) {
      getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_REPOSITORY_OBJECT" ) );
    }
    return true;
  }

  private void initializeAclManifest( IRepositoryFileBundle file ) {
    try {
      byte[] bytes = IOUtils.toByteArray( file.getInputStream() );
      ByteArrayInputStream in = new ByteArrayInputStream( bytes );
      ExportManifest manifest = ExportManifest.fromXml( in );
      getImportSession().setManifest( manifest );
    } catch ( Exception e ) {
      getLogger().error( "Failed to parse export manifest from backup file", e );
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

  public boolean isOverwriteFile() {
    return overwriteFile;
  }

  public void setOverwriteFile( boolean overwriteFile ) {
    this.overwriteFile = overwriteFile;
  }

  public boolean isPerformingRestore() {
    return isPerformingRestore;
  }

  public void setPerformingRestore( boolean value ) {
    this.isPerformingRestore = value;
  }

  /**
   * Get the UsersAndRolesImportHelper from the list of import helpers.
   * @return UsersAndRolesImportHelper or null if not found
   */
  @VisibleForTesting
  protected UsersAndRolesImportHelper getUsersAndRolesImportHelper() {
    for ( IImportHelper helper : importHelpers ) {
      if ( helper instanceof UsersAndRolesImportHelper ) {
        return (UsersAndRolesImportHelper) helper;
      }
    }
    return null;
  }

  /**
   * Get the MetastoreImportHelper from the list of import helpers.
   * @return MetastoreImportHelper or null if not found
   */
  @VisibleForTesting
  protected MetastoreImportHelper getMetastoreImportHelper() {
    for ( IImportHelper helper : importHelpers ) {
      if ( helper instanceof MetastoreImportHelper ) {
        return (MetastoreImportHelper) helper;
      }
    }
    return null;
  }

  /**
   * Delegation method for backward compatibility with tests.
   * Delegates to UsersAndRolesImportHelper.importUsers()
   */
  @VisibleForTesting
  public Map<String, List<String>> importUsers( List<UserExport> users ) throws Exception {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      return helper.importUsers( users, this );
    }
    return new HashMap<>();
  }

  /**
   * Delegation method for backward compatibility with tests.
   * Delegates to UsersAndRolesImportHelper.importRoles()
   */
  @VisibleForTesting
  public void importRoles( List<RoleExport> roles, Map<String, List<String>> roleToUserMap ) throws Exception {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      helper.importRoles( roles, roleToUserMap, this );
    }
  }

  /**
   * Delegation method for backward compatibility with tests.
   * Delegates to MetastoreImportHelper.importMetaStore()
   */
  @VisibleForTesting
  public void importMetaStore( ExportManifestMetaStore metaStore, boolean provideReplaceContentOption ) throws Exception {
    MetastoreImportHelper helper = getMetastoreImportHelper();
    if ( helper != null ) {
      helper.setOverwriteFile( provideReplaceContentOption );
      helper.importMetaStore( metaStore );
    }
  }

  /**
   * Delegation method for backward compatibility with tests.
   * Delegates to UsersAndRolesImportHelper.importUserSettings()
   */
  @VisibleForTesting
  public void importUserSettings( UserExport user ) throws Exception {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      helper.importUserSettings( user, this );
    }
  }

  /**
   * Delegation method for backward compatibility with tests.
   * Delegates to UsersAndRolesImportHelper.importUserAndRoleWithTracking()
   */
  @VisibleForTesting
  public void importUserAndRoleWithTracking( String tenantPath, UserExport user, Map<String, List<String>> roleToUserMap ) throws Exception {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      helper.importUserAndRoleWithTracking( tenantPath, user, roleToUserMap, this );
    }
  }

  /**
   * Import a schedule owner user by delegating to UsersAndRolesImportHelper.
   * This ensures the user, home folder, and roles are properly created before schedule import.
   *
   * @param username the username of the schedule owner to import
   * @param manifest the export manifest containing user and role information
   * @return true if user was imported successfully or already exists, false if user not found or import failed
   */
  public boolean importUserAndRole( String username, ExportManifest manifest ) {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      return helper.importUserAndRole( username, manifest, this );
    } else {
      getLogger().warn( "UsersAndRolesImportHelper not available - cannot import schedule owner user [ " + username + " ]" );
      return false;
    }
  }

  /**
   * Import a schedule owner user by delegating to UsersAndRolesImportHelper.
   * <p>
   * For the internal (jackrabbit) provider this creates the user, home folder and roles. For an
   * external authentication provider (jdbc/ldap) the user and role objects are managed externally,
   * so only the user's home folder is ensured and the runtime-to-logical role mappings from the
   * manifest are applied.
   *
   * @param username the username of the schedule owner to import
   * @param manifest the export manifest containing user and role information
   * @return true if the owner was handled successfully or already exists, false otherwise
   */
  public boolean importScheduleOwnerUser( String username, ExportManifest manifest ) {
    UsersAndRolesImportHelper helper = getUsersAndRolesImportHelper();
    if ( helper != null ) {
      return helper.importScheduleOwnerUser( username, manifest, this );
    } else {
      getLogger().warn( "UsersAndRolesImportHelper not available - cannot import schedule owner user [ " + username + " ]" );
      return false;
    }
  }
}
