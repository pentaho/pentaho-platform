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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.security;

import java.util.Map;

public interface ILoginAttemptService {

  /**
   * Called when a login attempt succeeds, clearing any failed attempts for the key
   * @param key the key (IP address or username) that successfully logged in
   */
  void loginSucceeded( String key );

  /**
   * Called when a login attempt fails, incrementing the failed attempt count for the key
   * @param key the key (IP address or username) that failed to log in
   */
  void loginFailed( String key );

  /**
   * Checks if the given key is currently blocked due to too many failed login attempts
   * @param key the key (IP address or username) to check
   * @return true if the key is blocked, false otherwise
   */
  boolean isBlocked( String key );

  /**
   * Get all entries in the attempts cache
   * @return Map of all cache entries
   */
  Map<String, Integer> getAllAttempts();

  /**
   * Remove a specific key from the cache
   * @param key the key to remove
   */
  void removeFromCache( String key );

  /**
   * Clear all entries from the cache
   */
  void clearCache();

  /**
   * Get the maximum number of allowed attempts
   * @return maximum attempts
   */
  int getMaxAttempt();
}
