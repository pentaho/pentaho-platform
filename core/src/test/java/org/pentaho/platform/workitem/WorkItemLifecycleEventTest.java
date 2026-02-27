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


package org.pentaho.platform.workitem;

import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

public class WorkItemLifecycleEventTest {

  private String workItemUid = "foo";
  private String workItemDetails = "foe";
  private WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String lifecycleDetails = "foe";
  private Date currentTimeStamp = new Date();

  @Test
  public void testValidInstantiation() {
    final WorkItemLifecycleEvent workItemLifecycleRecord = new WorkItemLifecycleEvent( workItemUid,
      workItemDetails, lifecyclePhase, lifecycleDetails, currentTimeStamp );

    Assert.assertEquals( workItemUid, workItemLifecycleRecord.getWorkItemUid() );
    Assert.assertEquals( workItemDetails, workItemLifecycleRecord.getWorkItemDetails() );
    Assert.assertEquals( lifecyclePhase, workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    Assert.assertEquals( lifecycleDetails, workItemLifecycleRecord.getLifecycleDetails() );
    Assert.assertEquals( currentTimeStamp, workItemLifecycleRecord.getSourceTimestamp() );

    // source host name and ip should be set within the constructor
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }

  @Test
  public void testNulls() {

    final WorkItemLifecycleEvent workItemLifecycleRecord = new WorkItemLifecycleEvent( null, null, null, null,
      currentTimeStamp );
    Assert.assertNull( workItemLifecycleRecord.getWorkItemDetails() );
    Assert.assertNull( workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    Assert.assertNull( workItemLifecycleRecord.getLifecycleDetails() );

    // workItemUid should be generated, if one is not provided in the constructor
    Assert.assertNotNull( workItemLifecycleRecord.getWorkItemUid() );
    // if null sourceTimestamp is passed, it is initialized when the WorkItemLifecycleRecord is initialized
    Assert.assertNotNull( workItemLifecycleRecord.getSourceTimestamp() );
    // source host name and ip should be set within the constructor
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    Assert.assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }
}
