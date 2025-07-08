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
 * The {@code IAuthorizationRulesEngine} interface provides top-level authorization operations.
 * <p>
 * Authorization evaluation combines the results of authorization evaluation of multiple rules.
 * Individual authorization rules can abstain, or grant or deny permission, as well as optionally
 * include reasons for the decision.
 */
public interface IAuthorizationRulesEngine {
  /**
   * Evaluates if a user can perform an action, with default options.
   * <p>
   * The default options are determined by {@link AuthorizationEvaluationOptions#getDefault()}.
   * <p>
   * This method is a convenience method equivalent to calling
   * {@code instance.evaluate(user, actionName, AuthorizationEvaluationOptions.getDefault())}.
   *
   * @param user       The user for whom the authorization is being evaluated.
   * @param actionName The name of the action (e.g. {@code org.pentaho.di.repository.create}).
   * @return The evaluation result, which includes whether the action execution is allowed.
   * @throws IllegalArgumentException              if the action name is {@code null} or empty.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected during the evaluation process.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   * @see #evaluate(IAuthorizationUser, String, AuthorizationEvaluationOptions)
   */
  @NonNull
  default AuthorizationEvaluationResult evaluate(
    @NonNull IAuthorizationUser user,
    @NonNull String actionName )
    throws AuthorizationEvaluationException {
    return evaluate( user, actionName, AuthorizationEvaluationOptions.getDefault() );
  }

  /**
   * Evaluates if a user can perform an action, with specified options.
   *
   * @param user       The user for whom the authorization is being evaluated.
   * @param actionName The name of the action (e.g. {@code org.pentaho.di.repository.create}).
   * @param options    The evaluation options for the evaluation.
   * @return The evaluation result, which includes whether the action execution is allowed.
   * @throws IllegalArgumentException              if the action name is {@code null} or empty.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected during the evaluation process.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  AuthorizationEvaluationResult evaluate(
    @NonNull IAuthorizationUser user,
    @NonNull String actionName,
    @NonNull AuthorizationEvaluationOptions options )
    throws AuthorizationEvaluationException;
}
