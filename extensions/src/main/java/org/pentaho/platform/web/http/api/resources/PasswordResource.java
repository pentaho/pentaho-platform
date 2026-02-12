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
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.util.PasswordHelper;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.Consumes;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * provides a resource for encrypting a password using the configured IPasswordService
 */
@Path( "/password" )
public class PasswordResource {
  private static final String FORM_HTML =
    "<html><body><form method='post'><input type=\"password\" name=\"password\"/>"
      + "<input type=\"submit\" value=\"Submit\"/></form><br/>%s</body></html>";

  @POST
  @Path( "/encrypt" )
  @Produces( MediaType.TEXT_HTML )
  @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
  @Facet( name = "Unsupported" )
  public Response encryptPassword( @FormParam( "password" ) String password ) {
    if ( isAllowed() ) {
      String encPwd = "";
      if ( password != null ) {
        encPwd = getPasswordHelper().encrypt( password );
      }
      return Response.ok( String.format( FORM_HTML, encPwd ) ).build();
    }
    return Response.status( UNAUTHORIZED ).build();
  }

  @GET
  @Path( "/encrypt" )
  @Produces( MediaType.TEXT_HTML )
  @Facet ( name = "Unsupported" )
  public Response encryptionForm() {
    if ( isAllowed() ) {
      return Response.ok( String.format( FORM_HTML, "" ) ).build();
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
