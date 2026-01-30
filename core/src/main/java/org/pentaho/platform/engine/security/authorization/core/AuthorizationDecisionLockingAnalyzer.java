package org.pentaho.platform.engine.security.authorization.core;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationOptions;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;

import java.util.LinkedHashSet;
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
 * A <b>denied</b> overall decision is locked if and only if <i>at least one</i> of its alternative child terms (AND
 * decisions) (and note that all child terms are necessarily denied) includes the reference decision (possibly opposed)
 * and at least one other sibling decision that is denied. In this case, the sibling decision "blocks" the effect of the
 * reference decision, whatever its status, and whether opposed or not.
 * <p>
 * An example of such a sibling decision would be one resulting from a "deny rule", expressing some unmet requirement.
 * <p>
 * All denied alternative child terms (AND decisions) meeting these criteria contribute with a justification for the
 * locked status.
 * <pre>
 *   OR: Denied <-- overall decision
 *     - AND: Denied <-- includes the reference decision and at least one other denied sibling decision
 *       - Reference Decision: Granted
 *       - Some Other Decision: Denied  <-- locks overall decision to denied, regardless of the reference decision status
 *       - Yet Another Decision: Granted <-- does not affect the locking
 *
 *     - AND: Denied
 *       - ...
 *
 *     - AND: Denied
 *       - ...
 *
 *     ... <-- all terms must be denied
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
      var alternativeDecision = (IAllAuthorizationDecision) allTerm;

      var result = dnfDecision.isGranted()
        ? analyzeGrantedAlternative( alternativeDecision, isLockingReferenceDecision )
        : analyzeDeniedAlternative( alternativeDecision, isLockingReferenceDecision );

      result.ifPresent( lockingAlternatives::add );
    }

    return lockingAlternatives.isEmpty()
      ? Optional.empty()
      : Optional.of( new AnyAuthorizationDecision( fullDecision.getRequest(), lockingAlternatives ) );
  }

  /**
   * Given an alternative decision from a granted overall decision, determines if it contributes to locking the
   * overall decision relative to a reference decision, and if so, returns a new alternative decision containing only
   * the child decisions that contribute to locking.
   *
   * @param alternativeDecision        The alternative child term (AND decision) to analyze, part of a decision
   *                                   in DNF format.
   * @param isLockingReferenceDecision A predicate that identifies the locking reference decision.
   * @return An optional containing a filtered alternative decision, if the given alternative decision is locked; an
   * empty optional, otherwise.
   */
  @NonNull
  protected Optional<IAllAuthorizationDecision> analyzeGrantedAlternative(
    @NonNull IAllAuthorizationDecision alternativeDecision,
    @NonNull Predicate<IAuthorizationDecision> isLockingReferenceDecision ) {

    // Check for any granted child terms but no reference decision.
    Set<IAuthorizationDecision> grantedOtherTerms = new LinkedHashSet<>();
    for ( var term : alternativeDecision.getDecisions() ) {
      if ( isMatchOrOpposedMatchDecision( term, isLockingReferenceDecision ) ) {
        return Optional.empty();
      }

      if ( term.isGranted() ) {
        // This term contributes to the locked status.
        grantedOtherTerms.add( term );
      }
    }

    if ( grantedOtherTerms.isEmpty() ) {
      return Optional.empty();
    }

    return Optional.of( new AllAuthorizationDecision( alternativeDecision.getRequest(), grantedOtherTerms ) );
  }

  /**
   * Given an alternative decision from a denied overall decision, determines if it contributes to locking the
   * overall decision relative to a reference decision, and if so, returns a new alternative decision containing only
   * the child decisions that contribute to locking.
   *
   * @param alternativeDecision        The alternative child term (AND decision) to analyze, part of a decision
   *                                   in DNF format.
   * @param isLockingReferenceDecision A predicate that identifies the locking reference decision.
   * @return An optional containing a filtered alternative decision, if the given alternative decision is locked; an
   * empty optional, otherwise.
   */
  @NonNull
  protected Optional<IAllAuthorizationDecision> analyzeDeniedAlternative(
    @NonNull IAllAuthorizationDecision alternativeDecision,
    @NonNull Predicate<IAuthorizationDecision> isLockingReferenceDecision ) {
    // Check for the reference decision and any denied sibling term.
    boolean hasReferenceDecision = false;
    Set<IAuthorizationDecision> deniedSiblingTerms = new LinkedHashSet<>();
    for ( var term : alternativeDecision.getDecisions() ) {
      if ( isMatchOrOpposedMatchDecision( term, isLockingReferenceDecision ) ) {
        hasReferenceDecision = true;
      } else if ( term.isDenied() ) {
        // Doesn't matter if it is directly a terminal or a NOT( terminal ).
        // At most, that will change the justification text.
        deniedSiblingTerms.add( term );
      }
    }

    if ( !hasReferenceDecision || deniedSiblingTerms.isEmpty() ) {
      return Optional.empty();
    }

    return Optional.of( new AllAuthorizationDecision( alternativeDecision.getRequest(), deniedSiblingTerms ) );
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
