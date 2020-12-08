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
import java.util.HashMap;
import java.util.Map;

/**
 * represents the entire regional cache structure for caching Mondrian Catalogs.  The cache is optimized so it can be
 * built incrementally, one catalog at a time, or fully depending upon demands.  The {@Link MondrianCatalogCacheState}
 * tracks whether and and when the cache was fully loaded.
 */
public class MondrianCatalogCache implements Serializable {

  private MondrianCatalogCacheState mondrianCatalogCacheState = new MondrianCatalogCacheState();
  private Map<String, MondrianCatalog> catalogs = new HashMap<>();

  public MondrianCatalogCacheState getMondrianCatalogCacheState() {
    return mondrianCatalogCacheState;
  }

  public void setMondrianCatalogCacheState(
    MondrianCatalogCacheState mondrianCatalogCacheState ) {
    this.mondrianCatalogCacheState = mondrianCatalogCacheState;
  }

  public Map<String, MondrianCatalog> getCatalogs() {
    return catalogs;
  }

  public void setCatalogs( Map<String, MondrianCatalog> catalogs ) {
    this.catalogs = catalogs;
  }

  public void putCatalog( String key, MondrianCatalog catalog ) {
    catalogs.put( key, catalog );
  }

  public MondrianCatalog getCatalog( String context ) {
    return catalogs.get( context );
  }

  public void setFullLoad() {
    mondrianCatalogCacheState.isFullyLoaded();
    mondrianCatalogCacheState.setLastFullUpdate( Instant.now() );
  }
}
