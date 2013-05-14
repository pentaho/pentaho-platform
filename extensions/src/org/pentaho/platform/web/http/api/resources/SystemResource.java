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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.messages.Messages;

/**
 * This api provides methods for discovering information about the system
 *
 * @author pminutillo
 */
@Path("/system/")
@ExternallyManagedLifecycle
public class SystemResource extends AbstractJaxRSResource {

  private static final Log logger = LogFactory.getLog(FileResource.class);
  private ISystemConfig systemConfig;

  public SystemResource() {
    this(PentahoSystem.get(ISystemConfig.class));
  }

  public SystemResource(ISystemConfig systemConfig) {
    this.systemConfig = systemConfig;
  }
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
      IConfiguration config = this.systemConfig.getConfiguration("security");
      String provider = config.getProperties().getProperty("provider");
      return new AuthenticationProvider(provider);
    }
    catch (Throwable t) {
      logger.error(Messages.getInstance().getString("SystemResource.GENERAL_ERROR"), t); //$NON-NLS-1$
      throw new Exception(t);
    }
  }
  
  /**
   * @return a list of TimeZones ensuring that the server (default) timezone is at the top of the list (0th element)
   */
  @GET
  @Path("/timezones")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public TimeZoneWrapper getTimeZones() {
    Map<String, String> timeZones = new HashMap<String, String>();
    for (String tzId : TimeZone.getAvailableIDs()) {
      if (!tzId.toLowerCase().contains("gmt")) {
        int offset = TimeZone.getTimeZone(tzId).getOffset(System.currentTimeMillis());
        String text = String.format("%s%02d%02d", offset >= 0 ? "+" : "", offset / 3600000, (offset / 60000) % 60); 
        timeZones.put(tzId, TimeZone.getTimeZone(tzId).getDisplayName(true, TimeZone.LONG) + " (UTC" + text + ")");       
      }
    }
    return new TimeZoneWrapper(timeZones, TimeZone.getDefault().getID());
  }

}
