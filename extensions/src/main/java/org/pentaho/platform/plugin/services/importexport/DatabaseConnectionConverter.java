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
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.importexport;


import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseAccessType;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseType;

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
      databaseConnectionExport.setAttributes( attributesModel2export( databaseConnection.getAttributes() ) );
      databaseConnectionExport.setChanged( databaseConnection.getChanged() );
      databaseConnectionExport.setConnectionPoolingProperties(
        connectionPoolingPropertiesModel2export( databaseConnection.getConnectionPoolingProperties() ) );
      databaseConnectionExport.setConnectSql( databaseConnection.getConnectSql() );
      databaseConnectionExport.setDatabaseName( databaseConnection.getDatabaseName() );
      databaseConnectionExport.setDatabasePort( databaseConnection.getDatabasePort() );
      databaseConnectionExport.setDatabaseType( model2export( databaseConnection.getDatabaseType() ) );
      databaseConnectionExport.setDataTablespace( databaseConnection.getDataTablespace() );
      databaseConnectionExport.setExtraOptions( extraOptionsModel2export( databaseConnection.getExtraOptions() ) );
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
      databaseConnection.setAttributes( attributesExport2model( databaseConnectionExport.getAttributes() ) );
      databaseConnection.setChanged( databaseConnectionExport.isChanged() );
      databaseConnection.setConnectionPoolingProperties(
        connectionPoolingPropertiesExport2model( databaseConnectionExport.getConnectionPoolingProperties() ) );
      databaseConnection.setConnectSql( databaseConnectionExport.getConnectSql() );
      databaseConnection.setDatabaseName( databaseConnectionExport.getDatabaseName() );
      databaseConnection.setDatabasePort( databaseConnectionExport.getDatabasePort() );
      databaseConnection.setDatabaseType( export2model( databaseConnectionExport.getDatabaseType() ) );
      databaseConnection.setDataTablespace( databaseConnectionExport.getDataTablespace() );
      databaseConnection.setExtraOptions( extraOptionsExport2model( databaseConnectionExport.getExtraOptions() ) );
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
      databaseTypeExport.setDefaultOptions( defaultOptionsModel2export( databaseType.getDefaultOptions() ) );
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
      databaseType.setDefaultOptions( defaultOptionsExport2model( databaseTypeExport.getDefaultOptions() ) );
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

  public static DatabaseConnection.Attributes attributesModel2export( Map<String, String> attributes ) {
    DatabaseConnection.Attributes attributesExport = new DatabaseConnection.Attributes();

    if ( null != attributes ) {
      for ( Map.Entry<String, String> attribute : attributes.entrySet() ) {
        DatabaseConnection.Attributes.Entry attributeEntry = new DatabaseConnection.Attributes.Entry();
        attributeEntry.setKey( attribute.getKey() );
        attributeEntry.setValue( attribute.getValue() );
        attributesExport.getEntry().add( attributeEntry );
      }
    }

    return attributesExport;
  }

  public static Map<String, String> attributesExport2model( DatabaseConnection.Attributes attributesExport ) {
    Map<String, String> attributes = new HashMap<>();

    if ( null != attributesExport ) {
      for ( DatabaseConnection.Attributes.Entry attributeEntry : attributesExport.getEntry() ) {
        attributes.put( attributeEntry.getKey(), attributeEntry.getValue() );
      }
    }

    return attributes;
  }

  public static DatabaseConnection.ExtraOptions extraOptionsModel2export( Map<String, String> extraOptions ) {
    DatabaseConnection.ExtraOptions extraOptionsExport = new DatabaseConnection.ExtraOptions();

    if ( null != extraOptions ) {
      for ( Map.Entry<String, String> extraOption : extraOptions.entrySet() ) {
        DatabaseConnection.ExtraOptions.Entry extraOptionEntry = new DatabaseConnection.ExtraOptions.Entry();
        extraOptionEntry.setKey( extraOption.getKey() );
        extraOptionEntry.setValue( extraOption.getValue() );
        extraOptionsExport.getEntry().add( extraOptionEntry );
      }
    }

    return extraOptionsExport;
  }

  public static Map<String, String> extraOptionsExport2model( DatabaseConnection.ExtraOptions extraOptionsExport ) {
    Map<String, String> extraOptions = new HashMap<>();

    if ( null != extraOptionsExport ) {
      for ( DatabaseConnection.ExtraOptions.Entry extraOptionEntry : extraOptionsExport.getEntry() ) {
        extraOptions.put( extraOptionEntry.getKey(), extraOptionEntry.getValue() );
      }
    }

    return extraOptions;
  }

  public static DatabaseConnection.ConnectionPoolingProperties connectionPoolingPropertiesModel2export(
    Map<String, String> connectionPoolingProperties ) {
    DatabaseConnection.ConnectionPoolingProperties connectionPoolingPropertiesExport =
      new DatabaseConnection.ConnectionPoolingProperties();

    if ( null != connectionPoolingProperties ) {
      for ( Map.Entry<String, String> extraOption : connectionPoolingProperties.entrySet() ) {
        DatabaseConnection.ConnectionPoolingProperties.Entry extraOptionEntry =
          new DatabaseConnection.ConnectionPoolingProperties.Entry();
        extraOptionEntry.setKey( extraOption.getKey() );
        extraOptionEntry.setValue( extraOption.getValue() );
        connectionPoolingPropertiesExport.getEntry().add( extraOptionEntry );
      }
    }

    return connectionPoolingPropertiesExport;
  }

  public static Map<String, String> connectionPoolingPropertiesExport2model(
    DatabaseConnection.ConnectionPoolingProperties connectionPoolingPropertiesExport ) {
    Map<String, String> connectionPoolingProperties = new HashMap<>();

    if ( null != connectionPoolingPropertiesExport ) {
      for ( DatabaseConnection.ConnectionPoolingProperties.Entry extraOptionEntry : connectionPoolingPropertiesExport
        .getEntry() ) {
        connectionPoolingProperties.put( extraOptionEntry.getKey(), extraOptionEntry.getValue() );
      }
    }

    return connectionPoolingProperties;
  }

  public static DatabaseType.DefaultOptions defaultOptionsModel2export( Map<String, String> defaultOptions ) {
    DatabaseType.DefaultOptions defaultOptionsExport = new DatabaseType.DefaultOptions();

    if ( null != defaultOptions ) {
      for ( Map.Entry<String, String> extraOption : defaultOptions.entrySet() ) {
        DatabaseType.DefaultOptions.Entry extraOptionEntry = new DatabaseType.DefaultOptions.Entry();
        extraOptionEntry.setKey( extraOption.getKey() );
        extraOptionEntry.setValue( extraOption.getValue() );
        defaultOptionsExport.getEntry().add( extraOptionEntry );
      }
    }

    return defaultOptionsExport;
  }

  public static Map<String, String> defaultOptionsExport2model( DatabaseType.DefaultOptions defaultOptionsExport ) {
    Map<String, String> defaultOptions = new HashMap<>();

    if ( null != defaultOptionsExport ) {
      for ( DatabaseType.DefaultOptions.Entry extraOptionEntry : defaultOptionsExport.getEntry() ) {
        defaultOptions.put( extraOptionEntry.getKey(), extraOptionEntry.getValue() );
      }
    }

    return defaultOptions;
  }
}
