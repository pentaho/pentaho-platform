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

/**
 * The {@code AuthorizationOptions} class is a basic implementation of the {@link IAuthorizationOptions} interface.
 */
public class AuthorizationOptions implements IAuthorizationOptions {

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
