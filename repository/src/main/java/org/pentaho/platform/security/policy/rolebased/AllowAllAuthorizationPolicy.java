/*!
 *
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
 * Copyright (c) 2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.security.policy.rolebased;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.util.Collections;
import java.util.List;

/**
 * An authorization policy that always authorizes.
 */
public class AllowAllAuthorizationPolicy implements IAuthorizationPolicy {
  public boolean isAllowed( String actionName ) {
    return true;
  }

  public List<String> getAllowedActions( String actionNamespace ) {
    return Collections.<String>emptyList();
  }
}
