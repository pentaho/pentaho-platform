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

import java.io.Serializable;

public class PluginBeanException extends Exception implements Serializable {

  private static final long serialVersionUID = 994L;

  public PluginBeanException() {
    super();
  }

  public PluginBeanException( final String message ) {
    super( message );
  }

  public PluginBeanException( final String message, final Throwable reas ) {
    super( message, reas );
  }

  public PluginBeanException( final Throwable reas ) {
    super( reas );
  }

}
