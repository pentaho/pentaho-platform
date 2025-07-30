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

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.util.Assert;

import javax.jcr.Credentials;
import javax.jcr.SimpleCredentials;

/**
 * A {@link CredentialsStrategy} that creates credentials from the current {@link IPentahoSession}.
 * 
 * @author mlowery
 */
public class PentahoSessionCredentialsStrategy implements CredentialsStrategy {

  // ~ Static fields/initializers
  // ======================================================================================

  private static final String ATTR_PRE_AUTHENTICATION_TOKEN = "pre_authentication_token"; //$NON-NLS-1$

  private static final char[] PASSWORD = "ingnored".toCharArray(); //$NON-NLS-1$

  // ~ Instance fields
  // =================================================================================================

  private String preAuthenticationToken;
  private ITenantedPrincipleNameResolver tenantedUserNameUtils;

  // ~ Constructors
  // ====================================================================================================

  public PentahoSessionCredentialsStrategy( final String preAuthenticationToken,
      final ITenantedPrincipleNameResolver tenantedUserNameUtils ) {
    super();
    Assert.hasText( preAuthenticationToken, "The pre-authentication token must not be null or empty. Ensure a valid token is provided." );
    this.preAuthenticationToken = preAuthenticationToken;
    this.tenantedUserNameUtils = tenantedUserNameUtils;
  }

  // ~ Methods
  // =========================================================================================================

  public Credentials getCredentials() {
    String userId = getUserId();
    SimpleCredentials creds = new SimpleCredentials( userId, PASSWORD );
    creds.setAttribute( ATTR_PRE_AUTHENTICATION_TOKEN, preAuthenticationToken );
    return creds;
  }

  private String getUserId() {
    IPentahoSession pentahoSession = PentahoSessionHolder.getSession();
    Assert.state( pentahoSession != null, "this method cannot be called with a null IPentahoSession" );
    return JcrTenantUtils.getTenantedUser( pentahoSession.getName() );
  }
}
