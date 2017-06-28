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
 * Copyright (c) 2017 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.workitem;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WorkItemLifecycleEventFileWriterTest {

  private WorkItemLifecycleEventFileWriter listener = null;

  @Before
  public void setup() {
    listener = new WorkItemLifecycleEventFileWriter();
  }

  @After
  public void teardown() {
    listener = null;
  }

  @Test
  public void testResources() {
    Assert.assertEquals(
      "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE - Work item lifecycle information is missing, cannot publish", listener
        .getMessageBundle().getErrorString( "ERROR_0001_MISSING_WORK_ITEM_LIFECYCLE" ) );
  }

  // TODO: finish
}
