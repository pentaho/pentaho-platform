/*
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
 * Copyright 2015 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.osgi.KarafInstance;
import org.pentaho.platform.settings.ServerPortDto;
import org.pentaho.platform.settings.ServerPortRegistry;
import org.pentaho.platform.settings.ServerPortService;
import org.pentaho.platform.settings.ServerPortServiceDto;

/**
 * This service provides methods related to karaf instances
 *
 * @author tkafalas
 */
@Path( "/port" )
public class PortResource extends AbstractJaxRSResource {

  public PortResource() {

  }

  /**
   * Returns information about ports set on the platform
   *
   * <p>
   * <b>Example Request:</b><br />
   * GET pentaho/api/port/assignment
   * </p>
   *
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @GET
  @Path( "/assignment" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully sent result." ),
    @ResponseCode( code = 500, condition = "Failed to get data." ) } )
  public List<ServerPortServiceDto> doGetPortAssignment() {
    ArrayList<ServerPortServiceDto> list = new ArrayList<ServerPortServiceDto>();
    for ( ServerPortService service : ServerPortRegistry.getServices() ) {
      list.add( new ServerPortServiceDto(service));
    }
    return list;
  }
}
