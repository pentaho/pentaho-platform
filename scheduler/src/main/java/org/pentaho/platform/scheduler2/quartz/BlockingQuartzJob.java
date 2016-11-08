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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.engine.core.audit.AuditHelper;
import org.pentaho.platform.engine.core.audit.MessageTypes;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.blockout.PentahoBlockoutManager;
import org.quartz.Job;
import org.quartz.JobDataMap;
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
        final long start = System.currentTimeMillis();
        makeAuditRecord( 0, MessageTypes.INSTANCE_START, jobExecutionContext );
        createUnderlyingJob().execute( jobExecutionContext );
        final long end = System.currentTimeMillis();
        makeAuditRecord( ( (float) ( end - start ) / 1000 ), MessageTypes.INSTANCE_END, jobExecutionContext );
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

  protected void makeAuditRecord( final float time, final String messageType,
                                  final JobExecutionContext jobExecutionContext ) {
    if ( jobExecutionContext != null && jobExecutionContext.getJobDetail() != null ) {
      final JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
      AuditHelper.audit( PentahoSessionHolder.getSession() != null ? PentahoSessionHolder.getSession().getId() : null,
              jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ).toString() : null,
              jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ).toString() : null,
              jobExecutionContext.getJobDetail().getJobClass() != null ? jobExecutionContext.getJobDetail().getJobClass().getName() : null,
              jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ).toString() : null,
              messageType,
              jobDataMap.get( "lineage-id" ) != null ? jobDataMap.get( "lineage-id" ).toString() : null,
              null,
              time,
              null ); //$NON-NLS-1$
    }
  }
}
