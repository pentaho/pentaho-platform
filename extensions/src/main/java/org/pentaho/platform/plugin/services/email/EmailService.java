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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
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
import org.pentaho.platform.api.email.IEmailAuthenticationResponse;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.messages.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

  private static final String REFRESH_TOKEN = "refresh_token";

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
    final Properties emailProperties = new Properties();
    emailProperties.setProperty( "mail.smtp.host", emailConfig.getSmtpHost() );
    emailProperties.setProperty( "mail.smtp.port", ObjectUtils.toString( emailConfig.getSmtpPort() ) );
    emailProperties.setProperty( "mail.transport.protocol", emailConfig.getSmtpProtocol() );
    emailProperties.setProperty( "mail.smtp.starttls.enable", ObjectUtils.toString( emailConfig.isUseStartTls() ) );
    emailProperties.setProperty( "mail.smtp.ssl", ObjectUtils.toString( emailConfig.isUseSsl() ) );
    emailProperties.setProperty( "mail.debug", ObjectUtils.toString( emailConfig.isDebug() ) );

    Session session = null;
    IEmailAuthenticationResponse token = null;
    if ( emailConfig.getAuthMechanism().equals( "XOAUTH2" ) ) {
      try {
        token = getOAuthToken( emailConfig );
        if ( emailConfig.getGrantType().equals( "authorization_code"  ) ) {
          emailConfig.setGrantType( REFRESH_TOKEN );
          emailConfig.setRefreshToken( token.getRefresh_token() );
          emailConfig.setAuthorizationCode( "" );
        }
        session = Session.getInstance( emailProperties );
      } catch ( Exception e ) {
        return "EmailTester.FAIL";
      }
    } else if ( emailConfig.isAuthenticate() ) {
      emailProperties.setProperty( "mail.smtp.auth", ObjectUtils.toString( emailConfig.isAuthenticate() ) );
      Authenticator authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          String decoded;
          String tmp = emailConfig.getPassword();
          try {
            byte[] b;
            if ( tmp.startsWith( "ENC:" ) ) {
              b = Base64Utils.fromBase64( tmp.substring( 4, tmp.length() ) );
            } else {
              b = Base64Utils.fromBase64( tmp );
            }
            decoded = new String( b, "UTF-8" );
          } catch ( Exception e ) {
            decoded = tmp;
          }
          return new PasswordAuthentication( emailConfig.getUserId(), decoded );
        }
      };
      session = Session.getInstance( emailProperties, authenticator );
    } else {
      session = Session.getInstance( emailProperties );
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
      sendEmail( emailConfig, token, session, msg );
      sendEmailMessage = "EmailTester.SUCESS";
    } catch ( Exception e ) {
      logger.error( messages.getString( "EmailService.NOT_CONFIGURED" ), e );
      sendEmailMessage = "EmailTester.FAIL";
    }
    setEmailConfig( emailConfig );
    return sendEmailMessage;
  }

  private void sendEmail( final IEmailConfiguration emailConfig, IEmailAuthenticationResponse token, Session session, MimeMessage msg ) throws Exception {
    try {
      if ( emailConfig.getAuthMechanism().equals( "XOAUTH2" ) ) {
        if ( emailConfig.getSmtpProtocol().equalsIgnoreCase( "graph_api" ) ) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          msg.writeTo( os );
          String s = Base64.getEncoder().encodeToString( os.toByteArray() );
          sendMailGraphApi( emailConfig, token.getAccess_token(), s );
        } else {
          SMTPTransport transport = new SMTPTransport( session, null );
          transport.connect( emailConfig.getSmtpHost(), emailConfig.getUserId(), null );
          transport.issueCommand( "AUTH XOAUTH2 " + Base64.getEncoder().encode( String.format( "user=%s\1auth=Bearer %s\1\1", emailConfig.getUserId(), token.getAccess_token() ).getBytes() ), 235 );
          transport.sendMessage( msg, msg.getAllRecipients() );
        }
      } else {
        Transport.send( msg );
      }
    } catch ( Exception e ) {
      throw e;
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
              "EmailService.ERROR_0001_INVALID_CONFIG_FILE_LOCATION", (Object) null ) );
    }
    if ( emailConfigFile.exists() ) {
      if ( emailConfigFile.isDirectory() ) {
        throw new IllegalArgumentException( messages.getErrorString(
                "EmailService.ERROR_0001_INVALID_CONFIG_FILE_LOCATION", emailConfigFile.getAbsolutePath() ) );
      }
    } else {
      final File parentFolder = emailConfigFile.getAbsoluteFile().getParentFile();
      if ( !parentFolder.exists() || !parentFolder.isDirectory() ) {
        throw new IllegalArgumentException( messages.getErrorString(
                "EmailService.ERROR_0001_INVALID_CONFIG_FILE_LOCATION", emailConfigFile.getAbsolutePath() ) );
      }
    }

    logger.debug( "Setting the email configuration file to [" + emailConfigFile.getAbsolutePath() + "]" );
    logger.debug( "\temail config file exists = " + emailConfigFile.exists() );
    this.emailConfigFile = emailConfigFile;
  }

  public boolean isValid() {
    try {
      EmailConfiguration c = new EmailConfigurationXml( emailConfigFile );
      if ( !StringUtils.isEmpty( c.getSmtpHost() ) && !StringUtils.isEmpty( c.getSmtpProtocol() )
              && !StringUtils.isEmpty( c.getDefaultFrom() ) ) {
        if ( c.isAuthenticate() ) {
          return !StringUtils.isEmpty( c.getUserId() ) && !StringUtils.isEmpty( c.getPassword() );
        }
        return true;
      }
    } catch ( Exception e ) {
      //ignore
    }
    return false;
  }

  public IEmailAuthenticationResponse getOAuthToken( final IEmailConfiguration emailConfig ) throws Exception {
    String tokenUrl = emailConfig.getTokenUrl();
    String clientId = emailConfig.getClientId();
    String clientSecret = emailConfig.getClientSecret();
    String scope = emailConfig.getScope();
    String grantType = emailConfig.getGrantType();
    String code = emailConfig.getAuthorizationCode();
    String refreshToken = emailConfig.getRefreshToken();
    String redirectUri = emailConfig.getRedirectUri();
    CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient();
    try {
      HttpPost httpPost = new HttpPost( tokenUrl );
      List<NameValuePair> form = new ArrayList<>();
      form.add( new BasicNameValuePair( "grant_type", grantType ) );
      form.add( new BasicNameValuePair( "scope", scope ) );
      form.add( new BasicNameValuePair( "client_id", clientId ) );
      form.add( new BasicNameValuePair( "client_secret", clientSecret ) );
      if ( grantType.equals( REFRESH_TOKEN ) ) {
        form.add( new BasicNameValuePair( REFRESH_TOKEN, refreshToken ) );
      }
      if ( grantType.equals( "authorization_code" ) ) {
        form.add( new BasicNameValuePair( "code", code ) );
        form.add( new BasicNameValuePair( "redirect_uri", redirectUri ) );
      }
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
      httpPost.setEntity( entity );
      CloseableHttpResponse response = client.execute( httpPost );
      try {
        if ( !( response.getStatusLine().getStatusCode() == HttpStatus.SC_OK ) ) {
          throw new Exception( "Unable to get authorization token " + response.getStatusLine().toString() );
        }
        String returnCode = EntityUtils.toString( response.getEntity() );
        ObjectMapper mapper = new ObjectMapper();
        EmailAuthenticationResponse authTokenResponse =
                mapper.readValue( returnCode, EmailAuthenticationResponse.class );
        return authTokenResponse;
      } finally {
        response.close();
      }
    } catch ( Exception ex ) {
      throw( ex );
    } finally {
      client.close();
    }
  }

  public void sendMailGraphApi( final IEmailConfiguration emailConfig, String accessToken, String message ) throws Exception {
    CloseableHttpClient client = HttpClientManager.getInstance().createDefaultClient();
    try {
      HttpPost httpPost = new HttpPost( "https://graph.microsoft.com/v1.0/users/" + emailConfig.getUserId() + "/sendMail" );
      httpPost.setHeader( " Authorization ", "Bearer " + accessToken );
      StringEntity entity = new StringEntity( message, ContentType.TEXT_PLAIN );
      httpPost.setEntity( entity );
      httpPost.setHeader( "Content-type", "text/plain" );
      CloseableHttpResponse response = client.execute( httpPost );
      try {
        if ( !( response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED ) ) {
          throw new Exception( response.getStatusLine().toString() );
        }
      } finally {
        response.close();
      }
    } catch ( Exception ex ) {
      throw( ex );
    } finally {
      client.close();
    }
  }

}
