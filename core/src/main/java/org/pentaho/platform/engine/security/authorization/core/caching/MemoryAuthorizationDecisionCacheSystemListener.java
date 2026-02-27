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

package org.pentaho.platform.engine.security.authorization.core.caching;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPentahoSystemListener;

import java.util.Objects;

/**
 * System listener that closes a {@link MemoryAuthorizationDecisionCache} when the Pentaho system is shutting down.
 */
public class MemoryAuthorizationDecisionCacheSystemListener implements IPentahoSystemListener {
  private static final Log logger = LogFactory.getLog( MemoryAuthorizationDecisionCacheSystemListener.class );

  @NonNull
  private final MemoryAuthorizationDecisionCache cache;

  public MemoryAuthorizationDecisionCacheSystemListener( @NonNull MemoryAuthorizationDecisionCache cache ) {
    this.cache = Objects.requireNonNull( cache );
  }

  @Override
  public boolean startup( IPentahoSession session ) {
    if ( logger.isTraceEnabled() ) {
      logger.trace( "Started authorization decision cache" );
    }

    return true;
  }

  @Override
  public void shutdown() {
    if ( logger.isTraceEnabled() ) {
      logger.trace( "Shutting down authorization decision cache..." );
    }

    try {
      this.cache.close();
    } catch ( Exception e ) {
      logger.error( "Error closing authorization decision cache", e );
    }

    if ( logger.isTraceEnabled() ) {
      logger.trace( "Shut down authorization decision cache successfully" );
    }
  }
}
