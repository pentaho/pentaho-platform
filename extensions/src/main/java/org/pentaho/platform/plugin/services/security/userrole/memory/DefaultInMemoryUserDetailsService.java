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


package org.pentaho.platform.plugin.services.security.userrole.memory;

import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Properties;

public class DefaultInMemoryUserDetailsService extends InMemoryUserDetailsManager {

  private ITenantedPrincipleNameResolver userNameUtils;

  public DefaultInMemoryUserDetailsService( Properties users, ITenantedPrincipleNameResolver userNameUtils ) {
    super( users );
    setUserNameUtils( userNameUtils );
  }

  @Override
  public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException, DataAccessException {
    return super.loadUserByUsername( getUserNameUtils().getPrincipleName( username ) );
  }

  public ITenantedPrincipleNameResolver getUserNameUtils() {
    return userNameUtils;
  }

  public void setUserNameUtils( ITenantedPrincipleNameResolver userNameUtils ) {
    this.userNameUtils = userNameUtils;
  }
}
