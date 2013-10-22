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

package org.pentaho.platform.util.xml.dom4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.util.messages.Messages;
import org.xml.sax.EntityResolver;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// TODO sbarkdull, exernalize strings, comment methods

/**
 * A set of static methods to help in with: * the construction of XML DOM Documents (org.dom4j.Document) from
 * files, streams, and Strings * in the creation of XML DOM Documents as the result of an XSLT transform *
 * persisting of XML DOM documents to the file system or a <code>Writer</code>. * the encoding of a String of Xml
 * text
 * 
 * Design notes: This class should never have any dependencies (i.e. imports) on anything on org.pentaho or
 * com.pentaho or their decendant packages. In general, methods in the class should not attempt to handle
 * exceptions, but should let the exceptions propogate to the caller to be handled there. Please do not use
 * european-reuse in this class. One of the primary design goals for this class was to construct it in a way that
 * it could be used without change outside of the Pentaho platform. Related XML-helper type code that is dependant
 * on the platform should be moved "up" to XmlHelper.
 */
public class XmlDom4JHelper {

  private static final Log logger = LogFactory.getLog( XmlDom4JHelper.class );

  /**
   * Create a <code>Document</code> from <code>str</code>.
   * 
   * @param str
   *          String containing the XML that will be used to create the Document
   * @param resolver
   *          EntityResolver an instance of an EntityResolver that will resolve any external URIs. See the docs on
   *          EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in <code>strXml</code>.
   * @throws XmlParseException
   */
  public static Document getDocFromString( final String strXml, final EntityResolver resolver )
    throws XmlParseException {
    Document document = null;
    try {
      document = XmlDom4JHelper.getDocFromStream( new ByteArrayInputStream( strXml.getBytes() ), resolver );
    } catch ( DocumentException e ) {
      throw new XmlParseException( Messages.getInstance().getErrorString(
          "XmlDom4JHelper.ERROR_0001_UNABLE_TO_GET_DOCUMENT_FROM_STRING" ), e ); //$NON-NLS-1$
    } catch ( IOException e ) {
      throw new XmlParseException( Messages.getInstance().getErrorString(
          "XmlDom4JHelper.ERROR_0002_UNSUPPORTED_ENCODING" ), e ); //$NON-NLS-1$
    }
    return document;
  }

  /**
   * Create a <code>Document</code> from the contents of a file.
   * 
   * @param path
   *          String containing the path to the file containing XML that will be used to create the Document.
   * @param resolver
   *          EntityResolver an instance of an EntityResolver that will resolve any external URIs. See the docs on
   *          EntityResolver. null is an acceptable value.
   * @return <code>Document</code> initialized with the xml in <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException
   *           if the file doesn't exist
   */
  public static Document getDocFromFile( final File file, final EntityResolver resolver ) throws DocumentException,
    IOException {
    SAXReader reader = new SAXReader();
    if ( resolver != null ) {
      reader.setEntityResolver( resolver );
    }
    return reader.read( file );
  }

  /**
   * Create a <code>Document</code> from the contents of an input stream, where the input stream contains valid
   * XML.
   * 
   * @param inStream
   * @return
   * @throws DocumentException
   * @throws IOException
   */
  public static Document getDocFromStream( final InputStream inStream, final EntityResolver resolver )
    throws DocumentException, IOException {

    SAXReader reader = new SAXReader();
    if ( resolver != null ) {
      reader.setEntityResolver( resolver );
    }
    return reader.read( inStream );
  }

  /**
   * Create a <code>Document</code> from the contents of an input stream, where the input stream contains valid
   * XML.
   * 
   * @param inStream
   * @return
   * @throws DocumentException
   * @throws IOException
   */
  public static Document getDocFromStream( final InputStream inStream ) throws DocumentException, IOException {

    return XmlDom4JHelper.getDocFromStream( inStream, null );
  }

  /**
   * Use the transform specified by xslSrc and transform the document specified by docSrc, and return the resulting
   * document.
   * 
   * @param xslSrc
   *          StreamSrc containing the xsl transform
   * @param docSrc
   *          StreamSrc containing the document to be transformed
   * @param params
   *          Map of properties to set on the transform
   * @param resolver
   *          URIResolver instance to resolve URI's in the output document.
   * 
   * @return StringBuffer containing the XML results of the transform
   * @throws TransformerConfigurationException
   *           if the TransformerFactory fails to create a Transformer.
   * @throws TransformerException
   *           if actual transform fails.
   */
  protected static final StringBuffer transformXml( final StreamSource xslSrc, final StreamSource docSrc,
      final Map params, final URIResolver resolver ) throws TransformerConfigurationException, TransformerException {

    StringBuffer sb = null;
    StringWriter writer = new StringWriter();

    TransformerFactory tf = TransformerFactory.newInstance();
    if ( null != resolver ) {
      tf.setURIResolver( resolver );
    }
    // TODO need to look into compiling the XSLs...
    Transformer t = tf.newTransformer( xslSrc ); // can throw
    // TransformerConfigurationException
    // Start the transformation
    if ( params != null ) {
      Set<?> keys = params.keySet();
      Iterator<?> it = keys.iterator();
      String key, val;
      while ( it.hasNext() ) {
        key = (String) it.next();
        val = (String) params.get( key );
        if ( val != null ) {
          t.setParameter( key, val );
        }
      }
    }
    t.transform( docSrc, new StreamResult( writer ) ); // can throw
    // TransformerException
    sb = writer.getBuffer();

    return sb;
  }

  /**
   * Convert a W3C Document to a String.
   * 
   * Note: if you are working with a dom4j Document, you can use it's asXml() method.
   * 
   * @param doc
   *          org.w3c.dom.Document to be converted to a String.
   * @return String representing the XML document.
   * 
   * @throws TransformerConfigurationException
   *           If unable to get an instance of a Transformer
   * @throws TransformerException
   *           If the attempt to transform the document fails.
   */
  public static final StringBuffer docToString( final org.w3c.dom.Document doc )
    throws TransformerConfigurationException, TransformerException {

    StringBuffer sb = null;
    StringWriter writer = new StringWriter();

    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer t = tf.newTransformer(); // can throw
    // TransformerConfigurationException

    Source docSrc = new DOMSource( doc );
    t.transform( docSrc, new StreamResult( writer ) ); // can throw
    // TransformerException
    sb = writer.getBuffer();

    return sb;
  }

  // TODO sbarkdull, this code is duplicated in LocaleHelper
  /**
   * convert any character in the XML input (<code>rawValue</code>) whose code position is greater than or equal to
   * 0x080 to its Numeric Character Reference. For a description of Numeric Character References see:
   * http://www.w3.org/TR/html4/charset.html#h-5.3.1
   * 
   * @param rawValue
   *          String containing the XML to be encoded.
   * @return String containing the encoded XML
   */
  public static String getXmlEncodedString( final String rawValue ) {
    StringBuffer value = new StringBuffer();
    for ( int n = 0; n < rawValue.length(); n++ ) {
      int charValue = rawValue.charAt( n );
      if ( charValue >= 0x80 ) {
        value.append( "&#x" ); //$NON-NLS-1$
        value.append( Integer.toString( charValue, 0x10 ) );
        value.append( ";" ); //$NON-NLS-1$
      } else {
        value.append( (char) charValue );
      }
    }
    return value.toString();

  }

  /**
   * Write an XML document to a file using the specified character encoding.
   * 
   * @param doc
   *          Document to be written
   * @param outputStream
   *          the output stream
   * @param encoding
   *          String specifying the character encoding. Can be null, in which case the default encoding will be
   *          used. See http://java.sun.com/j2se/1.5.0/docs/api/java/io/OutputStreamWriter.html
   * @throws IOException
   */
  public static void saveDom( final Document doc, final OutputStream outputStream,
                              String encoding ) throws IOException {
    saveDom( doc, outputStream, encoding, false );
  }

  public static void saveDom( final Document doc, final OutputStream outputStream, String encoding,
      boolean suppressDeclaration ) throws IOException {
    saveDom( doc, outputStream, encoding, suppressDeclaration, false );
  }

  public static void saveDom( final Document doc, final OutputStream outputStream, String encoding,
      boolean suppressDeclaration, boolean prettyPrint ) throws IOException {
    OutputFormat format = prettyPrint ? OutputFormat.createPrettyPrint() : OutputFormat.createCompactFormat();
    format.setSuppressDeclaration( suppressDeclaration );
    if ( encoding != null ) {
      format.setEncoding( encoding.toLowerCase() );
      if ( !suppressDeclaration ) {
        doc.setXMLEncoding( encoding.toUpperCase() );
      }
    }
    XMLWriter writer = new XMLWriter( outputStream, format );
    writer.write( doc );
    writer.flush();
  }

  /**
   * Convenience method to close an input stream and handle (log and throw away) any exceptions. Helps keep code
   * uncluttered.
   * 
   * @param strm
   *          InputStream to be closed
   */
  protected static void closeInputStream( final InputStream strm ) {
    if ( null != strm ) {
      try {
        strm.close();
      } catch ( IOException e ) {
        XmlDom4JHelper.logger.warn( "Failed to close InputStream.", e ); //$NON-NLS-1$
      }
    }
  }

  public static String getNodeText( final String xpath, final Node rootNode ) {
    return ( XmlDom4JHelper.getNodeText( xpath, rootNode, null ) );
  }

  public static long getNodeText( final String xpath, final Node rootNode, final long defaultValue ) {
    String valueStr = XmlDom4JHelper.getNodeText( xpath, rootNode, Long.toString( defaultValue ) );
    try {
      return Long.parseLong( valueStr );
    } catch ( Exception ignored ) {
      //ignore
    }
    return defaultValue;
  }

  public static double getNodeText( final String xpath, final Node rootNode, final double defaultValue ) {
    String valueStr = XmlDom4JHelper.getNodeText( xpath, rootNode, null );
    if ( valueStr == null ) {
      return defaultValue;
    }
    try {
      return Double.parseDouble( valueStr );
    } catch ( Exception ignored ) {
      //ignore
    }
    return defaultValue;
  }

  public static String getNodeText( final String xpath, final Node rootNode, final String defaultValue ) {
    if ( rootNode == null ) {
      return ( defaultValue );
    }
    Node node = rootNode.selectSingleNode( xpath );
    if ( node == null ) {
      return defaultValue;
    }
    return node.getText();
  }

  public static org.dom4j.Document convertToDom4JDoc( final org.w3c.dom.Document doc )
    throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError,
    DocumentException {
    DOMSource source = new DOMSource( doc );
    StreamResult result = new StreamResult( new StringWriter() );
    TransformerFactory.newInstance().newTransformer().transform( source, result );
    String theXML = result.getWriter().toString();
    org.dom4j.Document dom4jDoc = DocumentHelper.parseText( theXML );
    return dom4jDoc;
  }
}
