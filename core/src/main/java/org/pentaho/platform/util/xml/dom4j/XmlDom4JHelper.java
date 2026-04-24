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


package org.pentaho.platform.util.xml.dom4j;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.util.messages.Messages;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.xml.sax.EntityResolver;

import javax.xml.XMLConstants;
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
import java.util.Map;

// TODO sbarkdull, exernalize strings, comment methods

/**
 * A set of static methods to help in with: * the construction of XML DOM Documents (org.dom4j.Document) from
 * files, streams, and Strings * in the creation of XML DOM Documents as the result of an XSLT transform *
 * persisting of XML DOM documents to the file system or a <code>Writer</code>. * the encoding of a String of Xml
 * text
 * 
 * Design notes: This class should never have any dependencies (i.e. imports) on anything on org.pentaho or
 * com.pentaho or their descendant packages. In general, methods in the class should not attempt to handle
 * exceptions, but should let the exceptions propagate to the caller to be handled there. Please do not use
 * european-reuse in this class. One of the primary design goals for this class was to construct it in a way that
 * it could be used without change outside the Pentaho platform. Related XML-helper type code that is dependent
 * on the platform should be moved "up" to XmlHelper.
 */
public class XmlDom4JHelper {

  private static final Log logger = LogFactory.getLog( XmlDom4JHelper.class );

  /**
   * Create a {@link Document} from <code>strXml</code>.
   * 
   * @param strXml
   *          String containing the XML that will be used to create the Document
   * @param resolver
   *          an {@link EntityResolver} instance that will resolve any external URIs. See the docs on
   *          {@link EntityResolver}. <code>null</code> is an acceptable value.
   * @return a {@link Document} initialized with the XML in <code>strXml</code>.
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
   * Create a {@link Document} from the contents of a file.
   * 
   * @param file
   *          File containing XML that will be used to create the Document.
   * @param resolver
   *          an {@link EntityResolver} instance that will resolve any external URIs. See the docs on
   *          {@link EntityResolver}. <code>null</code> is an acceptable value.
   * @return a {@link Document} initialized with the XML in <code>strXml</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException
   *           if the file doesn't exist
   */
  public static Document getDocFromFile( final File file, final EntityResolver resolver ) throws DocumentException,
    IOException {
    SAXReader reader = XMLParserFactoryProducer.getSAXReader( resolver );
    return reader.read( file );
  }

  /**
   * Create a {@link Document} from the contents of an input stream, where the input stream contains valid XML.
   *
   * @param inStream
   *          the XML that will be used to create the Document.
   * @param resolver
   *          an {@link EntityResolver} instance that will resolve any external URIs. See the docs on
   *          {@link EntityResolver}. <code>null</code> is an acceptable value.
   * @return a {@link Document} initialized with the XML in <code>inStream</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException
   *           if the file doesn't exist
   */
  public static Document getDocFromStream( final InputStream inStream, final EntityResolver resolver )
    throws DocumentException, IOException {

    SAXReader reader = XMLParserFactoryProducer.getSAXReader( resolver );
    return reader.read( inStream );
  }

  /**
   * Create a {@link Document} from the contents of an input stream, where the input stream contains valid XML.
   * 
   * @param inStream
   *          the XML that will be used to create the Document.
   * @return a {@link Document} initialized with the XML in <code>inStream</code>.
   * @throws DocumentException
   *           if the document isn't valid
   * @throws IOException
   *           if the file doesn't exist
   */
  public static Document getDocFromStream( final InputStream inStream ) throws DocumentException, IOException {

    return XmlDom4JHelper.getDocFromStream( inStream, null );
  }

  /**
   * Use the transform specified by <code>xslSrc</code> and transform the document specified by <code>docSrc</code>,
   * and return the resulting document.
   * 
   * @param xslSrc
   *          StreamSrc containing the XSL transform
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
  protected static StringBuffer transformXml( final StreamSource xslSrc, final StreamSource docSrc,
      final Map params, final URIResolver resolver ) throws TransformerConfigurationException, TransformerException {

    TransformerFactory tf = XMLParserFactoryProducer.createSecureTransformerFactory( );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
    if ( null != resolver ) {
      tf.setURIResolver( resolver );
    }
    // TODO need to look into compiling the XSLs...
    Transformer t = tf.newTransformer( xslSrc ); // can throw TransformerConfigurationException
    // Start the transformation
    if ( params != null ) {
      for ( Map.Entry<String, String> entry : (Iterable<Map.Entry<String, String>>) params.entrySet() ) {
        if ( entry.getValue() != null ) {
          t.setParameter( entry.getKey(), entry.getValue() );
        }
      }
    }

    return transformSource( t, docSrc );
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
  public static StringBuffer docToString( final org.w3c.dom.Document doc )
    throws TransformerConfigurationException, TransformerException {

    TransformerFactory tf = XMLParserFactoryProducer.createSecureTransformerFactory( );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
    Transformer t = tf.newTransformer(); // can throw TransformerConfigurationException

    return transformSource( t, new DOMSource( doc ) );
  }

  /**
   * Transform the XML {@link Source} using the given {@link Transformer} instance, returning the result as a
   * {@link StringBuffer}.
   *
   * @param t
   *          the {@link Transformer} instance to use to transform the
   * @param docSrc
   *          the XML input to transform.
   * @return a {@link StringBuffer} containing the XML results of the transform.
   * @throws TransformerException if an unrecoverable error occurs during the course of the transformation.
   */
  private static StringBuffer transformSource( Transformer t, Source docSrc ) throws TransformerException {
    StringWriter writer = new StringWriter();

    t.transform( docSrc, new StreamResult( writer ) ); // can throw TransformerException

    return writer.getBuffer();
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
        value.append( "&#x" );
        value.append( Integer.toString( charValue, 0x10 ) );
        value.append( ';' );
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
    TransformerFactory tf = XMLParserFactoryProducer.createSecureTransformerFactory( );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
    tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
    tf.newTransformer().transform( source, result );
    String theXML = result.getWriter().toString();
    Document dom4jDoc = null;
    try {
      dom4jDoc = getDocFromString( theXML, null );
    } catch ( XmlParseException e ) {
      throw new TransformerFactoryConfigurationError( e );
    }
    return dom4jDoc;
  }
}
