/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2017 Hitachi Vantara..  All rights reserved.
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
}
