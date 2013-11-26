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
import org.springframework.security.providers.encoding.PasswordEncoder;

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
      syncedUser.setPassword( encoder.encodePassword( proxyUser.getPassword(), null ) );
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
