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



package com.pentaho.pdi.ws;

public class RepositorySyncException extends Exception {

  private static final long serialVersionUID = -1106883375641854028L; /* EESOURCE: UPDATE SERIALVERUID */

  public RepositorySyncException( String message, Exception cause ) {
    super( message, cause );
  }

  public RepositorySyncException( String message ) {
    super( message );
  }
}
