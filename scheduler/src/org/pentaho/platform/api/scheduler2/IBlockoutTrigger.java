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
 * @created Mar 13, 2013 
 * @author wseyler
 */


package org.pentaho.platform.api.scheduler2;

/**
 * @author wseyler
 *
 * Interface designed to be added to any "canned" trigger type for use as a blockout trigger.
 */
public interface IBlockoutTrigger {
  /**
   * @return a long that represents in milliseconds how long this trigger should be in effect once triggered
   */
  public long getBlockDuration();

  /**
   * @param blockDuration
   * 
   * Sets the length of time in milliseconds that this trigger should be in effect.
   */
  public void setBlockDuration(long blockDuration);
}
