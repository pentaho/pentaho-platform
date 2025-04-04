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


package org.pentaho.platform.api.cache;

/**
 * API for cached items that contain a last modified timestamp
 * 
 * @author rfellows
 */
public interface ILastModifiedCacheItem {
  /**
   * Get the timestamp of the last time this cache item was modified
   * 
   * @return
   */
  public long getLastModified();

  /**
   * Get the lookup key for this cache item
   * 
   * @return
   */
  public String getCacheKey();
}
