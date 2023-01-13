/*!
 *
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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.messages.Messages;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;

public class Emailer {

  private static final Log logger = LogFactory.getLog( Emailer.class );
  private static final String MAILER = "smtpsend"; //$NON-NLS-1$
  private static final String EMBEDDED_HTML = "mime-message/text/html";

  private Properties props = new Properties();
  private InputStream attachment = null;
  private String attachmentName = null;
  private String attachmentMimeType = null;
  private Authenticator authenticator = null;

  private String authMechanism;

  private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";

  IEmailService service;


  public Emailer() {
    this.service =
            PentahoSystem.get( IEmailService.class, "IEmailService", PentahoSessionHolder.getSession() );
  }

  public Emailer( IEmailService emailService ) {
    this.service = emailService;
  }

  public void setTo( String to ) {
    if ( to != null && !"".equals( to ) ) {
      to = to.replaceAll( ";", "," );
      props.put( "to", to );
    }
  }

  public void setCc( String cc ) {
    if ( cc != null && !"".equals( cc ) ) {
      cc = cc.replaceAll( ";", "," );
      props.put( "cc", cc );
    }
  }

  public void setBcc( String bcc ) {
    if ( bcc != null && !"".equals( bcc ) ) {
      bcc = bcc.replaceAll( ";", "," );
      props.put( "bcc", bcc );
    }
  }

  public void setSubject( String subject ) {
    props.put( "subject", cleanEmailField( subject ) );
  }

  public String getSubject() {
    return props.getProperty( "subject" );
  }

  public void setFrom( String from ) {
    props.put( "mail.from.default", from );
  }

  public void setFromName( String fromName ) {
    props.put( "mail.from.name", fromName );
  }

  public void setUseAuthentication( boolean useAuthentication ) {
    props.put( MAIL_SMTP_AUTH, ObjectUtils.toString( useAuthentication ) );
  }

  public void setSmtpHost( String smtpHost ) {
    props.put( "mail.smtp.host", smtpHost );
  }

  public void setSmtpPort( int port ) {
    props.put( "mail.smtp.port", ObjectUtils.toString( port ) );
  }

  public void setTransportProtocol( String protocol ) {
    props.put( "mail.transport.protocol", protocol );
  }

  public void setUseSSL( boolean useSSL ) {
    props.put( "mail.smtp.ssl", ObjectUtils.toString( useSSL ) );
  }

  public void setStartTLS( boolean startTLS ) {
    props.put( "mail.smtp.starttls.enable", ObjectUtils.toString( startTLS ) );
  }

  public void setQuitWait( boolean quitWait ) {
    props.put( "mail.smtp.quitwait", ObjectUtils.toString( quitWait ) );
  }

  public void setAttachment( InputStream attachment ) {
    this.attachment = attachment;
  }

  public void setAttachmentName( String attachmentName ) {
    this.attachmentName = attachmentName;
  }

  public String getAttachmentName() {
    return attachmentName;
  }

  public void setAttachmentMimeType( String mimeType ) {
    this.attachmentMimeType = mimeType;
  }

  public Authenticator getAuthenticator() {
    return authenticator;
  }

  public void setAuthenticator( Authenticator authenticator ) {
    this.authenticator = authenticator;
  }

  public void setBody( String body ) {
    props.put( "body", body );
  }

  public String getEmailFromName() {
    return Messages.getInstance().getString( "emailFromName" ); //$NON-NLS-1$
  }

  public Properties getProperties() {
    return props;
  }

  public String getAuthMechanism() {
    return authMechanism == null ? "" : authMechanism;
  }

  public void setAuthMechanism( final String authMechanism ) {
    this.authMechanism = authMechanism;
  }

  public boolean setup() {
    try {
      props.put( "mail.smtp.host", service.getEmailConfig().getSmtpHost() );
      props.put( "mail.smtp.port", ObjectUtils.toString( service.getEmailConfig().getSmtpPort() ) );
      props.put( "mail.transport.protocol", service.getEmailConfig().getSmtpProtocol() );
      props.put( "mail.smtp.starttls.enable", ObjectUtils.toString( service.getEmailConfig().isUseStartTls() ) );
      props.put( "mail.smtp.ssl", ObjectUtils.toString( service.getEmailConfig().isUseSsl() ) );
      props.put( "mail.smtp.quitwait", ObjectUtils.toString( service.getEmailConfig().isSmtpQuitWait() ) );
      props.put( "mail.from.default", service.getEmailConfig().getDefaultFrom() );
      setAuthMechanism( service.getEmailConfig().getAuthMechanism() );
      String fromName = service.getEmailConfig().getFromName();
      if ( StringUtils.isEmpty( fromName ) ) {
        fromName = getEmailFromName();
      }
      props.put( "mail.from.name", fromName );
      props.put( "mail.debug", ObjectUtils.toString( service.getEmailConfig().isDebug() ) );

      if ( service.getEmailConfig().isAuthenticate() && EmailConstants.AUTH_TYPE_BASIC.equals( getAuthMechanism() ) ) {
        props.put( "mail.userid", service.getEmailConfig().getUserId() );
        props.put( "mail.password", service.getEmailConfig().getPassword() );
        props.put( MAIL_SMTP_AUTH, "true" );
        setAuthenticator( new EncryptedPasswordAuthenticator( service.getEmailConfig().getUserId(),
                service.getEmailConfig().getPassword() ) );
      }

      return true;
    } catch ( Exception e ) {
      logger.error( "Email.ERROR_0013_CONFIG_FILE_INVALID", e ); //$NON-NLS-1$
    }
    return false;
  }

  private String cleanEmailField( String emailField ) {
    if ( emailField != null ) {
      // Remove CR symbols - email header injection
      emailField = emailField.replaceAll( "\r", "" );
      return emailField.replaceAll( "\n", "" );
    } else {
      return null;
    }
  }

  public boolean send() {
    String from = props.getProperty( "mail.from.default" );
    String fromName = props.getProperty( "mail.from.name" );
    String to = props.getProperty( "to" );
    String cc = props.getProperty( "cc" );
    String bcc = props.getProperty( "bcc" );
    boolean authenticate = "true".equalsIgnoreCase( props.getProperty( MAIL_SMTP_AUTH ) );
    String subject = props.getProperty( "subject" );
    String body = props.getProperty( "body" );

    logger.info( "Going to send an email to " + to + " from " + from + " with the subject '" + subject
      + "' and the body " + body );

    try {
      // Get a Session object
      Session session;
      session = getSmtpSession( authenticate );

      // if debugging is not set in the email config file, then default to false
      if ( !props.containsKey( "mail.debug" ) ) { //$NON-NLS-1$
        session.setDebug( false );
      }

      final MimeMessage msg;

      if ( EMBEDDED_HTML.equals( attachmentMimeType ) ) {

        //Message is ready
        msg = attachment != null ? new MimeMessage( session, attachment ) : new MimeMessage( session );

        if ( body != null ) {
          //We need to add message to the top of the email body
          setBodyForEmbeddedHtml( msg, body );

        }
      } else {

        // construct the message
        msg = new MimeMessage( session );
        Multipart multipart = setMultipart( body );

        // add the Multipart to the message
        msg.setContent( multipart );
      }

      setToFromSubject( from, fromName, to, cc, bcc, subject, msg );
      msg.setHeader( "X-Mailer", Emailer.MAILER ); //$NON-NLS-1$
      msg.setSentDate( new Date() );


      service.sendEmail( session, msg );

      return true;
    } catch ( SendFailedException e ) {
      logger.error( "Email.ERROR_0011_SEND_FAILED -" + to, e ); //$NON-NLS-1$
    } catch ( AuthenticationFailedException e ) {
      logger.error( "Email.ERROR_0014_AUTHENTICATION_FAILED - " + to, e ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.error( "Email.ERROR_0011_SEND_FAILED - " + to, e ); //$NON-NLS-1$
    }
    return false;
  }

  protected Session getSmtpSession( boolean authenticate ) {
    if ( authenticate && EmailConstants.AUTH_TYPE_BASIC.equals( getAuthMechanism() ) ) {
      return Session.getInstance( props, authenticator );
    } else {
      return Session.getInstance( props );
    }
  }

  private void setToFromSubject( String from, String fromName, String to, String cc, String bcc, String subject,
                                 MimeMessage msg ) throws UnsupportedEncodingException, MessagingException {
    if ( from != null ) {
      msg.setFrom( new InternetAddress( from, fromName ) );
    } else {
      // There should be no way to get here
      logger.error( "Email.ERROR_0012_FROM_NOT_DEFINED" ); //$NON-NLS-1$
    }

    if ( ( to != null ) && ( to.trim().length() > 0 ) ) {
      msg.setRecipients( Message.RecipientType.TO, InternetAddress.parse( to, false ) );
    }
    if ( ( cc != null ) && ( cc.trim().length() > 0 ) ) {
      msg.setRecipients( Message.RecipientType.CC, InternetAddress.parse( cc, false ) );
    }
    if ( ( bcc != null ) && ( bcc.trim().length() > 0 ) ) {
      msg.setRecipients( Message.RecipientType.BCC, InternetAddress.parse( bcc, false ) );
    }

    if ( subject != null ) {
      msg.setSubject( subject, LocaleHelper.getSystemEncoding() );
    }
  }

  private Multipart setMultipart( String body ) throws MessagingException, IOException {
    Multipart multipart = new MimeMultipart();
    if ( body != null ) {
      MimeBodyPart bodyMessagePart = new MimeBodyPart();
      bodyMessagePart.setText( body, LocaleHelper.getSystemEncoding() );
      multipart.addBodyPart( bodyMessagePart );
    }

    if ( attachment != null ) {
      ByteArrayDataSource dataSource = new ByteArrayDataSource( attachment, attachmentMimeType );
      // attach the file to the message
      MimeBodyPart attachmentBodyPart = new MimeBodyPart();
      attachmentBodyPart.setDataHandler( new DataHandler( dataSource ) );
      attachmentBodyPart.setFileName( attachmentName );
      multipart.addBodyPart( attachmentBodyPart );
    }
    return multipart;
  }

  private void setBodyForEmbeddedHtml( MimeMessage msg, String body ) throws MessagingException, IOException {
    MimeMultipart oldMultipart = (MimeMultipart) msg.getContent();
    for ( int i = 0; i < oldMultipart.getCount(); i++ ) {
      MimeBodyPart bodyPart = (MimeBodyPart) oldMultipart.getBodyPart( i );

      final Object content = bodyPart.getContent();
      //Main HTML body
      if ( content instanceof String ) {
        final String newContent = body + "<br/><br/>" + content;
        bodyPart.setText( newContent, StandardCharsets.UTF_8.name(), "html" );
        msg.saveChanges();
        break;
      }
    }
  }
}
