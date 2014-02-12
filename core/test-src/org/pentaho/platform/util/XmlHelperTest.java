/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.util;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.xml.XmlHelper;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.pentaho.platform.util.xml.w3c.XmlW3CHelper;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings( { "all" } )
public class XmlHelperTest extends TestCase {

  public void testGetEncoding() {
    // these should succeed, and cause the specified (windows-1252) encoding to be returned
    String[] winXmls = { "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding=\"windows-1252\" version=\"1.0\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding=\"windows-1252\" version='1.0'?><root></root>", //$NON-NLS-1$
      "<?xml encoding='windows-1252' version=\"1.0\"?><root></root>", //$NON-NLS-1$
      "<?xml encoding='windows-1252' version='1.0'?><root></root>" }; //$NON-NLS-1$

    // these should fail, and cause the default system encoding to be returned
    String[] defaultXmls = { "<?xml encoding='UTF-8' version='1.0'?><root></root>", //$NON-NLS-1$ 
      "<?xml encoding='UTF-8' version='1.0'?><root></root>", "<?xml encoding='UTF-8' version=\"1.0\"?><root></root>", //$NON-NLS-1$ //$NON-NLS-2$
      "<?xml encoding='UTF-8' version='1.0'?><root>encoding=bad</root>" }; //$NON-NLS-1$ 

    for ( String element : winXmls ) {
      String enc = XmlHelper.getEncoding( element );
      System.out.println( "xml: " + element + " enc: " + enc ); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue( enc.equals( "windows-1252" ) ); //$NON-NLS-1$
    }

    for ( String element : defaultXmls ) {
      String enc = XmlHelper.getEncoding( element );
      System.out.println( "LocaleHelper.getSystemEncoding() =   " + LocaleHelper.getSystemEncoding() );
      System.out.println( "xml encoding: " + element + " enc: " + enc ); //$NON-NLS-1$ //$NON-NLS-2$
      Assert.assertTrue( enc.equals( LocaleHelper.getSystemEncoding() ) );
    }
  }

  /**
   * Load an XML file into a dom4j.Document, convert that document to a string, load that string into a
   * w3c.Document, and turn it back into a string.
   * 
   * @throws FileNotFoundException
   * @throws TransformerConfigurationException
   * @throws TransformerException
   */
  public void testGetXMLFromDocument() throws FileNotFoundException, TransformerConfigurationException,
    TransformerException {
    try {
      InputStream in = new FileInputStream( "test-res/solution/test/xml/JFreeQuadrantForRegion.xml" );
      org.dom4j.Document doc = XmlDom4JHelper.getDocFromStream( in );

      org.w3c.dom.Document w3cDoc = XmlW3CHelper.getDomFromString( doc.asXML() );

      StringBuffer strBuff = XmlDom4JHelper.docToString( w3cDoc );
      System.out.println( strBuff.toString() );
      Assert.assertTrue( !StringUtils.isEmpty( strBuff.toString() ) );
    } catch ( Throwable e ) {
      System.out.println( "Exception thrown " + e.getMessage() ); //$NON-NLS-1$
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testEncodeDecode() {
    String emptyString = null;
    Assert.assertNull( "decode(null) Should return null", XmlHelper.decode( emptyString ) ); //$NON-NLS-1$
    Assert.assertNull( "encode(null) Should return null", XmlHelper.encode( emptyString ) ); //$NON-NLS-1$

    String[] emptyArray = null;
    try {
      XmlHelper.decode( emptyArray );
    } catch ( Throwable t ) {
      Assert.fail( "decode with a null array should not throw exception: " + t.getMessage() ); //$NON-NLS-1$
    }
    try {
      XmlHelper.encode( emptyArray );
    } catch ( Throwable t ) {
      Assert.fail( "encode with a null array should not throw exception: " + t.getMessage() ); //$NON-NLS-1$
    }

    String encodedXml =
        " ABC 123 abc &amp;amp;=&amp; &amp;lt;=&lt; &amp;gt;=&gt; &amp;apos;=&apos; &amp;quot;=&quot; ABC123abc &amp;quot = &quot; &amp;apos; = &apos; &amp;gt; = &gt; &amp;lt; = &lt; &amp;amp; = &amp; ABC123abc "; //$NON-NLS-1$
    String decodedXml =
        " ABC 123 abc &amp;=& &lt;=< &gt;=> &apos;=' &quot;=\" ABC123abc &quot = \" &apos; = ' &gt; = > &lt; = < &amp; = & ABC123abc "; //$NON-NLS-1$
    Assert.assertEquals( "Error in decode", decodedXml, XmlHelper.decode( encodedXml ) ); //$NON-NLS-1$
    Assert.assertEquals( "Error in encode", encodedXml, XmlHelper.encode( decodedXml ) ); //$NON-NLS-1$
    Assert
        .assertEquals( "Error encoding after decoding", encodedXml, XmlHelper.encode( XmlHelper.decode( encodedXml ) ) ); //$NON-NLS-1$
    Assert
        .assertEquals( "Error decoding after encoding", decodedXml, XmlHelper.decode( XmlHelper.encode( decodedXml ) ) ); //$NON-NLS-1$
  }

  //Does not test functionality in the current code base
  /*public void testXForm() throws TransformerException {
    try {
      InputStream inStrm = new FileInputStream( "test-res/solution/test/xml/XmlHelperTest1.xml" ); //$NON-NLS-1$
      String xslName = "CustomReportParametersForPortlet.xsl"; //$NON-NLS-1$
      String xslPath = "test-res/solution/system/custom/xsl"; //$NON-NLS-1$

      StringBuffer b = XmlHelper.transformXml( xslName, xslPath, inStrm, null, new TestEntityResolver() );
      Assert.assertTrue( !StringUtils.isEmpty( b.toString() ) );
    } catch ( Throwable e ) {
      System.out.println( "Exception thrown " + e.getMessage() ); //$NON-NLS-1$
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }*/

  public void testFailureGetDocFromString() {
    try {
      Document doc = XmlDom4JHelper.getDocFromString( "1231231231231", null ); //$NON-NLS-1$
      Assert.assertTrue( "Unexpected XML parsing success", doc == null );
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), true ); //$NON-NLS-1$
    }
  }

  public void testSuccessGetDocFromString() {
    try {
      Document doc =
          XmlDom4JHelper.getDocFromString(
              "<root><contents><value>One</value><value>Two</value></contents></root>", null ); //$NON-NLS-1$
      Assert.assertTrue( "Unexpected XML parsing failure", doc != null );
      System.out.println( "Document as String" + doc.getStringValue() ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testGetDocFromString() {
    try {
      InputStream in = new FileInputStream( "test-res/solution/test/xml/index.xml" );
      Document doc = XmlDom4JHelper.getDocFromStream( in );
      System.out.println( "Document as String" + doc.getStringValue() ); //$NON-NLS-1$      
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testFailureTransformXML() {
    try {
      String parameterXsl = "DefaultParameterForm.xsl"; //$NON-NLS-1$
      String xslPath = "system/custom/xsl";

      Map parameters = new HashMap();
      String actionUrl = "http://localhost:8080/pentaho/ViewAction?"; //$NON-NLS-1$
      String baseUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
      String displayUrl = "http://localhost:8080/pentaho"; //$NON-NLS-1$
      parameters.put( "baseUrl", baseUrl ); //$NON-NLS-1$ 
      parameters.put( "actionUrl", actionUrl ); //$NON-NLS-1$ 
      parameters.put( "displayUrl", displayUrl ); //$NON-NLS-1$ 

      //Document document = XmlHelper.getDocFromString("<?xml encoding=\"windows-1252\" version=\"1.0\"?><root></root>"); //$NON-NLS-1$
      Document document =
          XmlDom4JHelper.getDocFromString( "<?xml version=\"1.0\" encoding=\"windows-1252\"?><root></root>", null ); //$NON-NLS-1$
      Assert.assertEquals( document.getRootElement().getName(), "root" ); //$NON-NLS-1$

      StringBuffer content =
          XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), parameters, new JarEntityResolver() );
      System.out.println( "Transformed XML" + content ); //$NON-NLS-1$

    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testFailureTransformXML2() {
    try {
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
          "<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
              "Header" + "<id><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
              "]]></id><description><![CDATA[" + //$NON-NLS-1$
              "MyDescription" + "]]></description><icon><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
              "GetIcon" + "]]></icon><help><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
              "GetHelp" + "]]></help>" + //$NON-NLS-1$ //$NON-NLS-2$
              "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
              "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
              "Body" + "</filters>"; //$NON-NLS-1$ //$NON-NLS-2$

      Document document = XmlDom4JHelper.getDocFromString( xmlString, null );

      StringBuffer content =
          XmlHelper.transformXml( parameterXsl, xslPath, document.asXML(), parameters, new JarEntityResolver() );
      System.out.println( "Transformed XML" + content ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void testEncoding() {
    String unicodeElementText = new String( new char[] { '\uAA93', '\uAA94', '\uAA95' } );
    try {
      doEncodingTest( "rootElement", "hello", "UTF-8" );
      doEncodingTest( "rootElement", "hello", "UTF-16" );
      doEncodingTest( "rootElement", unicodeElementText, "UTF-8" );
      doEncodingTest( "rootElement", unicodeElementText, "UTF-16" );
    } catch ( Exception e ) {
      fail();
    }
  }

  public void testFailureTransformXML3() {
    try {
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
          XmlDom4JHelper
              .getDocFromString(
                  "<?xml version=\"1.0\" encoding=\"" + "UTF-8" + "\" ?><filters xmlns:xf=\"http://www.w3.org/2002/xforms\">" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
                      "Header" + "<id><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
                      "]]></id><description><![CDATA[" + //$NON-NLS-1$
                      "MyDescription" + "]]></description><icon><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
                      "GetIcon" + "]]></icon><help><![CDATA[" + //$NON-NLS-1$ //$NON-NLS-2$
                      "GetHelp" + "]]></help>" + //$NON-NLS-1$ //$NON-NLS-2$
                      "<action><![CDATA[" + actionUrl + "]]></action>" + //$NON-NLS-1$ //$NON-NLS-2$
                      "<display><![CDATA[" + displayUrl + "]]></display>" + //$NON-NLS-1$ //$NON-NLS-2$
                      "Body" + "</filters>", null ); //$NON-NLS-1$ //$NON-NLS-2$

      StringBuffer content =
          XmlHelper.transformXml( parameterXsl, xslPath, new ByteArrayInputStream( document.asXML().getBytes() ),
              parameters, new JarEntityResolver() );
      System.out.println( "Transformed XML" + content ); //$NON-NLS-1$
    } catch ( Exception e ) {
      e.printStackTrace();
      Assert.assertTrue( "Exception thrown " + e.getMessage(), false ); //$NON-NLS-1$
    }
  }

  public void doEncodingTest( String rootElementName, String rootElementText, String encoding ) throws IOException,
    DocumentException {
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
    System.out.println( document.asXML() );
    assertEquals( rootElementName, document.getRootElement().getName() );
    assertEquals( rootElementText, document.getRootElement().getText() );
  }

  public static void main( final String[] args ) {
  }

}
