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
import java.util.Map;

import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.ExportManifestMondrian;
import org.pentaho.platform.plugin.services.importexport.exportManifest.Parameters;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

/**
 * Import helper for Mondrian OLAP schema restoration.
 * Handles importing Mondrian cube definitions and OLAP schemas from backup.
 *
 * Profile: MONDRIAN
 * Filters: isIncludeMondrian()
 */
public class MondrianImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;

  @Override
  public String getName() {
    return "Mondrian OLAP Schema Import Helper";
  }

  public boolean shouldExecute( Object config ) {
    if ( config == null ) {
      return true; // Full restore - include all content
    }
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeMondrian();
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
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping Mondrian import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting Mondrian OLAP schema import..." );
      }

      try {
        importMondrian( manifest.getMondrianList() );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed Mondrian import" );
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import Mondrian schemas: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Mondrian import error", e );
        }
        throw new ImportException( "Failed to import Mondrian schemas: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "Mondrian import helper error: " + e.getMessage() );
      }
      throw new ImportException( "Mondrian import helper failed: " + e.getMessage(), e );
    }
  }

  /**
   * Import Mondrian OLAP schemas into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  private void importMondrian( List<ExportManifestMondrian> mondrianList ) {
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_MONDRIAN_DATASOURCE" ) );
    }
    if ( null != mondrianList ) {
      int successfulMondrianSchemaImport = 0;
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_MONDRIAN_DATASOURCE", mondrianList.size() ) );
      }
      for ( ExportManifestMondrian exportManifestMondrian : mondrianList ) {
        try {
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( "Restoring  [ " + exportManifestMondrian.getCatalogName() + " ] mondrian datasource" );
          }
          String catName = exportManifestMondrian.getCatalogName();
          // Validate required fields
          if ( catName == null || catName.trim().isEmpty() ) {
            solutionImportHandler.getLogger().warn( "Skipping Mondrian schema import - missing catalog name" );
            continue;
          }
          Parameters parametersMap = exportManifestMondrian.getParameters();
          StringBuilder parametersStr = new StringBuilder();
          if ( parametersMap != null ) {
            for ( Map.Entry<String, String> e : parametersMap.entrySet() ) {
              parametersStr.append( e.getKey() ).append( '=' ).append( e.getValue() ).append( ';' );
            }
          }

          RepositoryFileImportBundle.Builder bundleBuilder =
              new RepositoryFileImportBundle.Builder().charSet( "UTF-8" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT )
                  .schedulable( RepositoryFile.SCHEDULABLE_BY_DEFAULT ).name( catName ).overwriteFile(
                  solutionImportHandler.isOverwriteFile() ).mime( "application/vnd.pentaho.mondrian+xml" )
                  .withParam( "parameters", parametersStr.toString() )
                  .withParam( "domain-id", catName );
          // pass as param if not in parameters string
          String xmlaEnabled = "" + exportManifestMondrian.isXmlaEnabled();
          bundleBuilder.withParam( "EnableXmla", xmlaEnabled );

          // Cache with just the filename as key for lookup
          String manifestFile = exportManifestMondrian.getFile();
          String cacheKey = new java.io.File( manifestFile ).getName(); // Extract filename only
          
          solutionImportHandler.getLogger().debug( "[MondrianImportHelper] Caching Mondrian catalog [" + catName + "] with key: " + cacheKey );
          solutionImportHandler.getCachedImports().put( cacheKey, bundleBuilder );

          String annotationsFile = exportManifestMondrian.getAnnotationsFile();
          if ( annotationsFile != null ) {
            RepositoryFileImportBundle.Builder annotationsBundle =
                new RepositoryFileImportBundle.Builder().path( MondrianCatalogRepositoryHelper.ETC_MONDRIAN_JCR_FOLDER
                    + RepositoryFile.SEPARATOR + catName ).name( "annotations.xml" ).charSet( "UTF-8" ).overwriteFile(
                    solutionImportHandler.isOverwriteFile() ).mime( "text/xml" ).hidden( RepositoryFile.HIDDEN_BY_DEFAULT ).schedulable(
                    RepositoryFile.SCHEDULABLE_BY_DEFAULT ).withParam( "domain-id", catName );
            // Cache annotations by filename too
            String annotationsCacheKey = new java.io.File( annotationsFile ).getName();
            solutionImportHandler.getLogger().debug( "[MondrianImportHelper] Caching Mondrian annotations [" + annotationsFile + "] with key: " + annotationsCacheKey );
            solutionImportHandler.getCachedImports().put( annotationsFile, annotationsBundle );
            solutionImportHandler.getCachedImports().put( annotationsCacheKey, annotationsBundle );
          }
          successfulMondrianSchemaImport++;
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().debug( " Successfully prepared  [ " + exportManifestMondrian.getCatalogName() + " ] mondrian datasource for import" );
          }
        } catch ( Exception e ) {
          solutionImportHandler.getLogger().warn( "Failed to prepare Mondrian schema [ " + exportManifestMondrian.getCatalogName() + " ] for import: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "Mondrian preparation error", e );
        }
      }
      if ( solutionImportHandler.isPerformingRestore() ) {
        int mondrianFailedCount = mondrianList.size() - successfulMondrianSchemaImport;
        
        // Track Mondrian imports in metrics
        ImportExportMetrics metrics = solutionImportHandler.getMetrics();
        if ( metrics != null ) {
          for ( int i = 0; i < successfulMondrianSchemaImport; i++ ) {
            metrics.recordSuccess( ImportExportMetrics.Category.MONDRIAN );
          }
          for ( int i = 0; i < mondrianFailedCount; i++ ) {
            metrics.recordFailure( ImportExportMetrics.Category.MONDRIAN, "schema", "Import failed" );
          }
        }
        
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_MONDRIAN_DATASOURCE_IMPORT_COUNT", successfulMondrianSchemaImport, mondrianList.size() ) );
      }
    }
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_MONDRIAN_DATASOURCE" ) );
    }
  }
}
