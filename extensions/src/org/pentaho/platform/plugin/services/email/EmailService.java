/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.plugin.services.email;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
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
    emailProperties.setProperty( "mail.smtp.auth", ObjectUtils.toString( emailConfig.isAuthenticate() ) );
    emailProperties.setProperty( "mail.smtp.ssl", ObjectUtils.toString( emailConfig.isUseSsl() ) );
    emailProperties.setProperty( "mail.debug", ObjectUtils.toString( emailConfig.isDebug() ) );

    Session session = null;
    if ( emailConfig.isAuthenticate() ) {
      Authenticator authenticator = new Authenticator() {
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication( emailConfig.getUserId(), emailConfig.getPassword() );
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
      Transport.send( msg );
      sendEmailMessage = "EmailTester.SUCESS";
    } catch ( Exception e ) {
      logger.error( messages.getString( "EmailService.NOT_CONFIGURED" ), e );
      sendEmailMessage = "EmailTester.FAIL";
    }
    return sendEmailMessage;
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

}
