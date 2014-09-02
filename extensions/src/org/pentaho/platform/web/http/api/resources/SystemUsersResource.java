/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.web.http.api.resources.services.SystemService;

import javax.servlet.ServletException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path ( "/users" )
public class SystemUsersResource extends AbstractJaxRSResource {

  /**
   * Returns the list of users in the platform, this list is in an xml format as shown in the example response.
   * <p/>
   * <p><b>Example Request:</b><br>
   * GET api/users<br>
   * </p>
   *
   * @return Response object containing an xml list of users in the platform
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * &lt;xml&gt;
   * &lt;userList&gt;
   * &lt;users&gt;suzy&lt;/users&gt;
   * &lt;users&gt;pat&lt;/users&gt;
   * &lt;users&gt;tiffany&lt;/users&gt;
   * &lt;users&gt;admin&lt;/users&gt;
   * &lt;/userList&gt;
   * &lt;/xml&gt;
   * </pre>
   */
  @GET
  @Produces ( { MediaType.APPLICATION_XML } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Response object containing an xml list of the users in the platform." ),
      @ResponseCode ( code = 403, condition = "Response due to the requesting user not having sufficient privileges." ),
      @ResponseCode ( code = 500, condition = "Internal server error occurs when the server cannot retrieve the list of users." )
  } )
  public Response getUsers() throws Exception {
    try {
      return buildOkResponse( getSystemService().getUsers().asXML(), MediaType.APPLICATION_XML );
    } catch ( IllegalAccessException exception ) {
      return Response.status( UNAUTHORIZED ).build();
    } catch ( ServletException exception ) {
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    } catch ( IOException exception ) {
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  protected SystemService getSystemService() {
    return SystemService.getSystemService();
  }

  protected Response buildOkResponse( Object entity, String type ) {
    return Response.ok( entity ).type( type ).build();
  }
}
