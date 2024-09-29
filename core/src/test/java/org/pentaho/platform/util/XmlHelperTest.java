/*!
 *
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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.junit.Assert;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

import junit.framework.TestCase;

@SuppressWarnings( { "all" } )
public class XmlHelperTest extends TestCase {

  public void testGetEncoding_Valid() {
    // these should succeed, and cause the specified (windows-1252) encoding to be returned
    String[] winXmls = { "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding=\"windows-1252\" version=\"1.0\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding=\"windows-1252\" version='1.0'?><root></root>", //$NON-NLS-1$
      "<?xml encoding='windows-1252' version=\"1.0\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding='windows-1252' version='1.0'?><root></root>" //$NON-NLS-1$
    };

    for ( String element : winXmls ) {
      String enc = XmlHelper.getEncoding( element );
      assertTrue( enc.equals( "windows-1252" ) ); //$NON-NLS-1$
    }
  }

  public void testGetEncoding_DefaultInsteadOfInvalid() {
    // these should fail, and cause the default system encoding to be returned
    String[] defaultXmls = { "<?xml encoding='UTF-8' version='1.0'?><root></root>", //$NON-NLS-1$
      "<?xml encoding='UTF-8' version='1.0'?><root></root>", "<?xml encoding='UTF-8' version=\"1.0\"?><root></root>",
      // $NON-NLS-1$ //$NON-NLS-2$
      "<?xml encoding='UTF-8' version='1.0'?><root>encoding=bad</root>" //$NON-NLS-1$
    };

    for ( String element : defaultXmls ) {
      String enc = XmlHelper.getEncoding( element );
      assertTrue( enc.equals( LocaleHelper.getSystemEncoding() ) );
    }
  }

  /**
   * Load an XML file into a dom4j.Document, convert that document to a string, load that string into a w3c.Document,
   * and turn it back into a string.
   * 
   * @throws FileNotFoundException
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  public void testGetXMLFromDocument() throws Exception {
    InputStream in = prepareSampleXmlStream();
    org.dom4j.Document doc = XmlDom4JHelper.getDocFromStream( in );
    org.w3c.dom.Document w3cDoc = XmlW3CHelper.getDomFromString( doc.asXML() );

    String converted = XmlDom4JHelper.docToString( w3cDoc ).toString();
    assertFalse( StringUtils.isEmpty( converted ) );
  }

  public void testEncode_NullStringIsEncodedToNull() {
    assertNull( XmlHelper.encode( (String) null ) );
  }

  public void testEncode_NullArrayIsIgnored() {
    XmlHelper.encode( (String[]) null );
  }

  public void testDecode_NullStringIsDecodedToNull() {
    assertNull( XmlHelper.decode( (String) null ) );
  }

  public void testDecode_NullArrayIsIgnored() {
    XmlHelper.decode( (String[]) null );
  }

  public void testEncodeDecode() {
    String encodedXml =
        " ABC 123 abc &amp;amp;=&amp; &amp;lt;=&lt; &amp;gt;=&gt; &amp;apos;=&apos; &amp;quot;=&quot; ABC123abc &amp;"
            + "quot = &quot; &amp;apos; = &apos; &amp;gt; = &gt; &amp;lt; = &lt; &amp;amp; = &amp; ABC123abc "; //$NON-NLS-1$
    String decodedXml =
        " ABC 123 abc &amp;=& &lt;=< &gt;=> &apos;=' &quot;=\" ABC123abc &quot = \" &apos; = ' &gt; = > &lt; = < &amp; "
            + "= & ABC123abc "; //$NON-NLS-1$
    assertEquals( "Error in decode", decodedXml, XmlHelper.decode( encodedXml ) ); //$NON-NLS-1$
    assertEquals( "Error in encode", encodedXml, XmlHelper.encode( decodedXml ) ); //$NON-NLS-1$
    assertEquals( "Error encoding after decoding", encodedXml, XmlHelper.encode( XmlHelper.decode( encodedXml ) ) ); // $NON-NLS-1$
    assertEquals( "Error decoding after encoding", decodedXml, XmlHelper.decode( XmlHelper.encode( decodedXml ) ) ); // $NON-NLS-1$
  }

  public void testXForm() throws TransformerException {
    try {
      InputStream inStrm = new FileInputStream( "src/test/resources/solution/test/xml/XmlHelperTest1.xml" ); //$NON-NLS-1$
      String xslName = "CustomReportParametersForPortlet.xsl"; //$NON-NLS-1$
      String xslPath = "src/test/resources/solution/system/custom/xsl"; //$NON-NLS-1$

      StringBuffer b = XmlHelper.transformXml( xslName, xslPath, inStrm, null, new TestEntityResolver() );
      Assert.assertTrue( StringUtils.isNotEmpty( b.toString() ) );
    } catch ( Throwable e ) {
      System.out.println( "Exception thrown " + e.getMessage() ); //$NON-NLS-1$
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testFailureGetDocFromString() {
    try {
      Document doc = XmlDom4JHelper.getDocFromString( "1231231231231", null ); //$NON-NLS-1$
      fail( "Unexpected XML parsing success" );
    } catch ( Exception e ) {
      assertTrue( "Exception thrown " + e.getMessage(), true ); //$NON-NLS-1$
    }
  }

  public void testSuccessGetDocFromString() throws Exception {
    Document doc =
        XmlDom4JHelper.getDocFromString( "<root><contents><value>One</value><value>Two</value></contents></root>", //$NON-NLS-1$
            null );
    assertNotNull( "Unexpected XML parsing failure", doc );
  }

  public void testGetDocFromStream() throws Exception {
    InputStream in = prepareSampleXmlStream();
    Document doc = XmlDom4JHelper.getDocFromStream( in );
    assertNotNull( doc );
  }

  public void testFailureTransformXML() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl"; //$NON-NLS-1$
    String xslPath = "system/custom/xsl";

    Map<String, String> parameters = new HashMap<String, String>();
    String actionUrl = "http://localhost:8080/pentaho/ViewAction?"; //$NON-NLS-1$
    String baseUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    String displayUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    parameters.put( "baseUrl", baseUrl ); //$NON-NLS-1$
    parameters.put( "actionUrl", actionUrl ); //$NON-NLS-1$
    parameters.put( "displayUrl", displayUrl ); // $NON-NLS-1$

    Document document =
        XmlDom4JHelper.getDocFromString( "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", null ); //$NON-NLS-1$
    assertEquals( document.getRootElement().getName(), "root" ); //$NON-NLS-1$

    XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), parameters, new JarEntityResolver() );
  }

  public void testFailureTransformXML2() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl"; //$NON-NLS-1$
    String xslPath = "system/custom/xsl";

    Map parameters = new HashMap();
    String actionUrl = "http://localhost:8080/pentaho/ViewAction?"; //$NON-NLS-1$
    String baseUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    String displayUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    parameters.put( "baseUrl", baseUrl ); //$NON-NLS-1$
    parameters.put( "actionUrl", actionUrl ); //$NON-NLS-1$
    parameters.put( "displayUrl", displayUrl ); //$NON-NLS-1$

    String xmlString =
        "<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + // $NON-NLS-1$
                                                                                                                      // //$NON-NLS-2$
                                                                                                                      // //$NON-NLS-3$
            "<Header/>" + "<id><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "]]></id><description><![CDATA[" + //$NON-NLS-1$
            "MyDescription" + "]]></description><icon><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "GetIcon" + "]]></icon><help><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "GetHelp" + "]]></help>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<Body/>" + "</filters>"; //$NON-NLS-1$ //$NON-NLS-2$

    Document document = XmlDom4JHelper.getDocFromString( xmlString, null );

    XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), parameters, new JarEntityResolver() );
  }

  public void testFailureTransformXML3() throws Exception {
    String parameterXsl = "DefaultParameterForm.xsl"; //$NON-NLS-1$
    String xslPath = "system/custom/xsl";

    Map parameters = new HashMap();
    String actionUrl = "http://localhost:8080/pentaho/ViewAction?"; //$NON-NLS-1$
    String baseUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    String displayUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
    parameters.put( "baseUrl", baseUrl ); //$NON-NLS-1$
    parameters.put( "actionUrl", actionUrl ); //$NON-NLS-1$
    parameters.put( "displayUrl", displayUrl ); //$NON-NLS-1$

    Document document =
        XmlDom4JHelper.getDocFromString( "<?xml version=\"1.0\" encoding=\"" + "UTF-8"
            + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + // $NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            "<Header/>" + "<id><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "]]></id><description><![CDATA[" + //$NON-NLS-1$
            "MyDescription" + "]]></description><icon><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "GetIcon" + "]]></icon><help><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
            "GetHelp" + "]]></help>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
            "<Body/>" + "</filters>", null ); //$NON-NLS-1$ //$NON-NLS-2$

    XmlHelper.transformXml( parameterXsl, xslPath, new ByteArrayInputStream( document.asXML().getBytes() ), parameters,
        new JarEntityResolver() );
  }

  public void testEncoding() throws Exception {
    doEncodingTest( "rootElement", "hello", "UTF-8" );
    doEncodingTest( "rootElement", "hello", "UTF-16" );
    String unicodeElementText = new String( new char[] { '\uAA93', '\uAA94', '\uAA95' } );
    doEncodingTest( "rootElement", unicodeElementText, "UTF-8" );
    doEncodingTest( "rootElement", unicodeElementText, "UTF-16" );
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
    String sampleXml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<report name=\"Quadrant For Region\" orientation=\"portrait\" topmargin=\"0pt\" leftmargin=\"5pt\" "
            + "bottommargin=\"0pt\" rightmargin=\"5pt\" />";

    return new ByteArrayInputStream( sampleXml.getBytes() );
  }

}
