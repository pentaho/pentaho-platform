/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
