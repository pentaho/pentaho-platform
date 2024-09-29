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
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */
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
