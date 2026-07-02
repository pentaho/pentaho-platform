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


package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collection;

public class PentahoCachingLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {
  private static final Log logger = LogFactory.getLog( PentahoCachingLdapAuthoritiesPopulator.class );
  private static final String REGION_DEFAULT_NAME = "ldapPopulatorCache";

  private String cacheRegionName = REGION_DEFAULT_NAME;

  private final LdapAuthoritiesPopulator delegate;
  private final ICacheManager cacheManager = PentahoSystem.getCacheManager( null );
  private static final String ROLES_BY_USER = "GrantedAuthority by user ";

  public PentahoCachingLdapAuthoritiesPopulator( LdapAuthoritiesPopulator delegate ) {
    if ( delegate == null ) {
      throw new IllegalArgumentException( "delegate LdapAuthoritiesPopulator cannot be null" );
    }
    this.delegate = delegate;
    if ( !this.cacheManager.cacheEnabled( cacheRegionName ) ) {
      this.cacheManager.addCacheRegion( cacheRegionName );
    }
  }

  @SuppressWarnings( "squid:S1452" )
  private interface DelegateOperation {
    Collection<? extends GrantedAuthority> perform();
  }

  @SuppressWarnings( "unchecked" )
  private Collection<? extends GrantedAuthority> performOperation( String cacheEntry, DelegateOperation operation ) {
    Collection<? extends GrantedAuthority> results = null;
    Object fromRegionCache = cacheManager.getFromRegionCache( cacheRegionName, cacheEntry );
    if ( fromRegionCache instanceof Collection ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Cache Hit for  " + cacheEntry );
      }
      results = (Collection<GrantedAuthority>) fromRegionCache;
    } else {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "Cache miss for  " + cacheEntry );
      }
      results = operation.perform();
      cacheManager.putInRegionCache( cacheRegionName, cacheEntry, results );
    }
    return new ArrayList<>( results );
  }

  @Override
  public Collection<? extends GrantedAuthority> getGrantedAuthorities( DirContextOperations userData,
                                                                       String username ) {
    return performOperation( ROLES_BY_USER + username, () -> delegate.getGrantedAuthorities( userData, username ) );
  }

  public String getCacheRegionName() {
    return cacheRegionName;
  }

  public void setCacheRegionName( String cacheRegionName ) {
    this.cacheRegionName = cacheRegionName;
  }

}
