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

package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRuleOverrider;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The {@code AuthorizationOptions} class is a basic implementation of the {@link IAuthorizationOptions} interface.
 */
public class AuthorizationOptions implements IAuthorizationOptions {

  @NonNull
  private final AuthorizationDecisionReportingMode decisionReportingMode;

  @Nullable
  private final IAuthorizationRuleOverrider authorizationRuleOverrider;

  /**
   * Constructs an {@code AuthorizationOptions} instance with default settings.
   * By default, it uses a decision reporting mode of {@link AuthorizationDecisionReportingMode#SETTLED} and no
   * authorization rule overrider.
   */
  public AuthorizationOptions() {
    this( AuthorizationDecisionReportingMode.SETTLED, null );
  }

  /**
   * Constructs an {@code AuthorizationOptions} instance with the specified decision reporting mode and no authorization
   * rule overrider.
   *
   * @param decisionReportingMode The decision reporting mode.
   * @throws IllegalArgumentException if the decision reporting mode is {@code null}.
   */
  public AuthorizationOptions( @NonNull AuthorizationDecisionReportingMode decisionReportingMode ) {
    this( decisionReportingMode, null );
  }

  /**
   * Constructs an {@code AuthorizationOptions} instance with the specified decision reporting mode and authorization
   * rule overrider.
   *
   * @param decisionReportingMode The decision reporting mode.
   * @param authorizationRuleOverrider The authorization rule overrider, possibly {@code null}.
   *
   * @throws IllegalArgumentException if the decision reporting mode is {@code null}.
   */
  public AuthorizationOptions( @NonNull AuthorizationDecisionReportingMode decisionReportingMode,
                               @Nullable IAuthorizationRuleOverrider authorizationRuleOverrider ) {
    Assert.notNull( decisionReportingMode, "Argument 'decisionReportingMode' is required" );

    this.decisionReportingMode = decisionReportingMode;
    this.authorizationRuleOverrider = authorizationRuleOverrider;
  }

  /**
   * Indicates the level of reporting that authorization decisions should include.
   * @return The decision reporting mode.
   */
  @NonNull
  public AuthorizationDecisionReportingMode getDecisionReportingMode() {
    return decisionReportingMode;
  }

  /**
   * Gets the authorization rule overrider, if any.
   * @return The authorization rule overrider, or {@code null} if none is set.
   */
  @Nullable
  @Override
  public IAuthorizationRuleOverrider getAuthorizationRuleOverrider() {
    return authorizationRuleOverrider;
  }

  @Override
  public boolean equals( Object o ) {
    return o instanceof IAuthorizationOptions that
      && getDecisionReportingMode() == that.getDecisionReportingMode()
      && Objects.equals( getAuthorizationRuleOverrider(), that.getAuthorizationRuleOverrider() );

  }

  @Override
  public int hashCode() {
    return Objects.hash( getDecisionReportingMode(), getAuthorizationRuleOverrider() );
  }

  @Override
  public String toString() {
    return String.format(
      "AuthorizationOptions{decisionReportingMode=%s, authorizationRuleOverrider=%s}",
      decisionReportingMode,
      authorizationRuleOverrider );
  }
}
