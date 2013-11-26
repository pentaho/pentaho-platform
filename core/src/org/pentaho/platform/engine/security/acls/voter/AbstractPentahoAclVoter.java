/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.engine.security.acls.voter;

import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;

public abstract class AbstractPentahoAclVoter implements IAclVoter, IPentahoInitializer {
  protected GrantedAuthority adminRole;

  public abstract Authentication getAuthentication( IPentahoSession session );

  public GrantedAuthority getAdminRole() {
    return this.adminRole;
  }

  public void setAdminRole( final GrantedAuthority value ) {
    this.adminRole = value;
  }

  public void init( final IPentahoSession session ) {
    ISystemSettings settings = PentahoSystem.getSystemSettings();
    String roleName = settings.getSystemSetting( "acl-voter/admin-role", "Administrator" ); //$NON-NLS-1$ //$NON-NLS-2$
    adminRole = new GrantedAuthorityImpl( roleName );
  }

  public boolean isPentahoAdministrator( final IPentahoSession session ) {
    // A user is considered a manager if they're authenticated,
    // and a member of the adminRole specified.
    return isGranted( session, adminRole );
  }

  public boolean isGranted( final IPentahoSession session, GrantedAuthority role ) {
    Authentication auth = getAuthentication( session );
    if ( ( auth != null ) && auth.isAuthenticated() ) {
      GrantedAuthority[] userAuths = auth.getAuthorities();
      if ( userAuths == null ) {
        return false;
      }
      for ( GrantedAuthority element : userAuths ) {
        if ( element.equals( role ) ) {
          return true;
        }
      }
      return false;
    } else {
      return false;
    }
  }
}
