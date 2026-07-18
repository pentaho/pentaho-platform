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

import org.junit.Assert;
import org.junit.Test;

public class RepositoryCreateActionTest {
  @Test
  public void testGetName() {
    RepositoryCreateAction action = new RepositoryCreateAction();
    Assert.assertEquals( "org.pentaho.repository.create", action.getName() );
  }
}
