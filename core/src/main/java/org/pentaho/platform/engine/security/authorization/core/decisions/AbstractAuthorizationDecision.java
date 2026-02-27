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

  protected static final String DENIED_JUSTIFICATION_PREFIX =
    Messages.getInstance().getString( "AbstractAuthorizationDecision.DENIED_JUSTIFICATION_PREFIX" );

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
    return isGranted() ? getShortJustificationGranted() : getShortJustificationDenied();
  }

  /**
   * Gets a short justification string for a granted decision.
   * <p>
   * Subclasses should override this method to provide a meaningful justification.
   * Most subclasses only need to override this method, as {@link #getShortJustificationDenied()}
   * provides a default implementation that prefixes the granted justification with "_Not_".
   *
   * @return The short justification string for a granted decision.
   */
  @NonNull
  protected String getShortJustificationGranted() {
    return "";
  }

  /**
   * Gets a short justification string for a denied decision.
   * <p>
   * The default implementation returns "_Not_ " + {@link #getShortJustificationGranted()},
   * which composes well with opposition. Subclasses can override for custom phrasing.
   *
   * @return The short justification string for a denied decision.
   */
  @NonNull
  protected String getShortJustificationDenied() {
    String grantedJustification = getShortJustificationGranted();
    return grantedJustification.isEmpty() ? "" : DENIED_JUSTIFICATION_PREFIX + grantedJustification;
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
