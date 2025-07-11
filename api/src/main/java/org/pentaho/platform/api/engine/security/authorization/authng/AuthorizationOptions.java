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

package org.pentaho.platform.api.engine.security.authorization.authng;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The {@code AuthorizationOptions} class encapsulates options that control the authorization evaluation process.
 * <p>
 * The authorization options do not change the final grant/deny decision, but can affect extra or auxiliary behaviors
 * of the evaluation process.
 */
public class AuthorizationOptions {

  private static final AuthorizationOptions DEFAULT = new AuthorizationOptions();

  public static AuthorizationOptions getDefault() {
    return DEFAULT;
  }

  private final AuthorizationDecisionReportingMode decisionReportingMode;

  /**
   * Constructs an {@code AuthorizationOptions} instance with default settings.
   * By default, it does not include all decisions in the authorization result.
   */
  public AuthorizationOptions() {
    this( AuthorizationDecisionReportingMode.SETTLED );
  }

  public AuthorizationOptions( AuthorizationDecisionReportingMode decisionReportingMode ) {
    this.decisionReportingMode = decisionReportingMode;
  }

  /**
   * Indicates the level of reporting that authorization decisions should include.
   * @return The decision reporting mode.
   */
  @NonNull
  public AuthorizationDecisionReportingMode getDecisionReportingMode() {
    return decisionReportingMode;
  }
}
