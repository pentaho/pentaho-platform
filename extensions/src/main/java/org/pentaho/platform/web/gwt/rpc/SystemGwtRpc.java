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
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.messages.Messages;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.io.File;

/**
 * The <code>SystemGwtRpc</code> class is a specialized GWT-RPC which can be used
 * to handle remote calls to services of the Pentaho Platform itself, e.g. <code>/ws/gwt/serviceName</code>.
 * <p>
 * System GWT remote services must be registered in the Spring container file <code>pentahoServices.spring.xml</code>
 * as a bean whose identifier has the structure: <code>ws-gwt-&lt;serviceName&gt;</code>.
 * For example:
 * <pre>
 *
 * &lt;bean id="ws-gwt-&lt;serviceName&gt;" /&gt;
 * </pre>
 * <p>
 * This service would be exposed in the URL <code>/ws/gwt/&lt;serviceName&gt;</code>.
 * <p>
 * If the service name itself is composed of multiple sections, separated by <code>-</code>,
 * these will be reflected in the service's URL.
 * For example, if <code>&lt;serviceName&gt;</code> would be <code>products-manager</code>,
 * then the remote service would be exposed in the URL <code>/ws/gwt/products/manager</code>.
 * <p>
 * System remote services load GWT serialization policies from the directory of the root class loader
 * of the Pentaho web application.
 * <p>
 * For example, if the GWT module making the remote call lives at <code>/mantle</code>,
 * then the serialization policy file name, given its strong name, would be:
 * <code>/mantle/&lt;strongName&gt;.gwt.rpc</code>.
 */
public class SystemGwtRpc extends AbstractGwtRpc {

  public SystemGwtRpc( @NonNull HttpServletRequest request ) {
    super( request );
  }

  @NonNull @Override
  protected Object resolveTarget() throws GwtRpcProxyException {

    ApplicationContext beanFactory = createAppContext();

    String beanId = getTargetBeanId();
    if ( !beanFactory.containsBean( beanId ) ) {
      throw new GwtRpcProxyException( Messages.getInstance()
        .getErrorString( "GwtRpcProxyServlet.ERROR_0001_NO_BEAN_FOUND_FOR_SERVICE", beanId, getServletContextPath() ) );
    }

    try {
      return beanFactory.getBean( beanId );
    } catch ( BeansException ex ) {
      throw new GwtRpcProxyException( Messages.getInstance()
        .getErrorString( "GwtRpcProxyServlet.ERROR_0002_FAILED_TO_GET_BEAN_REFERENCE", beanId ), ex );
    }
  }

  @Nullable @Override
  protected SerializationPolicy loadSerializationPolicy( @NonNull String moduleContextPath,
                                                         @Nullable String strongName ) {
    /*
     * System request path break down example.
     *
     * - moduleBaseURL = 'http://localhost:8080/pentaho/mantle/'
     * - modulePath = '/pentaho/mantle/'
     * - appContextPath = '/pentaho'
     * - moduleContextPath = '/mantle/'
     * - serializationPolicyFilePath = '/mantle/{strongName}.gwt.rpc'
     */

    String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(
      moduleContextPath + strongName );

    return loadSerializationPolicyFromInputStream(
      () -> getServletContext().getResourceAsStream( serializationPolicyFilePath ),
      serializationPolicyFilePath );
  }

  // Visible For Testing
  @NonNull
  ApplicationContext createAppContext() {
    WebApplicationContext parent = WebApplicationContextUtils.getRequiredWebApplicationContext( getServletContext() );

    ConfigurableWebApplicationContext wac = new XmlWebApplicationContext() {
      @Override
      protected Resource getResourceByPath( @NonNull String path ) {
        return new FileSystemResource( new File( path ) );
      }
    };

    wac.setParent( parent );
    wac.setServletContext( getServletContext() );

    // This code was previously part of the GwtRpcProxyServlet class.
    // There were/are really no known uses of this class.
    // The only declared System, GWT-RPC service is /ws/gwt/unifiedRepository and is not used by any Pentaho code.
    //

    // No access to servlet config. Used to be passed, when this code was part of GwtRpcProxyServlet class.
    // The GwtRpcProxyServlet servlet had no init parameters configured.
    // wac.setServletConfig( getServletConfig() );

    // J.I.C. Used to be created with getServerName(), in the GwtRpcProxyServlet class.
    wac.setNamespace( "GwtRpcProxyServlet" );

    String springFile = PentahoSystem.getApplicationContext()
      .getSolutionPath( "system" + File.separator + "pentahoServices.spring.xml" );
    wac.setConfigLocations( springFile );
    wac.refresh();
    return wac;
  }

  // Example
  // servletContextPath = /ws/gwt/unifiedRepository
  // targetBeanId = ws-gwt-unifiedRepository
  @NonNull
  private String getTargetBeanId() {
    String servletContextPath = getServletContextPath();
    if ( servletContextPath.startsWith( "/" ) ) {
      servletContextPath = servletContextPath.substring( 1 );
    }

    return servletContextPath.replaceAll( "/", "-" );
  }

  /**
   * Gets the instance of {@link SystemGwtRpc} which is associated with the given HTTP request,
   * creating one, if needed.
   * <p>
   * This method does not use a {@link IGwtRpcSerializationPolicyCache} which becomes associated to
   * a created instance for retrieving the appropriate serialization policy.
   * To specify a serialization policy cache, use the method
   * {@link #getInstance(HttpServletRequest, IGwtRpcSerializationPolicyCache)}.
   *
   * @param httpRequest The HTTP request.
   * @return The associated {@link SystemGwtRpc} instance.
   */
  @NonNull
  public static SystemGwtRpc getInstance( @NonNull HttpServletRequest httpRequest ) {
    return getInstance( httpRequest, null );
  }

  /**
   * Gets the instance of {@link SystemGwtRpc} which is associated with the given HTTP request,
   * creating one, if needed.
   * <p>
   * When the instance needs to be created,
   * the given {@link IGwtRpcSerializationPolicyCache}, via <code>serializationPolicyCache</code>,
   * is associated with it.
   *
   * @param httpRequest              The HTTP request.
   * @param serializationPolicyCache A serialization policy cache instance to initialize a
   *                                 created instance with.
   * @return The associated {@link SystemGwtRpc} instance.
   * @see AbstractGwtRpc#getInstance(HttpServletRequest, java.util.function.Function, IGwtRpcSerializationPolicyCache)
   */
  @NonNull
  public static SystemGwtRpc getInstance( @NonNull HttpServletRequest httpRequest,
                                          @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    return getInstance( httpRequest, SystemGwtRpc::new, serializationPolicyCache );
  }
}
