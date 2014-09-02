/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.web.http.api.resources;

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.web.http.api.resources.services.SessionService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

/**
 * @author wseyler
 */
@Path ( "/session/" )
public class SessionResource extends AbstractJaxRSResource {

  private static SessionService sessionService;

  public SessionResource() {
    sessionService = new SessionService();
  }

  /**
   * Returns the current user's workspace folder path
   * <p/>
   * <p><b>Example Request:</b><br>
   * GET api/session/userWorkspaceDir
   * </p>
   *
   * @return String object containing the workspace folder path
   * <p/>
   * <p>Example Response:
   * </p>
   * <pre function="syntax.xml">
   * /home/admin/workspace
   * </pre>
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
   * Returns the workspace folder path for the selected user.
   * <p/>
   * <p><b>Example Request:</b><br>
   * GET api/session/workspaceDirForUser<br>
   * </p>
   *
   * @param user String of the user name
   * @return String object containing the workspace folder path
   * <p/>
   * <p>Example Response:</p>
   * <pre function="syntax.xml">
   * /home/user/workspace
   * </pre>
   */
  @GET
  @Path ( "/workspaceDirForUser" )
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
