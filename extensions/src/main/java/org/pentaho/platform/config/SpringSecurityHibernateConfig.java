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


package org.pentaho.platform.config;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.config.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;

/**
 * Object wrapper around contents of <code>applicationContext-spring-security-hibernate.xml</code>.
 * 
 * @author mlowery
 */
public class SpringSecurityHibernateConfig {
  Document document;

  private static final String ROOT_ELEMENT = "beans"; //$NON-NLS-1$

  private static final String PASSWORD_ENCODER_CLASS_XPATH = "/beans/bean[@id=\"passwordEncoder\"]/@class"; //$NON-NLS-1$

  public SpringSecurityHibernateConfig( File xmlFile ) throws IOException, DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromFile( xmlFile, new DtdEntityResolver() ) );
  }

  public SpringSecurityHibernateConfig( String xml ) throws DocumentException, XmlParseException {
    this( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  public SpringSecurityHibernateConfig( Document doc ) throws DocumentException {
    Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( Messages.getInstance().getErrorString( "PentahoXml.ERROR_0001_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$ 
    }
    document = doc;
  }

  public SpringSecurityHibernateConfig() {
    document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
  }

  /**
   * Returns the password encoder used during login in the platform or null if it cannot be instantiated.
   */
  public PasswordEncoder getPasswordEncoder() {
    try {
      String pentahoEncoderClassName = document.selectSingleNode( PASSWORD_ENCODER_CLASS_XPATH ).getText();
      Class passwordEncoderClass = Class.forName( pentahoEncoderClassName );
      return (PasswordEncoder) passwordEncoderClass.newInstance();
    } catch ( ClassNotFoundException e ) {
      return null;
    } catch ( InstantiationException e ) {
      return null;
    } catch ( IllegalAccessException e ) {
      return null;
    }
  }

  public Document getDocument() {
    return document;
  }
}
