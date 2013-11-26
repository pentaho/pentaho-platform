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

package org.pentaho.platform.util.beans;

/**
 * A callback object used in the process of bean property set operations.
 * 
 * @author aphillips
 * @see BeanUtil
 */
public interface ValueGenerator {

  /**
   * Calls back when the bean utility has verified that it can set the bean property. You are asked for the value
   * to set here and not earlier on because we want to delay any processing necessary to derive this value until
   * the last possible moment. Sometimes deriving the value can be costly, there is no sense requiring the value
   * prior to simple checks happening first.
   * 
   * @param name
   *          the property name for the requested value
   * @return the value of the property for setting on the bean
   * @throws Exception
   *           throw an exception here if/when your code encounters a terminal state
   */
  Object getValue( String name ) throws Exception;
}
