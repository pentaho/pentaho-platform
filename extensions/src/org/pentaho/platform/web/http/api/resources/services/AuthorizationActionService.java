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
package org.pentaho.platform.web.http.api.resources.services;

import java.util.List;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class AuthorizationActionService {

  @SuppressWarnings( "serial" )
  private List<IAuthorizationAction> authActionList;

  public AuthorizationActionService( List<IAuthorizationAction> authActionList ) {
    this.authActionList = authActionList;
  }

  public AuthorizationActionService() {

  }

  public boolean validateAuth( String authAction ) {

    boolean isAllowed = false;
    boolean validInput = false;
    for ( IAuthorizationAction a : getActionList() ) {
      if ( a.getName().equals( authAction ) ) {
        validInput = true;
        break;
      }
    }

    if ( validInput ) {
      IAuthorizationPolicy policy = getPolicy();
      isAllowed = policy.isAllowed( authAction );
    }
    return isAllowed;
  }

  protected List<IAuthorizationAction> getActionList() {
    if ( authActionList == null ) {
      authActionList = PentahoSystem.getAll( IAuthorizationAction.class );
    }
    return authActionList;
  }

  protected IAuthorizationPolicy getPolicy() {
    return PentahoSystem.get( IAuthorizationPolicy.class );
  }
}
