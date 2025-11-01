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
import org.pentaho.platform.api.engine.IPentahoBasicAclEntry;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

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

@Deprecated
public class PentahoUserOverridesVoter extends PentahoBasicAclVoter {

  @Override
  public IAclEntry[] getEffectiveAcls( final IPentahoSession session, final IAclHolder holder ) {
    Authentication auth = getAuthentication( session );
    // User is un-authenticated. Return no access controls.
    if ( auth == null ) {
      return null;
    }
    IAclEntry[] objectAcls = super.getEffectiveAcls( session, holder );
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
    for ( IAclEntry element : objectAcls ) {
      // First, search for the user name in the objectAcls. If it's there,
      // then that
      // overrides anything else. It's the only acl returned.
      IPentahoBasicAclEntry entry = (IPentahoBasicAclEntry) element;
      String recipient = entry.getRecipient().toString();
      // Found the user in there - That means that his/her access to the
      // object
      // has been spelled out. Therefore, we need to simply return that
      // ACL.
      if ( recipient.equals( userName ) ) {
        return new IAclEntry[] { entry };
      }
    }
    // Wasn't anything specifically on the user. So, return default
    // settings.
    return objectAcls;
  }

}
