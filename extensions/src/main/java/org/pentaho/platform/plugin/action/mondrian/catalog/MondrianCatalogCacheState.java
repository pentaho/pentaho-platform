/*!
 *
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
 *
 * Copyright (c) 2002-2020 Hitachi Vantara. All rights reserved.
 *
 */
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
