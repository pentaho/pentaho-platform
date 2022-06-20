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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import com.hitachivantara.security.web.impl.service.util.MultiReadHttpServletRequestWrapper;
import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IServiceOperationAwareContentGenerator;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.MessageFormatUtils;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;
import org.springframework.security.access.AccessDeniedException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenericServlet extends ServletBase {

  private static final long serialVersionUID = 6713118348911206464L;

  private static final Log logger = LogFactory.getLog( GenericServlet.class );
  private static final String CACHE_FILE = "file";
  private static final ICacheManager cache = PentahoSystem.getCacheManager( null );

  private boolean showDeprecationMessage;

  @Nullable
  private final CsrfValidator csrfValidator;

  static {
    if ( cache != null ) {
      cache.addCacheRegion( CACHE_FILE );
    }
  }

  public GenericServlet() {
    this( null );
  }

  public GenericServlet( @Nullable CsrfValidator csrfValidator ) {
    this.csrfValidator = csrfValidator != null ? csrfValidator : PentahoSystem.get( CsrfValidator.class );
  }

  /**
   * Gets the CSRF validator used to validate requests w.r.t CSRF attacks, if any.
   */
  @Nullable
  public CsrfValidator getCsrfValidator() {
    return csrfValidator;
  }

  @Override
  public Log getLogger() {
    return GenericServlet.logger;
  }

  @Override
  public void init() throws ServletException {
    super.init();
    String value = getServletConfig().getInitParameter( "showDeprecationMessage" );
    showDeprecationMessage = Boolean.parseBoolean( value );
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    doGet( request, response );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    if ( showDeprecationMessage ) {
      logDeprecationMessage( request );
    }

    PentahoSystem.systemEntryPoint();

    // BISERVER-2767 - grabbing the current class loader so we can replace it at the end
    ClassLoader origContextClassloader = Thread.currentThread().getContextClassLoader();
    try {
      String pathInfo = request.getPathInfo();
      if ( StringUtils.isEmpty( pathInfo ) ) {
        logger.error( Messages.getInstance().getErrorString( "GenericServlet.ERROR_0005_NO_RESOURCE_SPECIFIED" ) );
        response.sendError( HttpStatus.SC_FORBIDDEN );
        return;
      }

      String contentGeneratorId;
      String contentGeneratorCmd;
      String path = pathInfo.substring( 1 );
      int slashPos = path.indexOf( '/' );
      if ( slashPos != -1 ) {
        // Index is measured in `path`, but then used in `pathInfo`...
        // In practice, the following should be equivalent to `path.substring( slashPos )`...
        contentGeneratorCmd = pathInfo.substring( slashPos + 1 );
        contentGeneratorId = path.substring( 0, slashPos );
      } else {
        contentGeneratorCmd = null;
        contentGeneratorId = path;
      }

      String urlPath = "content/" + contentGeneratorId;
      if ( PentahoSystem.debug ) {
        debug( "GenericServlet contentGeneratorId=" + contentGeneratorId );
        debug( "GenericServlet urlPath=" + urlPath );
      }

      IPentahoSession session = getPentahoSession( request );
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, session );
      if ( pluginManager == null ) {
        String message = Messages.getInstance()
          .getErrorString( "GenericServlet.ERROR_0001_BAD_OBJECT", IPluginManager.class.getSimpleName() );
        writeErrorText( response, message );
        return;
      }

      String pluginId = pluginManager.getServicePlugin( pathInfo );
      if ( pluginId != null && pluginManager.isStaticResource( pathInfo ) ) {
        handleStaticResource( response, pathInfo, pluginManager, pluginId );
        return;
      }

      handleContentGenerator(
        request,
        response,
        contentGeneratorId,
        contentGeneratorCmd,
        urlPath,
        session,
        pluginManager,
        pluginId );

    } catch ( Exception ex ) {
      handleException( request, response, ex );
    } finally {
      // reset the classloader of the current thread
      Thread.currentThread().setContextClassLoader( origContextClassloader );
      PentahoSystem.systemExitPoint();
    }
  }

  private void logDeprecationMessage( @NonNull HttpServletRequest request ) {
    String deprecationMessage =
      "GenericServlet is deprecated and should no longer be handling requests. More detail below..."
        + "\n | You have issued a {0} request to {1} from referer {2} "
        + "\n | Please consider using one of the following REST services instead:"
        + "\n | * GET /api/repos/<pluginId>/<path> to read files from a plugin public dir"
        + "\n | * POST|GET /api/repos/<pathId>/generatedContent to create content resulting from execution of a "
        + "repo file"
        + "\n | * POST|GET /api/repos/<pluginId>/<contentGeneratorId> to execute a content generator by name (RPC "
        + "compatibility service)"
        + "\n \\ To turn this message off, set init-param 'showDeprecationMessage' to false in the GenericServlet "
        + "declaration"
        + "";
    String referrer = StringUtils.defaultString( request.getHeader( "Referer" ), "" );
    logger.warn( MessageFormat.format( deprecationMessage, request.getMethod(), request.getRequestURL(), referrer ) );
  }

  private void handleStaticResource( @NonNull HttpServletResponse response,
                                     @NonNull String pathInfo,
                                     @NonNull IPluginManager pluginManager,
                                     @NonNull String pluginId ) throws IOException {

    boolean cacheOn = "true".equals( pluginManager.getPluginSetting( pluginId, "settings/cache", "false" ) );
    String maxAge = (String) pluginManager.getPluginSetting( pluginId, "settings/max-age", null );
    allowBrowserCache( maxAge, response );

    String mimeType = MimeHelper.getMimeTypeFromFileName( pathInfo );
    if ( mimeType != null ) {
      response.setContentType( mimeType );
    }
    OutputStream out = response.getOutputStream();

    // Do we have this resource cached?
    ByteArrayOutputStream byteStream = null;
    if ( cacheOn ) {
      byteStream = (ByteArrayOutputStream) cache.getFromRegionCache( CACHE_FILE, pathInfo );
    }

    if ( byteStream != null ) {
      IOUtils.write( byteStream.toByteArray(), out );
      return;
    }

    InputStream resourceStream = pluginManager.getStaticResource( pathInfo );
    if ( resourceStream != null ) {
      try {
        byteStream = new ByteArrayOutputStream();
        IOUtils.copy( resourceStream, byteStream );

        // if cache is enabled, drop file in cache
        if ( cacheOn ) {
          cache.putInRegionCache( CACHE_FILE, pathInfo, byteStream );
        }

        // write it out
        IOUtils.write( byteStream.toByteArray(), out );
        return;
      } finally {
        IOUtils.closeQuietly( resourceStream );
      }
    }

    logger.error( Messages.getInstance().getErrorString(
      "GenericServlet.ERROR_0004_RESOURCE_NOT_FOUND", pluginId, pathInfo ) );
    response.sendError( HttpStatus.SC_NOT_FOUND );
  }

  private void handleContentGenerator( @NonNull HttpServletRequest request,
                                       @NonNull HttpServletResponse response,
                                       @NonNull String contentGeneratorId,
                                       @Nullable String contentGeneratorCmd,
                                       @NonNull String urlPath,
                                       @Nullable IPentahoSession session,
                                       @NonNull IPluginManager pluginManager,
                                       @Nullable String pluginId ) throws Exception {

    // Content generators defined in plugin.xml are registered with 2 aliases, one is the id, the other is type
    // so, we can still retrieve a content generator by id, even though this is not the correct way to find it.
    // The correct way is to look up a content generator by pluginManager.getContentGenerator(type, perspectiveName)
    IContentGenerator contentGenerator = (IContentGenerator) pluginManager.getBean( contentGeneratorId );
    if ( contentGenerator == null ) {
      String message = Messages.getInstance()
        .getErrorString( "GenericServlet.ERROR_0002_BAD_GENERATOR", Encode.forHtml( contentGeneratorId ) );
      writeErrorText( response, message );
      return;
    }

    // Set the classloader of the current thread to the class loader of the plugin
    // so that it can load its libraries.
    // Note: we cannot ask the contentGenerator class for it's classloader, since the cg may
    // actually be a proxy object loaded by main the WebAppClassloader.
    Thread.currentThread().setContextClassLoader( pluginManager.getClassLoader( pluginId ) );

    // Wrapping the request upfront ensures that `validateCsrf` is able to read the token parameter from the
    // request body, if needed, despite the `createParameterProviders` call, which "gets" the request's input stream.
    HttpServletRequestWrapper requestWrapper = MultiReadHttpServletRequestWrapper.wrap( request );

    Map<String, IParameterProvider> parameterProviders =
      createParameterProviders( requestWrapper, response, session, contentGeneratorCmd );

    if ( !validateCsrf( requestWrapper, response, contentGenerator, contentGeneratorCmd, parameterProviders ) ) {
      // Response already handled.
      return;
    }

    response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );

    IOutputHandler outputHandler = getOutputHandler( response, true );
    outputHandler.setMimeTypeListener( new HttpMimeTypeListener( requestWrapper, response ) );

    IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
    assert requestContext != null;
    SimpleUrlFactory urlFactory = new SimpleUrlFactory( requestContext.getContextPath() + urlPath + "?" );

    contentGenerator.setOutputHandler( outputHandler );
    contentGenerator.setMessagesList( new ArrayList<>() );
    contentGenerator.setParameterProviders( parameterProviders );
    contentGenerator.setSession( session );
    contentGenerator.setUrlFactory( urlFactory );

    contentGenerator.createContent();
    if ( PentahoSystem.debug ) {
      debug( "Generic Servlet content generate successfully" );
    }
  }

  @NonNull
  private Map<String, IParameterProvider> createParameterProviders( @NonNull HttpServletRequest request,
                                                                    @NonNull HttpServletResponse response,
                                                                    @Nullable IPentahoSession session,
                                                                    @Nullable String contentGeneratorCmd )
    throws IOException {

    // Request Parameters
    IParameterProvider requestParameters = new HttpRequestParameterProvider( request );

    // Path Parameters
    SimpleParameterProvider pathParams = new SimpleParameterProvider();
    if ( contentGeneratorCmd != null ) {
      pathParams.setParameter( "path", contentGeneratorCmd );
    }

    pathParams.setParameter( "query", request.getQueryString() );
    pathParams.setParameter( "contentType", request.getContentType() );
    pathParams.setParameter( "inputstream", request.getInputStream() );
    pathParams.setParameter( "httpresponse", response );
    pathParams.setParameter( "httprequest", request );
    pathParams.setParameter( "remoteaddr", request.getRemoteAddr() );

    // Header Parameters
    // TODO make doing the HTTP headers configurable per content generator
    SimpleParameterProvider headerParams = new SimpleParameterProvider();
    Enumeration<String> names = request.getHeaderNames();
    while ( names.hasMoreElements() ) {
      String name = names.nextElement();
      String value = request.getHeader( name );
      headerParams.setParameter( name, value );
    }

    // Session Parameters
    IParameterProvider sessionParameters = new HttpSessionParameterProvider( session );

    Map<String, IParameterProvider> parameterProviders = new HashMap<>();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
    parameterProviders.put( "headers", headerParams );
    parameterProviders.put( "path", pathParams );

    return parameterProviders;
  }

  @Nullable
  private boolean validateCsrf( @NonNull HttpServletRequestWrapper requestWrapper,
                                @NonNull HttpServletResponse response,
                                @NonNull IContentGenerator contentGenerator,
                                @Nullable String contentGeneratorCmd,
                                @NonNull Map<String, IParameterProvider> parameterProviders )
    throws IOException, ServletException {

    if ( csrfValidator != null ) {
      String operationName = null;

      // Allow the content generator to override the default operation name, in case it is derived using
      // a custom method.
      if ( contentGenerator instanceof IServiceOperationAwareContentGenerator ) {
        contentGenerator.setParameterProviders( parameterProviders );
        operationName = ( (IServiceOperationAwareContentGenerator) contentGenerator ).getServiceOperationName();
      } else if ( StringUtils.isNotEmpty( contentGeneratorCmd ) ) {
        // Remove leading slash
        assert contentGeneratorCmd.charAt( 0 ) == '/';
        operationName = contentGeneratorCmd.substring( 1 );
      }

      Class<? extends IContentGenerator> implementationClass = contentGenerator.getClass();
      Method implementationMethod;
      try {
        implementationMethod = implementationClass.getMethod( "createContent" );
      } catch ( NoSuchMethodException ex ) {
        // Should not happen.
        throw new IllegalArgumentException( ex );
      }

      try {
        csrfValidator.validateRequestOfOperation( requestWrapper, implementationMethod, operationName );
      } catch ( AccessDeniedException ex ) {
        response.sendError( HttpStatus.SC_FORBIDDEN );
        return false;
      }
    }

    return true;
  }

  private void handleException( @NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Exception ex ) throws IOException {
    StringBuffer buffer = new StringBuffer();
    error( Messages.getInstance()
      .getErrorString( "GenericServlet.ERROR_0002_BAD_GENERATOR", request.getQueryString() ), ex );

    List<String> errorList = new ArrayList<>();
    String msg = ex.getMessage();
    errorList.add( msg );
    MessageFormatUtils.formatFailureMessage( "text/html", null, buffer, errorList );
    response.getOutputStream().write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );
  }

  @Deprecated
  protected void allowBrowserCache( String maxAge, IParameterProvider pathParams ) {
    HttpServletResponse response = (HttpServletResponse) pathParams.getParameter( "httpresponse" );
    allowBrowserCache( maxAge, response );
  }

  protected void allowBrowserCache( String maxAge, HttpServletResponse response ) {
    if ( maxAge == null || "0".equals( maxAge ) ) {
      return;
    }

    if ( response != null ) {
      response.setHeader( "Cache-Control", "max-age=" + maxAge );
    }
  }

  protected IOutputHandler getOutputHandler( HttpServletResponse response, boolean allowFeedback ) throws IOException {
    OutputStream out = response.getOutputStream();
    return new HttpOutputHandler( response, out, allowFeedback );
  }

  private void writeErrorText( @NonNull HttpServletResponse response, String message ) throws IOException {
    error( message );
    response.getOutputStream().write( message.getBytes() );
  }
}
