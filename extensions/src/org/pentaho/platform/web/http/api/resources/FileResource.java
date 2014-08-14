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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.channels.IllegalSelectorException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.doc.ExcludeFromDocumentation;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.Exporter;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * Represents a file node in the getRepository(). This api provides methods for discovering information about repository
 * files as well as CRUD operations
 *
 * @author aaron
 */
@Path( "/repo/files/" )
public class FileResource extends AbstractJaxRSResource {
  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$

  protected static final Log logger = LogFactory.getLog( FileResource.class );

  protected FileService fileService;

  protected RepositoryDownloadWhitelist whitelist;

  protected static IUnifiedRepository repository;

  protected static DefaultUnifiedRepositoryWebService repoWs;

  protected static IAuthorizationPolicy policy;

  IRepositoryContentConverterHandler converterHandler;
  Map<String, Converter> converters;

  protected NameBaseMimeResolver mimeResolver;

  public FileResource() {
    fileService = new FileService();
  }

  public FileResource( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  public static String idToPath( String pathId ) {
    return FileUtils.idToPath( pathId );
  }

  /**
   * <p>Moves the list of files to the user's trash folder
   * <p/>
   * Move a list of files to the user's trash folder, the list should be comma separated.
   *
   * @param params Comma separated list of the files to be deleted
   *               <pre function="syntax.xml">
   *               fileid1, fileid2 ...
   *               </pre>
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @PUT
  @Path( "/delete" )
  @Consumes( { WILDCARD } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successful deletion of file." ),
      @ResponseCode( code = 500, condition = "Failure to delete the file." )
  })
  public Response doDeleteFiles( String params ) {
    try {
      fileService.doDeleteFiles( params );
      return buildOkResponse();

    } catch ( Throwable t ) {
      return buildServerErrorResponse( t );
    }
  }

  /**
   * Permanently deletes the selected list of files from the repository
   *
   * @param params Comma separated list of the files to be deleted
   * @return Server Response indicating the success of the operation
   */
  @PUT
  @Path( "/deletepermanent" )
  @Consumes( { WILDCARD } )
  @Facet ( name = "Unsupported" )
    public Response doDeleteFilesPermanent( String params ) {
    try {
      fileService.doDeleteFilesPermanent( params );
      return buildOkResponse();
    } catch ( Throwable t ) {
      t.printStackTrace();
      return buildServerErrorResponse( t );
    }
  }

  /**
   * <p>Moves a list of files from its current location to another.
   * <p/>
   * Moves a list of files from its current location to another, the list should be comma separated.
   *
   * @param destPathId colon separated path for the destination path
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param params comma separated list of files to be moved
   * <pre function="syntax.xml">
   *    pathId1,pathId2,...
   * </pre>
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @PUT
  @Path( "{pathId : .+}/move" )
  @Consumes( { WILDCARD } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully moved the file." ),
      @ResponseCode( code = 403, condition = "Failure to move the file due to path not found." ),
      @ResponseCode( code = 500, condition = "Failure to move the file." )

  })
  public Response doMove( @PathParam( "pathId" ) String destPathId, String params ) {
    try {
      fileService.doMoveFiles( destPathId, params );
      return buildOkResponse();
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.DESTINATION_PATH_UNKNOWN", destPathId ), e );
      return buildStatusResponse( NOT_FOUND );
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "SystemResource.FILE_MOVE_FAILED" ), t );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * <p>Restores a list of files from the user's trash folder
   * <p/>
   * Restores a list of files from the user's trash folder to their previous locations. The list should be comma
   * separated.
   *
   * @param params comma separated list of file ids to be restored
   * <pre function="syntax.xml">
   *    pathId1,pathId2,...
   * </pre>
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @PUT
  @Path( "/restore" )
  @Consumes( { WILDCARD } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully restored the file." ),
      @ResponseCode( code = 403, condition = "Failure to Restore the file." ),

  })
  public Response doRestore( String params ) {
    try {
      fileService.doRestoreFiles( params );
      return buildOkResponse();
    } catch ( InternalError e ) {
      logger.error( Messages.getInstance().getString( "FileResource.FILE_GET_LOCALES" ), e );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * <p>Create a new file.</p>
   *
   * Creates a new file with the provided contents at a given path
   *
   * @param pathId Colon separated path for the destination for files to be copied
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param fileContents An Input Stream with the contents of the file
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @PUT
  @Path( "{pathId : .+}" )
  @Consumes( { WILDCARD } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully restored the file." ),
      @ResponseCode( code = 403, condition = "Failure to Restore the file due to permissions, file already exists, or invalid path id." ),
  })
  public Response createFile( @PathParam( "pathId" ) String pathId, InputStream fileContents ) {
    try {
      fileService.createFile( httpServletRequest.getCharacterEncoding(), pathId, fileContents );
      return buildOkResponse();
    } catch ( Throwable t ) {
      return buildServerErrorResponse( t );
    }
  }

  /**
   * <p>Copy selected list of files to a new specified location.</p>
   *
   * Copy selected list of files to a new specified location.  List of files path ids should be comma separated.
   * An overwrite mode can be specified as below.
   *
   *  <p>Example Request:<br>
   *               PUT api/repo/files/pathToDir/children?mode=2
   *               </p>
   *
   * @param pathId Colon separated path for the destination for files to be copied
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param mode   Default is RENAME (2) which adds a number to the end of the file name. MODE_OVERWRITE (1) will just replace existing
   *               or MODE_NO_OVERWRITE (3) will not copy if file exists.
   *               <pre function="syntax.xml">
   *               2
   *               </pre>
   * @param params Comma separated list of file ids to be copied
   *               <pre function="syntax.xml">
   *               fileId1,fileId2...
   *               </pre>
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @PUT
  @Path( "{pathId : .+}/children" )
  @Consumes( { TEXT_PLAIN } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully copied the file." ),
      @ResponseCode( code = 500, condition = "Failure to Copy file due to exception while getting file with id fileid..."),
  })
  public Response doCopyFiles( @PathParam( "pathId" ) String pathId, @QueryParam( "mode" ) Integer mode,
      String params ) {
    try {
      fileService.doCopyFiles( pathId, mode, params );
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return buildSafeHtmlServerErrorResponse( e );
    }

    return buildOkResponse();
  }

  /**
   * <p>Retrieve a file or folder by path</p>
   *
   *  <p>Example Request:<br>
   *     GET api/repo/files/pathToFile
   *  </p>
   *
   * Takes a pathId and returns a response object with the output stream based on the file located at the pathId
   *
   * @param pathId Colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @GET
  @Path( "{pathId : .+}" )
  @Produces( { WILDCARD } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully get the file or dir." ),
      @ResponseCode( code = 404, condition = "Failed to find the file or resource."),
      @ResponseCode( code = 500, condition = "Failed to open content.")
  })
  public Response doGetFileOrDir( @PathParam( "pathId" ) String pathId ) {
    try {
      FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileOrDir( pathId );
      return buildOkResponse( wrapper );
    } catch ( FileNotFoundException fileNotFound ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), fileNotFound );
      return buildStatusResponse( NOT_FOUND );
    } catch ( IllegalArgumentException illegalArgument ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), illegalArgument );
      return buildStatusResponse( FORBIDDEN );
    }
  }

  // Overloaded this method to try and minimize calls to the repo
  // Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
  // @GET
  // @Path("{pathId : .+}")
  // @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip( @PathParam( "pathId" ) String pathId ) {
    String path = fileService.idToPath( pathId );

    if ( !isPathValid( path ) ) {
      return buildStatusResponse( FORBIDDEN );
    }

    // you have to have PublishAction in order to get dir as zip
    if ( !getPolicy().isAllowed( PublishAction.NAME ) ) {
      return buildStatusResponse( FORBIDDEN );
    }

    RepositoryFile repoFile = getRepository().getFile( path );

    if ( repoFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      return buildStatusResponse( NOT_FOUND );
    }

    return doGetDirAsZip( repoFile );
  }

  /**
   * @param repositoryFile
   * @return
   */
  public Response doGetDirAsZip( RepositoryFile repositoryFile ) {

    String path = repositoryFile.getPath();

    final InputStream is;

    try {
      Exporter exporter = getExporter();
      exporter.setRepoPath( path );
      exporter.setRepoWs( repoWs );

      File zipFile = exporter.doExportAsZip( repositoryFile );
      is = getFileInputStream( zipFile );
    } catch ( Exception e ) {
      return buildServerErrorResponse( e.toString() );
    }

    StreamingOutput streamingOutput = getStreamingOutput( is );

    return buildOkResponse( streamingOutput, APPLICATION_ZIP );
  }

  /**
   * Determines whether a selected file supports parameters or not
   *
   * @param pathId (colon separated path for the repository file)
   * @return ("true" or "false")
   * @throws FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}/parameterizable" )
  @Produces( TEXT_PLAIN )
  @Facet ( name = "Unsupported" )
  // have to accept anything for browsers to work
  public String doIsParameterizable( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = getRepository().getFile( fileService.idToPath( pathId ) );
    if ( repositoryFile != null ) {
      try {
        hasParameterUi = hasParameterUi( repositoryFile );
      } catch ( NoSuchBeanDefinitionException e ) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    if ( hasParameterUi ) {
      try {
        IContentGenerator parameterContentGenerator = getContentGenerator( repositoryFile );
        if ( parameterContentGenerator != null ) {
          ByteArrayOutputStream outputStream = getByteArrayOutputStream();
          parameterContentGenerator.setOutputHandler( new SimpleOutputHandler( outputStream, false ) );
          parameterContentGenerator.setMessagesList( new ArrayList<String>() );

          Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();

          SimpleParameterProvider parameterProvider = getSimpleParameterProvider();
          parameterProvider.setParameter( "path", encode( repositoryFile.getPath() ) );
          parameterProvider.setParameter( "renderMode", "PARAMETER" );
          parameterProviders.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );

          parameterContentGenerator.setParameterProviders( parameterProviders );
          parameterContentGenerator.setSession( getSession() );
          parameterContentGenerator.createContent();

          if ( outputStream.size() > 0 ) {
            Document document = parseText( outputStream.toString() );

            // exclude all parameters that are of type "system", xactions set system params that have to be ignored.
            @SuppressWarnings( "rawtypes" )
            List nodes = document.selectNodes( "parameters/parameter" );
            for ( int i = 0; i < nodes.size() && !hasParameters; i++ ) {
              Element elem = (Element) nodes.get( i );
              if ( elem.attributeValue( "name" ).equalsIgnoreCase( "output-target" )
                  && elem.attributeValue( "is-mandatory" ).equalsIgnoreCase( "true" ) ) {
                hasParameters = true;
                continue;
              }
              Element attrib =
                  (Element) elem.selectSingleNode( "attribute[@namespace='http://reporting.pentaho"
                      + ".org/namespaces/engine/parameter-attributes/core' and @name='role']" );
              if ( attrib == null || !"system".equals( attrib.attributeValue( "value" ) ) ) {
                hasParameters = true;
              }
            }
          }
        }
      } catch ( Exception e ) {
        logger.error( getMessagesInstance().getString( "FileResource.PARAM_FAILURE", e.getMessage() ), e );
      }
    }
    return Boolean.toString( hasParameters );
  }

  /**
   * <p>Download a file or folder from the repository.</p>
   *
   *   <p>Example Request:<br>
   *     GET api/repo/files/pathToFile/download?withManifest=true
   *  </p>
   *
   * Download the selected file or folder from the repository. In order to download file from the repository, the user needs to
   * have Publish action.  How the file comes down to the user and where it is saved is system and browser dependent.
   *
   * @param pathId          colon separated path for the repository file
   *                        <pre function="syntax.xml">
   *                        :Path:to:file:id
   *                        </pre>
   * @param strWithManifest true or false (download file with manifest).  Defaults to true (include manifest) if this string can't
   *                        be directly parsed to 'false' (case sensitive).  This argument is only used if a directory is being
   *                        downloaded.
   * @param userAgent       A string representing the type of browser to use.  Currently only applicable if contains 'FireFox' as FireFox
   *                        requires a header with encoding information (UTF-8) and a quoted filename, otherwise encoding information is not
   *                        supplied and the filename is not quoted.
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @GET
  @Path( "{pathId : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successful download." ),
      @ResponseCode( code = 400, condition = "Usually a bad pathId." ),
      @ResponseCode( code = 403, condition = "pathId points at a file the user doesn't have access to."),
      @ResponseCode( code = 404, condition = "File not found."),
      @ResponseCode( code = 500, condition = "Failed to download file for another reason.")

})
  // have to accept anything for browsers to work
  public Response doGetFileOrDirAsDownload( @HeaderParam( "user-agent" ) String userAgent,
      @PathParam( "pathId" ) String pathId, @QueryParam( "withManifest" ) String strWithManifest ) {
    FileService.DownloadFileWrapper wrapper = null;
    try {
      wrapper = fileService.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
      return buildZipOkResponse( wrapper );
    } catch ( InvalidParameterException e ) {
      logger.error( getMessagesInstance().getString( "FileResource.EXPORT_FAILED", e.getMessage() ), e );
      return buildStatusResponse( BAD_REQUEST );
    } catch ( IllegalSelectorException e ) {
      logger.error( getMessagesInstance().getString("FileResource.EXPORT_FAILED", e.getMessage() ), e );
      return buildStatusResponse( FORBIDDEN );
    } catch ( GeneralSecurityException e ) {
      logger.error( getMessagesInstance().getString("FileResource.EXPORT_FAILED", e.getMessage() ), e );
      return buildStatusResponse( FORBIDDEN );
    } catch ( FileNotFoundException e ) {
      logger.error( getMessagesInstance().getString("FileResource.EXPORT_FAILED", e.getMessage() ), e );
      return buildStatusResponse( NOT_FOUND );
    } catch ( Throwable e ) {
      logger.error( getMessagesInstance().getString( "FileResource.EXPORT_FAILED", pathId + " " + e.getMessage() ), e );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * <p>Retrieves the file from the repository as inline.
   * </p>
   * Retrieves the file from the repository as inline. This is mainly used for css and dependent files for the html
   * document.
   *
   *  <p>Example Request:<br>
   *     GET api/repo/files/pathToFile/inline
   *  </p>
   *
   * @param pathId Colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @GET
  @Path( "{pathId : .+}/inline" )
  @Produces( WILDCARD )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully retrieved file." ),
      @ResponseCode( code = 403, condition = "Failed to retrieve file due to permission problem."),
      @ResponseCode( code = 404, condition = "Failed to retrieve file due because file was not found."),
      @ResponseCode( code = 500, condition = "Failed to download file because of some other error.")
  })
  public Response doGetFileAsInline( @PathParam( "pathId" ) String pathId ) {
    try {
      FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileAsInline( pathId );
      return buildOkResponse( wrapper );
    } catch ( IllegalArgumentException e ) {
      logger.error( getMessagesInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return buildStatusResponse( FORBIDDEN );
    } catch ( FileNotFoundException e ) {
      logger.error( getMessagesInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return buildStatusResponse( NOT_FOUND );
    } catch ( InternalError e ) {
      logger.error( getMessagesInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * <p>Save the acls of the selected file to the repository</p>
   *
   * <p>This method is used to update and save the acls of the selected file to the repository</p>
   *
   * <p> Example Request:<br>
   *     PUT api/repo/files/pathToFile/acl
   * </p>
   *
   * @param pathId Colon separated path for the repository file <br/>
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param acl    Acl of the repository file <code> RepositoryFileAclDto </code>
   *               <pre function="syntax.xml">
   *                 &lt;repositoryFileAclDto&gt;
   *                   &lt;entriesInheriting&gt;true&lt;/entriesInheriting&gt;
   *                   &lt;id&gt;068390ba-f90d-46e3-8c55-bbe55e24b2fe&lt;/id&gt;
   *                   &lt;owner&gt;admin&lt;/owner&gt;
   *                   &lt;ownerType&gt;0&lt;/ownerType&gt;
   *                 &lt;/repositoryFileAclDto&gt;
   *               </pre>
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   *
   */
  @PUT
  @Path( "{pathId : .+}/acl" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully saved file." ),
      @ResponseCode( code = 403, condition = "Failed to save acls due to missing or incorrect properties."),
      @ResponseCode( code = 400, condition = "Failed to save acls due to malformed xml."),
      @ResponseCode( code = 500, condition = "Failed to save acls due to another error.")
  })

  public Response setFileAcls( @PathParam( "pathId" ) String pathId, RepositoryFileAclDto acl ) {
    try {
      fileService.setFileAcls( pathId, acl );
      return buildOkResponse();
    } catch ( Exception exception ) {
      logger.error( getMessagesInstance().getString( "SystemResource.GENERAL_ERROR" ), exception );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * <p>Store content creator.</p>
   *
   * Store content creator of the selected repository file.
   *
   * @param pathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param contentCreator repository file
   * <pre function="syntax.xml">
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   * </pre>
   *
   * @return A jax-rs Response object with the appropriate status code, header, and body.
   */
  @PUT
  @Path( "{pathId : .+}/creator" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes({
      @ResponseCode( code = 200, condition = "Successfully retrieved file." ),
      @ResponseCode( code = 500, condition = "Failed to download file because of some other error.")
  })

  public Response doSetContentCreator( @PathParam( "pathId" ) String pathId, RepositoryFileDto contentCreator ) {
    try {
      fileService.doSetContentCreator( pathId, contentCreator );
      return buildOkResponse();
    } catch ( FileNotFoundException e ) {
      logger.error( getMessagesInstance().getErrorString( "FileResource.FILE_NOT_FOUND", pathId ), e );
      return buildStatusResponse( NOT_FOUND );
    } catch ( Throwable t ) {
      logger.error( getMessagesInstance().getString( "SystemResource.GENERAL_ERROR" ), t );
      return buildStatusResponse( INTERNAL_SERVER_ERROR );
    }
  }

  /**
   * Retrieves the list of locale map for the selected repository file. The list will be empty if a problem occurs.
   *
   * @param pathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @return <code>List<LocaleMapDto></code> the list of locales
   *         <pre function="syntax.xml">
   *           <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *           &lt;localePropertiesMapEntries&gt;
   *             &lt;localeMapDto&gt;
   *               &lt;locale&gt;default&lt;/locale&gt;
   *               &lt;properties&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;file.title&lt;/key&gt;
   *                   &lt;value&gt;myFile&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *                   &lt;value&gt;nt:unstructured&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;title&lt;/key&gt;
   *                   &lt;value&gt;myFile&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *                 &lt;stringKeyStringValueDto&gt;
   *                   &lt;key&gt;file.description&lt;/key&gt;
   *                   &lt;value&gt;myFile Description&lt;/value&gt;
   *                 &lt;/stringKeyStringValueDto&gt;
   *               &lt;/properties&gt;
   *             &lt;/localeMapDto&gt;
   *           &lt;/localePropertiesMapEntries&gt;
   *         </pre>
   */
  @GET
  @Path( "{pathId : .+}/locales" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<LocaleMapDto> doGetFileLocales( @PathParam( "pathId" ) String pathId ) {
    List<LocaleMapDto> locales = new ArrayList<LocaleMapDto>();
    try {
      locales = fileService.doGetFileLocales( pathId );
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.FILE_NOT_FOUND", pathId ), e );
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), t );
    }
    return locales;
  }

  /**
   * Retrieve the list of locale properties for a given locale
   *
   * @param pathId Colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param locale The specified locale
   * @return <code>&lt;stringKeyStringValueDtoes&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.title&lt;/key&gt;
   * &lt;value&gt;File Title&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;jcr:primaryType&lt;/key&gt;
   * &lt;value&gt;nt:unstructured&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;title&lt;/key&gt;
   * &lt;value&gt;File Title&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;/stringKeyStringValueDtoes&gt;</code>
   */
  @GET
  @Path( "{pathId : .+}/localeProperties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<StringKeyStringValueDto> doGetLocaleProperties( @PathParam( "pathId" ) String pathId,
      @QueryParam( "locale" ) String locale ) {
    return fileService.doGetLocaleProperties( pathId, locale );
  }

  /**
   * Save list of locale properties for a given locale
   *
   * @param pathId Colon separated path for the repository file
   * @param locale The specified locale
   * @param properties
   * <code>&lt;stringKeyStringValueDtoes&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;file.title&lt;/key&gt;
   * &lt;value&gt;File Title&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;jcr:primaryType&lt;/key&gt;
   * &lt;value&gt;nt:unstructured&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;stringKeyStringValueDto&gt;
   * &lt;key&gt;title&lt;/key&gt;
   * &lt;value&gt;File Title&lt;/value&gt;
   * &lt;/stringKeyStringValueDto&gt;
   * &lt;/stringKeyStringValueDtoes&gt;</code>
   * @return Response object indicating the success or failure of this operation
   */
  @PUT
  @Path( "{pathId : .+}/localeProperties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doSetLocaleProperties( @PathParam( "pathId" ) String pathId, @QueryParam( "locale" ) String locale,
      List<StringKeyStringValueDto> properties ) {
    try {
      fileService.doSetLocaleProperties( pathId, locale, properties );
      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Delete the locale for the selected file and locale
   *
   * @param pathId Colon separated path for the repository file
   * <pre function="syntax.xml">
   *   path:to:file:id
   * </pre>
   *     
   * @param locale The locale to be deleted
   * <pre function="syntax.xml">
   *   en_US
   * </pre>    
   * @return Server Response indicating the success of the operation
   */
  @PUT
  @Path( "{pathId : .+}/deleteLocale" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doDeleteLocale( @PathParam( "pathId" ) String pathId, @QueryParam( "locale" ) String locale ) {
    try {
      fileService.doDeleteLocale( pathId, locale );
      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Retrieves the properties of the root directory
   *
   *<p>Example Request:<br>
   *               GET api/repo/files/properties<br>
   *               <p/>
   *
   * <p>Example Response:<br/>
   *               HTTP/1.1 200 OK
   *               </p>
   *               <pre function="syntax.xml">
   *               &lt;repositoryFileDto&gt;
   *                 &lt;createdDate&gt;1406731649407&lt;/createdDate&gt;
   *                 &lt;fileSize&gt;-1&lt;/fileSize&gt;
   *                 &lt;folder&gt;true&lt;/folder&gt;
   *                 &lt;hidden&gt;false&lt;/hidden&gt;
   *                 &lt;id&gt;6d93372c-4908-47af-9815-3aa6307e392c&lt;/id&gt;
   *                 &lt;locale&gt;en&lt;/locale&gt;
   *                 &lt;locked&gt;false&lt;/locked&gt;
   *                 &lt;name/&gt;
   *                 &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *                 &lt;path&gt;/&lt;/path&gt;
   *                 &lt;title/&gt;
   *                 &lt;versioned&gt;false&lt;/versioned&gt;
   *               &lt;/repositoryFileDto&gt;
   *               </pre>
   *
   * @return file properties object <code> RepositoryFileDto </code> for the root directory
   */
  @GET
  @Path( "/properties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetRootProperties() {
    return fileService.doGetRootProperties();
  }

  /**
   * Checks whether the current user has permissions to the selected files
   *
   * @param pathId Colon separated path for the repository file
   * <pre function="syntax.xml">
   *   path:to:file:id
   * </pre>
   *     
   * @param permissions Pipe separated permissions to be checked
   * <pre function="syntax.xml">
   *   permission1|permission2|permission3
   * </pre>    
   * @return List of permissions for the selected files
   */
  @GET
  @Path( "{pathId : .+}/canAccessMap" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<Setting> doGetCanAccessList( @PathParam( "pathId" ) String pathId,
      @QueryParam( "permissions" ) String permissions ) {
    return fileService.doGetCanAccessList( pathId, permissions );
  }

  /**
   * Checks whether the current user has permissions to the provided list of paths
   *
   * @param pathsWrapper Collection of paths to be checked
   * <pre function="syntax.xml">
   *   pathToFileId1
   *   pathToFileId2
   *   pathToFileId3
   * </pre>
   * @return A collection of the permission settings for the paths
   */
  @POST
  @Path( "/pathsAccessList" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<Setting> doGetPathsAccessList( StringListWrapper pathsWrapper ) {
    return fileService.doGetPathsAccessList( pathsWrapper );
  }

  /**
   * Check whether the current user has specific permission on the selected repository file
   *
   * @param pathId      Colon separated path for the repository file
   *                    <pre function="syntax.xml">
   *                    :path:to:file:id
   *                    </pre>
   * @param permissions Pipe separated list of permissions
   *                    <pre function="syntax.xml">
   *                    0|1|2|3|4
   *                    </pre>
   * @return "true" or "false"
   */
  @GET
  @Path( "{pathId : .+}/canAccess" )
  @Produces( TEXT_PLAIN )
  public String doGetCanAccess( @PathParam( "pathId" ) String pathId,
      @QueryParam( "permissions" ) String permissions ) {
    return fileService.doGetCanAccess( pathId, permissions );
  }

  /**
   * Checks whether the current user can administer the platform
   *
   * @return String <code>"true"</code> if the user can administer the platform, or <code>"false"</code> otherwise
   */
  @GET
  @Path( "/canAdminister" )
  @Produces( TEXT_PLAIN )
  public String doGetCanAdminister() {
    try {
      return fileService.doCanAdminister() ? "true" : "false";
    } catch ( Exception e ) {
      return "false";
    }
  }

  /**
   * Returns the repository reserved characters
   *
   * @return list of characters
   */
  @GET
  @Path( "/reservedCharacters" )
  @Produces( { TEXT_PLAIN } )
  public Response doGetReservedChars() {
    StringBuffer buffer = fileService.doGetReservedChars();
    return Response.ok( buffer.toString(), MediaType.TEXT_PLAIN ).build();
  }

  /**
   * Returns the repository reserved characters
   *
   * @return List of characters
   */
  @GET
  @Path( "/reservedCharactersDisplay" )
  @Produces( { TEXT_PLAIN } )
  public Response doGetReservedCharactersDisplay() {
    StringBuffer buffer = fileService.doGetReservedCharactersDisplay();
    return Response.ok( buffer.toString(), MediaType.TEXT_PLAIN ).build();
  }

  /**
   * Checks whether the current user can create content in the repository
   *
   * @return "true" or "false"
   */
  @GET
  @Path( "/canCreate" )
  @Produces( TEXT_PLAIN )
  public String doGetCanCreate() {
    return fileService.doGetCanCreate();
  }

  /**
   * Retrieves the acls of the selected repository file
   *
   * @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * <p>Example Request:
   *               <br>
   *               GET /pentaho/api/repo/files/%3Ahome%3Aadmin%3Aafile.prpti/acl
   *               <p/>
   * <p></p>Example Response:
   *               <br/>
   *               HTTP/1.1 200 OK
   *               Content-Type: application/xml
   *               <p/>
   *               <pre function="syntax.xml">
   *               {
   *                 &lt;repositoryFileAclDto&gt;
   *                   &lt;aces&gt;
   *                     &lt;modifiable&gt;true&lt;/modifiable&gt;
   *                     &lt;permissions&gt;4&lt;/permissions&gt;
   *                     &lt;recipient&gt;admin&lt;/recipient&gt;
   *                     &lt;recipientType&gt;0&lt;/recipientType&gt;
   *                   &lt;/aces&gt;
   *                   &lt;aces&gt;
   *                     &lt;modifiable&gt;false&lt;/modifiable&gt;
   *                     &lt;permissions&gt;4&lt;/permissions&gt;
   *                     &lt;recipient&gt;Administrator&lt;/recipient&gt;
   *                     &lt;recipientType&gt;1&lt;/recipientType&gt;
   *                   &lt;/aces&gt;
   *                   &lt;entriesInheriting&gt;true&lt;/entriesInheriting&gt;
   *                   &lt;id&gt;068390ba-f90d-46e3-8c55-bbe55e24b2fe&lt;/id&gt;
   *                   &lt;owner&gt;admin&lt;/owner&gt;
   *                   &lt;ownerType&gt;0&lt;/ownerType&gt;
   *                   &lt;/repositoryFileAclDto&gt;
   *                }
   *                </pre>
   * @return <code> RepositoryFileAclDto </code>
   */
  @GET
  @Path( "{pathId : .+}/acl" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileAclDto doGetFileAcl( @PathParam( "pathId" ) String pathId ) {
    return fileService.doGetFileAcl( pathId );
  }

  /**
   * Retrieves the properties of a selected repository file
   *
   * @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * <p>Example Request:
   *               <br>
   *               GET api/repo/files/%3Ahome%3Aadmin%3Aafile.prpti/properties
   *               <p/>
   * <p></p>Example Response:
   *               <br/>
   *               HTTP/1.1 200 OK
   *               Content-Type: application/json
   *               <p/>
   *               <pre function="syntax.xml">
   *               {
   *                 "createdDate":"1406732545857",
   *                 "description":"afile.prpti",
   *                 "fileSize":"7672",
   *                 "folder":"false",
   *                 "hidden":"false",
   *                 "id":"068390ba-f90d-46e3-8c55-bbe55e24b2fe",
   *                 "lastModifiedDate":"1406732545858",
   *                 "locale":"en",
   *                 "localePropertiesMapEntries":[{"locale":"default",
   *                 "properties":[{"key":"file.title","value":"afile"},
   *                 {"key":"description","value":"afile.prpti"},
   *                 {"key":"jcr:primaryType","value":"nt:unstructured"},
   *                 {"key":"title","value":"afile"},
   *                 {"key":"file.description","value":"afile.prpti"}]}],
   *                 "locked":"false",
   *                 "name":"afile.prpti",
   *                 "ownerType":"-1","path":"/home/admin/afile.prpti",
   *                 "title":"afile","versioned":"false"
   *                }
   *                </pre>
   *
   * @return file properties object <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/properties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetProperties( @PathParam( "pathId" ) String pathId ) {
    try {
      return fileService.doGetProperties( pathId );
    } catch ( FileNotFoundException fileNotFound ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), fileNotFound );
      //TODO: What do we return in this error case?
      return null;
    }
  }

  /**
   * Retrieves the file by creator id
   *
   * @param pathId Colon separated path for the destination for files to be copied
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return file properties object <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/creator" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetContentCreator( @PathParam( "pathId" ) String pathId ) {
    try {
      return fileService.doGetContentCreator( pathId );
    } catch ( Throwable t ) {
      return null;
    }
  }

  /**
   * Retrieve the list of executed contents for a selected content from the repository
   *
   * @param pathId the path for the file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @return list of <code> repositoryFileDto </code>
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *    &lt;List&gt;
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   *  &lt;/List&gt;
   * </pre>
   */
  @GET
  @Path( "{pathId : .+}/generatedContent" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContent( @PathParam( "pathId" ) String pathId ) {
    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    try {
      repositoryFileDtoList = fileService.doGetGeneratedContent( pathId );
    } catch ( FileNotFoundException e ) {
      //return the empty list
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "FileResource.GENERATED_CONTENT_FAILED", pathId ), t );
    }
    return repositoryFileDtoList;
  }

  /**
   * Retrieve the executed contents for a selected repository file and a given user
   *
   * @param pathId the path for the file <pre function="syntax.xml"> :path:to:file:id </pre>
   * @param user   the username for the generated content folder
   * @return list of <code> repositoryFileDto </code>
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *    &lt;List&gt;
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   *  &lt;/List&gt;
   * </pre>
   */
  @GET
  @Path( "{pathId : .+}/generatedContentForUser" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContentForUser( @PathParam( "pathId" ) String pathId,
                                                               @QueryParam( "user" ) String user ) {
    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    try {
      repositoryFileDtoList = fileService.doGetGeneratedContent( pathId, user );
    } catch ( FileNotFoundException e ) {
      //return the empty list
    } catch ( Throwable t ) {
      logger
        .error( Messages.getInstance().getString( "FileResource.GENERATED_CONTENT_FOR_USER_FAILED", pathId, user ), t );
    }
    return repositoryFileDtoList;
  }

  /**
   * Retrieve the list of execute content by lineage id.
   *
   * @param lineageId the path for the file
   * <pre function="syntax.xml">
   *  :path:to:file:id
   * </pre>
   * @return list of <code> repositoryFileDto </code>
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
   *    &lt;List&gt;
   *     &lt;repositoryFileDto&gt;
   *     &lt;createdDate&gt;1402911997019&lt;/createdDate&gt;
   *     &lt;fileSize&gt;3461&lt;/fileSize&gt;
   *     &lt;folder&gt;false&lt;/folder&gt;
   *     &lt;hidden&gt;false&lt;/hidden&gt;
   *     &lt;id&gt;ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/id&gt;
   *     &lt;lastModifiedDate&gt;1406647160536&lt;/lastModifiedDate&gt;
   *     &lt;locale&gt;en&lt;/locale&gt;
   *     &lt;localePropertiesMapEntries&gt;
   *       &lt;localeMapDto&gt;
   *         &lt;locale&gt;default&lt;/locale&gt;
   *         &lt;properties&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;jcr:primaryType&lt;/key&gt;
   *             &lt;value&gt;nt:unstructured&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;title&lt;/key&gt;
   *             &lt;value&gt;myFile&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *           &lt;stringKeyStringValueDto&gt;
   *             &lt;key&gt;file.description&lt;/key&gt;
   *             &lt;value&gt;myFile Description&lt;/value&gt;
   *           &lt;/stringKeyStringValueDto&gt;
   *         &lt;/properties&gt;
   *       &lt;/localeMapDto&gt;
   *     &lt;/localePropertiesMapEntries&gt;
   *     &lt;locked&gt;false&lt;/locked&gt;
   *     &lt;name&gt;myFile.prpt&lt;/name&gt;&lt;/name&gt;
   *     &lt;originalParentFolderPath&gt;/public/admin&lt;/originalParentFolderPath&gt;
   *     &lt;ownerType&gt;-1&lt;/ownerType&gt;
   *     &lt;path&gt;/public/admin/ff11ac89-7eda-4c03-aab1-e27f9048fd38&lt;/path&gt;
   *     &lt;title&gt;myFile&lt;/title&gt;
   *     &lt;versionId&gt;1.9&lt;/versionId&gt;
   *     &lt;versioned&gt;true&lt;/versioned&gt;
   *   &lt;/repositoryFileAclDto&gt;
   *  &lt;/List&gt;
   * </pre>
   */
  @GET
  @Path( "/generatedContentForSchedule" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( @QueryParam( "lineageId" ) String lineageId ) {
    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    try {
      repositoryFileDtoList = fileService.doGetGeneratedContentForSchedule( lineageId );
    } catch ( FileNotFoundException e ) {
      //return the empty list
    } catch ( Throwable t ) {
      logger
        .error( Messages.getInstance().getString( "FileResource.GENERATED_CONTENT_FOR_USER_FAILED", lineageId ), t );
    }
    return repositoryFileDtoList;
  }

  /**
   * Retrieve the list of files from root of the repository.
   *
   * @param depth       (how many level should the search go)
   *                    <pre function="syntax.xml">
   *                      1
   *                    </pre>
   *
   * @param filter      (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *                    Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *                    <p/>
   *                    File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *                    accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *                    whether to return files, folders, or both files and folders, respectively.
   *                    <p/>
   *                    The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *                    name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *                    filter does not apply to root node.
   *                    <p/>
   *                    The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *                    metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *                    comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *                    be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *                    Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *                    excludeMembers= clause in the same service call.
   *                    <p/>
   *                    Example:
   *                    http://localhost:8080/pentaho/api/repo/files/:public:Steel%20Wheels/tree?showHidden=false&filter=*|FILES
   *                    |includeMembers=name,fileSize,description,folder,id,title
   *                    <p/>
   *                    will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *                    include the name, filesize, description, id and title.
   *                    <pre function="syntax.xml">
   *                      *|FOLDERS
   *                    </pre>
   *
   * @param showHidden  (include or exclude hidden files from the file list)
   *                    <pre function="syntax.xml">
   *                      true
   *                    </pre>
   *
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetRootTree( @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return fileService.doGetTree( FileUtils.PATH_SEPARATOR, depth, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve a recursive list of files from the root of the repository
   *
   * @param filter      (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *                    Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *                    <p/>
   *                    File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *                    accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *                    whether to return files, folders, or both files and folders, respectively.
   *                    <p/>
   *                    The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *                    name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *                    filter does not apply to root node.
   *                    <p/>
   *                    The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *                    metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *                    comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *                    be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *                    Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *                    excludeMembers= clause in the same service call.
   *                    <p/>
   *                    Example:
   *                    <p/>
   *                    http://localhost:8080/pentaho/api/repo/files/children?showHidden=false&filter=*|
   *                    FILES |includeMembers=name,fileSize,description,folder,id,title
   *                    <p/>
   *                    will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *                    include the name, filesize, description, id and title.
   *                    <pre function="syntax.xml">
   *                      *|FILES |includeMembers=name,fileSize,description,folder,id,title
   *                    </pre>
   * @param showHidden  (include or exclude hidden files from the file list)
   *                    <pre function="syntax.xml">
   *                      true
   *                    </pre>
   * @param includeAcls (Include permission information about the file in the output)
   *                    <pre function="syntax.xml">
   *                      false
   *                    </pre>
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetRootChildren( @QueryParam( "filter" ) String filter,
      @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return fileService.doGetChildren(  FileUtils.PATH_SEPARATOR, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the children of the selected repository file. This is a recursive search with a selected level depth and
   * filter
   *
   * @param pathId      The path from the root folder to the root node of the tree to return using colon characters in place of /
   *                    or \ characters. To specify /public/Steel Wheels, the encoded pathId would be :public:Steel%20Wheels
   *                    <pre function="syntax.xml">
   *                      :path:to:id
   *                    </pre>
   *
   * @param depth       (how many level should the search go)
   *                    <pre function="syntax.xml">
   *                      1
   *                    </pre>
   *
   * @param filter      (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *                    Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *                    <p/>
   *                    File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *                    accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *                    whether to return files, folders, or both files and folders, respectively.
   *                    <p/>
   *                    The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *                    name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *                    filter does not apply to root node.
   *                    <p/>
   *                    The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *                    metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *                    comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *                    be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *                    Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *                    excludeMembers= clause in the same service call.
   *                    <p/>
   *                    Example:
   *                    http://localhost:8080/pentaho/api/repo/files/:public:Steel%20Wheels/tree?showHidden=false&filter=*|FILES
   *                    |includeMembers=name,fileSize,description,folder,id,title
   *                    <p/>
   *                    will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *                    include the name, filesize, description, id and title.
   *                    <pre function="syntax.xml">
   *                      *|FOLDERS
   *                    </pre>
   *
   * @param showHidden  (include or exclude hidden files from the file list)
   *                    <pre function="syntax.xml">
   *                      true
   *                    </pre>
   *
   * @param includeAcls (Include permission information about the file in the output)
   *                    <pre function="syntax.xml">
   *                      true
   *                    </pre>
   *
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetTree( @PathParam( "pathId" ) String pathId, @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return fileService.doGetTree( pathId, depth, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the children of the selected repository file. This is a recursive search with a selected level depth and
   * filter
   *
   * @param pathId      The path from the root folder to the root node of the tree to return using colon characters in place of /
   *                    or \ characters. To specify /public/Steel Wheels, the encoded pathId would be 
   *                    <pre function="syntax.xml">
   *                      :public:Steel%20Wheels
   *                    </pre>
   * @param filter      (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *                    Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *                    <p/>
   *                    File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *                    accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *                    whether to return files, folders, or both files and folders, respectively.
   *                    <p/>
   *                    The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *                    name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *                    filter does not apply to root node.
   *                    <p/>
   *                    The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *                    metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *                    comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *                    be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *                    Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *                    excludeMembers= clause in the same service call.
   *                    <p/>
   *                    Example:
   *                    <p/>
   *                    http://localhost:8080/pentaho/api/repo/files/:public:Steel%20Wheels/children?showHidden=false&filter=*|
   *                    FILES |includeMembers=name,fileSize,description,folder,id,title
   *                    <p/>
   *                    will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *                    include the name, filesize, description, id and title.
   *                    <pre function="syntax.xml">
   *                      *|FILES |includeMembers=name,fileSize,description,folder,id,title
   *                    </pre>
   * @param showHidden  (include or exclude hidden files from the file list)
   *                    <pre function="syntax.xml">
   *                      true
   *                    </pre>
   * @param includeAcls (Include permission information about the file in the output)
   *                    <pre function="syntax.xml">
   *                      false
   *                    </pre>
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetChildren( @PathParam( "pathId" ) String pathId,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {

    return fileService.doGetChildren( pathId, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the list of files in the user's trash folder
   *
   * @return list of <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "/deleted" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetDeletedFiles() {
    return fileService.doGetDeletedFiles();
  }

  /**
   * Retrieve the metadata of the selected repository file.
   *
   * @param pathId Colon separated path for the destination for files to be copied
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return list of <code> StringKeyStringValueDto </code>
   */
  @GET
  @Path( "{pathId : .+}/metadata" )
  @Produces( { APPLICATION_JSON } )
  public List<StringKeyStringValueDto> doGetMetadata( @PathParam( "pathId" ) String pathId ) {
    try {
      return fileService.doGetMetadata( pathId );
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.FILE_UNKNOWN", pathId ), e );
      return null;
    }
  }

  /**
   * Rename the name of the selected file
   *
   * @param pathId Colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param newName String indicating the new name of the file
   *
   * <p>Example Request:
   * <br>
   * PUT /pentaho/api/repo/files/PATH/rename
   * <p/>
   * <p></p>Example Response:
   * <br/>
   * HTTP/1.1 200 OK
   * Content-Type: text/plain
   * <p/>
   *
   * @return Response with 200 OK, if the file does not exist to be renamed the response will return 200 OK with the string "File to be renamed does not exist"
   *
   */
  @PUT
  @Path( "{pathId : .+}/rename" )
  @Consumes( { WILDCARD } )
  @Produces( { WILDCARD } )
  public Response doRename( @PathParam( "pathId" ) String pathId, @QueryParam( "newName" ) String newName ) {
    try {
      boolean success = fileService.doRename( pathId, newName );
      if ( success ) {
        return Response.ok().build();
      } else {
        return Response.ok( "File to be renamed does not exist" ).build();
      }
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Store the metadata of the selected fle. Even though the hidden flag is a property of the file node itself, and not
   * the metadata child, it is considered metadata from PUC and is included in the setMetadata call
   *
   * @param pathId Colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param metadata A list of <code> StringKeyStringValueDto </code>
   * @return Server Response indicating the success of the operation
   */
  @PUT
  @Path( "{pathId : .+}/metadata" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doSetMetadata( @PathParam( "pathId" ) String pathId, List<StringKeyStringValueDto> metadata ) {
    try {
      fileService.doSetMetadata( pathId, metadata );
      return Response.ok().build();
    } catch ( GeneralSecurityException e ) {
      return Response.status( Response.Status.UNAUTHORIZED ).build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  protected boolean isPathValid( String path ) {
    return fileService.isPathValid( path );
  }

  public RepositoryDownloadWhitelist getWhitelist() {
    if ( whitelist == null ) {
      whitelist = new RepositoryDownloadWhitelist();
    }
    return whitelist;
  }

  public void setWhitelist( RepositoryDownloadWhitelist whitelist ) {
    this.whitelist = whitelist;
  }

  public static IAuthorizationPolicy getPolicy() {
    if ( policy == null ) {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    }
    return policy;
  }

  public static IUnifiedRepository getRepository() {
    if ( repository == null ) {
      repository = PentahoSystem.get( IUnifiedRepository.class );
    }
    return repository;
  }

  public static DefaultUnifiedRepositoryWebService getRepoWs() {
    if ( repoWs == null ) {
      repoWs = new DefaultUnifiedRepositoryWebService();
    }
    return repoWs;
  }

  public void setConverterHandler( IRepositoryContentConverterHandler converterHandler ) {
    this.converterHandler = converterHandler;
  }

  public void setMimeResolver( NameBaseMimeResolver mimeResolver ) {
    this.mimeResolver = mimeResolver;
  }

  protected Response buildOkResponse() {
    return Response.ok().build();
  }

  protected Response buildStatusResponse( Response.Status status ) {
    return Response.status( status ).build();
  }

  protected Response buildServerErrorResponse( Throwable t ) {
    return buildServerErrorResponse( t.getMessage() );
  }

  protected Response buildServerErrorResponse( String msg ) {
    return Response.serverError().entity( msg ).build();
  }

  protected Response buildSafeHtmlServerErrorResponse( Exception e ) {
    return Response.serverError()
      .entity( new SafeHtmlBuilder().appendEscapedLines( e.getLocalizedMessage() ).toSafeHtml().asString() ).build();
  }

  protected Response buildOkResponse( FileService.RepositoryFileToStreamWrapper wrapper ) {
    Response.ResponseBuilder builder = Response.ok( wrapper.getOutputStream() );

    if ( wrapper.getMimetype() != null ) {
      builder = Response.ok( wrapper.getOutputStream(), wrapper.getMimetype() );
    }

    return builder.header( "Content-Disposition", "inline; filename=\"" + wrapper.getRepositoryFile().getName() + "\"" )
      .build();
  }

  protected Response buildZipOkResponse( FileService.DownloadFileWrapper wrapper ) {
    return Response.ok( wrapper.getOutputStream(), APPLICATION_ZIP + "; charset=UTF-8" )
      .header( "Content-Disposition", wrapper.getAttachment() ).build();
  }

  protected Response buildOkResponse( Object o, String s ) {
    return Response.ok( o, s ).build();
  }

  protected Exporter getExporter() {
    return new Exporter( repository );
  }

  protected FileInputStream getFileInputStream( File file ) throws FileNotFoundException {
    return new FileInputStream( file );
  }

  protected StreamingOutput getStreamingOutput( final InputStream is ) {
    return new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
  }

  protected boolean hasParameterUi( RepositoryFile repositoryFile ) {
    return ( PentahoSystem.get( IPluginManager.class ).getContentGenerator(
      repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameterUi" )
      != null );
  }

  protected IContentGenerator getContentGenerator( RepositoryFile repositoryFile ) {
    return PentahoSystem.get( IPluginManager.class ).getContentGenerator(
      repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameter" );
  }

  protected SimpleParameterProvider getSimpleParameterProvider() {
    return new SimpleParameterProvider();
  }

  protected String encode( String s ) throws UnsupportedEncodingException {
    return URLEncoder.encode( s, "UTF-8" );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected ByteArrayOutputStream getByteArrayOutputStream() {
    return new ByteArrayOutputStream();
  }

  protected Document parseText( String text ) throws DocumentException {
    return DocumentHelper.parseText( text );
  }

  protected Messages getMessagesInstance() {
    return Messages.getInstance();
  }
}
