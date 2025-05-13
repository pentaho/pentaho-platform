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

/**
 * The interface for a POJO component that wants to be provided with a logger. This is an optional interface for
 * POJO components
 * 
 * @author jamesdixon
 * @deprecated Pojo components are deprecated, use {@link org.pentaho.platform.api.action.IAction}
 */
@Deprecated
public interface ILoggablePojo {

  /**
   * Sets the logger for the POJO component to use
   * 
   * @param logger
   */
  public void setLogger( ILogger logger );

}
