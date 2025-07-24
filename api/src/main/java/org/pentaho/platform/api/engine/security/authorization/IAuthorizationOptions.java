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
 */
public interface IAuthorizationOptions {

  static IAuthorizationOptions getDefault() {
    return new IAuthorizationOptions() {
      @NonNull
      @Override
      public AuthorizationDecisionReportingMode getDecisionReportingMode() {
        return AuthorizationDecisionReportingMode.SETTLED;
      }
    };
  }

  /**
   * Indicates the level of reporting that authorization decisions should include.
   * @return The decision reporting mode.
   */
  @NonNull
  AuthorizationDecisionReportingMode getDecisionReportingMode();
}
