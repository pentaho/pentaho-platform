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

import java.util.Map;

/**
 * User: nbaker Date: 1/16/13
 * <p/>
 * IPentahoObjectReference objects represent a pointer to an available object in the IPentahoObjectFactory and contains
 * all attributes associated with that object (priority, adhoc attributes).
 * <p/>
 * Implementations of this class should be lazy where possible, deferring retrieval of the Object until the getObject()
 * method is called
 */
public interface IPentahoObjectReference<T> extends Comparable<IPentahoObjectReference<T>> {
  Map<String, Object> getAttributes();
  Integer DEFAULT_RANKING = 0;

  T getObject();

  /**
   * Get the declared type of the Object described/supplied by this reference. Implementations should not require the
   * construction of the Object to determine it's type. The actual Object must inherit from the type returned from this
   * method.
   *
   * @return declared type of this reference
   */
  Class<?> getObjectClass();

  /**
   * Returns the ranking of the reference. This ranking is used when determining which of several references should be
   * returned from a query. It also controls the ordering of reference lists.
   * <p/>
   * Zero is the default. The higher the number the higher the ranking. Collections of references are ordered highest
   * ranking first.
   *
   * @return Integer ranking
   */
  Integer getRanking();
}
