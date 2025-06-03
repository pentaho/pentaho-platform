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


package org.pentaho.platform.plugin.services.email;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.util.XmlParseException;
import org.pentaho.platform.config.DtdEntityResolver;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.IOException;

@XmlRootElement
public class EmailConfigurationXml extends EmailConfiguration {

  private static final long serialVersionUID = -3938443547757985845L;

  private static final String ROOT_ELEMENT = "email-smtp"; //$NON-NLS-1$
  private static final String PASSWORD_XPATH = ROOT_ELEMENT + "/mail.password"; //$NON-NLS-1$
  private static final String AUTHENTICATE_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.auth"; //$NON-NLS-1$
  private static final String DEBUG_XPATH = ROOT_ELEMENT + "/properties/mail.debug"; //$NON-NLS-1$
  private static final String DEFAULT_FROM_XPATH = ROOT_ELEMENT + "/mail.from.default"; //$NON-NLS-1$
  private static final String FROM_NAME_XPATH = ROOT_ELEMENT + "/mail.from.name"; //$NON-NLS-1$
  private static final String SMTP_HOST_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.host"; //$NON-NLS-1$
  private static final String SMTP_PORT_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.port"; //$NON-NLS-1$
  private static final String SMTP_PROTOCOL_XPATH = ROOT_ELEMENT + "/properties/mail.transport.protocol"; //$NON-NLS-1$
  private static final String SMTP_QUIT_WAIT_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.quitwait"; //$NON-NLS-1$
  private static final String USER_ID_XPATH = ROOT_ELEMENT + "/mail.userid"; //$NON-NLS-1$
  private static final String USE_SSL_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.ssl"; //$NON-NLS-1$
  private static final String USE_START_TLS_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.starttls.enable"; //$NON-NLS-1$
  private static final String AUTH_MECHANISM_XPATH = ROOT_ELEMENT + "/properties/mail.smtp.auth.mechanisms"; //$NON-NLS-1$
  private static final String CLIENT_ID_XPATH = ROOT_ELEMENT + "/mail.client.id";  //$NON-NLS-1$
  private static final String CLIENT_SECRET_XPATH = ROOT_ELEMENT + "/mail.client.secret";  //$NON-NLS-1$
  private static final String TOKEN_URL_XPATH = ROOT_ELEMENT + "/mail.token.url";  //$NON-NLS-1$
  private static final String SCOPE_XPATH = ROOT_ELEMENT + "/mail.scope";  //$NON-NLS-1$
  private static final String GRANT_TYPE_XPATH = ROOT_ELEMENT + "/mail.grant.type";  //$NON-NLS-1$
  private static final String REFRESH_TOKEN_XPATH = ROOT_ELEMENT + "/mail.refresh.token";  //$NON-NLS-1$
  private static final String AUTHORIZATION_CODE_XPATH = ROOT_ELEMENT + "/mail.authorization.code";  //$NON-NLS-1$
  private static final String REDIRECT_URI_XPATH = ROOT_ELEMENT + "/mail.redirect.uri";  //$NON-NLS-1$
  private static final Integer MIN_PORT_NUMBER = 0;
  private static final Integer MAX_PORT_NUMBER = 65535;

  private static final Log logger = LogFactory.getLog( EmailConfigurationXml.class );
  private static final Messages messages = Messages.getInstance();

  private static final String DEFAULT_EMAIL_CONFIG_PATH = "system" + File.separator + "smtp-email" + File.separator
      + "email_config.xml";

  public EmailConfigurationXml() throws Exception {
    this( new File( PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_EMAIL_CONFIG_PATH ) ) );
  }

  public EmailConfigurationXml( File pentahoXmlFile ) throws IOException, DocumentException {
    if ( null == pentahoXmlFile ) {
      throw new IllegalArgumentException();
    }
    loadFromConfigurationDocument( XmlDom4JHelper.getDocFromFile( pentahoXmlFile, new DtdEntityResolver() ) );
  }

  public EmailConfigurationXml( String xml ) throws DocumentException, XmlParseException {
    if ( null == xml ) {
      throw new IllegalArgumentException();
    }
    loadFromConfigurationDocument( XmlDom4JHelper.getDocFromString( xml, new DtdEntityResolver() ) );
  }

  /**
   * Protected empty constructor for testing and subclasses
   */
  protected EmailConfigurationXml( final Document doc ) throws DocumentException {
    if ( doc == null ) {
      throw new IllegalArgumentException();
    }
    loadFromConfigurationDocument( doc );
  }

  private void loadFromConfigurationDocument( final Document doc ) throws DocumentException {
    final Element rootElement = doc.getRootElement();
    if ( ( rootElement != null ) && !doc.getRootElement().getName().equals( ROOT_ELEMENT ) ) {
      throw new DocumentException( messages.getErrorString( "EmailConfigurationXml.ERROR_0002_INVALID_ROOT_ELEMENT" ) ); //$NON-NLS-1$
    }

    setSmtpHost( getStringValue( doc, SMTP_HOST_XPATH ) );
    setSmtpPort( getIntegerPortValue( doc, SMTP_PORT_XPATH ) );
    setSmtpProtocol( getStringValue( doc, SMTP_PROTOCOL_XPATH ) );
    setUseStartTls( getBooleanValue( doc, USE_START_TLS_XPATH ) );
    setAuthenticate( getBooleanValue( doc, AUTHENTICATE_XPATH ) );
    setUseSsl( getBooleanValue( doc, USE_SSL_XPATH ) );
    setDebug( getBooleanValue( doc, DEBUG_XPATH ) );
    setSmtpQuitWait( getBooleanValue( doc, SMTP_QUIT_WAIT_XPATH ) );

    setDefaultFrom( getStringValue( doc, DEFAULT_FROM_XPATH ) );
    setFromName( getStringValue( doc, FROM_NAME_XPATH ) );
    setUserId( getStringValue( doc, USER_ID_XPATH ) );
    String encryptedPass = getStringValue( doc, PASSWORD_XPATH );
    if ( !StringUtils.isEmpty( encryptedPass ) ) {
      setPassword( Encr.decryptPasswordOptionallyEncrypted( encryptedPass ) );
    }
    setAuthMechanism( getStringValue( doc, AUTH_MECHANISM_XPATH ) );
    setTokenUrl( getStringValue( doc, TOKEN_URL_XPATH ) );
    setClientId( getStringValue( doc, CLIENT_ID_XPATH ) );
    String encryptedSecret = getStringValue( doc, CLIENT_SECRET_XPATH );
    if ( !StringUtils.isEmpty( encryptedSecret ) ) {
      setClientSecret( Encr.decryptPasswordOptionallyEncrypted( encryptedSecret ) );
    }

    setScope( getStringValue( doc, SCOPE_XPATH ) );
    setGrantType( getStringValue( doc, GRANT_TYPE_XPATH ) );
    setRefreshToken( getStringValue( doc, REFRESH_TOKEN_XPATH ) );
    setAuthorizationCode( getStringValue( doc, AUTHORIZATION_CODE_XPATH ) );
    setRedirectUri( getStringValue( doc, REDIRECT_URI_XPATH ) );
  }

  private static String getStringValue( final Document doc, final String xpath ) {
    try {
      final Element element = (Element) doc.selectSingleNode( xpath );
      return element != null ? element.getText() : null;
    } catch ( Exception e ) {
      logger.error( messages.getErrorString( "EmailConfigurationXml.ERROR_0001_ERROR_PARSING_DATA", e
          .getLocalizedMessage() ) );
    }
    return null;
  }

  private static Integer getIntegerPortValue( final Document doc, final String xpath ) {
    try {
      final Element element = (Element) doc.selectSingleNode( xpath );
      Integer value = Integer.MIN_VALUE;
      if ( element != null && !StringUtils.isEmpty( element.getText() ) ) {
        value = Integer.parseInt( element.getText() );
      }
      if ( value >= MIN_PORT_NUMBER && value <= MAX_PORT_NUMBER ) {
        return value;
      }
    } catch ( Exception e ) {
      logger.error( messages.getErrorString( "EmailConfigurationXml.ERROR_0001_ERROR_PARSING_DATA", e
          .getLocalizedMessage() ) );
    }
    return Integer.MIN_VALUE;
  }

  private static boolean getBooleanValue( final Document doc, final String xpath ) {
    try {
      final Element element = (Element) doc.selectSingleNode( xpath );
      return element != null && Boolean.parseBoolean( element.getText() );
    } catch ( Exception e ) {
      logger.error( messages.getErrorString( "EmailConfigurationXml.ERROR_0001_ERROR_PARSING_DATA", e
          .getLocalizedMessage() ) );
    }
    return false;
  }

  private static void setValue( final Document doc, final String xPath, final String value ) {
    Element element = (Element) doc.selectSingleNode( xPath );
    if ( null == element ) {
      element = DocumentHelper.makeElement( doc, xPath );
    }
    element.setText( value );
  }

  public Document getDocument() {
    return EmailConfigurationXml.getDocument( this );
  }

  public static Document getDocument( final IEmailConfiguration emailConfiguration ) {
    final Document document = DocumentHelper.createDocument();
    document.addElement( ROOT_ELEMENT );
    setValue( document, SMTP_HOST_XPATH, ObjectUtils.toString( emailConfiguration.getSmtpHost() ) );
    setValue( document, SMTP_PORT_XPATH, ObjectUtils.toString( emailConfiguration.getSmtpPort() ) );
    setValue( document, SMTP_PROTOCOL_XPATH, ObjectUtils.toString( emailConfiguration.getSmtpProtocol() ) );
    setValue( document, USE_START_TLS_XPATH, ObjectUtils.toString( emailConfiguration.isUseStartTls(), Boolean.FALSE
        .toString() ) );
    setValue( document, AUTHENTICATE_XPATH, ObjectUtils.toString( emailConfiguration.isAuthenticate(), Boolean.FALSE
        .toString() ) );
    setValue( document, USE_SSL_XPATH, ObjectUtils.toString( emailConfiguration.isUseSsl(),
      Boolean.FALSE.toString() ) );
    setValue( document, DEBUG_XPATH, ObjectUtils.toString( emailConfiguration.isDebug(), Boolean.FALSE.toString() ) );
    setValue( document, SMTP_QUIT_WAIT_XPATH, ObjectUtils.toString( emailConfiguration.isSmtpQuitWait(), Boolean.FALSE
        .toString() ) );

    setValue( document, DEFAULT_FROM_XPATH, ObjectUtils.toString( emailConfiguration.getDefaultFrom() ) );
    setValue( document, FROM_NAME_XPATH, ObjectUtils.toString( emailConfiguration.getFromName() ) );
    setValue( document, USER_ID_XPATH, ObjectUtils.toString( emailConfiguration.getUserId() ) );
    String rawPassword = ObjectUtils.toString( emailConfiguration.getPassword() );
    setValue( document, PASSWORD_XPATH,
      StringUtils.isEmpty( rawPassword ) ? "" : Encr.encryptPasswordIfNotUsingVariables( rawPassword ) );
    setValue( document, AUTH_MECHANISM_XPATH, ObjectUtils.toString( emailConfiguration.getAuthMechanism() ) );
    setValue( document, CLIENT_ID_XPATH, ObjectUtils.toString( emailConfiguration.getClientId() ) );
    String rawSecret = ObjectUtils.toString( emailConfiguration.getClientSecret() );
    setValue( document, CLIENT_SECRET_XPATH,
      StringUtils.isEmpty( rawSecret ) ? "" : Encr.encryptPasswordIfNotUsingVariables( rawSecret ) );
    setValue( document, SCOPE_XPATH, ObjectUtils.toString( emailConfiguration.getScope() ) );
    setValue( document, GRANT_TYPE_XPATH, ObjectUtils.toString( emailConfiguration.getGrantType() ) );
    setValue( document, REFRESH_TOKEN_XPATH, ObjectUtils.toString( emailConfiguration.getRefreshToken() ) );
    setValue( document, AUTHORIZATION_CODE_XPATH, ObjectUtils.toString( emailConfiguration.getAuthorizationCode() ) );
    setValue( document, TOKEN_URL_XPATH, ObjectUtils.toString( emailConfiguration.getTokenUrl() ) );
    setValue( document, REDIRECT_URI_XPATH, ObjectUtils.toString( emailConfiguration.getRedirectUri() ) );
    return document;
  }
}
