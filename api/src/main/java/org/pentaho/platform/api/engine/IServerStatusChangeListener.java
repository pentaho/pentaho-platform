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
 * This is a listener for the IServerStatusProvider which should call onStatusChange whenever the values of
 * the ServerStatus or status message change value.
 * 
 * @author tkafalas
 */
public interface IServerStatusChangeListener {
  /**
   * Called whenever the IServerStatusProvider changes status, or status messages.
   */
  public void onStatusChange( );
}
