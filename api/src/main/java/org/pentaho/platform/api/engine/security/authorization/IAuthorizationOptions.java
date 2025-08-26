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

/**
 * The {@code IAuthorizationOptions} interface encapsulates options that control the authorization evaluation process.
 * <p>
 * The authorization options do not change the final grant/deny decision, but can affect extra or auxiliary behaviors
 * of the evaluation process.
 * <p>
 * Equality is based on the equality of the various options/properties.
 * The {@link Object#equals(Object)} and {@link Object#hashCode()} methods must ensure this behavior.
 * <p>
 * The {@link Object#toString()} methods should be appropriate for logging and debugging purposes and include all
 * options/properties.
 */
public interface IAuthorizationOptions {

  /**
   * Provides an instance of {@link IAuthorizationOptions} with the default option values.
   *
   * @return An instance of {@link IAuthorizationOptions}.
   */
  static IAuthorizationOptions getDefault() {
    return new IAuthorizationOptions() {
      @NonNull
      @Override
      public AuthorizationDecisionReportingMode getDecisionReportingMode() {
        return AuthorizationDecisionReportingMode.SETTLED;
      }

      @Override
      public boolean equals( Object obj ) {
        if ( this == obj ) {
          return true;
        }

        if ( !( obj instanceof IAuthorizationOptions ) ) {
          return false;
        }

        IAuthorizationOptions other = (IAuthorizationOptions) obj;
        return getDecisionReportingMode() == other.getDecisionReportingMode();
      }

      @Override
      public int hashCode() {
        return getDecisionReportingMode().hashCode();
      }

      @Override
      public String toString() {
        return String.format(
          "IAuthorizationOptions{decisionReportingMode=%s}",
          getDecisionReportingMode() );
      }
    };
  }

  /**
   * Indicates the level of reporting that authorization decisions should include.
   *
   * @return The decision reporting mode.
   */
  @NonNull
  AuthorizationDecisionReportingMode getDecisionReportingMode();
}
