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
 * The {@code IAllAuthorizationDecision} interface represents a decision for an authorization request determined from
 * the <i>conjunction</i> of the contained decisions for the same authorization request.
 * <p>
 * The composite decision is granted if all contained decisions are granted. Otherwise, if there are no contained
 * decisions, or if at least one of the contained decisions is denied, the composite decision is denied.
 */
public interface IAllAuthorizationDecision extends ICompositeAuthorizationDecision {
}
