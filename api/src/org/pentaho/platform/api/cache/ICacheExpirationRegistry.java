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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.cache;

import java.util.List;

/**
 * This interface defines the API for a registry of <code>ILastModifiedCacheItem</code>'s. All items in the
 * registry will have a last modified timestamp that can be used to determine if a cache item is up-to-date or not
 * 
 * @author rfellows
 * @see org.pentaho.platform.api.cache.ILastModifiedCacheItem
 */
public interface ICacheExpirationRegistry {
  /**
   * Adds an item to the registry
   * 
   * @param cacheItem
   */
  public void register( ILastModifiedCacheItem cacheItem );

  /**
   * . Removes an item from the registry
   * 
   * @param cacheItem
   */
  public void unRegister( ILastModifiedCacheItem cacheItem );

  /**
   * Gets a <code>List</code> of all items registered
   * 
   * @return
   */
  public List<ILastModifiedCacheItem> getCachedItems();
}
