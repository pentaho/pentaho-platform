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

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.web.gwt.rpc.AbstractGwtRpc;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;
import org.pentaho.platform.web.gwt.rpc.SystemGwtRpc;

import javax.servlet.http.HttpServletRequest;
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
