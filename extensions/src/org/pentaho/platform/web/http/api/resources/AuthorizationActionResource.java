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

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.web.http.api.resources.services.AuthorizationActionService;

/**
 * Resource deals with the Authorization Action in the platform.
 *
 * @author rmansoor
 */
@Path ( "/authorization/action" )
public class AuthorizationActionResource {

  private AuthorizationActionService authorizationActionService;

  public AuthorizationActionResource( List<IAuthorizationAction> authActionList ) {
    this.authorizationActionService = new AuthorizationActionService( authActionList );
  }

  public AuthorizationActionResource() {
    this.authorizationActionService = new AuthorizationActionService();
  }

  /**
   * Validates if a current user is authorized to perform a specific action
   * <p/>
   * <p><b>Example Request:</b></p>
   * <pre function="syntax.xml">
   * GET api/authorization/action/isauthorized
   * </pre>
   *
   * @param authAction Authorization Action to be validated for the current user
   * @return A boolean response based on the current user being authorized to perform a specific action within the system.
   */
  @GET
  @Path ( "/isauthorized" )
  @Produces ( { MediaType.TEXT_PLAIN } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns a boolean response." )
  } )
  public Response validateAuth( @QueryParam ( "authAction" ) String authAction ) {
    return Response.ok( ( String.valueOf( authorizationActionService.validateAuth( authAction ) ) ) ).build();
  }

}
