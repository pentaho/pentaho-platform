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
