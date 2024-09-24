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

package org.pentaho.platform.web.gwt.rpc.matcher;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.hitachivantara.security.web.impl.model.matcher.RegexRequestMatcher;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.servlet.GwtRpcProxyException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.util.Collection;
import java.util.Collections;

/**
 * The <code>AbstractGwtRpcRequestMatcher</code> class is the base class for GWT-RPC request matchers.
 * <p>
 * This request matcher class allows matching the GWT-RPC service method name of each request against a list of
 * method names. The service method name is present in the request body, which is encoded in the GWT-RPC request format.
 * <p>
 * The abstract {@link #getGwtRpc(HttpServletRequest)} method is responsible for obtaining the {@link AbstractGwtRpc}
 * object for the given request.
 * <p>
 * The service method name of the request is then obtained from the returned {@link AbstractGwtRpc} object.
 * <p>
 * Parsing the GWT-RPC request bodies requires a {@link SerializationPolicy} object, which is typically the result
 * of loading a file from disk. To make this process performant, this class optionally accepts an instance of
 * {@link IGwtRpcSerializationPolicyCache}, where these objects are stored and loaded from.
 */
public abstract class AbstractGwtRpcRequestMatcher extends RegexRequestMatcher {
  /**
   * The GWT RPC protocol only accepts POST requests. Let the other HTTP methods pass-through, because ultimately {@link
   * com.google.gwt.user.server.rpc.RemoteServiceServlet} will reject the request. This also ensures that this class
   * only tries to extract the method from the body when there's surely one.
   */
  private static final Collection<String> HTTP_METHODS_GWT_RPC = Collections.singletonList( HttpMethod.POST );

  @Nullable
  private final Collection<String> rpcMethodNames;

  @Nullable
  private final IGwtRpcSerializationPolicyCache serializationPolicyCache;

  /**
   * Constructs a GWT-RPC request matcher given a path pattern, collection of RPC method names,
   * and a GWT-RPC serialization policy cache.
   * <p>
   * The request matcher created with this constructor applies matches the request <i>path</i> with the given pattern
   * in a case-sensitive manner. Note that the RPC method name is always matched in a case-sensitive manner.
   *
   * @param pattern                  The request path pattern.
   * @param rpcMethodNames           The collection of service method names. Can be <code>null</code>,
   *                                 in which case all methods match.
   * @param serializationPolicyCache The serialization policy cache. Can be <code>null</code>,
   *                                 in which case no caching occurs.
   */
  public AbstractGwtRpcRequestMatcher( @NonNull String pattern,
                                       @Nullable Collection<String> rpcMethodNames,
                                       @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {

    super( pattern, HTTP_METHODS_GWT_RPC );

    this.rpcMethodNames = rpcMethodNames;
    this.serializationPolicyCache = serializationPolicyCache;
  }

  /**
   * Constructs a GWT-RPC request matcher given a path pattern, collection of RPC method names,
   * and a GWT-RPC serialization policy cache.
   *
   * @param pattern                  The request path pattern.
   * @param isCaseInsensitive        Indicates whether the given <code>pattern</code> matches the request <i>path</i>
   *                                 in a case-insensitive manner. Note that the RPC method name is always matched
   *                                 in a case-sensitive manner.
   * @param rpcMethodNames           The collection of service method names. Can be <code>null</code>,
   *                                 in which case all methods match.
   * @param serializationPolicyCache The serialization policy cache. Can be <code>null</code>,
   *                                 in which case no caching occurs.
   */
  public AbstractGwtRpcRequestMatcher( @NonNull String pattern,
                                       boolean isCaseInsensitive,
                                       @Nullable Collection<String> rpcMethodNames,
                                       @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {

    super( pattern, HTTP_METHODS_GWT_RPC, isCaseInsensitive );

    this.rpcMethodNames = rpcMethodNames;
    this.serializationPolicyCache = serializationPolicyCache;
  }

  /**
   * Gets the serialization policy cache, if any.
   */
  @Nullable
  public IGwtRpcSerializationPolicyCache getSerializationPolicyCache() {
    return serializationPolicyCache;
  }

  /**
   * Gets the collection of matching GWT-RPC service method names.
   * When <code>null</code>, all methods match.
   */
  @Nullable
  public Collection<String> getRpcMethodNames() {
    return rpcMethodNames;
  }

  @Override
  public boolean test( @NonNull HttpServletRequest httpRequest ) {
    return super.test( httpRequest )
      && ( rpcMethodNames == null || rpcMethodNames.contains( getRpcMethodName( httpRequest ) ) );
  }

  /**
   * Gets the {@link AbstractGwtRpc} instance associated with the given request,
   * creating one if it has not yet been created.
   *
   * @param httpRequest The HTTP request.
   * @return The associated {@link AbstractGwtRpc} instance.
   * @throws GwtRpcProxyException if the {@link AbstractGwtRpc} instance fails to be created.
   */
  @NonNull
  protected abstract AbstractGwtRpc getGwtRpc( @NonNull HttpServletRequest httpRequest );

  // Visible For Testing

  /**
   * Obtains the GWT-RPC service method name associated with the given HTTP request.
   * <p>
   * If calling {@link #getGwtRpc(HttpServletRequest)} fails with a {@link GwtRpcProxyException},
   * then this method returns an empty string. This special method name cannot match any actual method.
   *
   * @param httpRequest The HTTP request.
   * @return The service method name, if successful; an empty string, otherwise.
   */
  @NonNull
  String getRpcMethodName( @NonNull HttpServletRequest httpRequest ) {
    try {
      return getGwtRpc( httpRequest ).getRequest().getMethod().getName();
    } catch ( GwtRpcProxyException ex ) {
      // Exception is already logged.
      // Causes match to fail.
      return "";
    }
  }
}
