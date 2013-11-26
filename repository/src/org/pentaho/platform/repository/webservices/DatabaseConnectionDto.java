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

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.PartitionDatabaseMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseConnectionDto {

  public static final String EMPTY_OPTIONS_STRING = "><EMPTY><"; //$NON-NLS-1$

  // part of the generic database connection, move somewhere else
  public static final String ATTRIBUTE_CUSTOM_URL = "CUSTOM_URL"; //$NON-NLS-1$
  public static final String ATTRIBUTE_CUSTOM_DRIVER_CLASS = "CUSTOM_DRIVER_CLASS"; //$NON-NLS-1$

  public static final String ATTRIBUTE_PREFIX_EXTRA_OPTION = "EXTRA_OPTION_"; //$NON-NLS-1$
  String id;
  String name;
  String databaseName;
  String databasePort;
  String hostname;
  String username;
  String password;
  String dataTablespace;
  String indexTablespace;
  boolean streamingResults;
  boolean quoteAllFields;
  // should this be here?
  boolean changed;

  // dialect specific fields?
  boolean usingDoubleDecimalAsSchemaTableSeparator;

  // Informix server name
  String informixServername;

  boolean forcingIdentifiersToLowerCase;
  boolean forcingIdentifiersToUpperCase;
  String connectSql;
  boolean usingConnectionPool;

  String accessTypeValue = null;
  DatabaseAccessType accessType = null;
  String driver = null;
  Map<String, String> extraOptions = new HashMap<String, String>();
  Map<String, String> attributes = new HashMap<String, String>();
  Map<String, String> connectionPoolingProperties = new HashMap<String, String>();
  List<PartitionDatabaseMeta> partitioningInformation;
  int initialPoolSize;
  int maxPoolSize;
  boolean partitioned;

  public DatabaseConnectionDto() {
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public void setAccessType( DatabaseAccessType accessType ) {
    this.accessType = accessType;
  }

  public DatabaseAccessType getAccessType() {
    return accessType;
  }

  public void setAccessTypeValue( String value ) {
    accessTypeValue = value;
  }

  public String getAccessTypeValue() {
    return accessType == null ? accessTypeValue : accessType.toString();
  }

  public void setDatabaseType( String driver ) {
    this.driver = driver;
  }

  public String getDatabaseType() {
    return driver;
  }

  public Map<String, String> getExtraOptions() {
    return extraOptions;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setHostname( String hostname ) {
    this.hostname = hostname;
  }

  public String getHostname() {
    return hostname;
  }

  public void setDatabaseName( String databaseName ) {
    this.databaseName = databaseName;
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public void setDatabasePort( String databasePort ) {
    this.databasePort = databasePort;
  }

  public String getDatabasePort() {
    return databasePort;
  }

  public void setUsername( String username ) {
    this.username = username;
  }

  public String getUsername() {
    return username;
  }

  public void setPassword( String password ) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  public void setStreamingResults( boolean streamingResults ) {
    this.streamingResults = streamingResults;
  }

  public boolean isStreamingResults() {
    return streamingResults;
  }

  public void setDataTablespace( String dataTablespace ) {
    this.dataTablespace = dataTablespace;
  }

  public String getDataTablespace() {
    return dataTablespace;
  }

  public void setIndexTablespace( String indexTablespace ) {
    this.indexTablespace = indexTablespace;
  }

  public String getIndexTablespace() {
    return indexTablespace;
  }

  public void setSQLServerInstance( String sqlServerInstance ) {
    addExtraOption( "MSSQL", "instance", sqlServerInstance ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public String getSQLServerInstance() {
    return getExtraOptions().get( "MSSQL.instance" ); //$NON-NLS-1$
  }

  public void setUsingDoubleDecimalAsSchemaTableSeparator( boolean usingDoubleDecimalAsSchemaTableSeparator ) {
    this.usingDoubleDecimalAsSchemaTableSeparator = usingDoubleDecimalAsSchemaTableSeparator;
  }

  public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    return usingDoubleDecimalAsSchemaTableSeparator;
  }

  public void setInformixServername( String informixServername ) {
    this.informixServername = informixServername;
  }

  public String getInformixServername() {
    return informixServername;
  }

  public void addExtraOption( String databaseTypeCode, String option, String value ) {
    extraOptions.put( databaseTypeCode + "." + option, value ); //$NON-NLS-1$
  }

  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setChanged( boolean changed ) {
    this.changed = changed;
  }

  public boolean getChanged() {
    return changed;
  }

  public void setQuoteAllFields( boolean quoteAllFields ) {
    this.quoteAllFields = quoteAllFields;
  }

  public boolean isQuoteAllFields() {
    return quoteAllFields;
  }

  public void setForcingIdentifiersToLowerCase( boolean forcingIdentifiersToLowerCase ) {
    this.forcingIdentifiersToLowerCase = forcingIdentifiersToLowerCase;
  }

  public boolean isForcingIdentifiersToLowerCase() {
    return forcingIdentifiersToLowerCase;
  }

  public void setForcingIdentifiersToUpperCase( boolean forcingIdentifiersToUpperCase ) {
    this.forcingIdentifiersToUpperCase = forcingIdentifiersToUpperCase;
  }

  public boolean isForcingIdentifiersToUpperCase() {
    return forcingIdentifiersToUpperCase;
  }

  public void setConnectSql( String sql ) {
    this.connectSql = sql;
  }

  public String getConnectSql() {
    return connectSql;
  }

  public void setUsingConnectionPool( boolean usingConnectionPool ) {
    this.usingConnectionPool = usingConnectionPool;
  }

  public boolean isUsingConnectionPool() {
    return usingConnectionPool;
  }

  public void setInitialPoolSize( int initialPoolSize ) {
    this.initialPoolSize = initialPoolSize;
  }

  public int getInitialPoolSize() {
    return initialPoolSize;
  }

  public void setMaximumPoolSize( int maxPoolSize ) {
    this.maxPoolSize = maxPoolSize;
  }

  public int getMaximumPoolSize() {
    return maxPoolSize;
  }

  public void setPartitioned( boolean partitioned ) {
    this.partitioned = partitioned;
  }

  public boolean isPartitioned() {
    return partitioned;
  }

  public Map<String, String> getConnectionPoolingProperties() {
    return connectionPoolingProperties;
  }

  public void setConnectionPoolingProperties( Map<String, String> connectionPoolingProperties ) {
    this.connectionPoolingProperties = connectionPoolingProperties;
  }

  public void setPartitioningInformation( List<PartitionDatabaseMeta> partitioningInformation ) {
    this.partitioningInformation = partitioningInformation;
  }

  public List<PartitionDatabaseMeta> getPartitioningInformation() {
    return this.partitioningInformation;
  }

}
