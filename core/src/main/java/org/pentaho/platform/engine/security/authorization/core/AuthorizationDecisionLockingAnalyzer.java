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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The {@code AuthorizationDecisionLockingAnalyzer} class provides functionality to analyze authorization decisions and
 * determine if they are "locked" with respect to a certain reference decision type, such as an Action-Role binding
 * decision.
 * <p>
 * This class provides methods to analyze if a decision is locked, as well as extract justifications for why it is
 * locked.
 *
 * <h1>Locked Decision Concept</h1>
 * A decision is considered <b>locked</b> relative to a certain reference contained decision, if the overall granted
 * status cannot be changed by altering the granted status of the reference decision. In practice, this class identifies
 * the reference decision by its class type, given as an argument.
 * <p>
 * To be able to determine the locked status of a decision, it is necessary that it contains sufficient reporting
 * information. This is achieved by ensuring that the authorization operation that produced the decision was executed
 * with a {@link IAuthorizationOptions#getDecisionReportingMode() decision reporting mode} of
 * {@link AuthorizationDecisionReportingMode#FULL}.
 * <p>
 * Moreover, to facilitate the analysis of the decision structure, as well as shaping the corresponding locked
 * justifications text, it's advantageous to convert the decision into Disjunctive Normal Form (DNF), i.e., as an OR
 * ({@link IAnyAuthorizationDecision Any}) decision, composed of AND ({@link IAllAuthorizationDecision All}) decisions,
 * composed of (possibly {@link IOpposedAuthorizationDecision negated/opposed}) terminal decisions. This transformation
 * is handled internally by this class.
 *
 * <h1>Locked Decision Determination Algorithm</h1>
 * The following describes the algorithm to determine if a decision is locked and assumes the decision was obtained with
 * full reporting mode and is in DNF format.
 *
 * <h2>When Locked to Granted Status</h2>
 * A <b>granted</b> overall decision is locked if and only if <i>at least one</i> of its alternative child terms (AND
 * decisions) is granted and <i>does not include the reference decision</i>. All granted alternative child terms meeting
 * these criteria contribute with a justification for the locked status.
 * <p>
 * An example of such a case would be one that grants exclusively based on the user having a certain role (regardless
 * of specific actions, resources, etc.).
 * <pre>
 *   OR: Granted <-- overall decision
 *     - AND: Granted <-- does not include the reference decision
 *        - Has Role X decision: Granted  <-- locks overall decision to granted, regardless of the reference decision status
 *
 *     - AND: Granted
 *        - ...
 *
 *     - AND: Denied
 *        - ...
 *     ...
 * </pre>
 *
 * <h2>When Locked to Denied Status</h2>
 * <p>
 * In this case, first note that all alternative child terms (AND decisions) are necessarily denied.
 * <p>
 * A <b>denied</b> overall decision is locked if both of the following conditions are met:
 * <ol>
 *   <li>
 *     <i>at least one</i> of its alternative child terms includes the reference decision (possibly opposed) and at
 *     least one other sibling decision that is denied.
 * <p>
 *     An example of such a sibling decision would be one resulting from a "deny rule", expressing some unmet
 *     requirement.
 * <p>
 *     All denied alternative child terms (AND decisions) meeting these criteria contribute with a justification for the
 *     locked status.
 *   </li>
 *   <li>
 *     <i>none</i> of its alternative child terms is such that: it includes the reference decision (possibly opposed)
 *     and only granted sibling decisions, if any. In this case, it is the reference decision itself that is responsible
 *     for the denied status, and removing/flipping it would also flip the alternative term and overall decision to
 *     granted.
 *   </li>
 * </ol>
 * <p>
 * Example of a case meeting the first condition:
 * <pre>
 *   OR: Denied <-- overall decision
 *     - AND: Denied <-- includes the reference decision and at least one other denied sibling decision
 *       - Reference Decision: Granted or Denied
 *       - Some Other Decision: Denied   <-- locks overall decision to denied, regardless of the reference decision status
 *       - Yet Another Decision: Granted <-- does not affect the locking
 *
 *     ...
 * </pre>
 * <p>
 * Example of a case meeting the second condition:
 * <pre>
 *   OR: Denied
 *     - AND: Denied
 *       - Reference Decision: Denied    <-- flipping this to granted would flip the `AND` and overall decision to granted.
 *       - Some Other Decision: Granted  <-- all other siblings, if any, must be granted
 *       - Yet Another Decision: Granted
 *
 *     ...
 * </pre>
 */
public class AuthorizationDecisionLockingAnalyzer {

  /**
   * Analyzes a given decision to determine if it is locked relative to a specified reference decision.
   * When locked, returns a new decision structure, in DNF format, containing only the child decisions that contribute
   * to the locked status. These decisions can then be used to extract text justifications for the locked status.
   *
   * @param fullDecision               The full authorization decision to analyze. Must have been produced by an
   *                                   authorization operation with a
   *                                   {@link IAuthorizationOptions#getDecisionReportingMode() decision reporting mode}
   *                                   of {@link AuthorizationDecisionReportingMode#FULL}.
   * @param isLockingReferenceDecision A predicate that identifies the locking reference decision. For example, to
   *                                   evaluate locking relative to decisions of a certain class, the predicate could be
   *                                   implemented as: <pre>LockingReferenceDecisionClass.class::isInstance</pre>.
   * @return An optional containing a filtered decision, containing only elements which contribute to the locked status,
   * if the given full decision is locked; an empty optional, if the given full decision is not locked.
   * <p>
   * If the decision is granted, somehow invalidating/denying all the returned alternative decisions will be necessary
   * to unlock the decision relative to the reference decision.
   * If the decision is denied, somehow invalidating/granting any one of the returned alternative decisions will be
   * sufficient to unlock the decision relative to the reference decision.
   */
  @NonNull
  public Optional<IAnyAuthorizationDecision> analyze(
    @NonNull IAuthorizationDecision fullDecision,
    @NonNull Predicate<IAuthorizationDecision> isLockingReferenceDecision ) {
    // OR -> AND -> terminal | NOT( terminal )
    var dnfDecision = new AuthorizationDecisionDNFNormalizer().normalize( fullDecision );

    Set<IAuthorizationDecision> lockingAlternatives = new LinkedHashSet<>();
    for ( var allTerm : dnfDecision.getDecisions() ) {
      var isGranted = dnfDecision.isGranted();

      if ( allTerm.isGranted() != isGranted ) {
        continue;
      }

      var alternativeDecision = (IAllAuthorizationDecision) allTerm;

      var result = isGranted
        ? analyzeGrantedAlternative( alternativeDecision, isLockingReferenceDecision )
        : analyzeDeniedAlternative( alternativeDecision, isLockingReferenceDecision );

      var lockedFilteredDecisionOpt = result.getLockedFilteredDecision();
      var isAlternativeLocked = lockedFilteredDecisionOpt.isPresent();
      if ( isAlternativeLocked ) {
        lockingAlternatives.add( lockedFilteredDecisionOpt.get() );
      } else if ( !isGranted && result.getHasReferenceDecision() ) {
        // One unlocked alternative containing the reference decision is enough to conclude the overall decision is not
        // locked, even if other alternatives are locked.
        return Optional.empty();
      }
    }

    return lockingAlternatives.isEmpty()
      ? Optional.empty()
      : Optional.of( new AnyAuthorizationDecision( fullDecision.getRequest(), lockingAlternatives ) );
  }

  protected static final class AlternativeAnalysisResult {
    private final boolean hasReferenceDecision;
    @Nullable
    private final IAllAuthorizationDecision lockedFilteredDecision;

    public AlternativeAnalysisResult(
      boolean hasReferenceDecision,
      @Nullable
      IAllAuthorizationDecision lockedFilteredDecision ) {
      this.hasReferenceDecision = hasReferenceDecision;
      this.lockedFilteredDecision = lockedFilteredDecision;
    }

    public boolean getHasReferenceDecision() {
      return hasReferenceDecision;
    }

    @NonNull
    public Optional<IAllAuthorizationDecision> getLockedFilteredDecision() {
      return Optional.ofNullable( lockedFilteredDecision );
    }

    @Override
    public boolean equals( Object obj ) {
      if ( obj == this ) {
        return true;
      }
      if ( obj == null || obj.getClass() != this.getClass() ) {
        return false;
      }

      var that = (AlternativeAnalysisResult) obj;
      return this.hasReferenceDecision == that.hasReferenceDecision
        && Objects.equals( this.lockedFilteredDecision, that.lockedFilteredDecision );
    }

    @Override
    public int hashCode() {
      return Objects.hash( hasReferenceDecision, lockedFilteredDecision );
    }

    @Override
    public String toString() {
      return String.format(
        "AlternativeAnalysisResult[hasReferenceDecision=%s, lockedFilteredDecision=%s]",
        hasReferenceDecision,
        lockedFilteredDecision );
    }

  }

  /**
   * Given an alternative decision from a granted overall decision, determines if it contributes to locking the
   * overall decision relative to a reference decision, and if so, returns a new alternative decision containing only
   * the child decisions that contribute to locking.
   *
   * @param alternativeDecision        The alternative child term (AND decision) to analyze, part of a decision
   *                                   in DNF format.
   * @param isLockingReferenceDecision A predicate that identifies the locking reference decision.
   * @return An alternative analysis result containing a filtered alternative decision, if the given alternative
   * decision is locked; an empty optional, otherwise. Additionally, the result indicates whether the alternative
   * includes the reference decision, which is relevant for determining the locked status of denied overall decisions.
   */
  @NonNull
  protected AlternativeAnalysisResult analyzeGrantedAlternative(
    @NonNull IAllAuthorizationDecision alternativeDecision,
    @NonNull Predicate<IAuthorizationDecision> isLockingReferenceDecision ) {

    // Check for any granted child terms but no reference decision.
    Set<IAuthorizationDecision> grantedOtherTerms = new LinkedHashSet<>();
    for ( var term : alternativeDecision.getDecisions() ) {
      if ( isMatchOrOpposedMatchDecision( term, isLockingReferenceDecision ) ) {
        return new AlternativeAnalysisResult( true, null );
      }

      if ( term.isGranted() ) {
        // This term contributes to the locked status.
        grantedOtherTerms.add( term );
      }
    }

    if ( grantedOtherTerms.isEmpty() ) {
      return new AlternativeAnalysisResult( false, null );
    }

    return new AlternativeAnalysisResult(
      false,
      new AllAuthorizationDecision( alternativeDecision.getRequest(), grantedOtherTerms ) );
  }

  /**
   * Given an alternative decision from a denied overall decision, determines if it contributes to locking the
   * overall decision relative to a reference decision, and if so, returns a new alternative decision containing only
   * the child decisions that contribute to locking.
   *
   * @param alternativeDecision        The alternative child term (AND decision) to analyze, part of a decision
   *                                   in DNF format.
   * @param isLockingReferenceDecision A predicate that identifies the locking reference decision.
   * @return @return An alternative analysis result containing a filtered alternative decision, if the given alternative
   * decision is locked; an empty optional, otherwise. Additionally, the result indicates whether the alternative
   * includes the reference decision, which is relevant for determining the locked status of denied overall decisions.
   */
  @NonNull
  protected AlternativeAnalysisResult analyzeDeniedAlternative(
    @NonNull IAllAuthorizationDecision alternativeDecision,
    @NonNull Predicate<IAuthorizationDecision> isLockingReferenceDecision ) {

    boolean hasReferenceDecision = false;
    Set<IAuthorizationDecision> deniedSiblingTerms = new LinkedHashSet<>();
    for ( var term : alternativeDecision.getDecisions() ) {
      if ( isMatchOrOpposedMatchDecision( term, isLockingReferenceDecision ) ) {
        hasReferenceDecision = true;
      } else if ( term.isDenied() ) {
        deniedSiblingTerms.add( term );
      }
    }

    if ( !hasReferenceDecision ) {
      return new AlternativeAnalysisResult( false, null );
    }

    if ( !deniedSiblingTerms.isEmpty() ) {
      // Remains denied. Locked.
      return new AlternativeAnalysisResult( true,
        new AllAuthorizationDecision( alternativeDecision.getRequest(), deniedSiblingTerms ) );
    }

    // If all other siblings, if any, are granted, then the reference decision is solely responsible for the
    // alternative's decision value, and as a consequence, also for that of the overall decision. Not locked.
    return new AlternativeAnalysisResult( true, null );
  }

  /**
   * Determines if a given decision matches a given predicate, or an opposed decision of one that does.
   *
   * @param decision The decision to check.
   * @return <code>true</code> if the decision matches the predicate, or an opposed of one does; <code>false</code>
   * otherwise.
   */
  protected boolean isMatchOrOpposedMatchDecision(
    @NonNull IAuthorizationDecision decision,
    @NonNull Predicate<IAuthorizationDecision> matcher ) {
    return matcher.test( decision )
      || ( ( decision instanceof IOpposedAuthorizationDecision opposedDecision )
      && isMatchOrOpposedMatchDecision( ( opposedDecision ).getOpposedToDecision(), matcher ) );
  }
}
