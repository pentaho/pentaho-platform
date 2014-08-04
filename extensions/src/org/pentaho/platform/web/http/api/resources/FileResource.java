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
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.file.InvalidPathException;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.pentaho.jmeter.annotation.JMeterTest;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.jcr.PentahoJcrConstants;
import org.pentaho.platform.repository2.unified.webservices.DefaultUnifiedRepositoryWebService;
import org.pentaho.platform.repository2.unified.webservices.LocaleMapDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.StringKeyStringValueDto;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.util.RepositoryPathEncoder;
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
  private static final Integer MODE_OVERWRITE = 1;

  private static final Integer MODE_RENAME = 2;

  private static final Integer MODE_NO_OVERWRITE = 3;

  public static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$

  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog( FileResource.class );

  private static FileService fileService;

  protected RepositoryDownloadWhitelist whitelist;

  protected static IUnifiedRepository repository;

  protected static DefaultUnifiedRepositoryWebService repoWs;

  protected static IAuthorizationPolicy policy;

  IRepositoryContentConverterHandler converterHandler;
  Map<String, Converter> converters;

  protected NameBaseMimeResolver mimeResolver;

  public FileResource() {
  }

  public FileResource( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
    fileService = new FileService();
  }

  public static String idToPath( String pathId ) {
    String path = null;
    // slashes in pathId are illegal.. we scrub them out so the file will not be found
    // if the pathId was given in slash separated format
    if ( pathId.contains( PATH_SEPARATOR ) ) {
      logger.warn( Messages.getInstance().getString( "FileResource.ILLEGAL_PATHID", pathId ) ); //$NON-NLS-1$
    }
    path = pathId.replaceAll( PATH_SEPARATOR, "" ); //$NON-NLS-1$
    path = RepositoryPathEncoder.decodeRepositoryPath( path );
    if ( !path.startsWith( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR + path;
    }
    return path;
  }

  /**
   * Moves the list of files to the user's trash folder
   * <p/>
   * Move a list of files to the user's trash folder, the list should be comma separated.
   *
   * @param params Comma separated list of the files to be deleted
   *               <pre function="syntax.xml">
   *               a06ef783-203d-4b76-bb9a-2fa15b0904bd,de2b05b8-38f1-4e90-9660-b172f85a7e48,356f21dd-96f2-44d5-9321-fedde0f429dd
   *               </pre>
   * @return Server Response indicating the success of the operation
   */
  @PUT
  @Path( "/delete" )
  @Consumes( { WILDCARD } )
  public Response doDeleteFiles( String params ) {
    try {
      fileService.doDeleteFiles( params );
      return Response.ok().build();

    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
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
  @JMeterTest( url = "/repo/files/deletePermanent", requestType = "PUT", statusCode = "200",
      postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22,ff11ac89-7eda-4c03-aab1-e27f9048fd38" )
  public Response doDeleteFilesPermanent( String params ) {
    try {
      fileService.doDeleteFilesPermanent( params );
      return Response.ok().build();
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Moves a list of files from its current location to another.
   * <p/>
   * Moves a list of files from its current location to another, the list should be comma separated.
   *
   * @param destPathId colon separated path for the destiny path
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param params comma separated list of files to be moved
   * <pre function="syntax.xml">
   *    pathId1,pathId2,...
   * </pre>
   * @return Response Server Response indicating the success of the operation
   */
  @PUT
  @Path( "{pathId : .+}/move" )
  @Consumes( { WILDCARD } )
  @JMeterTest( url = "/repo/files/{pathId : .+}/move", requestType = "PUT", statusCode = "200",
      postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22,ff11ac89-7eda-4c03-aab1-e27f9048fd38" )
  public Response doMove( @PathParam( "pathId" ) String destPathId, String params ) {
    try {
      fileService.doMoveFiles( destPathId, params );
      return Response.ok().build();
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.DESTINY_PATH_UNKNOWN", destPathId ), e );
      return Response.status( NOT_FOUND ).build();
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), t );
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  /**
   * Restores a list of files from the user's trash folder
   * <p/>
   * Restores a list of files from the user's trash folder to their previous locations. The list should be comma
   * separated.
   *
   * @param params comma separated list of file ids to be restored
   * <pre function="syntax.xml">
   *    pathId1,pathId2,...
   * </pre>
   * @return Response Server Response indicating the success of the operation
   */
  @PUT
  @Path( "/restore" )
  @Consumes( { WILDCARD } )
  @JMeterTest( url = "/repo/files/restore", requestType = "PUT", statusCode = "200",
      postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22,ff11ac89-7eda-4c03-aab1-e27f9048fd38" )
  public Response doRestore( String params ) {
    try {
      fileService.doRestoreFiles( params );
      return Response.ok().build();
    } catch ( InternalError e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  /**
   * Create a new file
   * <p/>
   * Creates a new file with the provided contents at a given path
   *
   * @param pathId       Colon separated path for the repository file
   * @param fileContents An Input Stream with the contents of the file
   * @return Server Response indicating the success of the operation
   */
  @PUT
  @Path( "{pathId : .+}" )
  @Consumes( { WILDCARD } )
  @JMeterTest( url = "/repo/files/{pathId : .+}", requestType = "PUT" )
  public Response createFile( @PathParam( "pathId" ) String pathId, InputStream fileContents ) {
    try {
      fileService.createFile( httpServletRequest, pathId, fileContents );
      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Copy selected list of files to a new specified location
   *
   * @param pathId Colon separated path for the destination for files to be copied
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param mode   MODE_OVERWITE or MODE_NO_OVERWRITE
   * @param params Comma separated list of file ids to be copied
   *               <pre function="syntax.xml">
   *               fileId1,fileId2
   *               </pre>
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/children" )
  @Consumes( { TEXT_PLAIN } )
  @JMeterTest( url = "/repo/files/:home:admin/children", requestType = "PUT", statusCode = "200",
      postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22,ff11ac89-7eda-4c03-aab1-e27f9048fd38" )
  public Response doCopyFiles( @PathParam( "pathId" ) String pathId, @QueryParam( "mode" ) Integer mode,
      String params ) {
    try {
      fileService.doCopyFiles( pathId, mode, params );
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return Response.serverError().entity(
          new SafeHtmlBuilder().appendEscapedLines( e.getLocalizedMessage() ).toSafeHtml().asString() ).build();
    }

    return Response.ok().build();
  }

  /**
   * Takes a pathId and returns a response object with the output stream based on the file located at the pathID
   *
   * @param pathId @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return Response object containing the file stream for the file located at the pathId, along with the mimetype,
   * and file name.
   * @throws FileNotFoundException, IllegalArgumentException
   */
  @GET
  @Path( "{pathId : .+}" )
  @Produces( { WILDCARD } )
  public Response doGetFileOrDir( @PathParam( "pathId" ) String pathId ) {
    try {
      FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileOrDir( pathId );

      return Response.ok( wrapper.getOutputStream(), wrapper.getMimetype() ).header( "Content-Disposition",
          "inline; filename=\"" + wrapper.getRepositoryFile().getName() + "\"" ).build();

    } catch ( FileNotFoundException fileNotFound ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), fileNotFound );
      return Response.status( NOT_FOUND ).build();
    } catch ( IllegalArgumentException illegalArgument ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), illegalArgument );
      return Response.status( FORBIDDEN ).build();
    }
  }

  // Overloaded this method to try and minimize calls to the repo
  // Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
  // @GET
  // @Path("{pathId : .+}")
  // @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip( @PathParam( "pathId" ) String pathId ) {
    String path = FileUtils.idToPath( pathId );

    if ( !isPathValid( path ) ) {
      return Response.status( FORBIDDEN ).build();
    }

    // you have to have PublishAction in order to get dir as zip
    if ( getPolicy().isAllowed( PublishAction.NAME ) == false ) {
      return Response.status( FORBIDDEN ).build();
    }

    RepositoryFile repoFile = getRepository().getFile( path );

    if ( repoFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status( NOT_FOUND ).build();
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
    StreamingOutput streamingOutput = null;

    try {
      org.pentaho.platform.plugin.services.importexport.Exporter exporter =
          new org.pentaho.platform.plugin.services.importexport.Exporter( repository );
      exporter.setRepoPath( path );
      exporter.setRepoWs( repoWs );

      File zipFile = exporter.doExportAsZip( repositoryFile );
      is = new FileInputStream( zipFile );
    } catch ( Exception e ) {
      return Response.serverError().entity( e.toString() ).build();
    }

    streamingOutput = new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
    Response response = null;
    response = Response.ok( streamingOutput, APPLICATION_ZIP ).build();
    return response;
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
  // have to accept anything for browsers to work
  public String doIsParameterizable( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = getRepository().getFile( FileUtils.idToPath( pathId ) );
    if ( repositoryFile != null ) {
      try {
        hasParameterUi =
            ( PentahoSystem.get( IPluginManager.class ).getContentGenerator(
                repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameterUi" )
                != null );
      } catch ( NoSuchBeanDefinitionException e ) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    if ( hasParameterUi ) {
      try {
        IContentGenerator parameterContentGenerator =
            PentahoSystem.get( IPluginManager.class ).getContentGenerator(
                repositoryFile.getName().substring( repositoryFile.getName().lastIndexOf( '.' ) + 1 ), "parameter" );
        if ( parameterContentGenerator != null ) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          parameterContentGenerator.setOutputHandler( new SimpleOutputHandler( outputStream, false ) );
          parameterContentGenerator.setMessagesList( new ArrayList<String>() );
          Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
          SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
          parameterProvider.setParameter( "path", URLEncoder.encode( repositoryFile.getPath(), "UTF-8" ) );
          parameterProvider.setParameter( "renderMode", "PARAMETER" );
          parameterProviders.put( IParameterProvider.SCOPE_REQUEST, parameterProvider );
          parameterContentGenerator.setParameterProviders( parameterProviders );
          parameterContentGenerator.setSession( PentahoSessionHolder.getSession() );
          parameterContentGenerator.createContent();
          if ( outputStream.size() > 0 ) {
            Document document = DocumentHelper.parseText( outputStream.toString() );

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
        logger
            .error( Messages.getInstance().getString( "FileResource.PARAM_FAILURE", e.getMessage() ), e ); //$NON-NLS-1$
      }
    }
    return Boolean.toString( hasParameters );
  }

  /**
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
   * @return a Response object that contains either
   * 1) a response code of SUCCESS and a pay load that contains one of the follow
   * A) An attachment that represents a single file
   * B) An attachment that represents a zip file of the directory and all of its descendants and structure.
   * 2) a response code of 400 (Bad Request) - Usually a bad pathId
   * 3) a response code of 403 (Forbidden) - pathId points at a file the user doesn't have access to.
   * 4) a response code of 404 (Not found) - file was not found.
   * 5) a response with of 500 (Internal Server Error) an unexpected error.
   */
  @GET
  @Path( "{pathId : .+}/download" )
  @Produces( WILDCARD )
  @JMeterTest( url = "/repo/files/{pathId : .+}/download", requestType = "GET" )
  // have to accept anything for browsers to work
  public Response doGetFileOrDirAsDownload( @HeaderParam( "user-agent" ) String userAgent,
      @PathParam( "pathId" ) String pathId, @QueryParam( "withManifest" ) String strWithManifest ) {
    FileService.DownloadFileWrapper wrapper = null;
    try {
      wrapper = fileService.doGetFileOrDirAsDownload( userAgent, pathId, strWithManifest );
      return Response.ok( wrapper.getOutputStream(), APPLICATION_ZIP + "; charset=UTF-8" )
          .header( "Content-Disposition", wrapper.getAttachment() ).build();
    } catch ( InvalidParameterException e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( BAD_REQUEST ).build();
    } catch ( InvalidPathException e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( FORBIDDEN ).build();
    } catch ( GeneralSecurityException e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( FORBIDDEN ).build();
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( NOT_FOUND ).build();
    } catch ( Throwable e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", wrapper.getEncodedFileName() + " " + e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  /**
   * Retrieves the file from the repository as inline.
   * <p/>
   * Retrieves the file from the repository as inline. This is mainly used for css or and dependent files for the html
   * document
   *
   * @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @return Response wraps the output stream and file name
   * @throws IllegalStateException
   */
  @GET
  @Path( "{pathId : .+}/inline" )
  @Produces( WILDCARD )
  @JMeterTest( url = "/repo/files/{pathId : .+}/inline", requestType = "GET" )
  public Response doGetFileAsInline( @PathParam( "pathId" ) String pathId ) {
    try {
      FileService.RepositoryFileToStreamWrapper wrapper = fileService.doGetFileAsInline( pathId );
      return Response.ok( wrapper.getOutputStream() )
          .header( "Content-Disposition", "inline; filename=" + wrapper.getRepositoryFile().getName() )
          .build();
    } catch ( IllegalArgumentException e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return Response.status( FORBIDDEN ).build();
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return Response.status( NOT_FOUND ).build();
    } catch ( InternalError e ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), e );
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  /**
   * <p>Save the acls of the selected file to the repository<p/>
   *
   * <p>This method is used to update and save the acls of the selected file to the repository<p/>
   *
   * @param pathId colon separated path for the repository file
   *               <pre function="syntax.xml">
   *               :path:to:file:id
   *               </pre>
   * @param acl    Acl of the repository file <code> RepositoryFileAclDto </code>
   *
   * <p>Example Request:<br>
   *               PUT api/repo/files/%3Ahome%3Aadmin%3Aafile.prpti/acl HTTP/1.1<br>
   *               Content-Type: application/xml
   *               <p/>
   *               <pre function="syntax.xml">
   *               {@code
   *                 < xml>
   *                   < repositoryFileAclDto>
   *                     < entriesInheriting>true< /entriesInheriting>
   *                     < id>068390ba-f90d-46e3-8c55-bbe55e24b2fe< /id>
   *                     < owner>admin< /owner>
   *                     < ownerType>0< /ownerType>
   *                   < /repositoryFileAclDto>
   *                 < /xml>
   *               }
   *               </pre>
   * <p>Example Response:<br/>
   *               HTTP/1.1 200 OK
   *               </p>
   * @return response object indicating the success or failure of this operation
   */

  @PUT
  @Path( "{pathId : .+}/acl" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response setFileAcls( @PathParam( "pathId" ) String pathId, RepositoryFileAclDto acl ) {
    try {
      fileService.setFileAcls( pathId, acl );
      return Response.ok().build();
    } catch (Exception exception) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), exception );
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }
  }

  /**
   * Store content creator of the selected repository file
   *
   * @param pathId colon separated path for the repository file
   * <pre function="syntax.xml">
   *    :path:to:file:id
   * </pre>
   * @param contentCreator repository file
   * <pre function="syntax.xml">
   *   <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
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
   * @returns response object indicating the success or failure of this operation
   * @throws FileNotFoundException
   */
  @PUT
  @Path( "{pathId : .+}/creator" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/repo/files/{pathId : .+}/creator", requestType = "PUT", statusCode = "200",
    postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22" )
  public Response doSetContentCreator( @PathParam( "pathId" ) String pathId, RepositoryFileDto contentCreator ) {
    try {
      fileService.doSetContentCreator( pathId, contentCreator );
      return Response.ok().build();
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.FILE_UNKNOWN", pathId ), e );
      return Response.status( NOT_FOUND ).build();
    } catch ( Throwable t ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), t );
      return Response.status( INTERNAL_SERVER_ERROR ).build();
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
  @JMeterTest( url = "/repo/files/{pathId : .+}/locales", requestType = "GET", statusCode = "200",
    postData = "34ae56a4-2d96-4cd3-ad7a-21156f186c22" )
  public List<LocaleMapDto> doGetFileLocales( @PathParam( "pathId" ) String pathId ) {
    List<LocaleMapDto> locales = new ArrayList<LocaleMapDto>();
    try {
      locales = fileService.doGetFileLocales( pathId );
    } catch ( FileNotFoundException e ) {
      logger.error( Messages.getInstance().getErrorString( "FileResource.FILE_UNKNOWN", pathId ), e );
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
  @JMeterTest( url = "/repo/files/deleteLocale", requestType = "PUT", statusCode = "200" )
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
  @JMeterTest( url = "/repo/files/{pathId : .+}/canAccessMap", requestType = "PUT" )
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
  @JMeterTest( url = "/repo/files/pathsAccessList", requestType = "PUT" )
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
   * @return String <code>"true"</code> if the user can administer the platform, or "false" otherwise
   */
  @GET
  @Path( "/canAdminister" )
  @Produces( TEXT_PLAIN )
  @JMeterTest( url = "/repo/files/canAdminister", requestType = "GET", statusCode = "200" )
  public String doGetCanAdminister() {
    try {
      return fileService.doCanAdminister()? "true" : "false";
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
  @JMeterTest( url = "/repo/files/reservedCharacters", requestType = "PUT", statusCode = "200" )
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
  @JMeterTest( url = "/repo/files/reservedCharactersDisplay", requestType = "GET", statusCode = "200" )
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
  @JMeterTest( url = "/repo/files/canCreate", requestType = "GET", statusCode = "200" )
  public String doGetCanCreate() {
    return fileService.doGetCanCreate();
  }

  /**
   * Retrieves the acls of the selected repository file
   *
   * @param pathId (colon separated path for the repository file)
   * @return <code> RepositoryFileAclDto </code>
   */
  @GET
  @Path( "{pathId : .+}/acl" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileAclDto doGetFileAcl( @PathParam( "pathId" ) String pathId ) {
    RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
    RepositoryFileAclDto fileAcl = getRepoWs().getAcl( file.getId() );
    if ( fileAcl.isEntriesInheriting() ) {
      List<RepositoryFileAclAceDto> aces =
          getRepoWs().getEffectiveAcesWithForceFlag( file.getId(), fileAcl.isEntriesInheriting() );
      fileAcl.setAces( aces, fileAcl.isEntriesInheriting() );
    }
    addAdminRole( fileAcl );
    return fileAcl;
  }

  private void addAdminRole( RepositoryFileAclDto fileAcl ) {
    String adminRoleName =
        PentahoSystem.get( String.class, "singleTenantAdminAuthorityName", PentahoSessionHolder.getSession() );
    if ( fileAcl.getAces() == null ) {
      fileAcl.setAces( new LinkedList<RepositoryFileAclAceDto>() );
    }
    for ( RepositoryFileAclAceDto facl : fileAcl.getAces() ) {
      if ( facl.getRecipient().equals( adminRoleName ) && facl.getRecipientType() == 1 ) {
        return;
      }
    }
    RepositoryFileAclAceDto adminGroup = new RepositoryFileAclAceDto();
    adminGroup.setRecipient( adminRoleName );
    adminGroup.setRecipientType( 1 );
    adminGroup.setModifiable( false );
    List<Integer> perms = new LinkedList<Integer>();
    perms.add( 4 );
    adminGroup.setPermissions( perms );
    fileAcl.getAces().add( adminGroup );
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
   *               GET /pentaho/api/repo/files/%3Ahome%3Aadmin%3Aafile.prpti/properties HTTP/1.1
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
    } catch (FileNotFoundException fileNotFound) {
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
  @JMeterTest( url = "/repo/files/creator", requestType = "GET", statusCode = "200" )
  public RepositoryFileDto doGetContentCreator( @PathParam( "pathId" ) String pathId ) {
    try {
      return fileService.doGetContentCreator( pathId );
    } catch ( Throwable t ) {
      return null;
    }
  }

  /**
   * Retrieve the list of executed contents for a selected content from the repository.
   *
   * @param pathId (colon separated path for the repository file)
   * @return list of <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/generatedContent" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContent( @PathParam( "pathId" ) String pathId ) {
    RepositoryFileDto targetFile = doGetProperties( pathId );
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    if ( targetFile != null ) {
      String targetFileId = targetFile.getId();
      SessionResource sessionResource = new SessionResource();

      RepositoryFile workspaceFolder = getRepository().getFile( sessionResource.doGetCurrentUserDir() );
      if ( workspaceFolder != null ) {
        List<RepositoryFile> children = getRepository().getChildren( workspaceFolder.getId() );
        for ( RepositoryFile child : children ) {
          if ( !child.isFolder() ) {
            Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( child.getId() );
            String creatorId = (String) fileMetadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
            if ( creatorId != null && creatorId.equals( targetFileId ) ) {
              content.add( RepositoryFileAdapter.toFileDto( child, null, false ) );
            }
          }
        }
      }
    }
    return content;
  }

  /**
   * Retrieve the executed contents for a selected repository file and a given user
   *
   * @param pathId (colon separated path for the repository file)
   * @param user   (user of the platform)
   * @return list of <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/generatedContentForUser" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContentForUser( @PathParam( "pathId" ) String pathId,
      @QueryParam( "user" ) String user ) {
    RepositoryFileDto targetFile = doGetProperties( pathId );
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    if ( targetFile != null ) {
      String targetFileId = targetFile.getId();
      SessionResource sessionResource = new SessionResource();

      RepositoryFile workspaceFolder = getRepository().getFile( sessionResource.doGetUserDir( user ) );
      if ( workspaceFolder != null ) {
        List<RepositoryFile> children = getRepository().getChildren( workspaceFolder.getId() );
        for ( RepositoryFile child : children ) {
          if ( !child.isFolder() ) {
            Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( child.getId() );
            String creatorId = (String) fileMetadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
            if ( creatorId != null && creatorId.equals( targetFileId ) ) {
              content.add( RepositoryFileAdapter.toFileDto( child, null, false ) );
            }
          }
        }
      }
    }
    return content;
  }

  /**
   * Retrieve the list of execute content by lineage id.
   *
   * @param lineageId
   * @return list of <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "/generatedContentForSchedule" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetGeneratedContentForSchedule( @QueryParam( "lineageId" ) String lineageId ) {
    List<RepositoryFileDto> content = new ArrayList<RepositoryFileDto>();
    SessionResource sessionResource = new SessionResource();
    RepositoryFile workspaceFolder = getRepository().getFile( sessionResource.doGetCurrentUserDir() );
    if ( workspaceFolder != null ) {
      List<RepositoryFile> children = getRepository().getChildren( workspaceFolder.getId() );
      for ( RepositoryFile child : children ) {
        if ( !child.isFolder() ) {
          Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( child.getId() );
          String lineageIdMeta = (String) fileMetadata.get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID );
          if ( lineageIdMeta != null && lineageIdMeta.equals( lineageId ) ) {
            content.add( RepositoryFileAdapter.toFileDto( child, null, false ) );
          }
        }
      }
    }
    return content;
  }

  /**
   * Retrieve the list of files from root of the repository.
   *
   * @param depth      (how many level should the search go)
   * @param filter     (filter to be applied for search)
   * @param showHidden (include or exclude hidden files from the file list)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetRootTree( @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return doGetTree( FileUtils.PATH_SEPARATOR, depth, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the list of files from root of the repository.
   *
   * @param filter (filter to be applied for search)
   * @return list of files <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetRootChildren( @QueryParam( "filter" ) String filter,
      @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return doGetChildren( FileUtils.PATH_SEPARATOR, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the children of the selected repository file. This is a recursive search with a selected level depth and
   * filter
   *
   * @param pathId      The path from the root folder to the root node of the tree to return using colon characters in place of /
   *                    or \ characters. To specify /public/Steel Wheels, the encoded pathId would be :public:Steel%20Wheels
   * @param depth       (how many level should the search go)
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
   * @param showHidden  (include or exclude hidden files from the file list)
   * @param includeAcls (Include permission information about the file in the output)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetTree( @PathParam( "pathId" ) String pathId, @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {

    String path = null;
    if ( pathId == null || pathId.equals( FileUtils.PATH_SEPARATOR ) ) {
      path = FileUtils.PATH_SEPARATOR;
    } else {
      if ( !pathId.startsWith( FileUtils.PATH_SEPARATOR ) ) {
        path = FileUtils.idToPath( pathId );
      }
    }

    RepositoryRequest repositoryRequest = new RepositoryRequest( path, showHidden, depth, filter );
    repositoryRequest.setIncludeAcls( includeAcls );

    RepositoryFileTreeDto tree = getRepoWs().getTreeFromRequest( repositoryRequest );
    List<RepositoryFileTreeDto> filteredChildren = new ArrayList<RepositoryFileTreeDto>();

    // BISERVER-9599 - Use special sort order
    if ( isShowingTitle( repositoryRequest ) ) {
      Collator collator = Collator.getInstance( PentahoSessionHolder.getSession().getLocale() );
      collator.setStrength( Collator.PRIMARY ); // ignore case
      sortByLocaleTitle( collator, tree );
    }

    for ( RepositoryFileTreeDto child : tree.getChildren() ) {
      RepositoryFileDto file = child.getFile();
      Map<String, Serializable> fileMeta = getRepository().getFileMetadata( file.getId() );
      boolean isSystemFolder =
          fileMeta.containsKey( IUnifiedRepository.SYSTEM_FOLDER ) ? (Boolean) fileMeta
              .get( IUnifiedRepository.SYSTEM_FOLDER ) : false;
      if ( !isSystemFolder ) {
        filteredChildren.add( child );
      }
    }
    tree.setChildren( filteredChildren );

    return tree;
  }

  /**
   * Retrieve the children of the selected repository file. This is a recursive search with a selected level depth and
   * filter
   *
   * @param pathId      The path from the root folder to the root node of the tree to return using colon characters in place of /
   *                    or \ characters. To specify /public/Steel Wheels, the encoded pathId would be :public:Steel%20Wheels
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
   * @param showHidden  (include or exclude hidden files from the file list)
   * @param includeAcls (Include permission information about the file in the output)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetChildren( @PathParam( "pathId" ) String pathId,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {

    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( FileUtils.idToPath( pathId ) );

    if ( repositoryFileDto != null && isPathValid( repositoryFileDto.getPath() ) ) {
      RepositoryRequest repositoryRequest = new RepositoryRequest( repositoryFileDto.getId(), showHidden, 0, filter );
      repositoryRequest.setIncludeAcls( includeAcls );
      repositoryFileDtoList = getRepoWs().getChildrenFromRequest( repositoryRequest );

      // BISERVER-9599 - Use special sort order
      if ( isShowingTitle( repositoryRequest ) ) {
        Collator collator = Collator.getInstance( PentahoSessionHolder.getSession().getLocale() );
        collator.setStrength( Collator.PRIMARY ); // ignore case
        sortByLocaleTitle( collator, repositoryFileDtoList );
      }
    }
    return repositoryFileDtoList;
  }

  /**
   * Retrieve the list of files in the user's trash folder
   *
   * @return list of <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "/deleted" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @JMeterTest( url = "/repo/files/deleted", requestType = "GET", statusCode = "200" )
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
  @JMeterTest( url = "/repo/files/:home:admin:file.txt/metadata", requestType = "PUT", statusCode = "200" )
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
   * @param pathId  (colon separated path for the repository file)
   * @param newName (New name of the file)
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/rename" )
  @Consumes( { WILDCARD } )
  @Produces( { WILDCARD } )
  public Response doRename( @PathParam( "pathId" ) String pathId, @QueryParam( "newName" ) String newName ) {
    try {
      IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class, PentahoSessionHolder.getSession() );
      RepositoryFile fileToBeRenamed = repository.getFile( FileUtils.idToPath( pathId ) );
      StringBuilder buf = new StringBuilder( fileToBeRenamed.getPath().length() );
      buf.append( getParentPath( fileToBeRenamed.getPath() ) );
      buf.append( RepositoryFile.SEPARATOR );
      buf.append( newName );
      if ( !fileToBeRenamed.isFolder() ) {
        String extension = getExtension( fileToBeRenamed.getName() );
        if ( extension != null ) {
          buf.append( extension );
        }
      }
      repository.moveFile( fileToBeRenamed.getId(), buf.toString(), "Renaming the file" );
      RepositoryFile movedFile = repository.getFileById( fileToBeRenamed.getId() );
      if ( movedFile != null ) {
        if ( !movedFile.isFolder() ) {
          Map<String, Properties> localePropertiesMap = movedFile.getLocalePropertiesMap();
          if ( localePropertiesMap == null ) {
            localePropertiesMap = new HashMap<String, Properties>();
            Properties properties = new Properties();
            properties.setProperty( "file.title", newName );
            properties.setProperty( "title", newName );
            localePropertiesMap.put( "default", properties );
          } else {
            for ( Entry<String, Properties> entry : localePropertiesMap.entrySet() ) {
              Properties properties = entry.getValue();
              if ( properties.containsKey( "file.title" ) ) {
                properties.setProperty( "file.title", newName );
              }
              if ( properties.containsKey( "title" ) ) {
                properties.setProperty( "title", newName );
              }
            }
          }
          RepositoryFile updatedFile =
              new RepositoryFile.Builder( movedFile ).localePropertiesMap( localePropertiesMap ).name( newName ).title(
                  newName ).build();
          repository.updateFile( updatedFile, getData( movedFile ), "Updating the file" );
        }
        return Response.ok().build();
      } else {
        return Response.ok( "File to be renamed does not exist" ).build();
      }
    } catch ( Throwable t ) {
      return processErrorResponse( t.getClass().getName() );
    }
  }

  private Response processErrorResponse( String errMessage ) {
    return Response.ok( errMessage ).build();
  }

  private String getParentPath( final String path ) {
    if ( path == null ) {
      throw new IllegalArgumentException();
    } else if ( RepositoryFile.SEPARATOR.equals( path ) ) {
      return null;
    }
    int lastSlashIndex = path.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( lastSlashIndex == 0 ) {
      return RepositoryFile.SEPARATOR;
    } else if ( lastSlashIndex > 0 ) {
      return path.substring( 0, lastSlashIndex );
    } else {
      throw new IllegalArgumentException();
    }
  }

  private IRepositoryFileData getData( RepositoryFile repositoryFile ) {
    IRepositoryContentConverterHandler converterHandler;
    Map<String, Converter> converters;
    NameBaseMimeResolver mimeResolver;

    IRepositoryFileData repositoryFileData = null;

    if ( !repositoryFile.isFolder() ) {
      // Get the extension
      final String ext = RepositoryFilenameUtils.getExtension( repositoryFile.getName() );
      if ( ( ext == null ) || ( ext.isEmpty() ) ) {
        return null;
      }

      // Find the converter

      // If we have not been given a handler, try PentahoSystem
      converterHandler = PentahoSystem.get( IRepositoryContentConverterHandler.class );

      // fail if we have no converter handler
      if ( converterHandler == null ) {
        return null;
      }

      converters = converterHandler.getConverters();

      final Converter converter = converters.get( ext );
      if ( converter == null ) {
        return null;
      }

      // Check the mime type
      mimeResolver = PentahoSystem.get( NameBaseMimeResolver.class );

      // fail if we have no mime resolver
      if ( mimeResolver == null ) {
        return null;
      }

      final String mimeType = mimeResolver.resolveMimeTypeForFileName( repositoryFile.getName() ).getName();
      if ( ( mimeType == null ) || ( mimeType.isEmpty() ) ) {
        return null;
      }

      // Get the input stream
      InputStream inputStream = converter.convert( repositoryFile.getId() );
      if ( inputStream == null ) {
        return null;
      }

      // Get the file data
      repositoryFileData = converter.convert( inputStream, "UTF-8", mimeType );
      if ( repositoryFileData == null ) {
        return null;
      }
    }

    return repositoryFileData;
  }

  private String getExtension( final String name ) {
    int startIndex = name.lastIndexOf( '.' );
    if ( startIndex >= 0 ) {
      return name.substring( startIndex, name.length() );
    }
    return null;
  }

  /**
   * Store the metadata of the selected fle. Even though the hidden flag is a property of the file node itself, and not
   * the metadata child, it is considered metadata from PUC and is included in the setMetadata call
   *
   * @param pathId   (colon separated path for the repository file)
   * @param metadata (list of name value pair of metadata)
   * @return
   */
  //TODO: Refactor REST endpoint to not include operational logic
  @PUT
  @Path( "{pathId : .+}/metadata" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doSetMetadata( @PathParam( "pathId" ) String pathId, List<StringKeyStringValueDto> metadata ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( FileUtils.idToPath( pathId ) );
      RepositoryFileAclDto fileAcl = getRepoWs().getAcl( file.getId() );

      boolean canManage =
          PentahoSessionHolder.getSession().getName().equals( fileAcl.getOwner() )
              || ( getPolicy().isAllowed( RepositoryReadAction.NAME )
              && getPolicy().isAllowed( RepositoryCreateAction.NAME ) && getPolicy().isAllowed(
              AdministerSecurityAction.NAME ) );

      if ( !canManage ) {

        if ( fileAcl.isEntriesInheriting() ) {
          List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces( file.getId() );
          fileAcl.setAces( aces, fileAcl.isEntriesInheriting() );
        }

        for ( int i = 0; i < fileAcl.getAces().size(); i++ ) {
          RepositoryFileAclAceDto acl = fileAcl.getAces().get( i );
          if ( acl.getRecipient().equals( PentahoSessionHolder.getSession().getName() ) ) {
            if ( acl.getPermissions().contains( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() )
                || acl.getPermissions().contains( RepositoryFilePermission.ALL.ordinal() ) ) {
              canManage = true;
              break;
            }
          }
        }
      }

      if ( canManage ) {
        Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
        boolean isHidden = false;

        for ( StringKeyStringValueDto nv : metadata ) {
          // don't add hidden to the list because it is not actually part of the metadata node
          if ( ( nv.getKey().contentEquals( "_PERM_HIDDEN" ) ) ) {
            isHidden = Boolean.parseBoolean( nv.getValue() );
          } else {
            fileMetadata.put( nv.getKey(), nv.getValue() );
          }
        }

        // now update the rest of the metadata
        if ( !file.isFolder() ) {
          getRepository().setFileMetadata( file.getId(), fileMetadata );
        }

        // handle hidden flag if it is different
        if ( file.isHidden() != isHidden ) {
          file.setHidden( isHidden );

          /*
           * Since we cannot simply set the new value, use the RepositoryFileAdapter to create a new instance and then
           * update the original.
           */
          RepositoryFile sourceFile = getRepository().getFileById( file.getId() );
          RepositoryFileDto destFileDto = RepositoryFileAdapter.toFileDto( sourceFile, null, false );

          destFileDto.setHidden( isHidden );

          RepositoryFile destFile = RepositoryFileAdapter.toFile( destFileDto );

          // add the existing acls and file data
          RepositoryFileAcl acl = getRepository().getAcl( sourceFile.getId() );
          if ( !file.isFolder() ) {
            IRepositoryFileData data = getData( sourceFile );

            getRepository().updateFile( destFile, data, null );
            getRepository().updateAcl( acl );
          } else {
            getRepository().updateFolder( destFile, null );
          }
        }
        return Response.ok().build();
      } else {
        return Response.status( Response.Status.UNAUTHORIZED ).build();
      }
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Validate path and send appropriate response if necessary TODO: Add validation to IUnifiedRepository interface
   *
   * @param path
   * @return
   */
  private boolean isPathValid( String path ) {
    if ( path.startsWith( "/etc" ) || path.startsWith( "/system" ) ) {
      return false;
    }
    return true;
  }

  private boolean isPath( String pathId ) {
    return pathId != null && pathId.contains( "/" );
  }

  private void sortByLocaleTitle( final Collator collator, final RepositoryFileTreeDto tree ) {

    if ( tree == null || tree.getChildren() == null || tree.getChildren().size() <= 0 ) {
      return;
    }

    for ( RepositoryFileTreeDto rft : tree.getChildren() ) {
      sortByLocaleTitle( collator, rft );
      Collections.sort( tree.getChildren(), new Comparator<RepositoryFileTreeDto>() {
        @Override
        public int compare( RepositoryFileTreeDto repositoryFileTree, RepositoryFileTreeDto repositoryFileTree2 ) {
          String title1 = repositoryFileTree.getFile().getTitle();
          String title2 = repositoryFileTree2.getFile().getTitle();

          if ( collator.compare( title1, title2 ) == 0 ) {
            return title1.compareTo( title2 ); // use lexical order if equals ignore case
          }

          return collator.compare( title1, title2 );
        }
      } );
    }
  }

  private void sortByLocaleTitle( final Collator collator, final List<RepositoryFileDto> repositoryFileDtoList ) {

    if ( repositoryFileDtoList == null || repositoryFileDtoList.size() <= 0 ) {
      return;
    }

    for ( RepositoryFileDto rft : repositoryFileDtoList ) {
      Collections.sort( repositoryFileDtoList, new Comparator<RepositoryFileDto>() {
        @Override
        public int compare( RepositoryFileDto repositoryFile, RepositoryFileDto repositoryFile2 ) {
          String title1 = repositoryFile.getTitle();
          String title2 = repositoryFile2.getTitle();

          if ( collator.compare( title1, title2 ) == 0 ) {
            return title1.compareTo( title2 ); // use lexical order if equals ignore case
          }

          return collator.compare( title1, title2 );
        }
      } );
    }
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

  private boolean isShowingTitle( RepositoryRequest repositoryRequest ) {
    if ( repositoryRequest.getExcludeMemberSet() != null && !repositoryRequest.getExcludeMemberSet().isEmpty() ) {
      if ( repositoryRequest.getExcludeMemberSet().contains( "title" ) ) {
        return false;
      }
    } else if ( repositoryRequest.getIncludeMemberSet() != null
        && !repositoryRequest.getIncludeMemberSet().contains( "title" ) ) {
      return false;
    }
    return true;
  }

  public void setConverterHandler( IRepositoryContentConverterHandler converterHandler ) {
    this.converterHandler = converterHandler;
  }

  public void setMimeResolver( NameBaseMimeResolver mimeResolver ) {
    this.mimeResolver = mimeResolver;
  }
}
