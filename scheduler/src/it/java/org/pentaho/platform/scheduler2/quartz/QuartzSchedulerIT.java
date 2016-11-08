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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.ISchedulerListener;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@SuppressWarnings( "nls" )
public class QuartzSchedulerIT {
  private static final String CRON_EXPRESSION = "1 0 0 * * ? *";
  private static final String USER_NAME = "userName";
  private static final String JOB_NAME = "jobName";
  private static final String JOB_ID = USER_NAME + "\t" + JOB_NAME + "\t" + System.currentTimeMillis();

  private QuartzScheduler scheduler;
  private Scheduler quartzScheduler;
  private final Map<String, Serializable> jobDetails = new HashMap<>();

  @Before
  public void init() throws SchedulerException, PlatformInitializationException, org.quartz.SchedulerException {
    final SchedulerFactory schedulerFactory = mock( SchedulerFactory.class );
    quartzScheduler = mock( Scheduler.class );
    when( schedulerFactory.getScheduler() ).thenReturn( quartzScheduler );
    scheduler = spy( new QuartzScheduler( schedulerFactory ) );
    when( scheduler.getCurrentUser() ).thenReturn( USER_NAME );

    jobDetails.clear();
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "RESERVEDMAPKEY_ACTIONCLASS" );
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "RESERVEDMAPKEY_STREAMPROVIDER" );
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM, "RESERVEDMAPKEY_UIPASSPARAM" );
  }

  @Test
  public void getQuartzSchedulerTest() throws Exception {
    assertEquals( quartzScheduler, scheduler.getQuartzScheduler() );
  }

  @Test
  public void createJobTest() throws Exception {
    String actionId = "actionId";
    ComplexJobTrigger trigger = getComplexJobTrigger();
    IBackgroundExecutionStreamProvider outputStreamProvider = mock( IBackgroundExecutionStreamProvider.class );
    final Job job = scheduler.createJob( JOB_NAME, actionId, null, trigger, outputStreamProvider );

    assertNotNull( job );
    assertEquals( Job.JobState.NORMAL, job.getState() );

    assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) );
    assertEquals( actionId, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) );
    assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) );
    assertEquals( outputStreamProvider, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) );
    assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    assertNotNull( job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) );
    assertEquals( USER_NAME, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) );
  }

  @Test
  public void createJobTest_ForUser() throws Exception {
    String actionId = "actionId";
    ComplexJobTrigger trigger = getComplexJobTrigger();
    IBackgroundExecutionStreamProvider outputStreamProvider = mock( IBackgroundExecutionStreamProvider.class );
    Map<String, Serializable> paramMap = new HashMap<>();
    paramMap.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, "ninja" );
    final Job job = scheduler.createJob( JOB_NAME, paramMap, trigger, outputStreamProvider );

    assertNotNull( job );
    assertEquals( "ninja", job.getUserName() );
    assertEquals( Job.JobState.NORMAL, job.getState() );
  }

  @Test
  public void createQuartzTriggerComplexTriggerTest() throws Exception {
    final Trigger quartzTrigger = QuartzScheduler.createQuartzTrigger( getComplexJobTrigger(), getJobKey() );

    assertNotNull( quartzTrigger );
    assertTrue( quartzTrigger instanceof CronTrigger );
    assertEquals( SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, quartzTrigger.getMisfireInstruction() );
    assertEquals( CRON_EXPRESSION, ( (CronTrigger) quartzTrigger ).getCronExpression() );
    assertEquals( USER_NAME, quartzTrigger.getGroup() );

  }

  @Test
  public void createQuartzTriggerSimpleTriggerTest() throws Exception {
    final Calendar calendar = Calendar.getInstance();
    Date startTime = calendar.getTime();
    calendar.add( Calendar.MONTH, 1 );
    Date endTime = calendar.getTime();
    int repeatCount = 5;
    long repeatIntervalSeconds = 10;
    final SimpleJobTrigger simpleJobTrigger = new SimpleJobTrigger( startTime, endTime, repeatCount,
        repeatIntervalSeconds );
    final Trigger quartzTrigger = QuartzScheduler.createQuartzTrigger( simpleJobTrigger, getJobKey() );

    assertNotNull( quartzTrigger );
    assertTrue( quartzTrigger instanceof SimpleTrigger );
    assertEquals( SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT,
        quartzTrigger.getMisfireInstruction() );
    assertEquals( USER_NAME, quartzTrigger.getGroup() );

    SimpleTrigger simpleTrigger = (SimpleTrigger) quartzTrigger;
    assertEquals( startTime, simpleTrigger.getStartTime() );
    assertEquals( endTime, simpleTrigger.getEndTime() );
    assertEquals( repeatCount, simpleTrigger.getRepeatCount() );
    assertEquals( repeatIntervalSeconds * 1000, simpleTrigger.getRepeatInterval() );
  }

  @Test( expected = SchedulerException.class )
  public void createQuartzTriggerNotDefinedTriggerTest() throws Exception {
    final IJobTrigger trigger = mock( IJobTrigger.class );
    QuartzScheduler.createQuartzTrigger( trigger, getJobKey() );
  }

  @Test
  public void updateJobTest() throws Exception {
    final JobDetail jobDetail = setJobDataMap( USER_NAME );

    scheduler.updateJob( JOB_ID, new HashMap<String, Serializable>(), getComplexJobTrigger() );

    verify( quartzScheduler, times( 1 ) ).addJob( eq( jobDetail ), eq( true ) );
    verify( quartzScheduler, times( 1 ) ).rescheduleJob( eq( JOB_ID ), eq( USER_NAME ), any( Trigger.class ) );
  }

  @Test
  public void triggerNowTest() throws Exception {
    final SimpleTrigger simpleTrigger = mock( SimpleTrigger.class );
    final CronTrigger cronTrigger = mock( CronTrigger.class );
    when( quartzScheduler.getTriggersOfJob( eq( JOB_ID ), eq( USER_NAME ) ) ).thenReturn( new Trigger[] { simpleTrigger,
      cronTrigger
    } );

    scheduler.triggerNow( JOB_ID );

    verify( simpleTrigger, times( 1 ) ).setStartTime( any( Date.class ) );
    verify( cronTrigger, times( 1 ) ).setStartTime( any( Date.class ) );
    verify( quartzScheduler, times( 1 ) ).rescheduleJob( eq( JOB_ID ), eq( USER_NAME ), eq( simpleTrigger ) );
    verify( quartzScheduler, times( 1 ) ).rescheduleJob( eq( JOB_ID ), eq( USER_NAME ), eq( cronTrigger ) );
    verify( quartzScheduler, times( 1 ) ).triggerJob( eq( JOB_ID ), eq( USER_NAME ) );
  }

  @Test
  public void getJobTest() throws Exception {
    final CronTrigger cronTrigger = mock( CronTrigger.class );
    when( cronTrigger.getCronExpression() ).thenReturn( CRON_EXPRESSION );
    when( quartzScheduler.getTriggersOfJob( eq( JOB_ID ), eq( USER_NAME ) ) ).thenReturn( new Trigger[] { cronTrigger } );
    setJobDataMap( USER_NAME );

    final Job job = scheduler.getJob( JOB_ID );

    assertEquals( JOB_ID, job.getJobId() );
    assertEquals( jobDetails, job.getJobParams() );
    assertEquals( USER_NAME, job.getUserName() );
    assertEquals( JOB_NAME, job.getJobName() );
    assertEquals( Job.JobState.NORMAL, job.getState() );
  }

  @Test
  public void getJobsTest() throws Exception {
    final IJobFilter jobFilter = mock( IJobFilter.class );
    when( jobFilter.accept( any( Job.class ) ) ).thenReturn( true );

    final String groupName = "groupName";
    when( quartzScheduler.getJobGroupNames() ).thenReturn( new String[] { groupName } );
    when( quartzScheduler.getJobNames( eq( groupName ) ) ).thenReturn( new String[] { JOB_ID } );
    final Trigger trigger = mock( Trigger.class );
    when( trigger.getPreviousFireTime() ).thenReturn( new Date(  ) );
    when( trigger.getNextFireTime() ).thenReturn( new Date(  ) );
    final Trigger trigger2 = mock( Trigger.class );
    when( trigger2.getGroup() ).thenReturn( "MANUAL_TRIGGER" );
    when( quartzScheduler.getTriggersOfJob( eq( JOB_ID ), eq( groupName ) ) ).thenReturn( new Trigger[] { trigger, trigger2 } );
    setJobDataMap( groupName );

    final List<Job> jobs = scheduler.getJobs( jobFilter );

    assertNotNull( jobs );
    assertEquals( 1, jobs.size() );

    Job job = jobs.get( 0 );
    assertEquals( groupName, job.getGroupName() );
    assertEquals( USER_NAME, job.getUserName() );
    assertEquals( jobDetails, job.getJobParams() );
    assertEquals( JOB_ID, job.getJobId() );
    assertEquals( JOB_NAME, job.getJobName() );
    assertEquals( trigger.getPreviousFireTime(), job.getLastRun() );
    assertEquals( trigger.getNextFireTime(), job.getNextRun() );
  }

  private JobDetail setJobDataMap( String groupName ) throws org.quartz.SchedulerException {
    final JobDetail jobDetail = new JobDetail( JOB_ID, USER_NAME, BlockingQuartzJob.class );
    jobDetail.setJobDataMap( new JobDataMap( jobDetails ) );
    when( quartzScheduler.getJobDetail( eq( JOB_ID ), eq( groupName ) ) ).thenReturn( jobDetail );
    return jobDetail;
  }

  @Test
  public void pauseTest() throws Exception {
    scheduler.pause();

    verify( quartzScheduler, times( 1 ) ).standby();
  }

  @Test
  public void pauseJobTest() throws Exception {
    scheduler.pauseJob( JOB_ID );

    verify( quartzScheduler, times( 1 ) ).pauseJob( eq( JOB_ID ), eq( USER_NAME ) );
  }

  @Test
  public void removeJobTest() throws Exception {
    scheduler.removeJob( JOB_ID );

    verify( quartzScheduler, times( 1 ) ).deleteJob( eq( JOB_ID ), eq( USER_NAME ) );
  }

  @Test
  public void startTest() throws Exception {
    scheduler.start();

    verify( quartzScheduler, times( 1 ) ).start();
  }

  @Test
  public void resumeJobTest() throws Exception {
    scheduler.resumeJob( JOB_ID );
    verify( quartzScheduler, times( 1 ) ).resumeJob( eq( JOB_ID ), eq( USER_NAME ) );
  }

  @Test
  public void getStatusTest() throws Exception {
    when( quartzScheduler.isInStandbyMode() ).thenReturn( true );
    assertEquals( IScheduler.SchedulerStatus.PAUSED, scheduler.getStatus() );

    when( quartzScheduler.isInStandbyMode() ).thenReturn( false );
    when( quartzScheduler.isStarted() ).thenReturn( true );
    assertEquals( IScheduler.SchedulerStatus.RUNNING, scheduler.getStatus() );

    when( quartzScheduler.isInStandbyMode() ).thenThrow( new org.quartz.SchedulerException() );
    try {
      scheduler.getStatus();
      fail();
    } catch ( SchedulerException e ) {
      // it's expected
    }
  }

  @Test
  public void shutdownTest() throws Exception {
    scheduler.shutdown();

    verify( quartzScheduler, times( 1 ) ).shutdown( eq( true ) );
  }

  @Test
  public void fireJobCompletedTest() throws Exception {
    final IAction actionBean = mock( IAction.class );
    final String actionUser = "actionUser";
    final ISchedulerListener schedulerListener = mock( ISchedulerListener.class );
    scheduler.addListener( schedulerListener );
    final Map<String, Serializable> params = new HashMap<>();
    final IBackgroundExecutionStreamProvider streamProvider = mock( IBackgroundExecutionStreamProvider.class );
    scheduler.fireJobCompleted( actionBean, actionUser, params, streamProvider );

    verify( schedulerListener, times( 1 ) ).jobCompleted( eq( actionBean ), eq( actionUser ), eq( params ), eq( streamProvider ) );
  }

  private QuartzJobKey getJobKey() throws SchedulerException {
    return new QuartzJobKey( JOB_NAME, USER_NAME );
  }

  private ComplexJobTrigger getComplexJobTrigger() {
    final ComplexJobTrigger trigger = new ComplexJobTrigger();
    trigger.setSecondRecurrence( 1 );
    return trigger;
  }
}
