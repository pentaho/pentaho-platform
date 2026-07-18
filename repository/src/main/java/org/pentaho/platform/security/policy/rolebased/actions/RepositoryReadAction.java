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


package org.pentaho.platform.security.policy.rolebased.actions;

import edu.umd.cs.findbugs.annotations.NonNull;

public class RepositoryReadAction extends AbstractLocalizedAuthorizationAction {
  public static final String NAME = "org.pentaho.repository.read";

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }
}
