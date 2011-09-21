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
 * Copyright 2005-2009 Pentaho Corporation.  All rights reserved. 
 * 
 * @created Oct 27, 2009
 * @author Dan Kinsley
 */

package org.pentaho.platform.repository.subscription;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.commons.connection.ActivationHelper;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

public class SubscriptionEmailContent {

  private static final Log logger = LogFactory.getLog(SubscriptionEmailContent.class);
  private static final String MAILER = "smtpsend"; //$NON-NLS-1$
  private Properties props = new Properties();
  private IPentahoStreamSource attachment = null;
  private String attachmentName = null;

  SubscriptionEmailContent(IPentahoStreamSource inAttachment, String inAttachmentName, String inSubject, String destination) {
    setup();
    props.put("to", destination);
    props.put("subject", inSubject);
    props.put("body", Messages.getInstance().getString("SubscriptionExecute.EMAIL_BODY_MESSAGE")); //$NON-NLS-1$
    attachment = inAttachment;
    attachmentName = inAttachmentName;
  }

  public void setup() {
    try {
      Document configDocument = PentahoSystem.getSystemSettings().getSystemSettingsDocument("smtp-email/email_config.xml"); //$NON-NLS-1$
      List properties = configDocument.selectNodes("/email-smtp/properties/*"); //$NON-NLS-1$
      Iterator propertyIterator = properties.iterator();
      while (propertyIterator.hasNext()) {
        Node propertyNode = (Node) propertyIterator.next();
        String propertyName = propertyNode.getName();
        String propertyValue = propertyNode.getText();
        props.put(propertyName, propertyValue);
      }
      props.put("mail.from.default", PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.from.default", ""));
    } catch (Exception e) {
      logger.error("Email.ERROR_0013_CONFIG_FILE_INVALID", e); //$NON-NLS-1$
    }
  }

  public boolean send() {
    String cc = null;
    String bcc = null;
    String from = props.getProperty("mail.from.default");
    String to = props.getProperty("to");
    boolean authenticate = "true".equalsIgnoreCase(props.getProperty("mail.smtp.auth"));
    String subject = props.getProperty("subject");
    String body = props.getProperty("body");

    logger.info("Going to send an email to " + to + " from " + from + "with the subject '" + subject + "' and the body " + body);

    try {
      // Get a Session object
      Session session;

      if (authenticate) {
        Authenticator authenticator = new EmailAuthenticator();
        session = Session.getInstance(props, authenticator);
      } else {
        session = Session.getInstance(props);
      }

      // if debugging is not set in the email config file, then default to false
      if (!props.containsKey("mail.debug")) { //$NON-NLS-1$
        session.setDebug(false);
      }

      // construct the message
      MimeMessage msg = new MimeMessage(session);
      Multipart multipart = new MimeMultipart();

      if (from != null) {
        msg.setFrom(new InternetAddress(from));
      } else {
        // There should be no way to get here
        logger.error("Email.ERROR_0012_FROM_NOT_DEFINED"); //$NON-NLS-1$
      }

      if ((to != null) && (to.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
      }
      if ((cc != null) && (cc.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc, false));
      }
      if ((bcc != null) && (bcc.trim().length() > 0)) {
        msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc, false));
      }

      if (subject != null) {
        msg.setSubject(subject, LocaleHelper.getSystemEncoding());
      }

      if (body != null) {
        MimeBodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setContent(body, "text/plain; charset=" + LocaleHelper.getSystemEncoding()); //$NON-NLS-1$
        multipart.addBodyPart(textBodyPart);
      }

      // need to create a multi-part message...
      // create the Multipart and add its parts to it

      // create and fill the first message part
      IPentahoStreamSource source = attachment;
      if (source == null) {
        logger.error("Email.ERROR_0015_ATTACHMENT_FAILED"); //$NON-NLS-1$
        return false;
      }
      DataSource dataSource = new ActivationHelper.PentahoStreamSourceWrapper(source);

      // create the second message part
      MimeBodyPart attachmentBodyPart = new MimeBodyPart();

      // attach the file to the message
      attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
      attachmentBodyPart.setFileName(attachmentName);

      multipart.addBodyPart(attachmentBodyPart);

      // add the Multipart to the message
      msg.setContent(multipart);

      msg.setHeader("X-Mailer", SubscriptionEmailContent.MAILER); //$NON-NLS-1$
      msg.setSentDate(new Date());

      Transport.send(msg);

      return true;
      // TODO: persist the content set for a while...
    } catch (SendFailedException e) {
      logger.error("Email.ERROR_0011_SEND_FAILED -" + to, e); //$NON-NLS-1$

    } catch (AuthenticationFailedException e) {
      logger.error("Email.ERROR_0014_AUTHENTICATION_FAILED - " + to, e); //$NON-NLS-1$
    } catch (Throwable e) {
      logger.error("Email.ERROR_0011_SEND_FAILED - " + to, e); //$NON-NLS-1$
    }
    return false;
  }

  private class EmailAuthenticator extends Authenticator {
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      String user = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.userid", null); //$NON-NLS-1$ //$NON-NLS-2$
      String password = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.password", null); //$NON-NLS-1$ //$NON-NLS-2$
      return new PasswordAuthentication(user, password);
    }
  }
}
