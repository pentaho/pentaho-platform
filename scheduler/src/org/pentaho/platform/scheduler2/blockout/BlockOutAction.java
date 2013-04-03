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
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IVarArgsAction;
import org.pentaho.platform.api.scheduler2.IBlockOutManager;

/**
 * @author wseyler
 * This is the job that executes when the a block out trigger fires.  This job essentially does nothing more
 * than logging the firing of the trigger.
 */
public class BlockOutAction implements IVarArgsAction {

  private static final Log logger = LogFactory.getLog(BlockOutAction.class);

  long duration;

  @Override
  public void execute() throws Exception {
    // TODO - Retrieve duration
    logger.warn("Blocking Started at: " + new Date() + " and will last: " + this.duration + " milliseconds"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  @Override
  public void setVarArgs(Map<String, Object> args) {
    this.duration = (Long) args.get(IBlockOutManager.DURATION_PARAM);

  }

}
