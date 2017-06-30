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

import org.junit.Test;
import org.pentaho.platform.util.ActionUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

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

    assertEquals( workItemUid, workItemLifecycleRecord.getWorkItemUid() );
    assertEquals( workItemDetails, workItemLifecycleRecord.getWorkItemDetails() );
    assertEquals( lifecyclePhase, workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    assertEquals( lifecycleDetails, workItemLifecycleRecord.getLifecycleDetails() );
    assertEquals( currentTimeStamp, workItemLifecycleRecord.getSourceTimestamp() );

    // source host name and ip should be set within the constructor
    assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }

  @Test
  public void testNulls() {

    final WorkItemLifecycleEvent workItemLifecycleRecord = new WorkItemLifecycleEvent(
      null, null, null, null, currentTimeStamp );
    assertNull( workItemLifecycleRecord.getWorkItemDetails() );
    assertNull( workItemLifecycleRecord.getWorkItemLifecyclePhase() );
    assertNull( workItemLifecycleRecord.getLifecycleDetails() );

    // workItemUid should be generated, if one is not provided in the constructor
    assertNotNull( workItemLifecycleRecord.getWorkItemUid() );
    // if null sourceTimestamp is passed, it is initialized when the WorkItemLifecycleRecord is initialized
    assertNotNull( workItemLifecycleRecord.getSourceTimestamp() );
    // source host name and ip should be set within the constructor
    assertNotNull( workItemLifecycleRecord.getSourceHostName() );
    assertNotNull( workItemLifecycleRecord.getSourceHostIp() );
  }

  @Test
  public void testFetchUid() {
    assertNotNull( WorkItemLifecycleEvent.fetchUid( null ) );

    final Map map = new HashMap<>();
    assertNotNull( WorkItemLifecycleEvent.fetchUid( map ) );
    // the map should now contain a uid
    final String uid = (String) map.get( ActionUtil.WORK_ITEM_UID );
    assertNotNull( uid );

    // calling getUidFromMap on a map that contains the uid, should return in the uid being returned
    assertEquals( uid, WorkItemLifecycleEvent.fetchUid( map ) );
    // the map should still only have one element
    assertEquals( 1, map.size() );

    // set uid within map to null and verify that a new value is generated
    map.put( ActionUtil.WORK_ITEM_UID, null );
    assertNotNull( WorkItemLifecycleEvent.fetchUid( map ) );
    // the map should still only have one element
    assertEquals( 1, map.size() );

    // set uid within map to empty string and verify that a new value is generated
    map.put( ActionUtil.WORK_ITEM_UID, "" );
    assertNotNull( WorkItemLifecycleEvent.fetchUid( map ) );
    // the map should still only have one element
    assertEquals( 1, map.size() );
  }
}
