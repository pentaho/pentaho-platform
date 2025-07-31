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


package org.pentaho.platform.security.policy.rolebased.springsecurity;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.util.Assert;

import java.util.Collection;
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
    Assert.notNull( policy, "The authorization policy must not be null. Ensure a valid policy instance is provided." );
    Assert.notNull( prefix, "The prefix must not be null. Ensure a valid prefix is provided." );
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

  public int vote( final Authentication authentication, final Object object, final Collection configAttributes ) {
    int result = ACCESS_ABSTAIN;
    Iterator iter = configAttributes.iterator();

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
