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

package org.pentaho.platform.engine.security;

import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.vote.AccessDecisionVoter;

import java.util.Iterator;

/**
 * Similar to {@link org.springframework.security.vote.RoleVoter} except that it does not use a role prefix;
 * instead it uses a config attribute prefix which serves a similar, but not the same, purpose. In
 * {@code RoleVoter}, the role prefix serves as an indicator to the {@code RoleVoter} to participate in the voting.
 * {@code RoleVoter} assumes that the config attributes are role names. In this voter implementation, the
 * {@code processConfigAttributePrefix} is stripped from the config attribute before comparison to the roles
 * granted to the user.
 * 
 * <p>
 * For example, assume that a user has the role {@code Authenticated}. Also assume that the config attribute for a
 * method invocation is {@code VOTE_ROLE_FILE_Authenticated}. Finally assume that this voter instance is configured
 * with a {@code processConfigAttributePrefix} of {@code VOTE_ROLE_FILE_}. This voter implementation will strip the
 * {@code processConfigAttributePrefix} from the config attribute and compare to the roles granted to the user. In
 * this example, access will be granted.
 * </p>
 * 
 * @author mlowery
 */
public class PentahoSubstringRoleVoter implements AccessDecisionVoter {
  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private String processConfigAttributePrefix;

  // ~ Constructors
  // ====================================================================================================

  public PentahoSubstringRoleVoter( final String processConfigAttributePrefix ) {
    super();
    this.processConfigAttributePrefix = processConfigAttributePrefix;
  }

  // ~ Methods
  // =========================================================================================================

  public boolean supports( final ConfigAttribute attribute ) {
    if ( ( attribute.getAttribute() != null ) && attribute.getAttribute().startsWith( processConfigAttributePrefix ) ) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This implementation supports any type of class, because it does not query the presented secure object.
   * 
   * @param clazz
   *          the secure object
   * 
   * @return always <code>true</code>
   */
  public boolean supports( final Class clazz ) {
    return true;
  }

  public int vote( final Authentication authentication, final Object object, final ConfigAttributeDefinition config ) {
    int result = ACCESS_ABSTAIN;
    Iterator iter = config.getConfigAttributes().iterator();
    GrantedAuthority[] authorities = extractAuthorities( authentication );

    while ( iter.hasNext() ) {
      ConfigAttribute attribute = (ConfigAttribute) iter.next();

      if ( this.supports( attribute ) ) {
        result = ACCESS_DENIED;

        // Attempt to find a matching granted authority
        for ( int i = 0; i < authorities.length; i++ ) {
          if ( attribute.getAttribute().substring( processConfigAttributePrefix.length() ).equals(
              authorities[i].getAuthority() ) ) {
            return ACCESS_GRANTED;
          }
        }
      }
    }

    return result;
  }

  private GrantedAuthority[] extractAuthorities( Authentication authentication ) {
    return authentication.getAuthorities();
  }
}
