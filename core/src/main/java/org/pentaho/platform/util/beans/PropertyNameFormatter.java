/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

/**
 * Interface for formatters used during the processing of bean set operations.
 * @see BeanUtil
 * 
 * @author aphillips
 */

package org.pentaho.platform.util.beans;

/**
 * {@link PropertyNameFormatter}s are used to synchronize property names coming from a non Java Bean spec compliant
 * system with the actual property names (camelCase) on the target bean.
 * 
 * @author aphillips
 * @see BeanUtil#setValue(Object, String, Object, ValueSetErrorCallback, PropertyNameFormatter...)
 */
public interface PropertyNameFormatter {

  /**
   * Format the name to match the bean property name.
   * 
   * @param name
   *          the name to translate into the property name
   * @return the property name to match the bean
   */
  String format( String name );

}
