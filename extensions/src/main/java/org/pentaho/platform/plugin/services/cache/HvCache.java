/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.plugin.services.cache;

import org.hibernate.Cache;
import org.hibernate.cache.spi.DirectAccessRegion;
import org.hibernate.cache.spi.support.StorageAccess;

import java.util.Set;

public interface HvCache extends Cache {
  /**
   * Return all keys for the region
   * @return
   */
  Set getAllKeys( );

  /**
   * Return built in cache access
   * @return
   */
  DirectAccessRegion getDirectAccessRegion();

  /**
   * Return the object that allows direct storage access
   * @return
   */
  StorageAccess getStorageAccess();
}
