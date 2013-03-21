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
 * @created Mar 12, 2013 
 * @author wseyler
 */

package org.pentaho.platform.scheduler2.blockout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManager implements IBlockoutManager {

  Scheduler scheduler = null;

  public static final String ERR_WRONG_BLOCKER_TYPE = "DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE"; //$NON-NLS-1$

  public DefaultBlockoutManager() throws SchedulerException {
    super();
    QuartzScheduler qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
    scheduler = qs.getQuartzScheduler();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#addBlockout(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)
   */
  @Override
  public void addBlockout(IBlockoutTrigger blockout) throws SchedulerException {
    if (!(blockout instanceof Trigger)) {
      throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE));
    }
    Trigger blockoutTrigger = (Trigger) blockout;
    JobDetail jd = new JobDetail(blockoutTrigger.getName(), BLOCK_GROUP, BlockoutJob.class);
    blockoutTrigger.setJobName(jd.getName());
    blockoutTrigger.setJobGroup(jd.getGroup());
    scheduler.scheduleJob(jd, blockoutTrigger);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockout(java.lang.String)
   */
  @Override
  public IBlockoutTrigger getBlockout(String blockoutName) throws SchedulerException {
    return (IBlockoutTrigger) scheduler.getTrigger(blockoutName, BLOCK_GROUP);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockouts()
   */
  @Override
  public IBlockoutTrigger[] getBlockouts() throws SchedulerException {
    String[] blockedTriggerName = scheduler.getTriggerNames(BLOCK_GROUP);
    IBlockoutTrigger[] blockTriggers = new IBlockoutTrigger[blockedTriggerName.length];
    for (int i = 0; i < blockedTriggerName.length; i++) {
      blockTriggers[i] = (IBlockoutTrigger) scheduler.getTrigger(blockedTriggerName[i], BLOCK_GROUP);
    }
    return blockTriggers;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#updateBlockout(java.lang.String, org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)
   */
  @Override
  public void updateBlockout(String blockoutName, IBlockoutTrigger newBlockout) throws SchedulerException {
    if (!(newBlockout instanceof Trigger)) {
      throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE));
    }
    Trigger newBlockoutTrigger = (Trigger) newBlockout;
    IBlockoutTrigger oldBlockout = null;
    try {
      oldBlockout = getBlockout(blockoutName);
      if (oldBlockout == null) {
        throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE, blockoutName));
      }
    } catch (SchedulerException ex) {
      throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE, blockoutName), ex);
    }

    Trigger oldBlockoutTrigger = (Trigger) oldBlockout;
    JobDetail jd = scheduler.getJobDetail(oldBlockoutTrigger.getJobName(), oldBlockoutTrigger.getJobGroup());
    deleteBlockout(blockoutName);

    newBlockoutTrigger.setJobName(jd.getName());
    newBlockoutTrigger.setJobGroup(jd.getGroup());
    scheduler.scheduleJob(jd, newBlockoutTrigger);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#deleteBlockout(java.lang.String)
   */
  @Override
  public boolean deleteBlockout(String blockoutName) throws SchedulerException {
    return scheduler.deleteJob(blockoutName, BLOCK_GROUP);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#willFire(org.quartz.Trigger)
   */
  @Override
  public boolean willFire(Trigger scheduleTrigger) throws SchedulerException {
    long year = 1000L * 60L * 60L * 24L * 365L;

    for (IBlockoutTrigger blockOut : getBlockouts()) {

      // We must verify further if the schedule is blocked completely or if it will fire
      if (willBlockSchedule(scheduleTrigger, blockOut)) {

        // If recurrence intervals are the same, it will never fire
        if (getRecurrenceInterval((Trigger) blockOut) == getRecurrenceInterval(scheduleTrigger)) {
          return false;
        }

        // Calculate fire times for next year
        Date startDate = new Date(System.currentTimeMillis());
        Date endDate = new Date(startDate.getTime() + year);
        List fireTimes = TriggerUtils.computeFireTimesBetween(scheduleTrigger,
            this.scheduler.getCalendar(scheduleTrigger.getCalendarName()), startDate, endDate);

        // Loop through fire times and verify whether block out is blocking the schedule completely
        boolean scheduleCompletelyBlocked = true;
        for (Object fireTime : fireTimes) {
          if (!(scheduleCompletelyBlocked = willBlockDate(blockOut, (Date) fireTime))) {
            break;
          }
        }

        // Return false if after n iterations
        if (scheduleCompletelyBlocked) {
          return false;
        }
      }
    }

    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#shouldFireNow()
   */
  @Override
  public boolean shouldFireNow() throws SchedulerException {

    long currentTime = System.currentTimeMillis();
    for (IBlockoutTrigger blockOut : getBlockouts()) {

      if (!(blockOut instanceof Trigger)) {
        throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE));
      }

      Trigger blockOutTrigger = (Trigger) blockOut;
      long lastFireTime = blockOutTrigger.getPreviousFireTime() != null ? blockOutTrigger.getPreviousFireTime()
          .getTime() : blockOutTrigger.getStartTime().getTime();
      long endLastFireTime = lastFireTime + blockOut.getBlockDuration();

      if (lastFireTime <= currentTime && currentTime <= endLastFireTime) {
        return false;
      }
    }

    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)
   */
  @Override
  public List<Trigger> willBlockSchedules(IBlockoutTrigger blockOut) throws SchedulerException {

    List<Trigger> blockedSchedules = new ArrayList<Trigger>();

    // Loop over trigger group names
    for (String groupName : this.scheduler.getTriggerGroupNames()) {

      // Skip block outs
      if (BLOCK_GROUP.equals(groupName)) {
        continue;
      }

      // Loop over job names within group
      for (String jobName : this.scheduler.getJobNames(groupName)) {
        Trigger schedule = this.scheduler.getTrigger(jobName, groupName);

        // Add schedule to list if block out conflicts at all
        if (willBlockSchedule(schedule, blockOut)) {
          blockedSchedules.add(schedule);
        }
      }
    }

    return blockedSchedules;
  }

  /**
   * @param scheduleTrigger
   *        {@link Trigger} schedule to test
   * @param blockout
   *        {@link IBlockoutTrigger} to verify against the trigger
   * @return whether an {@link IBlockoutTrigger} will block {@link Trigger} schedule at all in the future
   * @throws SchedulerException
   */
  private boolean willBlockSchedule(Trigger scheduleTrigger, IBlockoutTrigger blockOut) throws SchedulerException {

    // Perform special willBlockSchedule for CronTrigger
    if (scheduleTrigger instanceof CronTrigger) {
      return willBlockSchedule((CronTrigger) scheduleTrigger, blockOut);
    }

    // Protect against non-trigger implementations
    if (!(blockOut instanceof Trigger)) {
      throw new SchedulerException(Messages.getInstance().getString(ERR_WRONG_BLOCKER_TYPE));
    }

    Trigger blockOutTrigger = (Trigger) blockOut;

    long blockOutRecurrence = getRecurrenceInterval(blockOutTrigger);
    long scheduleRecurrence = getRecurrenceInterval(scheduleTrigger);

    for (int i = 0; i < 1000; i++) {
      double shiftBy = (blockOutRecurrence - scheduleRecurrence) * i / (double) scheduleRecurrence;

      double x1 = (blockOutTrigger.getStartTime().getTime() - scheduleTrigger.getStartTime().getTime())
          / (double) scheduleRecurrence + shiftBy;

      double x2 = (blockOutTrigger.getStartTime().getTime() + blockOut.getBlockDuration() - scheduleTrigger
          .getStartTime().getTime()) / (double) scheduleRecurrence + shiftBy;

      // x2 has to be positive, as it indicates an xShift to the "future"
      if (hasIntBetween(x1, x2) && (x1 < x2 ? x2 >= 0 : x1 >= 0)) {

        int xShift = (int) Math.ceil(x1 < x2 ? x1 : x2);

        long scheduleDate = scheduleTrigger.getStartTime().getTime() + scheduleRecurrence * (i + xShift);
        long blockOutStartDate = blockOutTrigger.getStartTime().getTime() + blockOutRecurrence * i;

        // Ensure intersection dates fall within range
        if (scheduleTrigger.getStartTime().getTime() <= scheduleDate
            && (scheduleTrigger.getEndTime() == null || scheduleDate <= scheduleTrigger.getEndTime().getTime())
            && blockOutTrigger.getStartTime().getTime() <= blockOutStartDate
            && (blockOutTrigger.getEndTime() == null || blockOutStartDate <= blockOutTrigger.getEndTime().getTime())) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Due to the fact that {@link CronTrigger}s can be so complicated and, therefore, a 
   * recurrence interval might not be able to be determined, we must perform a brute force 
   * approach to calculate a year of fire times for the {@link CronTrigger} and ensure that 
   * they do/do not conflict with the {@link IBlockoutTrigger}
   * 
   * @param cronTrigger
   *        {@link CronTrigger}
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @return whether the {@link IBlockoutTrigger} will conflict at all with the {@link CronTrigger}
   * @throws SchedulerException
   */
  private boolean willBlockSchedule(CronTrigger cronTrigger, IBlockoutTrigger blockOut) throws SchedulerException {
    long year = 1000L * 60L * 60L * 24L * 365L;

    Date startDate = new Date(System.currentTimeMillis());
    Date endDate = new Date(startDate.getTime() + year);
    List<?> fireTimes = TriggerUtils.computeFireTimesBetween(cronTrigger,
        this.scheduler.getCalendar(cronTrigger.getCalendarName()), startDate, endDate);

    for (Object fireTime : fireTimes) {
      if (willBlockDate(blockOut, (Date) fireTime)) {
        return true;
      }
    }

    return false;
  }

  /**
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @param date
   *        {@link Date}
   * @return whether the {@link Date} is within the range of the {@link IBlockoutTrigger}
   */
  private static boolean willBlockDate(IBlockoutTrigger blockOut, Date date) {
    // S + Rx <= d <= S + Rx + D
    Trigger blockOutTrigger = (Trigger) blockOut;

    long blockOutRecurrenceInterval = getRecurrenceInterval(blockOutTrigger);

    double x1 = (date.getTime() - blockOutTrigger.getStartTime().getTime()) / (double) blockOutRecurrenceInterval;
    double x2 = (date.getTime() - blockOutTrigger.getStartTime().getTime() + blockOut.getBlockDuration())
        / (double) blockOutRecurrenceInterval;

    return x2 >= 0 && hasIntBetween(x1, x2);
  }

  /**
   * @param x1 double
   * @param x2 double
   * @return whether an {@link Integer} exists between x1 and x2
   */
  private static boolean hasIntBetween(double x1, double x2) {
    double ceilX = Math.ceil(x1);
    double floorX = Math.floor(x2);

    if (x1 > x2) {
      ceilX = Math.ceil(x2);
      floorX = Math.floor(x1);
    }

    return (floorX - ceilX) >= 0;
  }

  /**
   * @param trigger
   *        {@link Trigger}
   * @return the long interval of recurrence for a {@link Trigger}
   */
  private static long getRecurrenceInterval(Trigger trigger) {
    long recurrenceInterval = 0;

    if (trigger instanceof SimpleTrigger) {
      recurrenceInterval = ((SimpleTrigger) trigger).getRepeatInterval();
    } else {
      recurrenceInterval = trigger.getNextFireTime().getTime()
          - (trigger.getPreviousFireTime() != null ? trigger.getPreviousFireTime().getTime() : trigger.getStartTime()
              .getTime());
    }

    return recurrenceInterval;
  }

}
