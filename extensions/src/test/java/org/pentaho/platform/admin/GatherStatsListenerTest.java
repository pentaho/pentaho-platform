/*!
 *
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
 *
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.admin;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.IJob;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rfellows on 10/19/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class GatherStatsListenerTest {

  GatherStatsListener gatherStatsListener;

  @Mock IPentahoSession session;
  @Mock IScheduler scheduler;

  @Before
  public void setUp() throws Exception {
    gatherStatsListener = new GatherStatsListener();
    gatherStatsListener.setIntervalInSeconds( 101 );
    gatherStatsListener.setTransFileName( "MyTransform.ktr" );
  }

  @Test
  public void testStartUp() throws Exception {
    PentahoSystem.registerObject( scheduler );
    IJobTrigger trigger = scheduler.createSimpleJobTrigger( new Date(), null, -1, 101L );
    IJob job = mock( IJob.class );
    when( scheduler.createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      eq( trigger ) ) ).thenReturn( job );

    boolean startup = gatherStatsListener.startup( session );
    assertTrue( startup );
    assertEquals( 1, gatherStatsListener.jobMap.size() );
    assertEquals( gatherStatsListener.getTransFileName(), gatherStatsListener.jobMap.get( "transFileName" ) );
    assertEquals( 101, gatherStatsListener.getIntervalInSeconds() );
    verify( scheduler ).createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      eq( trigger ) );
  }

  @Test
  public void testShutdown() {
    // for the code coverage game, this method does nothing
    gatherStatsListener.shutdown();
  }

  @Test
  public void testStartup_withExceptionFromScheduleJobCall() throws Exception {
    PentahoSystem.registerObject( scheduler );
    IJobTrigger trigger = scheduler.createSimpleJobTrigger( new Date(), null, -1, 101L );
    when( scheduler.createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      eq( trigger ) ) ).thenThrow( new SchedulerException( "error" ) );

    boolean startup = gatherStatsListener.startup( session );
    assertTrue( startup );
    assertEquals( 1, gatherStatsListener.jobMap.size() );
    assertEquals( gatherStatsListener.getTransFileName(), gatherStatsListener.jobMap.get( "transFileName" ) );
    assertEquals( 101, gatherStatsListener.getIntervalInSeconds() );
    verify( scheduler ).createJob(
      eq( "Gather Stats" ),
      eq( GatherStatsAction.class ),
      eq( gatherStatsListener.jobMap ),
      eq( trigger ) );
  }

  @After
  public void cleanup() {
    PentahoSystem.clearObjectFactory();
  }
}
