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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
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
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.engine.core.output.SimpleOutputHandler;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.NameBaseMimeResolver;
import org.pentaho.platform.plugin.services.importexport.BaseExportProcessor;
import org.pentaho.platform.plugin.services.importexport.DefaultExportHandler;
import org.pentaho.platform.plugin.services.importexport.SimpleExportProcessor;
import org.pentaho.platform.plugin.services.importexport.ZipExportProcessor;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.locale.PentahoLocale;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;
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
import org.pentaho.platform.web.http.messages.Messages;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

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
  }

  public static String idToPath( String pathId ) {
    String path = null;
    // slashes in pathId are illegal.. we scrub them out so the file will not be found
    // if the pathId was given in slash separated format
    if ( pathId.contains( PATH_SEPARATOR ) ) {
      logger.warn( Messages.getInstance().getString( "FileResource.ILLEGAL_PATHID", pathId ) ); //$NON-NLS-1$
    }
    path = pathId.replaceAll( PATH_SEPARATOR, "" ); //$NON-NLS-1$
    path = path.replace( ':', '/' );
    if ( !path.startsWith( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR + path;
    }
    return path;
  }

  /**
   * Moves the list of files to the user's trash folder
   * 
   * @param params
   *          List of the files to be deleted
   * @return
   */
  @PUT
  @Path( "/delete" )
  @Consumes( { WILDCARD } )
  public Response doDeleteFiles( String params ) {
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFile( sourceFileIds[i], null );
      }
      return Response.ok().build();
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Permanently deletes the selected list of files from the repository
   * 
   * @param params
   *          list of files to be deleted
   * @return
   */
  @PUT
  @Path( "/deletepermanent" )
  @Consumes( { WILDCARD } )
  public Response doDeleteFilesPermanent( String params ) {
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().deleteFileWithPermanentFlag( sourceFileIds[i], true, null );
      }
      return Response.ok().build();
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Restore the selected list of files from the user's trash folder to their original location
   * 
   * @param params
   *          list of files to be restored
   * @return
   */
  @PUT
  @Path( "/restore" )
  @Consumes( { WILDCARD } )
  public Response doRestore( String params ) {
    String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
    try {
      for ( int i = 0; i < sourceFileIds.length; i++ ) {
        getRepoWs().undeleteFile( sourceFileIds[i], null );
      }
      return Response.ok().build();
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Creates a new file with the provided contents at a given path
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param fileContents
   *          (content of the file)
   * @return
   * @throws IOException
   */
  @PUT
  @Path( "{pathId : .+}" )
  @Consumes( { WILDCARD } )
  public Response createFile( @PathParam( "pathId" ) String pathId, InputStream fileContents ) throws IOException {
    RepositoryFileOutputStream rfos = new RepositoryFileOutputStream( idToPath( pathId ) );
    rfos.setCharsetName( httpServletRequest.getCharacterEncoding() );
    IOUtils.copy( fileContents, rfos );
    rfos.close();
    fileContents.close();
    return Response.ok().build();
  }

  /**
   * Copy selected list of files to a new specified location
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param mode
   *          (MODE_OVERWITE or MODE_NO_OVERWRITE)
   * @param params
   *          (List of files to be copied)
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/children" )
  @Consumes( { TEXT_PLAIN } )
  public Response copyFiles( @PathParam( "pathId" ) String pathId, @QueryParam( "mode" ) Integer mode, String params ) {
    if ( mode == null ) {
      mode = MODE_RENAME;
    }
    if ( !getPolicy().isAllowed( RepositoryCreateAction.NAME ) ) {
      return Response.status( FORBIDDEN ).build();
    }
    try {
      String path = idToPath( pathId );
      RepositoryFile destDir = getRepository().getFile( path );
      String[] sourceFileIds = params.split( "[,]" ); //$NON-NLS-1$
      if ( mode == MODE_OVERWRITE || mode == MODE_NO_OVERWRITE ) {
        for ( String sourceFileId : sourceFileIds ) {
          RepositoryFile sourceFile = getRepository().getFileById( sourceFileId );
          if ( destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder() ) {
            String fileName = sourceFile.getName();
            String sourcePath = sourceFile.getPath().substring( 0, sourceFile.getPath().lastIndexOf( PATH_SEPARATOR ) );
            if ( !sourcePath.equals( destDir.getPath() ) ) { // We're saving to a different folder than we're copying
                                                             // from
              IRepositoryFileData data = getData( sourceFile );
              RepositoryFileAcl acl = getRepository().getAcl( sourceFileId );
              RepositoryFile destFile = getRepository().getFile( destDir.getPath() + PATH_SEPARATOR + fileName );
              if ( destFile == null ) { // destFile doesn't exist so we'll create it.
                RepositoryFile duplicateFile =
                    new RepositoryFile.Builder( fileName ).hidden( sourceFile.isHidden() ).versioned(
                        sourceFile.isVersioned() ).build();
                final RepositoryFile repositoryFile =
                    getRepository().createFile( destDir.getId(), duplicateFile, data, acl, null );
                getRepository()
                    .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
              } else if ( mode == MODE_OVERWRITE ) { // destFile exists so check to see if we want to overwrite it.
                RepositoryFileDto destFileDto = RepositoryFileAdapter.toFileDto( destFile, null, false );
                destFileDto.setHidden( sourceFile.isHidden() );
                destFile = RepositoryFileAdapter.toFile( destFileDto );
                final RepositoryFile repositoryFile = getRepository().updateFile( destFile, data, null );
                getRepository().updateAcl( acl );
                getRepository()
                    .setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
              }
            }
          }
        }
      } else {
        for ( String sourceFileId : sourceFileIds ) {
          RepositoryFile sourceFile = getRepository().getFileById( sourceFileId );
          if ( destDir != null && destDir.isFolder() && sourceFile != null && !sourceFile.isFolder() ) {

            // First try to see if regular name is available
            String fileName = sourceFile.getName();
            String copyText = "";
            String rootCopyText = "";
            String nameNoExtension = fileName.substring( 0, fileName.lastIndexOf( '.' ) );
            String extension = fileName.substring( fileName.lastIndexOf( '.' ) );

            RepositoryFileDto testFile = getRepoWs().getFile( path + PATH_SEPARATOR + nameNoExtension + extension ); //$NON-NLS-1$
            if ( testFile != null ) {
              // Second try COPY_PREFIX, If the name already ends with a COPY_PREFIX don't append twice
              if ( !nameNoExtension.endsWith( Messages.getInstance().getString( "FileResource.COPY_PREFIX" ) ) ) { //$NON-NLS-1$
                copyText = rootCopyText = Messages.getInstance().getString( "FileResource.COPY_PREFIX" );
                fileName = nameNoExtension + copyText + extension;
                testFile = getRepoWs().getFile( path + PATH_SEPARATOR + fileName );
              }
            }

            // Third try COPY_PREFIX + DUPLICATE_INDICATOR
            Integer nameCount = 1;
            while ( testFile != null ) {
              nameCount++;
              copyText =
                  rootCopyText + Messages.getInstance().getString( "FileResource.DUPLICATE_INDICATOR", nameCount );
              fileName = nameNoExtension + copyText + extension;
              testFile = getRepoWs().getFile( path + PATH_SEPARATOR + fileName );
            }
            IRepositoryFileData data = getData( sourceFile );
            RepositoryFileAcl acl = getRepository().getAcl( sourceFileId );
            RepositoryFile duplicateFile = null;

            // If the title is different than the source file, copy it separately
            if ( !sourceFile.getName().equals( sourceFile.getTitle() ) ) {
              duplicateFile =
                  new RepositoryFile.Builder( fileName ).title( RepositoryFile.DEFAULT_LOCALE,
                      sourceFile.getTitle() + copyText ).hidden( sourceFile.isHidden() ).versioned(
                      sourceFile.isVersioned() ).build();
            } else {
              duplicateFile = new RepositoryFile.Builder( fileName ).hidden( sourceFile.isHidden() ).build();
            }

            final RepositoryFile repositoryFile =
                getRepository().createFile( destDir.getId(), duplicateFile, data, acl, null );
            getRepository().setFileMetadata( repositoryFile.getId(), getRepository().getFileMetadata( sourceFileId ) );
          }
        }
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().entity( new SafeHtmlBuilder().appendEscapedLines( t.getLocalizedMessage() ).toSafeHtml().asString() ).build();
    }
    return Response.ok().build();
  }

  // ///////
  // READ

  /**
   * Overloaded this method to try and reduce calls to the repository
   * 
   * @param pathId
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}" )
  @Produces( { WILDCARD } )
  public Response doGetFileOrDir( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    String path = idToPath( pathId );

    if ( !isPathValid( path ) ) {
      return Response.status( FORBIDDEN ).build();
    }

    RepositoryFile repoFile = getRepository().getFile( path );

    if ( repoFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status( NOT_FOUND ).build();
    }

    // check whitelist acceptance of file (based on extension)
    if ( getWhitelist().accept( repoFile.getName() ) == false ) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if ( getPolicy().isAllowed( PublishAction.NAME ) == false ) {
        return Response.status( FORBIDDEN ).build();
      }
    }

    return doGetFileOrDir( repoFile );
  }

  /**
   * Overloaded this method to try and reduce calls to the repository
   * 
   * @param repoFile
   * @return
   * @throws FileNotFoundException
   */
  public Response doGetFileOrDir( RepositoryFile repoFile ) throws FileNotFoundException {
    final RepositoryFileInputStream is = new RepositoryFileInputStream( repoFile );
    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        IOUtils.copy( is, output );
      }
    };
    return Response.ok( streamingOutput, is.getMimeType() ).header( "Content-Disposition",
        "inline; filename=\"" + repoFile.getName() + "\"" ).build();
  }

  // Overloaded this method to try and minimize calls to the repo
  // Had to unmap this method since browsers ask for resources with Accepts="*/*" which will default to this method
  // @GET
  // @Path("{pathId : .+}")
  // @Produces({ APPLICATION_ZIP })
  public Response doGetDirAsZip( @PathParam( "pathId" ) String pathId ) {
    String path = idToPath( pathId );

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
   * 
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
   * @param pathId
   *          (colon separated path for the repository file)
   * @return ("true" or "false")
   * @throws FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}/parameterizable" )
  @Produces( TEXT_PLAIN )
  // have to accept anything for browsers to work
    public
    String doIsParameterizable( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    boolean hasParameterUi = false;
    RepositoryFile repositoryFile = getRepository().getFile( FileResource.idToPath( pathId ) );
    if ( repositoryFile != null ) {
      try {
        hasParameterUi =
            ( PentahoSystem.get( IPluginManager.class ).getContentGenerator(
                repositoryFile.getName().substring( repositoryFile.getName().indexOf( '.' ) + 1 ), "parameterUi" ) != null );
      } catch ( NoSuchBeanDefinitionException e ) {
        // Do nothing.
      }
    }
    boolean hasParameters = false;
    if ( hasParameterUi ) {
      try {
        IContentGenerator parameterContentGenerator =
            PentahoSystem.get( IPluginManager.class ).getContentGenerator(
                repositoryFile.getName().substring( repositoryFile.getName().indexOf( '.' ) + 1 ), "parameter" );
        if ( parameterContentGenerator != null ) {
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
          parameterContentGenerator.setOutputHandler( new SimpleOutputHandler( outputStream, false ) );
          parameterContentGenerator.setMessagesList( new ArrayList<String>() );
          Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
          SimpleParameterProvider parameterProvider = new SimpleParameterProvider();
          parameterProvider.setParameter( "path", repositoryFile.getPath() );
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
        logger.error( Messages.getInstance().getString( "FileResource.PARAM_FAILURE", e.getMessage() ), e ); //$NON-NLS-1$
      }
    }
    return Boolean.toString( hasParameters );
  }

  /**
   * Download the selected file from the repository. In order to download file from the repository, the user needs to
   * have Publish action
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param strWithManifest
   *          (download file with manifest)
   * 
   * @return
   * 
   * @throws FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}/download" )
  @Produces( WILDCARD )
  // have to accept anything for browsers to work
    public
    Response doGetFileOrDirAsDownload( @HeaderParam( "user-agent" ) String userAgent,
        @PathParam( "pathId" ) String pathId, @QueryParam( "withManifest" ) String strWithManifest )
      throws FileNotFoundException {

    // you have to have PublishAction in order to download
    if ( getPolicy().isAllowed( PublishAction.NAME ) == false ) {
      return Response.status( FORBIDDEN ).build();
    }

    String quotedFileName = null;

    // send zip with manifest by default
    boolean withManifest = "false".equals( strWithManifest ) ? false : true;

    // change file id to path
    String path = idToPath( pathId );

    // if no path is sent, return bad request
    if ( StringUtils.isEmpty( pathId ) ) {
      return Response.status( BAD_REQUEST ).build();
    }

    // check if path is valid
    if ( !isPathValid( path ) ) {
      return Response.status( FORBIDDEN ).build();
    }

    // check if entity exists in repo
    RepositoryFile repositoryFile = getRepository().getFile( path );

    if ( repositoryFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status( NOT_FOUND ).build();
    }

    try {
      final InputStream is;
      StreamingOutput streamingOutput;
      Response response;
      BaseExportProcessor exportProcessor;

      // create processor
      if ( repositoryFile.isFolder() || withManifest ) {
        exportProcessor = new ZipExportProcessor( path, FileResource.repository, withManifest );
        quotedFileName = repositoryFile.getName() + ".zip"; //$NON-NLS-1$//$NON-NLS-2$
      } else {
        exportProcessor = new SimpleExportProcessor( path, FileResource.repository, withManifest );
        quotedFileName = repositoryFile.getName(); //$NON-NLS-1$//$NON-NLS-2$
      }
      quotedFileName = "\"" + URLEncoder.encode( quotedFileName, "UTF-8" ).replaceAll( "\\+", "%20" ) + "\"";

      // add export handlers for each expected file type
      exportProcessor.addExportHandler( PentahoSystem.get( DefaultExportHandler.class ) );

      File zipFile = exportProcessor.performExport( repositoryFile );
      is = new FileInputStream( zipFile );

      // copy streaming output
      streamingOutput = new StreamingOutput() {
        public void write( OutputStream output ) throws IOException {
          IOUtils.copy( is, output );
        }
      };

      // create response
      final String attachment;
      if ( userAgent.contains( "Firefox" ) ) {
        // special content-disposition for firefox browser to support utf8-encoded symbols in filename
        attachment = "attachment; filename*=UTF-8\'\'" + quotedFileName;
      } else {
        attachment = "attachment; filename=" + quotedFileName;
      }
      response =
          Response.ok( streamingOutput, APPLICATION_ZIP + "; charset=UTF-8" )
              .header( "Content-Disposition", attachment ).build();

      return response;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", quotedFileName + " " + e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }

  }

  /**
   * Retrieves the file from the repository as inline. This is mainly used for css or and dependent files for the html
   * document
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @return
   * @throws FileNotFoundException
   */
  @GET
  @Path( "{pathId : .+}/inline" )
  @Produces( WILDCARD )
  // have to accept anything for browsers to work
    public
    Response doGetFileAsInline( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    String path = null;
    RepositoryFile repositoryFile = null;
    // Check if the path is actually and ID
    if ( isPath( pathId ) ) {
      path = idToPath( pathId );
      if ( !isPathValid( path ) ) {
        return Response.status( FORBIDDEN ).build();
      }
      repositoryFile = getRepository().getFile( path );
    } else {
      // Yes path provided is an ID
      repositoryFile = getRepository().getFileById( pathId );
    }

    if ( repositoryFile == null ) {
      // file does not exist or is not readable but we can't tell at this point
      return Response.status( NOT_FOUND ).build();
    }

    // check whitelist acceptance of file (based on extension)
    if ( getWhitelist().accept( repositoryFile.getName() ) == false ) {
      // if whitelist check fails, we can still inline if you have PublishAction, otherwise we're FORBIDDEN
      if ( getPolicy().isAllowed( PublishAction.NAME ) == false ) {
        return Response.status( FORBIDDEN ).build();
      }
    }

    try {
      SimpleRepositoryFileData fileData =
          getRepository().getDataForRead( repositoryFile.getId(), SimpleRepositoryFileData.class );
      final InputStream is = fileData.getInputStream();

      StreamingOutput streamingOutput;
      Response response;

      // copy streaming output
      streamingOutput = new StreamingOutput() {
        public void write( OutputStream output ) throws IOException {
          IOUtils.copy( is, output );
        }
      };

      // create response
      response =
          Response.ok( streamingOutput ).header( "Content-Disposition", "inline; filename=" + repositoryFile.getName() )
              .build();

      return response;
    } catch ( Exception e ) {
      logger.error( Messages.getInstance().getString(
          "FileResource.EXPORT_FAILED", repositoryFile.getName() + " " + e.getMessage() ), e ); //$NON-NLS-1$
      return Response.status( INTERNAL_SERVER_ERROR ).build();
    }

  }

  /**
   * Save the acls of the selected file to the repository
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param acl
   *          Acl of the repository file <code> RepositoryFileAclDto </code>
   * @return
   */

  @PUT
  @Path( "{pathId : .+}/acl" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response setFileAcls( @PathParam( "pathId" ) String pathId, RepositoryFileAclDto acl ) {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    acl.setId( file.getId() );
    // here we remove fake admin role added for display purpose only
    List<RepositoryFileAclAceDto> aces = acl.getAces();
    if ( aces != null ) {
      Iterator<RepositoryFileAclAceDto> it = aces.iterator();
      while(it.hasNext()){
        RepositoryFileAclAceDto ace = it.next();
        if ( !ace.isModifiable() ){
          it.remove();
        }
      }
    }
    getRepoWs().updateAcl( acl );
    return Response.ok().build();
  }

  /**
   * Store content creator of the selected repository file
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param contentCreator
   *          <code> RepositoryFileDto </code>
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/creator" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  public Response setContentCreator( @PathParam( "pathId" ) String pathId, RepositoryFileDto contentCreator ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
      fileMetadata.put( PentahoJcrConstants.PHO_CONTENTCREATOR, contentCreator.getId() );
      getRepository().setFileMetadata( file.getId(), fileMetadata );
      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Retrieves the list of locale map for the selected repository file
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @return
   */
  @GET
  @Path( "{pathId : .+}/locales" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<LocaleMapDto> doGetFileLocales( @PathParam( "pathId" ) String pathId ) {
    List<LocaleMapDto> availableLocales = new ArrayList<LocaleMapDto>();
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    List<PentahoLocale> locales = getRepoWs().getAvailableLocalesForFileById( file.getId() );
    if ( locales != null && !locales.isEmpty() ) {
      for ( PentahoLocale locale : locales ) {
        availableLocales.add( new LocaleMapDto( locale.toString(), null ) );
      }
    }
    return availableLocales;
  }

  /**
   * Retrieve the list of locale properties for a given locale
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param locale
   * @return
   */
  @GET
  @Path( "{pathId : .+}/localeProperties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<StringKeyStringValueDto> doGetLocaleProperties( @PathParam( "pathId" ) String pathId,
      @QueryParam( "locale" ) String locale ) {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    List<StringKeyStringValueDto> keyValueList = new ArrayList<StringKeyStringValueDto>();
    if ( file != null ) {
      Properties properties = getRepoWs().getLocalePropertiesForFileById( file.getId(), locale );
      if ( properties != null && !properties.isEmpty() ) {
        for ( String key : properties.stringPropertyNames() ) {
          keyValueList.add( new StringKeyStringValueDto( key, properties.getProperty( key ) ) );
        }
      }
    }
    return keyValueList;
  }

  /**
   * Save list of locale properties for a given locale
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param locale
   * @param properties
   *          list of <code> StringKeyStringValueDto </code>
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/localeProperties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doSetLocaleProperties( @PathParam( "pathId" ) String pathId, @QueryParam( "locale" ) String locale,
      List<StringKeyStringValueDto> properties ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
      Properties fileProperties = new Properties();
      if ( properties != null && !properties.isEmpty() ) {
        for ( StringKeyStringValueDto dto : properties ) {
          fileProperties.put( dto.getKey(), dto.getValue() );
        }
      }
      getRepoWs().setLocalePropertiesForFileByFileId( file.getId(), locale, fileProperties );

      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  /**
   * Delete the locale for the selected file and locale
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param locale
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/deleteLocale" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doDeleteLocale( @PathParam( "pathId" ) String pathId, @QueryParam( "locale" ) String locale ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
      getRepoWs().deleteLocalePropertiesForFile( file.getId(), locale );

      return Response.ok().build();
    } catch ( Throwable t ) {
      return Response.serverError().entity( t.getMessage() ).build();
    }
  }

  @GET
  @Path( "/properties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetRootProperties() {
    return getRepoWs().getFile( PATH_SEPARATOR );
  }

  /**
   * Checks whether the current user has permissions to the selected files
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param permissions
   *          (list of permissions to be checked)
   * @return
   */
  @GET
  @Path( "{pathId : .+}/canAccessMap" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<Setting> doGetCanAccessList( @PathParam( "pathId" ) String pathId,
      @QueryParam( "permissions" ) String permissions ) {
    StringTokenizer tokenizer = new StringTokenizer( permissions, "|" );
    ArrayList<Setting> permMap = new ArrayList<Setting>();
    while ( tokenizer.hasMoreTokens() ) {
      Integer perm = Integer.valueOf( tokenizer.nextToken() );
      EnumSet<RepositoryFilePermission> permission = EnumSet.of( RepositoryFilePermission.values()[perm] );
      permMap.add( new Setting( perm.toString(), new Boolean( getRepository()
          .hasAccess( idToPath( pathId ), permission ) ).toString() ) );
    }
    return permMap;
  }

  /**
   * Checks whether the current user has permissions to the provided list of paths
   * 
   * @param pathsWrapper
   *          (list of paths to be checked)
   * @return
   */
  @POST
  @Path( "/pathsAccessList" )
  @Consumes( { APPLICATION_XML, APPLICATION_JSON } )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<Setting> doGetPathsAccessList( StringListWrapper pathsWrapper ) {

    List<Setting> pathsPermissonsSettings = new ArrayList<Setting>();

    String permissions =
        RepositoryFilePermission.READ.ordinal() + "|" + RepositoryFilePermission.WRITE.ordinal() + "|"
            + RepositoryFilePermission.DELETE.ordinal() + "|" + RepositoryFilePermission.ACL_MANAGEMENT.ordinal() + "|"
            + RepositoryFilePermission.ALL.ordinal();

    List<String> paths = pathsWrapper.getStrings();
    for ( String path : paths ) {
      List<Setting> permList = doGetCanAccessList( path, permissions );
      for ( Setting perm : permList ) {
        if ( Boolean.parseBoolean( perm.getValue() ) ) {
          Setting setting = new Setting();
          setting.setName( path );
          setting.setValue( perm.getName() );
          pathsPermissonsSettings.add( setting );
        }
      }
    }
    return pathsPermissonsSettings;
  }

  /**
   * Check whether the current user has specific permission on the selected repository file
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param permissions
   * @return
   */
  @GET
  @Path( "{pathId : .+}/canAccess" )
  @Produces( TEXT_PLAIN )
  public String doGetCanAccess( @PathParam( "pathId" ) String pathId, @QueryParam( "permissions" ) String permissions ) {
    StringTokenizer tokenizer = new StringTokenizer( permissions, "|" );
    List<Integer> permissionList = new ArrayList<Integer>();
    while ( tokenizer.hasMoreTokens() ) {
      Integer perm = Integer.valueOf( tokenizer.nextToken() );
      switch ( perm ) {
        case 0: {
          permissionList.add( RepositoryFilePermission.READ.ordinal() );
          break;
        }
        case 1: {
          permissionList.add( RepositoryFilePermission.WRITE.ordinal() );
          break;
        }
        case 2: {
          permissionList.add( RepositoryFilePermission.DELETE.ordinal() );
          break;
        }
        case 3: {
          permissionList.add( RepositoryFilePermission.ACL_MANAGEMENT.ordinal() );
          break;
        }
        case 4: {
          permissionList.add( RepositoryFilePermission.ALL.ordinal() );
          break;
        }
      }
    }
    return getRepoWs().hasAccess( idToPath( pathId ), permissionList ) ? "true" : "false";
  }

  /**
   * Checks whether the current user can administer the platform
   * 
   * @return ("true" or "false")
   */
  @GET
  @Path( "/canAdminister" )
  @Produces( TEXT_PLAIN )
  public String doGetCanAdminister() {
    return getPolicy().isAllowed( RepositoryReadAction.NAME ) && getPolicy().isAllowed( RepositoryCreateAction.NAME )
        && getPolicy().isAllowed( AdministerSecurityAction.NAME ) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Returns the repository reserved characters
   * 
   * @return list of characters
   */
  @GET
  @Path( "/reservedCharacters" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doGetReservedChars() {
    List<Character> reservedCharacters = getRepoWs().getReservedChars();
    StringBuffer buffer = new StringBuffer();
    for ( int i = 0; i < reservedCharacters.size(); i++ ) {
      buffer.append( reservedCharacters.get( i ) );
      if ( i + 1 < reservedCharacters.size() ) {
        buffer.append( ',' );
      }
    }
    return Response.ok( buffer.toString(), MediaType.APPLICATION_JSON ).build();
  }

  /**
   * Checks whether the current user can create content in the repository
   * 
   * @return
   */
  @GET
  @Path( "/canCreate" )
  @Produces( TEXT_PLAIN )
  public String doGetCanCreate() {
    return getPolicy().isAllowed( RepositoryCreateAction.NAME ) ? "true" : "false"; //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * Retrieves the acls of the selected repository file
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @return <code> RepositoryFileAclDto </code>
   */
  @GET
  @Path( "{pathId : .+}/acl" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileAclDto doGetFileAcl( @PathParam( "pathId" ) String pathId ) {
    RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
    RepositoryFileAclDto fileAcl = getRepoWs().getAcl( file.getId() );
    if ( fileAcl.isEntriesInheriting() ) {
      List<RepositoryFileAclAceDto> aces = getRepoWs().getEffectiveAces( file.getId() );
      fileAcl.setAces( aces, fileAcl.isEntriesInheriting() );
    }
    addAdminRole( fileAcl );
    return fileAcl;
  }
  
  private void addAdminRole( RepositoryFileAclDto fileAcl ){
    String adminRoleName = PentahoSystem.get( String.class, "singleTenantAdminAuthorityName",
        PentahoSessionHolder.getSession() );
    if (fileAcl.getAces() == null ){
      fileAcl.setAces( new LinkedList<RepositoryFileAclAceDto>() );
    }
    for (RepositoryFileAclAceDto facl: fileAcl.getAces()){
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
   * @param pathId
   *          (colon separated path for the repository file)
   * @return file properties object <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/properties" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetProperties( @PathParam( "pathId" ) String pathId ) {
    return getRepoWs().getFile( idToPath( pathId ) );
  }

  /**
   * Retrieves the file by creator id
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @return file properties object <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "{pathId : .+}/creator" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileDto doGetContentCreator( @PathParam( "pathId" ) String pathId ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
      Map<String, Serializable> fileMetadata = getRepository().getFileMetadata( file.getId() );
      String creatorId = (String) fileMetadata.get( PentahoJcrConstants.PHO_CONTENTCREATOR );
      if ( creatorId != null && creatorId.length() > 0 ) {
        return getRepoWs().getFileById( creatorId );
      } else {
        return null;
      }
    } catch ( Throwable t ) {
      return null;
    }
  }

  /**
   * Retrieve the list of executed contents for a selected content from the repository.
   * 
   * @param pathId
   *          (colon separated path for the repository file)
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
   * @param pathId
   *          (colon separated path for the repository file)
   * @param user
   *          (user of the platform)
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
   * @param depth
   *          (how many level should the search go)
   * @param filter
   *          (filter to be applied for search)
   * @param showHidden
   *          (include or exclude hidden files from the file list)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetRootTree( @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return doGetTree( PATH_SEPARATOR, depth, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the list of files from root of the repository.
   * 
   * @param filter
   *          (filter to be applied for search)
   * @return list of files <code> RepositoryFileDto </code>
   */
  @GET
  @Path( "/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetRootChildren( @QueryParam( "filter" ) String filter,
      @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {
    return doGetChildren( PATH_SEPARATOR, filter, showHidden, includeAcls );
  }

  /**
   * Retrieve the children of the selected repository file. This is a recursive search with a selected level depth and
   * filter
   * 
   * @param pathId
   *          The path from the root folder to the root node of the tree to return using colon characters in place of /
   *          or \ characters. To specify /public/Steel Wheels, the encoded pathId would be :public:Steel%20Wheels
   * @param depth
   *          (how many level should the search go)
   * @param filter
   *          (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *          Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *          <p>
   *          File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *          accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *          whether to return files, folders, or both files and folders, respectively.
   *          <p>
   *          The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *          name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *          filter does not apply to root node.
   *          <p>
   *          The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *          metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *          comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *          be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *          Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *          excludeMembers= clause in the same service call.
   *          <p>
   *          Example:
   *          http://localhost:8080/pentaho/api/repo/files/:public:Steel%20Wheels/tree?showHidden=false&filter=*|FILES
   *          |includeMembers=name,fileSize,description,folder,id,title
   *          <p>
   *          will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *          include the name, filesize, description, id and title.
   * 
   * @param showHidden
   *          (include or exclude hidden files from the file list)
   * @param includeAcls
   *          (Include permission information about the file in the output)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/tree" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public RepositoryFileTreeDto doGetTree( @PathParam( "pathId" ) String pathId, @QueryParam( "depth" ) Integer depth,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {

    String path = null;
    if ( pathId == null || pathId.equals( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR;
    } else {
      if ( !pathId.startsWith( PATH_SEPARATOR ) ) {
        path = idToPath( pathId );
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
   * @param pathId
   *          The path from the root folder to the root node of the tree to return using colon characters in place of /
   *          or \ characters. To specify /public/Steel Wheels, the encoded pathId would be :public:Steel%20Wheels
   * @param filter
   *          (filter to be applied for search). The filter can be broken down into 3 parts; File types, Child Node
   *          Filter, and Member Filters. Each part is separated with a pipe (|) character.
   *          <p>
   *          File Types are represented by a word phrase. This phrase is recognized as a file type phrase and processed
   *          accordingly. Valid File Type word phrases include "FILES", "FOLDERS", and "FILES_FOLDERS" and denote
   *          whether to return files, folders, or both files and folders, respectively.
   *          <p>
   *          The Child Node Filter is a list of allowed names of files separated by the pipe (|) character. Each file
   *          name in the filter may be a full name or a partial name with one or more wildcard characters ("*"). The
   *          filter does not apply to root node.
   *          <p>
   *          The Member Filter portion of the filter parameter allows the caller to specify which properties of the
   *          metadata to return. Member Filters start with "includeMembers=" or "excludeMembers=" followed by a list of
   *          comma separated field names that are to be included in, or, excluded from, the list. Valid field names can
   *          be found in <code> org.pentaho.platform.repository2.unified.webservices#RepositoryFileAdapter</code>.
   *          Omission of a member filter will return all members. It is invalid to both and includeMembers= and an
   *          excludeMembers= clause in the same service call.
   *          <p>
   *          Example:
   *          <p>
   *          http://localhost:8080/pentaho/api/repo/files/:public:Steel%20Wheels/children?showHidden=false&filter=*|
   *          FILES |includeMembers=name,fileSize,description,folder,id,title
   *          <p>
   *          will return files but not folders under the "/public/Steel Wheels" folder. The fields returned will
   *          include the name, filesize, description, id and title.
   * 
   * @param showHidden
   *          (include or exclude hidden files from the file list)
   * @param includeAcls
   *          (Include permission information about the file in the output)
   * @return list of files <code> RepositoryFileTreeDto </code>
   */
  @GET
  @Path( "{pathId : .+}/children" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public List<RepositoryFileDto> doGetChildren( @PathParam( "pathId" ) String pathId,
      @QueryParam( "filter" ) String filter, @QueryParam( "showHidden" ) Boolean showHidden,
      @DefaultValue( "false" ) @QueryParam( "includeAcls" ) Boolean includeAcls ) {

    List<RepositoryFileDto> repositoryFileDtoList = new ArrayList<RepositoryFileDto>();
    RepositoryFileDto repositoryFileDto = getRepoWs().getFile( idToPath( pathId ) );

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
  public List<RepositoryFileDto> doGetDeletedFiles() {
    return getRepoWs().getDeletedFiles();
  }

  /**
   * Retrieve the metadata of the selected repository file.
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @return list of <code> StringKeyStringValueDto </code>
   */
  @GET
  @Path( "{pathId : .+}/metadata" )
  @Produces( { APPLICATION_JSON } )
  public List<StringKeyStringValueDto> doGetMetadata( @PathParam( "pathId" ) String pathId ) {
    List<StringKeyStringValueDto> list = null;
    String path = null;
    if ( pathId == null || pathId.equals( PATH_SEPARATOR ) ) {
      path = PATH_SEPARATOR;
    } else {
      if ( !pathId.startsWith( PATH_SEPARATOR ) ) {
        path = idToPath( pathId );
      }
    }
    final RepositoryFileDto file = getRepoWs().getFile( path );
    if ( file != null ) {
      list = getRepoWs().getFileMetadata( file.getId() );
    }
    if ( list != null ) {
      boolean hasSchedulable = false;
      for ( StringKeyStringValueDto value : list ) {
        if ( value.getKey().equals( "_PERM_SCHEDULABLE" ) ) {
          hasSchedulable = true;
          break;
        }
      }
      if ( !hasSchedulable ) {
        StringKeyStringValueDto schedPerm = new StringKeyStringValueDto( "_PERM_SCHEDULABLE", "true" );
        list.add( schedPerm );
      }

      // check file object for hidden value and add it to the list
      list.add( new StringKeyStringValueDto( "_PERM_HIDDEN", String.valueOf( file.isHidden() ) ) );
    }

    return list;
  }

  /**
   * Rename the name of the selected file
   * 
   * @param pathId
   *          (colon separated path for the repository file)
   * @param newName
   *          (New name of the file)
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/rename" )
  @Consumes( { WILDCARD } )
  @Produces( { WILDCARD } )
  public Response doRename( @PathParam( "pathId" ) String pathId, @QueryParam( "newName" ) String newName ) {
    try {
      IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class, PentahoSessionHolder.getSession() );
      RepositoryFile fileToBeRenamed = repository.getFile( idToPath( pathId ) );
      StringBuilder buf = new StringBuilder( fileToBeRenamed.getPath().length() );
      buf.append( getParentPath( fileToBeRenamed.getPath() ) );
      buf.append( RepositoryFile.SEPARATOR );
      buf.append( newName );
      String extension = getExtension( fileToBeRenamed.getName() );
      if ( extension != null ) {
        buf.append( extension );
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
      return processErrorResponse( t.getLocalizedMessage() );
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
   * @param pathId
   *          (colon separated path for the repository file)
   * @param metadata
   *          (list of name value pair of metadata)
   * @return
   */
  @PUT
  @Path( "{pathId : .+}/metadata" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response doSetMetadata( @PathParam( "pathId" ) String pathId, List<StringKeyStringValueDto> metadata ) {
    try {
      RepositoryFileDto file = getRepoWs().getFile( idToPath( pathId ) );
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
    return pathId != null && pathId.contains( ":" );
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
