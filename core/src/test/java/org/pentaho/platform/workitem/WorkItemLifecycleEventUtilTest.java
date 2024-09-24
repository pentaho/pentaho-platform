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
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

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
