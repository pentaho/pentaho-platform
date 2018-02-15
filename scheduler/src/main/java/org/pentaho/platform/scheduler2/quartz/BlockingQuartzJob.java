/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
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
    final boolean jobRestarted;
    if ( jobExecutionContext != null && jobExecutionContext.getJobDetail() != null ) {
      jobRestarted = jobExecutionContext.getJobDetail().getJobDataMap().getBooleanValue( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG );
    } else {
      jobRestarted = false;
    }
    String messageType = jobRestarted ? MessageTypes.RECREATED_INSTANCE_START : MessageTypes.INSTANCE_START;
    long start = System.currentTimeMillis();
    long end = start;
    try {
      if ( getBlockoutManager().shouldFireNow() || isBlockoutAction( jobExecutionContext ) ) { // We should always let the blockouts fire //$NON-NLS-1$
        makeAuditRecord( 0, messageType, jobExecutionContext );
        createUnderlyingJob().execute( jobExecutionContext );
        end = System.currentTimeMillis();
        messageType = jobRestarted ? MessageTypes.RECREATED_INSTANCE_END : MessageTypes.INSTANCE_END;
      } else {
        getLogger().warn(
            "Job '" + jobExecutionContext.getJobDetail().getName()
                + "' attempted to run during a blockout period.  This job was not executed" );
      }
    } catch ( ActionAdapterQuartzJob.LoggingJobExecutionException le ) {
      // thrown by the execution code - if execution fails, there only thing we do is to write to pro_audit table failing message,
      // no point in trying to execute the job again
      end = System.currentTimeMillis();
      messageType = jobRestarted ? MessageTypes.RECREATED_INSTANCE_FAILED : MessageTypes.INSTANCE_FAILED;
    } catch ( SchedulerException e ) {
      end = System.currentTimeMillis();
      messageType = jobRestarted ? MessageTypes.RECREATED_INSTANCE_FAILED : MessageTypes.INSTANCE_FAILED;
      getLogger().warn(
          "Got Exception retrieving the Blockout Manager for job '" + jobExecutionContext.getJobDetail().getName()
              + "'. Executing the underlying job anyway", e );
      createUnderlyingJob().execute( jobExecutionContext );
      end = System.currentTimeMillis();
      messageType = jobRestarted ? MessageTypes.RECREATED_INSTANCE_END : MessageTypes.INSTANCE_END;
    } finally {
      makeAuditRecord( ( (float) ( end - start ) / 1000 ), messageType, jobExecutionContext );
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

      if ( null == jobDataMap || null == jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) || null == jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) ) {
        //it's an action, no need to log
        return;
      }

      AuditHelper.audit( PentahoSessionHolder.getSession() != null ? PentahoSessionHolder.getSession().getId() : "",
        jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER ).toString() : "",
        jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER ).toString() : "",
        jobExecutionContext.getJobDetail().getJobClass() != null ? jobExecutionContext.getJobDetail().getJobClass().getName() : "",
        jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ) != null ? jobDataMap.get( QuartzScheduler.RESERVEDMAPKEY_ACTIONID ).toString() : "",
        messageType,
        jobDataMap.get( "lineage-id" ) != null ? jobDataMap.get( "lineage-id" ).toString() : "",
        null,
        time,
        null ); //$NON-NLS-1$
    }
  }
}
