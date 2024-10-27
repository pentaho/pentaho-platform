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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.PluginGwtRpc;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is the plugin variant of the GwtRpcProxyServlet. This servlet routes incoming GWT RPC requests to POJOs found in
 * a plugin lib.
 */
public class GwtRpcPluginProxyServlet extends AbstractGwtRpcProxyServlet {
  public GwtRpcPluginProxyServlet() {
    super();
  }

  public GwtRpcPluginProxyServlet( @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    super( serializationPolicyCache );
  }
  
  @NonNull @Override
  protected AbstractGwtRpc getRpc( @NonNull HttpServletRequest httpRequest ) {
    return PluginGwtRpc.getInstance( httpRequest, getSerializationPolicyCache() );
  }
}
