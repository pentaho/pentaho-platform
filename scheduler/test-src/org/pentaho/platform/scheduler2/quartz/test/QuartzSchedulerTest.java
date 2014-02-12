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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz.test;

import java.io.Serializable;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IAuditEntry;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.JobTrigger;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.api.scheduler2.recur.ITimeRecurrence;
import org.pentaho.platform.engine.core.TestAuditEntry;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.QuartzSchedulerAvailability;
import org.pentaho.platform.scheduler2.recur.IncrementalRecurrence;
import org.pentaho.platform.scheduler2.recur.RecurrenceList;
import org.pentaho.platform.scheduler2.recur.SequentialRecurrence;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TestQuartzScheduler;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.userdetails.UserDetailsService;

@SuppressWarnings( "nls" )
public class QuartzSchedulerTest {
  private QuartzScheduler scheduler;

  public static final String SESSION_PRINCIPAL = "SECURITY_PRINCIPAL";

  private String TEST_USER = "TestUser";

  private HashMap<String, Serializable> jobParams;

  @Before
  public void init() throws SchedulerException, PlatformInitializationException {
    scheduler = new QuartzScheduler();
    scheduler.start();

    TestAction.reset();
    TestAction2.reset();
    TestAction3.reset();
    TestActionCustomParam.reset();

    MicroPlatform mp = new MicroPlatform();
    mp.define( "IScheduler2", TestQuartzScheduler.class ); //$NON-NLS-1$
    mp.define( IUserRoleListService.class, StubUserRoleListService.class );
    mp.define( UserDetailsService.class, StubUserDetailsService.class );
    mp.define( IAuditEntry.class, TestAuditEntry.class );
    mp.start();

    SecurityHelper.getInstance().becomeUser( TEST_USER );
    jobParams = new HashMap<String, Serializable>();
  }

  @After
  public void cleanup() throws SchedulerException {
    // have to clear the jobs between tests since quartz is actually serving us the same base scheduler instance
    for ( Job job : scheduler.getJobs( null ) ) {
      scheduler.removeJob( job.getJobId() );
    }
    Assert.assertEquals( "Stale jobs are hanging around!", scheduler.getJobs( null ).size(), 0 );
  }

  @Test
  public void quartzAvailabilityTest() {

    Date startTime = new Date();
    Date endTime = new Date( startTime.getTime() + 1000 );
    org.quartz.Calendar calendar = new QuartzSchedulerAvailability( startTime, endTime );
    Assert.assertTrue( calendar.isTimeIncluded( startTime.getTime() ) );
    Assert.assertTrue( calendar.isTimeIncluded( endTime.getTime() ) );
    Assert.assertFalse( calendar.isTimeIncluded( startTime.getTime() - 1 ) );
    Assert.assertFalse( calendar.isTimeIncluded( endTime.getTime() + 1 ) );
    Assert.assertEquals( calendar.getNextIncludedTime( startTime.getTime() - 1 ), startTime.getTime() );
    Assert.assertEquals( calendar.getNextIncludedTime( startTime.getTime() ), startTime.getTime() + 1 );
    Assert.assertEquals( calendar.getNextIncludedTime( endTime.getTime() - 1 ), endTime.getTime() );
    Assert.assertEquals( calendar.getNextIncludedTime( endTime.getTime() ), 0 );
    Assert.assertEquals( calendar.getNextIncludedTime( endTime.getTime() + 1 ), 0 );

    calendar = new QuartzSchedulerAvailability( null, endTime );
    Assert.assertTrue( calendar.isTimeIncluded( startTime.getTime() ) );
    Assert.assertTrue( calendar.isTimeIncluded( endTime.getTime() ) );
    Assert.assertFalse( calendar.isTimeIncluded( endTime.getTime() + 1 ) );
    Assert.assertEquals( calendar.getNextIncludedTime( startTime.getTime() ), startTime.getTime() + 1 );
    Assert.assertEquals( calendar.getNextIncludedTime( endTime.getTime() ), 0 );
    Assert.assertEquals( calendar.getNextIncludedTime( endTime.getTime() + 1 ), 0 );

    calendar = new QuartzSchedulerAvailability( startTime, null );
    Assert.assertTrue( calendar.isTimeIncluded( startTime.getTime() ) );
    Assert.assertFalse( calendar.isTimeIncluded( startTime.getTime() - 1 ) );
    Assert.assertTrue( calendar.isTimeIncluded( endTime.getTime() ) );
    Assert.assertEquals( calendar.getNextIncludedTime( startTime.getTime() - 1 ), startTime.getTime() );
    Assert.assertEquals( calendar.getNextIncludedTime( startTime.getTime() ), startTime.getTime() + 1 );
  }

  @Test
  public void runComplexTriggerTest() throws SchedulerException {
    Calendar calendar = Calendar.getInstance();

    int startingMinute = calendar.get( Calendar.MINUTE );

    RecurrenceList recurrenceList = new RecurrenceList( 40, 45 );
    SequentialRecurrence sequentialRecurrence = new SequentialRecurrence( 40, 45 );
    IncrementalRecurrence incrementalRecurrence = new IncrementalRecurrence( 40, 5 );

    if ( calendar.get( Calendar.SECOND ) > 30 ) {
      recurrenceList = new RecurrenceList( 10, 15 );
      sequentialRecurrence = new SequentialRecurrence( 10, 15 );
      incrementalRecurrence = new IncrementalRecurrence( 10, 5 );
      startingMinute++;
    }
    ComplexJobTrigger jobTrigger = new ComplexJobTrigger();
    jobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    jobTrigger.setMinuteRecurrence( startingMinute );

    jobTrigger.setSecondRecurrence( recurrenceList );
    System.out.println( jobTrigger.toString() );
    String jobName = "complexJob1";
    Job job = scheduler.createJob( jobName, TestAction.class, new HashMap<String, Serializable>(), jobTrigger );
    Assert.assertTrue( job.getJobName().contains( jobName ) );
    Assert.assertEquals( job.getSchedulableClass(), TestAction.class.getName() );
    Assert.assertEquals( job.getJobTrigger().toString(), jobTrigger.toString() );

    jobTrigger.setSecondRecurrence( sequentialRecurrence );
    jobName = "complexJob2";
    job = scheduler.createJob( jobName, TestAction2.class, new HashMap<String, Serializable>(), jobTrigger );
    Assert.assertTrue( job.getJobName().contains( jobName ) );
    Assert.assertEquals( job.getSchedulableClass(), TestAction2.class.getName() );
    Assert.assertEquals( job.getJobTrigger().toString(), jobTrigger.toString() );

    jobTrigger.setSecondRecurrence( incrementalRecurrence );
    jobName = "complexJob3";
    job = scheduler.createJob( jobName, TestAction3.class, new HashMap<String, Serializable>(), jobTrigger );
    Assert.assertTrue( job.getJobName().contains( jobName ) );
    Assert.assertEquals( job.getSchedulableClass(), TestAction3.class.getName() );
    Assert.assertEquals( job.getJobTrigger().toString(), jobTrigger.toString() );

    //
    // Wait for the jobs to complete, then check that each action executed the correct number of times
    //
    sleep( 60 );

    Assert.assertEquals( "Job did not run the correct number of times", 2, TestAction.counter );
    Assert.assertEquals( "Job did not run the correct number of times", 6, TestAction2.counter );
    System.out.println( "Counter = " + TestAction3.counter );
    Assert.assertTrue( "Job did not run.", TestAction3.counter > 3 );
  }

  @Test
  public void editJobTest() throws SchedulerException {
    SimpleJobTrigger repeater = new SimpleJobTrigger();
    repeater.setStartTime( new Date() );
    repeater.setRepeatInterval( 5 );
    repeater.setRepeatCount( 20 );

    Job job = scheduler.createJob( "testName", TestAction.class, new HashMap<String, Serializable>(), repeater );

    sleep( 12 );

    Assert.assertTrue( "Job did not run the correct number of times", TestAction.counter >= 2 );

    repeater = new SimpleJobTrigger();
    repeater.setStartTime( new Date() );
    repeater.setRepeatInterval( 20 );
    repeater.setRepeatCount( 3 );

    int count = TestAction.counter;
    System.out.println( "updating job! " + new Date() );
    scheduler.updateJob( job.getJobId(), new HashMap<String, Serializable>(), repeater );
    List<Job> jobs = scheduler.getJobs( null );
    Assert.assertEquals( "Unexpected number of scheduled jobs", 1, jobs.size() );
    SimpleJobTrigger simpleJobTrigger = (SimpleJobTrigger) jobs.get( 0 ).getJobTrigger();
    Assert.assertEquals( 20, simpleJobTrigger.getRepeatInterval() );
    Assert.assertEquals( 3, simpleJobTrigger.getRepeatCount() );
    sleep( 5 );
    Assert.assertEquals( "Job did not run the correct number of times", count + 1, TestAction.counter );
    count = TestAction.counter;
    sleep( 10 );
    Assert.assertEquals( "Job ran unexpectedly", count, TestAction.counter );
    sleep( 20 );
    Assert.assertTrue( "Job did not run the correct number of times", count < TestAction.counter );
  }

  @Test
  public void runTenTimesJobTest() throws SchedulerException {
    SimpleJobTrigger repeater = new SimpleJobTrigger();
    repeater.setStartTime( new Date() );
    repeater.setRepeatInterval( 1 );
    repeater.setRepeatCount( 9 );

    scheduler.createJob( "testName", TestAction.class, new HashMap<String, Serializable>(), repeater );

    sleep( 5 );
    Assert.assertTrue( "Job did not run the correct number of times", TestAction.counter > 3 );
    System.out.println( "Counter = " + TestAction.counter );
    Assert.assertTrue( "Job did not run the correct number of times", TestAction.counter < 7 );
    sleep( 10 );
    Assert.assertEquals( "Job did not run the correct number of times", 10, TestAction.counter );

    List<Job> jobs = scheduler.getJobs( null );
    Assert.assertEquals( 0, jobs.size() );
  }

  @Test
  public void testJobRunsAsSchedulingUser() throws SchedulerException {
    SimpleJobTrigger RUN_ONCE_IN_2_SECS = JobTrigger.ONCE_NOW;
    RUN_ONCE_IN_2_SECS.setStartTime( new Date( System.currentTimeMillis() + 2000L ) );

    scheduler.createJob( "testName", TestAction.class, new HashMap<String, Serializable>(), RUN_ONCE_IN_2_SECS );

    SecurityHelper.getInstance().becomeUser( "Ima Wronguser" );

    sleep( 3 );

    Assert.assertEquals( "Job did not run the correct number of times", 1, TestAction.counter );
    Assert.assertEquals( "Job did not run as the correct user", TEST_USER, TestAction.executedAsUser );

    List<Job> jobs = scheduler.getJobs( null );
    Assert.assertEquals( 0, jobs.size() );
  }

  @Test
  public void testParamterizedJob() throws SchedulerException {
    jobParams.put( "testParam1", "testParam1-value" );
    jobParams.put( "testParam2", "testParam2-value" );

    scheduler.createJob( "testName", TestAction.class, jobParams, JobTrigger.ONCE_NOW );

    sleep( 2 );

    Assert.assertEquals( "Job did not run the correct number of times", 1, TestAction.counter );
    Assert.assertEquals( "A parameter was not properly set", "testParam1-value", TestAction.testParam1_public );
    Assert.assertEquals( "A parameter was not properly set", "testParam2-value", TestAction.testParam2_public );

    List<Job> jobs = scheduler.getJobs( null );
    Assert.assertEquals( 0, jobs.size() );
  }

  @Test
  public void testPauseAndResumeJob() throws SchedulerException {
    String jobName = "complexJob1";
    int counter = TestAction.counter;
    Calendar calendar = Calendar.getInstance();
    int startingMin = calendar.get( Calendar.MINUTE );
    int startingSec = calendar.get( Calendar.SECOND ) + 10;
    if ( startingSec > 59 ) {
      startingSec = startingSec % 60;
      startingMin++;
    }
    ComplexJobTrigger complexJobTrigger = new ComplexJobTrigger();
    complexJobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    complexJobTrigger.setMinuteRecurrence( startingMin );
    complexJobTrigger.setSecondRecurrence( startingSec );
    Job job = scheduler.createJob( jobName, TestAction.class, jobParams, complexJobTrigger );
    scheduler.pauseJob( job.getJobId() );
    sleep( 30 );
    Assert.assertEquals( counter, TestAction.counter );
    Assert.assertEquals( 1, scheduler.getJobs( null ).size() );
    scheduler.resumeJob( job.getJobId() );
    sleep( 30 );
    Assert.assertTrue( counter != TestAction.counter );
    scheduler.removeJob( job.getJobId() );
    Assert.assertEquals( 0, scheduler.getJobs( null ).size() );
  }

  @Test
  public void testPauseAndResumeScheduler() throws SchedulerException {
    Calendar calendar = Calendar.getInstance();
    int startingMin = calendar.get( Calendar.MINUTE );
    int startingSec = calendar.get( Calendar.SECOND ) + 10;
    if ( startingSec > 59 ) {
      startingSec = startingSec % 60;
      startingMin++;
    }
    ComplexJobTrigger complexJobTrigger = new ComplexJobTrigger();
    complexJobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    complexJobTrigger.setMinuteRecurrence( (ITimeRecurrence) null );
    complexJobTrigger.setSecondRecurrence( startingSec );
    String jobName = "complexJob1";
    int counter = TestAction.counter;

    scheduler.pause();
    scheduler.createJob( jobName, TestAction.class, jobParams, complexJobTrigger );
    sleep( 30 );
    Assert.assertEquals( "Job executed while scheduler is supposedly paused!", counter, TestAction.counter );
    Assert.assertEquals( "Scheduler is not aware of the dormant job!", 1, scheduler.getJobs( null ).size() );

    scheduler.start();
    sleep( 90 );
    Assert.assertTrue( "Job did not execute after scheduler started back up!", counter != TestAction.counter );
  }

  @Ignore
  @Test
  public void testStartAndEndDate() throws SchedulerException {

    ComplexJobTrigger jobTrigger = new ComplexJobTrigger();
    ComplexJobTrigger jobTrigger2 = new ComplexJobTrigger();
    ComplexJobTrigger jobTrigger3 = new ComplexJobTrigger();
    jobTrigger.setHourlyRecurrence( (ITimeRecurrence) null );
    jobTrigger2.setHourlyRecurrence( (ITimeRecurrence) null );
    jobTrigger3.setHourlyRecurrence( (ITimeRecurrence) null );
    jobTrigger.setMinuteRecurrence( (ITimeRecurrence) null );
    jobTrigger2.setMinuteRecurrence( (ITimeRecurrence) null );
    jobTrigger3.setMinuteRecurrence( (ITimeRecurrence) null );
    jobTrigger.setSecondRecurrence( new IncrementalRecurrence( 0, 5 ) );
    jobTrigger2.setSecondRecurrence( new IncrementalRecurrence( 0, 5 ) );
    jobTrigger3.setSecondRecurrence( new IncrementalRecurrence( 0, 5 ) );

    Calendar calendar = Calendar.getInstance();
    int startingMin = calendar.get( Calendar.MINUTE );
    int startingSec = calendar.get( Calendar.SECOND ) + 20;
    if ( startingSec > 59 ) {
      startingSec = startingSec % 60;
      startingMin++;
    }
    calendar.set( Calendar.MINUTE, startingMin );
    calendar.set( Calendar.SECOND, startingSec );

    jobTrigger.setStartTime( calendar.getTime() );
    jobTrigger2.setStartTime( calendar.getTime() );

    calendar.add( Calendar.MINUTE, 1 );
    jobTrigger.setEndTime( calendar.getTime() );
    jobTrigger3.setEndTime( calendar.getTime() );

    int counter = TestAction.counter;
    int counter2 = TestAction2.counter;
    int counter3 = TestAction3.counter;

    // Job job = scheduler.createJob("complexJob", TestAction.class, jobParams, jobTrigger);
    Job job2 = scheduler.createJob( "complexJob2", TestAction2.class, jobParams, jobTrigger2 );
    Job job3 = scheduler.createJob( "complexJob3", TestAction3.class, jobParams, jobTrigger3 );

    sleep( 10 );
    // Assert.assertEquals(counter, TestAction.counter);
    Assert.assertEquals( counter2, TestAction2.counter );
    Assert.assertTrue( counter3 != TestAction3.counter );
    counter3 = TestAction3.counter;
    sleep( 20 );
    // Assert.assertTrue(counter != TestAction.counter);
    Assert.assertTrue( counter2 != TestAction2.counter );
    Assert.assertTrue( counter3 != TestAction3.counter );
    sleep( 60 );
    counter = TestAction.counter;
    counter2 = TestAction2.counter;
    // counter3 = TestAction3.counter;
    sleep( 30 );
    // Assert.assertEquals(counter, TestAction.counter);
    Assert.assertTrue( counter2 != TestAction2.counter );
    Assert.assertEquals( counter3, TestAction3.counter );

    // scheduler.removeJob(job.getJobId());
    scheduler.removeJob( job2.getJobId() );
    scheduler.removeJob( job3.getJobId() );
    Assert.assertEquals( 0, scheduler.getJobs( null ).size() );
  }

  @Test
  public void testWrongParameterTypeJob() throws SchedulerException {
    jobParams.put( "customParam", "Should not be able to convert this string to the CustomParamType" );

    scheduler.createJob( "testName", TestActionCustomParam.class, jobParams, JobTrigger.ONCE_NOW );

    sleep( 2 );

    Assert.assertEquals( "The job should have failed prior to execute being called", 0, TestActionCustomParam.counter );
    Assert
        .assertNull(
            "The root cause of failure should be the inability to convert a string to the custom type, "
                + "thus this value should definitely be null",
            TestActionCustomParam.customParam_public );
  }

  @Test( expected = SchedulerException.class )
  public void testNullAction() throws SchedulerException {
    scheduler.createJob( "testName", (Class<IAction>) null, jobParams, JobTrigger.ONCE_NOW );
  }

  @Test
  public void testRemoveJob() throws SchedulerException {
    scheduler.pause();
    ComplexJobTrigger trigger = new ComplexJobTrigger();
    trigger.addYearlyRecurrence( 2050 );
    scheduler.createJob( "testName1", TestAction.class, jobParams, trigger );
    Assert.assertEquals( 1, scheduler.getJobs( null ).size() );

    for ( Job oldJob : scheduler.getJobs( null ) ) {
      scheduler.removeJob( oldJob.getJobId() );
    }

    Assert.assertEquals( 0, scheduler.getJobs( null ).size() );
  }

  @Test
  public void testGetJobs() throws SchedulerException {
    QuartzScheduler testGetJobsSchduler = new QuartzScheduler();

    Assert.assertEquals( 0, testGetJobsSchduler.getJobs( null ).size() );

    String PARAM_KEY = "testGetJobsKey";

    jobParams.put( PARAM_KEY, "testParam1-value" );
    Calendar calendar = Calendar.getInstance();
    int startingMin = calendar.get( Calendar.MINUTE );
    int startingSec = calendar.get( Calendar.SECOND ) + 10;
    if ( startingSec > 59 ) {
      startingSec = startingSec % 60;
      startingMin++;
    }
    calendar.set( Calendar.MINUTE, startingMin );
    calendar.set( Calendar.SECOND, startingSec );
    SimpleJobTrigger jobTrigger = new SimpleJobTrigger( calendar.getTime(), null, 0, 0 );
    testGetJobsSchduler.createJob( "getJobsTestJob", TestAction.class, jobParams, jobTrigger );

    List<Job> jobs = testGetJobsSchduler.getJobs( null );
    Assert.assertEquals( 1, jobs.size() );

    Job theJob = jobs.get( 0 );
    Assert.assertTrue( theJob.getJobParams().containsKey( PARAM_KEY ) );
    Assert.assertTrue( theJob.getJobName().contains( "getJobsTestJob" ) );
  }

  public static class TestAction2 implements IAction {
    public static int counter = 0;

    public static String executedAsUser;

    public void execute() throws Exception {
      System.out.println( "execute called!" + new Date() );
      counter++;
      Principal p = SecurityHelper.getInstance().getAuthentication();
      executedAsUser = ( p == null ) ? null : p.getName();
    }

    public static void reset() {
      counter = 0;
      executedAsUser = null;
    }
  }

  public static class TestAction3 implements IAction {
    public static int counter = 0;

    public static String executedAsUser;

    public void execute() throws Exception {
      System.out.println( "execute called!" + new Date() );
      counter++;
      Principal p = SecurityHelper.getInstance().getAuthentication();
      executedAsUser = ( p == null ) ? null : p.getName();
    }

    public static void reset() {
      counter = 0;
      executedAsUser = null;
    }
  }

  public static class TestAction implements IAction {
    public static int counter = 0;

    public static String executedAsUser;

    private String testParam1;

    private String testParam2;

    public static String testParam1_public, testParam2_public;

    public String getTestParam1() {
      return testParam1;
    }

    public void setTestParam1( String testParam1 ) {
      this.testParam1 = testParam1;
    }

    public String getTestParam2() {
      return testParam2;
    }

    public void setTestParam2( String testParam2 ) {
      this.testParam2 = testParam2;
    }

    public void execute() throws Exception {
      System.out.println( "execute called! " + new Date() );
      counter++;
      Principal p = SecurityHelper.getInstance().getAuthentication();
      executedAsUser = ( p == null ) ? null : p.getName();

      testParam1_public = getTestParam1();
      testParam2_public = getTestParam2();
    }

    public static void reset() {
      counter = 0;
      executedAsUser = null;
      testParam1_public = null;
      testParam2_public = null;
    }
  }

  public static class TestActionCustomParam extends TestAction {
    private CustomParamType customParam;

    public static CustomParamType customParam_public;

    public CustomParamType getCustomParam() {
      return customParam;
    }

    public void setCustomParam( CustomParamType customParam ) {
      this.customParam = customParam;
    }

    @Override
    public void execute() throws Exception {
      super.execute();
      customParam_public = getCustomParam();
    }

    public static void reset() {
      TestAction.reset();
      customParam_public = null;
    }
  }

  public static class CustomParamType {

  }

  public static class NotAnIAction {

  }

  private void sleep( int seconds ) {
    try {
      Thread.sleep( seconds * 1000 );
    } catch ( InterruptedException e ) {
      throw new RuntimeException( e );
    }
  }
}
