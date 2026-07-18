/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.platform.engine.security.authorization.core.decisions;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pentaho.platform.api.engine.security.authorization.resources.IResourceAuthorizationRequest;

public class SupportedActionResourceTypeAuthorizationDecision extends AbstractAuthorizationDecision {

  public SupportedActionResourceTypeAuthorizationDecision( @NonNull IResourceAuthorizationRequest request,
                                                           boolean granted ) {
    super( request, granted );
  }

  @Override
  public String toString() {
    // Example: "SupportedActionResourceTypeAuthorizationDecision[Denied action='read' resourceType='file']"

    var resourceRequest = (IResourceAuthorizationRequest) getRequest();

    return String.format(
      "%s[%s action='%s' resourceType='%s']",
      getClass().getSimpleName(),
      getGrantedLogText(),
      resourceRequest.getAction(),
      resourceRequest.getResource().getType() );
  }
}
