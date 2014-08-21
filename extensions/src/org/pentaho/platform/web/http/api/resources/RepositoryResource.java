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

import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.engine.PluginBeanException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryDownloadWhitelist;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.RepositoryPathEncoder;
import org.pentaho.platform.web.http.api.resources.services.FileService;
import org.pentaho.platform.web.http.api.resources.services.RepositoryService;
import org.pentaho.platform.web.http.api.resources.utils.FileUtils;
import org.pentaho.platform.web.http.messages.Messages;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;

@Path( "/repos" )
public class RepositoryResource extends AbstractJaxRSResource {

  protected IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

  private static final Log logger = LogFactory.getLog( RepositoryResource.class );

  protected IUnifiedRepository repository = PentahoSystem.get( IUnifiedRepository.class );

  protected RepositoryService repositoryService = new RepositoryService();

  protected FileService fileService = new FileService();

  @GET
  @Path( "{pathId : .+}/content" )
  @Produces( { WILDCARD } )
  public Response doGetFileOrDir( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException {
    FileResource fileResource = new FileResource( httpServletResponse );
    fileResource.setWhitelist( getWhitelist() );
    return fileResource.doGetFileOrDir( pathId );
  }

  /**
   * Takes a pathId to a file and generates a URI that represents the URL to call to generate content from that file.
   *
   * @param pathId @param pathId <pre function="syntax.xml"> :path:to:file:id </pre>
   * @return URI that represents a forwarding URL to execute to generate content from the file (pathId)
   * @throws FileNotFoundException, MalformedURLException, URISyntaxException
   */
  @GET
  @Path( "{pathId : .+}/default" )
  @Produces( { WILDCARD } )
  public Response doExecuteDefault( @PathParam( "pathId" ) String pathId ) throws FileNotFoundException,
    MalformedURLException, URISyntaxException {
    return buildSeeOtherResponse( repositoryService.doExecuteDefault( pathId, httpServletRequest.getRequestURL(),
      httpServletRequest.getQueryString() ) );
  }

  /**
   * Services a HTTP form POST request for a resource identified by the compound key <code>contextId</code>, and
   * <code>resourceId</code>
   * <p/>
   * <code>contextId</code> may be one of:
   * <p/>
   * A. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br> B. repository
   * file extension, e.g. <code>prpt</code><br> C. plugin id</li>
   * <p/>
   * <code>resourceId</code> may be one of:
   * <p/>
   * 1. static file residing in a publicly visible plugin folder</br> 2. repository file id (colon-delimited path), e.g.
   * <code>:public:steel-wheels:sales.prpt</code></br> 3. content generator id</br>
   * <p/>
   * The resolution order is as follows, the first to find the resource wins: <ol> <li>A1</li> <li>A2</li> <li>A3</li>
   * <li>B1</li> <li>B3</li> <li>C1</li> <li>C3</li> </ol>
   *
   * @param contextId  identifies the context in which the resource should be retrieved <pre function="syntax.xml">
   *                   <p/>
   *                   </pre>
   * @param resourceId identifies a resource to be retrieved <pre function="syntax.xml">
   *                   <p/>
   *                   </pre>
   * @param formParams any arguments needed to render the resource <pre function="syntax.xml">
   *                   <p/>
   *                   </pre>
   * @return a JAX-RS {@link Response}, in many cases, this will trigger a streaming operation <b>after</b> it it is
   * returned to the caller
   */
  @Path( "/{contextId}/{resourceId : .+}" )
  @POST
  @Consumes( APPLICATION_FORM_URLENCODED )
  @Produces( { WILDCARD } )
  public Response doFormPost( @PathParam( "contextId" ) String contextId, @PathParam( "resourceId" ) String resourceId,
                              final MultivaluedMap<String, String> formParams )
    throws ObjectFactoryException, PluginBeanException,
    IOException, URISyntaxException {

    httpServletRequest = correctPostRequest( formParams );

    if ( logger.isDebugEnabled() ) {
      for ( Object key : httpServletRequest.getParameterMap().keySet() ) {
        logger.debug( "param [" + key + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService( contextId, resourceId );
  }

  /**
   * Services a HTTP GET request for a resource identified by the compound key <code>contextId</code>, and
   * <code>resourceId</code>. Any HTTP request params are pulled from the GET request and used in rendering the
   * resource.
   * <p/>
   * <code>contextId</code> may be one of:
   * <p/>
   * A. repository file id (colon-delimited path), e.g. <code>:public:steel-wheels:sales.prpt</code></br> B. repository
   * file extension, e.g. <code>prpt</code><br> C. plugin id</li>
   * <p/>
   * <code>resourceId</code> may be one of:
   * <p/>
   * 1. static file residing in a publicly visible plugin folder</br> 2. repository file id (colon-delimited path), e.g.
   * <code>:public:steel-wheels:sales.prpt</code></br> 3. content generator id</br>
   * <p/>
   * The resolution order is as follows, the first to find the resource wins: <ol> <li>A1</li> <li>A2</li> <li>A3</li>
   * <li>B1</li> <li>B3</li> <li>C1</li> <li>C3</li> </ol>
   *
   * @param contextId  identifies the context in which the resource should be retrieved
   *                   <pre function="syntax.xml">
   *                   </pre>
   * @param resourceId identifies a resource to be retrieved
   *                   <pre function="syntax.xml">
   *                   </pre>
   * @return a JAX-RS {@link Response}, in many cases, this will trigger a streaming operation <b>after</b> it it is
   * returned to the caller
   */
  @Path( "/{contextId}/{resourceId : .+}" )
  @GET
  @Produces( { WILDCARD } )
  public Response doGet( @PathParam( "contextId" ) String contextId, @PathParam( "resourceId" ) String resourceId )
    throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    if ( logger.isDebugEnabled() ) {
      for ( Object key : httpServletRequest.getParameterMap().keySet() ) {
        logger.debug( "param [" + key + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    return doService( contextId, resourceId );
  }

  /**
   * Retrieves the list of supported content type in the platform
   *
   * @return list of <code> ExecutableFileTypeDto </code>
   */
  @Path( "/executableTypes" )
  @GET
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  public Response getExecutableTypes() {
    return new SystemResource().getExecutableTypes();
  }

  protected Response doService( String contextId, String resourceId )
    throws ObjectFactoryException, PluginBeanException, IOException, URISyntaxException {

    ctxt( "Is [{0}] a repository file id?", contextId ); //$NON-NLS-1$
    if ( contextId.startsWith( ":" ) || contextId.matches( "^[A-z]::.*" ) ) { //$NON-NLS-1$
      //
      // The context is a repository file (A)
      //

      final RepositoryFile file = repository.getFile( FileUtils.idToPath( contextId ) );
      if ( file == null ) {
        logger.error( MessageFormat.format( "Repository file [{0}] not found", contextId ) );
        return Response.serverError().build();
      }

      Response response = null;

      ctxt( "Yep, [{0}] is a repository file id", contextId ); //$NON-NLS-1$
      final String ext = RepositoryFilenameUtils.getExtension( file.getName() );
      String pluginId = pluginManager.getPluginIdForType( ext );
      if ( pluginId == null ) {

        // A.3.a (faux content generator for .url files)
        response = getUrlResponse( file, resourceId );
        if ( response != null ) {
          return response;
        } else {
          logger.error( MessageFormat.format( "No plugin was found to service content of type [{0}]", ext ) );
          return Response.serverError().build();
        }
      }

      // A.1.
      response = getPluginFileResponse( pluginId, resourceId );
      if ( response != null ) {
        return response;
      }

      // A.2.
      response = getRepositoryFileResponse( file.getPath(), resourceId );
      if ( response != null ) {
        return response;
      }

      // A.3.b (real content generator)
      CGFactory fac = new RepositoryFileCGFactory( resourceId, file );
      response = getContentGeneratorResponse( fac );
      if ( response != null ) {
        return response;
      }

    } else {
      ctxt( "Nope, [{0}] is not a repository file id", contextId ); //$NON-NLS-1$
      ctxt( "Is [{0}] is a repository file extension?", contextId ); //$NON-NLS-1$
      String pluginId = pluginManager.getPluginIdForType( contextId );
      if ( pluginId != null ) {
        //
        // The context is a file extension (B)
        //
        ctxt( "Yep, [{0}] is a repository file extension", contextId ); //$NON-NLS-1$

        // B.1.
        Response response = getPluginFileResponse( pluginId, resourceId );
        if ( response != null ) {
          return response;
        }

        // B.3.
        CGFactory fac = new ContentTypeCGFactory( resourceId, contextId );
        response = getContentGeneratorResponse( fac );
        if ( response != null ) {
          return response;
        }
      } else {
        ctxt( "Nope, [{0}] is not a repository file extension", contextId ); //$NON-NLS-1$
        ctxt( "Is [{0}] is a plugin id?", contextId ); //$NON-NLS-1$
        if ( pluginManager.getRegisteredPlugins().contains( contextId ) ) {
          //
          // The context is a plugin id (C)
          //
          ctxt( "Yep, [{0}] is a plugin id", contextId ); //$NON-NLS-1$
          pluginId = contextId;

          // C.1.
          Response response = getPluginFileResponse( pluginId, resourceId );
          if ( response != null ) {
            return response;
          }

          // C.3.
          CGFactory fac = new DirectCGFactory( resourceId, contextId );
          response = getContentGeneratorResponse( fac );
          if ( response != null ) {
            return response;
          }
        } else {
          ctxt( "Nope, [{0}] is not a plugin id", contextId ); //$NON-NLS-1$
          logger.warn( MessageFormat.format( "Failed to resolve context [{0}]", contextId ) ); //$NON-NLS-1$
        }
      }
    }

    logger.warn( MessageFormat.format( "End of the resolution chain. No resource [{0}] found in context [{1}].",
      resourceId, contextId ) );
    return Response.status( NOT_FOUND ).build();
  }

  abstract class CGFactory implements RepositoryService.CGFactoryInterface {
    String contentGeneratorId;

    String command;

    public CGFactory( String contentGeneratorPath ) {
      if ( contentGeneratorPath.contains( "/" ) ) { //$NON-NLS-1$
        contentGeneratorId = contentGeneratorPath.substring( 0, contentGeneratorPath.indexOf( '/' ) );
        command = contentGeneratorPath.substring( contentGeneratorPath.indexOf( '/' ) + 1 );
        debug( "decomposing path [{0}] into content generator id [{1}] and command [{2}]", contentGeneratorPath,
          //$NON-NLS-1$
          contentGeneratorId, command );
      } else {
        contentGeneratorId = contentGeneratorPath;
      }
    }

    @Override
    public String getContentGeneratorId() {
      return contentGeneratorId;
    }

    @Override
    public String getCommand() {
      return command;
    }
  }

  class RepositoryFileCGFactory extends ContentTypeCGFactory {
    RepositoryFile file;

    public RepositoryFileCGFactory( String contentGeneratorPath, RepositoryFile file ) {
      super( contentGeneratorPath, file.getName().substring( file.getName().lastIndexOf( '.' ) + 1 ) );
      this.file = file;
    }

    @Override
    public GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
        file, command );
    }
  }

  class ContentTypeCGFactory extends CGFactory {
    String repoFileExt;

    public ContentTypeCGFactory( String contentGeneratorPath, String repoFileExt ) {
      super( contentGeneratorPath );
      this.repoFileExt = repoFileExt;
    }

    @Override
    public IContentGenerator create() {
      return pluginManager.getContentGenerator( repoFileExt, contentGeneratorId );
    }

    @Override
    public GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
        null, command );
    }

    @Override
    public String getServicingFileType() {
      return repoFileExt;
    }

    @Override
    public String getPluginId() {
      return PentahoSystem.get( IPluginManager.class ).getPluginIdForType( repoFileExt );
    }
  }

  class DirectCGFactory extends CGFactory {
    String pluginId;

    public DirectCGFactory( String contentGeneratorPath, String pluginId ) {
      super( contentGeneratorPath );
      this.pluginId = pluginId;
    }

    @Override
    public IContentGenerator create() {
      return pluginManager.getContentGenerator( null, contentGeneratorId );
    }

    @Override
    public GeneratorStreamingOutput getStreamingOutput( IContentGenerator cg ) {
      return new GeneratorStreamingOutput( cg, this, httpServletRequest, httpServletResponse, acceptableMediaTypes,
        null, command );
    }

    @Override
    public String getServicingFileType() {
      return null;
    }

    @Override
    public String getPluginId() {
      return pluginId;
    }

  }

  protected Response getUrlResponse( RepositoryFile file, String resourceId )
    throws MalformedURLException, URISyntaxException {
    return Response.seeOther( ( new URL( repositoryService.getUrl( file, resourceId ) ) ).toURI() ).build();
  }

  protected Response getContentGeneratorResponse( CGFactory fac ) {
    return Response.ok( repositoryService.getContentGeneratorStreamingOutput( fac ) ).build();
  }

  protected Response getPluginFileResponse( String pluginId, String filePath ) throws IOException {
    rsc( "Is [{0}] a path to a plugin file?", filePath ); //$NON-NLS-1$
    if ( pluginManager.isPublic( pluginId, filePath ) ) {
      PluginResource pluginResource = new PluginResource( httpServletResponse );
      Response readFileResponse = pluginResource.readFile( pluginId, filePath );
      // TODO: should we assume forbidden means move on in the resolution chain, or abort??
      if ( readFileResponse.getStatus() != Response.Status.NOT_FOUND.getStatusCode() ) {
        rsc( "Yep, [{0}] is a path to a static plugin file", filePath ); //$NON-NLS-1$
        return readFileResponse;
      }
    }
    rsc( "Nope, [{0}] is not a path to a static plugin file", filePath ); //$NON-NLS-1$
    return null;
  }

  protected Response getRepositoryFileResponse( String filePath, String relPath ) throws IOException {
    FileResource fileResource = new FileResource( httpServletResponse );
    String path = repositoryService.getRepositoryFileResourcePath( fileResource, filePath, relPath );

    try {
      FileService.RepositoryFileToStreamWrapper wrapper =
        fileService.doGetFileOrDir( RepositoryPathEncoder.encodeRepositoryPath( path ).substring( 1 ) );

      Response response = Response.ok( wrapper.getOutputStream(), wrapper.getMimetype() ).header( "Content-Disposition",
        "inline; filename=\"" + wrapper.getRepositoryFile().getName() + "\"" ).build();

      if ( response.getStatus() != Response.Status.NOT_FOUND.getStatusCode() ) {
        rsc( "Yep, [{0}] is a repository file", path ); //$NON-NLS-1$
        return response;
      }

      rsc( "Nope, [{0}] is not a repository file", path ); //$NON-NLS-1$
      return null;
    } catch ( FileNotFoundException fileNotFound ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), fileNotFound );
      return Response.status( NOT_FOUND ).build();
    } catch ( IllegalArgumentException illegalArgument ) {
      logger.error( Messages.getInstance().getString( "SystemResource.GENERAL_ERROR" ), illegalArgument );
      return Response.status( FORBIDDEN ).build();
    }
  }

  private void ctxt( String msg, Object... args ) {
    repositoryService.ctxt( msg, args );
  }

  private void rsc( String msg, Object... args ) {
    repositoryService.rsc( msg, args );
  }

  private void debug( String msg, Object... args ) {
    repositoryService.debug( msg, args );
  }

  protected String extractUrl( RepositoryFile file ) {
    return repositoryService.extractUrl( file );
  }

  public RepositoryDownloadWhitelist getWhitelist() {
    return repositoryService.getWhitelist();
  }

  public void setWhitelist( RepositoryDownloadWhitelist whitelist ) {
    repositoryService.setWhitelist( whitelist );
  }

  protected Response buildSeeOtherResponse( URI location ) {
    return Response.seeOther( location ).build();
  }

  protected HttpServletRequest correctPostRequest( MultivaluedMap<String, String> formParams ) {
    return JerseyUtil.correctPostRequest( formParams, httpServletRequest );
  }
}
