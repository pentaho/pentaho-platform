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

package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Set;

public class AnyAuthorizationDecision extends AbstractCompositeAuthorizationDecision
  implements IAnyAuthorizationDecision {

  public AnyAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    this( request, calculateIsGranted( decisions ), decisions );
  }

  public AnyAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted, decisions );
  }

  private static boolean calculateIsGranted( @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisions.stream().anyMatch( IAuthorizationDecision::isGranted );
  }

  @Override
  public String toString() {
    // Example: "Any[Granted, of: <contained decision 1 text>, <contained decision 2 text>]"
    return String.format( "Any[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
  }
}
