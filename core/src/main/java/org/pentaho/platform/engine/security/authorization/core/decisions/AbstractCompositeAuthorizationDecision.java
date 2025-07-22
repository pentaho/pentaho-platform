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
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractCompositeAuthorizationDecision extends AbstractAuthorizationDecision
  implements ICompositeAuthorizationDecision {

  @NonNull
  private final Set<IAuthorizationDecision> decisions;

  protected AbstractCompositeAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                                    boolean granted,
                                                    @NonNull Set<IAuthorizationDecision> decisions ) {
    super( request, granted );
    this.decisions = Collections.unmodifiableSet( decisions );
  }

  @NonNull
  public Set<IAuthorizationDecision> getDecisions() {
    return decisions;
  }

  @NonNull
  protected String getDecisionsLogText() {
    return getDecisions()
      .stream()
      .map( Object::toString )
      .collect( Collectors.joining( ", " ) );
  }
}
