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
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.DefaultAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.OpposedAuthorizationDecision;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test class for {@link AuthorizationDecisionLockingAnalyzer}.
 */
public class AuthorizationDecisionLockingAnalyzerTest {

  private AuthorizationDecisionLockingAnalyzer analyzer;
  private IAuthorizationRequest request;

  // Mock reference decision class for testing
  private static class ReferenceDecision extends DefaultAuthorizationDecision {
    public ReferenceDecision( IAuthorizationRequest request, boolean isGranted ) {
      super( request, isGranted );
    }
  }

  @Before
  public void setUp() {
    analyzer = new AuthorizationDecisionLockingAnalyzer();
    request = mock( IAuthorizationRequest.class );
  }

  // region analyze() - Granted Decision Tests

  /**
   * OR: Granted (overall decision)
   * - AND: Granted (does not include the reference decision)
   * - Has Role X decision: Granted (locks overall decision to granted)
   * <p>
   * Expected: Locked, returns the granted alternative that does not include reference decision.
   */
  @Test
  public void testAnalyzeGrantedDecisionLockedByAlternativeWithoutReferenceDecision() {
    var roleDecision = new DefaultAuthorizationDecision( request, true );
    var referenceDecision = new ReferenceDecision( request, true );

    // Create a granted decision with two alternatives:
    // 1. One granted alternative with only role decision (locks)
    // 2. One granted alternative with reference decision
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeGrantedDecisionNotLockedWhenAllAlternativesIncludeReferenceDecision() {
    var referenceDecision = new ReferenceDecision( request, true );
    var otherDecision = new DefaultAuthorizationDecision( request, true );

    // All alternatives include the reference decision
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision, otherDecision ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAnalyzeGrantedDecisionLockedByMultipleAlternativesWithoutReferenceDecision() {
    var roleDecision1 = new DefaultAuthorizationDecision( request, true );
    var roleDecision2 = new DefaultAuthorizationDecision( request, true );
    var referenceDecision = new ReferenceDecision( request, true );

    // Two alternatives without reference decision, both lock
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision1 ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision2 ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision1 ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision2 ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeGrantedDecisionNotLockedWhenGrantedTermIncludesReferenceDecision() {
    var referenceDecision = new ReferenceDecision( request, true );
    var otherDecision = new DefaultAuthorizationDecision( request, false );

    // Only granted alternative includes reference decision - not locked
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf( otherDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAnalyzeGrantedDecisionWithOpposedReferenceDecisionIsNotLocking() {
    var roleDecision = new DefaultAuthorizationDecision( request, true );
    var referenceDecision = new ReferenceDecision( request, false );

    // Opposed reference decision still counts as reference decision
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( referenceDecision ) ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeGrantedDecisionSkipsDeniedAlternativeEvenWithGrantedNonReferenceChildren() {
    var grantedDecisionInsideDeniedAlt = new DefaultAuthorizationDecision( request, true );
    var grantedDecisionInsideGrantedAlt = new DefaultAuthorizationDecision( request, true );

    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      // Denied AND term — must be ignored entirely.
      new AllAuthorizationDecision( request, false, orderedSetOf( grantedDecisionInsideDeniedAlt ) ),
      // Granted AND term without reference decision — the real locking alternative.
      new AllAuthorizationDecision( request, true, orderedSetOf( grantedDecisionInsideGrantedAlt ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( grantedDecisionInsideGrantedAlt ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    // Exactly one locking alternative — the denied AND term must NOT have contributed.
    assertEquals( 1, result.get().getDecisions().size() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }
  // endregion

  // region analyze() - Denied Decision Tests
  @Test
  public void testAnalyzeDeniedDecisionNotLockedForSingleAlternativeWithNoReferenceDecision() {
    var denyDecision1 = new DefaultAuthorizationDecision( request, false );
    var denyDecision2 = new DefaultAuthorizationDecision( request, false );

    // Denied overall but no reference decision present
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision1, denyDecision2 ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAnalyzeDeniedDecisionNotLockedForSingleAlternativeWithReferenceDecisionAndNoSiblings() {
    var referenceDecision = new ReferenceDecision( request, false );

    // Alternative with reference decision but no denied siblings
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAnalyzeDeniedDecisionNotLockedForSingleAlternativeWithReferenceDecisionAndGrantedSibling() {
    var referenceDecision = new ReferenceDecision( request, true );
    var grantedDecision = new DefaultAuthorizationDecision( request, true );

    // Alternative with reference decision but no denied siblings
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision, grantedDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }

  @Test
  public void testAnalyzeDeniedDecisionNotLockedForMultipleAlternativesOneLockingAndOneUnlocking() {
    var referenceDecision1 = new ReferenceDecision( request, false );
    var denyDecision = new DefaultAuthorizationDecision( request, false );

    var referenceDecision2 = new ReferenceDecision( request, false );
    var grantedDecision = new DefaultAuthorizationDecision( request, true );

    var lockingAlternative =
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision1, denyDecision ) );

    // To be locked, cannot have any "unlocking" alternatives.
    var unlockingAlternative =
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision2, grantedDecision ) );

    var input =
      new AnyAuthorizationDecision( request, false, orderedSetOf( lockingAlternative, unlockingAlternative ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertFalse( result.isPresent() );
  }


  @Test
  public void testAnalyzeDeniedDecisionLockedForSingleAlternativeWithReferenceDecisionAndDeniedSibling() {
    var referenceDecision = new ReferenceDecision( request, true );
    var denyDecision = new DefaultAuthorizationDecision( request, false );

    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision, denyDecision ) ) ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeDeniedDecisionLockedForSingleAlternativeWithReferenceDecisionAndDeniedAndGrantedSiblings() {
    var referenceDecision = new ReferenceDecision( request, true );
    var denyDecision1 = new DefaultAuthorizationDecision( request, false );
    var denyDecision2 = new DefaultAuthorizationDecision( request, false );
    var grantedDecision = new DefaultAuthorizationDecision( request, true );

    // Multiple denied siblings in the same alternative
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf(
        referenceDecision, denyDecision1, denyDecision2, grantedDecision ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision1, denyDecision2 ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeDeniedDecisionLockedForMultipleAlternativesWithReferenceDecisionAndDeniedSibling() {
    var referenceDecision1 = new ReferenceDecision( request, true );
    var referenceDecision2 = new ReferenceDecision( request, true );
    var denyDecision1 = new DefaultAuthorizationDecision( request, false );
    var denyDecision2 = new DefaultAuthorizationDecision( request, false );

    // Multiple alternatives, each with reference + deny decision
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision1, denyDecision1 ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision2, denyDecision2 ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision1 ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision2 ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeDeniedDecisionLockedForSingleAlternativeWithOpposedReferenceDecisionAndDeniedSibling() {
    var referenceDecision = new ReferenceDecision( request, false );
    var denyDecision = new DefaultAuthorizationDecision( request, false );

    // Opposed reference decision with denied sibling
    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( referenceDecision ), denyDecision ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }

  @Test
  public void testAnalyzeDeniedDecisionSkipsGrantedAlternative() {
    var referenceDecision1 = new ReferenceDecision( request, true );
    var denyDecision1 = new DefaultAuthorizationDecision( request, false );
    var referenceDecision2 = new ReferenceDecision( request, true );
    var denyDecision2 = new DefaultAuthorizationDecision( request, false );

    var input = new AnyAuthorizationDecision( request, false, orderedSetOf(
      // Granted AND term — must be ignored entirely.
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision1, denyDecision1 ) ),
      // Denied AND term with reference + deny — the real locking alternative.
      new AllAuthorizationDecision( request, false, orderedSetOf( referenceDecision2, denyDecision2 ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision2 ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    // Exactly one locking alternative — the granted AND term must NOT have contributed.
    assertEquals( 1, result.get().getDecisions().size() );
    assertTrue( equals( expectedOutput, result.get() ) );
  }
  // endregion


  // region Integration Tests

  @Test
  public void testAnalyzeWithComplexDecisionStructure() {
    // Create a complex decision structure that requires DNF normalization
    var roleDecision = new DefaultAuthorizationDecision( request, true );
    var referenceDecision = new ReferenceDecision( request, true );

    // Nested structure: AND( OR(role), OR(reference) )
    // After DNF: OR( AND(role, reference) )
    // The alternative includes reference, so should not be locked
    var nestedOr1 = new AnyAuthorizationDecision( request, true, orderedSetOf( roleDecision ) );
    var nestedOr2 = new AnyAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) );
    var input = new AllAuthorizationDecision( request, true, orderedSetOf( nestedOr1, nestedOr2 ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertNotNull( result );
    // The decision should be analyzed after normalization
  }

  @Test
  public void testAnalyzePreservesRequestFromOriginalDecision() {
    var roleDecision = new DefaultAuthorizationDecision( request, true );

    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) )
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertEquals( request, result.get().getRequest() );
  }

  @Test
  public void testAnalyzeMixedGrantedAndDeniedAlternativesInGrantedDecision() {
    var roleDecision = new DefaultAuthorizationDecision( request, true );
    var referenceDecision = new ReferenceDecision( request, true );
    var denyDecision = new DefaultAuthorizationDecision( request, false );

    // Overall granted, with mixed alternatives
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( roleDecision ) ), // Locks
      new AllAuthorizationDecision( request, true, orderedSetOf( referenceDecision ) ), // Doesn't lock
      new AllAuthorizationDecision( request, false, orderedSetOf( denyDecision ) ) // Denied, ignored
    ) );

    var result = analyzer.analyze( input, ReferenceDecision.class::isInstance );

    assertTrue( result.isPresent() );
    assertEquals( 1, result.get().getDecisions().size() );
  }

  // endregion

  // region Helper Methods

  /**
   * Creates an ordered set from the provided decisions to ensure deterministic ordering.
   */
  @NonNull
  private Set<IAuthorizationDecision> orderedSetOf( IAuthorizationDecision... decisions ) {
    return new LinkedHashSet<>( List.of( decisions ) );
  }

  /**
   * Compares two authorization decisions for equality using structural comparison.
   */
  private static boolean equals( @NonNull IAuthorizationDecision a, @NonNull IAuthorizationDecision b ) {
    if ( a == b ) {
      return true;
    }

    var baseTypeA = a.getBaseType();
    var baseTypeB = b.getBaseType();
    if ( baseTypeA != baseTypeB
      || a.getRequest() != b.getRequest()
      || a.isGranted() != b.isGranted() ) {
      return false;
    }

    if ( baseTypeA == IAnyAuthorizationDecision.class || baseTypeA == IAllAuthorizationDecision.class ) {
      return equals( (ICompositeAuthorizationDecision) a, (ICompositeAuthorizationDecision) b );
    }

    // Terminals or opposed are always reused as-is, so reference equality is sufficient.
    return false;
  }

  private static boolean equals( @NonNull ICompositeAuthorizationDecision compositeA,
                                 @NonNull ICompositeAuthorizationDecision compositeB ) {
    if ( compositeA.getDecisions().size() != compositeB.getDecisions().size() ) {
      return false;
    }

    // Convert sets to lists, for indexed access.
    List<IAuthorizationDecision> decisionsA = new ArrayList<>( compositeA.getDecisions() );
    List<IAuthorizationDecision> decisionsB = new ArrayList<>( compositeB.getDecisions() );
    for ( int i = 0; i < decisionsA.size(); i++ ) {
      if ( !equals( decisionsA.get( i ), decisionsB.get( i ) ) ) {
        return false;
      }
    }

    return true;
  }
  // endregion
}
