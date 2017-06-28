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

import org.junit.Assert;
import org.junit.Test;

public class WorkItemLifecyclePhaseTest {

  @Test
  public void testLifecycleNames() {
    Assert.assertEquals( "Submitted", WorkItemLifecyclePhase.SUBMITTED.getName() );
    Assert.assertEquals( "Dispatched", WorkItemLifecyclePhase.DISPATCHED.getName() );
    Assert.assertEquals( "Received", WorkItemLifecyclePhase.RECEIVED.getName() );
    Assert.assertEquals( "Rejected", WorkItemLifecyclePhase.REJECTED.getName() );
    Assert.assertEquals( "In progress", WorkItemLifecyclePhase.IN_PROGRESS.getName() );
    Assert.assertEquals( "Succeeded", WorkItemLifecyclePhase.SUCCEEDED.getName() );
    Assert.assertEquals( "Failed", WorkItemLifecyclePhase.FAILED.getName() );
    Assert.assertEquals( "Restarted", WorkItemLifecyclePhase.RESTARTED.getName() );

  }

  @Test
  public void testLifecycleDescriptions() {
    Assert.assertEquals( "The work item has been submitted to the component responsible for its execution",
      WorkItemLifecyclePhase.SUBMITTED.getDescription() );
    Assert
      .assertEquals( "The work item has been dispatched to the component responsible for its execution",
        WorkItemLifecyclePhase.DISPATCHED.getDescription() );
    Assert.assertEquals( "The work item has been received by the component responsible for its execution",
      WorkItemLifecyclePhase.RECEIVED.getDescription() );
    Assert.assertEquals( "The work item execution has been rejected",
      WorkItemLifecyclePhase.REJECTED.getDescription() );
    Assert
      .assertEquals( "The work item execution is in progress",
        WorkItemLifecyclePhase.IN_PROGRESS.getDescription() );
    Assert.assertEquals( "The work item execution has succeeded",
      WorkItemLifecyclePhase.SUCCEEDED.getDescription() );
    Assert.assertEquals( "The work item execution has failed",
      WorkItemLifecyclePhase.FAILED.getDescription() );
    Assert.assertEquals( "The work item execution has been restarted",
      WorkItemLifecyclePhase.RESTARTED.getDescription() );
  }


  @Test
  public void testGet() {

    Assert.assertNull( WorkItemLifecyclePhase.get( "foo" ) );
    Assert.assertNull( WorkItemLifecyclePhase.get( "" ) );
    Assert.assertNull( WorkItemLifecyclePhase.get( null ) );

    Assert.assertEquals( WorkItemLifecyclePhase.FAILED, WorkItemLifecyclePhase.get( WorkItemLifecyclePhase
      .getMessageBundle().getString( "LifecyclePhase.FAILED" ) ) );
    Assert.assertEquals( WorkItemLifecyclePhase.SUBMITTED, WorkItemLifecyclePhase.get( WorkItemLifecyclePhase
      .getMessageBundle().getString( "LifecyclePhase.SUBMITTED" ) ) );
    Assert.assertEquals( WorkItemLifecyclePhase.DISPATCHED, WorkItemLifecyclePhase.get( WorkItemLifecyclePhase
      .getMessageBundle().getString( "LifecyclePhase.DISPATCHED" ) ) );
  }
}
