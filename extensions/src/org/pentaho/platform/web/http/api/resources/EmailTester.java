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
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.dom4j.Document;
import org.dom4j.Node;
import org.pentaho.platform.config.i18n.Messages;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.util.logging.Logger;

@SuppressWarnings("all")
public class EmailTester {

	static final String EMAIL_CONFIG_XML = "smtp-email/email_config.xml";

	private static final String MAILER = "smtpsend";

	public String performTest() {

		SystemSettings systemSettings = new PathBasedSystemSettings();

		final String mailUserIDStr = systemSettings.getSystemSetting(EMAIL_CONFIG_XML, "email-smtp/mail.userid", "");
		final String password = systemSettings.getSystemSetting(EMAIL_CONFIG_XML, "email-smtp/mail.password", "");
		String smtpAuthStr = systemSettings.getSystemSetting(EMAIL_CONFIG_XML, "email-smtp/properties/mail.smtp.auth", "");
		boolean authenticate = "true".equalsIgnoreCase(smtpAuthStr);
		String fromDefaultStr = systemSettings.getSystemSetting(EMAIL_CONFIG_XML, "email-smtp/mail.from.default", "");
		String sendEmailMessage = "";

		Properties props = new Properties();

		try {
			Document configDocument = systemSettings.getSystemSettingsDocument(EMAIL_CONFIG_XML);
			List properties = configDocument.selectNodes("/email-smtp/properties/*");
			Iterator propertyIterator = properties.iterator();
			while (propertyIterator.hasNext()) {
				Node propertyNode = (Node) propertyIterator.next();
				String propertyName = propertyNode.getName();
				String propertyValue = propertyNode.getText();
				props.put(propertyName, propertyValue);
			}
		} catch (Exception e) {

		}

		Session session;
		if (authenticate) {
			Authenticator authenticator = new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					String user = mailUserIDStr;
					String passwd = password;
					return new PasswordAuthentication(user, passwd);
				}
			};
			session = Session.getInstance(props, authenticator);
		} else {
			session = Session.getInstance(props);
		}

		// send a test message
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(fromDefaultStr));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(fromDefaultStr));
			msg.setSubject(Messages.getString("EmailTester.SUBJECT"));
			msg.setText(Messages.getString("EmailTester.MESSAGE"));
			msg.setHeader("X-Mailer", MAILER);
			msg.setSentDate(new Date());
			Transport.send(msg);
			sendEmailMessage = Messages.getString("EmailTester.MESSAGE_SENT_TO", fromDefaultStr);
		} catch (Exception e) {
			Logger.error(EmailTester.class, Messages.getString("EmailTester.NOT_CONFIGURED"), e);
			sendEmailMessage = Messages.getString("EmailTester.ERROR_MESSAGE", e.getMessage() != null ? e.getMessage() : Messages.getString("EmailTester.NOT_CONFIGURED"));
		}

		return sendEmailMessage;
	}
}
