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
 * The {@code IAuthorizationContext} interface defines the context for an authorization evaluation process initiated by
 * {@link IAuthorizationRulesEngine}. The context is passed as an argument to evaluation rules methods, such as
 * {@link IAuthorizationRule#evaluate(IAuthorizationUser, String, IAuthorizationContext)}.
 */
public interface IAuthorizationContext {

  /**
   * Gets the options for the authorization evaluation.
   * @return The options for the authorization evaluation.
   */
  @NonNull
  AuthorizationEvaluationOptions getOptions();

  /**
   * Evaluates if a user can perform an action.
   *
   * @param user       The user for whom the authorization is being evaluated.
   * @param actionName The name of the action (e.g. {@code org.pentaho.di.repository.create}).
   * @return The result of the evaluation.
   * @throws IllegalArgumentException              if the action name is {@code null} or empty.
   * @throws AuthorizationEvaluationCycleException if an evaluation cycle is detected for the specified user and
   *                                               action name.
   * @throws AuthorizationEvaluationException      if an evaluation error occurs during the evaluation process.
   */
  @NonNull
  AuthorizationEvaluationResult evaluate(
    @NonNull IAuthorizationUser user,
    @NonNull String actionName )
    throws AuthorizationEvaluationException;
}
