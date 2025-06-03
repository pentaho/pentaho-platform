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

package org.pentaho.platform.plugin.services.cache;

import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.cache.spi.support.TimestampsRegionTemplate;

public class HvTimestampsRegion extends TimestampsRegionTemplate {
  StorageAccess storageAccess;

  public HvTimestampsRegion( String name, RegionFactory regionFactory, StorageAccess storageAccess) {
    super( name, regionFactory, storageAccess );
  }

  public StorageAccess getStorageAccess() {
    return (StorageAccess) super.getStorageAccess();
  }
}
