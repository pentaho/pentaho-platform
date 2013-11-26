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

public class PdiConfigXml implements IPdiConfig {

  private static final String ROOT_ELEMENT = "kettle-repository"; //$NON-NLS-1$
  private static final String REPOSITORY_FILE_XPATH = ROOT_ELEMENT + "/repositories.xml.file"; //$NON-NLS-1$
  private static final String REPOSITORY_TYPE_XPATH = ROOT_ELEMENT + "/repository.type"; //$NON-NLS-1$
  private static final String REPOSITORY_NAME_XPATH = ROOT_ELEMENT + "/repository.name"; //$NON-NLS-1$
  private static final String REPOSITORY_USER_XPATH = ROOT_ELEMENT + "/repository.userid"; //$NON-NLS-1$
  private static final String REPOSITORY_PWD_XPATH = ROOT_ELEMENT + "/repository.password"; //$NON-NLS-1$

  Document document;

  public PdiConfigXml( File pentahoXmlFile ) throws IOException, DocumentException {
    this( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, new DtdEntityResolver() ) );
  }

  public PdiConfigXml( String xml ) throws DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  public PdiConfigXml( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( Messages.getInstance().getErrorString(
          "GoogleMapsConfig.ERROR_0001_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$
    }
    document = doc;
  }

  public PdiConfigXml( IPdiConfig pdiConfig ) {
    this();
    setRepositoryName( pdiConfig.getRepositoryName() );
    setRepositoryPassword( pdiConfig.getRepositoryPassword() );
    setRepositoryType( pdiConfig.getRepositoryType() );
    setRepositoryUserId( pdiConfig.getRepositoryUserId() );
    setRepositoryXmlFile( pdiConfig.getRepositoryXmlFile() );
  }

  public PdiConfigXml() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  public String getRepositoryName() {
    return getValue( REPOSITORY_NAME_XPATH );
  }

  public String getRepositoryPassword() {
    return getValue( REPOSITORY_PWD_XPATH );
  }

  public String getRepositoryType() {
    return getValue( REPOSITORY_TYPE_XPATH );
  }

  public String getRepositoryUserId() {
    return getValue( REPOSITORY_USER_XPATH );
  }

  public String getRepositoryXmlFile() {
    return getValue( REPOSITORY_FILE_XPATH );
  }

  public void setRepositoryName( String name ) {
    setValue( REPOSITORY_NAME_XPATH, name );
  }

  public void setRepositoryPassword( String password ) {
    setValue( REPOSITORY_PWD_XPATH, password );
  }

  public void setRepositoryType( String type ) {
    setValue( REPOSITORY_TYPE_XPATH, type );
  }

  public void setRepositoryUserId( String userId ) {
    setValue( REPOSITORY_USER_XPATH, userId );
  }

  public void setRepositoryXmlFile( String xmlFile ) {
    setValue( REPOSITORY_FILE_XPATH, xmlFile );
  }

  private void setValue( String xPath, String value ) {
    Element element = (Element) document.selectSingleNode( xPath );
    if ( element == null ) {
      element = DocumentHelper.makeElement( document, xPath );
    }
    element.setText( value );
  }

  private String getValue( String xpath ) {
    Element element = (Element) document.selectSingleNode( xpath );
    return element != null ? element.getText() : null;
  }
}
