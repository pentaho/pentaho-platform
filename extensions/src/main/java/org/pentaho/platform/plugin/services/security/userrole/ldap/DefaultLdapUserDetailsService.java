/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

public class DefaultLdapUserDetailsService extends LdapUserDetailsService {

  ITenantedPrincipleNameResolver userNameUtils;

  public DefaultLdapUserDetailsService( LdapUserSearch userSearch, LdapAuthoritiesPopulator authoritiesPopulator,
      ITenantedPrincipleNameResolver userNameUtils ) {
    super( userSearch, authoritiesPopulator );
    this.userNameUtils = userNameUtils;
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
    return super.loadUserByUsername( userNameUtils.getPrincipleName( username ) );
  }

}
