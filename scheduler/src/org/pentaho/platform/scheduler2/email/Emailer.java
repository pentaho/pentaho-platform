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

package org.pentaho.platform.scheduler2.email;

import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class Emailer {

  private static final Log logger = LogFactory.getLog( Emailer.class );
  private static final String MAILER = "smtpsend"; //$NON-NLS-1$

  private Properties props = new Properties();
  private InputStream attachment = null;
  private String attachmentName = null;
  private String attachmentMimeType = null;
  private Authenticator authenticator = null;

  public Emailer() {
  }

  public void setTo( String to ) {
    to = to.replaceAll( ";", "," );
    if ( to != null && !"".equals( to ) ) {
      props.put( "to", to );
    }
  }

  public void setCc( String cc ) {
    cc = cc.replaceAll( ";", "," );
    if ( cc != null && !"".equals( cc ) ) {
      props.put( "cc", cc );
    }
  }

  public void setBcc( String bcc ) {
    bcc = bcc.replaceAll( ";", "," );
    if ( bcc != null && !"".equals( bcc ) ) {
      props.put( "bcc", bcc );
    }
  }

  public void setSubject( String subject ) {
    props.put( "subject", subject );
  }

  public void setFrom( String from ) {
    props.put( "mail.from.default", from );
  }

  public void setFromName( String fromName ) {
    props.put( "mail.from.name", fromName );
  }

  public void setUseAuthentication( boolean useAuthentication ) {
    props.put( "mail.smtp.auth", ObjectUtils.toString( useAuthentication ) );
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

  public boolean setup() {
    try {
      final IEmailService service =
          PentahoSystem.get( IEmailService.class, "IEmailService", PentahoSessionHolder.getSession() );
      props.put( "mail.smtp.host", service.getEmailConfig().getSmtpHost() );
      props.put( "mail.smtp.port", ObjectUtils.toString( service.getEmailConfig().getSmtpPort() ) );
      props.put( "mail.transport.protocol", service.getEmailConfig().getSmtpProtocol() );
      props.put( "mail.smtp.starttls.enable", ObjectUtils.toString( service.getEmailConfig().isUseStartTls() ) );
      props.put( "mail.smtp.auth", ObjectUtils.toString( service.getEmailConfig().isAuthenticate() ) );
      props.put( "mail.smtp.ssl", ObjectUtils.toString( service.getEmailConfig().isUseSsl() ) );
      props.put( "mail.smtp.quitwait", ObjectUtils.toString( service.getEmailConfig().isSmtpQuitWait() ) );
      props.put( "mail.from.default", service.getEmailConfig().getDefaultFrom() );
      String fromName = service.getEmailConfig().getFromName();
      if ( StringUtils.isEmpty( fromName ) ) {
        fromName = Messages.getInstance().getString( "schedulerEmailFromName" );
      }
      props.put( "mail.from.name", fromName );
      props.put( "mail.debug", ObjectUtils.toString( service.getEmailConfig().isDebug() ) );

      if ( service.getEmailConfig().isAuthenticate() ) {
        props.put( "mail.userid", service.getEmailConfig().getUserId() );
        props.put( "mail.password", service.getEmailConfig().getPassword() );
        setAuthenticator( new Authenticator() {
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication( service.getEmailConfig().getUserId(), service.getEmailConfig()
                .getPassword() );
          }
        } );
      }

      return true;
    } catch ( Exception e ) {
      logger.error( "Email.ERROR_0013_CONFIG_FILE_INVALID", e ); //$NON-NLS-1$
    }
    return false;
  }

  public boolean send() {
    String from = props.getProperty( "mail.from.default" );
    String fromName = props.getProperty( "mail.from.name" );
    String to = props.getProperty( "to" );
    String cc = props.getProperty( "cc" );
    String bcc = props.getProperty( "bcc" );
    boolean authenticate = "true".equalsIgnoreCase( props.getProperty( "mail.smtp.auth" ) );
    String subject = props.getProperty( "subject" );
    String body = props.getProperty( "body" );

    logger.info( "Going to send an email to " + to + " from " + from + " with the subject '" + subject
        + "' and the body " + body );

    try {
      // Get a Session object
      Session session;

      if ( authenticate ) {
        session = Session.getInstance( props, authenticator );
      } else {
        session = Session.getInstance( props );
      }

      // if debugging is not set in the email config file, then default to false
      if ( !props.containsKey( "mail.debug" ) ) { //$NON-NLS-1$
        session.setDebug( false );
      }

      // construct the message
      MimeMessage msg = new MimeMessage( session );
      Multipart multipart = new MimeMultipart();

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

      if ( attachment == null ) {
        logger.error( "Email.ERROR_0015_ATTACHMENT_FAILED" ); //$NON-NLS-1$
        return false;
      }

      ByteArrayDataSource dataSource = new ByteArrayDataSource( attachment, attachmentMimeType );

      if ( body != null ) {
        MimeBodyPart bodyMessagePart = new MimeBodyPart();
        bodyMessagePart.setText( body, LocaleHelper.getSystemEncoding() );
        multipart.addBodyPart( bodyMessagePart );
      }

      // attach the file to the message
      MimeBodyPart attachmentBodyPart = new MimeBodyPart();
      attachmentBodyPart.setDataHandler( new DataHandler( dataSource ) );
      attachmentBodyPart.setFileName( MimeUtility.encodeText( attachmentName, "UTF-8", null ) );
      multipart.addBodyPart( attachmentBodyPart );

      // add the Multipart to the message
      msg.setContent( multipart );

      msg.setHeader( "X-Mailer", Emailer.MAILER ); //$NON-NLS-1$
      msg.setSentDate( new Date() );

      Transport.send( msg );

      return true;
    } catch ( SendFailedException e ) {
      logger.error( "Email.ERROR_0011_SEND_FAILED -" + to, e ); //$NON-NLS-1$
    } catch ( AuthenticationFailedException e ) {
      logger.error( "Email.ERROR_0014_AUTHENTICATION_FAILED - " + to, e ); //$NON-NLS-1$
    } catch ( Throwable e ) {
      logger.error( "Email.ERROR_0011_SEND_FAILED - " + to, e ); //$NON-NLS-1$
    }
    return false;
  }
}
