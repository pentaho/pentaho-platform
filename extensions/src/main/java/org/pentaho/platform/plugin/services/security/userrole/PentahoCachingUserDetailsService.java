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


package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
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

    return new User( user.getUsername(), ( user.getPassword() == null ? "ignored" : user.getPassword() ), user.isEnabled(), user.isAccountNonExpired(),
      user.isCredentialsNonExpired(), user.isAccountNonLocked(), user.getAuthorities() );
  }
}
