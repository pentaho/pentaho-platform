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
    Assert.hasText( preAuthenticationToken );
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
