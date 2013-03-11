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
 * @created Mar 11, 2013 
 * @author wseyler
 */


package org.pentaho.platform.scheduler2.blockout;

import org.pentaho.platform.api.scheduler2.IBlockout;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

/**
 * @author wseyler
 *
 * Implementation of IBlockout backed by a SimpleTrigger and a long that represents a duration
 */
public class BlockoutImpl implements IBlockout {
  
  private SimpleTrigger blockoutTrigger;
  private int duration;
  
  /**
   * @param blockoutTrigger - a SimpleTrigger that represents the start and recurrance of a blockout period
   * @param duration - length of time in millis that the blockout will run for.
   */
  public BlockoutImpl(SimpleTrigger blockoutTrigger, int duration) {
    super();
    this.blockoutTrigger = blockoutTrigger;
    this.duration = duration;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockout#getBlockoutName()
   */
  @Override
  public String getBlockoutName() {
    return blockoutTrigger.getName();
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockout#getBlockoutTrigger()
   */
  @Override
  public Trigger getBlockoutTrigger() {
    return blockoutTrigger;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.scheduler2.IBlockout#getDuration()
   */
  @Override
  public long getDuration() {
    return duration;
  }

}
