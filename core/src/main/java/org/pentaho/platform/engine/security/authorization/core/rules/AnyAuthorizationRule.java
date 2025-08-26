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
 * The {@code OrAuthorizationRulesManager} class is an authorization rules manager which combines the results of a list
 * of authorization rules using a logical "OR" operation.
 */
public class AnyAuthorizationRule extends AbstractCompositeAuthorizationRule {

  public AnyAuthorizationRule( @NonNull List<IAuthorizationRule<IAuthorizationRequest>> rules ) {
    super( rules );
  }

  @NonNull
  @Override
  protected AbstractCompositeResultBuilder createResultBuilder( @NonNull IAuthorizationContext context ) {
    return new AnyResultBuilder( context.getOptions().getDecisionReportingMode() );
  }
}
