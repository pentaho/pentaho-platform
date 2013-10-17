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

package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.config.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.File;
import java.io.IOException;

public class SystemActionsXml {

  Document document;

  private static final String ROOT_ELEMENT = "beans"; //$NON-NLS-1$

  public SystemActionsXml( File pentahoXmlFile ) throws IOException, DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, new DtdEntityResolver() ) );
  }

  public SystemActionsXml( String xml ) throws DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  public SystemActionsXml( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( Messages.getInstance().getErrorString( "PentahoXml.ERROR_0001_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$ 
    }
    document = doc;
  }

  public SystemActionsXml() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  private void setValue( String xPath, String value ) {
    setValue( xPath, value, false );
  }

  private void setValue( String xPath, String value, boolean useCData ) {
    Element element = (Element) document.selectSingleNode( xPath );
    if ( element == null ) {
      element = DocumentHelper.makeElement( document, xPath );
    }
    if ( useCData ) {
      element.clearContent();
      element.addCDATA( value );
    } else {
      element.setText( value );
    }
  }

  private String getValue( String xpath ) {
    Element element = (Element) document.selectSingleNode( xpath );
    return element != null ? element.getText() : null;
  }

  public Document getDocument() {
    return document;
  }

}
