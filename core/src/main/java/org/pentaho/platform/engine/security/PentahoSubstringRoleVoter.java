/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.engine.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.AccessDecisionVoter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

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

  public int vote( final Authentication authentication, final Object object, final Collection configAttributes ) {
    int result = ACCESS_ABSTAIN;
    List<? extends GrantedAuthority> authorities =
      (List<? extends GrantedAuthority>) extractAuthorities( authentication );

    Iterator iter = configAttributes.iterator();
    while ( iter.hasNext() ) {
      ConfigAttribute attribute = (ConfigAttribute) iter.next();

      if ( this.supports( attribute ) ) {
        result = ACCESS_DENIED;

        // Attempt to find a matching granted authority
        for ( int i = 0; i < authorities.size(); i++ ) {
          if ( attribute.getAttribute().substring( processConfigAttributePrefix.length() ).equals(
            authorities.get( i ).getAuthority() ) ) {
            return ACCESS_GRANTED;
          }
        }
      }
    }

    return result;
  }

  private Collection<? extends GrantedAuthority> extractAuthorities( Authentication authentication ) {
    return authentication.getAuthorities();
  }
}
