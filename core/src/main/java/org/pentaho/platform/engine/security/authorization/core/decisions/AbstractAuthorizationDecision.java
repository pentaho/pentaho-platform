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

import java.util.Objects;

public abstract class AbstractAuthorizationDecision implements IAuthorizationDecision {

  @NonNull
  private final IAuthorizationRequest request;
  private final boolean granted;

  protected AbstractAuthorizationDecision( @NonNull IAuthorizationRequest request, boolean granted ) {
    this.request = Objects.requireNonNull( request );
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
