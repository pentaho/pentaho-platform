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

/**
 * The {@code DefaultAuthorizationDecision} class represents a default authorization decision, granted or denied without
 * additional information.
 */
public class DefaultAuthorizationDecision extends AbstractAuthorizationDecision {

  public DefaultAuthorizationDecision( @NonNull IAuthorizationRequest request,
                                       boolean granted ) {
    super( request, granted );
  }
}
