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

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;


/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManager implements IBlockoutManager {
  
  Scheduler scheduler = null;
  
  public DefaultBlockoutManager() throws SchedulerException {
    super();
    QuartzScheduler qs = (QuartzScheduler)PentahoSystem.get(IScheduler.class, "IScheduler2", null); //$NON-NLS-1$
    scheduler = qs.getQuartzScheduler();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#addBlockout(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)
   */
  @Override
  public void addBlockout(IBlockoutTrigger blockout) throws SchedulerException {
    if (!(blockout instanceof Trigger)) {
      throw new SchedulerException(Messages.getInstance().getString("DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE"));
    }
    Trigger blockoutTrigger = (Trigger)blockout;
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
    for (int i=0; i<blockedTriggerName.length; i++) {
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
      throw new SchedulerException(Messages.getInstance().getString("DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE"));
    }
    Trigger newBlockoutTrigger = (Trigger) newBlockout;
    IBlockoutTrigger oldBlockout = null;
    try {
      oldBlockout = getBlockout(blockoutName);
      if (oldBlockout == null) {
        throw new SchedulerException(Messages.getInstance().getString("DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE", blockoutName));
      }
    } catch (SchedulerException ex) {
      throw new SchedulerException(Messages.getInstance().getString("DefaultBlockoutManager.ERROR_0001_WRONG_BLOCKER_TYPE", blockoutName), ex);
    }
    
    deleteBlockout(blockoutName);
    Trigger oldBlockoutTrigger = (Trigger)oldBlockout;
    JobDetail jd = scheduler.getJobDetail(oldBlockoutTrigger.getJobName(), oldBlockoutTrigger.getJobGroup());
    
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
  public int willFire(Trigger scheduleTrigger) {
    // TODO Auto-generated method stub
    return 0;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#shouldFireNow()
   */
  @Override
  public boolean shouldFireNow() {
    // TODO Auto-generated method stub
    return true;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.SimpleBlockoutTrigger)
   */
  @Override
  public boolean willBlockSchedules(IBlockoutTrigger testBlockout) {
    // TODO Auto-generated method stub
    return false;
  }

}
