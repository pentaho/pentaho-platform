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

package org.pentaho.platform.engine.security.authorization.core.exceptions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.exceptions.AuthorizationFailureException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The {@code AuthorizationRequestCycleException} exception is used to signal when a cycle is detected during the
 * authorization process. This indicates that the evaluation logic would enter an infinite recursive cycle.
 * <p>
 * A cycle occurs when an authorization rule calls {@link IAuthorizationContext#authorize(IAuthorizationRequest)} to
 * perform a sub-authorization for a request which is already being evaluated upstream.
 */
public class AuthorizationRequestCycleException extends AuthorizationFailureException {
  public AuthorizationRequestCycleException(
    @NonNull Collection<IAuthorizationRequest> pendingRequests,
    @NonNull IAuthorizationRequest cycleRequest ) {

    super( createMessage( pendingRequests, cycleRequest ) );
  }

  @NonNull
  private static String createMessage( @NonNull Collection<IAuthorizationRequest> pendingRequests,
                                       @NonNull IAuthorizationRequest cycleRequest ) {
    Assert.notNull( pendingRequests, "Argument 'pendingRequests' is required" );
    Assert.notNull( cycleRequest, "Argument 'cycleRequest' is required" );

    StringBuilder builder = new StringBuilder();
    builder
      .append( "Authorization evaluation cycle detected for request " )
      .append( cycleRequest )
      .append( ".\n" )
      .append( "Evaluation path contains the following preceding requests:\n" );

    List<IAuthorizationRequest> pathRequestsReversed = new ArrayList<>( pendingRequests );
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
