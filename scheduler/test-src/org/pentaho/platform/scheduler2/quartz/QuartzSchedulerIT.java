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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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

@SuppressWarnings( "nls" )
public class QuartzSchedulerIT {
  private static final String CRON_EXPRESSION = "1 0 0 * * ? *";
  private static final String USER_NAME = "userName";
  private static final String JOB_NAME = "jobName";
  private static final String JOB_ID = USER_NAME + "\t" + JOB_NAME + "\t" + System.currentTimeMillis();

  private QuartzScheduler scheduler;
  private Scheduler quartzScheduler;
  private final Map<String, Serializable> jobDetails = new HashMap();

  @Before
  public void init() throws SchedulerException, PlatformInitializationException, org.quartz.SchedulerException {
    final SchedulerFactory schedulerFactory = Mockito.mock( SchedulerFactory.class );
    quartzScheduler = Mockito.mock( Scheduler.class );
    Mockito.when( schedulerFactory.getScheduler() ).thenReturn( quartzScheduler );
    scheduler = Mockito.spy( new QuartzScheduler( schedulerFactory ) );
    Mockito.when( scheduler.getCurrentUser() ).thenReturn( USER_NAME );

    jobDetails.clear();
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS, "RESERVEDMAPKEY_ACTIONCLASS" );
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER, "RESERVEDMAPKEY_STREAMPROVIDER" );
    jobDetails.put( QuartzScheduler.RESERVEDMAPKEY_UIPASSPARAM, "RESERVEDMAPKEY_UIPASSPARAM" );
  }

  @Test
  public void getQuartzSchedulerTest() throws Exception {
    Assert.assertEquals( quartzScheduler, scheduler.getQuartzScheduler() );
  }

  @Test
  public void createJobTest() throws Exception {
    String actionId = "actionId";
    ComplexJobTrigger trigger = getComplexJobTrigger();
    IBackgroundExecutionStreamProvider outputStreamProvider = Mockito.mock( IBackgroundExecutionStreamProvider.class );
    final Job job = scheduler.createJob( JOB_NAME, actionId, null, trigger, outputStreamProvider );

    Assert.assertNotNull( job );
    Assert.assertEquals( Job.JobState.NORMAL, job.getState() );

    Assert.assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) );
    Assert.assertEquals( actionId, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) );
    Assert.assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) );
    Assert.assertEquals( outputStreamProvider, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) );
    Assert.assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    Assert.assertNotNull( job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_LINEAGE_ID ) );
    Assert.assertTrue( job.getJobParams().containsKey( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) );
    Assert.assertEquals( USER_NAME, job.getJobParams().get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) );
  }

  @Test
  public void createJobTest_ForUser() throws Exception {
    String actionId = "actionId";
    ComplexJobTrigger trigger = getComplexJobTrigger();
    IBackgroundExecutionStreamProvider outputStreamProvider = Mockito.mock( IBackgroundExecutionStreamProvider.class );
    Map<String, Serializable> paramMap = new HashMap();
    paramMap.put( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER, "ninja" );
    final Job job = scheduler.createJob( JOB_NAME, paramMap, trigger, outputStreamProvider );

    Assert.assertNotNull( job );
    Assert.assertEquals( "ninja", job.getUserName() );
    Assert.assertEquals( Job.JobState.NORMAL, job.getState() );
  }

  @Test
  public void createQuartzTriggerComplexTriggerTest() throws Exception {
    final Trigger quartzTrigger = QuartzScheduler.createQuartzTrigger( getComplexJobTrigger(), getJobKey() );

    Assert.assertNotNull( quartzTrigger );
    Assert.assertTrue( quartzTrigger instanceof CronTrigger );
    Assert.assertEquals( SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW, quartzTrigger.getMisfireInstruction() );
    Assert.assertEquals( CRON_EXPRESSION, ( (CronTrigger) quartzTrigger ).getCronExpression() );
    Assert.assertEquals( USER_NAME, quartzTrigger.getGroup() );

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

    Assert.assertNotNull( quartzTrigger );
    Assert.assertTrue( quartzTrigger instanceof SimpleTrigger );
    Assert.assertEquals( SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT,
        quartzTrigger.getMisfireInstruction() );
    Assert.assertEquals( USER_NAME, quartzTrigger.getGroup() );

    SimpleTrigger simpleTrigger = (SimpleTrigger) quartzTrigger;
    Assert.assertEquals( startTime, simpleTrigger.getStartTime() );
    Assert.assertEquals( endTime, simpleTrigger.getEndTime() );
    Assert.assertEquals( repeatCount, simpleTrigger.getRepeatCount() );
    Assert.assertEquals( repeatIntervalSeconds * 1000, simpleTrigger.getRepeatInterval() );
  }

  @Test( expected = SchedulerException.class )
  public void createQuartzTriggerNotDefinedTriggerTest() throws Exception {
    final IJobTrigger trigger = Mockito.mock( IJobTrigger.class );
    QuartzScheduler.createQuartzTrigger( trigger, getJobKey() );
  }

  @Test
  public void updateJobTest() throws Exception {
    final JobDetail jobDetail = setJobDataMap( USER_NAME );

    scheduler.updateJob( JOB_ID, new HashMap<String, Serializable>(), getComplexJobTrigger() );

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).addJob( Mockito.eq( jobDetail ), Mockito.eq( true ) );
    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).rescheduleJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ), Mockito.any( Trigger.class ) );
  }

  @Test
  public void triggerNowTest() throws Exception {
    final SimpleTrigger simpleTrigger = Mockito.mock( SimpleTrigger.class );
    final CronTrigger cronTrigger = Mockito.mock( CronTrigger.class );
    Mockito.when( quartzScheduler.getTriggersOfJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) ) ).thenReturn( new Trigger[] { simpleTrigger,
      cronTrigger
    } );

    scheduler.triggerNow( JOB_ID );

    Mockito.verify( simpleTrigger, Mockito.times( 1 ) ).setPreviousFireTime( Mockito.any( Date.class ) );
    Mockito.verify( cronTrigger, Mockito.times( 1 ) ).setPreviousFireTime( Mockito.any( Date.class ) );
    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).rescheduleJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ), Mockito.eq( simpleTrigger ) );
    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).rescheduleJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ), Mockito.eq( cronTrigger ) );
    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).triggerJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) );
  }

  @Test
  public void getJobTest() throws Exception {
    final CronTrigger cronTrigger = Mockito.mock( CronTrigger.class );
    Mockito.when( cronTrigger.getCronExpression() ).thenReturn( CRON_EXPRESSION );
    Mockito.when( quartzScheduler.getTriggersOfJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) ) ).thenReturn( new Trigger[] { cronTrigger } );
    setJobDataMap( USER_NAME );

    final Job job = scheduler.getJob( JOB_ID );

    Assert.assertEquals( JOB_ID, job.getJobId() );
    Assert.assertEquals( jobDetails, job.getJobParams() );
    Assert.assertEquals( USER_NAME, job.getUserName() );
    Assert.assertEquals( JOB_NAME, job.getJobName() );
    Assert.assertEquals( Job.JobState.NORMAL, job.getState() );
  }

  @Test
  public void getJobsTest() throws Exception {
    final IJobFilter jobFilter = Mockito.mock( IJobFilter.class );
    Mockito.when( jobFilter.accept( Mockito.any( Job.class ) ) ).thenReturn( true );

    final String groupName = "groupName";
    Mockito.when( quartzScheduler.getJobGroupNames() ).thenReturn( new String[] { groupName } );
    Mockito.when( quartzScheduler.getJobNames( Mockito.eq( groupName ) ) ).thenReturn( new String[] { JOB_ID } );
    final Trigger trigger = Mockito.mock( Trigger.class );
    Date date1 = new Date();
    Mockito.when( trigger.getPreviousFireTime() ).thenReturn( new Date(  ) );
    Mockito.when( trigger.getFireTimeAfter( Mockito.any( Date.class ) ) ).thenReturn( date1 );
    Mockito.when( trigger.getNextFireTime() ).thenReturn( date1 );
    final Trigger trigger2 = Mockito.mock( Trigger.class );
    Mockito.when( trigger2.getGroup() ).thenReturn( "MANUAL_TRIGGER" );
    Mockito.when( quartzScheduler.getTriggersOfJob( Mockito.eq( JOB_ID ), Mockito.eq( groupName ) ) ).thenReturn( new Trigger[] { trigger, trigger2 } );
    setJobDataMap( groupName );

    final List<Job> jobs = scheduler.getJobs( jobFilter );

    Assert.assertNotNull( jobs );
    Assert.assertEquals( 1, jobs.size() );

    Job job = jobs.get( 0 );
    Assert.assertEquals( groupName, job.getGroupName() );
    Assert.assertEquals( USER_NAME, job.getUserName() );
    Assert.assertEquals( jobDetails, job.getJobParams() );
    Assert.assertEquals( JOB_ID, job.getJobId() );
    Assert.assertEquals( JOB_NAME, job.getJobName() );
    Assert.assertEquals( trigger.getPreviousFireTime(), job.getLastRun() );
    Assert.assertEquals( trigger.getNextFireTime(), job.getNextRun() );
  }

  private JobDetail setJobDataMap( String groupName ) throws org.quartz.SchedulerException {
    final JobDetail jobDetail = new JobDetail( JOB_ID, USER_NAME, BlockingQuartzJob.class );
    jobDetail.setJobDataMap( new JobDataMap( jobDetails ) );
    Mockito.when( quartzScheduler.getJobDetail( Mockito.eq( JOB_ID ), Mockito.eq( groupName ) ) ).thenReturn( jobDetail );
    return jobDetail;
  }

  @Test
  public void pauseTest() throws Exception {
    scheduler.pause();

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).standby();
  }

  @Test
  public void pauseJobTest() throws Exception {
    scheduler.pauseJob( JOB_ID );

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).pauseJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) );
  }

  @Test
  public void removeJobTest() throws Exception {
    scheduler.removeJob( JOB_ID );

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).deleteJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) );
  }

  @Test
  public void startTest() throws Exception {
    scheduler.start();

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).start();
  }

  @Test
  public void resumeJobTest() throws Exception {
    scheduler.resumeJob( JOB_ID );
    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).resumeJob( Mockito.eq( JOB_ID ), Mockito.eq( USER_NAME ) );
  }

  @Test
  public void getStatusTest() throws Exception {
    Mockito.when( quartzScheduler.isInStandbyMode() ).thenReturn( true );
    Assert.assertEquals( IScheduler.SchedulerStatus.PAUSED, scheduler.getStatus() );

    Mockito.when( quartzScheduler.isInStandbyMode() ).thenReturn( false );
    Mockito.when( quartzScheduler.isStarted() ).thenReturn( true );
    Assert.assertEquals( IScheduler.SchedulerStatus.RUNNING, scheduler.getStatus() );

    Mockito.when( quartzScheduler.isInStandbyMode() ).thenThrow( new org.quartz.SchedulerException() );
    try {
      scheduler.getStatus();
      Assert.fail();
    } catch ( SchedulerException e ) {
      // it's expected
    }
  }

  @Test
  public void shutdownTest() throws Exception {
    scheduler.shutdown();

    Mockito.verify( quartzScheduler, Mockito.times( 1 ) ).shutdown( Mockito.eq( true ) );
  }

  @Test
  public void fireJobCompletedTest() throws Exception {
    final IAction actionBean = Mockito.mock( IAction.class );
    final String actionUser = "actionUser";
    final ISchedulerListener schedulerListener = Mockito.mock( ISchedulerListener.class );
    scheduler.addListener( schedulerListener );
    final Map<String, Serializable> params = new HashMap();
    final IBackgroundExecutionStreamProvider streamProvider = Mockito.mock( IBackgroundExecutionStreamProvider.class );
    scheduler.fireJobCompleted( actionBean, actionUser, params, streamProvider );

    Mockito.verify( schedulerListener, Mockito.times( 1 ) ).jobCompleted( Mockito.eq( actionBean ), Mockito.eq( actionUser ), Mockito.eq( params ), Mockito.eq( streamProvider ) );
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
