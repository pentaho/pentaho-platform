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
