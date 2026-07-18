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



package org.pentaho.platform.plugin.services.importexport;

import org.pentaho.platform.api.engine.IAuthorizationPolicy;

import java.util.List;

public class TestAuthorizationPolicy implements IAuthorizationPolicy {
  @Override
  public boolean isAllowed( String actionName ) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public List<String> getAllowedActions( String actionNamespace ) {
    // TODO Auto-generated method stub
    return null;
  }
}
