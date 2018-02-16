/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

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
