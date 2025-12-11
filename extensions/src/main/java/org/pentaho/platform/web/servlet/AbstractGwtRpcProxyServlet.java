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


package org.pentaho.platform.web.servlet;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.StandardSerializationPolicy;
import com.google.gwt.user.server.rpc.jakarta.RPC;
import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.support.GwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;

import jakarta.servlet.http.HttpServletRequest;
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
