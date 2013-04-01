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
 * @created Apr 1, 2013 
 * @author wseyler
 */


package org.pentaho.platform.scheduler2.blockout;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IBlockoutTrigger;
import org.quartz.NthIncludedDayTrigger;

/**
 * @author wseyler
 *
 */
@XmlRootElement
public class NthIncludedDayBlockoutTrigger extends NthIncludedDayTrigger implements IBlockoutTrigger {

  /**
   * 
   */
  private static final long serialVersionUID = 5871839423919141146L;

  /**
   * 
   */
  public NthIncludedDayBlockoutTrigger() {
    super();
  }

  /**
   * @param name
   * @param group
   * @param jobName
   * @param jobGroup
   */
  public NthIncludedDayBlockoutTrigger(String name, long duration) {
    super(name, IBlockoutManager.BLOCK_GROUP, name, IBlockoutManager.BLOCK_GROUP);
    this.setBlockDuration(duration);
  }

  public long getBlockDuration() {
    return this.getJobDataMap().getLong(IBlockoutManager.BLOCK_DURATION_KEY);
  }

  public void setBlockDuration(long blockDuration) {
    this.getJobDataMap().put(IBlockoutManager.BLOCK_DURATION_KEY, blockDuration);
  }

}
