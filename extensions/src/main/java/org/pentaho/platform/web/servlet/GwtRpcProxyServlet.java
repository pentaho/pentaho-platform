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
import org.pentaho.platform.web.gwt.rpc.SystemGwtRpc;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This servlet is the traffic cop for GWT services core to the BIServer. See pentahoServices.spring.xml for bean
 * definitions referenced by this servlet.
 */
public class GwtRpcProxyServlet extends AbstractGwtRpcProxyServlet {

  public GwtRpcProxyServlet() {
    super();
  }

  public GwtRpcProxyServlet( @Nullable IGwtRpcSerializationPolicyCache serializationPolicyCache ) {
    super( serializationPolicyCache );
  }

  @NonNull @Override
  protected AbstractGwtRpc getRpc( @NonNull HttpServletRequest httpRequest ) {
    return SystemGwtRpc.getInstance( httpRequest, getSerializationPolicyCache() );
  }
}
