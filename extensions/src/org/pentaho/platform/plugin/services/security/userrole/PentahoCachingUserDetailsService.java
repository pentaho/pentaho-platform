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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;

/**
 * User: nbaker Date: 8/7/13
 * 
 * Copy of the org.springframework.security.config.CachingUserDetailsService for modification due to poor extensibility
 * of core class.
 */
public class PentahoCachingUserDetailsService implements UserDetailsService {
  private UserCache userCache = new NullUserCache();
  private UserDetailsService delegate;
  private final ITenantedPrincipleNameResolver nameResolver;

  public PentahoCachingUserDetailsService( UserDetailsService delegate, ITenantedPrincipleNameResolver nameResolver ) {
    this.delegate = delegate;
    this.nameResolver = nameResolver;
  }

  public UserCache getUserCache() {
    return userCache;
  }

  public void setUserCache( UserCache userCache ) {
    this.userCache = userCache;
  }

  /**
   * Gets the principle id from the principle name TODO: This method will not work with multi-tenancy
   */
  private String getPrincipleId( String principleName ) {
    ITenant tenant = JcrTenantUtils.getCurrentTenant();
    if ( tenant == null || tenant.getId() == null ) {
      tenant = JcrTenantUtils.getDefaultTenant();
    }
    return nameResolver.getPrincipleId( tenant, principleName );
  }

  public UserDetails loadUserByUsername( String username ) {
    boolean tenanted = JcrTenantUtils.isTenantedUser( username );
    String principleName = tenanted ? JcrTenantUtils.getPrincipalName( username, true ) : username;

    UserDetails user = userCache.getUserFromCache( principleName );

    if ( user == null ) {
      String principleId = tenanted ? username : getPrincipleId( username );
      try {
        user = delegate.loadUserByUsername( principleId );
      } catch ( UsernameNotFoundException e ) {
        user = new NotFoundUserDetails( principleName, e );
      }

      if ( user == null ) {
        user =
            new NotFoundUserDetails( principleName, new UsernameNotFoundException( "UserDetailsService " + delegate
                + " returned null for username " + username + ". " + "This is an interface contract violation" ) );
      }

      userCache.putUserInCache( user );
    }

    if ( user instanceof NotFoundUserDetails ) {
      UsernameNotFoundException e = ( (NotFoundUserDetails) user ).getOriginalException();
      throw new UsernameNotFoundException( e.getMessage(), e );
    }

    return user;
  }
}
