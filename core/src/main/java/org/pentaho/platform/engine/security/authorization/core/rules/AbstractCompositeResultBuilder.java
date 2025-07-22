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

package org.pentaho.platform.engine.security.authorization.core.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.engine.security.authorization.AuthorizationDecisionReportingMode;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.decisions.ICompositeAuthorizationDecision;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractCompositeResultBuilder {

  private enum CompositeResultState {
    EMPTY,
    SINGLE,
    MULTIPLE
  }

  @NonNull
  private final AuthorizationDecisionReportingMode reportingMode;

  private CompositeResultState state = CompositeResultState.EMPTY;

  @Nullable
  private IAuthorizationDecision resultDecision;

  protected AbstractCompositeResultBuilder( @NonNull AuthorizationDecisionReportingMode reportingMode ) {
    this.reportingMode = reportingMode;
  }

  public boolean isEmpty() {
    return state == CompositeResultState.EMPTY;
  }

  public boolean isSettled() {
    return resultDecision != null && resultDecision.isGranted() == getSettledGrantedStatus();
  }

  public boolean isImmutable() {
    return isSettled() && reportingMode == AuthorizationDecisionReportingMode.SETTLED;
  }

  private void setSingleDecision( @NonNull IAuthorizationDecision decision ) {
    resultDecision = decision;
    state = CompositeResultState.SINGLE;
  }

  private void setMultipleDecision( @NonNull ICompositeAuthorizationDecision decision ) {
    resultDecision = decision;
    state = CompositeResultState.MULTIPLE;
  }

  public void withDecision( @NonNull IAuthorizationDecision decision ) {
    if ( state == CompositeResultState.EMPTY ) {
      setSingleDecision( decision );
      return;
    }

    assert resultDecision != null;

    // 1. Settled
    if ( isSettled() ) {
      // In all cases, preserves current granted status.

      // 1.a. Immutable (settled and SETTLED reporting mode) -> ignore
      if ( isImmutable() ) {
        return;
      }

      // 1.b. SAME_GRANTED_STATUS reporting and different granted status -> ignore
      if ( reportingMode.equals( AuthorizationDecisionReportingMode.SAME_GRANTED_STATUS )
        && decision.isGranted() != resultDecision.isGranted() ) {
        return;
      }

      // Else:
      // 1.c. Same granted status (regardless of _other_ reporting mode) OR
      // 1.d. Different granted status and FULL reporting
      //   -> preserve granted status and combine
      addDecision( decision );
      return;
    }

    // 2. Not settled
    // 2.a. Same granted status -> preserve granted status and combine
    //      (regardless of reporting mode: SETTLED, SAME_GRANTED_STATUS, FULL)
    if ( resultDecision.isGranted() == decision.isGranted() ) {
      addDecision( decision );

    } else if ( reportingMode.equals( AuthorizationDecisionReportingMode.FULL ) ) {
      // 2.b. Different granted status
      // 2.b.1. FULL reporting -> replace granted status and combine
      addDecision( decision.isGranted(), decision );
    } else {
      // 2.b.2. else (SETTLED, SAME_GRANTED_STATUS reporting) -> replace (granted status and decisions)
      setSingleDecision( decision );
    }
  }

  private void addDecision( @NonNull IAuthorizationDecision decision ) {
    assert resultDecision != null;
    // Preserve granted status.
    addDecision( resultDecision.isGranted(), decision );
  }

  private void addDecision( boolean granted, @NonNull IAuthorizationDecision decision ) {
    assert state != CompositeResultState.EMPTY;
    assert resultDecision != null;

    // Single: compose with new decision.
    // Multiple: update granted status JIC, and add new decision to existing composite decision.

    if ( state == CompositeResultState.SINGLE ) {
      // Upgrade to mutable decision / decision builder.
      setMultipleDecision( new CompositeDecisionBuilder()
        .granted( granted )
        .withDecision( resultDecision )
        .withDecision( decision ) );
    } else {
      assert state == CompositeResultState.MULTIPLE;
      // Update existing mutable decision / decision builder.
      ( (CompositeDecisionBuilder) resultDecision )
        .granted( granted )
        .withDecision( decision );
    }
  }

  @NonNull
  public Optional<IAuthorizationDecision> build( @NonNull IAuthorizationRequest request ) {
    switch ( state ) {
      case EMPTY:
        // No decisions, return empty.
        return Optional.empty();

      case SINGLE:
        // Single decision, return it.
        assert resultDecision != null;
        return Optional.of( resultDecision );

      case MULTIPLE:
        // Multiple decisions, create composite decision.
        assert resultDecision != null;
        Set<IAuthorizationDecision> decisions = ( (CompositeDecisionBuilder) resultDecision ).getDecisions();
        return Optional.of( createDecision( request, resultDecision.isGranted(), decisions ) );

      default:
        throw new IllegalStateException( "Unexpected state: " + state );
    }
  }

  @NonNull
  protected abstract ICompositeAuthorizationDecision createDecision(
    @NonNull IAuthorizationRequest request,
    boolean isGranted,
    @NonNull Set<IAuthorizationDecision> decisions );

  protected abstract boolean getSettledGrantedStatus();

  private static class CompositeDecisionBuilder implements ICompositeAuthorizationDecision {
    private boolean isGranted;

    @NonNull
    private final Set<IAuthorizationDecision> decisions = new LinkedHashSet<>();

    @NonNull
    @Override
    public IAuthorizationRequest getRequest() {
      // Not really needed for internal use.
      throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public String getShortJustification() {
      // Not really needed for internal use.
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isGranted() {
      return isGranted;
    }

    public CompositeDecisionBuilder granted( boolean granted ) {
      isGranted = granted;
      return this;
    }

    @NonNull
    @Override
    public Set<IAuthorizationDecision> getDecisions() {
      return decisions;
    }

    public CompositeDecisionBuilder withDecision( @NonNull IAuthorizationDecision decision ) {
      decisions.add( decision );
      return this;
    }
  }
}
