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


package org.pentaho.platform.api.security;

import java.util.Map;

public interface ILoginAttemptService {

  /**
   * Called when a login attempt succeeds.
   * It clears the failed attempts counter for the key.
   *
   * @param key the key (IP address or username) that successfully logged in.
   */
  void loginSucceeded( String key );

  /**
   * Called when a login attempt fails.
   * It increments the failed attempt counter for the key.
   *
   * @param key the key (IP address or username) that failed to log in.
   */
  void loginFailed( String key );

  /**
   * Checks if the given key is currently blocked due to too many failed login attempts.
   *
   * @param key the key (IP address or username) to check.
   * @return true if the key is blocked, false otherwise.
   */
  boolean isBlocked( String key );

  /**
   * Get all entries in the attempts cache as an unmodifiable Map.
   * Each Map entry represents a key and its corresponding number of failed login attempts.
   *
   * @return an unmodifiable Map of all cache entries
   */
  Map<String, Integer> getAllAttempts();

  /**
   * Remove a specific key from the cache.
   * The key will be unblocked whatever its former state.
   *
   * @param key the key to remove.
   */
  void removeFromCache( String key );

  /**
   * Clear all entries from the cache.
   * All keys will be unblocked whatever their former state.
   */
  void clearCache();

  /**
   * Get the maximum number of failed login attempts allowed before blocking a key.
   *
   * @return maximum number of failed login attempts allowed before blocking a key.
   */
  int getMaxAttempt();
}
