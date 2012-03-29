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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 27, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.web.http.api.resources;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.pentaho.platform.config.i18n.Messages;
import org.pentaho.platform.util.logging.Logger;

@SuppressWarnings("all")
public class EmailTester {

	public String performTest(String authenticate, String debug, String defaultFrom, String smtpHost, String smtpPort, String smtpProtocol, final String userId, final String password, String useSsl, String useStartTls) {

		Properties emailProperties = new Properties();
		emailProperties.put("mail.smtp.host", smtpHost);
		emailProperties.put("mail.smtp.port", smtpPort);
		emailProperties.put("mail.transport.protocol", smtpProtocol);
		emailProperties.put("mail.smtp.starttls.enable", useStartTls);
		emailProperties.put("mail.smtp.auth", authenticate);
		emailProperties.put("mail.smtp.ssl", useSsl);
		emailProperties.put("mail.debug", debug);

		Session session = null;
		if ("true".equalsIgnoreCase(authenticate)) {
			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String user = userId;
					String passwd = password;
					return new PasswordAuthentication(user, passwd);
				}
			};
			session = Session.getInstance(emailProperties, authenticator);
		} else {
			session = Session.getInstance(emailProperties);
		}

		String sendEmailMessage = "";
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(defaultFrom));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(defaultFrom));
			msg.setSubject(Messages.getString("EmailTester.SUBJECT"));
			msg.setText(Messages.getString("EmailTester.MESSAGE"));
			msg.setHeader("X-Mailer", "smtpsend");
			msg.setSentDate(new Date());
			Transport.send(msg);
			sendEmailMessage = "EmailTester.SUCESS";
		} catch (Exception e) {
			Logger.error(EmailTester.class, Messages.getString("EmailTester.NOT_CONFIGURED"), e);
			sendEmailMessage = "EmailTester.FAIL";
		}

		return sendEmailMessage;
	}
}
