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
 * An exception raised when an {@link IPlatformPlugin} fails to register.
 * 
 * @author aphillips
 */
public class PlatformPluginRegistrationException extends Exception {

  private static final long serialVersionUID = 1791609786938478691L;

  public PlatformPluginRegistrationException( String message, Throwable cause ) {
    super( message, cause );
  }

  public PlatformPluginRegistrationException( String message ) {
    super( message );
  }
}
