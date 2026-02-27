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


package org.pentaho.platform.web.http.api.resources.services;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.List;

@SuppressWarnings( "removal" )
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
