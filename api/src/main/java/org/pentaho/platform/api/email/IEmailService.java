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

package org.pentaho.platform.api.email;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

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
