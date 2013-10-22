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

package org.pentaho.platform.security.policy.rolebased.springsecurity;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.springframework.security.Authentication;
import org.springframework.security.ConfigAttribute;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.vote.AccessDecisionVoter;
import org.springframework.util.Assert;

import java.util.Iterator;

/**
 * An {@link AccessDecisionVoter} that delegates to an {@link IAuthorizationPolicy} instance.
 * 
 * @author mlowery
 */
public class AuthorizationPolicyVoter implements AccessDecisionVoter {

  // ~ Static fields/initializers
  // ======================================================================================

  // ~ Instance fields
  // =================================================================================================

  private String prefix;

  private IAuthorizationPolicy policy;

  // ~ Constructors
  // ====================================================================================================

  public AuthorizationPolicyVoter( final IAuthorizationPolicy policy, final String prefix ) {
    super();
    Assert.notNull( policy );
    Assert.notNull( prefix );
    this.policy = policy;
    this.prefix = prefix;
  }

  // ~ Methods
  // =========================================================================================================

  public boolean supports( final ConfigAttribute attribute ) {
    if ( ( attribute.getAttribute() != null ) && attribute.getAttribute().startsWith( prefix ) ) {
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

    while ( iter.hasNext() ) {
      ConfigAttribute attribute = (ConfigAttribute) iter.next();

      if ( supports( attribute ) ) {
        String actionName = attribute.getAttribute().substring( prefix.length() );

        if ( policy.isAllowed( actionName ) ) {
          return ACCESS_GRANTED;
        } else {
          return ACCESS_DENIED;
        }
      }
    }

    return result;
  }

}
