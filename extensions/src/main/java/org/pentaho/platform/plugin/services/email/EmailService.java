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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gwt.user.server.Base64Utils;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.HttpException;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.platform.api.email.EmailServiceException;
import org.pentaho.platform.api.email.IEmailAuthenticationResponse;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.EmailConstants;
import org.pentaho.platform.util.EncryptedPasswordAuthenticator;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Manages the email connection information
 *
 * @author <a href="mailto:dkincade@pentaho.com">David M. Kincade</a>
 */
public class EmailService implements IEmailService {
  /**
   * Messages class
   */
  private static final Messages messages = Messages.getInstance();

  /**
   * The location of the default email configuration file
   */
  private static final String DEFAULT_EMAIL_CONFIG_PATH = "system" + File.separator + "smtp-email" + File.separator
          + "email_config.xml";

  /**
   * The logger for this class
   */
  private static final Log logger = LogFactory.getLog( EmailService.class );

  /**
   * The file which contains the email configuration information. This is guaranteed to be NOT NULL by the
   * setEmailConfigFile() method
   */
  private File emailConfigFile;

  public static final String GRANT_TYPE = "grant_type";
  public static final String SCOPE = "scope";
  public static final String CLIENT_ID = "client_id";
  public static final String CLIENT_SECRET = "client_secret";
  public static final String CODE = "code";
  public static final String REDIRECT_URI = "redirect_uri";
  public static final String TEST_EMAIL_FAIL = "EmailTester.FAIL";
  public static final String TEST_EMAIL_SUCCESS = "EmailTester.SUCESS";
  private static final String OAUTH2_COMMAND = "AUTH XOAUTH2 ";
  private static final String USER_TOKEN_STRING = "user=%s\1auth=Bearer %s\1\1";
  private static final String INVALID_CONFIG_FILE_LOCATION = "EmailService.ERROR_0001_INVALID_CONFIG_FILE_LOCATION";
  /**
   * The success code for SMTP Authentication being successful
   */
  private static final int SMTP_SC_AUTH_SUCCESS = 235;

  private static final String GRAPH_API_URL = "https://graph.microsoft.com/v1.0/users/{userId}/sendMail";

  /**
   * Constructs an instance of this class using the default settings location as defined by PentahoSystem
   *
   * @throws IllegalArgumentException
   *           Indicates that the default location for the email configuration file is invalid
   */
  public EmailService() throws IllegalArgumentException {
    logger.debug( "Using the default email configuration filename [" + DEFAULT_EMAIL_CONFIG_PATH + "]" );
    final String emailConfigFilePath =
            PentahoSystem.getApplicationContext().getSolutionPath( DEFAULT_EMAIL_CONFIG_PATH );
    logger.debug( "System converted default email configuration filename to [" + emailConfigFilePath + "]" );
    setEmailConfigFile( new File( emailConfigFilePath ) );
  }

  /**
   * Constructs an instance of this class using the specified file location as the source of the email configuration
   *
   * @param emailConfigFile
   *          the file reference to the email configuration
   * @throws IllegalArgumentException
   *           indicates the argument is either null or references a location which is invalid (the parent folder of the
   *           specified file doesn't exist)
   */
  public EmailService( final File emailConfigFile ) throws IllegalArgumentException {
    setEmailConfigFile( emailConfigFile );
  }

  public void setEmailConfig( final IEmailConfiguration emailConfiguration ) {
    if ( emailConfiguration == null ) {
      throw new IllegalArgumentException( messages.getErrorString( "EmailService.ERROR_0002_NULL_CONFIGURATION" ) );
    }

    final Document document = EmailConfigurationXml.getDocument( emailConfiguration );
    try {
      emailConfigFile.createNewFile();
      final FileOutputStream fileOutputStream = new FileOutputStream( emailConfigFile );
      XmlDom4JHelper.saveDom( document, fileOutputStream, "UTF-8" );
      fileOutputStream.close();
    } catch ( IOException e ) {
      logger.error( messages.getErrorString( "EmailService.ERROR_0003_ERROR_CREATING_EMAIL_CONFIG_FILE", e
              .getLocalizedMessage() ) );
    }
  }

  /**
   * TODO document
   *
   * @return
   */
  public EmailConfiguration getEmailConfig() {
    try {
      return new EmailConfigurationXml( emailConfigFile );
    } catch ( Exception e ) {
      logger.error( messages.getErrorString( "EmailService.ERROR_0004_LOADING_EMAIL_CONFIG_FILE", e
              .getLocalizedMessage() ) );
      return new EmailConfiguration();
    }
  }

  /**
   * Tests the provided email configuration by sending a test email. This will just indicate that the server
   * configuration is correct and a test email was successfully sent. It does not test the destination address.
   *
   * @param emailConfig
   *          the email configuration to test
   * @throws Exception
   *           indicates an error running the test (as in an invalid configuration)
   */
  public String sendEmailTest( final IEmailConfiguration emailConfig ) {
    setEmailConfig( emailConfig );
    final Properties emailProperties = new Properties();
    emailProperties.setProperty( "mail.smtp.host", emailConfig.getSmtpHost() );
    emailProperties.setProperty( "mail.smtp.port", ObjectUtils.toString( emailConfig.getSmtpPort() ) );
    emailProperties.setProperty( "mail.transport.protocol", emailConfig.getSmtpProtocol() );
    emailProperties.setProperty( "mail.smtp.starttls.enable", ObjectUtils.toString( emailConfig.isUseStartTls() ) );
    emailProperties.setProperty( "mail.smtp.ssl", ObjectUtils.toString( emailConfig.isUseSsl() ) );
    emailProperties.setProperty( "mail.debug", ObjectUtils.toString( emailConfig.isDebug() ) );

    Session session = null;
    if ( emailConfig.getAuthMechanism().equals( EmailConstants.AUTH_TYPE_XOAUTH2 ) || !emailConfig.isAuthenticate() ) {
      session = Session.getInstance( emailProperties );
    } else {
      emailProperties.setProperty( "mail.smtp.auth", "true" );
      session = Session.getInstance( emailProperties, new EncryptedPasswordAuthenticator( emailConfig.getUserId(),
              emailConfig.getPassword() ) );
    }
    String sendEmailMessage = "";
    try {
      MimeMessage msg = new MimeMessage( session );
      msg.setFrom( new InternetAddress( emailConfig.getDefaultFrom(), emailConfig.getFromName() ) );
      msg.setRecipients( Message.RecipientType.TO, InternetAddress.parse( emailConfig.getDefaultFrom() ) );
      msg.setSubject( messages.getString( "EmailService.SUBJECT" ) );
      msg.setText( messages.getString( "EmailService.MESSAGE" ) );
      msg.setHeader( "X-Mailer", "smtpsend" );
      msg.setSentDate( new Date() );
      sendEmail( session, msg );
      sendEmailMessage = TEST_EMAIL_SUCCESS;
    } catch ( Exception e ) {
      logger.error( messages.getString( "EmailService.NOT_CONFIGURED" ), e );
      sendEmailMessage = TEST_EMAIL_FAIL;
    }
    return sendEmailMessage;
  }

  public void sendEmail( Session session, MimeMessage msg ) throws EmailServiceException {
    final IEmailConfiguration emailConfig = getEmailConfig();
    try {
      if ( emailConfig.getAuthMechanism().equals( EmailConstants.AUTH_TYPE_XOAUTH2 ) ) {
        IEmailAuthenticationResponse token = getOAuthToken( emailConfig );
        if ( emailConfig.getGrantType().equals( EmailConstants.GRANT_TYPE_AUTH_CODE ) ) {
          emailConfig.setGrantType( EmailConstants.GRANT_TYPE_REFRESH_TOKEN );
          emailConfig.setRefreshToken( token.getRefreshToken() );
          emailConfig.setAuthorizationCode( "" );
          setEmailConfig( emailConfig );
        }
        if ( emailConfig.getSmtpProtocol().equals( EmailConstants.PROTOCOL_GRAPH_API ) ) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          msg.writeTo( os );
          String s = Base64.getEncoder().encodeToString( os.toByteArray() );
          sendMailGraphApi( emailConfig, token.getAccessToken(), s  );
        } else {
          SMTPTransport transport = new SMTPTransport( session, null );
          transport.connect( emailConfig.getSmtpHost(), emailConfig.getUserId(), null );
          String userTokenString = String.format( USER_TOKEN_STRING, emailConfig.getUserId(), token.getAccessToken() );
          String smtpCommand = OAUTH2_COMMAND + Base64Utils.toBase64( userTokenString.getBytes() );
          transport.issueCommand( smtpCommand, SMTP_SC_AUTH_SUCCESS );
          transport.sendMessage( msg, msg.getAllRecipients() );
        }
      } else {
        Transport.send( msg ); //sends message to all recipients set in the msg object.
      }
    } catch ( Exception he ) {
      throw new EmailServiceException( he.getMessage() );
    }
  }

  /**
   * Validates and sets the email configuration file to the specified value.
   *
   * @param emailConfigFile
   *          the email configuration file to use
   * @throws IllegalArgumentException
   *           indicates that the provided value is invalid (the value is null or the parent folder does not exist)
   */
  protected void setEmailConfigFile( final File emailConfigFile ) throws IllegalArgumentException {
    // Make sure the email config file provided isn't NULL, isn't a folder, or is specified to exist in a folder
    if ( emailConfigFile == null ) {
      throw new IllegalArgumentException( messages.getErrorString(
              INVALID_CONFIG_FILE_LOCATION, (Object) null ) );
    }
    if ( emailConfigFile.exists() ) {
      if ( emailConfigFile.isDirectory() ) {
        throw new IllegalArgumentException( messages.getErrorString(
                INVALID_CONFIG_FILE_LOCATION, emailConfigFile.getAbsolutePath() ) );
      }
    } else {
      final File parentFolder = emailConfigFile.getAbsoluteFile().getParentFile();
      if ( !parentFolder.exists() || !parentFolder.isDirectory() ) {
        throw new IllegalArgumentException( messages.getErrorString(
                INVALID_CONFIG_FILE_LOCATION, emailConfigFile.getAbsolutePath() ) );
      }
    }

    logger.debug( "Setting the email configuration file to [" + emailConfigFile.getAbsolutePath() + "]" );
    logger.debug( "\temail config file exists = " + emailConfigFile.exists() );
    this.emailConfigFile = emailConfigFile;
  }

  public boolean isValid() {
    try {
      EmailConfiguration c = new EmailConfigurationXml( emailConfigFile );
      return isValid( c );
    } catch ( Exception e ) {
      //ignore
    }
    return false;
  }

  protected boolean isValid( final EmailConfiguration c ) {
    if ( c.getAuthMechanism().equalsIgnoreCase( EmailConstants.AUTH_TYPE_XOAUTH2 ) ) {
      if ( c.getGrantType().equalsIgnoreCase( EmailConstants.GRANT_TYPE_CLIENT_CREDENTIALS ) ) {
        return !StringUtils.isEmpty( c.getClientId() ) && !StringUtils.isEmpty( c.getClientSecret() )
                && !StringUtils.isEmpty( c.getDefaultFrom() );
      } else if ( c.getGrantType().equalsIgnoreCase( EmailConstants.GRANT_TYPE_REFRESH_TOKEN ) ) {
        return !StringUtils.isEmpty( c.getClientId() ) && !StringUtils.isEmpty( c.getClientSecret() )
                && !StringUtils.isEmpty( c.getRefreshToken() ) && !StringUtils.isEmpty( c.getDefaultFrom() );
      } else {
        return !StringUtils.isEmpty( c.getClientId() ) && !StringUtils.isEmpty( c.getClientSecret() )
                && !StringUtils.isEmpty( c.getAuthorizationCode() ) && !StringUtils.isEmpty( c.getRedirectUri() )
                && !StringUtils.isEmpty( c.getDefaultFrom() );
      }
    } else if ( !StringUtils.isEmpty( c.getSmtpHost() ) && !StringUtils.isEmpty( c.getSmtpProtocol() )
            && !StringUtils.isEmpty( c.getDefaultFrom() ) ) {
      if ( c.isAuthenticate() ) {
        return !StringUtils.isEmpty( c.getUserId() ) && !StringUtils.isEmpty( c.getPassword() );
      }
      return true;
    }
    return false;
  }

  protected IEmailAuthenticationResponse getOAuthToken( final IEmailConfiguration emailConfig ) throws IOException,
          HttpException {
    String grantType = emailConfig.getGrantType();
    try ( CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient() ) {
      HttpPost httpPost = new HttpPost( emailConfig.getTokenUrl() );
      List<NameValuePair> form = new ArrayList<>();
      form.add( new BasicNameValuePair( GRANT_TYPE, grantType ) );
      form.add( new BasicNameValuePair( SCOPE, emailConfig.getScope() ) );
      form.add( new BasicNameValuePair( CLIENT_ID, emailConfig.getClientId() ) );
      form.add( new BasicNameValuePair( CLIENT_SECRET, emailConfig.getClientSecret() ) );
      if ( grantType.equals( EmailConstants.GRANT_TYPE_REFRESH_TOKEN ) ) {
        form.add( new BasicNameValuePair( EmailConstants.GRANT_TYPE_REFRESH_TOKEN, emailConfig.getRefreshToken() ) );
      }
      if ( grantType.equals( EmailConstants.GRANT_TYPE_AUTH_CODE ) ) {
        form.add( new BasicNameValuePair( CODE, emailConfig.getAuthorizationCode() ) );
        form.add( new BasicNameValuePair( REDIRECT_URI, emailConfig.getRedirectUri() ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
      httpPost.setEntity( entity );
      try ( CloseableHttpResponse response = client.execute( httpPost ) ) {
        if ( response.getStatusLine().getStatusCode() != HttpStatus.SC_OK ) {
          throw new HttpException( "Unable to get authorization token " + response.getStatusLine().toString() );
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue( EntityUtils.toString( response.getEntity() ), EmailAuthenticationResponse.class );
      }
    }
  }

  protected void sendMailGraphApi( final IEmailConfiguration emailConfig, String accessToken, String message ) throws
          HttpException, IOException {
    String urlEncodedUserId = URLEncoder.encode( emailConfig.getDefaultFrom(), StandardCharsets.UTF_8.name() );
    String uri = GRAPH_API_URL.replace( "{userId}", urlEncodedUserId );
    sendMailGraphApi( uri, accessToken, message );
  }

  protected void sendMailGraphApi( String uri, String accessToken, String message ) throws HttpException, IOException {
    try ( CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient() ) {
      HttpPost httpPost = new HttpPost( uri );
      httpPost.setHeader( "Authorization", "Bearer " + accessToken );
      StringEntity entity = new StringEntity( message, ContentType.TEXT_PLAIN );
      httpPost.setEntity( entity );
      httpPost.setHeader( "Content-type", "text/plain" );
      try ( CloseableHttpResponse response = client.execute( httpPost ) ) {
        if (  response.getStatusLine().getStatusCode() != HttpStatus.SC_ACCEPTED ) {
          throw new HttpException( response.getStatusLine().toString() );
        }
      }
    }
  }

}
