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

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.web.http.api.resources.services.SystemService;

import jakarta.servlet.ServletException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * This service allows for listing system users in the BA Platform.
 */
@Path ( "/users" )
public class SystemUsersResource extends AbstractJaxRSResource {

  /**
   * Returns the list of users in the platform, this list is in an xml format as shown in the example response.
   *
   * <p><b>Example Request:</b><br />
   * <pre function="syntax.xml">
   *    GET pentaho/api/users
   * </pre>
   *
   * @return Response object containing an xml list of users in the platform
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *     &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;users&gt;&lt;user&gt;pat&lt;/user&gt;&lt;user&gt;admin&lt;/user&gt;&lt;user&gt;suzy&lt;/user&gt;&lt;user&gt;tiffany&lt;/user&gt;&lt;user&gt;enco*de:te^s_t$&lt;/user&gt;&lt;/users&gt;
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
