/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.web.http.api.resources.services.FileService;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.WILDCARD;

/**
 * This resource manages directories in the repository
 *
 * @author wseyler
 */

@Path ( "/repo/dirs/" )
public class DirectoryResource extends AbstractJaxRSResource {
  protected FileService fileService;

  private static final Log logger = LogFactory.getLog( FileResource.class );

  public DirectoryResource() {
    super();
    fileService = new FileService();
  }

  public DirectoryResource( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  /**
   * <p>Creates a new folder with the specified name</p>
   * <p/>
   * <p>Creates a new folder from a given path. Example: (:public:admin:test). It will create folder
   * public --> admin --> test. If folder already exists then it skips to the next folder to be created. </p>
   * <p/>
   * <p><b>Example Request:</b>
   * <br>
   * PUT /pentaho/api/repo/dirs/pathToFile
   * <p/>
   *
   * @param pathId The path from the root folder to the root node of the tree to return using colon characters in
   *               place of / or \ characters. To clarify /path/to/file, the encoded pathId would be :path:to:file
   *               <pre function="syntax.xml">
   *               :path:to:file
   *               </pre>
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @PUT
  @Path ( "{pathId : .+}" )
  @Consumes ( { WILDCARD } )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully created folder." ),
      @ResponseCode ( code = 409, condition = "Path already exists." ),
      @ResponseCode ( code = 500, condition = "Server Error." )
  } )
  public Response createDirs( @PathParam ( "pathId" ) String pathId ) {
    try {
      if ( fileService.doCreateDir( pathId ) ) {
        return Response.ok().build();
      } else {
        return Response.status( Response.Status.CONFLICT ).entity( "couldNotCreateFolderDuplicate" ).build();
      }
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }
}
