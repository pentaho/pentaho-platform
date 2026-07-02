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

package org.pentaho.platform.plugin.action.mondrian.catalog;

import java.io.Serializable;
import java.time.Instant;

/**
 * Represents the state of the Mondrian Catalog Cache for one region
 */
public class MondrianCatalogCacheState implements Serializable {

  public static final String MONDRIAN_CATALOG_CACHE_STATE_KEY_PREFIX = "cache-state"; //$NON-NLS-1$

  private static final long serialVersionUID = 1L;
  private boolean isFullyLoaded = false;
  private Instant lastFullUpdate;

  public boolean isFullyLoaded() {
    return isFullyLoaded;
  }

  public void setFullyLoaded( boolean fullyLoaded ) {
    isFullyLoaded = fullyLoaded;
  }

  public void setLastFullUpdate( Instant lastFullUpdate ) {
    this.lastFullUpdate = lastFullUpdate;
  }

  public Instant getLastFullUpdate() {
    return lastFullUpdate;
  }

  public void setFullyLoaded() {
    setFullyLoaded( true );
    setLastFullUpdate( Instant.now() );
  }

}
