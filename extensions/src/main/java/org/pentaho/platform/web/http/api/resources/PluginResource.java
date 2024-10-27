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


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.web.http.api.resources.utils.SystemUtils;
import org.pentaho.platform.web.http.messages.Messages;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.WILDCARD;

/**
 * Represents the public files available in a plugin.
 *
 * @author aaron
 *
 */
@Path( "/plugins/{pluginId}" )
public class PluginResource {

  private static final String CACHE_FILE = "file"; //$NON-NLS-1$

  private static ICacheManager cache = PentahoSystem.getCacheManager( null );

  protected File systemFolder;

  protected IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class );

  private static final Log logger = LogFactory.getLog( PluginResource.class );

  protected IPluginManager pluginMgr = PentahoSystem.get( IPluginManager.class );

  static {
    if ( cache != null ) {
      cache.addCacheRegion( CACHE_FILE );
    }
  }

  @Context
  protected HttpServletResponse httpServletResponse;

  public PluginResource() {
    systemFolder = new File( PentahoSystem.getApplicationContext().getSolutionRootPath(), "system" ); //$NON-NLS-1$
  }

  public PluginResource( HttpServletResponse httpServletResponse ) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  protected InputStream getCacheBackedStream( String pluginId, String path, boolean useCache ) throws IOException {
    InputStream inputStream = null;

    final String canonicalPath = pluginId + "/" + path; //$NON-NLS-1$

    if ( useCache ) {
      byte[] bytes = (byte[]) cache.getFromRegionCache( CACHE_FILE, canonicalPath );
      if ( bytes != null ) {
        return new ByteArrayInputStream( bytes );
      }
    }

    ClassLoader loader = pluginManager.getClassLoader( pluginId );
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class );
    inputStream = resLoader.getResourceAsStream( loader, path );

    if ( inputStream == null ) {
      throw new FileNotFoundException( Messages.getInstance()
          .getString( "PluginFileResource.COULD_NOT_READ_FILE", path ) ); //$NON-NLS-1$
    }

    if ( useCache ) {
      // store bytes for next time
      ByteArrayOutputStream bos = null;
      try {
        bos = new ByteArrayOutputStream();
        IOUtils.copy( inputStream, bos );
        byte[] bytes = bos.toByteArray();
        cache.putInRegionCache( CACHE_FILE, canonicalPath, bytes );
        // new InputStream for caller since we just read it (can't call reset() as it's a generic InputStream)
        inputStream = new ByteArrayInputStream( bytes );
      } finally {
        IOUtils.closeQuietly( bos );
      }
    }

    return inputStream;
  }

  /**
   * Retrieve the file from the selected plugin. This file is a static file (i.e javascript, html, css etc)
   *
   * @param pluginId (Plugin ID of the selected Plugin)
   * @param path (Path of the file being retrieved. This is a colon separated path to the plugin file. This
   *        is usually a static file like a javascript, image or html
   * @return
   * @throws IOException
   */
  @GET
  @Path( "/files/{path : .+}" )
  @Produces( WILDCARD )
  @Facet( name = "Unsupported" )
  public Response readFile( @PathParam( "pluginId" ) String pluginId, @PathParam( "path" ) String path )
    throws IOException {
    return readFile( pluginId, path, true );
  }

  /**
   * Retrieve the file from the selected plugin, optionally not affecting the current HTTP response headers.
   * @param pluginId The plugin id.
   * @param path The file path.
   * @param changeResponseHeaders Whether to change the HTTP response headers to reflect aspects such as cache control.
   * @return The response containing the file content.
   */
  public Response readFile( String pluginId, String path, boolean changeResponseHeaders ) throws IOException {
    List<String> pluginRestPerspectives = pluginManager.getPluginRESTPerspectivesForId( pluginId );
    boolean useCache = "true".equals( pluginManager.getPluginSetting( pluginId, "settings/cache", "false" ) ); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

    if ( changeResponseHeaders ) {
      String maxAge = (String) pluginManager.getPluginSetting( pluginId, "settings/max-age", null ); //$NON-NLS-1$
      // //
      // Set browser cache if valid value and if the path is not one of the plugin REST perspectives. (/viewer, /editor,
      // /scheduler)
      if ( !pluginRestPerspectives.contains( path ) && maxAge != null && !"0".equals( maxAge ) ) { //$NON-NLS-1$
        httpServletResponse.setHeader( "Cache-Control", "max-age=" + maxAge ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    if ( !pluginManager.getRegisteredPlugins().contains( pluginId ) ) {
      return Response.status( Status.NOT_FOUND ).build();
    }

    if ( !pluginManager.isPublic( pluginId, path ) ) {
      return Response.status( Status.FORBIDDEN ).build();
    }

    // Only Admin users can access admin-plugin.
    if ( pluginId.equals( "admin-plugin" ) && !SystemUtils.canAdminister() ) {
      return Response.status( Status.FORBIDDEN ).build();
    }

    InputStream isTmp;
    try {
      isTmp = getCacheBackedStream( pluginId, path, useCache );
    } catch ( FileNotFoundException e ) {
      return Response.status( Status.NOT_FOUND ).build();
    } catch ( IllegalArgumentException e ) {
      return Response.status( Status.BAD_REQUEST ).build();
    }

    final InputStream is = isTmp;

    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write( OutputStream output ) throws IOException {
        try {
          IOUtils.copy( is, output );
        } finally {
          IOUtils.closeQuietly( is );
        }
      }
    };

    MediaType mediaType = MediaType.WILDCARD_TYPE;
    String mimeType = MimeHelper.getMimeTypeFromFileName( path );
    if ( mimeType != null ) {
      try {
        mediaType = MediaType.valueOf( mimeType );
      } catch ( IllegalArgumentException iae ) {
        logger.warn( MessageFormat.format( "PluginFileResource.UNDETERMINED_MIME_TYPE", path ) ); //$NON-NLS-1$
      }
    }

    return Response.ok( streamingOutput, mediaType ).build();
  }
}
