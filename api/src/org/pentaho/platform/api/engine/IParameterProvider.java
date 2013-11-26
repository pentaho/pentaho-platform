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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;

/**
 * A ParameterProvider is a source for input, output or resource parameters for a given action. The parameter
 * definitions exist within the SequenceDefinition. The values for the parameters are supplied from the
 * ParameterProvider.
 */
public interface IParameterProvider {

  public static final String SCOPE_REQUEST = "request"; //$NON-NLS-1$

  public static final String SCOPE_SESSION = "session"; //$NON-NLS-1$

  /**
   * Retrieve the requested parameter as type java.lang.String
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter, or the defaultValue if not found
   */
  public String getStringParameter( String name, String defaultValue );

  /**
   * Retrieve the requested parameter as primitive Java type long.
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter, or the defaultValue if not found
   */
  public long getLongParameter( String name, long defaultValue );

  /**
   * Retrieve the requested parameter as type java.util.Date.
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter, or the defaultValue if not found
   */
  public Date getDateParameter( String name, Date defaultValue );

  /**
   * Retrieve the requested parameter as decimal, returning a java.lang.Object.
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter, or the defaultValue if not found
   */
  public BigDecimal getDecimalParameter( String name, BigDecimal defaultValue );

  /**
   * Retrieve the requested parameter as an Object array
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter converted to an array if it wasn't already an array, or the defaultValue
   *         if not found
   */
  public Object[] getArrayParameter( String name, Object[] defaultValue );

  /**
   * Retrieve the requested parameter as a String array
   * 
   * @param name
   *          name of parameter to retrieve
   * @param defaultValue
   *          value to return if the named parameter can not be found
   * @return value of requested parameter converted to a String array if it wasn't already a String array, or the
   *         defaultValue if not found
   */
  public String[] getStringArrayParameter( String name, String[] defaultValue );

  /**
   * Return list of all avialable parameter names in this provider
   * 
   * @return Set of parameter names
   */
  @SuppressWarnings( "rawtypes" )
  public Iterator getParameterNames();

  /**
   * Gets the named parameter from the provider as it's native type
   * 
   * @param name
   *          The name of the parameter to retrieve
   * @return The native object
   */
  public Object getParameter( String name );

  /**
   * @param name
   *          Name of the parameter to look up
   * @return true if the parameter exists in the parameter provider
   */
  public boolean hasParameter( String name );

}
