/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.api.engine;

/**
 * @author wseyler Interface for logoutout listeners. These get called to invalidate caches or other session based
 *         items and to perform any additional cleanup or actions required when a user goes away
 */
public interface ILogoutListener {

  /**
   * @param session
   *          Performs any logout actions based on this session.
   */
  public void onLogout( IPentahoSession session );
}
