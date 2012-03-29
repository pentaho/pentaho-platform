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
 * Created Mar 03, 2012
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.json.JSONObject;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

@Path("/emailconfig/")
public class EmailResource extends AbstractJaxRSResource {

	private static final Log logger = LogFactory.getLog(EmailResource.class);

	@PUT
	@Path("/setEmailConfig")
	@Consumes({ WILDCARD })
	public Response setEmailConfig(@QueryParam("authenticate") String authenticate, @QueryParam("debug") String debug, @QueryParam("defaultFrom") String defaultFrom, @QueryParam("smtpHost") String smtpHost, @QueryParam("smtpPort") String smtpPort, @QueryParam("smtpProtocol") String smtpProtocol, @QueryParam("userId") String userId, @QueryParam("password") String password, @QueryParam("useSsl") String useSsl, @QueryParam("useStartTls") String useStartTls) {

		try {
			File emailConfigFile = new File(".." + File.separator + ".." + File.separator + "pentaho-solutions" + File.separator + "system" + File.separator + "smtp-email" + File.separator + "email_config.xml");
			EmailConfigXml emailConfigXml = new EmailConfigXml(emailConfigFile);
			emailConfigXml.setDebug(Boolean.parseBoolean(debug));
			emailConfigXml.setDefaultFrom(defaultFrom);
			emailConfigXml.setSmtpHost(smtpHost);
			emailConfigXml.setSmtpPort(Integer.parseInt(smtpPort));
			emailConfigXml.setSmtpProtocol(smtpProtocol);
			emailConfigXml.setUseSsl(Boolean.parseBoolean(useSsl));
			emailConfigXml.setUseStartTls(Boolean.parseBoolean(useStartTls));

			boolean useAuthentication = Boolean.parseBoolean(authenticate);
			emailConfigXml.setAuthenticate(useAuthentication);
			if (useAuthentication) {
				emailConfigXml.setUserId(userId);
				emailConfigXml.setPassword(password);
			}

			saveDom(emailConfigXml.getDocument(), emailConfigFile);
		} catch (Exception e) {
			logger.error("Email Configuration could not be saved."); //$NON-NLS-1$
		}
		return Response.ok().build();
	}

	private void saveDom(Document document, File file) throws IOException {
		file.createNewFile();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		XmlDom4JHelper.saveDom(document, fileOutputStream, "UTF-8");
		try {
			fileOutputStream.close();
		} catch (IOException ex) {
			// Do nothing.
		}
	}

	@GET
	@Path("/getEmailConfig")
	@Produces({ MediaType.APPLICATION_JSON })
	public String getEmailConfig() throws Exception {

		File emailConfigFile = new File(".." + File.separator + ".." + File.separator + "pentaho-solutions" + File.separator + "system" + File.separator + "smtp-email" + File.separator + "email_config.xml");
		EmailConfigXml emailConfigXml = new EmailConfigXml(emailConfigFile);

		JSONObject emailData = new JSONObject();
		emailData.put("authenticate", emailConfigXml.getAuthenticate());
		emailData.put("debug", emailConfigXml.getDebug());
		emailData.put("defaultFrom", emailConfigXml.getDefaultFrom());
		emailData.put("smtpHost", emailConfigXml.getSmtpHost());
		emailData.put("smtpPort", emailConfigXml.getSmtpPort());
		emailData.put("smtpProtocol", emailConfigXml.getSmtpProtocol());
		emailData.put("userId", emailConfigXml.getUserId());
		emailData.put("password", emailConfigXml.getPassword());
		emailData.put("useSsl", emailConfigXml.getUseSsl());
		emailData.put("useStartTls", emailConfigXml.getUseStartTls());

		return emailData.toString();
	}

	@GET
	@Path("/sendEmailTest")
	@Consumes({ WILDCARD })
	@Produces({ MediaType.TEXT_PLAIN })
	public String sendEmailTest(@QueryParam("authenticate") String authenticate, @QueryParam("debug") String debug, @QueryParam("defaultFrom") String defaultFrom, @QueryParam("smtpHost") String smtpHost, @QueryParam("smtpPort") String smtpPort, @QueryParam("smtpProtocol") String smtpProtocol, @QueryParam("userId") String userId, @QueryParam("password") String password, @QueryParam("useSsl") String useSsl, @QueryParam("useStartTls") String useStartTls) throws Exception {
		EmailTester emailTester = new EmailTester();
		return emailTester.performTest(authenticate, debug, defaultFrom, smtpHost, smtpPort, smtpProtocol, userId, password, useSsl, useStartTls);
	}
}
