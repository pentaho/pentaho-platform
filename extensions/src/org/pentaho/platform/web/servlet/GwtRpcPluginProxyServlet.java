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

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.engine.core.system.PentahoRequestContextHolder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginUtil;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings( "serial" )
/**
 * This is the plugin variant of the GwtRpcProxyServlet.  This servlet routes incoming GWT RPC
 * requests to POJOs found in a plugin lib.
 */
public class GwtRpcPluginProxyServlet extends AbstractGwtRpcProxyServlet {

  private static final Log logger = LogFactory.getLog( GwtRpcPluginProxyServlet.class );
  private static final Pattern pentahoBasePattern = Pattern.compile( "^/.*/WEBAPP_ROOT/" ); //$NON-NLS-1$

  @Override
  protected Object resolveDispatchTarget( String servletContextPath ) {
    IServiceManager serviceManager = PentahoSystem.get( IServiceManager.class, PentahoSessionHolder.getSession() );

    String key = getDispatchKey();

    if ( null == serviceManager.getServiceConfig( "gwt", key ) ) { //$NON-NLS-1$
      String errMsg =
          Messages.getInstance().getErrorString( "GwtRpcPluginProxyServlet.ERROR_0001_SERVICE_NOT_FOUND", key ); //$NON-NLS-1$
      logger.error( errMsg );
      throw new GwtRpcProxyException( errMsg );
    }

    Object targetBean = null;
    try {
      targetBean = serviceManager.getServiceBean( "gwt", key ); //$NON-NLS-1$
    } catch ( ServiceException e ) {
      throw new GwtRpcProxyException( Messages.getInstance().getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0002_FAILED_TO_GET_BEAN_REFERENCE", key ), e ); //$NON-NLS-1$
    }

    return targetBean;
  }

  /**
   * The request path is broken up for processing like so: Ex: given a request for serialization file at
   * '/pentaho/content/data-access/resources/gwt/{strongName}'
   * 
   * @param serializationPolicyFilename
   *          the name of the serialization policy file that GWT is looking for
   * @param appContextPath
   *          according to the example url, this would be '/pentaho'
   * @param servletContextPath
   *          according the the example url, this would be '/content/data-access/resources/gwt/{strongName}'
   * @return a URL to the serialization policy file, or <code>null</code> if none applies, in which case the default
   *         serialization policy will apply
   */
  protected URL getSerializationPolicyUrl( String serializationPolicyFilename, String appContextPath,
      String servletContextPath ) {
    // We will use the pluginContextPath to determine the service plugin for the serialization policy file
    //

    ClassLoader serviceClassloader = PluginUtil.getClassLoaderForService( servletContextPath );
    if ( serviceClassloader == null ) {
      // if we get here, then the service is not supplied by a plugin and thus we cannot hope to find
      // the appropriate serialization policy
      logger.error( Messages.getInstance().getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0005_FAILED_TO_FIND_PLUGIN", appContextPath ) ); //$NON-NLS-1$
    }

    // We know what plugin is supposed to have the serialization policy file, now go find it
    // in the plugin's filesystem
    //
    IPluginResourceLoader resLoader =
        PentahoSystem.get( IPluginResourceLoader.class, PentahoSessionHolder.getSession() );
    List<URL> urls = resLoader.findResources( serviceClassloader, serializationPolicyFilename );
    if ( urls.size() < 1 ) {
      logger.error( Messages.getInstance().getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0006_FAILED_TO_FIND_FILE", serializationPolicyFilename ) ); //$NON-NLS-1$
      return null;
    }
    if ( urls.size() > 1 ) {
      logger.warn( Messages.getInstance().getString(
          "GwtRpcPluginProxyServlet.WARN_MULTIPLE_RESOURCES_FOUND", serializationPolicyFilename ) ); //$NON-NLS-1$
    }

    return urls.get( 0 );
  }

  @Override
  protected SerializationPolicy doGetSerializationPolicy( HttpServletRequest request, String moduleBaseURL,
      String strongName ) {
    /*
     * The request path is broken up for processing like so: Ex: given a request for serialization file at
     * '/pentaho/content/data-access/resources/gwt/{strongName}' * modulePath = ????
     * '/pentaho/content/data-access/resources/gwt/{strongName}' * appContextPath = '/pentaho' * servletContextPath =
     * '/content/data-access/resources/gwt/{strongName}' * pluginContextPath = '/data-access/resources/gwt/{strongName}'
     */

    SerializationPolicy serializationPolicy = null;
    String appContextPath = request.getContextPath();

    String modulePath = null;
    if ( moduleBaseURL != null ) {
      try {
        modulePath = new URL( moduleBaseURL ).getPath();
      } catch ( MalformedURLException ex ) {
        logger.error( Messages.getInstance().getErrorString(
            "GwtRpcPluginProxyServlet.ERROR_0004_MALFORMED_URL", moduleBaseURL ), ex ); //$NON-NLS-1$
        // cannot proceed, default serialization policy will apply
        return null;
      }
    }

    if ( modulePath.contains( "WEBAPP_ROOT" ) ) {
      modulePath = scrubWebAppRoot( modulePath );
    }

    String servletContextPath = modulePath.substring( appContextPath.length() );

    // Special logic to use a spring defined SerializationPolicy for a plugin.
    String pluginId = PluginUtil.getPluginIdFromPath( servletContextPath );
    serializationPolicy =
        PentahoSystem.get( SerializationPolicy.class, PentahoSessionHolder.getSession(), Collections.singletonMap(
            "plugin", pluginId ) );
    if ( serializationPolicy != null ) {
      return serializationPolicy;
    }

    String serializationPolicyFilename = SerializationPolicyLoader.getSerializationPolicyFileName( strongName );
    URL serializationPolicyUrl =
        getSerializationPolicyUrl( serializationPolicyFilename, appContextPath, servletContextPath );
    if ( serializationPolicyUrl == null ) {
      // default serialization policy will apply
      return null;
    }

    InputStream rpcFileInputStream = null;

    try {
      rpcFileInputStream = serializationPolicyUrl.openStream();

      if ( rpcFileInputStream != null ) {
        serializationPolicy = SerializationPolicyLoader.loadFromStream( rpcFileInputStream, null );
      }

    } catch ( IOException e ) {
      logger.error( Messages.getInstance().getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0007_FAILED_TO_OPEN_FILE", serializationPolicyFilename ), e ); //$NON-NLS-1$
    } catch ( ParseException e ) {
      logger.error( Messages.getInstance().getErrorString(
          "GwtRpcPluginProxyServlet.ERROR_0008_FAILED_TO_PARSE_FILE", serializationPolicyFilename ), e ); //$NON-NLS-1$
    } finally {
      if ( rpcFileInputStream != null ) {
        try {
          rpcFileInputStream.close();
        } catch ( IOException e ) { // do nothing }
        }
      }
    }

    // if null, the default serialization policy will apply
    // Note: caching is handled by the parent class
    return serializationPolicy;
  }

  protected String scrubWebAppRoot( String modulePath ) {
    Matcher matcher = pentahoBasePattern.matcher( modulePath );

    if ( matcher.find() ) {
      String garbagePathPart = matcher.group();
      int idx = modulePath.indexOf( garbagePathPart );
      String contextPath = PentahoRequestContextHolder.getRequestContext().getContextPath();
      final String rewrittenModulePath =
          new StringBuffer( modulePath ).replace( idx, idx + garbagePathPart.length(), contextPath ).toString();
      return rewrittenModulePath;
    }
    return modulePath;
  }
}
