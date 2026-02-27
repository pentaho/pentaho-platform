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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@RunWith( MockitoJUnitRunner.class )
public class WorkItemLifecycleEventUtilTest {

  private final WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;

  @Test
  public void testPublisher() {

    IWorkItemLifecycleEventPublisher publisherMock = Mockito.spy( new DummyPublisher() );
    try ( MockedStatic<PentahoSystem> pentahoSystem = mockStatic( PentahoSystem.class ) ) {

      pentahoSystem.when( () -> PentahoSystem.get( eq( IWorkItemLifecycleEventPublisher.class ) ) ).thenReturn( publisherMock );
      String workItemUid = "foo";
      String workItemDetails = "foe";
      String lifecycleDetails = "foe";
      WorkItemLifecycleEvent workItemLifecycleEventMock = Mockito.spy( new WorkItemLifecycleEvent( workItemUid, workItemDetails,
        lifecyclePhase, lifecycleDetails, null ) );

      WorkItemLifecycleEventUtil.publish( workItemLifecycleEventMock );
      // verify that the publishEvent method is called as expected
      Mockito.verify( publisherMock, Mockito.times( 1 ) ).publish( workItemLifecycleEventMock );
    }
  }
}
