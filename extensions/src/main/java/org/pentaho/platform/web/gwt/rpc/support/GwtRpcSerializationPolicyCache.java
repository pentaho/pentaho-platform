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


package org.pentaho.platform.web.gwt.rpc.support;

import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.web.gwt.rpc.IGwtRpcSerializationPolicyCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The <code>GwtRpcSerializationPolicyCache</code> class is a basic in-memory, multi-threaded implementation of
 * the {@link IGwtRpcSerializationPolicyCache} interface.
 */
public class GwtRpcSerializationPolicyCache implements IGwtRpcSerializationPolicyCache {
  @NonNull
  private final Map<String, SerializationPolicy> serializationPolicyCache = new ConcurrentHashMap<>();

  @NonNull @Override
  public SerializationPolicy getSerializationPolicy( @Nullable String moduleBaseURL,
                                                     @Nullable String strongName,
                                                     @NonNull SerializationPolicyProvider sourceProvider ) {

    String key = moduleBaseURL + strongName;

    SerializationPolicy serializationPolicy = serializationPolicyCache.get( key );
    if ( serializationPolicy != null ) {
      return serializationPolicy;
    }

    serializationPolicy = sourceProvider.getSerializationPolicy( moduleBaseURL, strongName );
    if ( serializationPolicy == null ) {
      throw new RuntimeException(
        "Serialization Policy Provider returned null for " + moduleBaseURL + " " + strongName + "." );
    }

    serializationPolicyCache.put( key, serializationPolicy );

    return serializationPolicy;
  }
}
