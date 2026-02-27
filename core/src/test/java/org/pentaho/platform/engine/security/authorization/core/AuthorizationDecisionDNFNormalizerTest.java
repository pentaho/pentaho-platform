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
import org.pentaho.platform.api.engine.security.authorization.decisions.IDerivedAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.DefaultAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.OpposedAuthorizationDecision;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class AuthorizationDecisionDNFNormalizerTest {

  private IAuthorizationRequest request;

  @Before
  public void setUp() {
    request = mock( IAuthorizationRequest.class );
  }

  @Test
  public void testNormalizeReturnsSameObjectWhenAlreadyNormalized() {
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf(
        new DefaultAuthorizationDecision( request, true )
      ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertSame( input, output );
  }

  // Input:  (& A B)
  // Output: (| (& A B))
  @Test
  public void testNormalizeEnsuresTopLevelOrDecision() {
    var leafA = new DefaultAuthorizationDecision( request, true );

    var input = new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) );

    // Wrap an Any around `input`.
    var expectedOutput = new AnyAuthorizationDecision( request, true,
      orderedSetOf( new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ) ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input  (F): ~(& (| A B) C)
  // 1:           (| ~(| A B) ~C)   i)  move not inwards
  // Output (F):  (| (& ~A ~B) (& ~C))  ii) move not inwards
  @Test
  public void testNormalizeMovesNotInward() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );

    var input = new OpposedAuthorizationDecision(
      new AllAuthorizationDecision( request, true,
        orderedSetOf(
          new AnyAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) ),
          leafC
        ) ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false,
      orderedSetOf(
        new AllAuthorizationDecision( request, false,
          orderedSetOf(
            new OpposedAuthorizationDecision( leafA ),
            new OpposedAuthorizationDecision( leafB )
          ) ),
        new AllAuthorizationDecision( request, false,
          orderedSetOf(
            new OpposedAuthorizationDecision( leafC ) ) ) ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input  (F): ~(& ~A)
  // 1:           (| ~~A)     i)  move not inwards
  // Output (F):  (| (& A) )  ii) eliminate double negation and ensure strict DNF
  @Test
  public void testNormalizeEliminatesDoubleNegation() {
    var leafA = new DefaultAuthorizationDecision( request, true );

    var input = new OpposedAuthorizationDecision(
      new AllAuthorizationDecision( request, false,
        orderedSetOf( new OpposedAuthorizationDecision( leafA ) ) ) );

    var expectedOutput = new AnyAuthorizationDecision(
      request,
      true,
      orderedSetOf(
        new AllAuthorizationDecision(
          request,
          true,
          orderedSetOf( leafA ) ) ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input (F):  (& ~(| A B) ~(| C D))
  // 1:          (& (& ~A ~B) (& ~C ~D))  i) move not inwards
  // 2:          (& ~A ~B ~C ~D)          ii) flatten nested ANDs
  // Output (F): (| (& ~A ~B ~C ~D))      iii) add top level Or
  @Test
  public void testNormalizeMovesNotInwardsAndRebuildsCompositesWithModifiedChildren() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var leafD = new DefaultAuthorizationDecision( request, true );
    var input = new AllAuthorizationDecision( request, false, orderedSetOf(
      new OpposedAuthorizationDecision( new AnyAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) ) ),
      new OpposedAuthorizationDecision( new AnyAuthorizationDecision( request, true, orderedSetOf( leafC, leafD ) ) )
    ) );
    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false,
        orderedSetOf(
          new OpposedAuthorizationDecision( leafA ),
          new OpposedAuthorizationDecision( leafB ),
          new OpposedAuthorizationDecision( leafC ),
          new OpposedAuthorizationDecision( leafD ) ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:  (& A (| B C) D)
  // Input:  (& A (| B C) D (| E F))
  // Output: (| (& A D B E) (& A D B F) (& A D C E) (& A D C F))
  @Test
  public void testNormalizeDistributesAndOverOr() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var leafD = new DefaultAuthorizationDecision( request, true );
    var leafE = new DefaultAuthorizationDecision( request, true );
    var leafF = new DefaultAuthorizationDecision( request, true );

    var input = new AllAuthorizationDecision( request, true, orderedSetOf(
      leafA,
      new AnyAuthorizationDecision( request, true, orderedSetOf( leafB, leafC ) ),
      leafD,
      new AnyAuthorizationDecision( request, true, orderedSetOf( leafE, leafF ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      // Common And terms, A and D, are placed last on each Or.
      // Or terms are ordered depth-first.
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB, leafE, leafA, leafD ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB, leafF, leafA, leafD ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafE, leafA, leafD ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafF, leafA, leafD ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:  (& (| A B))
  // Output: (| (& A) (& B))
  @Test
  public void testNormalizeDistributesDegenerateAndOverSingleOr() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );

    var input = new AllAuthorizationDecision( request, true, orderedSetOf(
      new AnyAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:  (& A (| B C))
  // Output: (| (& B A) (& C A))
  @Test
  public void testNormalizeDistributesAndOverSingleOr() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );

    var input = new AllAuthorizationDecision( request, true, orderedSetOf(
      leafA,
      new AnyAuthorizationDecision( request, true, orderedSetOf( leafB, leafC ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB, leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafA ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:  ~(| (& A B) (& C D))
  // 2:        (& ~(& A B) ~(& C D))    i)  move not inwards
  // 3:        (& (| ~A ~B) (| ~C ~D))  ii) move not inwards
  // 4:                                 iii) distribute AND over OR (cross-product/all-combinations of terms)
  // Output: (| (& ~A ~C) (& ~A ~D) (& ~B ~C) (& ~B ~D))
  @Test
  public void testNormalizeMovesNotInwardsAndDistributesAndOverOr() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var leafD = new DefaultAuthorizationDecision( request, true );

    var input = new OpposedAuthorizationDecision(
      new AnyAuthorizationDecision( request, true, orderedSetOf(
        new AllAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) ),
        new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafD ) )
      ) ) );

    var expectedOutput = new AnyAuthorizationDecision( request, false, orderedSetOf(
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( leafA ),
        new OpposedAuthorizationDecision( leafC )
      ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( leafA ),
        new OpposedAuthorizationDecision( leafD )
      ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( leafB ),
        new OpposedAuthorizationDecision( leafC )
      ) ),
      new AllAuthorizationDecision( request, false, orderedSetOf(
        new OpposedAuthorizationDecision( leafB ),
        new OpposedAuthorizationDecision( leafD )
      ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );
    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:   (| (| A B) (& C D))
  // Output:  (| (& A) (& B) (& C D))
  @Test
  public void testNormalizeFlattensNestedOrs() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var leafD = new DefaultAuthorizationDecision( request, true );

    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AnyAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafD ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafD ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:   (& (& A B) (& C D))
  // Output:  (| (& A B C D))
  @Test
  public void testNormalizeFlattensNestedAnds() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var leafD = new DefaultAuthorizationDecision( request, true );

    var input = new AllAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC, leafD ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA, leafB, leafC, leafD ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:   (| (| A (| B C) ) )
  // Output:  (| (& A) (& B) (& C))
  @Test
  public void testNormalizeFlattensDoublyNestedOrs() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );
    var leafC = new DefaultAuthorizationDecision( request, true );
    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AnyAuthorizationDecision( request, true, orderedSetOf(
        leafA,
        new AnyAuthorizationDecision( request, true, orderedSetOf( leafB, leafC ) ) ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafC ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:   (| (& A (& B)))
  // Output:  (| (& A B))
  @Test
  public void testNormalizeFlattensNestedAndsWithOr() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );

    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA,
        new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) ) ) ) ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA, leafB ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
  }

  // Input:   (| (& A) (& B))
  // Output:  (| (& A) (& B))
  @Test
  public void testNormalizePreservesAndsWithASingleTerminal() {
    var leafA = new DefaultAuthorizationDecision( request, true );
    var leafB = new DefaultAuthorizationDecision( request, true );

    var input = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) )
    ) );

    var expectedOutput = new AnyAuthorizationDecision( request, true, orderedSetOf(
      new AllAuthorizationDecision( request, true, orderedSetOf( leafA ) ),
      new AllAuthorizationDecision( request, true, orderedSetOf( leafB ) )
    ) );

    var output = new AuthorizationDecisionDNFNormalizer().normalize( input );

    assertTrue( equals( expectedOutput, output ) );
    assertSame( input, output );
  }

  // Use ordered set so that children order is deterministic and comparisons can be made more easily.
  @NonNull
  private Set<IAuthorizationDecision> orderedSetOf( IAuthorizationDecision... decisions ) {
    return new LinkedHashSet<>( List.of( decisions ) );
  }

  // region Comparison Helpers
  private boolean equals( @NonNull IAuthorizationDecision a, @NonNull IAuthorizationDecision b ) {
    return new DecisionEqualityComparer().equals( a, b );
  }

  @FunctionalInterface
  interface EqualityComparer<T> {
    boolean equals( @NonNull T a, @NonNull T b );
  }

  private static class DecisionEqualityComparer implements EqualityComparer<IAuthorizationDecision> {
    @Override
    public boolean equals( @NonNull IAuthorizationDecision a, @NonNull IAuthorizationDecision b ) {
      if ( a == b ) {
        return true;
      }

      var baseTypeA = a.getBaseType();
      var baseTypeB = b.getBaseType();
      if ( baseTypeA != baseTypeB || !a.getRequest().equals( b.getRequest() ) || a.isGranted() != b.isGranted() ) {
        return false;
      }

      if ( baseTypeA == IAnyAuthorizationDecision.class || baseTypeA == IAllAuthorizationDecision.class ) {
        return equals( (ICompositeAuthorizationDecision) a, (ICompositeAuthorizationDecision) b );
      }

      if ( baseTypeA == IOpposedAuthorizationDecision.class ) {
        return equals( (IOpposedAuthorizationDecision) a, (IOpposedAuthorizationDecision) b );
      }

      if ( baseTypeA == IDerivedAuthorizationDecision.class ) {
        return equals( (IDerivedAuthorizationDecision) a, (IDerivedAuthorizationDecision) b );
      }

      return false;
    }

    private boolean equals( @NonNull ICompositeAuthorizationDecision compositeA,
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

    private boolean equals( @NonNull IOpposedAuthorizationDecision opposedA,
                            @NonNull IOpposedAuthorizationDecision opposedB ) {
      return equals( opposedA.getOpposedToDecision(), opposedB.getOpposedToDecision() );
    }

    private boolean equals( @NonNull IDerivedAuthorizationDecision opposedA,
                            @NonNull IDerivedAuthorizationDecision opposedB ) {
      return equals( opposedA.getDerivedFromDecision(), opposedB.getDerivedFromDecision() );
    }
  }
  // endregion
}
