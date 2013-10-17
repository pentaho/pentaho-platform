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

package org.pentaho.platform.web.servlet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

  private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

  private final TransformerFactory tf = TransformerFactory.newInstance();

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
