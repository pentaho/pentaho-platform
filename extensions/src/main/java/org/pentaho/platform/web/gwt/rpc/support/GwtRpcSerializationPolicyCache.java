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
