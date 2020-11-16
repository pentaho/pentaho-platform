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
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.scheduler2.email;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.*;


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
  public void testSubjectInjection() {
    emailer.setSubject( "Test\r\nCC: test@test.com" );
    Assert.assertEquals( "TestCC: test@test.com", emailer.getSubject() );
  }

  @Test
  public void testEmbeddedHtmlNoBody() throws IOException, MessagingException {
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    emailer.setAttachmentMimeType( "mime-message/text/html" );
    try ( ByteArrayInputStream stream = new ByteArrayInputStream( eml.getBytes() ) ) {
      emailer.setAttachment( stream );
    }
    emailer.send();

    assertEquals( 1, MockMail.size() );
    final Message message = MockMail.get( 0 );
    assertNotNull( message );
    final MimeMultipart content = (MimeMultipart) message.getContent();
    assertNotNull( content );
    assertEquals( 1, content.getCount() );
    final BodyPart bodyPart = content.getBodyPart( 0 );
    assertNotNull( bodyPart );
    assertEquals( bodyPart.getContentType(), "text/html; charset=UTF-8" );
    assertTrue( ( (String) bodyPart.getContent() ).contains( "Shiny test string" ) );
  }


  @Test
  public void testEmbeddedHtmlBody() throws IOException, MessagingException {
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

    assertEquals( 1, MockMail.size() );
    final Message message = MockMail.get( 0 );
    assertNotNull( message );
    final MimeMultipart content = (MimeMultipart) message.getContent();
    assertNotNull( content );
    assertEquals( 1, content.getCount() );
    final BodyPart bodyPart = content.getBodyPart( 0 );
    assertNotNull( bodyPart );
    assertEquals( bodyPart.getContentType(), "text/html; charset=UTF-8" );
    final String bodyPartContent = (String) bodyPart.getContent();
    assertTrue( bodyPartContent.contains( "Shiny test string" ) );
    assertTrue( bodyPartContent.contains( body ) );
  }

  @Test
  public void testEmbeddedHtmlBodyWithoutAttachment() throws IOException, MessagingException {
    emailer.setTo( "tets@test.email.com" );
    emailer.setFrom( "tets@test.email.com" );
    emailer.setSubject( "Test" );
    final String body = UUID.randomUUID().toString();
    emailer.setBody( body );
    emailer.send();

    assertEquals( 1, MockMail.size() );
    final Message message = MockMail.get( 0 );
    assertNotNull( message );
    final MimeMultipart content = (MimeMultipart) message.getContent();
    assertNotNull( content );
    assertEquals( 1, content.getCount() );
    final BodyPart bodyPart = content.getBodyPart( 0 );
    assertNotNull( bodyPart );
    assertEquals( bodyPart.getContentType(), "text/plain; charset=UTF-8" );
    final String bodyPartContent = (String) bodyPart.getContent();
    assertEquals( body, bodyPartContent );
  }

}
