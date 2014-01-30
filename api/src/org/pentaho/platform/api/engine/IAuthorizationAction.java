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

package org.pentaho.platform.api.engine;

/**
 * 
 * Represents a Logical Role name used by some IAuthorizationPolicy implementations. Also known as Action-Based Security
 * 
 * User: nbaker Date: 3/19/13
 */
public interface IAuthorizationAction {
  /**
   * Get the name of the action
   * 
   * @return action name
   */
  String getName();

  /**
   * Get the localized display name of action for a specific locale. If null is passed then default locale will be used
   * 
   * @param locale
   * @return localized name
   */
  String getLocalizedDisplayName( String locale );
}
