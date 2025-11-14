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


package org.pentaho.platform.api.email;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/**
 * The Email Service used in the Pentaho Platform
 */
public interface IEmailService {
  /**
   * Saves the email configuration
   *
   * @param emailConfiguration
   *          the email configuration to save as the current email configuration
   */
  public void setEmailConfig( final IEmailConfiguration emailConfiguration );

  /**
   * Retrieves the current email configuration
   */
  public IEmailConfiguration getEmailConfig();

  /**
   * Generates a test email via the specficied email configuration
   *
   * @param emailConfig
   *          the email configuration to use for sending the testing email
   */
  public String sendEmailTest( final IEmailConfiguration emailConfig );

  public boolean isValid();


  /**
   * Sends a mail using SMTP or Graph API depending on Params
   *
   * @param session
   *          the java mail session required for SMTP connections
   * @param msg
   *          the message object in form of MimeMessage. Sent as object for SMTP and as Base64 string in Graph API calls
   *
   */
  public void sendEmail( Session session, MimeMessage msg ) throws EmailServiceException;

}
