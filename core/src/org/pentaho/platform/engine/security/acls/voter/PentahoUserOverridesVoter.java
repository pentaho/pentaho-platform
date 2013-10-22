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
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.security.Authentication;
import org.springframework.security.acl.AclEntry;
import org.springframework.security.acl.basic.BasicAclEntry;
import org.springframework.security.userdetails.UserDetails;

/**
 * Extends the PentahoBasicAclVoter class, and overrides the getEffectiveAcls method to stipulate that if the
 * current user occurrs in the access control list, that whatever access controls are listed for that user, those
 * are the only ones returned.
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
 * With the standard <tt>PentahoBasicAclVoter</tt>, sally would have Execute permissions on this object because
 * that voter will simply aggregate all applicable access controls. With this voter, the returned access controls
 * for sally will be <tt>PentahoAclEntry.NOTHING</tt>.
 * 
 * 
 * @author mbatchel
 * 
 */

@SuppressWarnings( "deprecation" )
public class PentahoUserOverridesVoter extends PentahoBasicAclVoter {

  @Override
  public AclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    Authentication auth = getAuthentication( session );
    // User is un-authenticated. Return no access controls.
    if ( auth == null ) {
      return null;
    }
    AclEntry[] objectAcls = super.getEffectiveAcls( session, holder );
    if ( objectAcls == null ) {
      return null;
    }
    Object principal = auth.getPrincipal();
    String userName = null;
    if ( principal instanceof UserDetails ) {
      userName = ( (UserDetails) principal ).getUsername();
    } else {
      userName = principal.toString();
    }
    for ( AclEntry element : objectAcls ) {
      // First, search for the user name in the objectAcls. If it's there,
      // then that
      // overrides anything else. It's the only acl returned.
      BasicAclEntry entry = (BasicAclEntry) element;
      String recipient = entry.getRecipient().toString();
      // Found the user in there - That means that his/her access to the
      // object
      // has been spelled out. Therefore, we need to simply return that
      // ACL.
      if ( recipient.equals( userName ) ) {
        return new AclEntry[] { entry };
      }
    }
    // Wasn't anything specifically on the user. So, return default
    // settings.
    return objectAcls;
  }

}
