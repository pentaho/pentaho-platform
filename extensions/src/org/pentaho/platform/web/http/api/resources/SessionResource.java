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
 * 
 */
@Path( "/session/" )
public class SessionResource extends AbstractJaxRSResource {

  private static SessionService sessionService;

  public SessionResource() {
    sessionService = new SessionService();
  }

  /**
   * Returns the current user's workspace folder path
   *
   * <p>Example Request:<br>
   *               GET api/session/userWorkspaceDir<br>
   *               <p/>
   *
   * <p>Example Response:<br/>
   *               HTTP/1.1 200 OK
   *               </p>
   *               pre function="syntax.xml">
   *               /home/admin/workspace
   *               </pre>
   *
   * @return workspace folder path
   */
  @GET
  @Path( "/userWorkspaceDir" )
  @Produces( TEXT_PLAIN )
  public String doGetCurrentUserDir() {
    return ClientRepositoryPaths.getUserHomeFolderPath( PentahoSessionHolder.getSession().getName() ) + "/workspace";
  }
  
  /**
   * Returns the workspace folder path for the selected user.
   * 
   * @param user (user name)
   *
   * <p>Example Request:<br>
   *               GET api/session/workspaceDirForUser<br>
   *               <p/>
   *
   * <p>Example Response:<br/>
   *               HTTP/1.1 200 OK
   *               </p>
   *               pre function="syntax.xml">
   *               /home/user/workspace
   *               </pre>
   *               
   * @return workspace folder path
   */
  @GET
  @Path( "/workspaceDirForUser" )
  @Produces( TEXT_PLAIN )
  public String doGetUserDir( @PathParam( "user" ) String user ) {
    return ClientRepositoryPaths.getUserHomeFolderPath( user ) + "/workspace";
  }

  @GET
  @Path( "/setredirect" )
  @Produces( TEXT_PLAIN )
  public Response setredirect() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    pentahoSession.setAttribute( "redirect", true );

    return Response.ok().type( MediaType.TEXT_PLAIN ).build();
  }

}
