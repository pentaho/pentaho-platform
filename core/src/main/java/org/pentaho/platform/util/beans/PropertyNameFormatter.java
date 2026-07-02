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
