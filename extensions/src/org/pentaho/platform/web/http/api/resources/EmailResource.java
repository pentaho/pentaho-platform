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
import org.pentaho.platform.api.email.IEmailConfiguration;
import org.pentaho.platform.api.email.IEmailService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.email.EmailConfiguration;
import org.pentaho.platform.plugin.services.email.EmailService;

@Path("/emailconfig/")
public class EmailResource extends AbstractJaxRSResource {
  /**
   * The logger for this class
   */
  private static final Log logger = LogFactory.getLog(EmailResource.class);

  private IEmailService emailService = null;
  private IAuthorizationPolicy policy;

  /**
   * Constructs an instance of this class using the default email service
   * 
   * @throws IllegalArgumentException
   *           Indicates that the default location for the email configuration file is invalid
   */
  public EmailResource() throws IllegalArgumentException {
    try {
      emailService = PentahoSystem.get(IEmailService.class, "IEmailService", PentahoSessionHolder.getSession());
    } catch (RuntimeException ex) {
      // create default
      emailService = new EmailService();
    }
    init(emailService);
  }

  /**
   * Constructs an instance of this class using the default email service
   * 
   * @throws IllegalArgumentException
   *           Indicates that the default location for the email configuration file is invalid
   */
  public EmailResource(final IEmailService emailService) throws IllegalArgumentException {
    init(emailService);
  }

  private void init(final IEmailService emailService) {
    if (emailService == null) {
      throw new IllegalArgumentException();
    }
    this.emailService = emailService;
    try {
      policy = PentahoSystem.get(IAuthorizationPolicy.class);
    } catch (Exception ex) {
      logger.warn("Unable to get IAuthorizationPolicy: " + ex.getMessage());
    }
  }

  @PUT
  @Path("/setEmailConfig")
  @Consumes({ MediaType.APPLICATION_JSON })
  public Response setEmailConfig(EmailConfiguration emailConfiguration) {
    if (policy == null || policy.isAllowed(IAuthorizationPolicy.ADMINISTER_SECURITY_ACTION)) {
      try {
        emailService.setEmailConfig(emailConfiguration);
      } catch (Exception e) {
        return Response.serverError().build();
      }
    }
    return Response.ok().build();
  }

  @GET
  @Path("/getEmailConfig")
  @Produces({ MediaType.APPLICATION_JSON })
  public IEmailConfiguration getEmailConfig() {
    if (policy == null || policy.isAllowed(IAuthorizationPolicy.ADMINISTER_SECURITY_ACTION)) {
      try {
        return emailService.getEmailConfig();
      } catch (Exception e) {
        return new EmailConfiguration();
      }
    } else {
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
