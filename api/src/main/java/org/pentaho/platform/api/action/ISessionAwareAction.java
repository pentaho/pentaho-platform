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


package org.pentaho.platform.api.action;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * The interface for an Action that wants to be provided with a session.
 * 
 * @see IAction
 * @author aphillips
 * @since 3.6
 */
public interface ISessionAwareAction extends IAction {

  public void setSession( IPentahoSession session );

}
