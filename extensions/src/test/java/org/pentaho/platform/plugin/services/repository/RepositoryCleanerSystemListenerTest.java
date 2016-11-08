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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.plugin.services.repository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.scheduler2.CronJobTrigger;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.plugin.services.repository.RepositoryCleanerSystemListener.Frequency;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class RepositoryCleanerSystemListenerTest {

  private MicroPlatform mp;
  private IScheduler scheduler;
  private RepositoryCleanerSystemListener listener;

  @Before
  public void setUp() throws Exception {
    scheduler = mock( IScheduler.class );
    listener = new RepositoryCleanerSystemListener();
  }

  @After
  public void tearDown() throws Exception {
    if ( mp != null ) {
      mp.stop();
      mp = null;
    }
    scheduler = null;
    listener = null;
  }


  @Test
  public void gcEnabledIsTrue_executeIsNull_ByDefault() {
    assertTrue( listener.isGcEnabled() );
    assertNull( listener.getExecute() );
  }

  @Test
  public void stops_IfSchedulerIsNotDefined() {
    assertFalse( listener.startup( null ) );
  }


  private void prepareMp() throws Exception {
    mp = new MicroPlatform();
    mp.defineInstance( IScheduler.class, scheduler );
    mp.start();
  }

  private void verifyJobRemoved( String jobId ) throws SchedulerException {
    verify( scheduler ).removeJob( jobId );
  }

  private void verifyJobCreated( Frequency frequency ) throws SchedulerException {
    verify( scheduler ).createJob( eq( RepositoryGcJob.JOB_NAME ), eq( RepositoryGcJob.class ), anyMap(),
      isA( frequency.createTrigger().getClass() ) );
  }


  private void verifyJobHaveNotCreated() throws SchedulerException {
    verify( scheduler, never() )
      .createJob( eq( RepositoryGcJob.JOB_NAME ), eq( RepositoryGcJob.class ), anyMap(), any( IJobTrigger.class ) );
  }


  @Test
  public void returnsTrue_EvenGetsExceptions() throws Exception {
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenThrow( new SchedulerException( "test exception" ) );
    prepareMp();
    assertTrue( "The listener should not return false to let the system continue working", listener.startup( null ) );
  }


  @Test
  public void removesJobs_WhenDisabled() throws Exception {
    final String jobId = "jobId";
    Job job = new Job();
    job.setJobId( jobId );
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenReturn( Collections.singletonList( job ) );

    prepareMp();

    listener.setGcEnabled( false );

    assertTrue( listener.startup( null ) );
    verifyJobRemoved( jobId );
  }

  @Test
  public void schedulesJob_Now() throws Exception {
    testSchedulesJob( Frequency.NOW );
  }

  @Test
  public void schedulesJob_Weekly() throws Exception {
    testSchedulesJob( Frequency.WEEKLY );
  }

  @Test
  public void schedulesJob_Monthly() throws Exception {
    testSchedulesJob( Frequency.MONTHLY );
  }


  private void testSchedulesJob( Frequency frequency ) throws Exception {
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenReturn( Collections.<Job>emptyList() );
    prepareMp();
    listener.setExecute( frequency.getValue() );

    assertTrue( listener.startup( null ) );
    verifyJobCreated( frequency );
  }

  @Test
  public void schedulesJob_Unknown() throws Exception {
    testSchedulesJob_IncorrectExecute( "unknown" );
  }

  @Test
  public void schedulesJob_Null() throws Exception {
    testSchedulesJob_IncorrectExecute( null );
  }

  private void testSchedulesJob_IncorrectExecute( String execute ) throws Exception {
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenReturn( Collections.<Job>emptyList() );
    prepareMp();
    listener.setExecute( execute );

    listener.startup( null );
    verifyJobHaveNotCreated();
  }


  @Test
  public void reschedulesJob_IfFoundDifferent() throws Exception {
    final String oldJobId = "oldJobId";
    Job oldJob = new Job();
    oldJob.setJobTrigger( new CronJobTrigger() );
    oldJob.setJobId( oldJobId );
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenReturn( Collections.singletonList( oldJob ) );

    prepareMp();

    listener.setExecute( Frequency.NOW.getValue() );

    assertTrue( listener.startup( null ) );
    verifyJobRemoved( oldJobId );
    verifyJobCreated( Frequency.NOW );
  }

  @Test
  public void doesNotRescheduleJob_IfFoundSame() throws Exception {
    final String oldJobId = "oldJobId";
    Job oldJob = new Job();
    oldJob.setJobTrigger( Frequency.WEEKLY.createTrigger() );
    oldJob.setJobId( oldJobId );
    when( scheduler.getJobs( any( IJobFilter.class ) ) ).thenReturn( Collections.singletonList( oldJob ) );

    prepareMp();

    listener.setExecute( Frequency.WEEKLY.getValue() );

    assertTrue( listener.startup( null ) );
    verify( scheduler, never() ).removeJob( oldJobId );
    verifyJobHaveNotCreated();
  }
}
