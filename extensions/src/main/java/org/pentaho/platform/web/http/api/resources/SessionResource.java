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

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.web.http.api.resources.services.SessionService;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * The SessionResource service lists the user's current workspace as well as the workspace folder path.
 */
@Path ( "/session/" )
public class SessionResource extends AbstractJaxRSResource {

  private static SessionService sessionService;

  public SessionResource() {
    sessionService = new SessionService();
  }

  /**
   * Returns the current user's workspace folder path.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/session/userWorkspaceDir
   * </p>
   *
   * @return String object containing the workspace folder path.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *    /home/admin/workspace
   *  </pre>
   */
  @GET
  @Path ( "/userWorkspaceDir" )
  @Produces ( TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns the requested file path" )
  } )
  public String doGetCurrentUserDir() {
    return getUserHomeFolderPath( getSession().getName() ) + "/workspace";
  }

  /**
   * Returns the current user's user name.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/session/userName
   * </p>
   *
   * @return String object The current user name.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *    suzy
   *  </pre>
   */
  @GET
  @Path ( "/userName" )
  @Produces ( TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns the current user name" )
  } )
  public String doGetCurrentUser() {
    return getSession().getName();
  }

  /**
   * Returns the workspace folder path for the selected user.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/session/workspaceDirForUser/admin
   * </p>
   *
   * @param user String of the user name.
   *
   * @return String object containing the workspace folder path.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *    workspace
   *  </pre>
   */
  @GET
  @Path ( "/workspaceDirForUser/{user}" )
  @Produces ( TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Returns the workspace file path for the specified user." ),
      @ResponseCode ( code = 500, condition = "File path failed to be retrieved. This could be caused by an invalid user request." )
  } )
  public String doGetUserDir( @PathParam ( "user" ) String user ) {
    return getUserHomeFolderPath( user ) + "/workspace";
  }

  @GET
  @Path ( "/setredirect" )
  @Produces ( TEXT_PLAIN )
  @Facet ( name = "Unsupported" )
  public Response setredirect() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    pentahoSession.setAttribute( "redirect", true );

    return Response.ok().type( MediaType.TEXT_PLAIN ).build();
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected String getUserHomeFolderPath( String username ) {
    return ClientRepositoryPaths.getUserHomeFolderPath( username );
  }
}
