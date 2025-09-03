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

import java.util.Date;

/**
 * Augments the parameter provider by adding settings for the parameters. Some parameter providers allow setting
 * and updating values.
 * 
 * @author jdixon
 */

public interface IParameterSetter extends IParameterProvider {

  /**
   * Sets a named parameter to a <tt>String</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>String</tt> value to set
   */
  public void setParameter( String name, String value );

  /**
   * Sets a named parameter to a <tt>long</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>long</tt> value to set
   */
  public void setParameter( String name, long value );

  /**
   * Sets a named parameter to a <tt>Date</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>Date</tt> value to set
   */
  public void setParameter( String name, Date value );

  /**
   * Sets a named parameter to a <tt>Object</tt> value
   * 
   * @param name
   *          name of the parameter to set
   * @param value
   *          The <tt>Object</tt> value to set
   */
  public void setParameter( String name, Object value );

}
