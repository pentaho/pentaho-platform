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

package org.pentaho.platform.engine.security.authorization.authng.rules;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.authng.AuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.authng.IAuthorizationRule;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.IAuthorizationDecision;
import org.pentaho.platform.api.engine.security.authorization.authng.decisions.ICompositeAuthorizationDecision;

import java.util.List;
import java.util.Set;

/**
 * The {@code OrAuthorizationRulesManager} class is an authorization rules manager which combines the results of a list
 * of authorization rules using a logical "OR" operation.
 */
public class AnyAuthorizationRule extends AbstractCompositeAuthorizationRule {

  public AnyAuthorizationRule( @NonNull List<IAuthorizationRule> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  protected AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context ) {

    return new AbstractCompositeResultBuilder( context.getOptions().getDecisionReportingMode() ) {
      @NonNull
      @Override
      protected ICompositeAuthorizationDecision createDecision( @NonNull AuthorizationRequest request,
                                                                boolean isGranted,
                                                                @NonNull Set<IAuthorizationDecision> decisions ) {
        return getDecisionFactory().anyOf( request, isGranted, decisions );
      }

      @Override
      protected boolean getSettledGrantedStatus() {
        // Granted
        return true;
      }
    };
  }
}
