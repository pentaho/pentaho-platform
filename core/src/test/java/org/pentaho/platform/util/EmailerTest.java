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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.util;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.email.IEmailService;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class EmailerTest {

  private Emailer emailer;

  private String eml = "Tue, 21 Jun 2016 10:04:51 +0300 (MSK)\n"
          + "Message-ID: <822898890.3.1466492684668.JavaMail.test@test>\n"
          + "MIME-Version: 1.0\n"
          + "Content-Type: multipart/related; \n"
          + "\tboundary=\"----=_Part_2_299015846.1466492684508\"\n"
          + "From: \"test@test.com\" <test@test.com>\n"
          + "To: test@test.com\n"
          + "Subject: Test.\n"
          + "X-Mailer: smtpsend\n"
          + "Date: Tue, 21 Jun 2016 10:04:44 +0300 (MSK)\n"
          + "\n"
          + "------=_Part_2_299015846.1466492684508\n"
          + "Content-Type: text/html; charset=UTF-8\n"
          + "Content-Transfer-Encoding: 7bit\n"
          + "\n"
          + "<!DOCTYPE html\n"
          + "     PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
          + "     \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
          + "<html><h1>Shiny test string<h1><html/>\n"
          + "------=_Part_2_299015846.1466492684508--";

  @Before
  public void setUp() {
    emailer = new Emailer();

    MockMail.clear();
  }

  @Test
  public void setTo_NullTest() {
    emailer.setTo( null );
    assertNull( emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setTo_ValidTest() {
    emailer.setTo( "to@domain.com" );
    assertEquals( "to@domain.com", emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setTo_ReplaceToCommaTest() {
    emailer.setTo( "to@domain.com; another_to@domain.com" );
    assertEquals( "to@domain.com, another_to@domain.com", emailer.getProperties().getProperty( "to" ) );
  }

  @Test
  public void setCc_NullTest() {
    emailer.setCc( null );
    assertNull( emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setCc_Test() {
    emailer.setCc( "cc@domain.com" );
    assertEquals( "cc@domain.com", emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setCc_ReplaceToCommaTest() {
    emailer.setCc( "cc@domain.com; another_cc@domain.com" );
    assertEquals( "cc@domain.com, another_cc@domain.com", emailer.getProperties().getProperty( "cc" ) );
  }

  @Test
  public void setBcc_NullTest() {
    emailer.setBcc( null );
    assertNull( emailer.getProperties().getProperty( "bcc" ) );
  }

  @Test
  public void setBcc_ValidTest() {
    emailer.setBcc( "bcc@domain.com" );
    assertEquals( "bcc@domain.com", emailer.getProperties().getProperty( "bcc" ) );
  }

  @Test
  public void setBcc_ReplaceToCommaTest() {
    emailer.setBcc( "bcc@domain.com; another_bcc@domain.com" );
    assertEquals( "bcc@domain.com, another_bcc@domain.com", emailer.getProperties().getProperty( "bcc" ) );
  }

  @Test
  public void testEmbeddedHtmlNoBody() throws Exception {
    IEmailService emailService = Mockito.mock( IEmailService.class );
    emailer = new Emailer( emailService );
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    emailer.setAttachmentMimeType( "mime-message/text/html" );
    try ( ByteArrayInputStream stream = new ByteArrayInputStream( eml.getBytes() ) ) {
      emailer.setAttachment( stream );
    }
    emailer.send();
    Mockito.verify( emailService, Mockito.times( 1 ) ).sendEmail( Mockito.any( Session.class ),
            Mockito.any( MimeMessage.class ) );
  }


  @Test
  public void testEmbeddedHtmlBody() throws Exception {
    IEmailService emailService = Mockito.mock( IEmailService.class );
    emailer = new Emailer( emailService );
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    final String body = UUID.randomUUID().toString();
    emailer.setBody( body );
    emailer.setAttachmentMimeType( "mime-message/text/html" );
    try ( ByteArrayInputStream stream = new ByteArrayInputStream( eml.getBytes() ) ) {
      emailer.setAttachment( stream );
    }
    emailer.send();
    Mockito.verify( emailService, Mockito.times( 1 ) ).sendEmail( Mockito.any( Session.class ),
            Mockito.any( MimeMessage.class ) );

  }

  @Test
  public void testEmbeddedHtmlBodyWithoutAttachment() throws Exception {
    IEmailService emailService = Mockito.mock( IEmailService.class );
    emailer = new Emailer( emailService );
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    final String body = UUID.randomUUID().toString();
    emailer.setBody( body );
    emailer.send();
    Mockito.verify( emailService, Mockito.times( 1 ) ).sendEmail( Mockito.any( Session.class ),
            Mockito.any( MimeMessage.class ) );
  }

  @Test
  public void testEmbeddedHtmlBodyWithAttachment() throws Exception {
    IEmailService emailService = Mockito.mock( IEmailService.class );
    emailer = new Emailer( emailService );
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    final String body = UUID.randomUUID().toString();
    emailer.setBody( body );
    emailer.setAttachmentMimeType( "text/html" );
    try ( ByteArrayInputStream stream = new ByteArrayInputStream( eml.getBytes() ) ) {
      emailer.setAttachment( stream );
      emailer.setAttachmentName( "email-file" );
    }
    emailer.send();
    Mockito.verify( emailService, Mockito.times( 1 ) ).sendEmail( Mockito.any( Session.class ),
            Mockito.any( MimeMessage.class ) );
  }

  @Test
  public void testGetSmtpSessionOAuth2() {
    emailer.setSmtpHost( "smtp.outlook.com" );
    emailer.setSmtpPort( 587 );
    emailer.setTransportProtocol( "SMTP" );
    emailer.setStartTLS( true );
    emailer.setUseSSL( true );
    emailer.setQuitWait( false );
    emailer.setFromName( "test@pentaho.com" );
    emailer.setUseAuthentication( true );
    emailer.setAuthMechanism( "XOAUTH2" );
    assertEquals( "587", emailer.getSmtpSession( true ).getProperty( "mail.smtp.port" ) );
  }

  @Test
  public void testSend() {
    IEmailService emailService = Mockito.mock( IEmailService.class );
    emailer = new Emailer( emailService );
    emailer.setSmtpHost( "smtp.outlook.com" );
    emailer.setSmtpPort( 587 );
    emailer.setTransportProtocol( "SMTP" );
    emailer.setStartTLS( true );
    emailer.setUseSSL( true );
    emailer.setQuitWait( false );
    emailer.setFromName( "test@pentaho.com" );
    emailer.setUseAuthentication( true );
    emailer.setAuthMechanism( "basic" );
    emailer.getSmtpSession( true );
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    final String body = UUID.randomUUID().toString();
    emailer.setBody( body );
    EncryptedPasswordAuthenticator encryptedPasswordAuthenticator = Mockito.mock( EncryptedPasswordAuthenticator.class );
    emailer.setAuthenticator( encryptedPasswordAuthenticator );
    assertEquals( true, emailer.send() );
  }

  @Test
  public void testSetup_IEmailService() {
    // SETUP
    IEmailService mockEmailService = Mockito.mock( IEmailService.class );
    IEmailConfiguration mockEmailConfiguration = Mockito.mock( IEmailConfiguration.class );
    Mockito.when( mockEmailService.getEmailConfig() ).thenReturn( mockEmailConfiguration );
    Mockito.when( mockEmailConfiguration.getSmtpHost() ).thenReturn( "smtp.com" );
    Mockito.when( mockEmailConfiguration.getSmtpPort() ).thenReturn( 25 );
    Mockito.when( mockEmailConfiguration.getSmtpProtocol() ).thenReturn( "smtp" );
    Mockito.when( mockEmailConfiguration.isUseStartTls() ).thenReturn( true );
    Mockito.when( mockEmailConfiguration.isUseSsl() ).thenReturn( true );
    Mockito.when( mockEmailConfiguration.isSmtpQuitWait() ).thenReturn( true );
    Mockito.when( mockEmailConfiguration.getDefaultFrom() ).thenReturn( "test@pentaho.com" );
    Mockito.when( mockEmailConfiguration.getAuthMechanism() ).thenReturn( "basic" );
    Mockito.when( mockEmailConfiguration.getFromName() ).thenReturn( "" );
    Mockito.when( mockEmailConfiguration.isDebug() ).thenReturn( true );
    Mockito.when( mockEmailConfiguration.isAuthenticate() ).thenReturn( true );
    Mockito.when( mockEmailConfiguration.getUserId() ).thenReturn( "test@pentaho.com" );
    Mockito.when( mockEmailConfiguration.getPassword() ).thenReturn( "password" );

    Emailer testInstance = new Emailer( mockEmailService );
    assertTrue( testInstance.setup() );
  }

  @Test
  public void testSetup_IEmailService_Fail() {
    // SETUP
    IEmailService mockEmailService = Mockito.mock( IEmailService.class );
    Emailer testInstance = new Emailer( mockEmailService );
    assertFalse( testInstance.setup() ); // will throw exception
  }

}
