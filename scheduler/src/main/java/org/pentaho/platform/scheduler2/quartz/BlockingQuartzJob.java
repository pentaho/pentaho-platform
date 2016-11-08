/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;

/**
 * A Quartz job that checks if execution is currently suspended before passing on to the underlying job
 * 
 * @author kwalker
 */
public class BlockingQuartzJob implements Job {
  public void execute( final JobExecutionContext jobExecutionContext ) throws JobExecutionException {
    try {
      if ( getBlockoutManager().shouldFireNow() || isBlockoutAction( jobExecutionContext ) ) { // We should always let the blockouts fire //$NON-NLS-1$
        createUnderlyingJob().execute( jobExecutionContext );
      } else {
        getLogger().warn(
            "Job '" + jobExecutionContext.getJobDetail().getName()
                + "' attempted to run during a blockout period.  This job was not executed" );
      }
    } catch ( SchedulerException e ) {
      getLogger().warn(
          "Got Exception retrieving the Blockout Manager for job '" + jobExecutionContext.getJobDetail().getName()
              + "'. Executing the underlying job anyway", e );
      createUnderlyingJob().execute( jobExecutionContext );
    }
  }

  IBlockoutManager getBlockoutManager() throws SchedulerException {
    return new PentahoBlockoutManager();
  }

  Job createUnderlyingJob() {
    return new ActionAdapterQuartzJob();
  }

  Log getLogger() {
    return LogFactory.getLog( BlockingQuartzJob.class );
  }
  
  protected boolean isBlockoutAction( JobExecutionContext ctx ) {
    try {
      String actionClass = ctx.getJobDetail().getJobDataMap().getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS );
      return BlockoutAction.class.getName().equals( actionClass );
    } catch ( Throwable t ) {
      getLogger().warn( t.getMessage(), t );
      return false;
    }
  }    
  
}
