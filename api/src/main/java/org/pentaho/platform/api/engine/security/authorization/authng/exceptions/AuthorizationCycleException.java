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

package org.pentaho.platform.api.engine.security.authorization.authng.exceptions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The {@code AuthorizationEvaluationCycleException} class is thrown when a cycle is detected during the authorization
 * process. This indicates that the evaluation logic has entered an infinite recursive cycle. A cycle occurs when an
 * authorization rule calls {@link IAuthorizationContext#authorize(AuthorizationRequest)} to perform a
 * sub-authorization
 * for a request which is already being evaluated upstream.
 */
public class AuthorizationCycleException extends AuthorizationException {
  @NonNull
  private final Collection<AuthorizationRequest> pendingRequests;

  @NonNull
  private final AuthorizationRequest cycleRequest;

  public AuthorizationCycleException(
    @NonNull Collection<AuthorizationRequest> pendingRequests,
    @NonNull AuthorizationRequest cycleRequest ) {

    super( createMessage( pendingRequests, cycleRequest ) );

    this.pendingRequests = pendingRequests;
    this.cycleRequest = cycleRequest;
  }

  @NonNull
  public Collection<AuthorizationRequest> getPendingRequests() {
    return pendingRequests;
  }

  @NonNull
  public AuthorizationRequest getCycleRequest() {
    return cycleRequest;
  }

  private static String createMessage( @NonNull Collection<AuthorizationRequest> pathRequests,
                                       @NonNull AuthorizationRequest request ) {
    Objects.requireNonNull( pathRequests );
    Objects.requireNonNull( request );

    // TODO: ideally, this would include a description of the current rule in each step?

    StringBuilder builder = new StringBuilder();
    builder
      .append( "Authorization evaluation cycle detected for request " )
      .append( request )
      .append( ".\n" )
      .append( "Evaluation path contains the following preceding requests:\n" );

    List<AuthorizationRequest> pathRequestsReversed = new ArrayList<>( pathRequests );
    Collections.reverse( pathRequestsReversed );

    int position = pathRequestsReversed.size();
    for ( AuthorizationRequest stepRequest : pathRequestsReversed ) {
      builder.append( position );
      builder.append( ": " );
      builder.append( stepRequest );
      builder.append( "\n" );

      position--;
    }

    return builder.toString();
  }
}
