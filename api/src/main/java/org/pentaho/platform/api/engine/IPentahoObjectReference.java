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
  Integer DEFAULT_RANKING = 0;

  Map<String, Object> getAttributes();

  T getObject();

  /**
   * Get the declared type of the Object described/supplied by this reference. Implementations should not require the
   * construction of the Object to determine its type. The actual Object must inherit from the type returned from this
   * method.
   *
   * @return declared type of this reference
   */
  Class<?> getObjectClass();

  /**
   * Returns the ranking of the reference. This ranking is used when determining which of several references should be
   * returned from a query. It also controls the ordering of reference lists.
   * <p/>
   * Zero is the default, {@link #DEFAULT_RANKING}. The higher the number the higher the ranking. Collections of
   * references are ordered highest ranking first.
   *
   * @return Integer ranking
   */
  Integer getRanking();
}
