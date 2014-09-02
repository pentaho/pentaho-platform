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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.services.RepositoryPublishService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.InputStream;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Publishes a content file to the repository. Used for Analyzer and Report Writer publish
 *
 * @author tkafalas
 */
@Path ( "/repo/publish" )
public class RepositoryPublishResource {

  private static final Log logger = LogFactory.getLog( FileResource.class );

  protected RepositoryPublishService repositoryPublishService;

  public RepositoryPublishResource() {
    repositoryPublishService = new RepositoryPublishService();
  }

  /**
   * Publishes the file to the provided path in the repository. The file will be overwritten if the overwrite flag
   * is set to true
   * <p/>
   * <p/>
   * <p>Example Request:<br>
   * POST api/repo/publish/publishfile
   * </p>
   *
   * @param pathId        Colon separated path for the repository file)
   *                      <pre function="syntax.xml">
   *                      :path:to:file:id
   *                      </pre>
   * @param fileContents  (input stream containing the data)
   * @param overwriteFile (flag to determine whether to overwrite the existing file in the repository or not)
   *                      <pre function="syntax.xml">
   *                      true
   *                      </pre>
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @POST
  @Path ( "/publishfile" )
  @Consumes ( { MediaType.MULTIPART_FORM_DATA } )
  @Produces ( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
      @ResponseCode ( code = 200, condition = "Successfully publish the artifact." ),
      @ResponseCode ( code = 403, condition = "Failure to publish the file due to permissions." ),
      @ResponseCode ( code = 500, condition = "Failure to publish the file due to a server error." ),
  } )
  public Response writeFile( @FormDataParam ( "importPath" ) String pathId,
                             @FormDataParam ( "fileUpload" ) InputStream fileContents,
                             @FormDataParam ( "overwriteFile" ) Boolean overwriteFile,
                             @FormDataParam ( "fileUpload" ) FormDataContentDisposition fileInfo ) {
    try {
      repositoryPublishService.writeFile( pathId, fileContents, overwriteFile );
      return buildPlainTextOkResponse( "SUCCESS" );
    } catch ( PentahoAccessControlException e ) {
      return buildStatusResponse( UNAUTHORIZED, PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL );
    } catch ( PlatformImportException e ) {
      logger.error( e );
      return buildStatusResponse( PRECONDITION_FAILED, e.getErrorStatus() );
    } catch ( Exception e ) {
      logger.error( e );
      return buildServerErrorResponse( PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
  }

  protected Response buildPlainTextOkResponse( String msg ) {
    return Response.ok( msg ).type( MediaType.TEXT_PLAIN ).build();
  }

  protected Response buildStatusResponse( Status status, int entity ) {
    return Response.status( status ).entity( Integer.toString( entity ) ).build();
  }

  protected Response buildServerErrorResponse( int entity ) {
    return Response.serverError().entity( Integer.toString( entity ) ).build();
  }

}
