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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.importexport.IImportHelper;
import org.pentaho.platform.api.importexport.ImportException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.DatabaseConnectionConverter;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.importexport.exportManifest.ExportManifest;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.plugin.services.importer.SolutionImportHandler;

/**
 * Import helper for JDBC datasource restoration.
 * Handles importing JDBC database connection definitions from backup.
 *
 * Profile: DATASOURCES (same as Metadata)
 * Filters: isIncludeDatasources()
 */
public class JdbcDatasourceImportHelper implements IImportHelper {

  private SolutionImportHandler solutionImportHandler;

  @Override
  public String getName() {
    return "JDBC Datasource Import Helper";
  }

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
          solutionImportHandler.getLogger().debug( "Manifest is null - skipping JDBC datasource import" );
        }
        return;
      }

      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( "Starting JDBC datasource import..." );
      }

      try {
        importJDBCDataSource( manifest );

        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().info( "Successfully completed JDBC datasource import" );
        }
      } catch ( Exception e ) {
        if ( solutionImportHandler.isPerformingRestore() ) {
          solutionImportHandler.getLogger().error( "Failed to import JDBC datasources: " + e.getMessage() );
          solutionImportHandler.getLogger().debug( "JDBC datasource import error", e );
        }
        throw new ImportException( "Failed to import JDBC datasources: " + e.getMessage(), e );
      }
    } catch ( Exception e ) {
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().error( "JDBC datasource import helper error: " + e.getMessage() );
      }
      throw new ImportException( "JDBC datasource import helper failed: " + e.getMessage(), e );
    }
  }

  /**
   * Import JDBC datasources into the platform.
   * Internal implementation extracted from SolutionImportHandler.
   */
  private void importJDBCDataSource( ExportManifest manifest ) {
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_START_IMPORT_DATASOURCE" ) );
    }
    // Add DB Connections
    List<DatabaseConnection> datasourceList = manifest.getDatasourceList();
    if ( datasourceList != null ) {
      int successfulDatasourceImportCount = 0;
      if ( solutionImportHandler.isPerformingRestore() ) {
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_COUNT_DATASOURCE", datasourceList.size() ) );
      }
      IDatasourceMgmtService datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class );
      for ( DatabaseConnection databaseConnection : datasourceList ) {
        if ( databaseConnection.getDatabaseType() == null ) {
          // don't try to import the connection if there is no type it will cause an error
          // However, if this is the DI Server, and the connection is defined in a ktr, it will import automatically
          solutionImportHandler.getLogger().warn( Messages.getInstance()
              .getString( "SolutionImportHandler.ConnectionWithoutDatabaseType", databaseConnection.getName() ) );
          continue;
        }
        try {
          IDatabaseConnection existingDBConnection =
              datasourceMgmtSvc.getDatasourceByName( databaseConnection.getName() );
          if ( existingDBConnection != null && existingDBConnection.getName() != null ) {
            if ( solutionImportHandler.isOverwriteFile() ) {
              databaseConnection.setId( existingDBConnection.getId() );
              datasourceMgmtSvc.updateDatasourceByName( databaseConnection.getName(),
                  DatabaseConnectionConverter.export2model( databaseConnection ) );
            }
          } else {
            datasourceMgmtSvc.createDatasource( DatabaseConnectionConverter.export2model( databaseConnection ) );
          }
          successfulDatasourceImportCount++;
        } catch ( Exception e ) {
          if ( solutionImportHandler.isPerformingRestore() ) {
            solutionImportHandler.getLogger().error( Messages.getInstance().getString( "SolutionImportHandler.ERROR_IMPORTING_JDBC_DATASOURCE", databaseConnection.getName(), e.getLocalizedMessage() ) );
          }
        }
      }
      if ( solutionImportHandler.isPerformingRestore() ) {
        int datasourceFailedCount = datasourceList.size() - successfulDatasourceImportCount;
        
        // Track datasource imports in metrics
        ImportExportMetrics metrics = solutionImportHandler.getMetrics();
        if ( metrics != null ) {
          for ( int i = 0; i < successfulDatasourceImportCount; i++ ) {
            metrics.recordSuccess( ImportExportMetrics.Category.DATASOURCES );
          }
          for ( int i = 0; i < datasourceFailedCount; i++ ) {
            metrics.recordFailure( ImportExportMetrics.Category.DATASOURCES, "datasource", "Import failed" );
          }
        }
        
        solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_SUCCESSFUL_DATASOURCE_IMPORT_COUNT", successfulDatasourceImportCount, datasourceList.size() ) );
      }
    }
    if ( solutionImportHandler.isPerformingRestore() ) {
      solutionImportHandler.getLogger().info( Messages.getInstance().getString( "SolutionImportHandler.INFO_END_IMPORT_DATASOURCE" ) );
    }
  }
}
