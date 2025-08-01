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
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationContext;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule;

import java.util.List;

/**
 * The {@code AllAuthorizationRule} class is an authorization rule which combines the decisions of several authorization
 * rules using a logical "AND" operation. Abstention results are ignored. If all rules abstain, then the default
 * decision is denied.
 */
public class AllAuthorizationRule extends AbstractCompositeAuthorizationRule {

  public AllAuthorizationRule( @NonNull List<IAuthorizationRule<IAuthorizationRequest>> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  protected AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context ) {
    return new AllResultBuilder( context.getOptions().getDecisionReportingMode() );
  }
}
