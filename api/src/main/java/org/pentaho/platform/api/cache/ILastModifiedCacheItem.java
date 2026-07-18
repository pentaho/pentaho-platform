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
