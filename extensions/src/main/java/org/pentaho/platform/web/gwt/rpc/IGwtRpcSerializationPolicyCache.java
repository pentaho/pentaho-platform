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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.gwt.rpc;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * The <code>IGwtRpcSerializationPolicyCache</code> interface represents a cached serialization policy provider.
 *
 * @see SerializationPolicyProvider
 */
public interface IGwtRpcSerializationPolicyCache {
  /**
   * Get a {@link SerializationPolicy} from cache or from the given provider.
   *
   * @param moduleBaseURL  The base URL of the GWT module.
   * @param strongName     The strong name of the GWT module.
   * @param sourceProvider The provider to use to obtain a missing policy. Cannot return <code>null</code>.
   * @return A serialization policy.
   */
  @NonNull
  SerializationPolicy getSerializationPolicy( @Nullable String moduleBaseURL, @Nullable String strongName,
                                              @NonNull SerializationPolicyProvider sourceProvider );
}
