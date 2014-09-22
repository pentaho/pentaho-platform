/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 * 
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
 */

package org.pentaho.platform.scheduler2.blockout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.scheduler2.ComplexJobTrigger;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.blockout.BlockoutManagerUtil.TIME;
import org.pentaho.platform.scheduler2.quartz.test.StubUserDetailsService;
import org.pentaho.platform.scheduler2.quartz.test.StubUserRoleListService;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TestQuartzScheduler;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TstPluginManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.userdetails.UserDetailsService;

/**
 * @author wseyler
 * 
 */
public class PentahoBlockoutManagerTest {

  private final long duration;

  IBlockoutManager blockOutManager;

  IScheduler scheduler;

  Set<String> jobIdsToClear = new HashSet<String>();

  public PentahoBlockoutManagerTest() {
    duration = TIME.HOUR.time * 2;
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MicroPlatform mp = new MicroPlatform();
    mp.define( IPluginManager.class, TstPluginManager.class );
    mp.define( "IScheduler2", TestQuartzScheduler.class ); //$NON-NLS-1$
    mp.define( IUserRoleListService.class, StubUserRoleListService.class );
    mp.define( UserDetailsService.class, StubUserDetailsService.class );
    mp.define( IBlockoutManager.class, PentahoBlockoutManager.class );
    mp.start();

    blockOutManager = new PentahoBlockoutManager();
    scheduler = PentahoSystem.get( IScheduler.class, "IScheduler2", null ); //$NON-NLS-1$;

    jobIdsToClear.clear();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {

    Set<String> jobIdsIter = new HashSet<String>( this.jobIdsToClear );
    // Clear all jobs after each test
    for ( String jobId : jobIdsIter ) {
      deleteJob( jobId );
    }
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager#getBlockOut(java.lang.String)}.
   */
  @Test
  public void testGetBlockout() throws Exception {
    IJobTrigger blockOutJobTrigger1 = new SimpleJobTrigger( new Date(), null, -1, 1000000 );
    IJobTrigger blockOutJobTrigger2 = new SimpleJobTrigger( new Date(), null, -1, 1000000 );

    Job blockOutJob1 = addBlockOutJob( blockOutJobTrigger1 );
    Job blockOutJob2 = addBlockOutJob( blockOutJobTrigger2 );

    assertEquals( blockOutManager.getBlockOut( blockOutJob1.getJobId() ).toString(), blockOutJobTrigger1.toString() );
    assertEquals( blockOutManager.getBlockOut( blockOutJob2.getJobId() ).toString(), blockOutJobTrigger2.toString() );
    assertNotSame( blockOutJobTrigger1, blockOutManager.getBlockOut( blockOutJob2.getJobId() ) );
  }

  @Test
  public void testGetBlockouts() throws Exception {
    IJobTrigger trigger1 = new SimpleJobTrigger( new Date(), null, -1, 1000000 );
    IJobTrigger trigger2 = new SimpleJobTrigger( new Date(), null, -1, 1000000 );
    addBlockOutJob( trigger1 );
    addBlockOutJob( trigger2 );

    assertEquals( 2, this.blockOutManager.getBlockOutJobs().size() );
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager#willFire(org.quartz.IJobTrigger)}.
   */
  @Test
  public void testWillFire() throws Exception {
    Calendar blockOutStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 7 );
    IJobTrigger blockOutJobTrigger =
        new SimpleJobTrigger( blockOutStartDate.getTime(), null, -1, TIME.WEEK.time / 1000 );
    blockOutJobTrigger.setDuration( duration );

    /*
     * Simple Schedule Triggers
     */
    Calendar scheduleStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 7, 1, 0, 0 );
    IJobTrigger trueScheduleTrigger =
        new SimpleJobTrigger( scheduleStartDate.getTime(), null, -1, TIME.DAY.time / 1000 );

    IJobTrigger falseScheduleTrigger =
        new SimpleJobTrigger( scheduleStartDate.getTime(), null, -1, TIME.WEEK.time / 1000 );

    Job blockOutJob = addBlockOutJob( blockOutJobTrigger );
    assertTrue( this.blockOutManager.willFire( trueScheduleTrigger ) );
    assertFalse( this.blockOutManager.willFire( falseScheduleTrigger ) );

    /*
     * Complex Schedule Triggers
     */
    IJobTrigger trueComplexScheduleTrigger = new ComplexJobTrigger();
    trueComplexScheduleTrigger.setStartTime( scheduleStartDate.getTime() );
    trueComplexScheduleTrigger.setCronString( "0 0 1 ? * 2-3 *" ); //$NON-NLS-1$

    IJobTrigger falseComplexScheduleTrigger = new ComplexJobTrigger();
    falseComplexScheduleTrigger.setStartTime( scheduleStartDate.getTime() );
    falseComplexScheduleTrigger.setCronString( "0 0 1 ? * 2 *" ); //$NON-NLS-1$

    assertTrue( this.blockOutManager.willFire( trueComplexScheduleTrigger ) );
    assertFalse( this.blockOutManager.willFire( falseComplexScheduleTrigger ) );

    /*
     * Complex Block out
     */
    deleteJob( blockOutJob.getJobId() );
    blockOutJobTrigger = new ComplexJobTrigger();
    blockOutJobTrigger.setStartTime( blockOutStartDate.getTime() );
    blockOutJobTrigger.setDuration( duration );
    blockOutJobTrigger.setCronString( "0 0 0 ? * 2 *" ); //$NON-NLS-1$

    addBlockOutJob( blockOutJobTrigger );

    assertTrue( this.blockOutManager.willFire( trueScheduleTrigger ) );
    assertFalse( this.blockOutManager.willFire( falseScheduleTrigger ) );
    assertTrue( this.blockOutManager.willFire( trueComplexScheduleTrigger ) );
    assertFalse( this.blockOutManager.willFire( falseComplexScheduleTrigger ) );

  }

  /**
   * Test method for
   * {@link org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager#isPartiallyBlocked(org.quartz.IJobTrigger)}.
   */
  @Test
  public void testIsPartiallyBlocked() throws Exception {
    Calendar blockOutStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 1, 0, 0, 0 );
    IJobTrigger blockOutTrigger =
        new SimpleJobTrigger( blockOutStartDate.getTime(), null, -1, TIME.WEEK.time * 2 / 1000 );
    blockOutTrigger.setDuration( duration );

    /*
     * Simple Schedule Triggers
     */
    Calendar trueScheduleStartDate1 = new GregorianCalendar( 2013, Calendar.JANUARY, 15, 0, 0, 0 );
    IJobTrigger trueSchedule1 =
        new SimpleJobTrigger( trueScheduleStartDate1.getTime(), null, -1, TIME.WEEK.time * 2 / 1000 );

    Calendar trueScheduleStartDate2 = new GregorianCalendar( 2013, Calendar.JANUARY, 15, 0, 0, 0 );
    IJobTrigger trueSchedule2 =
        new SimpleJobTrigger( trueScheduleStartDate2.getTime(), null, -1, TIME.WEEK.time / 1000 );

    Calendar falseScheduleStartDate1 = new GregorianCalendar( 2013, Calendar.JANUARY, 1, 3, 0, 0 );
    IJobTrigger falseSchedule1 =
        new SimpleJobTrigger( falseScheduleStartDate1.getTime(), null, -1, TIME.WEEK.time / 1000 );

    Job blockOutJob = addBlockOutJob( blockOutTrigger );

    assertTrue( this.blockOutManager.isPartiallyBlocked( trueSchedule1 ) );
    assertTrue( this.blockOutManager.isPartiallyBlocked( trueSchedule2 ) );
    assertFalse( this.blockOutManager.isPartiallyBlocked( falseSchedule1 ) );

    /*
     * Complex Schedule Triggers
     */
    IJobTrigger trueComplexScheduleTrigger = new ComplexJobTrigger();
    trueComplexScheduleTrigger.setStartTime( trueScheduleStartDate1.getTime() );
    trueComplexScheduleTrigger.setCronString( "0 0 1 ? * 2-3 *" ); //$NON-NLS-1$

    IJobTrigger falseComplexScheduleTrigger = new ComplexJobTrigger();
    falseComplexScheduleTrigger.setStartTime( trueScheduleStartDate1.getTime() );
    falseComplexScheduleTrigger.setCronString( "0 0 1 ? * 2 *" ); //$NON-NLS-1$

    assertTrue( this.blockOutManager.isPartiallyBlocked( trueComplexScheduleTrigger ) );
    assertFalse( this.blockOutManager.isPartiallyBlocked( falseComplexScheduleTrigger ) );

    /*
     * Complex Block Out IJobTrigger
     */
    deleteJob( blockOutJob.getJobId() );
    blockOutTrigger = new ComplexJobTrigger();
    blockOutTrigger.setStartTime( blockOutStartDate.getTime() );
    blockOutTrigger.setCronString( "0 0 0 ? * 3 *" ); //$NON-NLS-1$
    blockOutTrigger.setDuration( duration );
    addBlockOutJob( blockOutTrigger );

    assertTrue( this.blockOutManager.isPartiallyBlocked( trueSchedule1 ) );
    assertTrue( this.blockOutManager.isPartiallyBlocked( trueSchedule2 ) );
    assertFalse( this.blockOutManager.isPartiallyBlocked( falseSchedule1 ) );
    assertTrue( this.blockOutManager.isPartiallyBlocked( trueComplexScheduleTrigger ) );
    assertFalse( this.blockOutManager.isPartiallyBlocked( falseComplexScheduleTrigger ) );
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager#shouldFireNow()}.
   */
  @Test
  public void testShouldFireNow() throws Exception {
    Date blockOutStartDate = new Date( System.currentTimeMillis() );
    IJobTrigger blockOutJobTrigger = new SimpleJobTrigger( blockOutStartDate, null, -1, TIME.WEEK.time * 2 / 1000 );
    blockOutJobTrigger.setDuration( duration );

    Job blockOutJob = addBlockOutJob( blockOutJobTrigger );

    assertFalse( this.blockOutManager.shouldFireNow() );

    deleteJob( blockOutJob.getJobId() );
    blockOutStartDate = new Date( System.currentTimeMillis() + TIME.HOUR.time );
    blockOutJobTrigger = new SimpleJobTrigger( blockOutStartDate, null, -1, TIME.WEEK.time * 2 / 1000 );
    blockOutJobTrigger.setDuration( duration );
    addBlockOutJob( blockOutJobTrigger );

    assertTrue( this.blockOutManager.shouldFireNow() );
  }

  /**
   * Test method for
   * {@link org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager#willBlockSchedules
   * (org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}
   * .
   */
  @Test
  public void testWillBlockSchedules() throws Exception {
    Calendar trueBlockOutStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 7 );
    IJobTrigger trueBlockOutTrigger =
        new SimpleJobTrigger( trueBlockOutStartDate.getTime(), null, -1, TIME.WEEK.time / 1000 );
    trueBlockOutTrigger.setDuration( duration );

    Calendar falseBlockOutStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 9 );
    IJobTrigger falseBlockOutTrigger =
        new SimpleJobTrigger( falseBlockOutStartDate.getTime(), null, -1, TIME.WEEK.time / 1000 );
    falseBlockOutTrigger.setDuration( duration );

    IJobTrigger trueComplexBlockOutTrigger = new ComplexJobTrigger();
    trueComplexBlockOutTrigger.setStartTime( trueBlockOutStartDate.getTime() );
    trueComplexBlockOutTrigger.setCronString( "0 0 0 ? * 2 *" ); //$NON-NLS-1$
    trueComplexBlockOutTrigger.setDuration( duration );

    IJobTrigger falseComplexBlockOutTrigger = new ComplexJobTrigger();
    falseComplexBlockOutTrigger.setStartTime( falseBlockOutStartDate.getTime() );
    falseComplexBlockOutTrigger.setCronString( "0 0 0 ? * WED *" ); //$NON-NLS-1$
    falseComplexBlockOutTrigger.setDuration( duration );

    Calendar scheduleStartDate = new GregorianCalendar( 2013, Calendar.JANUARY, 7, 1, 0, 0 );
    IJobTrigger scheduleTrigger = new SimpleJobTrigger( scheduleStartDate.getTime(), null, -1, TIME.WEEK.time / 1000 );
    addJob( scheduleTrigger, "scheduleTrigger" ); //$NON-NLS-1$

    assertEquals( 1, this.blockOutManager.willBlockSchedules( trueBlockOutTrigger ).size() );
    assertEquals( 1, this.blockOutManager.willBlockSchedules( trueComplexBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseComplexBlockOutTrigger ).size() );

    IJobTrigger cronTrigger = new ComplexJobTrigger();
    cronTrigger.setStartTime( scheduleStartDate.getTime() );
    cronTrigger.setCronString( "0 0 1 ? * 2-3 *" ); //$NON-NLS-1$
    addJob( cronTrigger, "cronTrigger" ); //$NON-NLS-1$

    assertEquals( 2, this.blockOutManager.willBlockSchedules( trueBlockOutTrigger ).size() );
    assertEquals( 2, this.blockOutManager.willBlockSchedules( trueComplexBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseComplexBlockOutTrigger ).size() );

    IJobTrigger complexJobTrigger1 = new ComplexJobTrigger( null, null, null, ComplexJobTrigger.MONDAY, 0 );
    complexJobTrigger1.setStartTime( scheduleStartDate.getTime() );
    addJob( complexJobTrigger1, "complexJobTrigger1" ); //$NON-NLS-1$

    assertEquals( 3, this.blockOutManager.willBlockSchedules( trueBlockOutTrigger ).size() );
    assertEquals( 3, this.blockOutManager.willBlockSchedules( trueComplexBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseBlockOutTrigger ).size() );
    assertEquals( 0, this.blockOutManager.willBlockSchedules( falseComplexBlockOutTrigger ).size() );

    // Test non-standard interval
    IJobTrigger complexJobTrigger2 = new ComplexJobTrigger( null, null, 1, null, 0 );
    complexJobTrigger2.setStartTime( scheduleStartDate.getTime() );
    addJob( complexJobTrigger2, "complexJobTrigger2" ); //$NON-NLS-1$

    assertEquals( 4, this.blockOutManager.willBlockSchedules( trueBlockOutTrigger ).size() );
    assertEquals( 4, this.blockOutManager.willBlockSchedules( trueComplexBlockOutTrigger ).size() );
    assertEquals( 1, this.blockOutManager.willBlockSchedules( falseBlockOutTrigger ).size() );
    assertEquals( 1, this.blockOutManager.willBlockSchedules( falseComplexBlockOutTrigger ).size() );
  }

  private Job addBlockOutJob( IJobTrigger blockOutJobTrigger ) throws Exception {
    Map<String, Serializable> jobParams = new HashMap<String, Serializable>();
    jobParams.put( IBlockoutManager.DURATION_PARAM, blockOutJobTrigger.getDuration() );

    return addJob( blockOutJobTrigger, IBlockoutManager.BLOCK_OUT_JOB_NAME, new BlockoutAction(), jobParams );
  }

  private Job addJob( IJobTrigger jobTrigger, String jobName ) throws Exception {
    return addJob( jobTrigger, jobName, new IAction() {
      @Override
      public void execute() throws Exception {
      }
    }, new HashMap<String, Serializable>() );
  }

  private Job addJob( IJobTrigger jobTrigger, String jobName, IAction action, Map<String, Serializable> jobParams )
    throws Exception {
    Job job = this.scheduler.createJob( jobName, action.getClass(), jobParams, jobTrigger );
    this.jobIdsToClear.add( job.getJobId() );
    return job;
  }

  private void deleteJob( String jobId ) throws Exception {
    this.scheduler.removeJob( jobId );
    this.jobIdsToClear.remove( jobId );
  }
}
