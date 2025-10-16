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


package org.pentaho.platform.engine.security.acls.voter;

import java.util.Collection;

import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoInitializer;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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
    adminRole = new SimpleGrantedAuthority( roleName );
  }

  public boolean isPentahoAdministrator( final IPentahoSession session ) {
    // A user is considered a manager if they're authenticated,
    // and a member of the adminRole specified.
    return isGranted( session, adminRole );
  }

  public boolean isGranted( final IPentahoSession session, GrantedAuthority role ) {
    Authentication auth = getAuthentication( session );
    if ( ( auth != null ) && auth.isAuthenticated() ) {
      Collection<? extends GrantedAuthority> userAuths = auth.getAuthorities();
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
