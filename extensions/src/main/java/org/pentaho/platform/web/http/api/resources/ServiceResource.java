/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.Service;
import org.pentaho.platform.settings.ServiceDto;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * This service provides methods related to karaf instances
 *
 * @author tkafalas
 */
@Path( "/service" )
public class ServiceResource extends AbstractJaxRSResource {

  public ServiceResource() {

  }

  /**
   * Returns information about services set on the platform.  Currently services contain port settings
   * but is expected to be expanded to other settings.
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/service/assignment
   * </p>
   *
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @GET
  @Path( "/assignment" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully sent result." ), @ResponseCode( code = 500, condition = "Failed to get data." ) } )
  public List<ServiceDto> doGetPortAssignment() {
    ArrayList<ServiceDto> list = new ArrayList<ServiceDto>();
    for ( Service service : ServerPortRegistry.getServices() ) {
      list.add( new ServiceDto( service ) );
    }
    return list;
  }
}
