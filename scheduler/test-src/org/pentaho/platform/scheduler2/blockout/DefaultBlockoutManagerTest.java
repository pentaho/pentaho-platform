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
import static org.junit.Assert.fail;

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
  enum TIME {
    MILLISECOND(1), SECOND(MILLISECOND.time * 1000), MINUTE(SECOND.time * 60), HOUR(MINUTE.time * 60), DAY(
        HOUR.time * 24), WEEK(DAY.time * 7), MONTH(DAY.time * 30), YEAR(DAY.time * 365 + HOUR.time * 8);

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
  public void testUpdateBlockout() {
    long INITIAL_BLOCK_DURATION = 50000l;
    long UPDATED_BLOCK_DURATION = 1000000l;
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("updateBlockout", new Date(), null, -1, 1000000, INITIAL_BLOCK_DURATION); //$NON-NLS-1$
    try {
      blockOutManager.addBlockout(trigger);
      SimpleBlockoutTrigger updatedTrigger = new SimpleBlockoutTrigger("updateBlockout", new Date(), null, -1, 1000000, UPDATED_BLOCK_DURATION); //$NON-NLS-1$
      blockOutManager.updateBlockout(updatedTrigger.getName(), updatedTrigger);
      SimpleBlockoutTrigger retrievedTrigger = (SimpleBlockoutTrigger) blockOutManager.getBlockout(updatedTrigger.getName());
      assertEquals(UPDATED_BLOCK_DURATION, retrievedTrigger.getBlockDuration());
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#deleteBlockout(java.lang.String)}.
   */
  @Test
  public void testDeleteBlockout() {
    SimpleBlockoutTrigger trigger = new SimpleBlockoutTrigger("deleteBlockout", new Date(), null, -1, 1000000, 50000); //$NON-NLS-1$
    try {
      blockOutManager.addBlockout(trigger);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
    try {
      boolean success = blockOutManager.deleteBlockout("deleteBlockout"); //$NON-NLS-1$
      assertTrue(success);
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willFire(org.quartz.Trigger)}.
   */
  @Test
  public void testWillFire() {
    try {
      Calendar blockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7);
      SimpleBlockoutTrigger blockOutTrigger = new SimpleBlockoutTrigger("blockOut", blockOutStartDate.getTime(), null, //$NON-NLS-1$
          -1, TIME.WEEK.time * 2, TIME.HOUR.time * 2);

      Calendar scheduleStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7, 1, 0, 0);
      SimpleTrigger trueScheduleTrigger = new SimpleTrigger("trueSchedule", "SCHEDULES", scheduleStartDate.getTime(),
          null, -1, TIME.WEEK.time);
      scheduleJob(trueScheduleTrigger);

      SimpleTrigger falseScheduleTrigger = new SimpleTrigger("falseSchedule", "SCHEDULES", scheduleStartDate.getTime(),
          null, -1, TIME.WEEK.time * 2);
      scheduleJob(falseScheduleTrigger);

      this.blockOutManager.addBlockout(blockOutTrigger);
      assertTrue(this.blockOutManager.willFire(trueScheduleTrigger));
      assertFalse(this.blockOutManager.willFire(falseScheduleTrigger));

      // Clean up
      deleteJob(falseScheduleTrigger);
      deleteJob(trueScheduleTrigger);

    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#shouldFireNow()}.
   */
  @Test
  public void testShouldFireNow() {
    try {
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
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test method for {@link org.pentaho.platform.scheduler2.blockout.DefaultBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)}.
   */
  @Test
  public void testWillBlockSchedules() {
    try {
      Calendar trueBlockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7);
      SimpleBlockoutTrigger trueBlockOutTrigger = new SimpleBlockoutTrigger(
          "blockOut", trueBlockOutStartDate.getTime(), null, //$NON-NLS-1$
          -1, TIME.WEEK.time * 2, TIME.HOUR.time * 2);

      Calendar falseBlockOutStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 8);
      SimpleBlockoutTrigger falseBlockOutTrigger = new SimpleBlockoutTrigger(
          "blockOut", falseBlockOutStartDate.getTime(), null, //$NON-NLS-1$
          -1, TIME.WEEK.time * 2, TIME.HOUR.time * 2);

      Calendar scheduleStartDate = new GregorianCalendar(2013, Calendar.JANUARY, 7, 1, 0, 0);
      SimpleTrigger scheduleTrigger = new SimpleTrigger("trueSchedule", "SCHEDULES", scheduleStartDate.getTime(), null,
          -1, TIME.WEEK.time);
      scheduleJob(scheduleTrigger);

      assertEquals(1, this.blockOutManager.willBlockSchedules(trueBlockOutTrigger).size());
      assertEquals(0, this.blockOutManager.willBlockSchedules(falseBlockOutTrigger).size());

      // Clean up
      deleteJob(scheduleTrigger);

    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Schedules a non-block out job
   * @param scheduleTrigger
   */
  private void scheduleJob(SimpleTrigger scheduleTrigger) {
    try {
      JobDetail jd = new JobDetail(scheduleTrigger.getName(), scheduleTrigger.getGroup(), TestJob.class);
      scheduleTrigger.setJobName(jd.getName());
      scheduleTrigger.setJobGroup(jd.getGroup());

      QuartzScheduler qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
      qs.getQuartzScheduler().scheduleJob(jd, scheduleTrigger);

    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Deletes a job from the scheduler
   * @param scheduleTrigger
   */
  private void deleteJob(SimpleTrigger scheduleTrigger) {
    try {
      QuartzScheduler qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
      qs.getQuartzScheduler().deleteJob(scheduleTrigger.getName(), scheduleTrigger.getGroup());
    } catch (SchedulerException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private class TestJob implements Job {

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
      // NoOp
    }
  }

}
