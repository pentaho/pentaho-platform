/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.web.http.api.resources.services.AuthorizationActionService;

/**
 * Resource deals with the Authorization Action in the BA Platform. This service validates if a current user is authorized to perform a specific action.
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
   * Validates if a current user is authorized to perform a specific action.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/authorization/action/isauthorized
   * </p>
   *
   * @param authAction Authorization Action to be validated for the current user.
   *
   * @return A boolean response based on the current user being authorized to perform a specific action within the system.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *     false
   * </pre>
   */
  @GET
  @Path ( "/isauthorized" )
  @Produces ( { MediaType.TEXT_PLAIN } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns a boolean response." )
    } )
  public Response validateAuth( @QueryParam ( "authAction" ) String authAction ) {
    return Response.ok( ( String.valueOf( getAuthorizationActionService().validateAuth( authAction ) ) ) ).build();
  }

  /* useful for unit test mocks */
  protected AuthorizationActionService getAuthorizationActionService() {
    return authorizationActionService;
  }
}
