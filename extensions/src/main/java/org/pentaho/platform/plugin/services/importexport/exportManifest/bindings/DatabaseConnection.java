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

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.25 at 11:25:28 AM EDT 
//

package org.pentaho.platform.plugin.services.importexport.exportManifest.bindings;

import org.pentaho.di.core.encryption.Encr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * <p/>
 * Java class for databaseConnection complex type.
 * <p/>
 * <p/>
 * The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="databaseConnection">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="accessType" type="{http://www.pentaho.com/schema/}databaseAccessType" minOccurs="0"/>
 *         &lt;element name="accessTypeValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="attributes">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="changed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="connectSql" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="connectionPoolingProperties">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="dataTablespace" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="databaseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="databasePort" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element ref="{http://www.pentaho.com/schema/}databaseType" minOccurs="0"/>
 *         &lt;element name="extraOptions">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="forcingIdentifiersToLowerCase" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="forcingIdentifiersToUpperCase" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="hostname" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="indexTablespace" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="informixServername" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="initialPoolSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="maximumPoolSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="partitioned" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="partitioningInformation" type="{http://www.pentaho.com/schema/}partitionDatabaseMeta"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="quoteAllFields" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="SQLServerInstance" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="streamingResults" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="usingConnectionPool" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="usingDoubleDecimalAsSchemaTableSeparator" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="extraOptionsOrder">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="entry" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;sequence>
 *                             &lt;element name="key" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                             &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *                           &lt;/sequence>
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType( XmlAccessType.FIELD )
@XmlType( name = "databaseConnection", propOrder = { "accessType", "accessTypeValue", "attributes", "changed",
  "connectSql", "connectionPoolingProperties", "dataTablespace", "databaseName", "databasePort", "databaseType",
  "extraOptions", "forcingIdentifiersToLowerCase", "forcingIdentifiersToUpperCase", "hostname", "id",
  "indexTablespace", "informixServername", "initialPoolSize", "maximumPoolSize", "name", "partitioned",
  "partitioningInformation", "password", "quoteAllFields", "sqlServerInstance", "streamingResults", "username",
  "usingConnectionPool", "usingDoubleDecimalAsSchemaTableSeparator", "extraOptionsOrder" } )
public class DatabaseConnection {

  protected DatabaseAccessType accessType;
  protected String accessTypeValue;
  @XmlElement( required = true )
  protected DatabaseConnection.Attributes attributes;
  protected boolean changed;
  protected String connectSql;
  @XmlElement( required = true )
  protected DatabaseConnection.ConnectionPoolingProperties connectionPoolingProperties;
  protected String dataTablespace;
  protected String databaseName;
  protected String databasePort;
  @XmlElement( namespace = "http://www.pentaho.com/schema/" )
  protected DatabaseType databaseType;
  @XmlElement( required = true )
  protected DatabaseConnection.ExtraOptions extraOptions;
  @XmlElement( required = true )
  protected DatabaseConnection.ExtraOptionsOrder extraOptionsOrder;
  protected boolean forcingIdentifiersToLowerCase;
  protected boolean forcingIdentifiersToUpperCase;
  protected String hostname;
  protected String id;
  protected String indexTablespace;
  protected String informixServername;
  protected int initialPoolSize;
  protected int maximumPoolSize;
  protected String name;
  protected boolean partitioned;
  @XmlElement( nillable = true )
  protected List<PartitionDatabaseMeta> partitioningInformation;
  @XmlJavaTypeAdapter( PasswordEncryptionAdapter.class )
  protected String password;
  protected boolean quoteAllFields;
  @XmlElement( name = "SQLServerInstance" )
  protected String sqlServerInstance;
  protected boolean streamingResults;
  protected String username;
  protected boolean usingConnectionPool;
  protected boolean usingDoubleDecimalAsSchemaTableSeparator;

  /**
   * Gets the value of the accessType property.
   *
   * @return possible object is {@link DatabaseAccessType }
   */
  public DatabaseAccessType getAccessType() {
    return accessType;
  }

  /**
   * Sets the value of the accessType property.
   *
   * @param value allowed object is {@link DatabaseAccessType }
   */
  public void setAccessType( DatabaseAccessType value ) {
    this.accessType = value;
  }

  /**
   * Gets the value of the accessTypeValue property.
   *
   * @return possible object is {@link String }
   */
  public String getAccessTypeValue() {
    return accessTypeValue;
  }

  /**
   * Sets the value of the accessTypeValue property.
   *
   * @param value allowed object is {@link String }
   */
  public void setAccessTypeValue( String value ) {
    this.accessTypeValue = value;
  }

  /**
   * Gets the value of the attributes property.
   *
   * @return possible object is {@link DatabaseConnection.Attributes }
   */
  public DatabaseConnection.Attributes getAttributes() {
    return attributes;
  }

  /**
   * Sets the value of the attributes property.
   *
   * @param value allowed object is {@link DatabaseConnection.Attributes }
   */
  public void setAttributes( DatabaseConnection.Attributes value ) {
    this.attributes = value;
  }

  /**
   * Gets the value of the changed property.
   */
  public boolean isChanged() {
    return changed;
  }

  /**
   * Sets the value of the changed property.
   */
  public void setChanged( boolean value ) {
    this.changed = value;
  }

  /**
   * Gets the value of the connectSql property.
   *
   * @return possible object is {@link String }
   */
  public String getConnectSql() {
    return connectSql;
  }

  /**
   * Sets the value of the connectSql property.
   *
   * @param value allowed object is {@link String }
   */
  public void setConnectSql( String value ) {
    this.connectSql = value;
  }

  /**
   * Gets the value of the connectionPoolingProperties property.
   *
   * @return possible object is {@link DatabaseConnection.ConnectionPoolingProperties }
   */
  public DatabaseConnection.ConnectionPoolingProperties getConnectionPoolingProperties() {
    return connectionPoolingProperties;
  }

  /**
   * Sets the value of the connectionPoolingProperties property.
   *
   * @param value allowed object is {@link DatabaseConnection.ConnectionPoolingProperties }
   */
  public void setConnectionPoolingProperties( DatabaseConnection.ConnectionPoolingProperties value ) {
    this.connectionPoolingProperties = value;
  }

  /**
   * Gets the value of the dataTablespace property.
   *
   * @return possible object is {@link String }
   */
  public String getDataTablespace() {
    return dataTablespace;
  }

  /**
   * Sets the value of the dataTablespace property.
   *
   * @param value allowed object is {@link String }
   */
  public void setDataTablespace( String value ) {
    this.dataTablespace = value;
  }

  /**
   * Gets the value of the databaseName property.
   *
   * @return possible object is {@link String }
   */
  public String getDatabaseName() {
    return databaseName;
  }

  /**
   * Sets the value of the databaseName property.
   *
   * @param value allowed object is {@link String }
   */
  public void setDatabaseName( String value ) {
    this.databaseName = value;
  }

  /**
   * Gets the value of the databasePort property.
   *
   * @return possible object is {@link String }
   */
  public String getDatabasePort() {
    return databasePort;
  }

  /**
   * Sets the value of the databasePort property.
   *
   * @param value allowed object is {@link String }
   */
  public void setDatabasePort( String value ) {
    this.databasePort = value;
  }

  /**
   * Gets the value of the databaseType property.
   *
   * @return possible object is {@link DatabaseType }
   */
  public DatabaseType getDatabaseType() {
    return databaseType;
  }

  /**
   * Sets the value of the databaseType property.
   *
   * @param value allowed object is {@link DatabaseType }
   */
  public void setDatabaseType( DatabaseType value ) {
    this.databaseType = value;
  }

  /**
   * Gets the value of the extraOptions property.
   *
   * @return possible object is {@link DatabaseConnection.ExtraOptions }
   */
  public DatabaseConnection.ExtraOptions getExtraOptions() {
    return extraOptions;
  }

  /**
   * Sets the value of the extraOptions property.
   *
   * @param value allowed object is {@link DatabaseConnection.ExtraOptions }
   */
  public void setExtraOptions( DatabaseConnection.ExtraOptions value ) {
    this.extraOptions = value;
  }

  /**
   * Gets the value of the extraOptionsOrder property.
   *
   * @return possible object is {@link DatabaseConnection.ExtraOptionsOrder }
   */
  public DatabaseConnection.ExtraOptionsOrder getExtraOptionsOrder() {
    return extraOptionsOrder;
  }

  /**
   * Sets the value of the extraOptionsOrder property.
   *
   * @param value allowed object is {@link DatabaseConnection.ExtraOptionsOrder }
   */
  public void setExtraOptionsOrder( DatabaseConnection.ExtraOptionsOrder value ) {
    this.extraOptionsOrder = value;
  }

  /**
   * Gets the value of the forcingIdentifiersToLowerCase property.
   */
  public boolean isForcingIdentifiersToLowerCase() {
    return forcingIdentifiersToLowerCase;
  }

  /**
   * Sets the value of the forcingIdentifiersToLowerCase property.
   */
  public void setForcingIdentifiersToLowerCase( boolean value ) {
    this.forcingIdentifiersToLowerCase = value;
  }

  /**
   * Gets the value of the forcingIdentifiersToUpperCase property.
   */
  public boolean isForcingIdentifiersToUpperCase() {
    return forcingIdentifiersToUpperCase;
  }

  /**
   * Sets the value of the forcingIdentifiersToUpperCase property.
   */
  public void setForcingIdentifiersToUpperCase( boolean value ) {
    this.forcingIdentifiersToUpperCase = value;
  }

  /**
   * Gets the value of the hostname property.
   *
   * @return possible object is {@link String }
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * Sets the value of the hostname property.
   *
   * @param value allowed object is {@link String }
   */
  public void setHostname( String value ) {
    this.hostname = value;
  }

  /**
   * Gets the value of the id property.
   *
   * @return possible object is {@link String }
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of the id property.
   *
   * @param value allowed object is {@link String }
   */
  public void setId( String value ) {
    this.id = value;
  }

  /**
   * Gets the value of the indexTablespace property.
   *
   * @return possible object is {@link String }
   */
  public String getIndexTablespace() {
    return indexTablespace;
  }

  /**
   * Sets the value of the indexTablespace property.
   *
   * @param value allowed object is {@link String }
   */
  public void setIndexTablespace( String value ) {
    this.indexTablespace = value;
  }

  /**
   * Gets the value of the informixServername property.
   *
   * @return possible object is {@link String }
   */
  public String getInformixServername() {
    return informixServername;
  }

  /**
   * Sets the value of the informixServername property.
   *
   * @param value allowed object is {@link String }
   */
  public void setInformixServername( String value ) {
    this.informixServername = value;
  }

  /**
   * Gets the value of the initialPoolSize property.
   */
  public int getInitialPoolSize() {
    return initialPoolSize;
  }

  /**
   * Sets the value of the initialPoolSize property.
   */
  public void setInitialPoolSize( int value ) {
    this.initialPoolSize = value;
  }

  /**
   * Gets the value of the maximumPoolSize property.
   */
  public int getMaximumPoolSize() {
    return maximumPoolSize;
  }

  /**
   * Sets the value of the maximumPoolSize property.
   */
  public void setMaximumPoolSize( int value ) {
    this.maximumPoolSize = value;
  }

  /**
   * Gets the value of the name property.
   *
   * @return possible object is {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value allowed object is {@link String }
   */
  public void setName( String value ) {
    this.name = value;
  }

  /**
   * Gets the value of the partitioned property.
   */
  public boolean isPartitioned() {
    return partitioned;
  }

  /**
   * Sets the value of the partitioned property.
   */
  public void setPartitioned( boolean value ) {
    this.partitioned = value;
  }

  /**
   * Gets the value of the partitioningInformation property.
   * <p/>
   * <p/>
   * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
   * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
   * the partitioningInformation property.
   * <p/>
   * <p/>
   * For example, to add a new item, do as follows:
   * <p/>
   * <pre>
   * getPartitioningInformation().add( newItem );
   * </pre>
   * <p/>
   * <p/>
   * <p/>
   * Objects of the following type(s) are allowed in the list {@link PartitionDatabaseMeta }
   */
  public List<PartitionDatabaseMeta> getPartitioningInformation() {
    if ( partitioningInformation == null ) {
      partitioningInformation = new ArrayList<>();
    }
    return this.partitioningInformation;
  }

  /**
   * Gets the value of the password property.
   *
   * @return possible object is {@link String }
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the value of the password property.
   *
   * @param value allowed object is {@link String }
   */
  public void setPassword( String value ) {
    this.password = value;
  }

  /**
   * Gets the value of the quoteAllFields property.
   */
  public boolean isQuoteAllFields() {
    return quoteAllFields;
  }

  /**
   * Sets the value of the quoteAllFields property.
   */
  public void setQuoteAllFields( boolean value ) {
    this.quoteAllFields = value;
  }

  /**
   * Gets the value of the sqlServerInstance property.
   *
   * @return possible object is {@link String }
   */
  public String getSQLServerInstance() {
    return sqlServerInstance;
  }

  /**
   * Sets the value of the sqlServerInstance property.
   *
   * @param value allowed object is {@link String }
   */
  public void setSQLServerInstance( String value ) {
    this.sqlServerInstance = value;
  }

  /**
   * Gets the value of the streamingResults property.
   */
  public boolean isStreamingResults() {
    return streamingResults;
  }

  /**
   * Sets the value of the streamingResults property.
   */
  public void setStreamingResults( boolean value ) {
    this.streamingResults = value;
  }

  /**
   * Gets the value of the username property.
   *
   * @return possible object is {@link String }
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the value of the username property.
   *
   * @param value allowed object is {@link String }
   */
  public void setUsername( String value ) {
    this.username = value;
  }

  /**
   * Gets the value of the usingConnectionPool property.
   */
  public boolean isUsingConnectionPool() {
    return usingConnectionPool;
  }

  /**
   * Sets the value of the usingConnectionPool property.
   */
  public void setUsingConnectionPool( boolean value ) {
    this.usingConnectionPool = value;
  }

  /**
   * Gets the value of the usingDoubleDecimalAsSchemaTableSeparator property.
   */
  public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
    return usingDoubleDecimalAsSchemaTableSeparator;
  }

  /**
   * Sets the value of the usingDoubleDecimalAsSchemaTableSeparator property.
   */
  public void setUsingDoubleDecimalAsSchemaTableSeparator( boolean value ) {
    this.usingDoubleDecimalAsSchemaTableSeparator = value;
  }

  /**
   * <p>Defining an inner class to support the attributes'Map object from the original class.</p>
   * {@inheritDoc}
   */
  public static class Attributes extends MapExport {
  }

  /**
   * <p>Defining an inner class to support the connectionPoolingProperties'Map object from the original class.</p>
   * {@inheritDoc}
   */
  public static class ConnectionPoolingProperties extends MapExport {
  }

  /**
   * <p>Defining an inner class to support the extraOptions'Map object from the original class.</p>
   * {@inheritDoc}
   */
  public static class ExtraOptions extends MapExport {
  }

  /**
   * <p>Defining an inner class to support the extraOptionsOrder'Map object from the original class.</p>
   * {@inheritDoc}
   */
  public static class ExtraOptionsOrder extends MapExport {
  }

  /**
   * An implementation of {@link XmlAdapter} that allows us to have passwords encrypted on serialization.
   */
  public static class PasswordEncryptionAdapter extends XmlAdapter<String, String> {
    @Override
    public String marshal( final String plainText ) throws Exception {
      return Encr.encryptPasswordIfNotUsingVariables( plainText );
    }

    @Override
    public String unmarshal( final String encodedText ) throws Exception {
      return Encr.decryptPasswordOptionallyEncrypted( encodedText );
    }
  }
}
