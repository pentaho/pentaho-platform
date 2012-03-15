package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.pentaho.platform.config.ConsoleConfig;
import org.pentaho.platform.config.i18n.Messages;
import org.pentaho.platform.util.xml.dom4j.XmlDom4JHelper;

@Path("/emailconfig/")
public class EmailResource extends AbstractJaxRSResource {

	private static final Log logger = LogFactory.getLog(EmailResource.class);

	@PUT
	@Path("/setEmailConfig")
	@Consumes({ APPLICATION_XML, APPLICATION_JSON })
	public Response setEmailConfig(@QueryParam("authenticate") String authenticate, @QueryParam("debug") String debug, @QueryParam("defaultFrom") String defaultFrom, @QueryParam("smtpHost") String smtpHost, @QueryParam("smtpPort") String smtpPort, @QueryParam("smtpProtocol") String smtpProtocol, @QueryParam("userId") String userId, @QueryParam("password") String password, @QueryParam("useSsl") String useSsl, @QueryParam("useStartTls") String useStartTls) {

		try {
			File emailConfigFile = ConsoleConfig.getInstance().getEmailConfigFile();
			EmailConfigXml emailConfigXml = new EmailConfigXml(emailConfigFile);
		      /*emailConfigXml.setAuthenticate(emailServerConfig.getAuthenticate());
		      emailConfigXml.setDebug(emailServerConfig.getDebug());
		      emailConfigXml.setDefaultFrom(emailServerConfig.getDefaultFrom());
		      emailConfigXml.setSmtpHost(emailServerConfig.getSmtpHost());
		      emailConfigXml.setSmtpPort(emailServerConfig.getSmtpPort());
		      emailConfigXml.setSmtpProtocol(emailServerConfig.getSmtpProtocol());
		      emailConfigXml.setUserId(emailServerConfig.getUserId());
		      emailConfigXml.setUseSsl(emailServerConfig.getUseSsl());
		      emailConfigXml.setUseStartTls(emailServerConfig.getUseStartTls());*/

			saveDom(emailConfigXml.getDocument(), emailConfigFile);
		} catch (Exception e) {
			logger.error(Messages.getErrorString("ERROR PENDING")); //$NON-NLS-1$
		}
		return Response.ok().build();
	}

	private void saveDom(Document document, File file) throws IOException {
		file.createNewFile();
		FileOutputStream fileOutputStream = new FileOutputStream(file);
		XmlDom4JHelper.saveDom(document, fileOutputStream, ConsoleConfig.getInstance().getXmlEncoding());
		try {
			fileOutputStream.close();
		} catch (IOException ex) {
			// Do nothing.
		}
	}
}
