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
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.api.scheduler2.IBlockoutManager;
import org.pentaho.platform.api.scheduler2.IJobTrigger;
import org.pentaho.platform.api.scheduler2.IScheduler;
import org.pentaho.platform.api.scheduler2.SimpleJobTrigger;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.action.DefaultActionInvoker;
import org.pentaho.platform.scheduler2.blockout.BlockoutAction;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.ws.ListParamValue;
import org.pentaho.platform.scheduler2.ws.MapParamValue;
import org.pentaho.platform.scheduler2.ws.StringParamValue;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.workitem.WorkItemLifecycleEventUtil;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * A Quartz job that is responsible for executing the {@link IAction} referred to in the job context.
 *
 * @author aphillips
 */
public class ActionAdapterQuartzJob implements Job {

  static final Log log = LogFactory.getLog( ActionAdapterQuartzJob.class );

  private IActionInvoker actionInvoker = new DefaultActionInvoker(); // default

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

  private static Map<String, Serializable> getSerializableMap( final Map<String, Serializable> originalMap ) {
    final Map<String, Serializable> serializableMap = new HashMap<>( );

    final Iterator<Map.Entry<String, Serializable>> iter = originalMap.entrySet().iterator();
    while ( iter.hasNext() ) {
      final Map.Entry<String, Serializable> entry = iter.next();
      final String key = entry.getKey();
      final Serializable value = entry.getValue();
      if ( value instanceof MapParamValue ) {
        serializableMap.put( key, new HashMap<String, Serializable>( (MapParamValue) value ) );
      } else if ( value instanceof ListParamValue ) {
        serializableMap.put( key, new ArrayList<Serializable>( (ListParamValue) value ) );
      } else if ( value instanceof StringParamValue ) {
        serializableMap.put( key, ( (StringParamValue) value ).getStringValue() );
      } else {
        serializableMap.put( key, value );
      }
    }

    return serializableMap;
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

    final String workItemName = ActionUtil.extractName( params );

    WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.SUBMITTED );

    // creates an instance of IActionInvoker, which knows how to invoke this IAction - if the IActionInvoker bean is
    // not defined through spring, fall back on the default action invoker
    final IActionInvoker actionInvoker = Optional.ofNullable( PentahoSystem.get( IActionInvoker.class ) ).orElse(
      getActionInvoker() );
    // Instantiate the requested IAction bean
    final IAction actionBean = (IAction) ActionUtil.createActionBean( actionClassName, actionId );

    if ( actionInvoker == null ||  actionBean == null ) {
      final String failureMessage = Messages.getInstance().getErrorString(
        "ActionAdapterQuartzJob.ERROR_0002_FAILED_TO_CREATE_ACTION", //$NON-NLS-1$
        getActionIdentifier( null, actionClassName, actionId ), StringUtil.getMapAsPrettyString( params ) );
      WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.FAILED, failureMessage );
      throw new LoggingJobExecutionException( failureMessage );
    }

    if ( actionBean instanceof BlockoutAction ) {
      params.put( IBlockoutManager.SCHEDULED_FIRE_TIME, context.getScheduledFireTime() );
    }

    // Invoke the action and get the status of the invocation
    final IActionInvokeStatus status = actionInvoker.invokeAction( actionBean, actionUser, getSerializableMap( params ) );

    // Status may not be available for remote execution, which is expected
    if ( status == null ) {
      if ( log.isWarnEnabled() ) {
        log.warn( Messages.getInstance().getErrorString(
          "ActionAdapterQuartzJob.WARN_0002_NO_STATUS", //$NON-NLS-1$
          getActionIdentifier( actionBean, actionClassName, actionId ), StringUtil.getMapAsPrettyString( params ) ) );
      }
      return;
    }

    // Some of the ktr/kjb execution failures are not thrown as exception as scheduler might try them again.
    // To resolve this, the errors are flagged. If the execution status returns false, then it throws the
    // exception
    if ( !status.isExecutionSuccessful() ) {
      // throw job exception
      throw new JobExecutionException( Messages.getInstance().getActionFailedToExecute( actionBean //$NON-NLS-1$
        .getClass().getName() ) );
    }

    final boolean requiresUpdate = status.requiresUpdate();
    final Throwable throwable = status.getThrowable();
    Object objsp = status.getStreamProvider();
    IBackgroundExecutionStreamProvider sp = null;
    if ( objsp != null && IBackgroundExecutionStreamProvider.class.isAssignableFrom( objsp.getClass() ) ) {
      sp = (IBackgroundExecutionStreamProvider) objsp;
    }
    final IBackgroundExecutionStreamProvider streamProvider = sp;


    final Map<String, Serializable> jobParams = new HashMap<String, Serializable>( params ); // shallow copy

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
            WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.RESTARTED );
            scheduler.createJob( jobName, iaction, jobParams, trigger, streamProvider );
            log.warn( "New RunOnce job created for " + jobName + " -> possible startup synchronization error" );
            return null;
          }
        } );
      } else {
        log.warn( "RunOnce already created, skipping" );
      }
      throw new JobExecutionException( throwable );
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
            WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.RESTARTED );
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

  public IActionInvoker getActionInvoker() {
    return actionInvoker;
  }

  public void setActionInvoker( IActionInvoker actionInvoker ) {
    this.actionInvoker = actionInvoker;
  }
}
