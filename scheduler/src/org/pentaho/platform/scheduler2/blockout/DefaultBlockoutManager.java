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

import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.quartz.CronTrigger;
import org.quartz.DateIntervalTrigger;
import org.quartz.DateIntervalTrigger.IntervalUnit;
import org.quartz.JobDetail;
import org.quartz.NthIncludedDayTrigger;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;

/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManager implements IBlockoutManager {

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

  protected IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);

  QuartzScheduler qs = null;

  public static final String ERR_WRONG_BLOCKER_TYPE = "DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE"; //$NON-NLS-1$

  public static final String ERR_CANT_PARSE_RECURRENCE_INTERVAL = "DefaultBlockoutManager.ERROR_0003_CANT_PARSE_RECURRENCE_INTERVAL"; //$NON-NLS-1$

  public DefaultBlockoutManager() throws SchedulerException {
    super();
    qs = (QuartzScheduler) PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
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
    blockoutTrigger.setGroup(BLOCK_GROUP);
    JobDetail jd = new JobDetail(blockoutTrigger.getName(), BLOCK_GROUP, BlockoutJob.class);
    blockoutTrigger.setJobName(jd.getName());
    blockoutTrigger.setJobGroup(jd.getGroup());
    qs.getQuartzScheduler().scheduleJob(jd, blockoutTrigger);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockout(java.lang.String)
   */
  @Override
  public IBlockoutTrigger getBlockout(String blockoutName) throws SchedulerException {
    return (IBlockoutTrigger) qs.getQuartzScheduler().getTrigger(blockoutName, BLOCK_GROUP);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockouts()
   */
  @Override
  @Deprecated
  public IBlockoutTrigger[] getBlockouts() throws SchedulerException {
    String[] blockedTriggerName = qs.getQuartzScheduler().getTriggerNames(BLOCK_GROUP);
    IBlockoutTrigger[] blockTriggers = new IBlockoutTrigger[blockedTriggerName.length];
    for (int i = 0; i < blockedTriggerName.length; i++) {
      blockTriggers[i] = (IBlockoutTrigger) qs.getQuartzScheduler().getTrigger(blockedTriggerName[i], BLOCK_GROUP);
    }
    return blockTriggers;
  }

  public List<Job> getBlockoutJobs(final Boolean canAdminister) {
    try {
      IPentahoSession session = PentahoSessionHolder.getSession();
      final String principalName = session.getName(); // this authentication wasn't matching with the job user name, changed to get name via the current session
      List<Job> jobs = qs.getJobs(new IJobFilter() {
        public boolean accept(Job job) {
          if (canAdminister) {
            return IBlockoutManager.BLOCK_GROUP.equals(job.getGroupName());
          }
          return principalName.equals(job.getUserName());
        }
      });
      return jobs;
    } catch (org.pentaho.platform.api.scheduler2.SchedulerException e) {
      throw new RuntimeException(e);
    }
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
    JobDetail jd = qs.getQuartzScheduler().getJobDetail(oldBlockoutTrigger.getJobName(),
        oldBlockoutTrigger.getJobGroup());
    deleteBlockout(blockoutName);

    newBlockoutTrigger.setJobName(jd.getName());
    newBlockoutTrigger.setJobGroup(jd.getGroup());
    qs.getQuartzScheduler().scheduleJob(jd, newBlockoutTrigger);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#deleteBlockout(java.lang.String)
   */
  @Override
  public boolean deleteBlockout(String blockoutName) throws SchedulerException {
    return qs.getQuartzScheduler().deleteJob(blockoutName, BLOCK_GROUP);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#willFire(org.quartz.Trigger)
   */
  @Override
  public boolean willFire(Trigger scheduleTrigger) throws SchedulerException {
    List<Date> fireTimes = getFireTimes(scheduleTrigger);

    for (IBlockoutTrigger blockOut : getBlockouts()) {

      // We must verify further if the schedule is blocked completely or if it will fire
      if (willBlockSchedule(scheduleTrigger, blockOut)) {

        Trigger blockOutTrigger = (Trigger) blockOut;

        // If recurrence intervals are the same, it will never fire
        if (!isComplexTrigger(blockOutTrigger) && !isComplexTrigger(scheduleTrigger)
            && getRecurrenceInterval(blockOutTrigger) == getRecurrenceInterval(scheduleTrigger)) {
          return false;
        }

        // Loop through fire times and verify whether block out is blocking the schedule completely
        boolean scheduleCompletelyBlocked = true;
        for (Date fireTime : fireTimes) {
          if (!(scheduleCompletelyBlocked = willBlockDate(blockOut, fireTime))) {
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
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#isPartiallyBlocked
   */
  @Override
  public boolean isPartiallyBlocked(Trigger scheduleTrigger) throws SchedulerException {

    // Loop through blockout triggers
    for (IBlockoutTrigger blockOut : getBlockouts()) {
      if (willBlockSchedule(scheduleTrigger, blockOut)) {
        return true;
      }
    }

    return false;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#shouldFireNow(org.quartz.Trigger)
   */
  @Override
  public boolean shouldFireNow() throws SchedulerException {

    Date currentTime = new Date(System.currentTimeMillis());
    for (IBlockoutTrigger blockOut : getBlockouts()) {

      if (willBlockDate(blockOut, currentTime)) {
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
    for (String groupName : this.qs.getQuartzScheduler().getTriggerGroupNames()) {

      // Skip block outs
      if (BLOCK_GROUP.equals(groupName)) {
        continue;
      }

      // Loop over job names within group
      for (String jobName : this.qs.getQuartzScheduler().getJobNames(groupName)) {
        Trigger schedule = this.qs.getQuartzScheduler().getTrigger(jobName, groupName);

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
    Trigger blockOutTrigger = (Trigger) blockOut;

    boolean isScheduleTriggerComplex = isComplexTrigger(scheduleTrigger);
    boolean isBlockOutTriggerComplex = isComplexTrigger(blockOutTrigger);

    // Both Schedule and BlockOut are complex
    if (isScheduleTriggerComplex && isBlockOutTriggerComplex) {
      return willComplexBlockOutBlockComplexScheduleTrigger(blockOut, scheduleTrigger);
    }

    // Complex Schedule Trigger
    if (isScheduleTriggerComplex) {
      return willBlockComplexScheduleTrigger(scheduleTrigger, blockOut);
    }

    // Complex BlockOut Trigger
    if (isBlockOutTriggerComplex) {
      return willComplexBlockOutTriggerBlockSchedule(blockOut, scheduleTrigger);
    }

    /*
     * Both blockOut and schedule triggers are simple. Continue with mathematical calculations
     */
    long blockOutRecurrence = getRecurrenceInterval(blockOutTrigger);
    long scheduleRecurrence = getRecurrenceInterval(scheduleTrigger);

    for (int i = 0; i < 1000; i++) {
      double shiftBy = (blockOutRecurrence - scheduleRecurrence) * i / (double) scheduleRecurrence;

      double x1 = (blockOutTrigger.getStartTime().getTime() - scheduleTrigger.getStartTime().getTime())
          / (double) scheduleRecurrence + shiftBy;

      double x2 = (blockOutTrigger.getStartTime().getTime() + blockOut.getBlockDuration() - scheduleTrigger
          .getStartTime().getTime()) / (double) scheduleRecurrence + shiftBy;

      if (hasIntBetween(x1, x2)) {

        int xShift = (int) Math.ceil(x1 < x2 ? x1 : x2);

        long scheduleDate = scheduleTrigger.getStartTime().getTime() + scheduleRecurrence * (i + xShift);
        long blockOutStartDate = blockOutTrigger.getStartTime().getTime() + blockOutRecurrence * i;

        // Test intersection of dates fall within range
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
   * @param trigger
   *        {@link Trigger}
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @return whether the {@link IBlockoutTrigger} will conflict at all with the complex {@link Trigger}s
   * @throws SchedulerException
   */
  private boolean willBlockComplexScheduleTrigger(Trigger trigger, IBlockoutTrigger blockOut) throws SchedulerException {

    for (Date fireTime : getFireTimes(trigger)) {
      if (willBlockDate(blockOut, fireTime)) {
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
   * @throws SchedulerException 
   */
  private boolean willBlockDate(IBlockoutTrigger blockOut, Date date) throws SchedulerException {
    // S + Rx <= d <= S + Rx + D
    Trigger blockOutTrigger = (Trigger) blockOut;

    // Out of range of block out
    if (date.before(blockOutTrigger.getStartTime())
        || (blockOutTrigger.getEndTime() != null && date.after(blockOutTrigger.getEndTime()))) {
      return false;
    }

    if (isComplexTrigger(blockOutTrigger)) {
      return willComplexBlockOutTriggerBlockDate(blockOut, date);
    }

    long blockOutRecurrenceInterval = getRecurrenceInterval(blockOutTrigger);

    double x1 = (date.getTime() - blockOutTrigger.getStartTime().getTime()) / (double) blockOutRecurrenceInterval;
    double x2 = (date.getTime() - (blockOutTrigger.getStartTime().getTime() + blockOut.getBlockDuration()))
        / (double) blockOutRecurrenceInterval;

    return hasPositiveIntBetween(x1, x2);
  }

  /**
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @param scheduleTrigger 
   *        {@link Trigger}
   * @return whether the complex {@link IBlockoutTrigger} will block the scheduled {@link Trigger}
   * @throws SchedulerException
   */
  private boolean willComplexBlockOutTriggerBlockSchedule(IBlockoutTrigger blockOut, Trigger scheduleTrigger)
      throws SchedulerException {
    Trigger blockOutTrigger = (Trigger) blockOut;

    // Short circuit if schedule trigger after end time of block out trigger
    if ((blockOutTrigger.getEndTime() != null && scheduleTrigger.getStartTime().after(blockOutTrigger.getEndTime()))
        || (scheduleTrigger.getEndTime() != null && blockOutTrigger.getStartTime().after(scheduleTrigger.getEndTime()))) {
      return false;
    }

    long duration = blockOut.getBlockDuration();

    // Loop through fire times of block out trigger
    for (Date blockOutStartDate : getFireTimes(blockOutTrigger)) {
      Date blockOutEndDate = new Date(blockOutStartDate.getTime() + duration);

      if (willBlockOutRangeBlockSimpleTrigger(blockOutStartDate, blockOutEndDate, scheduleTrigger)) {
        return true;
      }
    }

    return false;
  }

  /**
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @param scheduleTrigger
   *        {@link Trigger}
   * @return whether a complex BlockOut trigger will block a complex Schedule Trigger
   * @throws SchedulerException
   */
  private boolean willComplexBlockOutBlockComplexScheduleTrigger(IBlockoutTrigger blockOut, Trigger scheduleTrigger)
      throws SchedulerException {
    Trigger blockOutTrigger = (Trigger) blockOut;
    List<Date> blockOutFireTimes = getFireTimes(blockOutTrigger);

    int iStart = 0;
    for (Date scheduleFireTime : getFireTimes(scheduleTrigger)) {
      for (int i = iStart; i < blockOutFireTimes.size(); i++) {
        Date blockOutStartDate = blockOutFireTimes.get(i);

        // BlockOut start date after scheduled fire time
        if (blockOutStartDate.after(scheduleFireTime)) {
          iStart = i;
          break;
        }

        Date blockOutEndDate = new Date(blockOutStartDate.getTime() + blockOut.getBlockDuration());

        if (isDateIncludedInRangeInclusive(blockOutStartDate, blockOutEndDate, scheduleFireTime)) {
          return true;
        }
      }

    }

    return false;
  }

  /**
   * @param blockOut
   *        {@link IBlockoutTrigger}
   * @param date
   *        {@link Date}
   * @return whether the complex {@link IBlockoutTrigger} blocks the date
   * @throws SchedulerException
   */
  private boolean willComplexBlockOutTriggerBlockDate(IBlockoutTrigger blockOut, Date date) throws SchedulerException {
    Trigger blockOutTrigger = (Trigger) blockOut;

    // Short circuit if date does not fall within a valid start/end date range
    if (date.before(blockOutTrigger.getStartTime())
        || (blockOutTrigger.getEndTime() != null && date.after(blockOutTrigger.getEndTime()))) {
      return false;
    }

    long blockOutDuration = blockOut.getBlockDuration();
    for (Date blockOutStartDate : getFireTimes(blockOutTrigger)) {

      // Block out date has passed the date being tested
      if (blockOutStartDate.after(date)) {
        break;
      }

      Date blockOutEndDate = new Date(blockOutStartDate.getTime() + blockOutDuration);

      // Date falls within inclusive block out range
      if (isDateIncludedInRangeInclusive(blockOutStartDate, blockOutEndDate, date)) {
        return true;
      }
    }

    return false;
  }

  /**
   * @param startBlockOutRange
   * @param endBlockOutRange
   * @param scheduleTrigger
   * @return whether a block out range will block a trigger
   */
  private boolean willBlockOutRangeBlockSimpleTrigger(Date startBlockOutRange, Date endBlockOutRange,
      Trigger scheduleTrigger) {
    // ( S1 - S ) / R <= x <= ( S2 - S ) / R 

    double recurrence = getRecurrenceInterval(scheduleTrigger);
    double x1 = (startBlockOutRange.getTime() - scheduleTrigger.getStartTime().getTime()) / recurrence;
    double x2 = (endBlockOutRange.getTime() - scheduleTrigger.getStartTime().getTime()) / recurrence;

    return hasPositiveIntBetween(x1, x2);
  }

  /**
   * @param trigger
   *        {@link Trigger}
   * @return whether the {@link Trigger} is one that has a complex recurrence interval 
   */
  private static boolean isComplexTrigger(Trigger trigger) {
    return trigger instanceof CronTrigger
        || trigger instanceof NthIncludedDayTrigger
        || (trigger instanceof DateIntervalTrigger && ((DateIntervalTrigger) trigger).getRepeatIntervalUnit().equals(
            IntervalUnit.MONTH));
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
   * @param x1 double
   * @param x2 double
   * @return whether there is a positive integer between x1 and x2 
   */
  private static boolean hasPositiveIntBetween(double x1, double x2) {
    return (x1 < x2 ? x2 >= 0 : x1 >= 0) && hasIntBetween(x1, x2);
  }

  /**
   * @param trigger
   *        {@link Trigger}
   * @return the long interval of recurrence for a {@link Trigger}
   */
  private static long getRecurrenceInterval(Trigger trigger) {

    if (trigger instanceof SimpleTrigger) {
      return ((SimpleTrigger) trigger).getRepeatInterval();

    } else if (trigger instanceof DateIntervalTrigger) {
      DateIntervalTrigger dateIntervalTrigger = (DateIntervalTrigger) trigger;

      long intervalUnit = 0;
      switch (dateIntervalTrigger.getRepeatIntervalUnit()) {
        case SECOND:
          intervalUnit = TIME.SECOND.time;
          break;
        case MINUTE:
          intervalUnit = TIME.MINUTE.time;
          break;
        case HOUR:
          intervalUnit = TIME.HOUR.time;
          break;
        case DAY:
          intervalUnit = TIME.DAY.time;
          break;
        case WEEK:
          intervalUnit = TIME.WEEK.time;
          break;
        case YEAR:
          intervalUnit = TIME.YEAR.time;
          break;

        default:
          throw new RuntimeException(Messages.getInstance().getString(ERR_CANT_PARSE_RECURRENCE_INTERVAL));
      }

      return dateIntervalTrigger.getRepeatInterval() * intervalUnit;
    }

    throw new RuntimeException(Messages.getInstance().getString(ERR_CANT_PARSE_RECURRENCE_INTERVAL));
  }

  /**
   * @param trigger
   *        {@link Trigger}
   * @return List of Dates representing times the {@link Trigger} will fire for the next 4 years, as to include leap years
   * @throws SchedulerException
   */
  @SuppressWarnings("unchecked")
  private List<Date> getFireTimes(Trigger trigger) throws SchedulerException {

    Date startDate = trigger.getStartTime();
    Date endDate = new Date(startDate.getTime() + 4 * TIME.YEAR.time);

    // Calculate fire times for next 4 years 
    return TriggerUtils.computeFireTimesBetween(trigger,
        this.qs.getQuartzScheduler().getCalendar(trigger.getCalendarName()), startDate, endDate);
  }

  /**
   * @param dateRangeStart
   *        {@link Date} start of range
   * @param dateRangeEnd
   *        {@link Date} end of range
   * @param date
   *        {@link Date}
   * @return whether the date falls within the date inclusive date range
   */
  private boolean isDateIncludedInRangeInclusive(Date dateRangeStart, Date dateRangeEnd, Date date) {
    long dateTime = date.getTime();
    return dateRangeStart.getTime() <= dateTime && dateTime <= dateRangeEnd.getTime();
  }
}
