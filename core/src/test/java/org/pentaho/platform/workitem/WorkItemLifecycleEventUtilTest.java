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

package org.pentaho.platform.workitem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.pentaho.platform.api.workitem.IWorkItemLifecycleEventPublisher;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.isA;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith( PowerMockRunner.class )
@PrepareForTest( { PentahoSystem.class } )
public class WorkItemLifecycleEventUtilTest {

  private IWorkItemLifecycleEventPublisher publisherMock = null;
  private WorkItemLifecycleEvent workItemLifecycleEventMock = null;
  private String workItemUid = "foo";
  private String workItemDetails = "foe";
  private WorkItemLifecyclePhase lifecyclePhase = WorkItemLifecyclePhase.DISPATCHED;
  private String lifecycleDetails = "foe";

  @Before
  public void setup() throws Exception {
    PowerMockito.mockStatic( PentahoSystem.class );
    publisherMock = Mockito.spy( new DummyPublisher() );
    when( PentahoSystem.get( isA( IWorkItemLifecycleEventPublisher.class.getClass() ) ) ).thenReturn( publisherMock );

    workItemLifecycleEventMock = Mockito.spy( new WorkItemLifecycleEvent( workItemUid, workItemDetails,
      lifecyclePhase, lifecycleDetails, null ) );
  }

  @Test
  public void testPublisher() throws InterruptedException {
    WorkItemLifecycleEventUtil.publish( workItemLifecycleEventMock );
    // verify that the publishEvent method is called as expected
    Mockito.verify( publisherMock, Mockito.times( 1 ) ).publish( workItemLifecycleEventMock );
  }
}
