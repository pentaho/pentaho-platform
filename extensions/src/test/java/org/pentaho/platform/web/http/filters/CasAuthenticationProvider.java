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


package org.pentaho.platform.web.http.filters;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Is needed in {@link HttpSessionPentahoSessionIntegrationFilterTest}
 */
class CasAuthenticationProvider implements AuthenticationProvider {

  @Override public Authentication authenticate( Authentication authentication ) throws AuthenticationException {
    return null;
  }

  @Override public boolean supports( Class<?> authentication ) {
    return false;
  }
}
