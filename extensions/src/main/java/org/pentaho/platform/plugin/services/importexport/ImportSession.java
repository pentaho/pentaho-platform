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


package org.pentaho.platform.plugin.services.importexport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestEntity;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;

/**
 * General purpose objects whose lifecycle is that of an formal import session
 *
 * @author TKafalas
 */
public class ImportSession {

  private static final ThreadLocal<ImportSession> sessions = new ThreadLocal<ImportSession>();

  private HashSet<String> skippedFiles = new HashSet<String>(); // Files skipped due to overwriteFlag = false
  private final HashSet<String> foldersCreatedImplicitly = new HashSet<String>(); // Folders created implicitly

  private Log log;
  private Boolean isNotRunningImport = true;

  private ExportManifest manifest;
  private boolean applyAclSettings;
  private boolean retainOwnership;
  private boolean overwriteAclSettings;
  private String currentManifestKey;
  private ComponentConfig componentOverrides; // Component selection overrides for selective restore

  private final HashSet<RepositoryFile> importedRepositoryFiles = new HashSet<RepositoryFile>();
  private final List<String> importedScheduleJobIds = new ArrayList<String>();
  public static IPlatformImporter iPlatformImporter; //This variable allows injection on unit tests

  public static ImportSession getSession() {
    ImportSession session = sessions.get();
    if ( session == null ) {
      session = new ImportSession();
      sessions.set( session );
    }
    return session;
  }

  /**
   * Clear out any collections, most likely before starting a new import
   */
  public void initialize() {
    importedRepositoryFiles.clear();
    importedScheduleJobIds.clear();
    skippedFiles.clear();
    foldersCreatedImplicitly.clear();
    componentOverrides = null; // Clear component overrides
  }

  public Log getLogger() {
    if ( log == null ) {
      if ( iPlatformImporter == null ) {
        iPlatformImporter = PentahoSystem.get( IPlatformImporter.class );
      }
      IRepositoryImportLogger logger = iPlatformImporter.getRepositoryImportLogger();
      if ( logger != null && logger.hasLogger() ) {
        // An import is running from the /repo/file/import endpoint
        log = logger;
        isNotRunningImport = false;
      } else {
        // A publish is running from some other endpoint
        log = LogFactory.getLog( RepositoryFileImportFileHandler.class );
        isNotRunningImport = true;
      }
    }
    return log;
  }

  /**
   * Check if the file is hidden by manifest definition and import flags. Returns null if the manifest is not present or
   * the manifest is being ignored.
   *
   * @param isApplyAclSettings
   * @param isRetainOwnership
   * @param filePath
   * @return
   */
  @Deprecated
  public Boolean isFileHidden( String filePath ) {
    return getManifestFile( filePath, true ).isFileHidden();
  }

  public ManifestFile getManifestFile( String filePath, boolean isFileExist ) {
    return new ManifestFile( filePath, isFileExist );
  }


  public class ManifestFile {

    private RepositoryFile rf;

    private ManifestFile( String filePath, boolean isFileExist ) {
      if ( ( !isFileExist || ( applyAclSettings || !retainOwnership ) ) && manifest != null ) {
        ExportManifestEntity entity = manifest.getExportManifestEntity( filePath );
        if ( entity != null ) {
          rf = entity.getRepositoryFile();
        }
      }
    }

    public Boolean isFileHidden() {
      return rf == null ? null : rf.isHidden();
    }

    public Boolean isFileSchedulable() {
      return rf == null ? null : rf.isSchedulable();
    }
  }

  public RepositoryFileAcl processAclForFile( String filePath ) {
    // If we are not overwriting ACL's or owners then return null.
    // If we are writing ACL's we'll have to check later in RepositoryFileImportHandler whether to overwrite
    // based on the isOverwriteAcl setting and whether we are creating or updating the RepositoryFile.
    RepositoryFileAcl acl = null;
    
    getLogger().trace( "=== processAclForFile DEBUG ===" );
    getLogger().trace( "  filePath (decoded): " + filePath );
    
    // CRITICAL FIX: The manifest stores paths with URL encoding ONLY for the filename part
    // e.g., "home/mike/Buyer+Report+%28sparkline+report%292026-06-02.html" 
    // We need to try multiple encoding strategies for backward compatibility
    
    List<String> pathsToTry = new ArrayList<>();
    pathsToTry.add( filePath );  // First try: decoded (legacy format)
    
    // Second try: encode using the proper path encoder that preserves separators
    try {
      String encodedPath = ExportFileNameEncoder.encodeZipPathName( filePath );
      if ( !pathsToTry.contains( encodedPath ) ) {
        pathsToTry.add( encodedPath );
        getLogger().trace( "  encodeZipPathName result: " + encodedPath );
      }
    } catch ( Exception e ) {
      getLogger().debug( "Failed with encodeZipPathName: " + e.getMessage() );
    }
    
    // Third try: encode only the filename part manually (in case path has mixed separators)
    try {
      int lastSlash = filePath.lastIndexOf( '/' );
      if ( lastSlash >= 0 ) {
        String pathPart = filePath.substring( 0, lastSlash + 1 );  // e.g., "home/mike/"
        String namePart = filePath.substring( lastSlash + 1 );      // e.g., "Buyer Report..."
        String encodedName = ExportFileNameEncoder.encodeZipFileName( namePart );
        String result = pathPart + encodedName;
        if ( !pathsToTry.contains( result ) ) {
          pathsToTry.add( result );
          getLogger().trace( "  Selective encoding result: " + result );
        }
      }
    } catch ( Exception e ) {
      getLogger().debug( "Failed with selective encoding: " + e.getMessage() );
    }
    
    getLogger().trace( "  Paths to try (in order): " + pathsToTry );
    getLogger().trace( "  applyAclSettings: " + applyAclSettings );
    getLogger().trace( "  retainOwnership: " + retainOwnership );
    getLogger().trace( "  Condition (applyAclSettings || !retainOwnership): " + ( applyAclSettings || !retainOwnership ) );
    getLogger().trace( "  manifest is null: " + (manifest == null) );
    
    if ( applyAclSettings || !retainOwnership ) {
      getLogger().trace( "  -> CONDITION PASSED, attempting to fetch ACL from manifest" );
      try {
        if ( manifest != null ) {
          // Try all path formats until one matches
          for ( String pathToTry : pathsToTry ) {
            getLogger().trace( "  -> Trying path: '" + pathToTry + "'" );
            ExportManifestEntity entity = manifest.getExportManifestEntity( pathToTry );
            if ( entity != null ) {
              acl = entity.getRepositoryFileAcl();
              getLogger().trace( "  -> FOUND! Using: " + pathToTry + ". ACL is " + (acl != null ? "NOT null (has permissions)" : "null (no permissions)") );
              if ( acl != null ) {
                getLogger().trace( "       ACL owner: " + acl.getOwner() );
                getLogger().trace( "       ACL aces count: " + (acl.getAces() != null ? acl.getAces().size() : 0) );
              }
              break;  // Found it, stop trying
            }
          }
          
          if ( acl == null ) {
            getLogger().trace( "  -> Manifest entity NOT FOUND for any format: " + pathsToTry );
            getLogger().trace( "  -> Note: File entities may not be indexed in manifest HashMap due to namespace issues" );
            getLogger().trace( "  -> ACL will be null, using default permissions" );
          }
        } else {
          getLogger().trace( "  -> Manifest is null, cannot fetch ACL" );
        }
      } catch ( Exception e ) {
        getLogger().trace( "  -> Exception when fetching ACL: " + e.getMessage() );
        getLogger().trace( e );
      }
    } else {
      getLogger().trace( "  -> CONDITION FAILED (applyAclSettings=false AND retainOwnership=true), returning null WITHOUT checking manifest" );
    }
    
    getLogger().trace( "  -> RETURNING: " + (acl != null ? "ACL object" : "NULL") );
    getLogger().trace( "=== END processAclForFile DEBUG ===" );
    
    return acl;
  }


  public RepositoryFileExtraMetaData processExtraMetaDataForFile( String filePath ) {
    RepositoryFileExtraMetaData extraMetaData = null;
    try {
      if ( manifest != null ) {
        ExportManifestEntity entity = manifest.getExportManifestEntity( filePath );
        if ( entity != null ) {
          extraMetaData = entity.getRepositoryFileExtraMetaData();
        }
      }
    } catch ( Exception e ) {
      getLogger().trace( e );
    }

    return extraMetaData;
  }
  /**
   * @return the skippedFiles
   */
  public HashSet<String> getSkippedFiles() {
    return skippedFiles;
  }

  /**
   * @param skippedFiles the skippedFiles to set
   */
  public void setSkippedFiles( HashSet<String> skippedFiles ) {
    this.skippedFiles = skippedFiles;
  }

  /**
   * @return the isNotRunningImport
   */
  public Boolean getIsNotRunningImport() {
    return isNotRunningImport;
  }

  /**
   * @return the manifest
   */
  public ExportManifest getManifest() {
    return manifest;
  }

  /**
   * @param manifest the manifest to set
   */
  public void setManifest( ExportManifest manifest ) {
    this.manifest = manifest;
  }

  /**
   * @return the applyAclSettings
   */
  public boolean isApplyAclSettings() {
    return applyAclSettings;
  }

  /**
   * @param applyAclSettings the applyAclSettings to set
   */
  public void setApplyAclSettings( boolean applyAclSettings ) {
    this.applyAclSettings = applyAclSettings;
  }

  /**
   * @return the retainOwnership
   */
  public boolean isRetainOwnership() {
    return retainOwnership;
  }

  /**
   * @param retainOwnership the retainOwnership to set
   */
  public void setRetainOwnership( boolean retainOwnership ) {
    this.retainOwnership = retainOwnership;
  }

  /**
   * @return the overwriteAclSettings
   */
  public boolean isOverwriteAclSettings() {
    return overwriteAclSettings;
  }

  /**
   * @param overwriteAclSettings the overwriteAclSettings to set
   */
  public void setOverwriteAclSettings( boolean overwriteAclSettings ) {
    this.overwriteAclSettings = overwriteAclSettings;
  }

  /**
   * @return The key to finding the current file the export manifest.
   */
  public String getCurrentManifestKey() {
    return currentManifestKey;
  }

  /**
   * @param Set the key for looking up the current file in the manifest
   */
  public void setCurrentManifestKey( String currentManifestKey ) {
    this.currentManifestKey = currentManifestKey;
  }

  /**
   * @return A set of folders (complete path) that were created on the fly due to files that had a path that included
   * these folders. The list is kept so that if the folder entry, itself, is encountered later on in the zip file, the
   * importer will know that this file was already processed as a new file, and not process it as a pre-existing file.
   */
  public HashSet<String> getFoldersCreatedImplicitly() {
    return foldersCreatedImplicitly;
  }

  /**
   * Sets the AclProperties.
   *
   * @param applyAclSettingsFlag
   * @param retainOwnershipFlag
   * @param overwriteAclSettingsFlag
   */
  public void setAclProperties( boolean applyAclSettingsFlag, boolean retainOwnershipFlag,
                                boolean overwriteAclSettingsFlag ) {
    setApplyAclSettings( applyAclSettingsFlag );
    setRetainOwnership( retainOwnershipFlag );
    setOverwriteAclSettings( overwriteAclSettingsFlag );
  }

  /**
   * @param repositoryFile
   */
  public void addImportedRepositoryFile( RepositoryFile repositoryFile ) {
    importedRepositoryFiles.add( repositoryFile );
  }

  /**
   * @return
   */
  public HashSet<RepositoryFile> getImportedRepositoryFiles() {
    return importedRepositoryFiles;
  }

  /**
   * @param jobId
   */
  public void addImportedScheduleJobId( String jobId ) {
    importedScheduleJobIds.add( jobId );
  }

  /**
   * @return
   */
  public List<String> getImportedScheduleJobIds() {
    return importedScheduleJobIds;
  }

  /**
   * Set component overrides for selective restore
   * @param componentOverrides ComponentConfig specifying which components to restore
   */
  public void setComponentOverrides( ComponentConfig componentOverrides ) {
    this.componentOverrides = componentOverrides;
  }

  /**
   * Get component overrides for selective restore
   * @return ComponentConfig if selective restore, null for full restore
   */
  public ComponentConfig getComponentOverrides() {
    return componentOverrides;
  }

  /**
   * Removes the current thread's value for this thread-local variable
   * */
  public static void clearSession() {
    ImportSession session = sessions.get();
    if ( session != null ) {
      sessions.remove();
    }
  }
}
