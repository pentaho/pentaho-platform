/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.plugin.services.cache;

import net.sf.ehcache.Ehcache;
import org.hibernate.Cache;
import org.hibernate.cache.ehcache.internal.StorageAccessImpl;
import org.hibernate.cache.spi.DirectAccessRegion;

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
  StorageAccessImpl getStorageAccess();

  /**
   * Exposes the underlaying EhCache associated with this HvCache
   * @return The EhCache
   */
  Ehcache getCache();

}
