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

public class SchedulerExecuteActionTest {
  @Test
  public void testGetName() {
    SchedulerExecuteAction action = new SchedulerExecuteAction();
    Assert.assertEquals( "org.pentaho.scheduler.execute", action.getName() );
  }
}
