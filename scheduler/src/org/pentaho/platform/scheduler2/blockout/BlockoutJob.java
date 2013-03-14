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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

/**
 * @author wseyler
 * This is the job that executes when the a blockout trigger fires.  This job essentially does nothing more
 * than logging the firing of the trigger.
 */
public class BlockoutJob implements Job {

  private static final Log logger = LogFactory.getLog(BlockoutJob.class);
  
  /* (non-Javadoc)
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext jec) throws JobExecutionException {
    Trigger blockoutTrigger = jec.getTrigger();
    if (!(blockoutTrigger instanceof IBlockoutTrigger)) {
      throw new JobExecutionException("A BlockoutJob instance must be fired by a instance of IBlockoutTrigger");
    }
    long duration = ((IBlockoutTrigger)blockoutTrigger).getBlockDuration();
    logger.warn("Blocking Started at: " + new Date() + " and will last: " + duration + " milliseconds"); //$NON-NLS-1$
  }

}
