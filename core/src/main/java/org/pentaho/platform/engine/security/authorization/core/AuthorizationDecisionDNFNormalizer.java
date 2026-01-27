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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAllAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAnyAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.IOpposedAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AllAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.AnyAuthorizationDecision;
import org.pentaho.platform.engine.security.authorization.core.decisions.OpposedAuthorizationDecision;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/***
 * Normalizes authorization decisions into Disjunctive Normal Form (DNF).
 * <p>
 * The normalization process involves:
 * 1. Moving NOTs inwards, by applying De Morgan's laws and eliminating double negations.
 * 2. Moving ANDs inwards, by distributing them over ORs.
 * 3. Flattening nested composites of the same type, i.e. ANDs and ORs.
 * <p>
 * The resulting decision structure is such that:
 * - The top-level decision is always an OR (IAnyAuthorizationDecision).
 * - Each child of the top-level OR is an AND (IAllAuthorizationDecision).
 * - Each child of the ANDs are terminal decisions (not composites),
 *   possibly negated (i.e. wrapped in NOT/IOpposedAuthorizationDecision).
 */
public class AuthorizationDecisionDNFNormalizer {

  /**
   * Abstracts authorization composite decision constructors.
   * This signature does not receive the granted status as for the present purposes it typically needs to be
   * re-determined from the contained children.
   */
  @FunctionalInterface
  private interface ICompositeAuthorizationDecisionFactory {
    @NonNull
    ICompositeAuthorizationDecision create(
      @NonNull IAuthorizationRequest request,
      @NonNull Set<IAuthorizationDecision> decisions );
  }

  /**
   * Supports the method
   * {@link AuthorizationDecisionVisitor#visitComposite(ICompositeAuthorizationDecision, IAuthorizationDecisionVisitor, ICompositeAuthorizationDecisionPostVisitor)}
   * to visit a composite decision and its children.
   * This is used to handle the resulting composite given the original composite and the visited children.
   * Either creating a new composite or returning the original one.
   */
  @FunctionalInterface
  private interface ICompositeAuthorizationDecisionPostVisitor {
    @NonNull
    ICompositeAuthorizationDecision visit(
      @NonNull ICompositeAuthorizationDecision decision,
      @NonNull Set<IAuthorizationDecision> visitedChildren,
      boolean hasModifiedChildren );
  }

  @FunctionalInterface
  private interface IAuthorizationDecisionVisitor {
    @NonNull
    IAuthorizationDecision visit( @NonNull IAuthorizationDecision decision );
  }


  @NonNull
  private static ICompositeAuthorizationDecisionFactory getCompositeAuthorizationDecisionFactory(
    @NonNull ICompositeAuthorizationDecision decision ) {
    return getCompositeAuthorizationDecisionFactory( decision, false );
  }

  @NonNull
  private static ICompositeAuthorizationDecisionFactory getCompositeAuthorizationDecisionFactory(
    @NonNull ICompositeAuthorizationDecision decision,
    boolean invert ) {

    Class<? extends IAuthorizationDecision> decisionType = decision.getBaseType();
    if ( decisionType == IAllAuthorizationDecision.class ) {
      return invert ? AnyAuthorizationDecision::new : AllAuthorizationDecision::new;
    }

    assert decisionType == IAnyAuthorizationDecision.class;

    return invert ? AllAuthorizationDecision::new : AnyAuthorizationDecision::new;
  }

  private static class AuthorizationDecisionVisitor implements IAuthorizationDecisionVisitor {
    @NonNull
    public IAuthorizationDecision visit( @NonNull IAuthorizationDecision decision ) {
      if ( decision instanceof IOpposedAuthorizationDecision ) {
        return visitOpposed( (IOpposedAuthorizationDecision) decision );
      }

      if ( decision instanceof ICompositeAuthorizationDecision ) {
        return visitComposite( (ICompositeAuthorizationDecision) decision );
      }

      return visitTerminal( decision );
    }

    @NonNull
    protected IAuthorizationDecision visitTerminal( @NonNull IAuthorizationDecision decision ) {
      return decision;
    }

    @NonNull
    protected IAuthorizationDecision visitOpposed( @NonNull IOpposedAuthorizationDecision decision ) {
      var opposedToVisited = visit( decision.getOpposedToDecision() );
      if ( opposedToVisited == decision.getOpposedToDecision() ) {
        return decision;
      }

      // If the visited decision is different, we need to wrap it back into an opposed decision
      return new OpposedAuthorizationDecision( opposedToVisited );
    }

    @NonNull
    protected ICompositeAuthorizationDecision visitComposite( @NonNull ICompositeAuthorizationDecision decision ) {
      return visitComposite(
        decision,
        // No child transformation. Normal visit.
        this,
        ( ignored, visitedChildren, hasModifiedChildren ) ->
          !hasModifiedChildren
            ? decision
            : getCompositeAuthorizationDecisionFactory( decision ).create( decision.getRequest(), visitedChildren )
      );
    }

    @NonNull
    protected ICompositeAuthorizationDecision visitComposite(
      @NonNull ICompositeAuthorizationDecision decision,
      @NonNull IAuthorizationDecisionVisitor childVisitor,
      @NonNull ICompositeAuthorizationDecisionPostVisitor compositePostVisitor ) {

      Set<IAuthorizationDecision> children = decision.getDecisions();

      Set<IAuthorizationDecision> visitedChildren = new LinkedHashSet<>( children.size() );
      boolean hasModifiedChildren = false;

      for ( IAuthorizationDecision child : children ) {
        var visitedChild = childVisitor.visit( child );
        if ( visitedChild != child ) {
          hasModifiedChildren = true;
        }

        visitedChildren.add( visitedChild );
      }

      return compositePostVisitor.visit( decision, visitedChildren, hasModifiedChildren );
    }
  }

  /**
   * Moves NOTs inwards, by applying De Morgan's laws and eliminating double negations.
   */
  private static class MoveNotInwardsTransformer extends AuthorizationDecisionVisitor {
    @NonNull
    @Override
    protected IAuthorizationDecision visitOpposed( @NonNull IOpposedAuthorizationDecision decision ) {
      IAuthorizationDecision childDecision = decision.getOpposedToDecision();

      // 1.1. Eliminate double negation ~~A = A
      if ( childDecision instanceof IOpposedAuthorizationDecision ) {
        return visit( ( (IOpposedAuthorizationDecision) childDecision ).getOpposedToDecision() );
      }

      if ( childDecision instanceof ICompositeAuthorizationDecision ) {
        // 1.2. Distribute NOT over AND/OR, by applying De Morgan's laws
        return visitCompositeNegated( (ICompositeAuthorizationDecision) childDecision );
      }

      // 1.3. Decision is: ~A, where A is a terminal, irreducible decision. Already normalized.
      return decision;
    }

    // Apply De Morgan's laws to the composite decision, i.e. negate the children and change the composite type from
    // AND to OR or vice versa.
    //
    // This is used to move NOTs inwards, as part of the normalization process.
    // - Distribute Not Over And ~(A ∧ B) = ~A ∨ ~B
    // - Distribute Not Over Or  ~(A ∨ B) = ~A ∧ ~B
    private ICompositeAuthorizationDecision visitCompositeNegated( @NonNull ICompositeAuthorizationDecision decision ) {

      return visitComposite(
        decision,
        child -> visit( new OpposedAuthorizationDecision( child ) ),
        ( ignored, visitedChildren, hasModifiedChildren ) ->
          getCompositeAuthorizationDecisionFactory( decision, true )
            .create( decision.getRequest(), visitedChildren )
      );
    }
  }

  /**
   * Moves ANDs inwards, by distributing them over ORs.
   */
  private static class MoveAndInwardsTransformer extends AuthorizationDecisionVisitor {
    @NonNull
    @Override
    protected ICompositeAuthorizationDecision visitComposite( @NonNull ICompositeAuthorizationDecision decision ) {
      if ( decision instanceof IAllAuthorizationDecision ) {
        return visitComposite( (IAllAuthorizationDecision) decision );
      }

      return super.visitComposite( decision );
    }

    @NonNull
    private ICompositeAuthorizationDecision visitComposite( @NonNull IAllAuthorizationDecision decision ) {
      // 2.1. Distribute AND over OR:
      //      A ∧ (B ∨ C) = (A ∧ B) ∨ (A ∧ C)
      //
      //      A ∧ (B ∨ C) ∧ D = (A ∧ B ∧ D) ∨ (A ∧ C ∧ D)
      //
      //      A ∧ (B ∨ C) ∧ (D ∨ E) = (A ∧ B ∧ D) ∨ (A ∧ B ∧ E) ∨ (A ∧ C ∧ D) ∨ (A ∧ C ∧ E)

      // 1. Separate OR children from non-OR children.
      // 2. All non-OR children will be common AND terms, part of all resulting conjunctions.
      // 3. Generate one resulting conj. per unique combination of terms from each of the OR children...

      // Collect all OR children and non-OR children
      List<IAnyAuthorizationDecision> orChildren = new ArrayList<>();
      List<IAuthorizationDecision> nonOrChildren = new ArrayList<>();
      for ( IAuthorizationDecision child : decision.getDecisions() ) {
        if ( child instanceof IAnyAuthorizationDecision ) {
          orChildren.add( (IAnyAuthorizationDecision) child );
        } else {
          nonOrChildren.add( child );
        }
      }

      if ( orChildren.isEmpty() ) {
        return decision;
      }

      // Degenerate AND with a single OR child and no non-OR children.
      if ( orChildren.size() == 1 && nonOrChildren.isEmpty() ) {
        return orChildren.get( 0 );
      }

      // The list of terms that will feed all AND decisions.
      // Mutated along the way, copied at the leaf of the recursion when building each AND decision.
      List<IAuthorizationDecision> andTerms = new ArrayList<>( orChildren.size() + nonOrChildren.size() );
      for ( int i = 0; i < orChildren.size(); i++ ) {
        // Add one placeholder position for each OR term, one per OR child.
        andTerms.add( null );
      }

      // All non-OR children are common terms and are placed at the end of the list.
      // This allows the OR terms' index to start at 0 and more easily iterate over the OR children
      // and fill in the AND terms.
      andTerms.addAll( nonOrChildren );

      Set<IAuthorizationDecision> resultingTerms = new LinkedHashSet<>();

      buildAndDecisionsRecursive( resultingTerms, decision.getRequest(), andTerms, orChildren, 0 );

      return new AnyAuthorizationDecision( decision.getRequest(), resultingTerms );
    }

    private void buildAndDecisionsRecursive(
      @NonNull Set<IAuthorizationDecision> resultingTerms,
      @NonNull IAuthorizationRequest request,
      @NonNull List<IAuthorizationDecision> andTerms,
      @NonNull List<IAnyAuthorizationDecision> orChildren,
      int orChildIndex ) {

      if ( orChildIndex == orChildren.size() ) {
        // Copy the AND terms.
        resultingTerms.add( new AllAuthorizationDecision( request, new LinkedHashSet<>( andTerms ) ) );
      } else {
        IAnyAuthorizationDecision orChild = orChildren.get( orChildIndex );
        for ( IAuthorizationDecision term : orChild.getDecisions() ) {
          // Set the current term to the placeholder position of AND terms.
          andTerms.set( orChildIndex, term );

          // Recur to process the next OR child
          buildAndDecisionsRecursive( resultingTerms, request, andTerms, orChildren, orChildIndex + 1 );
        }
      }
    }
  }

  /**
   * Flattens nested composites of the same type, i.e. ANDs and ORs.
   */
  private static class FlattenCompositesTransformer extends AuthorizationDecisionVisitor {
    @NonNull
    @Override
    protected ICompositeAuthorizationDecision visitComposite( @NonNull ICompositeAuthorizationDecision decision ) {
      // No child transformation. Normal child visit.
      // Handle composing the visited children into a new composite.
      return visitComposite( decision, this, this::visitCompositePost );
    }

    @NonNull
    protected Predicate<IAuthorizationDecision> buildMatchesDecisionTypePredicate(
      @NonNull Class<? extends IAuthorizationDecision> baseType ) {
      return child -> child.getBaseType() == baseType;
    }

    @NonNull
    protected ICompositeAuthorizationDecision visitCompositePost(
      @NonNull ICompositeAuthorizationDecision decision,
      @NonNull Set<IAuthorizationDecision> visitedChildren,
      boolean hasModifiedChildren ) {

      var compositeTypePredicate = buildMatchesDecisionTypePredicate( decision.getBaseType() );

      // Check children for any And/Or composites and flatten them.
      var hasChildrenSameType = visitedChildren.stream().anyMatch( compositeTypePredicate );
      if ( !hasChildrenSameType ) {
        return !hasModifiedChildren
          ? decision
          : getCompositeAuthorizationDecisionFactory( decision )
          .create( decision.getRequest(), visitedChildren );
      }

      Set<IAuthorizationDecision> newChildren = new LinkedHashSet<>( visitedChildren.size() );
      for ( IAuthorizationDecision visitedChild : visitedChildren ) {
        if ( compositeTypePredicate.test( visitedChild ) ) {
          newChildren.addAll( ( (ICompositeAuthorizationDecision) visitedChild ).getDecisions() );
        } else {
          newChildren.add( visitedChild );
        }
      }

      return getCompositeAuthorizationDecisionFactory( decision )
        .create( decision.getRequest(), newChildren );
    }
  }

  /**
   * As a final pass, ensure that all children of the top-level OR are ANDs.
   * Any terminal children are wrapped into single-child ANDs.
   */
  private static class EnsureChildOfOrIsAndTransformer implements IAuthorizationDecisionVisitor {

    @NonNull
    @Override
    public IAuthorizationDecision visit( @NonNull IAuthorizationDecision decision ) {
      if ( !( decision instanceof IAnyAuthorizationDecision ) ) {
        throw new IllegalStateException( "IAnyAuthorizationDecision expected" );
      }

      return visitOr( (IAnyAuthorizationDecision) decision );
    }

    @NonNull
    private IAnyAuthorizationDecision visitOr( @NonNull IAnyAuthorizationDecision decision ) {
      Set<IAuthorizationDecision> newChildren = new LinkedHashSet<>( decision.getDecisions().size() );

      boolean hasModifiedChildren = false;

      for ( var child : decision.getDecisions() ) {
        if ( child instanceof IAllAuthorizationDecision ) {
          newChildren.add( child );
        } else {
          hasModifiedChildren = true;
          // Wrap terminal decision into an AND
          newChildren.add(
            new AllAuthorizationDecision(
              decision.getRequest(),
              Set.of( child )
            )
          );
        }
      }

      return !hasModifiedChildren
        ? decision
        : new AnyAuthorizationDecision( decision.getRequest(), newChildren );
    }
  }

  @NonNull
  public IAnyAuthorizationDecision normalize( @NonNull IAuthorizationDecision decision ) {
    Assert.notNull( decision, "Argument 'decision' is required" );

    decision = new MoveNotInwardsTransformer().visit( decision );
    decision = new MoveAndInwardsTransformer().visit( decision );
    decision = new FlattenCompositesTransformer().visit( decision );

    // Make sure there is a top-level Or decision.
    // There will not be one, if the input decision is just an And containing terminals.
    if ( decision instanceof IAllAuthorizationDecision ) {
      decision = new AnyAuthorizationDecision( decision.getRequest(), Set.of( decision ) );
    }

    decision = new EnsureChildOfOrIsAndTransformer().visit( decision );

    return (IAnyAuthorizationDecision) decision;
  }
}
