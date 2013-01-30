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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.plugin.services.email.EmailConfiguration;
import org.pentaho.platform.plugin.services.email.EmailService;

@Path("/emailconfig/")
public class EmailResource extends AbstractJaxRSResource {

	private EmailService emailService = null;

	/**
	 * Constructs an instance of this class using the default email service
	 * 
	 * @throws IllegalArgumentException
	 *             Indicates that the default location for the email
	 *             configuration file is invalid
	 */
	public EmailResource() throws IllegalArgumentException {
		this(new EmailService());
	}

	/**
	 * Constructs an instance of this class using the default email service
	 * 
	 * @throws IllegalArgumentException
	 *             Indicates that the default location for the email
	 *             configuration file is invalid
	 */
	public EmailResource(final EmailService emailService) throws IllegalArgumentException {
		if (emailService == null) {
			throw new IllegalArgumentException();
		}
		this.emailService = emailService;
	}

	@PUT
	@Path("/setEmailConfig")
	@Consumes({ MediaType.APPLICATION_JSON })
	public Response setEmailConfig(EmailConfiguration emailConfiguration) {
		try {
			emailService.setEmailConfig(emailConfiguration);
		} catch (Exception e) {
			return Response.serverError().build();
		}
		return Response.ok().build();
	}

	@GET
	@Path("/getEmailConfig")
	@Produces({ MediaType.APPLICATION_JSON })
	public EmailConfiguration getEmailConfig() {
		try {
			return emailService.getEmailConfig();
		} catch (Exception e) {
			return new EmailConfiguration();
		}
	}

	@PUT
	@Path("/sendEmailTest")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.TEXT_PLAIN })
	public String sendEmailTest(EmailConfiguration emailConfiguration) throws Exception {
		return emailService.sendEmailTest(emailConfiguration);
	}
	
  @GET
  @Path("/isValid")
  @Produces({ MediaType.TEXT_PLAIN })
  public Response isValid() {
    try {
      if (emailService.isValid()) {
        return Response.ok("true").build();
      }
    } catch (Exception e) {
    }
    return Response.ok("false").build();
  }	
	
}
