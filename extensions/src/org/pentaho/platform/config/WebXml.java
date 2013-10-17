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
import org.dom4j.Node;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public class WebXml {

  Document document;

  private static final String PARAM_NAME_ELEMENT = "param-name"; //$NON-NLS-1$
  private static final String PARAM_VALUE_ELEMENT = "param-value"; //$NON-NLS-1$
  private static final String ROOT_ELEMENT = "web-app"; //$NON-NLS-1$
  private static final String CONTEXT_CONFIG_CONTEXT_PARAM_NAME = "contextConfigLocation"; //$NON-NLS-1$
  private static final String BASE_URL_CONTEXT_PARAM_NAME = "base-url"; //$NON-NLS-1$
  private static final String FULLY_QUALIFIED_SERVER_URL_CONTEXT_PARAM_NAME = "fully-qualified-server-url"; //$NON-NLS-1$
  private static final String SOLUTION_PATH_CONTEXT_PARAM_NAME = "solution-path"; //$NON-NLS-1$
  private static final String LOCALE_LANGUAGE_CONTEXT_PARAM_NAME = "locale-language"; //$NON-NLS-1$
  private static final String LOCALE_COUNTRY_CONTEXT_PARAM_NAME = "locale-country"; //$NON-NLS-1$
  private static final String ENCODING_CONTEXT_PARAM_NAME = "encoding"; //$NON-NLS-1$
  private static final String HOME_SERVLET_NAME = "Home"; //$NON-NLS-1$
  private static final String CONTEXT_PARAM_ELEMENT = "context-param"; //$NON-NLS-1$
  private static final String CONTEXT_PARAM_XPATH = ROOT_ELEMENT + "/" + CONTEXT_PARAM_ELEMENT; //$NON-NLS-1$
  private static final String CONTEXT_PARAM_NAME_TEMPLATE_XPATH = CONTEXT_PARAM_XPATH + "/param-name[text()=\"{0}\"]"; //$NON-NLS-1$
  private static final String SERVLET_NAME_TEMPLATE_XPATH = ROOT_ELEMENT + "/servlet/servlet-name[text() = \"{0}\"]"; //$NON-NLS-1$

  public WebXml( File pentahoXmlFile ) throws IOException, DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, new DtdEntityResolver() ) );
  }

  public WebXml( String xml ) throws DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  public WebXml( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( "Invalid root element." ); //$NON-NLS-1$
    }
    document = doc;
  }

  public WebXml() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  public String getContextConfigFileName() {
    return getContextParamValue( CONTEXT_CONFIG_CONTEXT_PARAM_NAME );
  }

  @Deprecated
  public String getBaseUrl() {
    return getFullyQualifiedServerUrl();
  }

  public String getFullyQualifiedServerUrl() {
    return getContextParamValue( FULLY_QUALIFIED_SERVER_URL_CONTEXT_PARAM_NAME );
  }

  public String getSolutionPath() {
    return getContextParamValue( SOLUTION_PATH_CONTEXT_PARAM_NAME );
  }

  public String getLocaleLanguage() {
    return getContextParamValue( LOCALE_LANGUAGE_CONTEXT_PARAM_NAME );
  }

  public String getLocaleCountry() {
    return getContextParamValue( LOCALE_COUNTRY_CONTEXT_PARAM_NAME );
  }

  public String getEncoding() {
    return getContextParamValue( ENCODING_CONTEXT_PARAM_NAME );
  }

  public String getHomePage() {
    return getServletMapping( HOME_SERVLET_NAME );
  }

  public void setContextConfigFileName( String fileName ) {
    setContextParamValue( CONTEXT_CONFIG_CONTEXT_PARAM_NAME, fileName );
  }

  public void setBaseUrl( String baseUrl ) {
    setContextParamValue( BASE_URL_CONTEXT_PARAM_NAME, baseUrl );
  }

  public void setFullyQualifiedServerUrl( String fullyQualifiedServerUrl ) {
    setContextParamValue( FULLY_QUALIFIED_SERVER_URL_CONTEXT_PARAM_NAME, fullyQualifiedServerUrl );
  }

  public void setSolutionPath( String solutionPath ) {
    setContextParamValue( SOLUTION_PATH_CONTEXT_PARAM_NAME, solutionPath );
  }

  public void setLocaleLanguage( String language ) {
    setContextParamValue( LOCALE_LANGUAGE_CONTEXT_PARAM_NAME, language );
  }

  public void setLocaleCountry( String country ) {
    setContextParamValue( LOCALE_COUNTRY_CONTEXT_PARAM_NAME, country );
  }

  public void setEncoding( String encoding ) {
    setContextParamValue( ENCODING_CONTEXT_PARAM_NAME, encoding );
  }

  public void setHomePage( String homePage ) {
    setServletMapping( HOME_SERVLET_NAME, homePage );
  }

  public Document getDocument() {
    return document;
  }

  public String getContextParamValue( String name ) {
    String xPath = MessageFormat.format( CONTEXT_PARAM_NAME_TEMPLATE_XPATH, name );
    Node node = document.selectSingleNode( xPath );
    String value = null;
    if ( node != null ) {
      node = node.selectSingleNode( "../param-value" ); //$NON-NLS-1$
    }
    if ( node != null ) {
      value = node.getText();
    }
    return value;
  }

  public void setContextParamValue( String name, String value ) {
    String xPath = MessageFormat.format( CONTEXT_PARAM_NAME_TEMPLATE_XPATH, name );
    Element contextParamNameElement = (Element) document.selectSingleNode( xPath );
    if ( value == null ) {
      if ( contextParamNameElement != null ) {
        contextParamNameElement.getParent().detach();
      }
    } else {
      if ( contextParamNameElement == null ) {
        contextParamNameElement = document.getRootElement().addElement( CONTEXT_PARAM_ELEMENT );
        Element paramNameElement = contextParamNameElement.addElement( PARAM_NAME_ELEMENT );
        paramNameElement.setText( name );
      }
      Element paramValueElement = DocumentHelper.makeElement( contextParamNameElement.getParent(),
        PARAM_VALUE_ELEMENT );
      paramValueElement.setText( value );
    }
  }

  public boolean setServletMapping( String name, String value ) {
    String xPath = MessageFormat.format( SERVLET_NAME_TEMPLATE_XPATH, name );
    Node node = document.selectSingleNode( xPath );
    if ( node != null ) {
      node = node.selectSingleNode( "../jsp-file" ); //$NON-NLS-1$
    }
    if ( node != null ) {
      node.setText( value );
      return true;
    }
    return false;
  }

  public String getServletMapping( String name ) {
    String xPath = MessageFormat.format( SERVLET_NAME_TEMPLATE_XPATH, name );
    Node node = document.selectSingleNode( xPath );
    String value = null;
    if ( node != null ) {
      node = node.selectSingleNode( "../jsp-file" ); //$NON-NLS-1$
    }
    if ( node != null ) {
      value = node.getText();
    }
    return value;
  }
}
