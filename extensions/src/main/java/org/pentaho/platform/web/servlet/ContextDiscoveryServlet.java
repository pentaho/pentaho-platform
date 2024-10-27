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


package org.pentaho.platform.web.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.util.xml.XMLParserFactoryProducer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;

/**
 * @deprecated
 * @author Alex Silva
 * 
 */
@Deprecated
public class ContextDiscoveryServlet extends HttpServlet {

  private static final long serialVersionUID = -8747147437664663719L;

  private static final Log logger = LogFactory.getLog( ContextDiscoveryServlet.class );

  private final DocumentBuilderFactory factory;

  private final TransformerFactory tf = TransformerFactory.newInstance();

  public ContextDiscoveryServlet() {
    DocumentBuilderFactory documentBuilderFactory;
    try {
      documentBuilderFactory = XMLParserFactoryProducer.createSecureDocBuilderFactory();
    } catch ( ParserConfigurationException e ) {
      logger.error( e.getLocalizedMessage() );
      documentBuilderFactory = DocumentBuilderFactory.newInstance();
    }
    factory = documentBuilderFactory;
  }

  @Override
  public void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException {

    String path = request.getContextPath();

    try {
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.newDocument();
      Element epath = document.createElement( "application-context-root" ); //$NON-NLS-1$
      epath.setTextContent( path );
      document.appendChild( epath );
      Transformer trans = tf.newTransformer();
      trans.transform( new DOMSource( document ), new StreamResult( response.getOutputStream() ) );
    } catch ( ParserConfigurationException e ) {
      throw new ServletException( e );
    } catch ( TransformerConfigurationException e ) {
      throw new ServletException( e );
    } catch ( TransformerException e ) {
      throw new ServletException( e );
    } catch ( IOException e ) {
      throw new ServletException( e );
    }
  }

}
