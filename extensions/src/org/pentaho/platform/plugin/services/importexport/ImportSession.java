/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.importexport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportFileHandler;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifestEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * General purpose objects whose lifecycle is that of an formal import session
 *
 * @author TKafalas
 */
public class ImportSession {

  private HashSet<String> skippedFiles = new HashSet<String>(); // Files skipped due to overwriteFlag = false
  private HashSet<String> foldersCreatedImplicitly = new HashSet<String>(); // Folders created implicitly
  private Log log;
  private Boolean isNotRunningImport = true;
  private ExportManifest manifest;
  private boolean applyAclSettings;
  private boolean retainOwnership;
  private boolean overwriteAclSettings;
  private String currentManifestKey;
  private HashSet<RepositoryFile> importedRepositoryFiles = new HashSet<RepositoryFile>();
  private List<String> importedScheduleJobIds = new ArrayList<String>();

  private static ThreadLocal<ImportSession> sessions = new ThreadLocal<ImportSession>();

  public static ImportSession getSession() {
    if ( sessions.get() != null ) {
      return sessions.get();
    }
    ImportSession session = new ImportSession();
    sessions.set( session );
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
  }

  public Log getLogger() {
    if ( log == null ) {
      IRepositoryImportLogger logger = PentahoSystem.get( IPlatformImporter.class ).getRepositoryImportLogger();
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
  public Boolean isFileHidden( String filePath ) {
    if ( ( applyAclSettings || retainOwnership ) && manifest != null
      && manifest.getExportManifestEntity( filePath ) != null ) {
      return manifest.getExportManifestEntity( filePath ).getRepositoryFile().isHidden();
    }
    return null;
  }

  public RepositoryFileAcl processAclForFile( String filePath ) {
    // If we are not overwriting ACL's or owners then return null.
    // If we are writing ACL's we'll have to check later in RepositoryFileImportHandler whether to overwrite
    // based on the isOverwriteAcl setting and whether we are creating or updating the RepositoryFile.
    RepositoryFileAcl acl = null;
    if ( applyAclSettings || retainOwnership ) {
      try {
        if ( manifest != null ) {
          ExportManifestEntity entity = manifest.getExportManifestEntity( filePath );
          if ( entity != null ) {
            acl = entity.getRepositoryFileAcl();
          }
        }
      } catch ( Exception e ) {
        getLogger().trace( e );
      }
    }
    return acl;
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
   *         these folders. The list is kept so that if the folder entry, itself, is encountered later on in the zip
   *         file, the importer will know that this file was already processed as a new file, and not process it as a
   *         pre-existing file.
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
}
