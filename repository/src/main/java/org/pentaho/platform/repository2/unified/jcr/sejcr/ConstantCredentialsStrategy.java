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
    Assert.hasText( userID, "The user ID must not be null or empty. Ensure a valid user ID is provided." );
    Assert.hasText( preAuthenticationToken, "The pre-authentication token must not be null or empty. Ensure a valid token is provided." );
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
