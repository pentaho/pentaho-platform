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

package org.pentaho.platform.security.policy.rolebased.actions;

import org.junit.Assert;
import org.junit.Test;

public class AdministerSecurityActionTest {
  @Test
  public void testGetName() {
    AdministerSecurityAction action = new AdministerSecurityAction();
    Assert.assertEquals( "org.pentaho.security.administerSecurity", action.getName() );
  }
}
