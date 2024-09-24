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
