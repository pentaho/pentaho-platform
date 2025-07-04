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


package org.pentaho.platform.plugin.services.importexport;

/**
 * Exception generated when any initialization is unsuccessful User: dkincade
 */
public class InitializationException extends Exception {
  public InitializationException() {
  }

  public InitializationException( final String message ) {
    super( message );
  }

  public InitializationException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public InitializationException( final Throwable cause ) {
    super( cause );
  }
}
