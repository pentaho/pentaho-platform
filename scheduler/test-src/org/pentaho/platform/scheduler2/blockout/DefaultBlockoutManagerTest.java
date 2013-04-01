/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Mar 13, 2013 
 * @author wseyler
 */

package org.pentaho.platform.scheduler2.blockout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.scheduler2.quartz.test.StubUserRoleListService;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TestQuartzScheduler;
import org.pentaho.platform.scheduler2.ws.test.JaxWsSchedulerServiceTest.TstPluginManager;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.quartz.CronTrigger;
import org.quartz.DateIntervalTrigger;
import org.quartz.DateIntervalTrigger.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManagerTest {
  /**
   * Standard Units of Time
   */
  static enum TIME {
    MILLISECOND(1), SECOND(MILLISECOND.time * 1000), MINUTE(SECOND.time * 60), HOUR(MINUTE.time * 60), DAY(
        HOUR.time * 24), WEEK(DAY.time * 7), YEAR(DAY.time * 365);

    private long time;

    private TIME(long time) {
      this.time = time;
    }
  }

  IBlockoutManager blockOutManager;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MicroPlatform mp = new MicroPlatform();
    mp.define(IPluginManager.class, TstPluginManager.class);
    mp.define("IScheduler2", TestQuartzScheduler.class); //$NON-NLS-1$
    mp.define(IUserRoleListService.class, StubUserRoleListService.class);
    mp.start();

    blockOutManager = new DefaultBlockoutManager();
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
    // Clear all block outs after each test
    for (IBlockoutTrigger blockOutTrigger : this.blockOutManager.getBlockouts()) {
      this.blockOutManager.deleteBlockout(((Trigger) blockOutTrigger).getName());
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#addBlockout(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testAddBlockout() {
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("blockout", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
    try {
      blockOutManager.addBlockout(trigger);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    try {
      assertEquals(blockOutManager.getBlockout("blockout"), trigger); //$NON-NLS-1$
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#getBlockout(java.lang.String)}.
   */
  @Test
  public void testGetBlockout() {
    SimpleBlockoutTrigger trigger1 = new SimpleBlockoutTrigger("blockout1", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
    SimpleBlockoutTrigger trigger2 = new SimpleBlockoutTrigger("blockout2", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
    try {
      blockOutManager.addBlockout(trigger1);
      blockOutManager.addBlockout(trigger2);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    try {
      assertEquals(blockOutManager.getBlockout("blockout1"), trigger1); //$NON-NLS-1$
      assertEquals(blockOutManager.getBlockout("blockout2"), trigger2); //$NON-NLS-1$
      assertNotSame(trigger1, blockOutManager.getBlockout("blockout2")); //$NON-NLS-1$
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testGetBlockouts() {
    try {
      IBlockoutTrigger[] blockouts = blockOutManager.getBlockouts();
      for (IBlockoutTrigger trigger : blockouts) {
        blockOutManager.deleteBlockout(((Trigger) trigger).getName());
      }
      SimpleBlockoutTrigger trigger1 = new SimpleBlockoutTrigger("blockout1", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
      SimpleBlockoutTrigger trigger2 = new SimpleBlockoutTrigger("blockout2", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
      blockOutManager.addBlockout(trigger1);
      blockOutManager.addBlockout(trigger2);

      blockouts = blockOutManager.getBlockouts();
      assertEquals(2, blockouts.length);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#updateBlockout(java.lang.String, org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testUpdateBlockout() throws Exception {
    long INITIAL_BLOCK_DURATION = 50000l;
    long UPDATED_BLOCK_DURATION = 1000000l;
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger(
        "updateBlockout", new Date(), null, -1, 1000000, INITIAL_BLOCK_DURATION); //$NON-NLS-1$
    blockOutManager.addBlockout(trigger);
    SimpleBlockoutTrigger updatedTrigger = new SimpleBlockoutTrigger(
        "updateBlockout", new Date(), null, -1, 1000000, UPDATED_BLOCK_DURATION); //$NON-NLS-1$
    blockOutManager.updateBlockout(updatedTrigger.getName(), updatedTrigger);
    SimpleBlockoutTrigger retrievedTrigger = (SimpleBlockoutTrigger) blockOutManager.getBlockout(updatedTrigger
        .getName());
    assertEquals(UPDATED_BLOCK_DURATION, retrievedTrigger.getBlockDuration());
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#deleteBlockout(java.lang.String)}.
   */
  @Test
  public void testDeleteBlockout() throws Exception {
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("deleteBlockout", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
    blockOutManager.addBlockout(trigger);
    boolean success = blockOutManager.deleteBlockout("deleteBlockout"); //$NON-NLS-1$
    assertTrue(success);
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willFire(org.quartz.Trigger)}.
   */
  @Test
  public void testWillFire() throws Exception {
    Calendar blockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7);
    IBlockoutTrigger blockOutTrigger = new SimpleBlockoutTrigger("blockOut", blockOutStartDate.getTime(), null, //$NON-NLS-1$
        -1, TIME.WEEK.time, TIME.HOUR.time * 2);

    /*
     * Simple Schedule Triggers
     */
    Calendar scheduleStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7, 1, 0, 0);
    Trigger trueScheduleTrigger = new SimpleTrigger("trueSchedule", "SCHEDULES", scheduleStartDate.getTime(), //$NON-NLS-1$ //$NON-NLS-2$
        null, -1, TIME.DAY.time);

    Trigger falseScheduleTrigger = new SimpleTrigger("falseSchedule", "SCHEDULES", scheduleStartDate.getTime(), //$NON-NLS-1$ //$NON-NLS-2$
        null, -1, TIME.WEEK.time);

    this.blockOutManager.addBlockout(blockOutTrigger);
    assertTrue(this.blockOutManager.willFire(trueScheduleTrigger));
    assertFalse(this.blockOutManager.willFire(falseScheduleTrigger));

    /*
     * Complex Schedule Triggers
     */
    Trigger trueComplexScheduleTrigger = new CronTrigger("trueCronTrigger", "SCHEDULES", "cronJob", "CRONJOBS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        scheduleStartDate.getTime(), null, "0 0 1 ? * MON-TUE"); //$NON-NLS-1$

    Trigger falseComplexScheduleTrigger = new CronTrigger("falseCronTrigger", "SCHEDULES", "cronJob", "CRONJOBS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        scheduleStartDate.getTime(), null, "0 0 1 ? * MON"); //$NON-NLS-1$

    assertTrue(this.blockOutManager.willFire(trueComplexScheduleTrigger));
    assertFalse(this.blockOutManager.willFire(falseComplexScheduleTrigger));

    /*
     * Complex Block out
     */
    this.blockOutManager.deleteBlockout(((Trigger) blockOutTrigger).getName());
    blockOutTrigger = new CronBlockOutTrigger("blockOut", blockOutStartDate.getTime(), null, "0 0 0 ? * MON", null, //$NON-NLS-1$ //$NON-NLS-2$
        TIME.HOUR.time * 2);
    this.blockOutManager.addBlockout(blockOutTrigger);

    assertTrue(this.blockOutManager.willFire(trueScheduleTrigger));
    assertFalse(this.blockOutManager.willFire(falseScheduleTrigger));
    assertTrue(this.blockOutManager.willFire(trueComplexScheduleTrigger));
    assertFalse(this.blockOutManager.willFire(falseComplexScheduleTrigger));

  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#isPartiallyBlocked(org.quartz.Trigger)}.
   */
  @Test
  public void testIsPartiallyBlocked() throws Exception {
    Calendar blockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 1, 0, 0, 0);
    IBlockoutTrigger blockOutTrigger = new SimpleBlockoutTrigger("blockOut", blockOutStartDate.getTime(), null, -1, //$NON-NLS-1$
        TIME.WEEK.time * 2, TIME.HOUR.time * 2);

    /*
     * Simple Schedule Triggers
     */
    Calendar trueScheduleStartDate1 = new GregorianCalendar(2013, Calendar.JANUARY, 15, 0, 0, 0);
    Trigger trueSchedule1 = new SimpleTrigger("trueSchedule1", trueScheduleStartDate1.getTime(), null, -1, //$NON-NLS-1$
        TIME.WEEK.time * 2);

    Calendar trueScheduleStartDate2 = new GregorianCalendar(2013, Calendar.JANUARY, 15, 0, 0, 0);
    Trigger trueSchedule2 = new SimpleTrigger("trueSchedule2", trueScheduleStartDate2.getTime(), null, -1, //$NON-NLS-1$
        TIME.WEEK.time);

    Calendar falseScheduleStartDate1 = new GregorianCalendar(2013, Calendar.JANUARY, 1, 3, 0, 0);
    Trigger falseSchedule1 = new SimpleTrigger("falseSchedule1", falseScheduleStartDate1.getTime(), null, -1, //$NON-NLS-1$
        TIME.WEEK.time);

    this.blockOutManager.addBlockout(blockOutTrigger);

    assertTrue(this.blockOutManager.isPartiallyBlocked(trueSchedule1));
    assertTrue(this.blockOutManager.isPartiallyBlocked(trueSchedule2));
    assertFalse(this.blockOutManager.isPartiallyBlocked(falseSchedule1));

    /*
     * Complex Schedule Triggers 
     */
    Trigger trueComplexScheduleTrigger = new CronTrigger("trueCronTrigger", "SCHEDULES", "cronJob", "CRONJOBS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        trueScheduleStartDate1.getTime(), null, "0 0 1 ? * MON-TUE"); //$NON-NLS-1$

    Trigger falseComplexScheduleTrigger = new CronTrigger("trueCronTrigger", "SCHEDULES", "cronJob", "CRONJOBS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        trueScheduleStartDate1.getTime(), null, "0 0 1 ? * MON"); //$NON-NLS-1$

    assertTrue(this.blockOutManager.isPartiallyBlocked(trueComplexScheduleTrigger));
    assertFalse(this.blockOutManager.isPartiallyBlocked(falseComplexScheduleTrigger));

    /*
     * Complex Block Out Trigger
     */
    this.blockOutManager.deleteBlockout(((Trigger) blockOutTrigger).getName());
    blockOutTrigger = new CronBlockOutTrigger("blockOut", blockOutStartDate.getTime(), null, "0 0 0 ? * TUE", null, //$NON-NLS-1$ //$NON-NLS-2$
        TIME.HOUR.time * 2);
    this.blockOutManager.addBlockout(blockOutTrigger);

    assertTrue(this.blockOutManager.isPartiallyBlocked(trueSchedule1));
    assertTrue(this.blockOutManager.isPartiallyBlocked(trueSchedule2));
    assertFalse(this.blockOutManager.isPartiallyBlocked(falseSchedule1));
    assertTrue(this.blockOutManager.isPartiallyBlocked(trueComplexScheduleTrigger));
    assertFalse(this.blockOutManager.isPartiallyBlocked(falseComplexScheduleTrigger));
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#shouldFireNow()}.
   */
  @Test
  public void testShouldFireNow() throws Exception {
    Date blockOutStartDate = new Date(System.currentTimeMillis());
    SimpleBlockoutTrigger blockOutTrigger = new SimpleBlockoutTrigger("blockOut1", blockOutStartDate, null, //$NON-NLS-1$
        -1, TIME.WEEK.time * 2, TIME.HOUR.time * 2);
    this.blockOutManager.addBlockout(blockOutTrigger);

    assertFalse(this.blockOutManager.shouldFireNow());

    this.blockOutManager.deleteBlockout(blockOutTrigger.getName());
    blockOutStartDate = new Date(System.currentTimeMillis() + TIME.HOUR.time);
    blockOutTrigger = new SimpleBlockoutTrigger("blockOut1", blockOutStartDate, null, //$NON-NLS-1$
        -1, TIME.WEEK.time * 2, TIME.HOUR.time * 2);
    this.blockOutManager.addBlockout(blockOutTrigger);

    assertTrue(this.blockOutManager.shouldFireNow());
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testWillBlockSchedules() throws Exception {
    try {
      Calendar trueBlockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7);
      IBlockoutTrigger trueBlockOutTrigger = new SimpleBlockoutTrigger(
          "blockOut", trueBlockOutStartDate.getTime(), null, //$NON-NLS-1$
          -1, TIME.WEEK.time, TIME.HOUR.time * 2);

      Calendar falseBlockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 9);
      IBlockoutTrigger falseBlockOutTrigger = new SimpleBlockoutTrigger(
          "blockOut", falseBlockOutStartDate.getTime(), null, //$NON-NLS-1$
          -1, TIME.WEEK.time, TIME.HOUR.time * 2);

      IBlockoutTrigger trueComplexBlockOutTrigger = new CronBlockOutTrigger("blockOut", //$NON-NLS-1$
          trueBlockOutStartDate.getTime(), null, "0 0 0 ? * MON", null, TIME.HOUR.time * 2); //$NON-NLS-1$

      IBlockoutTrigger falseComplexBlockOutTrigger = new CronBlockOutTrigger("blockOut", //$NON-NLS-1$
          falseBlockOutStartDate.getTime(), null, "0 0 0 ? * WED", null, TIME.HOUR.time * 2); //$NON-NLS-1$

      Calendar scheduleStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7, 1, 0, 0);
      Trigger scheduleTrigger = new SimpleTrigger("schedule", "SCHEDULES", scheduleStartDate.getTime(), null, //$NON-NLS-1$ //$NON-NLS-2$
          -1, TIME.WEEK.time);
      scheduleJob(scheduleTrigger);

      assertEquals(1, this.blockOutManager.willBlockSchedules(trueBlockOutTrigger).size());
      assertEquals(1, this.blockOutManager.willBlockSchedules(trueComplexBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseComplexBlockOutTrigger).size());

      Trigger cronTrigger = new CronTrigger("cronTrigger", "SCHEDULES", "cronJob", "CRONJOBS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
          scheduleStartDate.getTime(), null, "0 0 1 ? * MON-TUE"); //$NON-NLS-1$
      scheduleJob(cronTrigger);

      assertEquals(2, this.blockOutManager.willBlockSchedules(trueBlockOutTrigger).size());
      assertEquals(2, this.blockOutManager.willBlockSchedules(trueComplexBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseComplexBlockOutTrigger).size());

      Trigger dateIntervalTrigger1 = new DateIntervalTrigger("dateIntervalTrigger1", scheduleStartDate.getTime(), null, //$NON-NLS-1$
          IntervalUnit.WEEK, 2);
      scheduleJob(dateIntervalTrigger1);

      assertEquals(3, this.blockOutManager.willBlockSchedules(trueBlockOutTrigger).size());
      assertEquals(3, this.blockOutManager.willBlockSchedules(trueComplexBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseComplexBlockOutTrigger).size());

      // Test non-standard interval 
      Trigger dateIntervalTrigger2 = new DateIntervalTrigger("dateIntervalTrigger2", scheduleStartDate.getTime(), null, //$NON-NLS-1$
          IntervalUnit.MONTH, 1);
      scheduleJob(dateIntervalTrigger2);

      assertEquals(4, this.blockOutManager.willBlockSchedules(trueBlockOutTrigger).size());
      assertEquals(4, this.blockOutManager.willBlockSchedules(trueComplexBlockOutTrigger).size());
      assertEquals(1, this.blockOutManager.willBlockSchedules(falseBlockOutTrigger).size());
      assertEquals(1, this.blockOutManager.willBlockSchedules(falseComplexBlockOutTrigger).size());

      // Clean up
      deleteJob(scheduleTrigger);
      deleteJob(cronTrigger);
      deleteJob(dateIntervalTrigger1);
      deleteJob(dateIntervalTrigger2);

    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Schedules a non-block out job
   * @param scheduleTrigger
   */
  private void scheduleJob(Trigger scheduleTrigger) throws Exception {
    JobDetail jd = new JobDetail(scheduleTrigger.getName(), scheduleTrigger.getGroup(), TestJob.class);
    scheduleTrigger.setJobName(jd.getName());
    scheduleTrigger.setJobGroup(jd.getGroup());

    QuartzScheduler qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
    qs.getQuartzScheduler().scheduleJob(jd, scheduleTrigger);

  }

  /**
   * Deletes a job from the scheduler
   * @param scheduleTrigger
   */
  private void deleteJob(Trigger scheduleTrigger) throws Exception {
    QuartzScheduler qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
    qs.getQuartzScheduler().deleteJob(scheduleTrigger.getName(), scheduleTrigger.getGroup());
  }

  private class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
      // NoOp
    }
  }

}
