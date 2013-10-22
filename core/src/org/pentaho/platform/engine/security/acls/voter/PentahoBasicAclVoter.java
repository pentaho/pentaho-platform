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
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.Authentication;
import org.springframework.security.acl.AclEntry;
import org.springframework.security.acl.basic.BasicAclEntry;
import org.springframework.security.acl.basic.GrantedAuthorityEffectiveAclsResolver;

import java.util.List;

/**
 * Standard basic ACL Voter. This voter simply aggregates all the applicable access controls on an object when
 * asked for the effective ACL.
 * <p>
 * For example, if the user (sally) belongs to the following roles:
 * 
 * <pre>
 *   <table>
 *     <tr>
 *       <th>User Id</th><th>Role</th>
 *     </tr>
 *     <tr>
 *       <td>sally</td><td>dev</td>
 *     </tr>
 *     <tr>
 *       <td></td><td>mgr</td>
 *     </tr>
 *   </table>
 * </pre>
 * 
 * And the object has the following defined access controls:
 * 
 * <pre>
 *   <table>
 *     <tr>
 *       <th>Role</th><th>Access</th>
 *     </tr>
 *     <tr>
 *       <td>dev</td><td>Execute</td>
 *     </tr>
 *     <tr>
 *       <td>sales</td><td>Execute and Subscribe</td>
 *     </tr>
 *     <tr>
 *       <td>sally</td><td>Nothing</td>
 *     </tr>
 *   </table>
 * </pre>
 * 
 * With voter, sally would have Execute permissions on this object because this voter simply aggregates all
 * applicable access controls.
 * <p>
 * 
 * @author mbatchel
 * @see PentahoUserOverridesVoter
 * 
 */

@SuppressWarnings( "deprecation" )
public class PentahoBasicAclVoter extends AbstractPentahoAclVoter implements IAclVoter {

  // Allow overriding of the obtaining of the authentication. This
  // allows someone to decide whether to create an anonymous authentication
  // or not.
  @Override
  public Authentication getAuthentication( final IPentahoSession session ) {
    return SecurityHelper.getInstance().getAuthentication();
  }

  public boolean hasAccess( final IPentahoSession session, final IAclHolder holder, final int mask ) {
    Authentication auth = getAuthentication( session );
    // If we're not authenticated, default to no access and return.
    if ( auth == null ) {
      return false;
    }
    // admins can do anything they want!
    if ( isPentahoAdministrator( session ) ) {
      return true;
    }
    AclEntry[] effectiveAcls = getEffectiveAcls( session, holder );
    if ( ( effectiveAcls == null ) || ( effectiveAcls.length == 0 ) ) {
      return false;
    }
    for ( AclEntry element : effectiveAcls ) {
      BasicAclEntry acl = (BasicAclEntry) element;
      if ( acl.isPermitted( mask ) ) {
        return true;
      }
    }
    return false;
  }

  public AclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    Authentication auth = getAuthentication( session );
    if ( auth == null ) {
      return null; // No user, so no ACLs.
    }
    List allAcls = holder.getEffectiveAccessControls();
    AclEntry[] acls = new AclEntry[allAcls.size()];
    acls = (AclEntry[]) allAcls.toArray( acls );
    GrantedAuthorityEffectiveAclsResolver resolver = new GrantedAuthorityEffectiveAclsResolver();
    AclEntry[] resolvedAcls = resolver.resolveEffectiveAcls( acls, auth );
    return resolvedAcls;
  }

  public PentahoAclEntry getEffectiveAcl( final IPentahoSession session, final IAclHolder holder ) {
    // First, get all the ACLs on the object that apply to the user.
    AclEntry[] effectiveAcls = getEffectiveAcls( session, holder );
    PentahoAclEntry entry = new PentahoAclEntry();
    entry.setMask( IPentahoAclEntry.PERM_NOTHING );
    // By default, we'll OR together all the acls to create the whole mask
    // which
    // indicates their access.
    if ( ( effectiveAcls != null ) && ( effectiveAcls.length > 0 ) ) {
      int[] allAcls = new int[effectiveAcls.length];
      for ( int i = 0; i < effectiveAcls.length; i++ ) {
        allAcls[i] = ( (IPentahoAclEntry) effectiveAcls[i] ).getMask();
      }
      entry.addPermissions( allAcls );
      return entry;
    } else {
      return entry;
    }
  }

}
