/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.platform.plugin.services.importexport.exportManifest;

import org.junit.Test;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseConnection;
import org.pentaho.platform.plugin.services.importexport.exportManifest.bindings.DatabaseType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ExportManifestDatabaseTypeNamespaceTest {

  @Test
  public void testDeserializeDatabaseConnection_withNs2DatabaseTypeTag() throws Exception {
    DatabaseConnection databaseConnection = unmarshalDatabaseConnection( buildDatabaseConnectionXml( "ns2:databaseType" ) );

    assertDatabaseTypeWasDeserialized( databaseConnection );
  }

  @Test
  public void testDeserializeDatabaseConnection_withDatabaseTypeTag() throws Exception {
    DatabaseConnection databaseConnection = unmarshalDatabaseConnection( buildDatabaseConnectionXml( "databaseType" ) );

    assertDatabaseTypeWasDeserialized( databaseConnection );
  }

  @Test
  public void testSerializeAndDeserializeDatabaseConnection_roundTripPreservesDatabaseType() throws Exception {
    DatabaseConnection original = unmarshalDatabaseConnection( buildDatabaseConnectionXml( "ns2:databaseType" ) );
    assertDatabaseTypeWasDeserialized( original );

    String xml = marshalDatabaseConnection( original );
    assertNotNull( xml );
    assertFalse( xml.isEmpty() );
    assertFalse( xml.indexOf( "databaseType" ) < 0 );

    DatabaseConnection roundTrip = unmarshalDatabaseConnection( xml );
    assertDatabaseTypeWasDeserialized( roundTrip );
  }

  /**
   * Test that serialization generates correctly formatted &lt;databaseType&gt; elements.
   * This test confirms the current serialization behavior and prevents regression.
   * 
   * NOTE: JAXB currently serializes with namespace prefix (e.g., &lt;ns2:databaseType&gt;)
   * based on the @XmlElementDecl annotation in ObjectFactory.
   */
  @Test
  public void testSerializeDatabaseConnection_generatesDatabaseTypeElement() throws Exception {
    // Create a DatabaseConnection with databaseType
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( "TestConnection" );
    connection.setHostname( "localhost" );

    DatabaseType dbType = new DatabaseType();
    dbType.setName( "PostgreSQL" );
    dbType.setShortName( "POSTGRESQL" );
    connection.setDatabaseType( dbType );

    // Serialize to XML
    String serializedXml = marshalDatabaseConnection( connection );

    // Verify the serialized XML contains a databaseType element
    // JAXB serializes with namespace prefix: <ns2:databaseType> or similar
    assertTrue( "Serialized XML should contain databaseType element",
      serializedXml.contains( "databaseType>" ) );

    // Additional verification: ensure it can be deserialized properly
    DatabaseConnection deserializedConnection = unmarshalDatabaseConnection( serializedXml );
    assertNotNull( "DatabaseType should not be null after deserialization",
      deserializedConnection.getDatabaseType() );
    assertEquals( "DatabaseType name should match", "PostgreSQL",
      deserializedConnection.getDatabaseType().getName() );
  }

  private DatabaseConnection unmarshalDatabaseConnection( String xml ) throws Exception {
    JAXBContext context = JAXBContext.newInstance( "org.pentaho.platform.plugin.services.importexport.exportManifest.bindings" );
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Object unmarshalled = unmarshaller.unmarshal( new StringReader( xml ) );

    if ( unmarshalled instanceof JAXBElement ) {
      return ( (JAXBElement<DatabaseConnection>) unmarshalled ).getValue();
    }

    return (DatabaseConnection) unmarshalled;
  }

  private String marshalDatabaseConnection( DatabaseConnection databaseConnection ) throws Exception {
    JAXBContext context = JAXBContext.newInstance( "org.pentaho.platform.plugin.services.importexport.exportManifest.bindings" );
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

    StringWriter writer = new StringWriter();
    marshaller.marshal( new JAXBElement<>(
      new QName( "http://www.pentaho.com/schema/", "databaseConnection" ),
      DatabaseConnection.class,
      databaseConnection ), writer );

    return writer.toString();
  }

  private void assertDatabaseTypeWasDeserialized( DatabaseConnection databaseConnection ) {
    assertNotNull( databaseConnection );

    DatabaseType databaseType = databaseConnection.getDatabaseType();
    assertNotNull( databaseType );
    assertEquals( "MonetDB", databaseType.getName() );
    assertEquals( "MONETDB", databaseType.getShortName() );
  }

  private String buildDatabaseConnectionXml( String databaseTypeTagName ) {
    String databaseTypeOpenTag = "<" + databaseTypeTagName + ">";
    String databaseTypeCloseTag = "</" + databaseTypeTagName + ">";
    String childNsReset = "";

    if ( "databaseType".equals( databaseTypeTagName ) ) {
      databaseTypeOpenTag = "<databaseType xmlns=\"http://www.pentaho.com/schema/\">";
      databaseTypeCloseTag = "</databaseType>";
      childNsReset = " xmlns=\"\"";
    }

    return "<ns2:databaseConnection xmlns:ns2=\"http://www.pentaho.com/schema/\">"
      + "<accessType>NATIVE</accessType>"
      + "<attributes/>"
      + "<changed>false</changed>"
      + "<connectSql></connectSql>"
      + "<connectionPoolingProperties/>"
      + "<databaseName>pentaho-instaview</databaseName>"
      + "<databasePort>50006</databasePort>"
      + databaseTypeOpenTag
      + "<defaultDatabasePort" + childNsReset + ">50000</defaultDatabasePort>"
      + "<extraOptionsHelpUrl" + childNsReset + "></extraOptionsHelpUrl>"
      + "<name" + childNsReset + ">MonetDB</name>"
      + "<shortName" + childNsReset + ">MONETDB</shortName>"
      + "<supportedAccessTypes" + childNsReset + ">NATIVE</supportedAccessTypes>"
      + "<defaultOptions" + childNsReset + "/>"
      + databaseTypeCloseTag
      + "<extraOptions/>"
      + "<extraOptionsOrder/>"
      + "<forcingIdentifiersToLowerCase>false</forcingIdentifiersToLowerCase>"
      + "<forcingIdentifiersToUpperCase>false</forcingIdentifiersToUpperCase>"
      + "<hostname>localhost</hostname>"
      + "<id>84f658ec-7ff4-4dff-9fd1-e9544eb0f9cf</id>"
      + "<initialPoolSize>5</initialPoolSize>"
      + "<maximumPoolSize>10</maximumPoolSize>"
      + "<name>AgileBI</name>"
      + "<partitioned>false</partitioned>"
      + "<password>Encrypted 2be98afc86aa7f2e4cb14a17edb86abd8</password>"
      + "<quoteAllFields>false</quoteAllFields>"
      + "<streamingResults>false</streamingResults>"
      + "<username>monetdb</username>"
      + "<usingConnectionPool>false</usingConnectionPool>"
      + "<usingDoubleDecimalAsSchemaTableSeparator>false</usingDoubleDecimalAsSchemaTableSeparator>"
      + "</ns2:databaseConnection>";
  }
}
