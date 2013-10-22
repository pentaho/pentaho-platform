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

package org.pentaho.platform.repository.webservices;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DatabaseConnectionAdapter extends XmlAdapter<DatabaseConnectionDto, DatabaseConnection> {

  @Override
  public DatabaseConnectionDto marshal( DatabaseConnection dbConn ) throws Exception {
    if ( dbConn != null ) {
      DatabaseConnectionDto dbConnDto = new DatabaseConnectionDto();
      dbConnDto.setId( dbConn.getId() );
      dbConnDto.setAccessType( dbConn.getAccessType() );
      dbConnDto.setAccessTypeValue( dbConn.getAccessTypeValue() );
      dbConnDto.setChanged( dbConn.getChanged() );
      dbConnDto.setConnectionPoolingProperties( dbConn.getConnectionPoolingProperties() );
      dbConnDto.setConnectSql( dbConn.getConnectSql() );
      dbConnDto.setDatabaseName( dbConn.getDatabaseName() );
      dbConnDto.setDatabasePort( dbConn.getDatabasePort() );
      if ( dbConn.getDatabaseType() != null ) {
        dbConnDto.setDatabaseType( dbConn.getDatabaseType().getShortName() );
      }
      dbConnDto.setDataTablespace( dbConn.getDataTablespace() );
      dbConnDto.setForcingIdentifiersToLowerCase( dbConn.isForcingIdentifiersToLowerCase() );
      dbConnDto.setForcingIdentifiersToUpperCase( dbConn.isForcingIdentifiersToUpperCase() );
      dbConnDto.setHostname( dbConn.getHostname() );
      dbConnDto.setIndexTablespace( dbConn.getIndexTablespace() );
      dbConnDto.setInformixServername( dbConn.getInformixServername() );
      dbConnDto.setInitialPoolSize( dbConn.getInitialPoolSize() );
      dbConnDto.setMaximumPoolSize( dbConn.getMaximumPoolSize() );
      dbConnDto.setName( dbConn.getName() );
      dbConnDto.setPartitioned( dbConn.isPartitioned() );
      dbConnDto.setPartitioningInformation( dbConn.getPartitioningInformation() );
      dbConnDto.setPassword( dbConn.getPassword() );
      dbConnDto.setQuoteAllFields( dbConn.isQuoteAllFields() );
      dbConnDto.setSQLServerInstance( dbConn.getSQLServerInstance() );
      dbConnDto.setStreamingResults( dbConn.isStreamingResults() );
      dbConnDto.setUsername( dbConn.getUsername() );
      dbConnDto.setUsingConnectionPool( dbConn.isUsingConnectionPool() );
      dbConnDto.setUsingDoubleDecimalAsSchemaTableSeparator( dbConn.isUsingDoubleDecimalAsSchemaTableSeparator() );
      return dbConnDto;
    } else {
      return null;
    }
  }

  @Override
  public DatabaseConnection unmarshal( DatabaseConnectionDto dbConnDto ) throws Exception {
    if ( dbConnDto != null ) {
      IDatabaseDialectService databaseDialectService = new DatabaseDialectService();
      DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( databaseDialectService.getDatabaseTypes() );
      DatabaseConnection dbConn = new DatabaseConnection();
      dbConn.setId( dbConnDto.getId() );
      dbConn.setAccessType( dbConnDto.getAccessType() );
      dbConn.setAccessTypeValue( dbConnDto.getAccessTypeValue() );
      dbConn.setChanged( dbConnDto.getChanged() );
      dbConn.setConnectionPoolingProperties( dbConnDto.getConnectionPoolingProperties() );
      dbConn.setConnectSql( dbConnDto.getConnectSql() );
      dbConn.setDatabaseName( dbConnDto.getDatabaseName() );
      dbConn.setDatabasePort( dbConnDto.getDatabasePort() );
      if ( dbConnDto.getDatabaseType() != null ) {
        dbConn.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( dbConnDto.getDatabaseType() ) );
      }
      dbConn.setDataTablespace( dbConnDto.getDataTablespace() );
      dbConn.setForcingIdentifiersToLowerCase( dbConnDto.isForcingIdentifiersToLowerCase() );
      dbConn.setForcingIdentifiersToUpperCase( dbConnDto.isForcingIdentifiersToUpperCase() );
      dbConn.setHostname( dbConnDto.getHostname() );
      dbConn.setIndexTablespace( dbConnDto.getIndexTablespace() );
      dbConn.setInformixServername( dbConnDto.getInformixServername() );
      dbConn.setInitialPoolSize( dbConnDto.getInitialPoolSize() );
      dbConn.setMaximumPoolSize( dbConnDto.getMaximumPoolSize() );
      dbConn.setName( dbConnDto.getName() );
      dbConn.setPartitioned( dbConnDto.isPartitioned() );
      dbConn.setPartitioningInformation( dbConnDto.getPartitioningInformation() );
      dbConn.setPassword( dbConnDto.getPassword() );
      dbConn.setQuoteAllFields( dbConnDto.isQuoteAllFields() );
      dbConn.setSQLServerInstance( dbConnDto.getSQLServerInstance() );
      dbConn.setStreamingResults( dbConnDto.isStreamingResults() );
      dbConn.setUsername( dbConnDto.getUsername() );
      dbConn.setUsingConnectionPool( dbConnDto.isUsingConnectionPool() );
      dbConn.setUsingDoubleDecimalAsSchemaTableSeparator( dbConnDto.isUsingDoubleDecimalAsSchemaTableSeparator() );
      return dbConn;
    } else {
      return null;
    }
  }

}
