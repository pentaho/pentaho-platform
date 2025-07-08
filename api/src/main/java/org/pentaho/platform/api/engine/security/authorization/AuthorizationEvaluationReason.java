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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;

/**
 * The {@code AuthorizationEvaluationReason} class represents a reason for an authorization evaluation result. The
 * reason consists of a {@link #getCode() code} and a {@link #getDescription() description}. The code may be used to
 * identify the reason programmatically, while the description provides a human-readable explanation and should be
 * localized in the current (thread's) system locale, regardless of the user for which permissions are being evaluated.
 * <p>
 * Reason codes are defined by authorization rules. It is advised that these are "dot-namespaced" with domains under
 * their control (e.g. {@code org.pentaho.authorization.rbac-explicit}).
 */
public class AuthorizationEvaluationReason {
  @NonNull
  private final String code;

  @NonNull
  private final String description;

  public AuthorizationEvaluationReason( @NonNull String code, @NonNull String description ) {
    this.code = requireNonNullOrEmpty( code, "code" );
    this.description = requireNonNullOrEmpty( description, "description" );
  }

  @NonNull
  private static String requireNonNullOrEmpty( @Nullable String value, @NonNull String argumentName ) {
    if ( StringUtils.isEmpty( value ) ) {
      throw new IllegalArgumentException( String.format( "Argument '%s' must not be null or empty.", argumentName ) );
    }

    return value;
  }

  /**
   * Gets the code of the reason.
   *
   * @return The code of the reason. Non-null and non-empty.
   */
  @NonNull
  public String getCode() {
    return code;
  }

  /**
   * Gets the description of the reason.
   * <p>
   * This description provides a human-readable explanation of the reason for the authorization evaluation result,
   * whether a grant or a denial. It should be localized in the current (thread's) system locale.
   *
   * @return The description of the reason. Non-null and non-empty.
   */
  @NonNull
  public String getDescription() {
    return description;
  }
}
