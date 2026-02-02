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

package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest;
import org.pentaho.platform.api.engine.security.authorization.decisions.IAuthorizationDecision;
import org.pentaho.platform.engine.security.messages.Messages;
import org.springframework.util.Assert;

public abstract class AbstractAuthorizationDecision implements IAuthorizationDecision {

  protected static final String LIST_SEPARATOR =
    Messages.getInstance().getString( "AbstractAuthorizationDecision.LIST_SEPARATOR" );

  @NonNull
  private final IAuthorizationRequest request;
  private final boolean granted;

  protected AbstractAuthorizationDecision( @NonNull IAuthorizationRequest request, boolean granted ) {
    Assert.notNull( request, "Argument 'request' is required" );

    this.request = request;
    this.granted = granted;
  }

  @NonNull
  @Override
  public IAuthorizationRequest getRequest() {
    return request;
  }

  @Override
  public boolean isGranted() {
    return granted;
  }

  @NonNull
  @Override
  public String getShortJustification() {
    return getShortJustification( isGranted() );
  }

  @NonNull
  @Override
  public String getOpposedShortJustification() {
    return getShortJustification( isDenied() );
  }

  /**
   * Gets a short justification string based on whether the decision is granted or denied.
   * @param granted {@code true}, to get the justification for a granted decision; {@code false}, for a denied decision.
   * @return The short justification string.
   */
  @NonNull
  protected String getShortJustification( boolean granted ) {
    return "";
  }

  @Override
  public String toString() {
    // Example: "SomeAuthorizationDecision[Granted]"
    return String.format( "%s[%s]", getClass().getSimpleName(), getGrantedLogText() );
  }

  @NonNull
  protected String getGrantedLogText() {
    return isGranted() ? "Granted" : "Denied";
  }
}
