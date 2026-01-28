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
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.services.SystemService;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path( "/roles" )
@Facet( name = "Unsupported" )
public class SystemRolesResource extends AbstractJaxRSResource {

  /**
   * Returns the roles in the platform
   * 
   * @return list of roles
   * 
   * @throws Exception
   */
  @GET
  @Produces( { MediaType.APPLICATION_XML } )
  @Facet ( name = "Unsupported" )
  public Response getRoles() throws Exception {
    try {
      if ( canAdminister() ) {
        return Response.ok( SystemService.getSystemService().getRoles().asXML() ).type( MediaType.APPLICATION_XML ).build();
      } else {
        return Response.status( UNAUTHORIZED ).build();
      }
    } catch ( Throwable t ) {
      throw new WebApplicationException( t );
    }
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }
}
