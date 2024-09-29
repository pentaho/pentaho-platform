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


package org.pentaho.platform.api.engine;

import java.util.List;

/**
 * An access control policy.
 * 
 * <p>
 * Reponsible for determining if access to a given action should be allowed or denied. A implementation could be
 * one based on roles, as is done in the Servlet specification. (In other words, if the policy has an association
 * between the given action and a role that has been granted to the user, then the decision will be to allow.)
 * </p>
 * 
 * @author mlowery
 */
public interface IAuthorizationPolicy {

  /**
   * Returns {@code true} if the the action should be allowed.
   * 
   * @param actionName
   *          name of action (e.g. {@code org.pentaho.di.repository.create})
   * @return {@code true} to allow
   */
  boolean isAllowed( final String actionName );

  /**
   * Returns all actions in the given namespace that are currently allowed.
   * 
   * @param actionNamespace
   *          action namespace (e.g. {@code org.pentaho.di.repository}); {@code null} means all allowed actions
   * @return list of actions
   */
  List<String> getAllowedActions( final String actionNamespace );

}
