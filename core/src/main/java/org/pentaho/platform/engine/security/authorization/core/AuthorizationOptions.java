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
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;

import java.util.Objects;

/**
 * The {@code AuthorizationOptions} class is a basic implementation of the {@link IAuthorizationOptions} interface.
 */
public class AuthorizationOptions implements IAuthorizationOptions {

  @NonNull
  private final AuthorizationDecisionReportingMode decisionReportingMode;

  /**
   * Constructs an {@code AuthorizationOptions} instance with default settings.
   * By default, it does not include all decisions in the authorization result.
   */
  public AuthorizationOptions() {
    this( AuthorizationDecisionReportingMode.SETTLED );
  }

  /**
   * Constructs an {@code AuthorizationOptions} instance with the specified decision reporting mode.
   *
   * @param decisionReportingMode The decision reporting mode.
   * @throws NullPointerException if the decision reporting mode is null.
   */
  public AuthorizationOptions( @NonNull AuthorizationDecisionReportingMode decisionReportingMode ) {
    this.decisionReportingMode = Objects.requireNonNull( decisionReportingMode );
  }

  /**
   * Indicates the level of reporting that authorization decisions should include.
   * @return The decision reporting mode.
   */
  @NonNull
  public AuthorizationDecisionReportingMode getDecisionReportingMode() {
    return decisionReportingMode;
  }

  @Override
  public boolean equals( Object o ) {
    if ( !( o instanceof IAuthorizationOptions ) ) {
      return false;
    }

    IAuthorizationOptions that = (IAuthorizationOptions) o;
    return getDecisionReportingMode() == that.getDecisionReportingMode();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode( getDecisionReportingMode() );
  }

  @Override
  public String toString() {
    return String.format(
      "AuthorizationOptions{decisionReportingMode=%s}",
      decisionReportingMode );
  }
}
