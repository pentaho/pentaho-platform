/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/
package org.pentaho.platform.api.importexport;

public class ImportException extends Exception {
  public ImportException( ) {
  }

  public ImportException( final String message ) {
    super( message );
  }

  public ImportException( final String message, final Throwable cause ) {
    super( message, cause );
  }

  public ImportException( final Throwable cause ) {
    super( cause );
  }
}

