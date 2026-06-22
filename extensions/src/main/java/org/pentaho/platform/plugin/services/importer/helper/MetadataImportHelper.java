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

import java.util.List;

import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetadata;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;

/**
 * Import helper for metadata (datasources) restoration.
 * Handles importing data source definitions and metadata models from backup.
 *
 * Profile: DATASOURCES
 * Filters: isIncludeDatasources()
 */
public class MetadataImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;
  private boolean preserveDsw = false;

  @Override
  public String getName() {
    return "Metadata (Datasources) Import Helper";
  }

  @Override
  public boolean shouldExecute( Object config ) {
    if ( config == null ) {
      return true; // Full restore - include all content
    }
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeDatasources();
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
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping metadata import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting metadata (datasources) import..." );
      }

      try {
        importMetadata( manifest.getMetadataList() );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed metadata import" );
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import metadata: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Metadata import error", e );
        }
        throw new ImportException( "Failed to import metadata: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "Metadata import helper error: " + e.getMessage() );
      }
      throw new ImportException( "Metadata import helper failed: " + e.getMessage(), e );
    }
  }

  /**
   * Set whether to preserve DSW (Data Source Wizard) settings during import
   */
  public void setPreserveDsw( boolean preserveDsw ) {
    this.preserveDsw = preserveDsw;
  }

  /**
   * Import metadata domains into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  private void importMetadata( List<ExportManifestMetadata> metadataList ) {
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METADATA_DATASOURCE" ) );
    }
    if ( null != metadataList ) {
      int successfulMetadataModelImport = 0;
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_METADATA_DATASOURCE", metadataList.size() ) );
      }
      for ( ExportManifestMetadata exportManifestMetadata : metadataList ) {
        try {
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( "Restoring  [ " + exportManifestMetadata.getDomainId() + " ] datasource" );
          }
          String domainId = exportManifestMetadata.getDomainId();
          if ( domainId != null && !domainId.endsWith( ".xmi" ) ) {
            domainId = domainId + ".xmi";
          }
          // Validate required fields
          if ( domainId == null || exportManifestMetadata.getFile() == null ) {
            solutionImportHandler.getLogger().warn( "Skipping metadata import - missing domainId or file path" );
            continue;
          }
          RepositoryFileImportBundle.Builder bundleBuilder =
              new RepositoryFileImportBundle.Builder().charSet( "UTF-8" )
                  .hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT )
                  .preserveDsw( preserveDsw )
                  .overwriteFile( solutionImportHandler.isOverwriteFile() )
                  .mime( "text/xmi+xml" )
                  .withParam( "domain-id", domainId );

          // Cache with just the filename as key for lookup
          // The manifest stores relative paths, but we need to match against computed bundle paths
          // Using filename-only key ensures metadata files are found regardless of path structure
          String manifestFile = exportManifestMetadata.getFile();
          String cacheKey = new java.io.File( manifestFile ).getName(); // Extract filename only
          
          solutionImportHandler.getLogger().debug( "[MetadataImportHelper] Caching metadata domain [" + domainId + "] with key: " + cacheKey );
          solutionImportHandler.getCachedImports().put( cacheKey, bundleBuilder );
          
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( " Successfully prepared  [ " + exportManifestMetadata.getDomainId() + " ] datasource for import" );
          }
          successfulMetadataModelImport++;
        } catch ( Exception e ) {
          solutionImportHandler.getLogger().warn( "Failed to prepare metadata [ " + exportManifestMetadata.getDomainId() + " ] for import: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Metadata preparation error", e );
        }
      }
      if ( solutionImportHandler.isPerformingRestore() ) {
        int metadataFailedCount = metadataList.size() - successfulMetadataModelImport;
        
        // Track metadata imports in metrics
        ImportExportMetrics metrics = solutionImportHandler.getMetrics();
        if ( metrics != null ) {
          for ( int i = 0; i < successfulMetadataModelImport; i++ ) {
            metrics.recordSuccess( ImportExportMetrics.Category.METADATA );
          }
          for ( int i = 0; i < metadataFailedCount; i++ ) {
            metrics.recordFailure( ImportExportMetrics.Category.METADATA, "metadata", "Import failed" );
          }
        }
        
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_METDATA_DATASOURCE_COUNT", successfulMetadataModelImport, metadataList.size() ) );
      }
    }
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METADATA_DATASOURCE" ) );
    }
  }
}
