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
 * Copyright 2013 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 03/19/2013
 * @author Peter Minutillo
 *
 */
package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.config.PentahoSpringBeansConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.messages.Messages;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;

/**
 * This api provides methods for discovering information about the system
 *
 * @author pminutillo
 */
@Path("/system/")
public class SystemResource extends AbstractJaxRSResource {

  private static final Log logger = LogFactory.getLog(FileResource.class);

  /**
   * Returns all users, roles, and ACLs in an XML document. Moved
   * here from now removed SystemAllResource class
   *
   * Response Sample:
   * <content>
   *   <users>
   *     <user>joe</user>
   *   </users>
   *   <roles>
   *     <role>Admin</role>
   *   </roles>
   *   <acls>
   *     <acl>
   *       <name>Update</name>
   *       <mask>8</mask>
   *     </acl>
   *   </acls>
   * </content>
   * @return Response containing roles, users, and acls
   * @throws Exception
   */
  @GET
  @Produces({MediaType.APPLICATION_XML})
  public Response getAll()  throws Exception {
    try {
      return Response.ok(SystemResourceUtil.getAll().asXML()).type(MediaType.APPLICATION_XML).build();
    } catch (Throwable t) {
      throw new WebApplicationException(t);
    }
  }

  /**
   * Return JSON string reporting which authentication provider is
   * currently in use
   *
   * Response sample:
   * {
   *  "authenticationType": "JCR_BASED_AUTHENTICATION"
   * }
   *
   * @return AuthenticationProvider represented as JSON response
   * @throws Exception
   */
  @GET
  @Path("/authentication-provider")
  @Produces({MediaType.APPLICATION_JSON})
  public AuthenticationProvider getAuthenticationProvider() throws Exception {
    try{
      File configFile = new File(PentahoSystem.getApplicationContext().getSolutionPath("system/pentaho-spring-beans.xml"));
      // File configFile = new File("/home/pminutillo/IntelliJProjects/pentaho-platform/extensions/test-res/solution1-no-config/system/pentaho-spring-beans.xml");
      PentahoSpringBeansConfig config = new PentahoSpringBeansConfig(configFile);

      return new AuthenticationProvider(config.getAuthenticationProvider().toString());
    }
    catch (Throwable t) {
      logger.error(Messages.getInstance().getString("SystemResource.GENERAL_ERROR"), t); //$NON-NLS-1$
      throw new Exception(t);
    }
  }
}
