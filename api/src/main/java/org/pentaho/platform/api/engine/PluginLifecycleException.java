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

import org.pentaho.platform.api.util.PentahoCheckedChainedException;

public class PluginLifecycleException extends PentahoCheckedChainedException {

  private static final long serialVersionUID = 974L;

  public PluginLifecycleException() {
    super();
  }

  public PluginLifecycleException( final String message ) {
    super( message );
  }

  public PluginLifecycleException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public PluginLifecycleException( final Throwable reas ) {
    super( reas );
  }

}
