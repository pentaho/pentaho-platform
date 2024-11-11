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


package org.pentaho.platform.action;

import org.junit.Assert;
import org.junit.Test;

public class ActionInvokeStatusTest {

  @Test
  public void setAndGetRequiresUpdateTest() throws Exception {
    ActionInvokeStatus actionInvokeStatus = new ActionInvokeStatus();
    actionInvokeStatus.setRequiresUpdate( false );
    Assert.assertFalse( actionInvokeStatus.requiresUpdate() );
    actionInvokeStatus.setRequiresUpdate( true );
    Assert.assertTrue( actionInvokeStatus.requiresUpdate() );
  }

  @Test
  public void setAndGetThrowableTest() throws Exception {
    ActionInvokeStatus actionInvokeStatus = new ActionInvokeStatus();
    actionInvokeStatus.setThrowable( new Throwable( "test_message" ) );
    Assert.assertEquals( actionInvokeStatus.getThrowable().getMessage(), "test_message" );
  }

  @Test
  public void setAndGetExecutionStatusTest() throws Exception {
    ActionInvokeStatus actionInvokeStatus = new ActionInvokeStatus();
    actionInvokeStatus.setExecutionStatus( true );
    Assert.assertTrue( actionInvokeStatus.isExecutionSuccessful() );
    actionInvokeStatus.setExecutionStatus( false );
    Assert.assertFalse( actionInvokeStatus.isExecutionSuccessful() );
  }
}
