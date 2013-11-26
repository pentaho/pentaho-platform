/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.repository2.unified.jcr.sejcr;

import org.springframework.util.Assert;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * Uses hard-coded credentials.
 * 
 * @author mlowery
 */
public class ConstantCredentialsStrategy implements CredentialsStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String ATTR_PRE_AUTHENTICATION_TOKEN = "pre_authentication_token"; //$NON-NLS-1$

  private static final char[] PASSWORD = "ignored".toCharArray(); //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private Credentials credentials;

  // ~ Constructors
  // ====================================================================================================

  /**
   * Null credentials.
   */
  public ConstantCredentialsStrategy() {
    super();
  }

  public ConstantCredentialsStrategy( final Credentials credentials ) {
    super();
    this.credentials = credentials;
  }

  public ConstantCredentialsStrategy( final String userID, final String preAuthenticationToken ) {
    Assert.hasText( userID );
    Assert.hasText( preAuthenticationToken );
    SimpleCredentials creds = new SimpleCredentials( userID, PASSWORD );
    creds.setAttribute( ATTR_PRE_AUTHENTICATION_TOKEN, preAuthenticationToken );
    this.credentials = creds;
  }

  // ~ Methods
  // =========================================================================================================

  public Credentials getCredentials() {
    return credentials;
  }
}
