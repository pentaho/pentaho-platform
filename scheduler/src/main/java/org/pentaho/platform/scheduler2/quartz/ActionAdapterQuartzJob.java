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
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.scheduler2.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * A Quartz job that is responsible for executing the {@link IAction} referred to in the job context.
 *
 * @author aphillips
 */
public class ActionAdapterQuartzJob implements Job {

  static final Log log = LogFactory.getLog( ActionAdapterQuartzJob.class );

  public void execute( JobExecutionContext context ) throws JobExecutionException {
    JobDataMap jobDataMap = context.getMergedJobDataMap();
    String actionUser = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONUSER );

    final String actionClassName = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS );
    final String actionId = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONID );

    try {
      invokeAction( actionClassName, actionId, actionUser, context, jobDataMap.getWrappedMap() );

    } catch ( Throwable t ) {
      // We should not distinguish between checked and unchecked exceptions here. All job execution failures
      // should result in a rethrow of a quartz exception
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
        "ActionAdapterQuartzJob.ERROR_0004_ACTION_FAILED", //$NON-NLS-1$
        getActionIdentifier( null, actionClassName, actionId ) ), t );
    }
  }

  private static String getActionIdentifier( final IAction actionBean, final String actionClassName, final String
    actionId ) {
    if ( actionBean != null ) {
      return actionBean.getClass().getName();
    } else if ( !StringUtil.isEmpty( actionClassName ) ) {
      return actionClassName;
    } else if ( !StringUtil.isEmpty( actionId ) ) {
      return actionId;
    }
    return "?"; //$NON-NLS-1$
  }

  /**
   * @deprecated as of 8.0, use {@link #invokeAction(String, String, String, JobExecutionContext, Map)}} instead
   */
  @Deprecated
  protected void invokeAction( final IAction actionBean, final String actionUser, final JobExecutionContext context,
                               final Map<String, Serializable> params ) throws Exception {

    final JobDataMap jobDataMap = context.getMergedJobDataMap();
    final String actionClass = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONCLASS );
    final String actionId = jobDataMap.getString( QuartzScheduler.RESERVEDMAPKEY_ACTIONID );

    invokeAction( actionClass, actionId, actionUser, context, params );
  }

  /**
   * Invokes the {@link IAction} bean that is created from the provided {@code actionClassName} and {@code actionId} as
   * the provided {@code actionUser}. If the {@code IAction} execution fails as-is, the scheduler attempts to re-create
   * the job that will try to invoke the {@link IAction} again.
   *
   * @param actionClassName The class name of the {@link IAction} bean; used as a backup, if the {@code actionId} is not
   *                        available or vald
   * @param actionId        The bean id of the {@link IAction} requested to be invoked.
   * @param actionUser      The user invoking the {@link IAction}
   * @param context         the {@code JobExecutionContext}
   * @param params          the {@link Map} or parameters needed to invoke the {@link IAction}
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected void invokeAction( final String actionClassName, final String actionId, final String actionUser, final
    JobExecutionContext context, final Map<String, Serializable> params ) throws Exception {

    // create an instance of IActionInvoker, which knows know to invoke this IAction
    final IActionInvoker actionInvoker = PentahoSystem.get( IActionInvoker.class, "IActionInvoker", PentahoSessionHolder
      .getSession() );
    if ( actionInvoker == null ) {
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
        "ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        getActionIdentifier( null, actionClassName, actionId ), StringUtil.getMapAsPrettyString( params ) ) );
    }

    // Instantiate the requested IAction bean
    final IAction actionBean = (IAction) ActionUtil.createActionBean( actionClassName, actionId );
    if ( actionBean == null ) {
      throw new LoggingJobExecutionException( Messages.getInstance().getErrorString(
        "ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        getActionIdentifier( actionBean, actionClassName, actionId ), StringUtil.getMapAsPrettyString( params ) ) );
    }

    // Invoke the action and get the status of the invocation
    final IActionInvokeStatus status = actionInvoker.runInBackground( actionBean, actionUser, params );

    // Status may not be available for remote execution, which is expected
    if ( status == null ) {
      if ( log.isWarnEnabled() ) {
        log.warn( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.WARN_0002_NO_STATUS", //$NON-NLS-1$
          getActionIdentifier( actionBean, actionClassName, actionId ), StringUtil.getMapAsPrettyString( params ) ) );
      }
      return;
    }

    final boolean requiresUpdate = status.requiresUpdate();
    final Throwable throwable = status.getThrowable();

    Object objsp = params.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );
    IBackgroundExecutionStreamProvider sp = null;
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      sp = (IBackgroundExecutionStreamProvider) objsp;
    }
    final IBackgroundExecutionStreamProvider streamProvider = sp;


    final Map<String, Serializable> jobParams = new HashMap<String, Serializable>( params ); // shallow copy
    if ( actionBean instanceof BlockoutAction ) {
      params.put( IBlockoutManager.SCHEDULED_FIRE_TIME, context.getScheduledFireTime() );
    }

    final IScheduler scheduler = PentahoSystem.getObjectFactory().get( IScheduler.class, "IScheduler2", null );
    if ( throwable != null ) {
      Object restartFlag = jobParams.get( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG );
      if ( restartFlag == null ) {
        final SimpleJobTrigger trigger = new SimpleJobTrigger( new Date(), null, 0, 0 );
        final Class<IAction> iaction = (Class<IAction>) actionBean.getClass();
        // recreate the job in the context of the original creator
        SecurityHelper.getInstance().runAsUser( actionUser, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            if ( streamProvider != null ) {
              streamProvider.setStreamingAction( null ); // remove generated content
            }
            QuartzJobKey jobKey = QuartzJobKey.parse( context.getJobDetail().getName() );
            String jobName = jobKey.getJobName();
            jobParams.put( QuartzScheduler.RESERVEDMAPKEY_RESTART_FLAG, Boolean.TRUE );
            scheduler.createJob( jobName, iaction, jobParams, trigger, streamProvider );
            log.warn( "New RunOnce job created for " + jobName + " -> possible startup synchronization error" );
            return null;
          }
        } );
      } else {
        log.warn( "RunOnce already created, skipping" );
        throw new Exception( throwable );
      }
    }

    scheduler.fireJobCompleted( actionBean, actionUser, params, streamProvider );

    if ( requiresUpdate ) {
      log.warn( "Output path for job: " + context.getJobDetail().getName() + " has changed. Job requires update" );
      try {
        final IJobTrigger trigger = scheduler.getJob( context.getJobDetail().getName() ).getJobTrigger();
        final Class<IAction> iaction = (Class<IAction>) actionBean.getClass();

        // remove job with outdated/invalid output path
        scheduler.removeJob( context.getJobDetail().getName() );

        // recreate the job in the context of the original creator
        SecurityHelper.getInstance().runAsUser( actionUser, new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            streamProvider.setStreamingAction( null ); // remove generated content
            QuartzJobKey jobKey = QuartzJobKey.parse( context.getJobDetail().getName() );
            String jobName = jobKey.getJobName();
            org.pentaho.platform.api.scheduler2.Job j =
              scheduler.createJob( jobName, iaction, jobParams, trigger, streamProvider );
            log.warn( "New Job: " + j.getJobId() + " created" );
            return null;
          }
        } );
      } catch ( Exception e ) {
        log.error( e.getMessage(), e );
      }
    }

    if ( log.isDebugEnabled() ) {
      log.debug( MessageFormat.format(
        "Scheduling system successfully invoked action {0} as user {1} with params [ {2} ]", actionBean //$NON-NLS-1$
          .getClass().getName(), actionUser, QuartzScheduler.prettyPrintMap( params ) ) );
    }
  }

  class LoggingJobExecutionException extends JobExecutionException {
    private static final long serialVersionUID = -4124907454208034326L;

    public LoggingJobExecutionException( String msg ) {
      super( msg );
      log.error( msg );
    }

    public LoggingJobExecutionException( String msg, Throwable t ) {
      super( msg, t );
      log.error( msg, t );
    }

  }

}
