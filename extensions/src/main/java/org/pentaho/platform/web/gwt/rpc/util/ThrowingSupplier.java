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
 * Copyright (c) 2021 Hitachi Vantara. All rights reserved.
 */

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
