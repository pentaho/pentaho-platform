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

/**
 * The {@code AuthorizationEvaluationOptions} class encapsulates options for the authorization evaluation process.
 */
public class AuthorizationEvaluationOptions {

  private static final AuthorizationEvaluationOptions DEFAULT = new AuthorizationEvaluationOptions();

  public static AuthorizationEvaluationOptions getDefault() {
    return DEFAULT;
  }

  private final boolean includesReasons;

  public AuthorizationEvaluationOptions() {
    this( false );
  }

  public AuthorizationEvaluationOptions( boolean includesReasons ) {
    this.includesReasons = includesReasons;
  }

  /**
   * Indicates whether the evaluation results should include reasons.
   *
   * @return {@code true} if reasons should be included; {@code false}, otherwise.
   */
  public boolean getIncludesReasons() {
    return includesReasons;
  }
}
