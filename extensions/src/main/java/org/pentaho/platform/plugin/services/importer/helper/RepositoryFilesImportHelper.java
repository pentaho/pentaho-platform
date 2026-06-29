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
 */

package org.pentaho.platform.plugin.services.importer.helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.io.Serializable;
import org.pentaho.platform.api.repository2.unified.RepositoryFileExtraMetaData;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ExportFileNameEncoder;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.LocaleFilesProcessor;
import org.pentaho.platform.plugin.services.importer.PentahoPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.ImportSession.ManifestFile;
import org.pentaho.platform.plugin.services.importexport.ImportSource.IRepositoryFileBundle;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;

/**
 * Import helper for repository content (files and folders) restoration.
 * Handles importing repository files, folders, and directory structures from backup.
 *
 * Profile: CONTENT
 * Filters: isIncludeContent() OR has dependencies (for schedule file dependencies)
 */
public class RepositoryFilesImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;
  private IPlatformImportBundle bundle;


  @Override
  public String getName() {
    return "Repository Files and Folders Import Helper";
  }

  public boolean shouldExecute( Object config ) {
    if ( config == null ) {
      return true; // Full restore - include all content
    }
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeContent();
    }
    return true; // Unknown type - default to include
  }

  @Override
  public void doImport( Object importArg ) throws ImportException {
    solutionImportHandler = (SolutionImportHandler) importArg;
    if ( !shouldExecute( solutionImportHandler.getImportSession().getComponentOverrides() ) ) {
      return;
    }
    try {
      ExportManifest manifest = solutionImportHandler.getImportSession().getManifest();

      if ( manifest == null ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping repository files import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting repository files and folders import..." );
      }

      try {
        importRepositoryFilesAndFolders( manifest, bundle, solutionImportHandler );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed repository files import" );
        }
      } catch ( IOException e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import repository files and folders: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Repository files import error", e );
        }
        throw new ImportException( "Failed to import repository files and folders: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "Repository files import helper error: " + e.getMessage() );
      }
      throw new ImportException( "Repository files import helper failed: " + e.getMessage(), e );
    }
  }

  protected void importRepositoryFilesAndFolders( ExportManifest manifest, IPlatformImportBundle bundle, SolutionImportHandler solutionImportHandler ) throws IOException {
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_FILEFOLDER" ) );
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_FILEFOLDER", solutionImportHandler.getFiles().size() ) );
    }
    int successfulFilesImportCount = 0;
    int skippedGeneratedContentCount = 0;
    String manifestVersion = null;
    if ( manifest != null ) {
      manifestVersion = manifest.getManifestInformation().getManifestVersion();
    }
    RepositoryFileImportBundle importBundle = (RepositoryFileImportBundle) bundle;

    IPlatformMimeResolver mimeResolver = PentahoSystem.get( IPlatformMimeResolver.class );
    LocaleFilesProcessor localeFilesProcessor = new LocaleFilesProcessor();
    IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );

    for ( IRepositoryFileBundle fileBundle : solutionImportHandler.getFiles() ) {
      String fileName = fileBundle.getFile().getName();
      String actualFilePath = fileBundle.getPath();
      if ( manifestVersion != null ) {
        fileName = ExportFileNameEncoder.decodeZipFileName( fileName );
        actualFilePath = ExportFileNameEncoder.decodeZipFileName( actualFilePath );
      }
      String repositoryFilePath =
        RepositoryFilenameUtils.concat( PentahoPlatformImporter.computeBundlePath( actualFilePath ), fileName );

      Map<String, RepositoryFileImportBundle.Builder> cachedImports = solutionImportHandler.getCachedImports();
      
      // Try to find in cache - check both original and normalized paths
      RepositoryFileImportBundle.Builder cachedBuilder = null;
      
      if ( cachedImports.containsKey( repositoryFilePath ) ) {
        solutionImportHandler.getLogger().debug( "[RepositoryFilesImportHelper] CACHE HIT (full path): " + repositoryFilePath );
        cachedBuilder = cachedImports.get( repositoryFilePath );
      } else {
        // Try just the filename - metadata/mondrian helpers cache by filename only
        if ( cachedImports.containsKey( fileName ) ) {
          solutionImportHandler.getLogger().debug( "[RepositoryFilesImportHelper] CACHE HIT (filename): " + fileName );
          cachedBuilder = cachedImports.get( fileName );
        } else if ( fileName.endsWith( ".xmi" ) || fileName.endsWith( ".mondrian.xml" ) ) {
          solutionImportHandler.getLogger().warn( "[RepositoryFilesImportHelper] CACHE MISS for " + fileName + " - available keys: " + cachedImports.keySet() );
        }
      }
      if ( cachedBuilder != null ) {
        solutionImportHandler.getLogger().debug( "Repository object with path [ " + repositoryFilePath + " ] found in the cache" );
        byte[] bytes = IOUtils.toByteArray( fileBundle.getInputStream() );
        cachedBuilder.input( new ByteArrayInputStream( bytes ) );

        try {
          importer.importFile( solutionImportHandler.build( cachedBuilder ) );
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ] from the cache" );
          }
          successfulFilesImportCount++;
          solutionImportHandler.getMetrics().recordSuccess( ImportExportMetrics.Category.FILES );
          continue;
        } catch ( PlatformImportException e ) {
          solutionImportHandler.getMetrics().recordFailure( ImportExportMetrics.Category.FILES, repositoryFilePath, e );
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", repositoryFilePath, e.getLocalizedMessage() ) );
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
          solutionImportHandler.getLogger().trace( Messages.getInstance()
            .getString( "SolutionImportHandler.SkipLocaleFile", repositoryFilePath ) );
          continue;
        }
bundleBuilder.input( bundleInputStream );
        bundleBuilder.mime( mimeResolver.resolveMimeForFileName( fileName ));

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

      // Check if we should skip this file based on generated content filtering
      // Priority 1: Check manifest attribute (new feature - most reliable)
      // Priority 2: Check file metadata for backward compatibility with old manifests
      ComponentConfig componentOverrides = solutionImportHandler.getImportSession().getComponentOverrides();
      if ( componentOverrides != null && !componentOverrides.isIncludeGeneratedContent() ) {
        boolean isFileAGC = false;
        String gcSource = null;
        
        // Priority 1: Check manifest attribute set during export
        if ( manifest != null && manifest.getExportManifestEntity( sourcePath ) != null ) {
          if ( manifest.getExportManifestEntity( sourcePath ).getEntityMetaData().isGeneratedContent() ) {
            isFileAGC = true;
            gcSource = "manifest attribute";
          }
        }
        
        // Priority 2: Check actual file metadata if manifest attribute not available (backward compatibility)
        if ( !isFileAGC ) {
          RepositoryFileExtraMetaData extraMetaData = solutionImportHandler.getImportSession().processExtraMetaDataForFile( sourcePath );
          if ( extraMetaData != null ) {
            Map<String, Serializable> metadata = extraMetaData.getExtraMetaData();
            if ( metadata != null && metadata.containsKey( "lineage-id" ) ) {
              isFileAGC = true;
              gcSource = "file metadata";
            }
          }
        }
        
        if ( isFileAGC ) {
          skippedGeneratedContentCount++;
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( "Skipping generated content file during restore: " + sourcePath
              + " (identified via " + gcSource + ")" );
          }
          continue;
        }
      }

       

      solutionImportHandler.getImportSession().setCurrentManifestKey( sourcePath );

      bundleBuilder.charSet( bundle.getCharSet() );
      bundleBuilder.overwriteFile( bundle.overwriteInRepository() );
      bundleBuilder.applyAclSettings( bundle.isApplyAclSettings() );
      bundleBuilder.retainOwnership( bundle.isRetainOwnership() );
      bundleBuilder.overwriteAclSettings( bundle.isOverwriteAclSettings() );
      bundleBuilder.acl( solutionImportHandler.getImportSession().processAclForFile( sourcePath ) );
      bundleBuilder.extraMetaData( solutionImportHandler.getImportSession().processExtraMetaDataForFile( sourcePath ) );

      RepositoryFile file = solutionImportHandler.getFile( importBundle, fileBundle );
      ManifestFile manifestFile = solutionImportHandler.getImportSession().getManifestFile( sourcePath, file != null );

      bundleBuilder.hidden( solutionImportHandler.isFileHidden( file, manifestFile, sourcePath ) );
      boolean isSchedulable = solutionImportHandler.isSchedulable( file, manifestFile );

      if ( isSchedulable ) {
        bundleBuilder.schedulable( isSchedulable );
      } else {
        bundleBuilder.schedulable( solutionImportHandler.fileIsScheduleInputSource( manifest, sourcePath ) );
      }

      IPlatformImportBundle platformImportBundle = solutionImportHandler.build( bundleBuilder );
      try {
        importer.importFile( platformImportBundle );
        successfulFilesImportCount++;
        solutionImportHandler.getMetrics().recordSuccess( ImportExportMetrics.Category.FILES );
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().debug( "Successfully restored repository object with path [ " + repositoryFilePath + " ]" );
        }
      } catch ( PlatformImportException e ) {
        solutionImportHandler.getMetrics().recordFailure( ImportExportMetrics.Category.FILES, repositoryFilePath, e );
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", repositoryFilePath, e.getLocalizedMessage() ) );
        }
      }

      if ( bundleInputStream != null ) {
        bundleInputStream.close();
      }
    }

    // Process locale files.
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_LOCALEFILE" ) );
    }
    int successfulLocaleFilesProcessed = 0;
    try {
      successfulLocaleFilesProcessed = localeFilesProcessor.processLocaleFiles( importer );
    } catch ( PlatformImportException e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_LOCALE_FILE", e.getLocalizedMessage() ) );
      }
    } finally {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_LOCALEFILE" ) );
      }
    }

    // Feed locale file count into metrics so the RESTORE OPERATION SUMMARY matches
    // the "Successfully restored X out of Y" line (which includes both counters).
    for ( int i = 0; i < successfulLocaleFilesProcessed; i++ ) {
      solutionImportHandler.getMetrics().recordSuccess( ImportExportMetrics.Category.FILES );
    }

    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString(
        "SolutionImportHandler.INFO_SUCCESSFUL_REPOSITORY_IMPORT_COUNT", successfulFilesImportCount
          + successfulLocaleFilesProcessed, solutionImportHandler.getFiles().size() ) );
      if ( skippedGeneratedContentCount > 0 ) {
        solutionImportHandler.getLogger().info( "Excluded " + skippedGeneratedContentCount
          + " generated content file(s) during restore (include-generated-content=false)" );
      }
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_FILEFOLDER" ) );
    }
  }


  public void setBundle( IPlatformImportBundle bundle ) {
    this.bundle = bundle;
  }
}
