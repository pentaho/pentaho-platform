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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.services.RepositoryPublishService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Optional;
import java.util.Properties;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

/**
 * Publishes a content file to the repository. Used for Analyzer and Report Writer publish
 *
 * @author tkafalas
 */
@Path ( "/repo/publish" )
public class RepositoryPublishResource {

  private static final Log logger = LogFactory.getLog( RepositoryPublishResource.class );

  protected RepositoryPublishService repositoryPublishService;

  public RepositoryPublishResource() {
    repositoryPublishService = new RepositoryPublishService();
  }

  /**
   * Publishes the file to the provided path in the repository. The file will be overwritten if the overwrite flag
   * is set to true.
   *
   * <p><b>Example Request:</b><br />
   *    POST pentaho/api/repo/publish/publishfile
   * </p>
   *
   * @param pathId        Path for the repository file, e.g. /public/file.txt
   * @param fileContents  Input stream containing the data.
   * @param overwriteFile Flag to determine whether to overwrite the existing file in the repository or not.
   * @param fileInfo  File information (Currently not being used).
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   * @deprecated use {@linkplain #writeFileWithEncodedName(String, InputStream, Boolean, FormDataContentDisposition)} instead
   */
  @POST
  @Path ( "/publishfile" )
  @Consumes ( { MediaType.MULTIPART_FORM_DATA } )
  @Produces ( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully publish the artifact." ),
    @ResponseCode ( code = 403, condition = "Failure to publish the file due to permissions." ),
    @ResponseCode ( code = 500, condition = "Failure to publish the file due to a server error." ), } )
  @Facet( name = "Unsupported" )
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

  /**
   * Publishes the file to the provided path in the repository. The file will be overwritten if {@code overwriteFile}
   * is {@code true}.
   *
   * This method should be used instead of
   * {@linkplain #writeFile(String, InputStream, Boolean, FormDataContentDisposition)}. Contrary to
   * {@linkplain FileResource} convention, it expects {@code pathId} <b>not</b> to be separated by colons, but to be
   * simply encoded with {@linkplain java.net.URLEncoder}. Also it expects {@code pathId} to be a well-formatted
   * Unix-style path with no slash at the end.
   *
   * Examples of correct {@code pathId}:
   * <ul>
   *   <li><tt>%2Fpublic%2Fmyfile.txt</tt></li>
   *   <li><tt>%2Fpublic%2Fmyfile</tt></li>
   *   <li><tt>%2Fpublic%2Fmyfile</tt></li>
   *   <li><tt>%2Fpublic%2Fmyfile%22quoted%22</tt></li>
   * </ul>
   *
   * @param pathId        slash-separated path for the repository file
   * @param fileContents  input stream containing the data
   * @param overwriteFile flag to determine whether to overwrite the existing file in the repository or not
   * @param fileInfo      file information (Currently not being used).
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @POST
  @Path ( "/file" )
  @Consumes ( { MediaType.MULTIPART_FORM_DATA } )
  @Produces ( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully publish the file." ),
    @ResponseCode ( code = 403, condition = "Failure to publish the file due to permissions." ),
    @ResponseCode ( code = 422, condition = "Failure to publish the file due to failed validation." ),
    @ResponseCode ( code = 500, condition = "Failure to publish the file due to a server error." ), } )
  @Facet( name = "Unsupported" )
  public Response writeFileWithEncodedName( @FormDataParam( "importPath" ) String pathId,
                                            @FormDataParam( "fileUpload" ) InputStream fileContents,
                                            @FormDataParam( "overwriteFile" ) Boolean overwriteFile,
                                            @FormDataParam( "fileUpload" ) FormDataContentDisposition fileInfo ) {
    Optional<Properties> fileProperties = Optional.of( new Properties() );
    fileProperties.get().setProperty( "overwriteFile", String.valueOf( overwriteFile ) );
    return writeFile( pathId, fileContents, fileInfo, fileProperties );
  }

  @POST
  @Path ( "/fileWithOptions" )
  @Consumes ( { MediaType.MULTIPART_FORM_DATA } )
  @Produces ( MediaType.TEXT_PLAIN )
  @StatusCodes ( {
    @ResponseCode ( code = 200, condition = "Successfully publish the file." ),
    @ResponseCode ( code = 403, condition = "Failure to publish the file due to permissions." ),
    @ResponseCode ( code = 422, condition = "Failure to publish the file due to failed validation." ),
    @ResponseCode ( code = 500, condition = "Failure to publish the file due to a server error." ), } )
  @Facet( name = "Unsupported" )
  public Response writeFileWithEncodedNameWithOptions( @FormDataParam( "properties" ) String properties,
                                                       @FormDataParam( "importPath" ) String pathId,
                                                       @FormDataParam( "fileUpload" ) InputStream fileContents,
                                                       @FormDataParam( "fileUpload" ) FormDataContentDisposition fileInfo ) {
    try ( ByteArrayInputStream bios = new ByteArrayInputStream( properties.getBytes() ) ) {
      Optional<Properties> fileProperties = Optional.of( new Properties() );
      fileProperties.get().loadFromXML( bios );
      return writeFile( pathId, fileContents, fileInfo, fileProperties );
    } catch ( IOException e ) {
      logger.error( e );
      return buildServerErrorResponse( PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
  }

  private Response writeFile( String pathId, InputStream fileContents, FormDataContentDisposition fileInfo, Optional<Properties> fileProperties ) {
    try {
      String decodedPath = URLDecoder.decode( pathId, "UTF-8" );
      if ( invalidPath( decodedPath ) ) {
        final int UNPROCESSABLE_ENTITY = 422;
        return Response.status( UNPROCESSABLE_ENTITY )
          .type( MediaType.TEXT_PLAIN_TYPE )
          .entity( "Cannot publish [" + decodedPath + "] because it contains reserved character(s)" )
          .build();
      }
      repositoryPublishService.publishFile( decodedPath, fileContents, fileProperties );
    } catch ( PentahoAccessControlException e ) {
      return buildStatusResponse( UNAUTHORIZED, PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL );
    } catch ( PlatformImportException e ) {
      logger.error( e );
      return buildStatusResponse( PRECONDITION_FAILED, e.getErrorStatus() );
    } catch ( Exception e ) {
      logger.error( e );
      return buildServerErrorResponse( PlatformImportException.PUBLISH_GENERAL_ERROR );
    }
    return buildPlainTextOkResponse( "SUCCESS" );
  }

  // package-local visibility for testing purposes
  boolean invalidPath( String path ) {
    char[] chars = new FileService().doGetReservedChars().toString().toCharArray();
    return FileUtils.containsReservedCharacter( path, chars );
  }

  protected Response buildPlainTextOkResponse( String msg ) {
    return Response.ok( msg ).type( MediaType.TEXT_PLAIN_TYPE ).build();
  }

  protected Response buildStatusResponse( Status status, int entity ) {
    return Response.status( status ).entity( Integer.toString( entity ) ).build();
  }

  protected Response buildServerErrorResponse( int entity ) {
    return Response.serverError().entity( Integer.toString( entity ) ).build();
  }

}
