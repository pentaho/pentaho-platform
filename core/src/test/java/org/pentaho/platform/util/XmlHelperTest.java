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


package org.pentaho.platform.util;

import net.sf.saxon.Configuration;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith( JUnit4.class )
public class XmlHelperTest {

  private static final String TEST_ACTION_URL = "http://localhost:8080/pentaho/ViewAction?";
  private static final String TEST_BASE_URL = "http://localhost:8080/pentaho";
  private static final String TEST_DISPLAY_URL = "http://localhost:8080/pentaho";

  @BeforeClass
  public static void setupSaxonExtensions() {
    Configuration config = XMLParserFactoryProducer.getSaxonConfig();
    BogusXactionSaxonExtensions.registerAll( config );
  }

  @Test
  public void testGetEncoding_Valid() {
    // these should succeed, and cause the specified (windows-1252) encoding to be returned
    String[] winXmls = { "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>",
      "<?xml encoding=\"windows-1252\" version=\"1.0\"?><root></root>",
      "<?xml encoding=\"windows-1252\" version='1.0'?><root></root>",
      "<?xml encoding='windows-1252' version=\"1.0\"?><root></root>",
      "<?xml encoding='windows-1252' version='1.0'?><root></root>"
    };

    for ( String element : winXmls ) {
      assertEquals( "windows-1252", XmlHelper.getEncoding( element ) );
    }
  }

  @Test
  public void testGetEncoding_DefaultInsteadOfInvalid() {
    // these should fail, and cause the default system encoding to be returned
    String[] defaultXmls = { "<?xml encoding='UTF-8' version='1.0'?><root></root>",
      "<?xml encoding='UTF-8' version='1.0'?><root></root>",
      "<?xml encoding='UTF-8' version=\"1.0\"?><root></root>",
      "<?xml encoding='UTF-8' version='1.0'?><root>encoding=bad</root>"
    };

    String systemEncoding = LocaleHelper.getSystemEncoding();
    for ( String element : defaultXmls ) {
      assertEquals( systemEncoding, XmlHelper.getEncoding( element ) );
    }
  }

  /**
   * Load an XML file into a dom4j.Document, convert that document to a string, load that string into a w3c.Document,
   * and turn it back into a string.
   */
  @Test
  public void testGetXMLFromDocument() throws Exception {
    InputStream in = prepareSampleXmlStream();
    org.dom4j.Document doc = XmlDom4JHelper.getDocFromStream( in );
    org.w3c.dom.Document w3cDoc = XmlW3CHelper.getDomFromString( doc.asXML() );

    String converted = XmlDom4JHelper.docToString( w3cDoc ).toString();
    assertFalse( StringUtils.isEmpty( converted ) );
  }

  @Test
  public void testEncode_NullStringIsEncodedToNull() {
    assertNull( XmlHelper.encode( (String) null ) );
  }

  @Test
  public void testEncode_NullArrayIsIgnored() {
    XmlHelper.encode( (String[]) null );
  }

  @Test
  public void testDecode_NullStringIsDecodedToNull() {
    assertNull( XmlHelper.decode( (String) null ) );
  }

  @Test
  public void testDecode_NullArrayIsIgnored() {
    XmlHelper.decode( (String[]) null );
  }

  @Test
  public void testEncodeDecode() {
    String encodedXml =
      " ABC 123 abc &amp;amp;=&amp; &amp;lt;=&lt; &amp;gt;=&gt; &amp;apos;=&apos; &amp;quot;=&quot; ABC123abc &amp;"
        + "quot = &quot; &amp;apos; = &apos; &amp;gt; = &gt; &amp;lt; = &lt; &amp;amp; = &amp; ABC123abc ";
    String decodedXml =
      " ABC 123 abc &amp;=& &lt;=< &gt;=> &apos;=' &quot;=\" ABC123abc &quot = \" &apos; = ' &gt; = > &lt; = < &amp; "
        + "= & ABC123abc ";
    assertEquals( "Error in decode", decodedXml, XmlHelper.decode( encodedXml ) );
    assertEquals( "Error in encode", encodedXml, XmlHelper.encode( decodedXml ) );
    assertEquals( "Error encoding after decoding", encodedXml, XmlHelper.encode( XmlHelper.decode( encodedXml ) ) );
    assertEquals( "Error decoding after encoding", decodedXml, XmlHelper.decode( XmlHelper.encode( decodedXml ) ) );
  }

  @Test
  public void testXForm() throws Exception {
    try (
      InputStream inStrm = new FileInputStream( "src/test/resources/solution/test/xml/XmlHelperTest1.xml" ) ) {
      String xslName = "CustomReportParametersForPortlet.xsl";
      String xslPath = "src/test/resources/solution/system/custom/xsl";

      StringBuffer b = XmlHelper.transformXml( xslName, xslPath, inStrm, null, new TestEntityResolver() );
      assertTrue( StringUtils.isNotEmpty( b.toString() ) );
    }
  }

  @Test( expected = XmlParseException.class )
  public void testFailureGetDocFromString() throws Exception {
    Document doc = XmlDom4JHelper.getDocFromString( "1231231231231", null );
    fail( "Unexpected XML parsing success" );
  }

  @Test
  public void testSuccessGetDocFromString() throws Exception {
    Document doc =
      XmlDom4JHelper.getDocFromString( "<root><contents><value>One</value><value>Two</value></contents></root>",
        null );
    assertNotNull( "Unexpected XML parsing failure", doc );
  }

  @Test
  public void testGetDocFromStream() throws Exception {
    InputStream in = prepareSampleXmlStream();
    Document doc = XmlDom4JHelper.getDocFromStream( in );
    assertNotNull( doc );
  }

  @Test
  public void testFailureTransformXML() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl";
    String xslPath = "system/custom/xsl";

    Document document =
      XmlDom4JHelper.getDocFromString( "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", null );
    assertEquals( "root", document.getRootElement().getName() );

    XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), getURLParameters(), new JarEntityResolver() );
  }

  @Test
  public void testFailureTransformXML2() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl";
    String xslPath = "system/custom/xsl";

    String xmlString =
      "<?xml version=\"1.0\" encoding=\"" + StandardCharsets.UTF_8.name()
        + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" +
        "<Header/>" + "<id><![CDATA[" +
        "]]></id><description><![CDATA[" +
        "MyDescription" + "]]></description><icon><![CDATA[" +
        "GetIcon" + "]]></icon><help><![CDATA[" +
        "GetHelp" + "]]></help>" +
        "<action><![CDATA[" + TEST_ACTION_URL + "]]></action>" +
        "<display><![CDATA[" + TEST_DISPLAY_URL + "]]></display>" +
        "<Body/>" + "</filters>";

    Document document = XmlDom4JHelper.getDocFromString( xmlString, null );

    XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), getURLParameters(), new JarEntityResolver() );
  }

  @Test
  public void testFailureTransformXML3() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl";
    String xslPath = "system/custom/xsl";


    Document document =
      XmlDom4JHelper.getDocFromString( "<?xml version=\"1.0\" encoding=\"" + StandardCharsets.UTF_8.name()
        + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" +
        "<Header/>" + "<id><![CDATA[" +
        "]]></id><description><![CDATA[" +
        "MyDescription" + "]]></description><icon><![CDATA[" +
        "GetIcon" + "]]></icon><help><![CDATA[" +
        "GetHelp" + "]]></help>" +
        "<action><![CDATA[" + TEST_ACTION_URL + "]]></action>" +
        "<display><![CDATA[" + TEST_DISPLAY_URL + "]]></display>" +
        "<Body/>" + "</filters>", null );

    XmlHelper.transformXml( parameterXsl, xslPath, new ByteArrayInputStream( document.asXML().getBytes() ),
      getURLParameters(),
      new JarEntityResolver() );
  }

  @Test
  public void testEncoding() throws Exception {
    doEncodingTest( "rootElement", "hello", StandardCharsets.UTF_8.name() );
    doEncodingTest( "rootElement", "hello", StandardCharsets.UTF_16.name() );
    String unicodeElementText = new String( new char[] { '\uAA93', '\uAA94', '\uAA95' } );
    doEncodingTest( "rootElement", unicodeElementText, StandardCharsets.UTF_8.name() );
    doEncodingTest( "rootElement", unicodeElementText, StandardCharsets.UTF_16.name() );
  }

  public void doEncodingTest( String rootElementName, String rootElementText, String encoding ) throws Exception {
    // Create the test document.
    Element rootElement = new DefaultElement( rootElementName );
    Document document = DocumentHelper.createDocument( rootElement );
    rootElement.setText( rootElementText );

    // Write out the document to a byte array.
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    XmlDom4JHelper.saveDom( document, outputStream, encoding );

    // Read in the document from the byte array, and make sure it's decoded properly.
    InputStream inputStream = new ByteArrayInputStream( outputStream.toByteArray() );

    // Read in the XML string using a java reader and make sure the decoded string
    // contains the same strings as the encoded dom4j document.
    BufferedReader in = new BufferedReader( new InputStreamReader( inputStream, encoding ) );
    StringBuffer stringBuffer = new StringBuffer();
    int intValue = in.read();
    while ( intValue != -1 ) {
      stringBuffer.append( (char) intValue );
      intValue = in.read();
    }
    assertTrue( stringBuffer.toString().indexOf( rootElementName ) != -1 );
    assertTrue( stringBuffer.toString().indexOf( rootElementText ) != -1 );

    inputStream.reset();

    // Read in the XML string using the dom4j api and make sure the decoded xml
    // contains the same strings as the original document.
    document = XmlDom4JHelper.getDocFromStream( inputStream );
    assertEquals( rootElementName, document.getRootElement().getName() );
    assertEquals( rootElementText, document.getRootElement().getText() );
  }

  private static InputStream prepareSampleXmlStream() {
    String sampleXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
      + "<report name=\"Quadrant For Region\" orientation=\"portrait\" topmargin=\"0pt\" leftmargin=\"5pt\" "
      + "bottommargin=\"0pt\" rightmargin=\"5pt\" />";

    return new ByteArrayInputStream( sampleXml.getBytes() );
  }

  private static Map<String, String> getURLParameters() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put( "baseUrl", TEST_BASE_URL );
    parameters.put( "actionUrl", TEST_ACTION_URL );
    parameters.put( "displayUrl", TEST_DISPLAY_URL );
    return parameters;
  }
}
