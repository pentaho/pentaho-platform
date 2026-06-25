/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.util.xml;

import net.sf.saxon.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import java.util.Objects;

public class XMLParserFactoryProducer {

  private static final Log logger = LogFactory.getLog( XMLParserFactoryProducer.class );

  private static Configuration saxonConfig = new Configuration();

  /**
   * Creates an instance of {@link DocumentBuilderFactory} class with enabled
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING} property.
   * Enabling this feature prevents from some XXE attacks (e.g. XML bomb).
   * See PPP-3506 for more details.
   *
   * @throws ParserConfigurationException if feature can't be enabled
   *
   */
  public static DocumentBuilderFactory createSecureDocBuilderFactory() throws ParserConfigurationException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    docBuilderFactory.setFeature( "http://apache.org/xml/features/disallow-doctype-decl", true );

    return docBuilderFactory;
  }

  /**
   * Creates an instance of {@link SAXParserFactory} class with enabled
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING} property.
   * Enabling this feature prevents from some XXE attacks (e.g. XML bomb).
   *
   * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration.
   * @throws SAXNotRecognizedException    When the underlying XMLReader does not recognize the property name.
   * @throws SAXNotSupportedException     When the underlying XMLReader recognizes the property name but doesn't
   *                                      support the property.
   */
  public static SAXParserFactory createSecureSAXParserFactory()
    throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    factory.setFeature( "http://xml.org/sax/features/external-general-entities", false );
    factory.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
    factory.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
    return factory;
  }

  public static SAXReader getSAXReader( final EntityResolver resolver ) {
    SAXReader reader = new SAXReader();
    if ( resolver != null ) {
      reader.setEntityResolver( resolver );
    }
    try {
      reader.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
      reader.setFeature( "http://xml.org/sax/features/external-general-entities", false );
      reader.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
      reader.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
    } catch ( SAXException e ) {
      logger.error( "Some parser properties are not supported." );
    }
    reader.setIncludeExternalDTDDeclarations( false );
    reader.setIncludeInternalDTDDeclarations( false );
    return reader;
  }

  /**
   * Returns the Saxon configuration object being used by this class to create instances of {@link TransformerFactory}
   *
   * @return Saxon configuration object being used by this class to create instances of {@link TransformerFactory}
   */
  public static Configuration getSaxonConfig() {
    return saxonConfig;
  }

  /**
   * Sets the Saxon configuration object to be used by this class to create instances of {@link TransformerFactory}.
   * A {@code null} value will reset the configuration to a new instance of {@link Configuration}
   */
  public static void setSaxonConfig( Configuration config ) {
    saxonConfig = Objects.requireNonNullElseGet( config, Configuration::new );
  }

  /**
   * Creates an instance of {@link TransformerFactory} class with enabled
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING} property.
   *
   * @throws TransformerConfigurationException if a TransformerFactory cannot be created which satisfies the
   *                                           requested configuration
   */
  public static TransformerFactory createSecureTransformerFactory()
    throws TransformerConfigurationException {
    return createSecureTransformerFactory( false );
  }

  /**
   * Creates an instance of {@link TransformerFactory} class with enabled
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING} property. Depending on {@code useConfiguration} parameter value,
   * it may use the class' Saxon configuration object for creation
   *
   * @param useConfiguration if this class' Saxon configuration object is to be used when creating the factory
   * @throws TransformerConfigurationException if a TransformerFactory cannot be created which satisfies the
   *                                           requested configuration
   */
  public static TransformerFactory createSecureTransformerFactory( boolean useConfiguration )
    throws TransformerConfigurationException {
    return createSecureTransformerFactory( useConfiguration ? saxonConfig : null );
  }

  /**
   * Creates an instance of {@link TransformerFactory} class with enabled
   * {@link XMLConstants#FEATURE_SECURE_PROCESSING} property or using this class' Saxon configuration object
   *
   * @param config Saxon configuration to use, or null to use default TransformerFactory implementation
   * @throws TransformerConfigurationException if a TransformerFactory cannot be created which satisfies the
   *                                           requested configuration
   */
  public static TransformerFactory createSecureTransformerFactory( Configuration config )
    throws TransformerConfigurationException {
    TransformerFactory transformerFactory = null;

    if ( config == null ) {
      transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature( XMLConstants.FEATURE_SECURE_PROCESSING, true );
    } else {
      transformerFactory = new net.sf.saxon.jaxp.SaxonTransformerFactory( config );
    }

    return transformerFactory;
  }
}
