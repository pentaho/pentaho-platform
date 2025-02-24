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

import java.io.OutputStream;

/**
 * An OutputDef represents one output parameter in a SequenceDefinition or ActionDefinition.
 */
public interface IOutputDef {

  /**
   * Retrieves the type of the output parameter.
   * 
   * @return the output parameter type
   */
  public String getType();

  /**
   * Retrieves the name of the output parameter.
   * 
   * @return the name of the ouotput parameter
   */
  public String getName();

  /**
   * Determine whether the value associated with this parameter is a list or not.
   * 
   * @return rue if the parameter value is a list, otherwise false
   */
  public boolean isList();

  /**
   * Sets the value of the output parameter.
   * 
   * @param value
   *          the value to set
   */
  public void setValue( Object value );

  /**
   * Retrieve the OutputStream associated with this output parameter.
   * 
   * @return the OutputStream for this parameter
   */
  public OutputStream getOutputStream();

  /**
   * Adds the given value to the value list for this output parameter.
   * 
   * @param value
   *          value to add to the parameter value list.
   */
  public void addToList( Object value );
}
