/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.web.http.security;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;

/**
 * An authentication exception which represents the failure of CSRF validation during authentication.
 * <p>
 * The specific CSRF validation failure depends on the specific class of {@link #getCause()},
 * {@link org.springframework.security.web.csrf.MissingCsrfTokenException} or
 * {@link org.springframework.security.web.csrf.InvalidCsrfTokenException}.
 * Other errors such as {@link javax.servlet.ServletException} or {@link java.io.IOException}
 * may also result from CSRF validation.
 */
public class CsrfValidationAuthenticationException extends AuthenticationException {
  public CsrfValidationAuthenticationException( @NonNull Throwable cause ) {
    super( "CSRF validation failed", cause );
  }
}
