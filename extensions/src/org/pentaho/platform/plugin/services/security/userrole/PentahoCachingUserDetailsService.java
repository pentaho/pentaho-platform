package org.pentaho.platform.plugin.services.security.userrole;

import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.repository2.unified.jcr.JcrTenantUtils;
import org.springframework.security.userdetails.UserDetailsService;


import org.springframework.security.providers.dao.UserCache;
import org.springframework.security.providers.dao.cache.NullUserCache;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.util.Assert;

/**
 * User: nbaker
 * Date: 8/7/13
 *
 * Copy of the org.springframework.security.config.CachingUserDetailsService for modification due to
 * poor extensibility of core class.
 */
public class PentahoCachingUserDetailsService implements UserDetailsService {
  private UserCache userCache = new NullUserCache();
  private UserDetailsService delegate;
  private final ITenantedPrincipleNameResolver nameResolver;

  public PentahoCachingUserDetailsService(UserDetailsService delegate, ITenantedPrincipleNameResolver nameResolver) {
    this.delegate = delegate;
    this.nameResolver = nameResolver;
  }

  public UserCache getUserCache() {
    return userCache;
  }

  public void setUserCache(UserCache userCache) {
    this.userCache = userCache;
  }

  public UserDetails loadUserByUsername(String username) {

    // we need to have a tenanted form of the username as the cache will be populated with names as such.
    if(!JcrTenantUtils.isTenantedUser(username)){
      ITenant tenant = JcrTenantUtils.getCurrentTenant();
      if (tenant == null || tenant.getId() == null) {
        tenant = JcrTenantUtils.getDefaultTenant();
      }
      username = nameResolver.getPrincipleId(tenant, username);
    }
    UserDetails user = userCache.getUserFromCache(username);
    if (user == null) {
      user = delegate.loadUserByUsername(username);
      userCache.putUserInCache(user);
    }

    Assert.notNull(user, "UserDetailsService " + delegate + " returned null for username " + username + ". " +
        "This is an interface contract violation");

    return user;
  }
}