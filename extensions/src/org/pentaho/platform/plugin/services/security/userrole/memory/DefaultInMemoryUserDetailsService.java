package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.security.userdetails.memory.InMemoryDaoImpl;

public class DefaultInMemoryUserDetailsService extends InMemoryDaoImpl{
  
  ITenantedPrincipleNameResolver userNameUtils;
  public DefaultInMemoryUserDetailsService(ITenantedPrincipleNameResolver userNameUtils) {
    super();
    this.userNameUtils = userNameUtils;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
    return super.loadUserByUsername(userNameUtils.getPrincipleName(username));
  }

}
