/*
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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created 1/21/2011
 * @author Aaron Phillips
 * 
 */
package org.pentaho.platform.web.http.api.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.web.MimeHelper;
import org.pentaho.platform.web.http.messages.Messages;

import static javax.ws.rs.core.MediaType.WILDCARD;

/**
 * Represents the public files available in a plugin.
 * 
 * @author aaron
 * 
 */
@Path("/plugins/{pluginId}")
public class PluginResource {

  private static final String CACHE_FILE = "file"; //$NON-NLS-1$

  private static ICacheManager cache = PentahoSystem.getCacheManager(null);

  protected File systemFolder;
  
  protected IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class);

  private static final Log logger = LogFactory.getLog(PluginResource.class);
  
  protected IPluginManager pluginMgr = PentahoSystem.get(IPluginManager.class);

  static {
    if (cache != null) {
      cache.addCacheRegion(CACHE_FILE);
    }
  }

  @Context
  protected HttpServletResponse httpServletResponse;

  public PluginResource() {
    systemFolder = new File(PentahoSystem.getApplicationContext().getSolutionRootPath(), "system"); //$NON-NLS-1$
  }
  
  public PluginResource(HttpServletResponse httpServletResponse) {
    this();
    this.httpServletResponse = httpServletResponse;
  }

  protected InputStream getCacheBackedStream(String pluginId, String path, boolean useCache) throws IOException {
    InputStream inputStream = null;

    final String canonicalPath = pluginId + "/" + path; //$NON-NLS-1$

    if (useCache) {
      ByteArrayOutputStream bos = (ByteArrayOutputStream) cache.getFromRegionCache(CACHE_FILE, canonicalPath);
      if (bos != null) {
        inputStream = new ByteArrayInputStream(bos.toByteArray());
      }
    }

    ClassLoader loader = pluginManager.getClassLoader(pluginId);
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class);
    inputStream = resLoader.getResourceAsStream(loader, path);

    if (inputStream == null) {
      throw new FileNotFoundException(Messages.getInstance().getString("PluginFileResource.COULD_NOT_READ_FILE", path)); //$NON-NLS-1$
    }

    if (useCache) {
      // store bytes for next time
      ByteArrayOutputStream bos = null;
      try {
        bos = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, bos);
        cache.putInRegionCache(CACHE_FILE, canonicalPath, bos);
      } finally {
        IOUtils.closeQuietly(bos);
      }
    }

    return inputStream;
  }

  @GET
  @Path("/files/{path : .+}")
  @Produces(WILDCARD)
  public Response readFile(@PathParam("pluginId") String pluginId, @PathParam("path") String path) throws IOException {

    boolean useCache = "true".equals(pluginManager.getPluginSetting(pluginId, "settings/cache", "false")); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    String maxAge = (String) pluginManager.getPluginSetting(pluginId, "settings/max-age", null); //$NON-NLS-1$
    ////
    // Set browser cache if valid value
    if (maxAge != null && !"0".equals(maxAge)) { //$NON-NLS-1$
      httpServletResponse.setHeader("Cache-Control", "max-age=" + maxAge); //$NON-NLS-1$ //$NON-NLS-2$
    }

    if (!pluginManager.getRegisteredPlugins().contains(pluginId)) {
      return Response.status(Status.NOT_FOUND).build();
    }

    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class);
    ClassLoader loader = pluginManager.getClassLoader(pluginId);
    if (!pluginManager.isPublic(pluginId, path) && resLoader.getResourceAsStream(loader, path) != null) {
      return Response.status(Status.FORBIDDEN).build();
    }

    InputStream isTmp = null;
    try {
      isTmp = getCacheBackedStream(pluginId, path, useCache);
    } catch (FileNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    final InputStream is = isTmp;

    StreamingOutput streamingOutput = new StreamingOutput() {
      public void write(OutputStream output) throws IOException {
        try {
          IOUtils.copy(is, output);
        } finally {
          IOUtils.closeQuietly(is);
        }
      }
    };

    MediaType mediaType = MediaType.WILDCARD_TYPE;
    String mimeType = MimeHelper.getMimeTypeFromFileName(path);
    if (mimeType != null) {
      try {
        mediaType = MediaType.valueOf(mimeType);
      } catch (IllegalArgumentException iae) {
        logger.warn(MessageFormat.format("PluginFileResource.UNDETERMINED_MIME_TYPE", path)); //$NON-NLS-1$
      }
    }

    return Response.ok(streamingOutput, mediaType).build();
  }
}
