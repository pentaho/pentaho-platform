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
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;


/**
 * @author wseyler
 *
 */
public class DefaultBlockoutManager implements IBlockoutManager {
  public static final String BLOCK_GROUP = "BLOCK_GROUP"; //$NON-NLS-1$
  
  Scheduler scheduler = null;
  
  public DefaultBlockoutManager() throws SchedulerException {
    super();
    SchedulerFactory sf = new StdSchedulerFactory();
    scheduler = sf.getScheduler();  // Return the default scheduler;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#addBlockout(org.pentaho.platform.scheduler2.blockout.BlockoutTrigger)
   */
  @Override
  public void addBlockout(BlockoutTrigger blockout) throws SchedulerException {
    JobDetail jd = new JobDetail(blockout.getName(), BLOCK_GROUP, BlockoutJob.class);
    blockout.setJobName(jd.getName());
    blockout.setJobGroup(jd.getGroup());
    scheduler.scheduleJob(jd, blockout);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockout(java.lang.String)
   */
  @Override
  public BlockoutTrigger getBlockout(String blockoutName) throws SchedulerException {
    return (BlockoutTrigger) scheduler.getTrigger(blockoutName, BLOCK_GROUP);
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#getBlockouts()
   */
  @Override
  public BlockoutTrigger[] getBlockouts() throws SchedulerException {
    String[] blockedTriggerName = scheduler.getTriggerNames(BLOCK_GROUP);
    BlockoutTrigger[] blockTriggers = new BlockoutTrigger[blockedTriggerName.length];
    for (int i=0; i<blockedTriggerName.length; i++) {
      blockTriggers[i] = (BlockoutTrigger) scheduler.getTrigger(blockedTriggerName[i], BLOCK_GROUP);
    }
    return blockTriggers;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#updateBlockout(java.lang.String, org.pentaho.platform.scheduler2.blockout.BlockoutTrigger)
   */
  @Override
  public void updateBlockout(String blockoutName, BlockoutTrigger updatedBlockout) {
    // TODO Auto-generated method stub

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
    return false;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockoutManager#willBlockSchedules(org.pentaho.platform.scheduler2.blockout.BlockoutTrigger)
   */
  @Override
  public boolean willBlockSchedules(BlockoutTrigger testBlockout) {
    // TODO Auto-generated method stub
    return false;
  }

}
