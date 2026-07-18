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

import java.util.List;
import java.util.Map;

public interface IPentahoSystemInitializer {

  @SuppressWarnings( "rawtypes" )
  public boolean init( final IApplicationContext pApplicationContext, final Map listenerMap );

  public boolean getInitializedOK();

  public int getInitializedStatus();

  public List<String> getInitializationFailureMessages();

  public void addInitializationFailureMessage( final int failureBit, final String message );

}
