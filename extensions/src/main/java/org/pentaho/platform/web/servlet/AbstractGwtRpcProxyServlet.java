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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.servlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.support.GwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for GWT RPC proxying servlets, which allows developers to write GWT Services as Pojos and have the GWT
 * client POST requests to subclasses of this class.
 */
public abstract class AbstractGwtRpcProxyServlet extends RemoteServiceServlet {

  private static final Log logger = LogFactory.getLog( AbstractGwtRpcProxyServlet.class );

  @NonNull
  private final IGwtRpcSerializationPolicyCache serializationPolicyCache;

  protected AbstractGwtRpcProxyServlet() {
    this( null );
  }

  protected AbstractGwtRpcProxyServlet( @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    this.serializationPolicyCache = serializationPolicyCache != null
      ? serializationPolicyCache
      : new GwtRpcSerializationPolicyCache();
  }

  @NonNull
  public IGwtRpcSerializationPolicyCache getSerializationPolicyCache() {
    return serializationPolicyCache;
  }

  protected void doUnexpectedFailure( Throwable e ) {
    super.doUnexpectedFailure( e );
    logger.error( e );
  }

  @NonNull
  protected abstract AbstractGwtRpc getRpc( @NonNull HttpServletRequest httpRequest );

  @Override
  protected String readContent( HttpServletRequest httpRequest ) {
    return getRpc( httpRequest ).getRequestPayload();
  }

  @Override
  public String processCall( String requestPayload ) throws SerializationException {

    checkPermutationStrongName();

    try {
      return getRpc( getThreadLocalRequest() ).invoke();
    } catch ( GwtRpcProxyException ex ) {
      return RPC.encodeResponseForFailure( null, ex, getBasicSerializationPolicy() );
    }
  }

  @Override
  protected SerializationPolicy doGetSerializationPolicy( HttpServletRequest request,
                                                          String moduleBaseURL,
                                                          String strongName ) {
    throw new UnsupportedOperationException( "Class is not being used as SerializationPolicyProvider" );
  }

  @NonNull
  static StandardSerializationPolicy getBasicSerializationPolicy() {
    Map<Class<?>, Boolean> whitelist = new HashMap<>();
    whitelist.put( GwtRpcProxyException.class, Boolean.TRUE );

    Map<Class<?>, String> obfuscatedTypeIds = new HashMap<>();

    return new StandardSerializationPolicy( whitelist, whitelist, obfuscatedTypeIds );
  }
}
