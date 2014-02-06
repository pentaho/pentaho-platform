/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2014 Pentaho Corporation.  All rights reserved.
 */
package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.PasswordHelper;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * provides a resource for encrypting a password using the configured IPasswordService
 */
@Path( "/password" )
public class PasswordResource {

  @GET
  @Path( "/encrypt" )
  @Produces( MediaType.TEXT_PLAIN )
  public Response encryptPassword( @QueryParam( "password" ) String password ) {
    if ( isAllowed() ) {
      return Response.ok( getPasswordHelper().encrypt( password ) ).build();
    }
    return Response.status( UNAUTHORIZED ).build();
  }

  PasswordHelper getPasswordHelper() {
    return new PasswordHelper();
  }

  private boolean isAllowed() {
    return getAuthorizationPolicy().isAllowed( AdministerSecurityAction.NAME );
  }

  IAuthorizationPolicy getAuthorizationPolicy() {
    return PentahoSystem.get( IAuthorizationPolicy.class );
  }
}
