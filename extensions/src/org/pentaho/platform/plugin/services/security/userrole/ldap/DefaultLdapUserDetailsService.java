package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.LdapUserSearch;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.security.userdetails.ldap.LdapUserDetailsService;

public class DefaultLdapUserDetailsService extends LdapUserDetailsService{

  ITenantedPrincipleNameResolver userNameUtils;
  public DefaultLdapUserDetailsService(LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator,ITenantedPrincipleNameResolver userNameUtils) {
    super(userSearch, authoritiesPopulator);
    this.userNameUtils = userNameUtils;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return super.loadUserByUsername(userNameUtils.getPrincipleName(username));
  }

}
