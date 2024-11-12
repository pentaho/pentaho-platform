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


package org.pentaho.platform.engine.core.system.boot;

public class PlatformInitializationException extends Exception {
  private static final long serialVersionUID = 6886731993305469276L;

  public PlatformInitializationException( String message, Throwable cause ) {
    super( message, cause );
  }

  public PlatformInitializationException( String message ) {
    super( message );
  }
}
