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
