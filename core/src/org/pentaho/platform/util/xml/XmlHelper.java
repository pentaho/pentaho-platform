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

package org.pentaho.platform.util.xml;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IDocumentResourceLoader;
import org.pentaho.platform.util.FileHelper;
import org.pentaho.platform.util.logging.Logger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.Messages;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A set of static methods for performing various operations on DOM Documents and XML text (in the form of streams,
 * Strings, and files). The operations include creating DOM Documents (dom4j) transforming DOM Documents creating
 * XML from Objects, Lists and Maps creating Lists or Maps from XML getting an XML node's text
 * 
 * @author mbatchel/jdixon
 * 
 */
public class XmlHelper {

  /*
   * this regular expression pattern should match the value of the encoding pseudo-attribute in the xml processing
   * instruction in an xml document. e.g. <?xml version="1.0" encoding="UTF-8" ?>
   */
  private static final Pattern RE_ENCODING = Pattern.compile(
      "<\\?xml.*encoding=('|\")([^'\"]*)\\1.*\\?>.*", Pattern.DOTALL ); //$NON-NLS-1$

  private static final String DEFAULT_XSL_FOLDER = "system/custom/xsl/"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( XmlHelper.class );

  public static String listToXML( final List l ) throws UnsupportedOperationException {
    return XmlHelper.listToXML( l, "" ); //$NON-NLS-1$
  }

  public static String listToXML( final List l, final String indent ) throws UnsupportedOperationException {
    StringBuffer sb = new StringBuffer();
    sb.append( indent ).append( "<list>\r" ); //$NON-NLS-1$
    String newIndent = indent + "  "; //$NON-NLS-1$
    Object obj;
    for ( int i = 0; i < l.size(); i++ ) {
      obj = l.get( i );
      sb.append( newIndent ).append( "<list-element>\r" ); //$NON-NLS-1$
      XmlHelper.objToXML( obj, sb, newIndent );
      sb.append( newIndent ).append( "</list-element>\r" ); //$NON-NLS-1$
    }
    sb.append( indent ).append( "</list>\r" ); //$NON-NLS-1$
    return sb.toString();
  }

  public static String mapToXML( final Map m ) throws UnsupportedOperationException {
    return XmlHelper.mapToXML( m, "" ); //$NON-NLS-1$
  }

  public static String mapToXML( final Map mp, final String indent ) throws UnsupportedOperationException {
    StringBuffer sb = new StringBuffer();
    sb.append( indent ).append( "<map>\r" ); //$NON-NLS-1$
    String newIndent = indent + "  "; //$NON-NLS-1$
    Iterator it = mp.entrySet().iterator();
    Map.Entry ent;
    Object obj;
    while ( it.hasNext() ) {
      ent = (Map.Entry) it.next();
      if ( !( ent.getKey() instanceof String ) ) {
        throw new UnsupportedOperationException( Messages.getInstance().getErrorString( "XMLUTL.ERROR_0011_MAP_KEYS" ) ); //$NON-NLS-1$
      }
      sb.append( newIndent ).append( "<map-entry>\r" ); //$NON-NLS-1$
      sb.append( newIndent ).append( "<key>\r" ); //$NON-NLS-1$
      sb.append( newIndent ).append( "<![CDATA[" ).append( ent.getKey().toString() ).append( "]]>\r" ); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append( newIndent ).append( "</key>\r" ); //$NON-NLS-1$
      obj = ent.getValue();
      XmlHelper.objToXML( obj, sb, newIndent );
      sb.append( newIndent ).append( "</map-entry>\r" ); //$NON-NLS-1$
    }
    sb.append( indent ).append( "</map>\r" ); //$NON-NLS-1$
    return sb.toString();
  }

  private static void objToXML( final Object obj, final StringBuffer sb, final String newIndent ) {
    if ( obj instanceof String ) {
      sb.append( newIndent ).append( "<string-value>\r" ); //$NON-NLS-1$
      sb.append( newIndent ).append( "<![CDATA[" ).append( (String) obj ).append( "]]>\r" ); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append( newIndent ).append( "</string-value>\r" ); //$NON-NLS-1$
    } else if ( obj instanceof StringBuffer ) {
      sb.append( newIndent ).append( "<stringbuffer-value>\r" ); //$NON-NLS-1$
      sb.append( newIndent ).append( "<![CDATA[" ).append( obj.toString() ).append( "]]>\r" ); //$NON-NLS-1$ //$NON-NLS-2$
      sb.append( newIndent ).append( "</stringbuffer-value>\r" ); //$NON-NLS-1$
    } else if ( obj instanceof BigDecimal ) {
      sb.append( newIndent ).append( "<bigdecimal-value>" ).append( obj.toString() ).append( "</bigdecimal-value>\r" ); //$NON-NLS-1$ //$NON-NLS-2$
    } else if ( obj instanceof Date ) {
      SimpleDateFormat fmt = new SimpleDateFormat();
      fmt.setTimeZone( TimeZone.getTimeZone( "GMT" ) ); //$NON-NLS-1$
      sb.append( newIndent ).append( "<date-value>" ); //$NON-NLS-1$
      sb.append( fmt.format( (Date) obj ) ).append( "</date-value>\r" ); //$NON-NLS-1$
    } else if ( obj instanceof Long ) {
      sb.append( newIndent ).append( "<long-value>" ); //$NON-NLS-1$
      sb.append( obj.toString() ).append( "</long-value>\r" ); //$NON-NLS-1$
    } else if ( obj instanceof Map ) {
      sb.append( newIndent ).append( "<map-value>\r" ); //$NON-NLS-1$
      sb.append( XmlHelper.mapToXML( (Map) obj, newIndent ) );
      sb.append( newIndent ).append( "</map-value>\r" ); //$NON-NLS-1$
    } else if ( obj instanceof List ) {
      sb.append( newIndent ).append( "<list-value>\r" ); //$NON-NLS-1$
      sb.append( XmlHelper.listToXML( (List) obj, newIndent ) );
      sb.append( newIndent ).append( "</list-value>\r" ); //$NON-NLS-1$
    } else {
      throw new UnsupportedOperationException( Messages.getInstance().getErrorString(
          "XMLUTL.ERROR_0012_DATA_TYPE", obj.getClass().getName() ) ); //$NON-NLS-1$
    }

  }

  public static void decode( final String[] strings ) {
    if ( strings != null ) {
      for ( int i = 0; i < strings.length; ++i ) {
        strings[i] = XmlHelper.decode( strings[i] );
      }
    }
  }

  public static String decode( String string ) {
    // TODO replace this is a more robust encoder
    if ( string != null ) {
      string = string.replaceAll( "&lt;", "<" ) //$NON-NLS-1$ //$NON-NLS-2$
          .replaceAll( "&gt;", ">" ) //$NON-NLS-1$ //$NON-NLS-2$
          .replaceAll( "&apos;", "'" ) //$NON-NLS-1$ //$NON-NLS-2$
          .replaceAll( "&quot;", "\"" ) //$NON-NLS-1$ //$NON-NLS-2$
          .replaceAll( "&amp;", "&" ); //$NON-NLS-1$ //$NON-NLS-2$ // DO THE & LAST!!!!
    }
    return string;
  }

  public static void encode( final String[] strings ) {
    if ( strings != null ) {
      for ( int i = 0; i < strings.length; ++i ) {
        strings[i] = XmlHelper.encode( strings[i] );
      }
    }
  }

  public static String encode( final String string ) {

    return StringEscapeUtils.escapeXml( string );
  }

  private static final int BUFF_SIZE = 512;

  public static String getEncoding( final File f ) throws IOException {
    char[] cbuf = new char[XmlHelper.BUFF_SIZE];
    Reader rdr = null;
    try {
      rdr = new FileReader( f );
      rdr.read( cbuf );
    } finally {
      if ( rdr != null ) {
        rdr.close();
      }
    }
    String strEnc = String.valueOf( cbuf );
    return XmlHelper.getEncoding( strEnc );
  }

  public static String getEncoding( final InputStream inStream ) throws IOException {
    String encodingPI = XmlHelper.readEncodingProcessingInstruction( inStream );
    return XmlHelper.getEncoding( encodingPI );
  }

  /**
   * Find the character encoding specification in the xml String. If it exists, return the character encoding.
   * Otherwise, return null.
   * 
   * @param xml
   *          String containing the xml
   * @return String containing the character encoding in the xml processing instruction if it exists, else null.
   */
  public static String getEncoding( final String xml ) {
    Matcher m = XmlHelper.RE_ENCODING.matcher( xml );
    boolean bMatches = m.matches();
    if ( bMatches && ( m.groupCount() == 2 ) ) {
      return m.group( 2 );
    }
    // no encoding found
    return null;
  }

  /**
   * Find the character encoding specification in the xml String. If it exists, return the character encoding.
   * Otherwise, return the system encoding.
   * 
   * @param xml
   *          String containing the xml
   * @param defaultEncoding
   *          Encoding to use if there is no encoding in the xml document
   * @return String containing the character encoding in the xml processing instruction, or defaultEncoding if
   *         there is no encoding in the xml document. If defaultEncoding is also null, then it returns the value
   *         in LocaleHelper.getSystemEncoding(). if it exists, else the system encoding.
   */
  public static String getEncoding( final String xml, final String defaultEncoding ) {
    String enc = XmlHelper.getEncoding( xml );
    return null != enc ? enc : ( defaultEncoding != null ? defaultEncoding : LocaleHelper.getSystemEncoding() );
  }

  /**
   * WARNING: if the <param>inStream</param> instance does not support mark/reset, when this method returns,
   * subsequent reads on <param>inStream</param> will be 256 bytes into the stream. This may not be the expected
   * behavior. FileInputStreams are an example of an InputStream that does not support mark/reset. InputStreams
   * that do support mark/reset will be reset to the beginning of the stream when this method returns.
   * 
   * @param inStream
   * @return
   * @throws IOException
   */
  public static String readEncodingProcessingInstruction( final InputStream inStream ) throws IOException {
    final int BUFF_SZ = 256;
    if ( inStream.markSupported() ) {
      inStream.mark( BUFF_SZ + 1 ); // BUFF_SZ+1 forces mark to NOT be forgotten
    }
    byte[] buf = new byte[BUFF_SZ];
    int totalBytesRead = 0;
    int bytesRead;
    do {
      bytesRead = inStream.read( buf, totalBytesRead, BUFF_SZ - totalBytesRead );
      if ( bytesRead == -1 ) {
        break;
      }
      totalBytesRead += bytesRead;
    } while ( totalBytesRead < BUFF_SZ );

    if ( inStream.markSupported() ) {
      inStream.reset();
    }

    return new String( buf );
  }

  /**
   * Use the transform specified by xslName and transform the document specified by docInStrm, and return the
   * resulting document.
   * 
   * @param xslName
   *          String containing the name of a file in the repository containing the xsl transform
   * @param xslPath
   *          String containing the path to the file identifyied by <code>xslName</code>
   * @param uri
   *          String containing the URI of a resource containing the document to be transformed
   * @param params
   *          Map of properties to set on the transform
   * @param session
   *          IPentahoSession containing a URIResolver instance to resolve URI's in the output document.
   * 
   * @return StringBuffer containing the XML results of the transform. Null if there was an error.
   * @throws TransformerException
   *           If attempt to transform the document fails.
   */
  public static final StringBuffer transformXml( final String xslName, final String xslPath, final String strDocument,
      final Map params, final IDocumentResourceLoader loader ) throws TransformerException {
    InputStream inStrm = null;
    try {
      // Read the encoding from the XML file - see BISERVER-895
      String encoding = XmlHelper.getEncoding( strDocument, null );
      inStrm = new ByteArrayInputStream( strDocument.getBytes( encoding ) );
    } catch ( UnsupportedEncodingException e ) {
      if ( XmlHelper.logger.isErrorEnabled() ) {
        XmlHelper.logger.error( e );
      }
    }
    StringBuffer result = XmlHelper.transformXml( xslName, xslPath, inStrm, params, loader );
    FileHelper.closeInputStream( inStrm );

    return result;
  }

  /**
   * Use the transform specified by xslPath and xslName and transform the document specified by docInStrm, and
   * return the resulting document.
   * 
   * @param xslSrc
   *          StreamSrc containing the xsl transform
   * @param docSrc
   *          StreamSrc containing the document to be transformed
   * @param params
   *          Map of properties to set on the transform
   * @param session
   *          IPentahoSession containing a URIResolver instance to resolve URI's in the output document.
   * 
   * @return StringBuffer containing the XML results of the transform. Null if there was an error.
   * @throws TransformerException
   *           If attempt to transform the document fails.
   */
  @SuppressWarnings( { "unchecked" } )
  public static final StringBuffer transformXml( final String xslName, final String xslPath,
      final InputStream docInStrm, Map params, final IDocumentResourceLoader loader ) throws TransformerException {
    StringBuffer result = null;

    InputStream xslInStrm = XmlHelper.getLocalizedXsl( xslPath, xslName, loader );
    if ( null == xslInStrm ) {
      Logger.error( XmlHelper.class.getName(), Messages.getInstance().getErrorString(
          "XmlHelper.ERROR_0003_NULL_XSL_SOURCE" ) ); //$NON-NLS-1$
    } else if ( null == docInStrm ) {
      Logger.error( XmlHelper.class.getName(), Messages.getInstance().getErrorString(
          "XmlHelper.ERROR_0004_NULL_DOCUMENT" ) ); //$NON-NLS-1$
    } else {

      // at this point, we have both of our InputStreams

      // Add encoding for any xsl that may set/use it
      if ( params == null ) {
        params = new HashMap();
      }
      params.put( "output-encoding", LocaleHelper.getSystemEncoding() ); //$NON-NLS-1$

      try {
        result = XmlHelper.transformXml( xslInStrm, docInStrm, params, loader );
      } catch ( TransformerException e ) {
        Logger.error( XmlHelper.class.getName(), Messages.getInstance().getErrorString(
            "XmlHelper.ERROR_0006_TRANSFORM_XML_ERROR", e.getMessage(), xslName ), e ); //$NON-NLS-1$
        throw e;
      } finally {
        FileHelper.closeInputStream( xslInStrm );
      }
    }
    return result;
  }

  /**
   * Use the transform specified by xslSrc and transform the document specified by docSrc, and return the resulting
   * document.
   * 
   * @param xslInStream
   *          InputStream containing the xsl transform
   * @param docInStrm
   *          InputStream containing the document to be transformed
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
  public static final StringBuffer transformXml( final InputStream xslInStream, final InputStream docInStrm,
      final Map params, final URIResolver resolver ) throws TransformerConfigurationException, TransformerException {

    StreamSource xslSrc = new StreamSource( xslInStream );
    StreamSource docSrc = new StreamSource( docInStrm );
    return XmlHelper.transformXml( xslSrc, docSrc, params, resolver );
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
      Set keys = params.keySet();
      Iterator it = keys.iterator();
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
   * Get the File object corresponding to the path, filename (xslName), and locale. The path is relative to the
   * solution path.
   * 
   * @param path
   * @param xslName
   * @return
   */
  public static final InputStream getLocalizedXsl( final String path, final String xslName,
      final IDocumentResourceLoader loader ) {
    String fullPath = null;
    String defaultPath = null;
    InputStream file = null;
    if ( null != path ) {
      // try to find it on the specified path
      fullPath = ( path + File.separator + xslName ).replace( '\\', '/' );
      file = XmlHelper.getLocalizedFile( fullPath, LocaleHelper.getLocale(), loader );
    }
    if ( null == file ) {
      // didn't find the file, let's try default path
      defaultPath = ( XmlHelper.DEFAULT_XSL_FOLDER + xslName ).replace( '\\', '/' );
      file = XmlHelper.getLocalizedFile( defaultPath, LocaleHelper.getLocale(), loader );
    }
    if ( null == file ) {
      // we should not get this far...
      Logger.error( XmlHelper.class.getName(), Messages.getInstance().getErrorString(
          "XmlHelper.ERROR_0011_TRANSFORM_XSL_DOES_NOT_EXIST", xslName, fullPath, defaultPath ) ); //$NON-NLS-1$
    }
    return file;
  }

  public static InputStream getLocalizedFile( final String fullPath, final Locale locale,
      final IDocumentResourceLoader loader ) {
    String language = locale.getLanguage();
    String country = locale.getCountry();
    String variant = locale.getVariant();

    // File file = new File(fullPath);

    String fileName = fullPath;
    int dotIndex = fileName.indexOf( '.' );
    String baseName = dotIndex == -1 ? fileName : fileName.substring( 0, dotIndex ); // These two lines fix an
                                                                                     // index out
                                                                                     // of bounds
    String extension = dotIndex == -1 ? "" : fileName.substring( dotIndex ); // Exception that occurs when a filename has no extension //$NON-NLS-1$

    InputStream in = null;
    try {
      if ( !variant.equals( "" ) ) { //$NON-NLS-1$
        in = loader.loadXsl( baseName + "_" + language + "_" + country + "_" + variant + extension ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
      if ( in == null ) {
        in = loader.loadXsl( baseName + "_" + language + "_" + country + extension ); //$NON-NLS-1$//$NON-NLS-2$
      }
      if ( in == null ) {
        in = loader.loadXsl( baseName + "_" + language + extension ); //$NON-NLS-1$
      }
      if ( in == null ) {
        in = loader.loadXsl( baseName + extension );
      }
    } catch ( Exception e ) {
      Logger.error( XmlHelper.class.getName(), "Error loading localized file: " + fullPath ); //$NON-NLS-1$
    }
    return in;
  }

  /**
   * 
   * @param version
   * @param encoding
   * @return String Xml Processing instruction text with the specified version (usually 1.0) and encoding (for
   *         instance, UTF-8)
   */
  public static String createXmlProcessingInstruction( final String version, final String encoding ) {
    return "<?xml version=\"" + version + "\" encoding = \"" + encoding + "\" ?>"; //$NON-NLS-1$  //$NON-NLS-2$ //$NON-NLS-3$
  }

}
