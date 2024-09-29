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
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */
package org.pentaho.platform.api.scheduler2;

/**
 * This interface is used register and resolve action class name
 */
public interface IActionClassResolver {
  /**
   * This method is used to resolve a action class name and returns a bean id that is registered with this class name
   * @param className
   * @return bean id
   */
  String resolve( String className );

  /**
   * This method provides a component way to register a bean id matching a action class name
   * @param className
   * @param beanId
   */
  void register( String className, String beanId );
}
