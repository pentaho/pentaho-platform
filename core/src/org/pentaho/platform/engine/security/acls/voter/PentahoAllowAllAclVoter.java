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

import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.acl.AclEntry;

import java.util.List;

@SuppressWarnings( "deprecation" )
public class PentahoAllowAllAclVoter extends AbstractPentahoAclVoter {

  public boolean hasAccess( final IPentahoSession session, final IAclHolder holder, final int mask ) {
    // Return true indicating that there are no access prohibitions.
    return true;
  }

  @Override
  public Authentication getAuthentication( final IPentahoSession session ) {
    return SecurityHelper.getInstance().getAuthentication();
  }

  public AclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    // Returns all the ACLs on the object which indicates that the
    // user has all the necessary acls to access the object.
    List allAcls = holder.getEffectiveAccessControls();
    AclEntry[] acls = new AclEntry[allAcls.size()];
    acls = (AclEntry[]) allAcls.toArray( acls );
    return acls;
  }

  public IPentahoAclEntry getEffectiveAcl( final IPentahoSession session, final IAclHolder holder ) {
    IPentahoAclEntry rtn = new PentahoAclEntry();
    rtn.setMask( IPentahoAclEntry.PERM_FULL_CONTROL );
    return rtn;
  }

  @Override
  public boolean isPentahoAdministrator( final IPentahoSession session ) {
    // This system is wide open. All users are managers.
    return true;
  }

  @Override
  public boolean isGranted( final IPentahoSession session, final GrantedAuthority auth ) {
    // This system is wide open. Everyone is granted everything.
    return true;
  }

}
