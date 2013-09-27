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
