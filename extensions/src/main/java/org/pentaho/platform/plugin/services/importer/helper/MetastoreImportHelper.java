package org.pentaho.platform.plugin.services.importer.helper;

import org.apache.commons.io.IOUtils;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.ImportSource;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMetaStore;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
      return ( (ComponentConfig) config ).isIncludeMetastore();
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
      solutionImportHandler.getLogger()
        .info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_METASTORE" ) );
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

      try {
        InputStream input = getFileInputStream( manifestMetaStore.getFile() );
        if ( input != null ) {
          try ( InputStream is = input ) {
            byte[] bytes = IOUtils.toByteArray( is );
            bundleBuilder.input( new ByteArrayInputStream( bytes ) );
          }
          IPlatformImporter importer = PentahoSystem.get( IPlatformImporter.class );
          importer.importFile( solutionImportHandler.build( bundleBuilder ) );
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger()
              .info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_IMPORT_METASTORE" ) );
            // Track metastore import as success in metrics
            ImportExportMetrics metrics = solutionImportHandler.getMetrics();
            if ( metrics != null ) {
              metrics.recordSuccess( ImportExportMetrics.Category.METASTORE );
            }
          }
        } else {
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger()
              .warn( Messages.getInstance()
                .getString( "SolutionImportHandler.WARN_METASTORE_FILE_NOT_FOUND", manifestMetaStore.getFile() ) );
            // Track metastore import as failure in metrics
            ImportExportMetrics metrics = solutionImportHandler.getMetrics();
            if ( metrics != null ) {
              metrics.recordFailure( ImportExportMetrics.Category.METASTORE, "metastore", "Import failed" );
            }
          }
        }
      } catch ( Exception e ) {
        solutionImportHandler.getMetrics()
          .recordFailure( ImportExportMetrics.Category.METASTORE, "metastore", "Import failed" );
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( Messages.getInstance()
            .getString( "SolutionImportHandler.ERROR_IMPORTING_REPOSITORY_OBJECT", manifestMetaStore.getFile(),
              e.getLocalizedMessage() ) );
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
      solutionImportHandler.getLogger()
        .info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_METASTORE" ) );
    }
  }

  private InputStream getFileInputStream( String fileName ) {
    InputStream inputStream = null;
    if ( fileName != null ) {
      try {
        for ( ImportSource.IRepositoryFileBundle fileBundle : solutionImportHandler.getFiles() ) {
          if ( fileBundle.getFile() != null && fileBundle.getFile().getName().equals( fileName ) ) {
            inputStream = fileBundle.getInputStream();
            break;
          }
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Error retrieving metastore file input stream: " + e.getMessage() );
        }
      }
    }
    return inputStream;
  }
}
