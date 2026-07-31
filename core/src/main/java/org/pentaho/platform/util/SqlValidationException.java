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


package org.pentaho.platform.util;

/**
 * Thrown when a user supplied SQL string fails validation, meaning that it is not a single, read-only
 * <code>SELECT</code> statement.
 *
 * @see SqlQueryValidator
 */
public class SqlValidationException extends Exception {
  private static final long serialVersionUID = 3427391050374829961L;

  public SqlValidationException( final String message ) {
    super( message );
  }

  public SqlValidationException( final String message, final Throwable cause ) {
    super( message, cause );
  }
}
