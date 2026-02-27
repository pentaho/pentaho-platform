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


package org.pentaho.platform.web.gwt.rpc.util;

/**
 * The <code>ThrowingSupplier</code> represents a supplier of a value, of type <code>T</code> which,
 * when failing, throws an exception of type <code>E</code>.
 * @param <T> The type of value returned.
 * @param <E> The type of value thrown when it is not possible to obtain the value.
 */
public interface ThrowingSupplier<T, E extends Throwable> {
  /**
   * Gets the value.
   * @return The value, possibly <code>null</code>.
   * @throws E When it is not possible to obtain the value.
   */
  T get() throws E;
}
