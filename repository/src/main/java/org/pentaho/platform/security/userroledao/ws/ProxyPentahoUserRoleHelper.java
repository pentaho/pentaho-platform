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


package org.pentaho.platform.security.userroledao.ws;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoRole;
import org.pentaho.platform.api.engine.security.userroledao.IPentahoUser;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.userroledao.PentahoRole;
import org.pentaho.platform.security.userroledao.PentahoUser;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * This class contains helper methods for converting from and to proxy user and roles
 * 
 * @author Will Gorman (wgorman@pentaho.com)
 * 
 */
public class ProxyPentahoUserRoleHelper {

  public static ProxyPentahoUser toProxyUser( IPentahoUser user ) {
    ProxyPentahoUser proxyPentahoUser = new ProxyPentahoUser();
    proxyPentahoUser.setName( user.getUsername() );
    proxyPentahoUser.setDescription( user.getDescription() );
    proxyPentahoUser.setEnabled( user.isEnabled() );
    proxyPentahoUser.setPassword( "" ); //$NON-NLS-1$
    ITenant tenant = user.getTenant();
    proxyPentahoUser.setTenant( new Tenant( tenant.getId(), tenant.isEnabled() ) );
    return proxyPentahoUser;
  }

  /**
   * Synchronizes <code>user</code> with fields from <code>proxyUser</code>. The roles set of given
   * <code>user</code> is unmodified.
   */
  public static IPentahoUser syncUsers( IPentahoUser user, ProxyPentahoUser proxyUser ) {
    IPentahoUser syncedUser = user;
    if ( syncedUser == null ) {
      syncedUser = new PentahoUser( proxyUser.getName() );
    }
    syncedUser.setDescription( proxyUser.getDescription() );

    // PPP-1527: Password is never sent back to the UI. It always shows as blank. If the user leaves it blank,
    // password is not changed. If the user enters a value, set the password.
    if ( !StringUtils.isBlank( proxyUser.getPassword() ) ) {
      PasswordEncoder encoder =
          PentahoSystem.get( PasswordEncoder.class, "passwordEncoder", PentahoSessionHolder.getSession() ); //$NON-NLS-1$
      syncedUser.setPassword( encoder.encode( proxyUser.getPassword() ) );
    }
    syncedUser.setEnabled( proxyUser.getEnabled() );
    return syncedUser;
  }

  /**
   * Synchronizes <code>role</code> with fields from <code>proxyRole</code>. The users set of given
   * <code>role</code> is unmodified.
   */
  public static IPentahoRole syncRoles( IPentahoRole role, ProxyPentahoRole proxyRole ) {
    IPentahoRole syncedRole = role;
    if ( syncedRole == null ) {
      syncedRole = new PentahoRole( proxyRole.getName() );
    }
    syncedRole.setDescription( proxyRole.getDescription() );
    return syncedRole;
  }

  public static ProxyPentahoRole toProxyRole( IPentahoRole role ) {
    ProxyPentahoRole proxyRole = new ProxyPentahoRole( role.getName() );
    proxyRole.setDescription( role.getDescription() );
    ITenant tenant = role.getTenant();
    proxyRole.setTenant( new Tenant( tenant.getId(), tenant.isEnabled() ) );
    return proxyRole;
  }

}
