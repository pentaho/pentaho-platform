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
package org.pentaho.platform.api.email;

public class EmailServiceException extends Exception {
  private static final long serialVersionUID = 1L;

  public EmailServiceException() {
    super();
  }

  public EmailServiceException( String message ) {
    super( message );
  }

  public EmailServiceException( String message, Throwable cause ) {
    super( message, cause );
  }

  public EmailServiceException( Throwable cause ) {
    super( cause );
  }
}
