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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IContentGenerator;
import org.pentaho.platform.api.engine.IOutputHandler;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.engine.core.solution.SimpleParameterProvider;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;
import org.pentaho.platform.web.http.HttpMimeTypeListener;
import org.pentaho.platform.web.http.HttpOutputHandler;
import org.pentaho.platform.web.http.api.resources.GeneratorStreamingOutputProvider.MimeTypeCallback;
import org.pentaho.platform.web.http.request.HttpRequestParameterProvider;
import org.pentaho.platform.web.http.session.HttpSessionParameterProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorStreamingOutput {

  private static final Log logger = LogFactory.getLog( GeneratorStreamingOutput.class );

  private static final Log mimeTypeLogger = LogFactory.getLog( "MIME_TYPE" ); //$NON-NLS-1$

  protected IContentGenerator contentGenerator;

  protected String contentGeneratorID;

  protected RepositoryFile file;

  protected String command;

  protected HttpServletRequest httpServletRequest;

  protected HttpServletResponse httpServletResponse;

  protected IPluginManager pluginMgr;

  protected String fileType;

  protected String mimeType;

  protected List<MediaType> acceptableMediaTypes = new ArrayList<MediaType>();

  protected ContentGeneratorDescriptor contentGeneratorDescriptor;

  private static final boolean MIMETYPE_MUTABLE = true;

  /**
   * Invokes a content generator to produce some content either in the context of a repository file, or in the form of a
   * direct service call (no repository file in view).
   * 
   * @param contentGenerator
   *          the content generator to invoke
   * @param ContentGeneratorDescriptor
   *          a descriptor detailing info about the content generator
   * @param request
   *          the HTTP request
   * @param response
   *          the HTTP response
   * @param producesMimeType
   *          the requested return type of the output (can be null if none is preferred)
   * @param file
   *          the repository file being rendered (can be null if a repository file does not apply)
   * @param command
   *          the trailing part of the URL path of the request, typically used as a command sequence (can be null)
   */
  public GeneratorStreamingOutput( IContentGenerator contentGenerator, ContentGeneratorDescriptor desc,
      HttpServletRequest request, HttpServletResponse response, List<MediaType> acceptableMediaTypes,
      RepositoryFile file, String command ) {
    if ( contentGenerator == null ) {
      throw new IllegalArgumentException( "contentGenerator cannot be null" );
    }
    this.contentGenerator = contentGenerator;
    this.contentGeneratorDescriptor = desc;
    this.command = command;
    this.contentGeneratorID = desc.getContentGeneratorId();
    this.fileType = desc.getServicingFileType();
    this.file = file;
    mimeTrace( "Request is requiring content generator to return content of type [{0}]", acceptableMediaTypes
        .toString() );
    if ( acceptableMediaTypes != null ) {
      this.acceptableMediaTypes = acceptableMediaTypes;
    }
    this.httpServletRequest = request;
    this.httpServletResponse = response;
    pluginMgr = PentahoSystem.get( IPluginManager.class );

    // if command seems like a file path, then use the file extension to determine response mime type, otherwise
    // leave it up to the content generator
    if ( command != null && command.contains( "." ) ) { //$NON-NLS-1$
      String tmpMimeType = MimeHelper.getMimeTypeFromFileName( command );
      if ( tmpMimeType != null ) {
        mimeType = tmpMimeType;
        mimeTrace( "Setting response mime type to [{0}] (based on extension of resource [{1}])", mimeType, command ); //$NON-NLS-1$
      }
    }
  }

  private void mimeTrace( String msg, Object... args ) {
    if ( mimeTypeLogger.isDebugEnabled() ) {
      String prologue =
          MessageFormat.format(
            "<< {3} [MIME TRACE] Content generator id [{0}] for file type [{1}] and resource [{2}]>>: ",
            contentGeneratorID, fileType, command, this );
      mimeTypeLogger.debug( MessageFormat.format( prologue + msg, args ) );
    }
  }

  public void write( OutputStream output, MimeTypeCallback callback ) throws IOException {
    if ( file != null ) {
      fileType = RepositoryFilenameUtils.getExtension( file.getName() );
    }

    try {
      if ( !MIMETYPE_MUTABLE && getMimeType() != null ) {
        // the mime type has been predetermined
        mimeTrace(
            "Return mime type has been pretermined based on the resource addressed in the URI [{0}]."
              + " Forcing content generator to return content of type [{1}]",
            command, getMimeType() );
        callback.setMimeType( getMimeType() );
      }

      generateContent( output, callback );
    } catch ( Exception e ) {
      // logging here because it's the last place we can log to file before
      // the error is streamed back in the http 500 response
      String msg =
          MessageFormat.format( "Error generating content from content generator with id [{0}]", contentGeneratorID );
      logger.error( msg, e );
      throw new IOException( msg, e );
    } finally {
      PentahoSystem.systemExitPoint();
    }
  }

  protected void generateContent( OutputStream outputStream, final MimeTypeCallback callback ) throws Exception {
    try {
      httpServletResponse.setCharacterEncoding( LocaleHelper.getSystemEncoding() );
    } catch ( Throwable t ) {
      logger.warn( "could not set encoding, servlet-api is likely too old.  are we in a unit test?" ); //$NON-NLS-1$
    }

    IOutputHandler outputHandler = new HttpOutputHandler( httpServletResponse, outputStream, true );
    outputHandler.setMimeTypeListener( new HttpMimeTypeListener( httpServletRequest, httpServletResponse ) {
      /*
       * This content generator is setting the mimeType
       */
      @Override
      public void setMimeType( String mimeType ) {
        try {
          if ( !MIMETYPE_MUTABLE && GeneratorStreamingOutput.this.getMimeType() != null ) {
            mimeTrace(
                "Content generator is trying to set response mime type to [{0}], but mime type [{1}] has already been imposed. Content generator request to change mime type will be ignored.", //$NON-NLS-1$
                mimeType, GeneratorStreamingOutput.this.getMimeType() );
            return;
          } else {
            mimeTrace( "Content generator is setting response mime type to [{0}]", mimeType ); //$NON-NLS-1$
            callback.setMimeType( mimeType );
            GeneratorStreamingOutput.this.setMimeType( mimeType );
          }
        } catch ( Throwable th ) {
          mimeTrace( "Failed to set mime type: {0}", th.getMessage() );
          logger.error( MessageFormat.format( "Failed to set mime type: {0}", th.getMessage() ) ); //$NON-NLS-1$
        }
        super.setMimeType( mimeType );
      }
    } );

    Map<String, IParameterProvider> parameterProviders = new HashMap<String, IParameterProvider>();
    parameterProviders.put( IParameterProvider.SCOPE_REQUEST, createRequestParamProvider() );
    parameterProviders.put( IParameterProvider.SCOPE_SESSION, createSessionParameterProvider() );
    parameterProviders.put( "headers", createHeaderParamProvider() ); //$NON-NLS-1$
    parameterProviders.put( "path", createPathParamProvider() ); //$NON-NLS-1$

    String pluginId = contentGeneratorDescriptor.getPluginId();

    IPentahoUrlFactory urlFactory =
        new SimpleUrlFactory( PentahoRequestContextHolder.getRequestContext().getContextPath()
            + "api/repos/" + pluginId + "/" + contentGeneratorID + "?" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    // set the classloader of the current thread to the class loader of
    // the plugin so that it can load its libraries
    // Note: we cannot ask the contentGenerator class for it's classloader,
    // since the cg may
    // actually be a proxy object loaded by main the WebAppClassloader
    ClassLoader origContextClassloader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader( pluginMgr.getClassLoader( pluginId ) );
    try {
      contentGenerator.setOutputHandler( outputHandler );
      contentGenerator.setMessagesList( new ArrayList<String>() );
      contentGenerator.setParameterProviders( parameterProviders );
      contentGenerator.setSession( PentahoSessionHolder.getSession() );
      if ( urlFactory != null ) {
        contentGenerator.setUrlFactory( urlFactory );
      }
      contentGenerator.createContent();
    } finally {
      Thread.currentThread().setContextClassLoader( origContextClassloader );
    }
  }

  protected IParameterProvider createRequestParamProvider() {
    return new HttpRequestParameterProvider( httpServletRequest );
  }

  protected IParameterProvider createSessionParameterProvider() {
    return new HttpSessionParameterProvider( PentahoSessionHolder.getSession() );
  }

  protected IParameterProvider createHeaderParamProvider() {
    SimpleParameterProvider headerParams = new SimpleParameterProvider();
    Enumeration<?> names = httpServletRequest.getHeaderNames();
    while ( names.hasMoreElements() ) {
      String name = (String) names.nextElement();
      String value = httpServletRequest.getHeader( name );
      headerParams.setParameter( name, value );
    }
    return headerParams;
  }

  protected IParameterProvider createPathParamProvider() throws IOException {
    SimpleParameterProvider pathParams = null;
    if ( StringUtils.isEmpty( httpServletRequest.getPathInfo() ) ) {
      httpServletResponse.sendError( 403 );
    } else {
      pathParams = new SimpleParameterProvider();
      pathParams.setParameter( "query", httpServletRequest.getQueryString() ); //$NON-NLS-1$

      List<String> mediaTypes = new ArrayList<String>( acceptableMediaTypes.size() );
      for ( MediaType type : acceptableMediaTypes ) {
        mediaTypes.add( type.toString() );
      }
      pathParams.setParameter( "acceptableMediaTypes", mediaTypes ); //$NON-NLS-1$
      if ( mediaTypes.size() > 0 ) {
        pathParams.setParameter( "contentType", acceptableMediaTypes.get( 0 ) ); //$NON-NLS-1$
      }
      pathParams.setParameter( "inputstream", httpServletRequest.getInputStream() ); //$NON-NLS-1$
      pathParams.setParameter( "httpresponse", httpServletResponse ); //$NON-NLS-1$
      pathParams.setParameter( "httprequest", httpServletRequest ); //$NON-NLS-1$
      pathParams.setParameter( "remoteaddr", httpServletRequest.getRemoteAddr() ); //$NON-NLS-1$
      if ( file != null ) {
        pathParams.setParameter( "path", URLEncoder.encode( file.getPath() , "UTF-8" ) ); //$NON-NLS-1$
        pathParams.setParameter( "file", file ); //$NON-NLS-1$
      }
      if ( command != null ) {
        // path beyond that which matched the GeneratorResource
        pathParams.setParameter( "cmd", command ); //$NON-NLS-1$
      }
    }
    return pathParams;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType( String mimeType ) {
    this.mimeType = mimeType;
  }
}
