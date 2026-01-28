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


package org.pentaho.platform.web.http.security;

import org.springframework.security.core.AuthenticationException;

public class PreventBruteForceException extends AuthenticationException {

  public PreventBruteForceException( String message ) {
    super( message );
  }

  public PreventBruteForceException( String message, Throwable throwable ) {
    super( message, throwable );
  }
}
