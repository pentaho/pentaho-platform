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

package org.pentaho.platform.api.engine.security.authorization;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The {@code AuthorizationEvaluationResult} class encapsulates the result of an authorization evaluation. It contains
 * information about whether the authorization was granted or denied, as well as, optionally, the reasons for the
 * decision.
 * <p>
 * Whenever necessary to express a vote of abstention, an empty {@link Optional <AuthorizationEvaluationResult>}
 * instance should be used
 *
 * @see AuthorizationEvaluationOptions#getIncludesReasons()
 */
public class AuthorizationEvaluationResult {
  private final boolean granted;

  @NonNull
  private final List<AuthorizationEvaluationReason> reasons;

  public AuthorizationEvaluationResult( boolean granted ) {
    this( granted, List.of() );
  }

  public AuthorizationEvaluationResult( boolean granted, @NonNull List<AuthorizationEvaluationReason> reasons ) {
    this.granted = granted;
    this.reasons = Objects.requireNonNull( reasons );
  }

  /**
   * Indicates whether the authorization was granted.
   *
   * @return {@code true} if the authorization was granted; {@code false} if it was denied.
   */
  public boolean isGranted() {
    return granted;
  }

  /**
   * Gets the reasons for the authorization evaluation result.
   *
   * @return A non-null list of reasons for the authorization evaluation result. The list may be empty if no reasons
   * were provided, or if the evaluation option's {@link AuthorizationEvaluationOptions#getIncludesReasons()} option
   * was specified {@code false}.
   */
  @NonNull
  public List<AuthorizationEvaluationReason> getReasons() {
    return reasons;
  }
}
