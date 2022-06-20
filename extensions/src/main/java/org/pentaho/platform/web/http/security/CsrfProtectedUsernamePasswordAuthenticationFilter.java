/*!
 *
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
 *
 * Copyright (c) 2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.security;

import com.hitachivantara.security.web.model.servop.annotation.ServiceId;
import com.hitachivantara.security.web.service.csrf.servlet.CsrfValidator;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Processes an authentication form submission in a CSRF safe way.
 */
@ServiceId
public class CsrfProtectedUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
  /**
   * The operation name, which can be used to disable CSRF validation via configuration.
   */
  public static final String CSRF_OPERATION_NAME = "authenticate";

  @Nullable
  private CsrfValidator csrfValidator;

  /**
   * Creates an instance of the filter which has no CSRF processor set.
   */
  public CsrfProtectedUsernamePasswordAuthenticationFilter() {
    this( null );
  }

  /**
   * Creates an instance of the filter with a given CSRF processor.
   *
   * @param csrfValidator The CSRF validator used to validate requests or <code>null</code>.
   */
  public CsrfProtectedUsernamePasswordAuthenticationFilter( @Nullable CsrfValidator csrfValidator ) {
    super();
    this.csrfValidator = csrfValidator;
  }

  /**
   * Sets the CSRF validator used to validate requests w.r.t CSRF attacks.
   * When set to <code>null</code>, CSRF validation is disabled. Although, note, it is preferable to use
   * this operation's identifier to disable it via configuration.
   *
   * @param csrfValidator The CSRF validator.
   */
  public void setCsrfValidator( @Nullable CsrfValidator csrfValidator ) {
    this.csrfValidator = csrfValidator;
  }

  /**
   * Gets the CSRF validator used to validate requests w.r.t CSRF attacks, if any.
   */
  @Nullable
  public CsrfValidator getCsrfValidator() {
    return csrfValidator;
  }

  @Override
  public Authentication attemptAuthentication( HttpServletRequest request, HttpServletResponse response )
    throws CsrfValidationAuthenticationException, InternalAuthenticationServiceException {

    if ( csrfValidator != null ) {
      try {
        csrfValidator.validateRequestOfMutationOperation( request, this.getClass(), CSRF_OPERATION_NAME );
      } catch ( AccessDeniedException ex ) {
        throw new CsrfValidationAuthenticationException( ex );
      } catch ( ServletException | IOException ex ) {
        throw new InternalAuthenticationServiceException( "CSRF validation failed", ex );
      }
    }

    return super.attemptAuthentication( request, response );
  }
}
