/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.context;

import com.google.common.annotations.VisibleForTesting;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleClientEnvironment.ClientType;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.pentaho.platform.web.http.PentahoHttpSessionHelper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Overrides <code>getResourceByPath</code> so that relative paths are relative to the Hitachi Vantara solution repository's
 * system directory instead of being relative to servlet context root.
 * 
 * @author mlowery
 */
public class PentahoSolutionSpringApplicationContext extends XmlWebApplicationContext {

  private static final String DEFAULT_NAMESPASE = "ns";

  private static final String SYSTEM_FOLDER = "system";

  private static final String DEFAULT_SPRING_XML = "pentaho-spring-beans.xml";
  private static final String IMPORT_TAG = "import";
  private static final String RESOURCE_ATTR = "resource";
  private static final String RESOURCE = "importExport.xml";

  private static final String IMPORT_COMMENT = "Import \"{0}\" was added by \"{1}\" class automatically";

  private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

  protected Resource getResourceByPath( String path ) {
    Resource resource = null;
    String solutionPath = PentahoHttpSessionHelper.getSolutionPath( getServletContext() );
    if ( solutionPath != null ) {
      File file = new File( solutionPath + File.separator + SYSTEM_FOLDER + File.separator + path ); //$NON-NLS-1$
      resource = new FileSystemResource( file );
    } else {
      resource = super.getResourceByPath( path );
    }
    ClientType clientType = null;
    // We need to check if we are running in spoon. For that we need to get the kettle client type
    if ( KettleClientEnvironment.isInitialized( )
        && KettleClientEnvironment.getInstance() != null ) {
      clientType  = KettleClientEnvironment.getInstance().getClient();
    }
    // If the client type is spoon then we will skip adding the xml file
    if ( path.toLowerCase().endsWith( DEFAULT_SPRING_XML )
        && ( clientType == null || !clientType.equals( ClientType.SPOON ) ) ) {
      try {

        Document doc = getResourceDocument( resource.getInputStream() );

        Node node = doc.getDocumentElement();

        NodeList nodes =
            evaluateXPath( node, MessageFormat.format( "./{0}:{1}[@{2}=''{3}'']", DEFAULT_NAMESPASE, IMPORT_TAG,
                RESOURCE_ATTR, RESOURCE ), XPathConstants.NODESET );

        if ( nodes.getLength() > 0 ) {
          return resource;
        }

        Element importEl = doc.createElementNS( node.getNamespaceURI(), IMPORT_TAG );
        importEl.setAttribute( RESOURCE_ATTR, RESOURCE );
        Comment comment =
            doc.createComment( MessageFormat.format( IMPORT_COMMENT, RESOURCE, this.getClass().getSimpleName() ) );
        addLineBreak( node );
        node.appendChild( comment );
        addLineBreak( node );
        node.appendChild( importEl );
        addLineBreak( node );

        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource( doc );
        FileOutputStream out = new FileOutputStream( resource.getFile() );
        try {
          StreamResult result = new StreamResult( out );
          transformer.transform( source, result );
        } finally {
          out.flush();
          out.close();
        }
      } catch ( Exception e ) {
        e.printStackTrace();
      }
    }

    return resource;
  }

  private void addLineBreak( Node node ) {
    node.appendChild( node.getOwnerDocument().createTextNode( "\n" ) );
  }

  @SuppressWarnings( "unchecked" )
  private <T> T evaluateXPath( Node node, String exp, QName res ) throws XPathExpressionException {
    XPath xpath = XPATH_FACTORY.newXPath();
    xpath.setNamespaceContext( new DefNamespaceContext( node.getNamespaceURI() ) );
    return (T) xpath.compile( exp ).evaluate( node, res );
  }

  private static class DefNamespaceContext implements NamespaceContext {

    String defaultNS;

    public DefNamespaceContext( String defaultNS ) {
      this.defaultNS = defaultNS;
    }

    @Override
    public String getNamespaceURI( String prefix ) {
      if ( DEFAULT_NAMESPASE.equals( prefix ) ) {
        return defaultNS;
      }
      return null;
    }

    @Override
    public String getPrefix( String namespaceURI ) {
      return null;
    }

    @Override
    public Iterator<String> getPrefixes( String namespaceURI ) {
      return null;
    }

  }

  @VisibleForTesting
  Document getResourceDocument( InputStream is )
    throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory dFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
    dFactory.setNamespaceAware( true );
    DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
    return dBuilder.parse( is );
  }
}
