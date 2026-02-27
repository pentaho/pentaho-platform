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

package org.pentaho.platform.api.engine.security.authorization.impl;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;

import java.util.Objects;

/**
 * The {@code DefaultAuthorizationOptions} class provides an implementation of {@link IAuthorizationOptions} with the
 * default option values.
 * <p>
 * This class is intended for internal use.
 * It supports the implementation of the {@link IAuthorizationOptions#getDefault()} method.
 */
public class DefaultAuthorizationOptions implements IAuthorizationOptions {

  public static final DefaultAuthorizationOptions INSTANCE = new DefaultAuthorizationOptions();

  private DefaultAuthorizationOptions() {
    // Prevent external instantiation.
  }

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

    if ( !( obj instanceof IAuthorizationOptions other ) ) {
      return false;
    }

    if ( getDecisionReportingMode() != other.getDecisionReportingMode() ) {
      return false;
    }

    return Objects.equals( getAuthorizationRuleOverrider(), other.getAuthorizationRuleOverrider() );
  }

  @Override
  public int hashCode() {
    return Objects.hash( getDecisionReportingMode(), getAuthorizationRuleOverrider() );
  }

  @Override
  public String toString() {
    return String.format(
      "IAuthorizationOptions{decisionReportingMode=%s, authorizationRuleOverrider=%s}",
      getDecisionReportingMode(),
      getAuthorizationRuleOverrider() );
  }
}
