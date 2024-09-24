/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.api.engine;

public interface ISessionStartupAction {

  public String getSessionType();

  public void setSessionType( String sessionType );

  public String getActionOutputScope();

  public void setActionOutputScope( String actionOutputScope );

  public String getActionPath();

  public void setActionPath( String actionPath );
}
