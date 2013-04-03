package org.pentaho.platform.scheduler2.blockout;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoBlockoutManager implements IBlockoutManager {

  private IScheduler scheduler;

  public PentahoBlockoutManager() {
    this.scheduler = PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
  }

  @Override
  public IJobTrigger getBlockOut(String blockOutJobId) {
    try {
      Job blockOutJob = this.scheduler.getJob(blockOutJobId);
      IJobTrigger blockOutJobTrigger = blockOutJob.getJobTrigger();
      blockOutJobTrigger.setDuration((Long) blockOutJob.getJobParams().get(DURATION_PARAM));
      return blockOutJobTrigger;
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<Job> getBlockOutJobs(final Boolean canAdminister) {
    try {
      List<Job> jobs = scheduler.getJobs(new IJobFilter() {
        public boolean accept(Job job) {
          if (canAdminister && BLOCK_OUT_JOB_NAME.equals(job.getJobName())) {
            job.getJobTrigger().setDuration((Long) job.getJobParams().get(DURATION_PARAM));
            return true;
          }
          return false;
        }
      });
      return jobs;

    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public boolean willFire(IJobTrigger scheduleTrigger) {

    return BlockoutManagerUtil.willFire(scheduleTrigger, getBlockOutJobTriggers(), this.scheduler);
  }

  @Override
  public boolean shouldFireNow() {
    return BlockoutManagerUtil.shouldFireNow(getBlockOutJobTriggers(), this.scheduler);
  }

  @Override
  public List<IJobTrigger> willBlockSchedules(IJobTrigger testBlockOutJobTrigger) {
    List<IJobTrigger> blockedSchedules = new ArrayList<IJobTrigger>();

    List<Job> scheduledJobs = new ArrayList<Job>();
    try {
      scheduledJobs = this.scheduler.getJobs(new IJobFilter() {

        @Override
        public boolean accept(Job job) {
          return !BLOCK_OUT_JOB_NAME.equals(job.getJobName());
        }
      });
    } catch (SchedulerException e) {
      throw new RuntimeException(e);
    }

    // Loop over trigger group names
    for (Job scheduledJob : scheduledJobs) {

      // Add schedule to list if block out conflicts at all
      if (BlockoutManagerUtil.willBlockSchedule(scheduledJob.getJobTrigger(), testBlockOutJobTrigger, this.scheduler)) {
        blockedSchedules.add(scheduledJob.getJobTrigger());
      }
    }

    return blockedSchedules;
  }

  @Override
  public boolean isPartiallyBlocked(IJobTrigger scheduleJobTrigger) {
    return BlockoutManagerUtil.isPartiallyBlocked(scheduleJobTrigger, getBlockOutJobTriggers(), this.scheduler);
  }

  private List<IJobTrigger> getBlockOutJobTriggers() {
    List<IJobTrigger> blockOutJobTriggers = new ArrayList<IJobTrigger>();

    for (Job blockOutJob : getBlockOutJobs(true)) {
      blockOutJobTriggers.add(blockOutJob.getJobTrigger());
    }

    return blockOutJobTriggers;
  }

}
