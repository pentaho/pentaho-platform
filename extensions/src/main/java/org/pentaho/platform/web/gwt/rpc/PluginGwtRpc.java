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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.gwt.rpc;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.pluginmgr.PluginUtil;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * The <code>PluginGwtRpc</code> class is a specialized GWT-RPC which can be used
 * to handle remote calls to services of Pentaho Platform plugins, e.g. <code>/gwtrpc/serviceName</code>.
 * <p>
 * Plugin GWT remote services must be registered in the current {@link IServiceManager} with the service type `gwt`.
 * <p>
 * Plugins can register a remote services in their <code>plugin.xml</code> file,
 * using the <code>webservice</code> element, like in the following:
 * <pre>
 *
 * &lt;webservice id="&lt;serviceName&gt;" type="gwt" class="&lt;my.remote.service.Class&gt;" /&gt;
 * </pre>
 * <p>
 * This service would become exposed under the Pentaho context URL <code>/gwtrpc/&lt;serviceName&gt;</code>.
 * The class <code>&lt;my.remote.service.Class&gt;</code> must be accessible in the plugin's class loader
 * and should implement the {@link com.google.gwt.user.client.rpc.RemoteService} interface.
 * <p>
 * Plugin remote services handle GWT serialization policies in one of two ways:
 * <p>
 * A static serialization policy can be defined in the plugin's <code>plugin.spring.xml</code>,
 * by registering with the Pentaho System a bean of a class extending from {@link SerializationPolicy},
 * such as {@link org.pentaho.platform.web.servlet.PentahoSerializationPolicy}.
 * This policy will be used whatever the <i>strong name</i> being requested.
 * For example:
 *
 * <pre>
 *   &lt;bean class="org.pentaho.platform.web.servlet.PentahoSerializationPolicy"&gt;
 *     &lt;property name="whiteList"&gt;
 *       &lt;util:list&gt;
 *         &lt;value&gt;com.allen_sauer.gwt.dnd.client.DragHandlerCollection&lt;/value&gt;
 *         ...
 *       &lt;/util:list&gt;
 *     &lt;/property&gt;
 *     &lt;pen:publish as-type="CLASSES"&gt;
 *       &lt;pen:attributes&gt;
 *         &lt;pen:attr key="plugin" value="[my-plugin]"/&gt;
 *       &lt;/pen:attributes&gt;
 *     &lt;/pen:publish&gt;
 *   &lt;/bean&gt;
 * </pre>
 *
 * <p>
 * In this example, the classes which are allowed to be exchanged between client and server must be specified in
 * the property named <code>whiteList</code>.
 * Moreover, instead of <code>[my-plugin]</code>, the identifier of the plugin should be used instead.
 * <p>
 * The second way to register serialization policies is by using the GWT-RPC files that GWT automatically
 * creates when compiling GWT code. These files have a custom internal format,
 * are named like <code>&lt;strong-name&gt;.gwt.rpc</code> and are placed in the same directory as the
 * remaining compiled GWT code (e.g. <code>/content/data-access/resources/gwt/</code>).
 * One such file could be named <code>825CCA5A63214CE13B23AAA2FCB5C8CA.gwt.rpc</code>.
 * The serialization policy strong name is unique to it and is not necessarily shared with the strong name of a
 * particular GWT module. This file is searched for <i>anywhere</i> in the plugin's file system,
 * via {@link IPluginResourceLoader#findResources(ClassLoader, String)} and at least one file with such name must exist,
 * or an error is logged and the loading fails. If more than one file exists, the first one found is used and
 * a warning is logged.
 */
public class PluginGwtRpc extends AbstractGwtRpc {

  private static final Log logger = LogFactory.getLog( PluginGwtRpc.class );

  public PluginGwtRpc( @NonNull HttpServletRequest request ) {
    super( request );
  }

  @NonNull @Override
  protected Object resolveTarget() throws GwtRpcProxyException {

    IServiceManager serviceManager = PentahoSystem.get( IServiceManager.class, PentahoSessionHolder.getSession() );

    String key = getServiceKey();
    if ( serviceManager.getServiceConfig( "gwt", key ) == null ) {
      throw new GwtRpcProxyException( Messages.getInstance()
        .getErrorString( "GwtRpcPluginProxyServlet.ERROR_0001_SERVICE_NOT_FOUND", key ) );
    }

    try {
      return serviceManager.getServiceBean( "gwt", key );
    } catch ( ServiceException e ) {
      throw new GwtRpcProxyException( Messages.getInstance()
        .getErrorString( "GwtRpcPluginProxyServlet.ERROR_0002_FAILED_TO_GET_BEAN_REFERENCE", key ), e );
    }
  }

  @Nullable @Override
  protected SerializationPolicy loadSerializationPolicy( @NonNull String moduleContextPath,
                                                         @Nullable String strongName ) {
    /*
     * Plugin request path break down example.
     *
     * - moduleBaseURL = 'http://localhost:8080/pentaho/content/data-access/resources/gwt/'
     * - modulePath = '/pentaho/content/data-access/resources/gwt/'
     * - appContextPath = '/pentaho'
     * - moduleContextPath = '/content/data-access/resources/gwt/'
     * - pluginContextPath = '/data-access/resources/gwt/'
     * - serializationPolicyFileName = '/data-access/resources/gwt/{strongName}.gwt.rpc'
     */

    // Special logic to use a spring defined SerializationPolicy for a plugin.
    String pluginId = PluginUtil.getPluginIdFromPath( moduleContextPath );

    SerializationPolicy serializationPolicy = PentahoSystem.get(
      SerializationPolicy.class,
      PentahoSessionHolder.getSession(),
      Collections.singletonMap( "plugin", pluginId ) );
    if ( serializationPolicy != null ) {
      return serializationPolicy;
    }

    String serializationPolicyFileName = SerializationPolicyLoader.getSerializationPolicyFileName( strongName );
    URL serializationPolicyUrl = getSerializationPolicyUrl( serializationPolicyFileName, moduleContextPath );

    return serializationPolicyUrl != null
      ? loadSerializationPolicyFromInputStream( getInputStreamSupplier( serializationPolicyUrl ),
      serializationPolicyFileName )
      : null;
  }

  // Visible For Testing
  @NonNull
  ThrowingSupplier<InputStream, IOException> getInputStreamSupplier( @NonNull URL inputStreamURL ) {
    return inputStreamURL::openStream;
  }

  /**
   * The request path is broken up for processing like so: Ex: given a request for serialization file at
   * '/pentaho/content/data-access/resources/gwt/'
   *
   * @param serializationPolicyFilename the name of the serialization policy file that GWT is looking for
   * @param moduleContextPath           according to the example url, this would be
   *                                    '/content/data-access/resources/gwt/'
   * @return a URL to the serialization policy file, or <code>null</code> if none applies, in which case the default
   * serialization policy will apply
   */
  @Nullable
  private URL getSerializationPolicyUrl( @NonNull String serializationPolicyFilename, String moduleContextPath ) {

    ClassLoader serviceClassloader = PluginUtil.getClassLoaderForService( moduleContextPath );
    if ( serviceClassloader == null ) {
      // if we get here, then the service is not supplied by a plugin and thus we cannot hope to find
      // the appropriate serialization policy.
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0005_FAILED_TO_FIND_PLUGIN", getAppContextPath() ) );
      return null;
    }

    // We know what plugin is supposed to have the serialization policy file,
    // now go find it in the plugin's filesystem.
    IPluginResourceLoader resLoader =
      PentahoSystem.get( IPluginResourceLoader.class, PentahoSessionHolder.getSession() );
    List<URL> urls = resLoader.findResources( serviceClassloader, serializationPolicyFilename );
    if ( urls.size() < 1 ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0006_FAILED_TO_FIND_FILE", serializationPolicyFilename ) );
      return null;
    }

    if ( urls.size() > 1 ) {
      logger.warn( Messages.getInstance().getString(
        "GwtRpcPluginProxyServlet.WARN_MULTIPLE_RESOURCES_FOUND", serializationPolicyFilename ) );
    }

    return urls.get( 0 );
  }

  /**
   * Returns the dispatch key for this request. This name is the part of the request path beyond the servlet base path.
   * I.e. if the GwtRpcPluginProxyServlet is mapped to the "/gwtrpc" context in web.xml, then this method will return
   * "testservice" upon a request to "http://localhost:8080/pentaho/gwtrpc/testservice".
   *
   * @return the part of the request url used to dispatch the request
   */
  @NonNull
  private String getServiceKey() {
    // Path info will give us what we want with.
    String requestPathInfo = getServletRequest().getPathInfo();
    if ( requestPathInfo.startsWith( "/" ) ) {
      requestPathInfo = requestPathInfo.substring( 1 );
    }

    if ( requestPathInfo.contains( "/" ) ) {
      // if the request path happens to be multiple levels deep, return the last element in the path
      String[] elements = requestPathInfo.split( "/" );
      return elements[ elements.length - 1 ];
    }

    return requestPathInfo;
  }

  /**
   * Gets the instance of {@link PluginGwtRpc} which is associated with the given HTTP request, creating one, if needed.
   * <p>
   * This method does not use a {@link IGwtRpcSerializationPolicyCache} which becomes associated to
   * a created instance for retrieving the appropriate serialization policy.
   * To specify a serialization policy cache, use the method
   * {@link #getInstance(HttpServletRequest, IGwtRpcSerializationPolicyCache)}.
   *
   * @param httpRequest The HTTP request.
   * @return The associated {@link PluginGwtRpc} instance.
   */
  @NonNull
  public static PluginGwtRpc getInstance( @NonNull HttpServletRequest httpRequest ) {
    return getInstance( httpRequest, null );
  }

  /**
   * Gets the instance of {@link PluginGwtRpc} which is associated with the given HTTP request, creating one, if needed.
   * <p>
   * When the instance needs to be created, the given {@link IGwtRpcSerializationPolicyCache},
   * via <code>serializationPolicyCache</code>, is associated with it.
   *
   * @param httpRequest              The HTTP request.
   * @param serializationPolicyCache A serialization policy cache instance to initialize a created instance with.
   * @return The associated {@link PluginGwtRpc} instance.
   * @see AbstractGwtRpc#getInstance(HttpServletRequest, java.util.function.Function, IGwtRpcSerializationPolicyCache)
   */
  @NonNull
  public static PluginGwtRpc getInstance( @NonNull HttpServletRequest httpRequest,
                                          @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    return getInstance( httpRequest, PluginGwtRpc::new, serializationPolicyCache );
  }
}
