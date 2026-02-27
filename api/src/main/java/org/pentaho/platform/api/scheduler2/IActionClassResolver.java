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
