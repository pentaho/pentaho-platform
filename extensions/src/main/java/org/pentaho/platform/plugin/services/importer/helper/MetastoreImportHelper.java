package org.pentaho.platform.plugin.services.importer.helper;

import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;

/**
 * Import helper for metastore restoration.
 * Handles importing metastore data from backup.
 *
 * Profile: METASTORE
 * Filters: isIncludeMetastore()
 */
public class MetastoreImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;
  private boolean overwriteFile = false;

  @Override
  public String getName() {
    return "Metastore Import Helper";
  }

  public boolean shouldExecute( Object config ) {
    if ( config == null ) {
      return true; // Full restore - include all content
    }
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeMetastore();
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
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping metastore import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting metastore import..." );
      }

      try {
        importMetaStore( manifest.getMetaStore() );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed metastore import" );
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import metastore: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Metastore import error", e );
        }
        throw new ImportException( "Failed to import metastore: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "Metastore import helper error: " + e.getMessage() );
      }
      throw new ImportException( "Metastore import helper failed: " + e.getMessage(), e );
    }
  }

  /**
   * Set whether to overwrite existing metastore files
   */
  public void setOverwriteFile( boolean overwriteFile ) {
    this.overwriteFile = overwriteFile;
  }

  /**
   * Import metastore configuration into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  public void importMetaStore( ExportManifestMetaStore manifestMetaStore ) {
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METASTORE" ) );
    }
    if ( manifestMetaStore != null ) {
      // get the zipped metastore from the export bundle
      RepositoryFileImportBundle.Builder bundleBuilder =
          new RepositoryFileImportBundle.Builder()
              .path( manifestMetaStore.getFile() )
              .name( manifestMetaStore.getName() )
              .withParam( "description", manifestMetaStore.getDescription() )
              .charSet( "UTF-8" )
              .overwriteFile( overwriteFile )
              .mime( "application/vnd.pentaho.metastore" );

      solutionImportHandler.getCachedImports().put( manifestMetaStore.getFile(), bundleBuilder );
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_IMPORT_METASTORE" ) );
        // Track metastore import as success in metrics
        ImportExportMetrics metrics = solutionImportHandler.getMetrics();
        if ( metrics != null ) {
          metrics.recordSuccess( ImportExportMetrics.Category.METASTORE );
        }
      }
    } else {
      // Metastore was not included in export
      ImportExportMetrics metrics = solutionImportHandler.getMetrics();
      if ( metrics != null ) {
        metrics.recordSkip( ImportExportMetrics.Category.METASTORE, "metastore", "Not included in export" );
      }
    }
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METASTORE" ) );
    }
  }
}
