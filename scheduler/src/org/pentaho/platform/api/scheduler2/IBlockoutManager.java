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


package org.pentaho.platform.api.scheduler2;

import org.quartz.Trigger;

/**
 * @author wseyler
 * 
 * Interface for managing Block-outs (time when schedules should NOT be executed)
 */
/**
 * @author wseyler
 *
 */
public interface IBlockoutManager {
  
  /**
   * @param blockout - A trigger that determines the recurrence of the block-out
   * @param duration - A long that determines how long the recurrence will last each time
   * 
   * Adds a block-out, the blockout recurrence is based on the blockout (trigger) argument
   * and the duration
   */
  void adBlockout(IBlockout blockout);

  /**
   * @param blockoutName
   * @return a IBlockout Object that represents the blockout with the name blockoutName
   */
  IBlockout getBlockout(String blockoutName);
  
  /**
   * @param blockoutName
   * @param updatedBlockout
   * 
   * Replaces the blockout with blockoutName with the IBlockout updatedBlockout
   */
  void updateBlockout(String blockoutName, IBlockout updatedBlockout);
  
  
  /**
   * @param blockoutName
   * 
   * removes the blockout with the name blokoutName from the active blockouts
   */
  void deleteBlockout(String blockoutName);
  
  /**
   * @param scheduleTrigger
   * @return the minimum number of times that the scheduleTrigger will fire if applying the
   * current list of blockouts.
   */
  int willFire(Trigger scheduleTrigger);
  
  
  /**
   * @return true if there are no current blockouts active at the moment this method is called
   */
  boolean shouldFireNow();
  
  
  /**
   * @param testBlockout
   * @return true if testBlockout will block ANY existing schedules.
   */
  boolean willBlockSchedules(IBlockout testBlockout);
}
