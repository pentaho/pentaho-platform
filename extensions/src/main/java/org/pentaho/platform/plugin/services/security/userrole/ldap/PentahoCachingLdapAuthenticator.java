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
 * Copyright (c) 2020 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.authentication.LdapAuthenticator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class PentahoCachingLdapAuthenticator implements LdapAuthenticator {
  private static final Log logger = LogFactory.getLog( PentahoCachingLdapAuthenticator.class );
  private static final String REGION_DEFAULT_NAME = "ldapAuthenticatorCache";
  private static final String PASSWORD_HASH_METHOD = "SHA-256";

  private String cacheRegionName = REGION_DEFAULT_NAME;
  private String passwordHashMethod = PASSWORD_HASH_METHOD;

  private MessageDigest messageDigest;
  private final LdapAuthenticator delegate;
  private final ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
  private static final String ROLES_BY_USER = "AuthenticatorCache_";
  private static final int HASH_SALT = ( new Random() ).nextInt();

  public PentahoCachingLdapAuthenticator( LdapAuthenticator delegate ) {
    if ( delegate == null ) {
      throw new IllegalArgumentException( "delegate LdapAuthenticator cannot be null" );
    }
    this.delegate = delegate;
    if ( !this.cacheManager.cacheEnabled( cacheRegionName ) ) {
      this.cacheManager.addCacheRegion( cacheRegionName );
    }

    try {
      this.messageDigest = MessageDigest.getInstance( PASSWORD_HASH_METHOD );
    } catch ( NoSuchAlgorithmException e ) {
      throw new IllegalArgumentException( "Issue trying to create a messageDigest for MD5" );
    }
  }

  private interface DelegateOperation {
    DirContextOperations perform();
  }

  private DirContextOperations performOperation( Authentication authentication, DelegateOperation operation ) {
    DirContextOperations results = null;
    Object fromRegionCache = null;

    String cacheEntry = ROLES_BY_USER + hashUserAndPassword( authentication );
    if ( logger.isTraceEnabled() ) {
      logger.trace( "cacheEntry:" + cacheEntry );
    }
    fromRegionCache = cacheManager.getFromRegionCache( cacheRegionName, cacheEntry );

    if ( fromRegionCache instanceof DirContextOperations ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Cache Hit for " + authentication.getPrincipal() );
      }
      results = (DirContextOperations) fromRegionCache;
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Cache miss for " + authentication.getPrincipal() );
      }
      results = operation.perform();
      cacheManager.putInRegionCache( cacheRegionName, cacheEntry, results );
    }
    return results;
  }

  @Override
  public DirContextOperations authenticate( Authentication authentication ) {
    return performOperation( authentication, () -> delegate.authenticate( authentication ) );
  }

  protected String hashUserAndPassword( Authentication authentication ) {
    String stringToEncrypt = HASH_SALT + ":" + authentication.getPrincipal() + ":" + authentication.getCredentials();
    String encryptedString = new String( messageDigest.digest( stringToEncrypt.getBytes() ) );

    //To protect from Odd characters in the CACHE KEY, we will convert to base64
    return new String( Base64.getEncoder().encode( encryptedString.getBytes() ) );
  }

  public String getCacheRegionName() {
    return cacheRegionName;
  }

  public void setCacheRegionName( String cacheRegionName ) {
    this.cacheRegionName = cacheRegionName;
  }

  public String getPasswordHashMethod() {
    return passwordHashMethod;
  }

  public void setPasswordHashMethod( String passwordHashMethod ) {
    this.passwordHashMethod = passwordHashMethod;

    try {
      this.messageDigest = MessageDigest.getInstance( passwordHashMethod );
    } catch ( NoSuchAlgorithmException e ) {
      throw new IllegalArgumentException( "hashMethod NoSuchAlgorithmException, default is SHA-256" );
    }
  }

}
