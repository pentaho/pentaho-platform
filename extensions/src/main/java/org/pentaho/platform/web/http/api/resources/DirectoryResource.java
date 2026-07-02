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
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

/**
 * This resource manages folders in the repository and provides methods for creating new folders.
 */
@Path ( "/repo/dirs/" )
public class DirectoryResource extends AbstractJaxRSResource {
  protected FileService fileService;

  public DirectoryResource() {
    super();
    fileService = new FileService();
  }

  public DirectoryResource( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  /**
   * Creates a new folder with the specified name.
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/api/repo/dirs/home
   * <br /><b>PUT data:</b>
   *  <pre function="syntax.xml">
   *    This PUT body does not contain data.
   *  </pre>
   * </p>
   *
   * @param pathId The path from the root folder to the root node of the tree to return using colon characters in
   *               place of / or \ characters. To clarify /path/to/file, the encoded pathId would be :path:to:file.
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   * <p><b>Example Response:</b></p>
   *  <pre function="syntax.xml">
   *     couldNotCreateFolderDuplicate
   *  </pre>
   */
  @PUT
  @Path( "{pathId : .+}" )
  @Consumes( { WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully created folder." ),
    @ResponseCode( code = 403, condition = "Forbidden." ),
    @ResponseCode( code = 409, condition = "Path already exists." ),
    @ResponseCode( code = 500, condition = "Server Error." ) } )
  public Response createDirs( @PathParam ( "pathId" ) String pathId ) {
    try {
      if ( FileUtils.isRootLevelPath( FileUtils.idToPath( pathId ) ) ) {
        return Response.status( Response.Status.FORBIDDEN ).entity( "couldNotCreateRootLevelFolder" ).build();
      }
      if ( fileService.doCreateDirSafe( pathId ) ) {
        return Response.ok().build();
      } else {
        return Response.status( Response.Status.CONFLICT ).entity( "couldNotCreateFolderDuplicate" ).build();
      }
    } catch ( FileService.InvalidNameException e ) {
      return Response.status( Response.Status.FORBIDDEN ).entity( "containsIllegalCharacters" ).build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( "" ).build();
    }
  }

  /**
   * Determines whether a current user has permission to see the folder or not
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/repo/dirs/home/isVisible
   * </p>
   *
   * @param pathId The path from the root folder to the root node of the tree to return using colon characters in
   *               place of / or \ characters. To clarify /path/to/file, the encoded pathId would be :path:to:file.
   *
   * @return String "true" if the folder is visible to the current user, or "false" otherwise.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      true
   *    </pre>
   */
  @GET
  @Path ( "{pathId : .+}/isVisible" )
  @Consumes( { WILDCARD } )
  @Produces( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully returns a boolean value, either true or false" ) } )
  public Response isDirVisible( @PathParam ( "pathId" ) String pathId ) {
    try {
      if ( fileService.doesExist( pathId ) && fileService.isFolder( pathId ) ) {
        return Response.ok( ( String.valueOf( fileService.doGetIsVisible( pathId ) ) ) ).build();
      } else {
        return Response.ok( "false" ).build();
      }
    } catch ( Exception e ) {
      return Response.ok( "false" ).build();
    }
  }

  /**
   * Checks to see if the current user is an administer of the platform and returns a boolean response.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/api/repo/dirs/home/defaultSaveLocation
   * </p>
   *
   * @return path for a default save location.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      /public
   *    </pre>
   */
  @GET
  @Path ( "{pathId : .+}/defaultLocation" )
  @Produces( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully returns a default open/save location" ) } )
  public Response getDefaultLocation( @PathParam ( "pathId" ) String pathId ) {
    try {
      return Response.ok( String.valueOf( fileService.doGetDefaultLocation( pathId ) ) ).build();
    } catch ( Exception e ) {
      return Response.ok( pathId ).build();
    }
  }
}
