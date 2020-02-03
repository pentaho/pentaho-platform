/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2019-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;


import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseAccessType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.MapExport;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.PartitionDatabaseMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnectionConverter {

  private DatabaseConnectionConverter() {
    // Private constructor to be compliant with squid:S1118
  }

  public static DatabaseConnection model2export( org.pentaho.database.model.IDatabaseConnection databaseConnection ) {
    DatabaseConnection databaseConnectionExport = null;

    if ( null != databaseConnection ) {
      databaseConnectionExport = new DatabaseConnection();

      databaseConnectionExport.setAccessType( model2export( databaseConnection.getAccessType() ) );

      DatabaseConnection.Attributes attributes = new DatabaseConnection.Attributes();
      attributes.getEntry().addAll( mapModel2export( databaseConnection.getAttributes() ) );
      databaseConnectionExport.setAttributes( attributes );

      databaseConnectionExport.setChanged( databaseConnection.getChanged() );

      DatabaseConnection.ConnectionPoolingProperties connectionPoolingProperties =
        new DatabaseConnection.ConnectionPoolingProperties();
      connectionPoolingProperties.getEntry()
        .addAll( mapModel2export( databaseConnection.getConnectionPoolingProperties() ) );
      databaseConnectionExport.setConnectionPoolingProperties( connectionPoolingProperties );

      databaseConnectionExport.setConnectSql( databaseConnection.getConnectSql() );
      databaseConnectionExport.setDatabaseName( databaseConnection.getDatabaseName() );
      databaseConnectionExport.setDatabasePort( databaseConnection.getDatabasePort() );
      databaseConnectionExport.setDatabaseType( model2export( databaseConnection.getDatabaseType() ) );
      databaseConnectionExport.setDataTablespace( databaseConnection.getDataTablespace() );

      DatabaseConnection.ExtraOptions extraOptions = new DatabaseConnection.ExtraOptions();
      extraOptions.getEntry().addAll( mapModel2export( databaseConnection.getExtraOptions() ) );
      databaseConnectionExport.setExtraOptions( extraOptions );

      DatabaseConnection.ExtraOptionsOrder extraOptionsOrder = new DatabaseConnection.ExtraOptionsOrder();
      extraOptionsOrder.getEntry().addAll( mapModel2export( databaseConnection.getExtraOptionsOrder() ) );
      databaseConnectionExport.setExtraOptionsOrder( extraOptionsOrder );

      databaseConnectionExport.setForcingIdentifiersToLowerCase( databaseConnection.isForcingIdentifiersToLowerCase() );
      databaseConnectionExport.setForcingIdentifiersToUpperCase( databaseConnection.isForcingIdentifiersToUpperCase() );
      databaseConnectionExport.setHostname( databaseConnection.getHostname() );
      databaseConnectionExport.setId( databaseConnection.getId() );
      databaseConnectionExport.setIndexTablespace( databaseConnection.getIndexTablespace() );
      databaseConnectionExport.setInformixServername( databaseConnection.getInformixServername() );
      databaseConnectionExport.setInitialPoolSize( databaseConnection.getInitialPoolSize() );
      databaseConnectionExport.setMaximumPoolSize( databaseConnection.getMaximumPoolSize() );
      databaseConnectionExport.setName( databaseConnection.getName() );
      databaseConnectionExport.setPartitioned( databaseConnection.isPartitioned() );
      partitioningInformationModel2export( databaseConnection, databaseConnectionExport );
      databaseConnectionExport.setPassword( databaseConnection.getPassword() );
      databaseConnectionExport.setQuoteAllFields( databaseConnection.isQuoteAllFields() );
      databaseConnectionExport.setSQLServerInstance( databaseConnection.getSQLServerInstance() );
      databaseConnectionExport.setStreamingResults( databaseConnection.isStreamingResults() );
      databaseConnectionExport.setUsername( databaseConnection.getUsername() );
      databaseConnectionExport.setUsingConnectionPool( databaseConnection.isUsingConnectionPool() );
      databaseConnectionExport
        .setUsingDoubleDecimalAsSchemaTableSeparator( databaseConnection.isUsingDoubleDecimalAsSchemaTableSeparator() );
    }

    return databaseConnectionExport;
  }

  public static org.pentaho.database.model.IDatabaseConnection export2model(
    DatabaseConnection databaseConnectionExport ) {
    org.pentaho.database.model.DatabaseConnection databaseConnection = null;

    if ( null != databaseConnectionExport ) {
      databaseConnection = new org.pentaho.database.model.DatabaseConnection();

      databaseConnection.setAccessType( export2model( databaseConnectionExport.getAccessType() ) );
      databaseConnection.setAttributes( mapExport2model( databaseConnectionExport.getAttributes() ) );
      databaseConnection.setChanged( databaseConnectionExport.isChanged() );
      databaseConnection
        .setConnectionPoolingProperties( mapExport2model( databaseConnectionExport.getConnectionPoolingProperties() ) );
      databaseConnection.setConnectSql( databaseConnectionExport.getConnectSql() );
      databaseConnection.setDatabaseName( databaseConnectionExport.getDatabaseName() );
      databaseConnection.setDatabasePort( databaseConnectionExport.getDatabasePort() );
      databaseConnection.setDatabaseType( export2model( databaseConnectionExport.getDatabaseType() ) );
      databaseConnection.setDataTablespace( databaseConnectionExport.getDataTablespace() );
      databaseConnection.setExtraOptions( mapExport2model( databaseConnectionExport.getExtraOptions() ) );
      databaseConnection.setExtraOptionsOrder( mapExport2model( databaseConnectionExport.getExtraOptionsOrder() ) );
      databaseConnection.setForcingIdentifiersToLowerCase( databaseConnectionExport.isForcingIdentifiersToLowerCase() );
      databaseConnection.setForcingIdentifiersToUpperCase( databaseConnectionExport.isForcingIdentifiersToUpperCase() );
      databaseConnection.setHostname( databaseConnectionExport.getHostname() );
      databaseConnection.setId( databaseConnectionExport.getId() );
      databaseConnection.setIndexTablespace( databaseConnectionExport.getIndexTablespace() );
      databaseConnection.setInformixServername( databaseConnectionExport.getInformixServername() );
      databaseConnection.setInitialPoolSize( databaseConnectionExport.getInitialPoolSize() );
      databaseConnection.setMaximumPoolSize( databaseConnectionExport.getMaximumPoolSize() );
      databaseConnection.setName( databaseConnectionExport.getName() );
      databaseConnection.setPartitioned( databaseConnectionExport.isPartitioned() );
      partitioningInformationExport2model( databaseConnectionExport, databaseConnection );
      databaseConnection.setPassword( databaseConnectionExport.getPassword() );
      databaseConnection.setQuoteAllFields( databaseConnectionExport.isQuoteAllFields() );
      databaseConnection.setSQLServerInstance( databaseConnectionExport.getSQLServerInstance() );
      databaseConnection.setStreamingResults( databaseConnectionExport.isStreamingResults() );
      databaseConnection.setUsername( databaseConnectionExport.getUsername() );
      databaseConnection.setUsingConnectionPool( databaseConnectionExport.isUsingConnectionPool() );
      databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator(
        databaseConnectionExport.isUsingDoubleDecimalAsSchemaTableSeparator() );
    }

    return databaseConnection;
  }

  public static DatabaseAccessType model2export( org.pentaho.database.model.DatabaseAccessType databaseAccessType ) {
    DatabaseAccessType databaseAccessTypeExport = null;

    if ( null != databaseAccessType ) {
      databaseAccessTypeExport = DatabaseAccessType.fromValue( databaseAccessType.getValue() );
    }

    return databaseAccessTypeExport;
  }

  public static org.pentaho.database.model.DatabaseAccessType export2model(
    DatabaseAccessType databaseAccessTypeExport ) {
    org.pentaho.database.model.DatabaseAccessType databaseAccessType = null;

    if ( null != databaseAccessTypeExport ) {
      databaseAccessType =
        org.pentaho.database.model.DatabaseAccessType.valueOf( databaseAccessTypeExport.value() );
    }

    return databaseAccessType;
  }

  public static DatabaseType model2export( org.pentaho.database.model.IDatabaseType databaseType ) {
    DatabaseType databaseTypeExport = new DatabaseType();

    if ( null != databaseType ) {
      databaseTypeExport.setDefaultDatabaseName( databaseType.getDefaultDatabaseName() );
      databaseTypeExport.setDefaultDatabasePort( databaseType.getDefaultDatabasePort() );
      DatabaseType.DefaultOptions defaultOptions = new DatabaseType.DefaultOptions();
      defaultOptions.getEntry().addAll( mapModel2export( databaseType.getDefaultOptions() ) );
      databaseTypeExport.setDefaultOptions( defaultOptions );
      databaseTypeExport.setExtraOptionsHelpUrl( databaseType.getExtraOptionsHelpUrl() );
      databaseTypeExport.setName( databaseType.getName() );
      databaseTypeExport.setShortName( databaseType.getShortName() );
      for ( org.pentaho.database.model.DatabaseAccessType supportedAccessType : databaseType
        .getSupportedAccessTypes() ) {
        databaseTypeExport.getSupportedAccessTypes().add( model2export( supportedAccessType ) );
      }
    }

    return databaseTypeExport;
  }

  public static org.pentaho.database.model.DatabaseType export2model( DatabaseType databaseTypeExport ) {
    org.pentaho.database.model.DatabaseType databaseType = new org.pentaho.database.model.DatabaseType();

    if ( null != databaseTypeExport ) {
      databaseType.setDefaultDatabaseName( databaseTypeExport.getDefaultDatabaseName() );
      databaseType.setDefaultDatabasePort( databaseTypeExport.getDefaultDatabasePort() );
      databaseType.setDefaultOptions( mapExport2model( databaseTypeExport.getDefaultOptions() ) );
      databaseType.setExtraOptionsHelpUrl( databaseTypeExport.getExtraOptionsHelpUrl() );
      databaseType.setName( databaseTypeExport.getName() );
      databaseType.setShortName( databaseTypeExport.getShortName() );
      List<org.pentaho.database.model.DatabaseAccessType> supportedAccessTypes = new ArrayList<>();
      for ( DatabaseAccessType supportedAccessType : databaseTypeExport.getSupportedAccessTypes() ) {
        supportedAccessTypes.add( export2model( supportedAccessType ) );
      }
      databaseType.setSupportedAccessTypes( supportedAccessTypes );
    }

    return databaseType;
  }

  public static List<MapExport.Entry> mapModel2export( Map<String, String> mapModel ) {
    List<MapExport.Entry> exportList = new ArrayList<>();

    if ( null != mapModel ) {
      for ( Map.Entry<String, String> modelEntry : mapModel.entrySet() ) {
        MapExport.Entry exportEntry = new MapExport.Entry();
        exportEntry.setKey( modelEntry.getKey() );
        exportEntry.setValue( modelEntry.getValue() );
        exportList.add( exportEntry );
      }
    }

    return exportList;
  }

  public static Map<String, String> mapExport2model( MapExport mapExport ) {
    Map<String, String> mapModel = new HashMap<>();

    if ( null != mapExport ) {
      for ( MapExport.Entry exportEntry : mapExport.getEntry() ) {
        mapModel.put( exportEntry.getKey(), exportEntry.getValue() );
      }
    }

    return mapModel;
  }

  public static void partitioningInformationModel2export(
    org.pentaho.database.model.IDatabaseConnection databaseConnection, DatabaseConnection databaseConnectionExport ) {
    List<org.pentaho.database.model.PartitionDatabaseMeta> partitioningInformationList =
      databaseConnection.getPartitioningInformation();
    List<PartitionDatabaseMeta> partitioningInformationExportList =
      databaseConnectionExport.getPartitioningInformation();

    if ( null != partitioningInformationList ) {
      for ( org.pentaho.database.model.PartitionDatabaseMeta partitioningInformation : partitioningInformationList ) {
        PartitionDatabaseMeta partitionDatabaseMetaExport = new PartitionDatabaseMeta();
        partitionDatabaseMetaExport.setDatabaseName( partitioningInformation.getDatabaseName() );
        partitionDatabaseMetaExport.setHostname( partitioningInformation.getHostname() );
        partitionDatabaseMetaExport.setPartitionId( partitioningInformation.getPartitionId() );
        partitionDatabaseMetaExport.setPassword( partitioningInformation.getPassword() );
        partitionDatabaseMetaExport.setPort( partitioningInformation.getPort() );
        partitionDatabaseMetaExport.setUsername( partitioningInformation.getUsername() );
        partitioningInformationExportList.add( partitionDatabaseMetaExport );
      }
    }
  }

  public static void partitioningInformationExport2model( DatabaseConnection databaseConnectionExport,
                                                          org.pentaho.database.model.IDatabaseConnection databaseConnection ) {
    List<org.pentaho.database.model.PartitionDatabaseMeta> partitioningInformationList = new ArrayList<>();
    List<PartitionDatabaseMeta> partitioningInformationExportList =
      databaseConnectionExport.getPartitioningInformation();

    if ( null != partitioningInformationExportList ) {
      for ( PartitionDatabaseMeta partitioningInformationExport : partitioningInformationExportList ) {
        org.pentaho.database.model.PartitionDatabaseMeta partitionDatabaseMeta =
          new org.pentaho.database.model.PartitionDatabaseMeta();
        partitionDatabaseMeta.setDatabaseName( partitioningInformationExport.getDatabaseName() );
        partitionDatabaseMeta.setHostname( partitioningInformationExport.getHostname() );
        partitionDatabaseMeta.setPartitionId( partitioningInformationExport.getPartitionId() );
        partitionDatabaseMeta.setPassword( partitioningInformationExport.getPassword() );
        partitionDatabaseMeta.setPort( partitioningInformationExport.getPort() );
        partitionDatabaseMeta.setUsername( partitioningInformationExport.getUsername() );
        partitioningInformationList.add( partitionDatabaseMeta );
      }
    }

    databaseConnection.setPartitioningInformation( partitioningInformationList );
  }
}
