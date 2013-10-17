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

package org.pentaho.platform.web.servlet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.owasp.esapi.ESAPI;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IMessageFormatter;
import org.pentaho.platform.api.engine.IMimeTypeListener;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoRequestContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

public class GenericServlet extends ServletBase {

  private static final long serialVersionUID = 6713118348911206464L;

  private static final Log logger = LogFactory.getLog( GenericServlet.class );
  private static final String CACHE_FILE = "file"; //$NON-NLS-1$
  private static ICacheManager cache = PentahoSystem.getCacheManager( null );

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
      InputStream in = request.getInputStream();
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

      pathParams.setParameter( "query", request.getQueryString() ); //$NON-NLS-1$
      pathParams.setParameter( "contentType", request.getContentType() ); //$NON-NLS-1$
      pathParams.setParameter( "inputstream", in ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse", response ); //$NON-NLS-1$
      pathParams.setParameter( "httprequest", request ); //$NON-NLS-1$
      pathParams.setParameter( "remoteaddr", request.getRemoteAddr() ); //$NON-NLS-1$
      if ( PentahoSystem.debug ) {
        debug( "GenericServlet contentGeneratorId=" + contentGeneratorId ); //$NON-NLS-1$
        debug( "GenericServlet urlPath=" + urlPath ); //$NON-NLS-1$
      }
      IPentahoSession session = getPentahoSession( request );
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, session );
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
        boolean cacheOn = "true".equals( pluginManager
          .getPluginSetting( pluginId, "settings/cache", "false" ) ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
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
            ESAPI.encoder().encodeForHTML( contentGeneratorId ) ); //$NON-NLS-1$
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
      IParameterProvider requestParameters = new HttpRequestParameterProvider( request );
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
      PentahoSystem.get( IMessageFormatter.class, PentahoSessionHolder.getSession() ).formatFailureMessage(
        "text/html", null, buffer, errorList ); //$NON-NLS-1$
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
}
