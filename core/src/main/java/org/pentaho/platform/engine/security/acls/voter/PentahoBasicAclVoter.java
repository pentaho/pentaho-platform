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

import org.pentaho.platform.api.engine.IAclEntry;
import org.pentaho.platform.api.engine.IAclHolder;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.IPentahoAclEntry;
import org.pentaho.platform.api.engine.IPentahoBasicAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.security.acls.PentahoAclEntry;
import org.springframework.security.core.Authentication;
import org.pentaho.platform.engine.security.acls.PentahoGrantedAuthorityEffectiveAclsResolver;

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
    IAclEntry[] effectiveAcls = getEffectiveAcls( session, holder );
    if ( ( effectiveAcls == null ) || ( effectiveAcls.length == 0 ) ) {
      return false;
    }
    for ( IAclEntry element : effectiveAcls ) {
      IPentahoBasicAclEntry acl = (IPentahoBasicAclEntry) element;
      if ( acl.isPermitted( mask ) ) {
        return true;
      }
    }
    return false;
  }

  public IAclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    Authentication auth = getAuthentication( session );
    if ( auth == null ) {
      return null; // No user, so no ACLs.
    }
    List allAcls = holder.getEffectiveAccessControls();
    IAclEntry[] acls = new IAclEntry[allAcls.size()];
    acls = (IAclEntry[]) allAcls.toArray( acls );
    PentahoGrantedAuthorityEffectiveAclsResolver resolver = new PentahoGrantedAuthorityEffectiveAclsResolver();
    IAclEntry[] resolvedAcls = resolver.resolveEffectiveAcls( acls, auth );
    return resolvedAcls;
  }

  public PentahoAclEntry getEffectiveAcl( final IPentahoSession session, final IAclHolder holder ) {
    // First, get all the ACLs on the object that apply to the user.
    IAclEntry[] effectiveAcls = getEffectiveAcls( session, holder );
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
