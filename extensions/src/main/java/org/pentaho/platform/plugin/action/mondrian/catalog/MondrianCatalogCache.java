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
