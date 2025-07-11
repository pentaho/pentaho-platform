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
 * The {@code AuthorizationDecisionReportingMode} enum defines the level of detail that authorization decisions include
 * as justification for the decision made.
 * <p>
 * For any mode, the decision's granted status is always the same. However, depending on the mode, the type of decision
 * object returned and the level of detail it contains may vary.
 * <p>
 * Mostly, the reporting mode affects how composition rules, such as "Any" or "All", build composite decision results.
 * <p>
 * A higher level of detail is useful for diagnostics and auditing purposes, and even possibly for informing an end user
 * of all the conditions that contribute to a result in an authorization management user interface.
 */
public enum AuthorizationDecisionReportingMode {
  /**
   * The decision result of an authorization process includes only information collected until a decision is settled.
   * <p>
   * Composite decisions, such as
   * {@link org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision "Any"} or
   * {@link org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision "All"},
   * are only returned if they contain <i>more than one</i> decision, whatever their granted status. Otherwise, when
   * there no are no contained decisions (all contained rules abstained), a default decision is returned, depending on
   * the rule. And when there is a single contained decision, that decision is returned directly.
   * <p>
   * Composition rules such as "Any" or "All" will stop evaluating further contained rules as soon as the composite
   * decision becomes settled (either granted or denied) by the decisions produced by the rules consulted thus far.
   * While this mode is the most performant, the actual returned decision(s) is dependent on the evaluation order of
   * contained rules.
   * <p>
   * When an "Any" rule produces a <i>denied</i> decision, then all the denied decisions produced by the contained rules
   * are returned. If a single contained rule produces a <i>denied</i> decision, then that decision is returned, but,
   * otherwise, an "Any" decision is returned containing all the denied decisions.
   * <p>
   * Likewise, when an "All" rule produces a <i>granted</i> decision, then all the granted decisions produced by the
   * contained rules are returned. If a single contained rule produces a <i>granted</i> decision, then that decision
   * is returned, but, otherwise, an "All" decision is returned containing all the granted decisions.
   */
  SETTLED,

  /**
   * The decision result of an authorization process includes extra information, for composition operations where
   * alternative contained decisions could equally justify the resulting granted status.
   * <p>
   * Just like with the {@link #SETTLED SETTLED} mode, composite decisions such as
   * {@link org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision "Any"} or
   * {@link org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision "All"},
   * are only returned if they contain <i>more than one</i> decision, whatever their granted status.
   * However, unlike with the {@link #SETTLED SETTLED} mode, evaluation does not stop as soon as the composite decision
   * becomes settled (either granted or denied) by the decisions produced by the rules consulted thus far. Instead,
   * evaluation continues to collect further decisions that would support the same settled granted status.
   * <p>
   * In this mode, combination decisions will always include all the decisions that support the same granted status
   * (either granted or denied) as the composite decision itself, making the result more informative, as well as
   * independent of the rules' evaluation order. Compare this behavior to that of the {@link #SETTLED SETTLED} mode,
   * where this property would only hold for <i>denied</i> "Any" decisions, and for <i>granted</i> "All" decisions.
   * <p>
   * This reporting mode is less performant than the {@link #SETTLED SETTLED} mode, as it generally requires evaluating
   * additional rules to build the composite decision.
   */
  SAME_GRANTED_STATUS,

  /**
   * The decision result of an authorization process includes all decisions produced by the rules consulted and no
   * structure simplifications are performed.
   * <p>
   * For most cases, the structure of the resulting decision will match the structure of the composition authorization
   * rules used.
   */
  FULL
}
