/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
