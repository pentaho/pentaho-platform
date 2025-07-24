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


package org.pentaho.platform.web.servlet;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericServlet extends ServletBase {

  private static final long serialVersionUID = 6713118348911206464L;

  private static final Log logger = LogFactory.getLog( GenericServlet.class );
  static final String CACHE_FILE = "file";
  private static ICacheManager cache = PentahoSystem.getCacheManager( null );

  /**
   * This pattern is used to match the path in the format of "/content/<static-resource-url>".
   * It assumes the REST resource is being served via its default URL, {@code /content}.
   * Used by {@link #isStaticResource(HttpServletRequest)}.
   */
  private static final Pattern PATH_PATTERN = Pattern.compile( "\\A/content(/.+)\\Z" );

  private boolean showDeprecationMessage;

  static {
    if ( cache != null ) {
      cache.addCacheRegion( CACHE_FILE );
    }
  }

  @Override
  public Log getLogger() {
    return GenericServlet.logger;
  }

  @Override
  public void init() throws ServletException {
    // TODO Auto-generated method stub
    super.init();
    String value = getServletConfig().getInitParameter( "showDeprecationMessage" );
    showDeprecationMessage = Boolean.parseBoolean( value );
  }

  /**
   * Determines if the request is for an existing static resource handled by this content generator handler
   * assuming it's being served via its default URL, {@code /content}.
   * <p>
   * This particular method can be used without performing servlet initialization, via {@link #init()}.
   * <p>
   * The implementation of this method uses the exact same rules used by
   * {@link #doGet(HttpServletRequest, HttpServletResponse)} to identify static resources. The two must be kept in sync.
   *
   * @param request The request.
   * @return {@code true} if the request is for a static resource; {@code false}, otherwise.
   */
  public boolean isStaticResource( @NonNull final HttpServletRequest request ) {
    if ( StringUtils.isEmpty( request.getPathInfo() ) ) {
      return false;
    }

    String fullPath = request.getServletPath() + request.getPathInfo();

    Matcher matcher = PATH_PATTERN.matcher( fullPath );
    if ( !matcher.matches() ) {
      return false;
    }

    // Assume it is a static resource, but then check the hypothesis.
    // Equal in value to getPathInfo() in doGet()
    String staticResourceUrl = matcher.group( 1 );

    IPluginManager pluginManager = getPluginManager( request );
    if ( pluginManager == null ) {
      return false;
    }

    // Determine the plugin from the static resource URL, and then double check it is a static resource.
    // Must use the deprecated methods to have the exact same behavior as the doGet() method.
    @SuppressWarnings( {"deprecation"} )
    String pluginId = pluginManager.getServicePlugin( staticResourceUrl );
    if ( pluginId == null ) {
      return false;
    }

    @SuppressWarnings( "deprecation" )
    boolean isStaticResourceUrl = pluginManager.isStaticResource( staticResourceUrl );

    return isStaticResourceUrl && doesStaticResourceExist( pluginManager, pluginId, staticResourceUrl );
  }

  /**
   * Checks if a static resource, given its relative URL actually exists.
   * <p>
   * This method is used by {@link #isStaticResource(HttpServletRequest)} to determine if a static resource actually
   * exists, and not only whether its URL is public.
   * <p>
   * This method uses the deprecated method {@link IPluginManager#getStaticResource(String)} so that it behaves the
   * closest possible to {@link #doGet(HttpServletRequest, HttpServletResponse)}.
   *
   * @param pluginManager     The plugin manager.
   * @param pluginId          The plugin ID.
   * @param staticResourceUrl The static resource URL.
   * @return {@code true} if the static resource exists; {@code false}, otherwise.
   */
  protected boolean doesStaticResourceExist( @NonNull IPluginManager pluginManager,
                                             @NonNull String pluginId,
                                             @NonNull String staticResourceUrl ) {
    // Check the file actually exists.
    boolean cacheOn = isPluginCacheOn( pluginManager, pluginId );

    // Do we have this resource cached?
    if ( cacheOn && getCacheManager().getFromRegionCache( CACHE_FILE, staticResourceUrl ) != null ) {
      return true;
    }

    @SuppressWarnings( "deprecation" )
    InputStream resourceStream = pluginManager.getStaticResource( staticResourceUrl );
    if ( resourceStream != null ) {
      IOUtils.closeQuietly( resourceStream );
      return true;
    }

    return false;
  }

  // visible for testing
  protected ICacheManager getCacheManager() {
    return cache;
  }

  /**
   * Checks if the cache is enabled for the given plugin.
   *
   * @param pluginManager The plugin manager.
   * @param pluginId      The plugin ID.
   * @return {@code true} if the cache is enabled; {@code false}, otherwise.
   */
  protected boolean isPluginCacheOn( @NonNull IPluginManager pluginManager, @NonNull String pluginId ) {
    return "true".equals( pluginManager.getPluginSetting( pluginId, "settings/cache", "false" ) );
  }

  @Override
  protected void doPost( final HttpServletRequest request, final HttpServletResponse response )
    throws ServletException, IOException {
    doGet( request, response );
  }

  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws ServletException,
    IOException {
    if ( showDeprecationMessage ) {
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
      String referer = StringUtils.defaultString( request.getHeader( "Referer" ), "" );
      logger.warn( MessageFormat.format( deprecationMessage, request.getMethod(), request.getRequestURL(), referer ) );
    }

    PentahoSystem.systemEntryPoint();

    IOutputHandler outputHandler = null;
    // BISERVER-2767 - grabbing the current class loader so we can replace it at the end
    ClassLoader origContextClassloader = Thread.currentThread().getContextClassLoader();
    try {
      String servletPath = request.getServletPath();
      String pathInfo = request.getPathInfo();
      String contentGeneratorId = ""; //$NON-NLS-1$
      String urlPath = ""; //$NON-NLS-1$
      SimpleParameterProvider pathParams = new SimpleParameterProvider();
      if ( StringUtils.isEmpty( pathInfo ) ) {
        logger.error(
          Messages.getInstance().getErrorString( "GenericServlet.ERROR_0005_NO_RESOURCE_SPECIFIED" ) ); //$NON-NLS-1$
        response.sendError( 403 );
        return;
      }

      String path = pathInfo.substring( 1 );
      int slashPos = path.indexOf( '/' );
      if ( slashPos != -1 ) {
        pathParams.setParameter( "path", pathInfo.substring( slashPos + 1 ) ); //$NON-NLS-1$
        contentGeneratorId = path.substring( 0, slashPos );
      } else {
        contentGeneratorId = path;
      }
      urlPath = "content/" + contentGeneratorId; //$NON-NLS-1$

      IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
      pathParams.setParameter( "query", request.getQueryString() ); //$NON-NLS-1$
      pathParams.setParameter( "contentType", request.getContentType() ); //$NON-NLS-1$

      InputStream in = request.getInputStream();
      pathParams.setParameter( "inputstream", in ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse", response ); //$NON-NLS-1$
      pathParams.setParameter( "httprequest", request ); //$NON-NLS-1$
      pathParams.setParameter( "remoteaddr", request.getRemoteAddr() ); //$NON-NLS-1$
      if ( PentahoSystem.debug ) {
        debug( "GenericServlet contentGeneratorId=" + contentGeneratorId ); //$NON-NLS-1$
        debug( "GenericServlet urlPath=" + urlPath ); //$NON-NLS-1$
      }

      IPentahoSession session = getPentahoSession( request );
      IPluginManager pluginManager = getPluginManager( request );
      if ( pluginManager == null ) {
        OutputStream out = response.getOutputStream();
        String message =
          Messages.getInstance().getErrorString(
            "GenericServlet.ERROR_0001_BAD_OBJECT", IPluginManager.class.getSimpleName() ); //$NON-NLS-1$
        error( message );
        out.write( message.getBytes() );
        return;
      }

      // TODO make doing the HTTP headers configurable per content generator
      SimpleParameterProvider headerParams = new SimpleParameterProvider();
      Enumeration names = request.getHeaderNames();
      while ( names.hasMoreElements() ) {
        String name = (String) names.nextElement();
        String value = request.getHeader( name );
        headerParams.setParameter( name, value );
      }

      String pluginId = pluginManager.getServicePlugin( pathInfo );

      if ( pluginId != null && pluginManager.isStaticResource( pathInfo ) ) {
        boolean cacheOn = isPluginCacheOn( pluginManager, pluginId );
        String maxAge = (String) pluginManager.getPluginSetting( pluginId, "settings/max-age", null ); //$NON-NLS-1$
        allowBrowserCache( maxAge, pathParams );

        String mimeType = MimeHelper.getMimeTypeFromFileName( pathInfo );
        if ( mimeType != null ) {
          response.setContentType( mimeType );
        }
        OutputStream out = response.getOutputStream();

        // do we have this resource cached?
        ByteArrayOutputStream byteStream = null;

        if ( cacheOn ) {
          byteStream = (ByteArrayOutputStream) getCacheManager().getFromRegionCache( CACHE_FILE, pathInfo );
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
              getCacheManager().putInRegionCache( CACHE_FILE, pathInfo, byteStream );
            }

            // write it out
            IOUtils.write( byteStream.toByteArray(), out );
            return;
          } finally {
            IOUtils.closeQuietly( resourceStream );
          }
        }
        logger.error( Messages.getInstance().getErrorString(
          "GenericServlet.ERROR_0004_RESOURCE_NOT_FOUND", pluginId, pathInfo ) ); //$NON-NLS-1$
        response.sendError( 404 );
        return;
      }

      // content generators defined in plugin.xml are registered with 2 aliases, one is the id, the other is type
      // so, we can still retrieve a content generator by id, even though this is not the correct way to find
      // it. the correct way is to look up a content generator by pluginManager.getContentGenerator(type,
      // perspectiveName)
      IContentGenerator contentGenerator = (IContentGenerator) pluginManager.getBean( contentGeneratorId );
      if ( contentGenerator == null ) {
        OutputStream out = response.getOutputStream();
        String message =
          Messages.getInstance().getErrorString(
            "GenericServlet.ERROR_0002_BAD_GENERATOR",
            Encode.forHtml( contentGeneratorId ) ); //$NON-NLS-1$
        error( message );
        out.write( message.getBytes() );
        return;
      }

      // set the classloader of the current thread to the class loader of
      // the plugin so that it can load its libraries
      // Note: we cannot ask the contentGenerator class for it's classloader, since the cg may
      // actually be a proxy object loaded by main the WebAppClassloader
      Thread.currentThread().setContextClassLoader( pluginManager.getClassLoader( pluginId ) );

      // String proxyClass = PentahoSystem.getSystemSetting( module+"/plugin.xml" ,
      // "plugin/content-generators/"+contentGeneratorId,
      // "content generator not found");
      // see if this is an upload

      // File uploading is a service provided by UploadFileServlet where appropriate protections
      // are in place to prevent uploads that are too large.

      // boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      // if (isMultipart) {
      // requestParameters = new SimpleParameterProvider();
      // // Create a factory for disk-based file items
      // FileItemFactory factory = new DiskFileItemFactory();
      //
      // // Create a new file upload handler
      // ServletFileUpload upload = new ServletFileUpload(factory);
      //
      // // Parse the request
      // List<?> /* FileItem */items = upload.parseRequest(request);
      // Iterator<?> iter = items.iterator();
      // while (iter.hasNext()) {
      // FileItem item = (FileItem) iter.next();
      //
      // if (item.isFormField()) {
      // ((SimpleParameterProvider) requestParameters).setParameter(item.getFieldName(), item.getString());
      // } else {
      // String name = item.getName();
      // ((SimpleParameterProvider) requestParameters).setParameter(name, item.getInputStream());
      // }
      // }
      // }

      response.setCharacterEncoding( LocaleHelper.getSystemEncoding() );

      IMimeTypeListener listener = new HttpMimeTypeListener( request, response );

      outputHandler = getOutputHandler( response, true );
      outputHandler.setMimeTypeListener( listener );

      IParameterProvider sessionParameters = new HttpSessionParameterProvider( session );
      IPentahoRequestContext requestContext = PentahoRequestContextHolder.getRequestContext();
      Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
      parameterProviders.put( IParameterProvider.SCOPE_REQUEST, requestParameters );
      parameterProviders.put( IParameterProvider.SCOPE_SESSION, sessionParameters );
      parameterProviders.put( "headers", headerParams ); //$NON-NLS-1$
      parameterProviders.put( "path", pathParams ); //$NON-NLS-1$
      SimpleUrlFactory urlFactory =
        new SimpleUrlFactory( requestContext.getContextPath() + urlPath + "?" ); //$NON-NLS-1$ //$NON-NLS-2$
      List<String> messages = new ArrayList<String>();
      contentGenerator.setOutputHandler( outputHandler );
      contentGenerator.setMessagesList( messages );
      contentGenerator.setParameterProviders( parameterProviders );
      contentGenerator.setSession( session );
      contentGenerator.setUrlFactory( urlFactory );
      // String contentType = request.getContentType();
      // contentGenerator.setInput(input);
      contentGenerator.createContent();
      if ( PentahoSystem.debug ) {
        debug( "Generic Servlet content generate successfully" ); //$NON-NLS-1$
      }

    } catch ( Exception e ) {
      StringBuffer buffer = new StringBuffer();
      error( Messages.getInstance()
        .getErrorString( "GenericServlet.ERROR_0002_BAD_GENERATOR", request.getQueryString() ), e ); //$NON-NLS-1$
      List errorList = new ArrayList();
      String msg = e.getMessage();
      errorList.add( msg );
      MessageFormatUtils.formatFailureMessage( "text/html", null, buffer, errorList ); //$NON-NLS-1$
      response.getOutputStream().write( buffer.toString().getBytes( LocaleHelper.getSystemEncoding() ) );

    } finally {
      // reset the classloader of the current thread
      Thread.currentThread().setContextClassLoader( origContextClassloader );
      PentahoSystem.systemExitPoint();
    }
  }

  protected void allowBrowserCache( String maxAge, IParameterProvider pathParams ) {
    if ( maxAge == null || "0".equals( maxAge ) ) { //$NON-NLS-1$
      return;
    }
    HttpServletResponse response = (HttpServletResponse) pathParams.getParameter( "httpresponse" ); //$NON-NLS-1$
    if ( response != null ) {
      response.setHeader( "Cache-Control", "max-age=" + maxAge ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  protected IOutputHandler getOutputHandler( HttpServletResponse response, boolean allowFeedback ) throws IOException {
    OutputStream out = response.getOutputStream();
    HttpOutputHandler handler = new HttpOutputHandler( response, out, allowFeedback );
    return handler;
  }

  protected IPluginManager getPluginManager( final HttpServletRequest request ) {
    IPentahoSession session = getPentahoSession( request );
    return PentahoSystem.get( IPluginManager.class, session );
  }
}
