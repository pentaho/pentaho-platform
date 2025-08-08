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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The {@code CycleAuthorizationDecision} class is used to handle when a cycle is detected during the authorization
 * process. This indicates that the evaluation logic would enter an infinite recursive cycle, and, instead, a denied
 * decision is returned.
 * <p>
 * A cycle occurs when an authorization rule calls {@link IAuthorizationContext#authorize(IAuthorizationRequest)} to
 * perform a sub-authorization for a request which is already being evaluated upstream.
 */
public class CycleAuthorizationDecision extends AbstractAuthorizationDecision {
  @NonNull
  private final Collection<IAuthorizationRequest> pendingRequests;

  public CycleAuthorizationDecision(
    @NonNull IAuthorizationRequest cycleRequest,
    @NonNull Collection<IAuthorizationRequest> pendingRequests ) {

    super( cycleRequest, false );

    this.pendingRequests = pendingRequests;
  }

  @NonNull
  public Collection<IAuthorizationRequest> getPendingRequests() {
    return pendingRequests;
  }

  // TODO: review this text
  @Override
  public String toString() {
    // Example: "CycleAuthorizationDecision[Denied]"
    return String.format(
      "%s[%s %s]",
      getClass().getSimpleName(),
      getGrantedLogText(),
      buildText( pendingRequests, getRequest() ) );
  }

  @NonNull
  private static String buildText( @NonNull Collection<IAuthorizationRequest> pathRequests,
                                   @NonNull IAuthorizationRequest request ) {
    Objects.requireNonNull( pathRequests );
    Objects.requireNonNull( request );

    // TODO: ideally, this would include a description of the current rule in each step?

    StringBuilder builder = new StringBuilder();
    builder
      .append( "Authorization evaluation cycle detected for request " )
      .append( request )
      .append( ".\n" )
      .append( "Evaluation path contains the following preceding requests:\n" );

    List<IAuthorizationRequest> pathRequestsReversed = new ArrayList<>( pathRequests );
    Collections.reverse( pathRequestsReversed );

    int position = pathRequestsReversed.size();
    for ( IAuthorizationRequest stepRequest : pathRequestsReversed ) {
      builder.append( position );
      builder.append( ": " );
      builder.append( stepRequest );
      builder.append( "\n" );

      position--;
    }

    return builder.toString();
  }
}
