package org.pentaho.platform.scheduler2.blockout;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.scheduler2.IBlockOutManager;
import org.pentaho.platform.api.scheduler2.IJobFilter;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.Job;
import org.pentaho.platform.api.scheduler2.SchedulerException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PentahoBlockOutManager implements IBlockOutManager {

  private IScheduler scheduler;

  public PentahoBlockOutManager() {
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
          if (canAdminister) {
            return BLOCK_OUT_JOB_NAME.equals(job.getJobName());
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

    return BlockOutManagerUtil.willFire(scheduleTrigger, getBlockOutJobTriggers(), this.scheduler);
  }

  @Override
  public boolean shouldFireNow() {
    return BlockOutManagerUtil.shouldFireNow(getBlockOutJobTriggers(), this.scheduler);
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
      if (BlockOutManagerUtil.willBlockSchedule(scheduledJob.getJobTrigger(), testBlockOutJobTrigger, this.scheduler)) {
        blockedSchedules.add(scheduledJob.getJobTrigger());
      }
    }

    return blockedSchedules;
  }

  @Override
  public boolean isPartiallyBlocked(IJobTrigger scheduleJobTrigger) {
    return BlockOutManagerUtil.isPartiallyBlocked(scheduleJobTrigger, getBlockOutJobTriggers(), this.scheduler);
  }

  private List<IJobTrigger> getBlockOutJobTriggers() {
    List<IJobTrigger> blockOutJobTriggers = new ArrayList<IJobTrigger>();

    for (Job blockOutJob : getBlockOutJobs(true)) {
      IJobTrigger blockOutTrigger = blockOutJob.getJobTrigger();
      blockOutTrigger.setDuration((Long) blockOutJob.getJobParams().get(DURATION_PARAM));
      blockOutJobTriggers.add(blockOutTrigger);
    }

    return blockOutJobTriggers;
  }

}
