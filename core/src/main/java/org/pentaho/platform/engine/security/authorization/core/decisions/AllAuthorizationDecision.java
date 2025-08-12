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
import org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;

import java.util.Set;

public class AllAuthorizationDecision extends AbstractCompositeAuthorizationDecision
  implements IAllAuthorizationDecision {

  public AllAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    this( request, calculateIsGranted( decisions ), decisions );
  }

  public AllAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                   boolean granted,
                                   @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted, decisions );
  }

  private static boolean calculateIsGranted( @NonNull Set<IAuthorizationDecision> decisions ) {
    return decisions.stream().allMatch( IAuthorizationDecision::isGranted );
  }

  @Override
  public String toString() {
    // Example: "All[Denied, of: <contained decision 1 text>, <contained decision 2 text>]"
    return String.format( "All[%s, of: %s]", getGrantedLogText(), getDecisionsLogText() );
  }
}
