/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved. 
 *
 *
 * @created Apr 28, 2005
 * @author James Dixon
 */

package org.pentaho.platform.plugin.action.builtin;

import java.io.OutputStream;
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
import org.pentaho.actionsequence.dom.ActionInputConstant;
import org.pentaho.actionsequence.dom.IActionInput;
import org.pentaho.actionsequence.dom.actions.EmailAction;
import org.pentaho.actionsequence.dom.actions.EmailAttachment;
import org.pentaho.commons.connection.ActivationHelper;
import org.pentaho.commons.connection.IPentahoStreamSource;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.api.util.PasswordServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.solution.ComponentBase;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.messages.LocaleHelper;

/**
 * @author James Dixon
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class EmailComponent extends ComponentBase {

  /**
   * 
   */
  private static final long serialVersionUID = 1584906077946023715L;

  private String defaultFrom;

  private static final String MAILER = "smtpsend"; //$NON-NLS-1$

  /*
   private String protocol = null;
   private String host = null;
   private String recordDir = null;
   */

  @Override
  public Log getLogger() {
    return LogFactory.getLog(EmailComponent.class);
  }

  @Override
  protected boolean validateSystemSettings() {
    // get the settings from the system configuration file
    String mailhost = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.smtp.host", null); //$NON-NLS-1$ //$NON-NLS-2$
    boolean authenticate = "true".equalsIgnoreCase(PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.smtp.auth", "false")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    // don't store these in a class member for secutiry
    String user = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.userid", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    String password = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.password", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    defaultFrom = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.from.default", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // Check the email server settings...
    if (mailhost.equals("") || (user.equals("") && authenticate) || defaultFrom.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      // looks like the email stuff is not configured yet...
      // see if we can provide feedback to the user...

      boolean allowParameterUI = feedbackAllowed();

      if (allowParameterUI) {
        OutputStream feedbackStream = getFeedbackOutputStream();
        StringBuffer messageBuffer = new StringBuffer();
        PentahoSystem
            .get(IMessageFormatter.class, getSession())
            .formatErrorMessage(
                "text/html", Messages.getInstance().getString("Email.USER_COULD_NOT_SEND_EMAIL"), Messages.getInstance().getString("Email.USER_SETTINGS_HELP"), messageBuffer); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        try {
          feedbackStream.write(messageBuffer.toString().getBytes(LocaleHelper.getSystemEncoding()));
        } catch (Exception e) {
          error(Messages.getInstance().getErrorString("Base.ERROR_0003_INVALID_FEEDBACK_STREAM"), e); //$NON-NLS-1$
          return false;
        }
        return false;
      } else {
        // we are not allowed to provide feedback and cannot continue...
        error(Messages.getInstance().getErrorString("Email.ERROR_0009_SERVER_SETTINGS_NOT_SET")); //$NON-NLS-1$
        return false;
      }
    }
    boolean ok = (mailhost != null);
    if (authenticate) {
      ok &= (user != null) && (password != null);
    }
    ok &= defaultFrom != null;
    return ok;
  }

  @Override
  public boolean init() {
    return true;
  }

  @Override
  public boolean validateAction() {
    boolean result = true;
    // make sure that we can get a "to" email address
    if (!(getActionDefinition() instanceof EmailAction)) {
      error(Messages.getInstance().getErrorString(
          "ComponentBase.ERROR_0001_UNKNOWN_ACTION_TYPE", getActionDefinition().getElement().asXML())); //$NON-NLS-1$
      result = false;
    } else {
      EmailAction emailAction = (EmailAction) getActionDefinition();

      IActionInput to = emailAction.getTo();
      IActionInput subject = emailAction.getSubject();
      IActionInput plainMsg = emailAction.getMessagePlain();
      IActionInput htmlMsg = emailAction.getMessageHtml();

      if (to == ActionInputConstant.NULL_INPUT) {
        error(Messages.getInstance().getErrorString("Email.ERROR_0001_TO_NOT_DEFINED", getActionName())); //$NON-NLS-1$
        result = false;
      } else if (subject == ActionInputConstant.NULL_INPUT) {
        error(Messages.getInstance().getErrorString("Email.ERROR_0002_SUBJECT_NOT_DEFINED", getActionName())); //$NON-NLS-1$
        result = false;
      } else if ((plainMsg == ActionInputConstant.NULL_INPUT) && (htmlMsg == ActionInputConstant.NULL_INPUT)) {
        error(Messages.getInstance().getErrorString("Email.ERROR_0003_BODY_NOT_DEFINED", getActionName())); //$NON-NLS-1$
        result = false;
      }
    }

    return result;

  }

  @Override
  public boolean executeAction() {
    EmailAction emailAction = (EmailAction) getActionDefinition();

    String messagePlain = emailAction.getMessagePlain().getStringValue();
    String messageHtml = emailAction.getMessageHtml().getStringValue();
    String subject = emailAction.getSubject().getStringValue();
    String to = emailAction.getTo().getStringValue();
    String cc = emailAction.getCc().getStringValue();
    String bcc = emailAction.getBcc().getStringValue();
    String from = emailAction.getFrom().getStringValue(defaultFrom);
    if (from.trim().length() == 0) {
      from = defaultFrom;
    }

    /*
     * if( context.getInputNames().contains( "attach" ) ) { //$NON-NLS-1$
     * Object attachParameter = context.getInputParameter( "attach"
     * ).getValue(); //$NON-NLS-1$ // We have a list of attachments, each
     * element of the list is the name of the parameter containing the
     * attachment // Use the parameter filename portion as the attachment
     * name. if ( attachParameter instanceof String ) { String attachName =
     * context.getInputParameter( "attach-name" ).getStringValue();
     * //$NON-NLS-1$ AttachStruct attachData = getAttachData( context,
     * (String)attachParameter, attachName ); if ( attachData != null ) {
     * attachments.add( attachData ); } } else if ( attachParameter
     * instanceof List ) { for ( int i = 0; i <
     * ((List)attachParameter).size(); ++i ) { AttachStruct attachData =
     * getAttachData( context, ((List)attachParameter).get( i ).toString(),
     * null ); if ( attachData != null ) { attachments.add( attachData ); } } }
     * else if ( attachParameter instanceof Map ) { for ( Iterator it =
     * ((Map)attachParameter).entrySet().iterator(); it.hasNext(); ) {
     * Map.Entry entry = (Map.Entry)it.next(); AttachStruct attachData =
     * getAttachData( context, (String)entry.getValue(),
     * (String)entry.getKey() ); if ( attachData != null ) {
     * attachments.add( attachData ); } } } }
     * 
     * int maxSize = Integer.MAX_VALUE; try { maxSize = new Integer(
     * props.getProperty( "mail.max.attach.size" ) ).intValue(); } catch(
     * Throwable t ) { //ignore if not set to a valid value }
     * 
     * if ( totalAttachLength > maxSize ) { // Sort them in order TreeMap tm =
     * new TreeMap(); for( int idx=0; idx<attachments.size(); idx++ ) { //
     * tm.put( new Integer( )) } }
     */

    if (ComponentBase.debug) {
      debug(Messages.getInstance().getString("Email.DEBUG_TO_FROM", to, from)); //$NON-NLS-1$
      debug(Messages.getInstance().getString("Email.DEBUG_CC_BCC", cc, bcc)); //$NON-NLS-1$
      debug(Messages.getInstance().getString("Email.DEBUG_SUBJECT", subject)); //$NON-NLS-1$
      debug(Messages.getInstance().getString("Email.DEBUG_PLAIN_MESSAGE", messagePlain)); //$NON-NLS-1$
      debug(Messages.getInstance().getString("Email.DEBUG_HTML_MESSAGE", messageHtml)); //$NON-NLS-1$
    }

    if ((to == null) || (to.trim().length() == 0)) {

      // Get the output stream that the feedback is going into
      OutputStream feedbackStream = getFeedbackOutputStream();
      if (feedbackStream != null) {
        createFeedbackParameter("to", Messages.getInstance().getString("Email.USER_ENTER_EMAIL_ADDRESS"), "", "", true); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        setFeedbackMimeType("text/html"); //$NON-NLS-1$
        return true;
      } else {
        return false;
      }
    }
    if (subject == null) {
      error(Messages.getInstance().getErrorString("Email.ERROR_0005_NULL_SUBJECT", getActionName())); //$NON-NLS-1$
      return false;
    }
    if ((messagePlain == null) && (messageHtml == null)) {
      error(Messages.getInstance().getErrorString("Email.ERROR_0006_NULL_BODY", getActionName())); //$NON-NLS-1$
      return false;
    }

    if (getRuntimeContext().isPromptPending()) {
      return true;
    }

    try {

      Properties props = new Properties();

      try {
        Document configDocument = PentahoSystem.getSystemSettings().getSystemSettingsDocument(
            "smtp-email/email_config.xml"); //$NON-NLS-1$
        List properties = configDocument.selectNodes("/email-smtp/properties/*"); //$NON-NLS-1$
        Iterator propertyIterator = properties.iterator();
        while (propertyIterator.hasNext()) {
          Node propertyNode = (Node) propertyIterator.next();
          String propertyName = propertyNode.getName();
          String propertyValue = propertyNode.getText();
          props.put(propertyName, propertyValue);
        }
      } catch (Exception e) {
        error(Messages.getInstance().getString("Email.ERROR_0013_CONFIG_FILE_INVALID"), e); //$NON-NLS-1$
        return false;
      }

      boolean authenticate = "true".equalsIgnoreCase(props.getProperty("mail.smtp.auth")); //$NON-NLS-1$//$NON-NLS-2$

      // Get a Session object

      Session session;
      if (authenticate) {
        Authenticator authenticator = new EmailAuthenticator();
        session = Session.getInstance(props, authenticator);
      } else {
        session = Session.getInstance(props);
      }

      // if debugging is not set in the email config file, match the
      // component debug setting
      if (ComponentBase.debug && !props.containsKey("mail.debug")) { //$NON-NLS-1$
        session.setDebug(true);
      }

      // construct the message
      MimeMessage msg = new MimeMessage(session);
      if (from != null) {
        msg.setFrom(new InternetAddress(from));
      } else {
        // There should be no way to get here
        error(Messages.getInstance().getString("Email.ERROR_0012_FROM_NOT_DEFINED")); //$NON-NLS-1$
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

      EmailAttachment[] emailAttachments = emailAction.getAttachments();
      if ((messagePlain != null) && (messageHtml == null) && (emailAttachments.length == 0)) {
        msg.setText(messagePlain, LocaleHelper.getSystemEncoding());
      } else if (emailAttachments.length == 0) {
        if (messagePlain != null) {
          msg.setContent(messagePlain, "text/plain; charset=" + LocaleHelper.getSystemEncoding()); //$NON-NLS-1$          
        }
        if (messageHtml != null) {
          msg.setContent(messageHtml, "text/html; charset=" + LocaleHelper.getSystemEncoding()); //$NON-NLS-1$
        }
      } else {
        // need to create a multi-part message...
        // create the Multipart and add its parts to it
        Multipart multipart = new MimeMultipart();
        // create and fill the first message part
        if (messageHtml != null) {
          // create and fill the first message part
          MimeBodyPart htmlBodyPart = new MimeBodyPart();
          htmlBodyPart.setContent(messageHtml, "text/html; charset="+LocaleHelper.getSystemEncoding()); //$NON-NLS-1$
          multipart.addBodyPart(htmlBodyPart);
        }

        if (messagePlain != null) {
          MimeBodyPart textBodyPart = new MimeBodyPart();
          textBodyPart.setContent(messagePlain, "text/plain; charset="+LocaleHelper.getSystemEncoding()); //$NON-NLS-1$
          multipart.addBodyPart(textBodyPart);
        }

        for (EmailAttachment element : emailAttachments) {
          IPentahoStreamSource source = element.getContent();
          if (source == null) {
            error(Messages.getInstance().getErrorString("Email.ERROR_0015_ATTACHMENT_FAILED")); //$NON-NLS-1$
            return false;
          }
          DataSource dataSource = new ActivationHelper.PentahoStreamSourceWrapper(source);
          String attachmentName = element.getName();
          if (ComponentBase.debug) {
            debug(Messages.getInstance().getString("Email.DEBUG_ADDING_ATTACHMENT", attachmentName)); //$NON-NLS-1$
          }

          // create the second message part
          MimeBodyPart attachmentBodyPart = new MimeBodyPart();

          // attach the file to the message
          attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
          attachmentBodyPart.setFileName(attachmentName);
          if (ComponentBase.debug) {
            debug(Messages.getInstance().getString("Email.DEBUG_ATTACHMENT_SOURCE", dataSource.getName())); //$NON-NLS-1$
          }
          multipart.addBodyPart(attachmentBodyPart);
        }

        // add the Multipart to the message
        msg.setContent(multipart);
      }

      msg.setHeader("X-Mailer", EmailComponent.MAILER); //$NON-NLS-1$
      msg.setSentDate(new Date());

      Transport.send(msg);

      if (ComponentBase.debug) {
        debug(Messages.getInstance().getString("Email.DEBUG_EMAIL_SUCCESS")); //$NON-NLS-1$
      }
      return true;
      // TODO: persist the content set for a while...
    } catch (SendFailedException e) {
      error(Messages.getInstance().getErrorString("Email.ERROR_0011_SEND_FAILED", to), e); //$NON-NLS-1$
      /*
       Exception ne;
       MessagingException sfe = e;
       while ((ne = sfe.getNextException()) != null && ne instanceof MessagingException) {
       sfe = (MessagingException) ne;
       error(Messages.getInstance().getErrorString("Email.ERROR_0011_SEND_FAILED", sfe.toString()), sfe); //$NON-NLS-1$
       }
       */

    } catch (AuthenticationFailedException e) {
      error(Messages.getInstance().getString("Email.ERROR_0014_AUTHENTICATION_FAILED", to), e); //$NON-NLS-1$
    } catch (Throwable e) {
      error(Messages.getInstance().getErrorString("Email.ERROR_0011_SEND_FAILED", to), e); //$NON-NLS-1$
    }
    return false;
  }

  @Override
  public void done() {

  }

  public String decryptPassword(String encpass) {
    if (encpass.startsWith("ENC:")) {
      IPasswordService passwordService = PentahoSystem.get(IPasswordService.class, null);
      String tmp = encpass.substring(4);
      try {
        return passwordService.decrypt(tmp);
      } catch (PasswordServiceException ex) {
        getLogger().error("Exception decrypting mail password", ex);
        throw new RuntimeException(ex);
      }
    } else {
      return encpass;
    }
  }
  
  
  private class EmailAuthenticator extends Authenticator {

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
      String user = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.userid", null); //$NON-NLS-1$ //$NON-NLS-2$
      String password = PentahoSystem.getSystemSetting("smtp-email/email_config.xml", "mail.password", null); //$NON-NLS-1$ //$NON-NLS-2$
      if (password != null) {
        password = decryptPassword(password);
      }
      return new PasswordAuthentication(user, password);
    }
  }
}
