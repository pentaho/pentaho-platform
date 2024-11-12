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


package org.pentaho.platform.api.data;

import org.pentaho.commons.connection.IDisposable;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.platform.api.engine.IComponent;

import java.util.Map;

/**
 * The prepared component interface extends component, allowing components to go into a prepared state vs. execute
 * state. These components may place themselves as an output parameter, and then be used later in the
 * action-sequence for execution of prepared statements / etc.
 * 
 * @author Will Gorman
 * 
 * @see MDXBaseComponent
 * @see SQLBaseComponent
 * @see IPentahoResultSet
 * 
 */
public interface IPreparedComponent extends IComponent, IDisposable {

  /**
   * this term may appear when resolving parameters "{PREPARELATER:PARAM_NAME}"
   */
  public static final String PREPARE_LATER_PREFIX = "PREPARELATER"; //$NON-NLS-1$

  /**
   * this is an intermediate term used when resolving parameters in the executePrepared call
   */
  public static final String PREPARE_LATER_INTER_PREFIX = "PREPARELATER_INTER"; //$NON-NLS-1$

  /**
   * The name of the output. If this appears as an output of an IPreparedComponent the component should alter its
   * behavior and go into prepared mode.
   */
  public static final String PREPARED_COMPONENT_NAME = "prepared_component"; //$NON-NLS-1$

  /**
   * The type of the output. If this appears in an output of an IPreparedComponent the component should alter its
   * behavior and go into prepared mode.
   */
  public static final String PREPARED_OUTPUT_TYPE = "prepared_component"; //$NON-NLS-1$

  /**
   * A placeholder for template strings and potential prepared lists, so template fields can be replaced on the fly
   * vs. during initial setup of a prepared statement
   */
  public static final String PREPARE_LATER_PLACEHOLDER = "prepare-later-placeholder"; //$NON-NLS-1$

  /**
   * executes a prepared method that returns a result set executePrepared looks up any "PREPARELATER" params in the
   * preparedParams map.
   * 
   * @param preparedParams
   *          a map of possible parameters.
   * @return result set
   */
  @SuppressWarnings( "rawtypes" )
  public IPentahoResultSet executePrepared( Map preparedParams );

  /**
   * exposes the connection object for others to use. The connection object in a prepared component is not closed
   * until parameters are disposed at the end of an action sequence execution.
   * 
   * Note: getConnection was already in use when naming this method.
   * 
   * @return connection object
   */
  public IPentahoConnection shareConnection();

  /**
   * Disposes of resources held by the prepared component
   */
  public void dispose();

}
