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

package org.pentaho.platform.plugin.services.exporter.helper;

import org.castor.core.util.Assert;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.importexport.ExportException;
import org.pentaho.platform.api.importexport.IExportHelper;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.exporter.PentahoPlatformExporter;
import org.pentaho.platform.plugin.services.importexport.ComponentConfig;
import org.pentaho.platform.plugin.services.importexport.DatabaseConnectionConverter;
import org.pentaho.platform.plugin.services.importexport.ImportExportMetrics;
import org.pentaho.platform.plugin.services.messages.Messages;

import java.util.List;

/**
 * Export helper for JDBC datasources.
 * Contains all logic for exporting datasource connections with metrics tracking.
 */
public class DatasourcesExportHelper implements IExportHelper {
  private IDatasourceMgmtService datasourceMgmtService;

  @Override
  public String getName() {
    return "DatasourcesExporter";
  }


  public boolean shouldExecute( Object config ) {
    if ( config instanceof ComponentConfig ) {
      return ( ( ComponentConfig ) config ).isIncludeDatasources();
    }
    return false;
  }

  @Override
  public void doExport( Object exportArg ) throws ExportException {
    Assert.notNull( exportArg, "PentahoPlatformExporter is expected to be not null");
    PentahoPlatformExporter exporter = (PentahoPlatformExporter) exportArg;

    Object config = exporter.getComponentConfig();
    if ( !shouldExecute( config ) ) {
      return;
    }

    // Check if datasources should be exported
    if ( !shouldExecute( config ) ) {
      exporter.getRepositoryExportLogger().debug( "Skipping datasources export (not included in backup configuration)" );
      return;
    }

    try {
      exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_START_EXPORT_JDBC_DATASOURCE" ) );
      int successfulExportJDBCDSCount = 0;
      int failedCount = 0;
      int databaseConnectionsSize = 0;
      
      List<IDatabaseConnection> databaseConnections = getDatasourceMgmtService().getDatasources( );
      if ( databaseConnections != null ) {
        databaseConnectionsSize = databaseConnections.size();
        exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_JDBC_DATASOURCE_TO_EXPORT", databaseConnectionsSize ) );
        for ( IDatabaseConnection datasource : databaseConnections ) {
          if ( datasource instanceof org.pentaho.database.model.DatabaseConnection ) {
            exporter.getRepositoryExportLogger().debug( "Starting to perform backup of datasource [ " + datasource.getName() + " ]" );
            try {
              exporter.getExportManifest().addDatasource( DatabaseConnectionConverter.model2export( datasource ) );
              exporter.getRepositoryExportLogger().debug( "Finished performing backup of datasource [ " + datasource.getName() + " ]" );
              successfulExportJDBCDSCount++;
              if ( exporter.getExportMetrics() != null ) {
                exporter.getExportMetrics().recordSuccess( ImportExportMetrics.Category.DATASOURCES );
              }
            } catch ( Exception e ) {
              failedCount++;
              if ( exporter.getExportMetrics() != null ) {
                exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.DATASOURCES, datasource.getName(), e );
              }
            }
          }
        }
        exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_SUCCESSFUL_JDBC_DATASOURCE_EXPORT_COUNT", successfulExportJDBCDSCount, databaseConnectionsSize ) );
        exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_END_EXPORT_JDBC_DATASOURCE" ) );
      } else {
        exporter.getRepositoryExportLogger().info( Messages.getInstance().getString( "PentahoPlatformExporter.INFO_COUNT_JDBC_DATASOURCE_TO_EXPORT", 0 ) );
      }
    } catch ( Exception e ) {
      if ( exporter.getExportMetrics() != null ) {
        exporter.getExportMetrics().recordFailure( ImportExportMetrics.Category.DATASOURCES, "datasources", e );
      }
      throw new ExportException( "Failed to export datasources: " + e.getMessage(), e );
    }
  }

  public IDatasourceMgmtService getDatasourceMgmtService() {
    if ( datasourceMgmtService == null ) {
      datasourceMgmtService = PentahoSystem.get( IDatasourceMgmtService.class, PentahoSessionHolder.getSession() );
    }
    return datasourceMgmtService;
  }

  public void setDatasourceMgmtService( IDatasourceMgmtService datasourceMgmtService ) {
    this.datasourceMgmtService = datasourceMgmtService;
  }
}
