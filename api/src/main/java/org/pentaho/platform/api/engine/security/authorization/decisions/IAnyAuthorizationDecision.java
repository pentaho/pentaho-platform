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

package org.pentaho.platform.api.engine.security.authorization.decisions;

/**
 * The {@code IAnyAuthorizationDecision} interface represents a decision for an authorization request determined from
 * the <i>disjunction</i> of the contained decisions for the same authorization request.
 * <p>
 * The composite decision is granted if at least one of the contained decisions is granted. Otherwise, if there are no
 * contained decisions, or if all contained decisions are denied, the composite decision is denied.
 */
public interface IAnyAuthorizationDecision extends ICompositeAuthorizationDecision {
}
