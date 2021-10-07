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

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.gwt.rpc.impl.GwtRpcUtil;
import org.pentaho.platform.web.gwt.rpc.util.ThrowingSupplier;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;
import org.pentaho.platform.web.servlet.messages.Messages;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Function;

/**
 * The <code>AbstractGwtRpc</code> class is the base abstract class of classes that represent a GWT
 * <b>Remote Procedure Call</b> which is made via HTTP.
 * <p>
 * One instance of this class is initialized with the {@link HttpServletRequest} instance which represents a GWT-RPC
 * request.
 * <p>
 * The class exposes functionality to:
 * <ol>
 *   <li>
 *     obtain the target object of the GWT-RPC request, via {@link #getTarget()};
 *   </li>
 *   <li>
 *     obtain the class loader which should be current when the call is performed, via {@link #getTargetClassLoader()};
 *   </li>
 *   <li>
 *     obtain the serialized request payload from the HTTP request, via {@link #getRequestPayload()};
 *     this is useful for low-level integration with {@link com.google.gwt.user.server.rpc.RemoteServiceServlet};
 *   </li>
 *   <li>
 *     obtain the parsed request, via {@link #getRequest()};
 *   </li>
 *   <li>
 *     invoke the target method on the target object within its class loader and to return a serialized response.
 *   </li>
 * </ol>
 * <p>
 * Concrete specialized classes exist that can handle GWT-RPC requests for services of:
 * <ul>
 *   <li>
 *     {@link PluginGwtRpc} - services of Pentaho Platform plugins, e.g. <code>/gwtrpc/serviceName</code>;
 *   </li>
 *   <li>
 *     {@link SystemGwtRpc} - services of the Pentaho Platform itself, e.g. <code>/ws/gwt/serviceName</code>.
 *   </li>
 * </ul>
 */
public abstract class AbstractGwtRpc {

  private static final Log logger = LogFactory.getLog( AbstractGwtRpc.class );

  protected static final String HTTP_GWT_RPC_ATTRIBUTE = AbstractGwtRpc.class.getSimpleName();

  @NonNull
  private final HttpServletRequest httpRequest;

  @Nullable
  private Object target;

  @Nullable
  private String requestPayload;

  @Nullable
  private RPCRequest request;

  @Nullable
  private IGwtRpcSerializationPolicyCache serializationPolicyCache;

  /**
   * Creates an instance given an HTTP request.
   *
   * @param httpRequest An HTTP request.
   */
  protected AbstractGwtRpc( @NonNull HttpServletRequest httpRequest ) {
    Objects.requireNonNull( httpRequest );
    this.httpRequest = httpRequest;
  }

  /**
   * Sets the serialization policy cache which will be used to load a serialization policy before creating one,
   * or to store the created one if none exists.
   * <p>
   * The serialization policy is used to safely deserialize the GWT-RPC request and serialize the GWT-RPC response
   * of this call.
   *
   * @param serializationPolicyCache A serialization policy cache instance, or <code>null</code>.
   */
  public void setSerializationPolicyCache( @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    this.serializationPolicyCache = serializationPolicyCache;
  }

  /**
   * Gets the serialization policy cache instance used by this GWT-RPC call.
   *
   * @return The serialization policy cache instance, if any; <code>null</code>, otherwise.
   */
  @Nullable
  public IGwtRpcSerializationPolicyCache getSerializationPolicyCache() {
    return serializationPolicyCache;
  }

  // region Servlet

  /**
   * Gets the associated HTTP request.
   *
   * @return An HTTP request.
   */
  @NonNull
  public HttpServletRequest getServletRequest() {
    return httpRequest;
  }

  /**
   * Gets the associated servlet context.
   * <p>
   * Syntax-sugar method equivalent to <code>getServletRequest().getServletContext()</code>.
   *
   * @return A servlet context.
   */
  @NonNull
  protected ServletContext getServletContext() {
    return getServletRequest().getServletContext();
  }

  /**
   * Gets the associated web app context path.
   * <p>
   * Syntax-sugar method equivalent to <code>getServletRequest().getContextPath()</code>.
   * <p>
   * Example value: <code>/pentaho</code>.
   *
   * @return The servlet context path.
   */
  @NonNull
  protected String getAppContextPath() {
    return httpRequest.getContextPath();
  }

  /**
   * Gets the associated servlet context path.
   * <p>
   * Example value: <code>/gwt/rpc/serviceName?bar=foo</code>.
   *
   * @return The servlet context path.
   */
  @NonNull
  protected String getServletContextPath() {
    String path = httpRequest.getServletPath() + "/" + httpRequest.getPathInfo();
    if ( path.contains( "//" ) ) {
      path = path.replaceAll( "//", "/" );
    }

    return path;
  }
  // endregion

  // region Target

  /**
   * Gets the target object on which the remote call will be made.
   *
   * @return The target object.
   * @throws GwtRpcProxyException if the target object cannot be resolved.
   */
  @NonNull
  public Object getTarget() {
    if ( target == null ) {
      try {
        target = resolveTarget();
      } catch ( GwtRpcProxyException ex ) {
        logger.error( Messages.getInstance().getErrorString(
          "AbstractGwtRpcProxyServlet.ERROR_0001_FAILED_TO_RESOLVE_DISPATCH_TARGET",
          getServletContextPath() ), ex );
        throw ex;
      }
    }

    return target;
  }

  /**
   * Gets the class loader which must be current to deserialize the request, invoke the method and
   * serialize the response.
   *
   * @return The target class laoder.
   * @throws GwtRpcProxyException if the target class loader cannot be resolved.
   */
  @NonNull
  public ClassLoader getTargetClassLoader() {
    return getTarget().getClass().getClassLoader();
  }

  /**
   * Resolves the target object on which the remote call will be made.
   *
   * @return The target object.
   * @throws GwtRpcProxyException if the target object cannot be resolved.
   */
  @NonNull
  protected abstract Object resolveTarget();
  // endregion

  // region Request Payload, Decoding

  /**
   * Gets the serialized request payload which is present in the HTTP request.
   *
   * @return The serialized request payload.
   * @throws GwtRpcProxyException if the request is not valid.
   * @see RPCServletUtils#readContentAsGwtRpc(HttpServletRequest)
   */
  @NonNull
  public String getRequestPayload() throws GwtRpcProxyException {
    if ( requestPayload == null ) {
      try {
        requestPayload = RPCServletUtils.readContentAsGwtRpc( httpRequest );
      } catch ( IOException | ServletException ex ) {
        String message =
          Messages.getInstance().getErrorString( "AbstractGwtRpcProxyServlet.ERROR_0002_RPC_INVALID_REQUEST" );
        logger.error( message, ex );
        throw new GwtRpcProxyException( message, ex );
      }
    }

    return requestPayload;
  }

  // Based on RemoteServiceServlet#processPost(..)

  /**
   * Gets the processed GWT-RPC request object.
   *
   * @return The processed request.
   * @throws GwtRpcProxyException if the request is invalid.
   * @see RPC#decodeRequest(String, Class, com.google.gwt.user.server.rpc.SerializationPolicyProvider)
   */
  @NonNull
  public RPCRequest getRequest() {
    if ( request == null ) {

      String requestPayload = getRequestPayload();

      request = GwtRpcUtil.withClassLoader( getTargetClassLoader(), () -> getRequestCore( requestPayload ) );
    }

    return request;
  }

  // Visible For Testing
  @NonNull
  RPCRequest getRequestCore( @NonNull String requestPayload ) {
    try {
      return RPC.decodeRequest( requestPayload, null, this::getSerializationPolicy );
    } catch ( IllegalArgumentException | IncompatibleRemoteServiceException ex ) {
      String message =
        Messages.getInstance().getErrorString( "AbstractGwtRpcProxyServlet.ERROR_0002_RPC_INVALID_REQUEST" );
      logger.error( message, ex );
      throw new GwtRpcProxyException( message, ex );
    }
  }
  // endregion

  // region Serialization Policy

  /*
   * Request path break down.
   *
   * Plugin Example
   *
   * - moduleBaseURL = 'http://localhost:8080/pentaho/content/data-access/resources/gwt/'
   * - modulePath = '/pentaho/content/data-access/resources/gwt/'
   * - appContextPath = '/pentaho'
   * - moduleContextPath = '/content/data-access/resources/gwt/'
   *
   * System Example
   *
   * - moduleBaseURL = 'http://localhost:8080/pentaho/mantle/'
   * - modulePath = '/pentaho/mantle/'
   * - appContextPath = '/pentaho'
   * - moduleContextPath = '/mantle/'
   */

  @NonNull
  protected SerializationPolicy getSerializationPolicy( @Nullable String moduleBaseURL, @Nullable String strongName ) {
    if ( serializationPolicyCache != null ) {
      return serializationPolicyCache.getSerializationPolicy(
        moduleBaseURL,
        strongName,
        this::getSerializationPolicyCore );
    }

    return getSerializationPolicyCore( moduleBaseURL, strongName );
  }

  @NonNull
  private SerializationPolicy getSerializationPolicyCore( @Nullable String moduleBaseURL,
                                                          @Nullable String strongName ) {
    if ( moduleBaseURL == null ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0004_MALFORMED_URL", "" ) );
      return getDefaultSerializationPolicy();
    }

    String modulePath;
    try {
      modulePath = new URL( moduleBaseURL ).getPath();
    } catch ( MalformedURLException ex ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0004_MALFORMED_URL", moduleBaseURL ), ex );
      return getDefaultSerializationPolicy();
    }

    String appContextPath = getAppContextPath();
    modulePath = GwtRpcUtil.scrubWebAppRoot( modulePath, appContextPath );

    if ( !modulePath.startsWith( appContextPath ) ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0004_MALFORMED_URL", moduleBaseURL ) );
      return getDefaultSerializationPolicy();
    }

    String moduleContextPath = modulePath.substring( appContextPath.length() );

    SerializationPolicy serializationPolicy = loadSerializationPolicy( moduleContextPath, strongName );

    return serializationPolicy != null ? serializationPolicy : getDefaultSerializationPolicy();
  }

  /**
   * Loads the serialization policy having the given GWT module context path and strong name.
   * <p>
   * Typically, this method loads a serialization policy from a persistence medium.
   *
   * @param moduleContextPath The GWT module context path (e.g. <code>/content/data-access/resources/gwt/</code>).
   * @param strongName        The serialization policy strong name.
   * @return The associated serialization policy, if one can be loaded;
   * <code>null</code>, otherwise, in which case the default serialization policy is assumed,
   * as returned by {@link #getDefaultSerializationPolicy()}.
   */
  @Nullable
  protected abstract SerializationPolicy loadSerializationPolicy( @NonNull String moduleContextPath,
                                                                  @Nullable String strongName );

  /**
   * Gets a serialization policy to use when no specific serialization policy is available.
   * <p>
   * the default serialization policy is a legacy, 1.3.3 compatible, serialization policy and
   * may result in {@link SerializationException}.
   *
   * @return The default serialization policy.
   * @see RPC#getDefaultSerializationPolicy()
   */
  @NonNull
  protected static SerializationPolicy getDefaultSerializationPolicy() {
    return RPC.getDefaultSerializationPolicy();
  }

  /**
   * Loads a serialization policy given an input stream whose content is in GWT-RPC
   * serialization policy standard format.
   * <p>
   * Helper method for custom implementations.
   *
   * @param inputStreamSupplier         A supplier of an input stream of the serialization policy.
   * @param serializationPolicyFileName The serialization policy file name; used for logging purposes, only.
   * @return The loaded serialization policy, if successfully loaded; <code>null</code>, otherwise.
   */
  @Nullable
  protected static SerializationPolicy loadSerializationPolicyFromInputStream(
    @NonNull ThrowingSupplier<InputStream, IOException> inputStreamSupplier,
    @NonNull String serializationPolicyFileName ) {

    try ( InputStream rpcFileInputStream = inputStreamSupplier.get() ) {
      if ( rpcFileInputStream != null ) {
        return SerializationPolicyLoader.loadFromStream( rpcFileInputStream, null );
      }

      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0007_FAILED_TO_OPEN_FILE", serializationPolicyFileName ) );
    } catch ( IOException e ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0007_FAILED_TO_OPEN_FILE", serializationPolicyFileName ), e );
    } catch ( ParseException e ) {
      logger.error( Messages.getInstance().getErrorString(
        "GwtRpcPluginProxyServlet.ERROR_0008_FAILED_TO_PARSE_FILE", serializationPolicyFileName ), e );
    }

    return null;
  }
  // endregion

  // region Invocation

  /**
   * Makes the remote call on the target object and returns the serialized response.
   *
   * @return The serialized response.
   * @throws GwtRpcProxyException if the call does not succeed.
   */
  @NonNull
  public String invoke() {

    Object target = getTarget();

    Class<?> targetClass = target.getClass();

    RPCRequest rpcRequest = getRequest();

    try {
      Method targetMethod = getTargetMethod( targetClass, rpcRequest );

      return GwtRpcUtil.withClassLoaderThrowing(
        // Making it this way, effectively repeating getTarget(), makes it easier to test.
        getTargetClassLoader(),
        () -> invokeCore( target, targetMethod, rpcRequest ) );

    } catch ( NoSuchMethodException | SerializationException ex ) {
      String message = Messages.getInstance().getErrorString(
        "AbstractGwtRpcProxyServlet.ERROR_0003_RPC_INVOCATION_FAILED", targetClass.getName() );
      logger.error( message, ex );
      throw new GwtRpcProxyException( message, ex );
    }
  }

  // Visible For Testing
  @NonNull
  Method getTargetMethod( @NonNull Class<?> targetClass, @NonNull RPCRequest rpcRequest )
    throws NoSuchMethodException {

    Method serviceInterfaceMethod = rpcRequest.getMethod();

    // Don't require the target class to implement the service interface.
    return targetClass.getMethod(
      serviceInterfaceMethod.getName(),
      serviceInterfaceMethod.getParameterTypes() );
  }

  // Visible For Testing
  @NonNull
  String invokeCore( @NonNull Object target, @NonNull Method targetMethod, @NonNull RPCRequest rpcRequest )
    throws SerializationException {

    return RPC.invokeAndEncodeResponse(
      target,
      targetMethod,
      rpcRequest.getParameters(),
      rpcRequest.getSerializationPolicy() );
  }
  // endregion

  /**
   * Gets the instance of a sub-class of {@link AbstractGwtRpc}, <code>R</code>,
   * which is stored in the given HTTP request, if there is one already, or creates one and stores it if not.
   * <p>
   * The GWT-RPC instance is stored in an HTTP request attribute,
   * as per {@link HttpServletRequest#getAttribute(String)} and {@link HttpServletRequest#setAttribute(String, Object)}.
   * <p>
   * Helper method for custom implementations.
   *
   * @param httpRequest              The HTTP request.
   * @param factory                  A function which creates an instance of class <code>R</code> given an HTTP request.
   * @param serializationPolicyCache A serialization policy cache instance to initialize a created instance with.
   * @return The associated GWT-RPC instance.
   * @see AbstractGwtRpc#setSerializationPolicyCache(IGwtRpcSerializationPolicyCache)
   */
  @NonNull
  protected static <R extends AbstractGwtRpc> R getInstance(
    @NonNull HttpServletRequest httpRequest,
    @NonNull Function<HttpServletRequest, R> factory,
    @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {

    Objects.requireNonNull( httpRequest );
    Objects.requireNonNull( factory );

    @SuppressWarnings( "unchecked" )
    R rpc = (R) httpRequest.getAttribute( HTTP_GWT_RPC_ATTRIBUTE );
    if ( rpc == null ) {
      rpc = factory.apply( httpRequest );
      if ( rpc == null ) {
        throw new RuntimeException( "Factory returned a null GwtRpc instance" );
      }

      rpc.setSerializationPolicyCache( serializationPolicyCache );

      httpRequest.setAttribute( HTTP_GWT_RPC_ATTRIBUTE, rpc );
    }

    return rpc;
  }
}
