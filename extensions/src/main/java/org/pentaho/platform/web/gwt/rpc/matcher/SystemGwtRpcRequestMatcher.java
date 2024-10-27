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


package org.pentaho.platform.web.gwt.rpc.matcher;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.SystemGwtRpc;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * The <code>SystemGwtRpcRequestMatcher</code> class is a specialized GWT-RPC request matcher
 * which can be used to match URLs of GWT-RPC services of the Pentaho Platform itself (e.g.
 * <code>/ws/gwt/serviceName</code>).
 *
 * @see SystemGwtRpc
 */
public class SystemGwtRpcRequestMatcher extends AbstractGwtRpcRequestMatcher {

  public SystemGwtRpcRequestMatcher( @NonNull String pattern,
                                     @Nullable Collection<String> rpcMethods,
                                     @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    super( pattern, rpcMethods, serializationPolicyCache );
  }

  public SystemGwtRpcRequestMatcher( @NonNull String pattern,
                                     boolean isCaseInsensitive,
                                     @Nullable Collection<String> rpcMethods,
                                     @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    super( pattern, isCaseInsensitive, rpcMethods, serializationPolicyCache );
  }

  @NonNull @Override
  protected AbstractGwtRpc getGwtRpc( @NonNull HttpServletRequest httpRequest ) {
    return SystemGwtRpc.getInstance( httpRequest, getSerializationPolicyCache() );
  }
}
