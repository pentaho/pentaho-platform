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

package org.pentaho.platform.scheduler2.action;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.action.ActionInvokeStatus;
import org.pentaho.platform.api.action.ActionInvocationException;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.api.scheduler2.IBackgroundExecutionStreamProvider;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.scheduler2.messsages.Messages;
import org.pentaho.platform.scheduler2.quartz.QuartzScheduler;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecycleEventUtil;

import java.io.Serializable;
import java.util.Map;

/**
 * A concrete implementation of the {@link IActionInvoker} interface that invokes the {@link IAction} locally.
 */
public class DefaultActionInvoker implements IActionInvoker {

  private static final Log logger = LogFactory.getLog( DefaultActionInvoker.class );

  /**
   * Gets the stream provider from the {@code RESERVEDMAPKEY_STREAMPROVIDER} key within the {@code params} {@link Map}.
   *
   * @param params the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return a {@link IBackgroundExecutionStreamProvider} represented in the {@code params} {@link Map}
   */
  protected IBackgroundExecutionStreamProvider getStreamProvider( final Map<String, Serializable> params ) {
    if ( params == null ) {
      logger.warn( Messages.getInstance().getMapNullCantReturnSp() );
      return null;
    }

    final Object obj = params.get( QuartzScheduler.RESERVEDMAPKEY_STREAMPROVIDER );
    return ( obj instanceof IBackgroundExecutionStreamProvider ) ? (IBackgroundExecutionStreamProvider) obj : null;
  }

  /**
   *
   * Validates that the conditions required for the {@link IAction} to be invoked are true, throwing an
   * {@link ActionInvocationException}, if the conditions are not met.
   *
   * @param actionBean The {@link IAction} to be invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws ActionInvocationException when conditions needed to invoke the {@link IAction} are not met
   */
  public void validate( final IAction actionBean, final String actionUser,
                        final Map<String, Serializable> params ) throws ActionInvocationException {

    final String workItemName = ActionUtil.extractName( params );

    if ( actionBean == null || params == null ) {
      final String failureMessage = Messages.getInstance().getCantInvokeNullAction();
      WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.FAILED,  failureMessage );
      throw new ActionInvocationException( failureMessage );
    }

    if ( !isSupportedAction( actionBean ) ) {
      final String failureMessage = Messages.getInstance().getUnsupportedAction( actionBean.getClass().getName() );
      WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.FAILED, failureMessage );
      throw new ActionInvocationException( failureMessage );
    }
  }

  /**
   * Invokes the provided {@link IAction} as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  @Override
  public IActionInvokeStatus invokeAction( final IAction actionBean,
                                           final String actionUser,
                                           final Map<String, Serializable> params ) throws Exception {
    validate( actionBean, actionUser, params );
    return invokeActionImpl( actionBean, actionUser, params );
  }

  /**
   * Invokes the provided {@link IAction} as the provided {@code actionUser}.
   *
   * @param actionBean the {@link IAction} being invoked
   * @param actionUser The user invoking the {@link IAction}
   * @param params     the {@link Map} or parameters needed to invoke the {@link IAction}
   * @return the {@link IActionInvokeStatus} object containing information about the action invocation
   * @throws Exception when the {@code IAction} cannot be invoked for some reason.
   */
  protected IActionInvokeStatus invokeActionImpl( final IAction actionBean,
                                           final String actionUser,
                                           final Map<String, Serializable> params ) throws Exception {

    final String workItemName = ActionUtil.extractName( params );

    if ( actionBean == null || params == null ) {
      final String failureMessage = Messages.getInstance().getCantInvokeNullAction();
      WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.FAILED, failureMessage );
      throw new ActionInvocationException( failureMessage );
    }

    WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.IN_PROGRESS );

    if ( logger.isDebugEnabled() ) {
      logger.debug( Messages.getInstance().getRunningInBackgroundLocally( actionBean.getClass().getName(), params ) );
    }

    // set the locale, if not already set
    if ( params.get( LocaleHelper.USER_LOCALE_PARAM ) == null || StringUtils.isEmpty(
      params.get( LocaleHelper.USER_LOCALE_PARAM ).toString() ) ) {
      params.put( LocaleHelper.USER_LOCALE_PARAM, LocaleHelper.getLocale() );
    }

    // remove the scheduling infrastructure properties
    ActionUtil.removeKeyFromMap( params, ActionUtil.INVOKER_ACTIONCLASS );
    ActionUtil.removeKeyFromMap( params, ActionUtil.INVOKER_ACTIONID );
    ActionUtil.removeKeyFromMap( params, ActionUtil.INVOKER_ACTIONUSER );
    // build the stream provider
    final IBackgroundExecutionStreamProvider streamProvider = getStreamProvider( params );
    ActionUtil.removeKeyFromMap( params, ActionUtil.INVOKER_STREAMPROVIDER );
    ActionUtil.removeKeyFromMap( params, ActionUtil.INVOKER_UIPASSPARAM );

    final ActionRunner actionBeanRunner = new ActionRunner( actionBean, actionUser, params, streamProvider );
    final IActionInvokeStatus status = new ActionInvokeStatus();
    status.setStreamProvider( streamProvider );

    boolean requiresUpdate = false;
    try {
      if ( ( StringUtil.isEmpty( actionUser ) ) || ( actionUser.equals( "system session" ) ) ) { //$NON-NLS-1$
        // For now, don't try to run quartz jobs as authenticated if the user
        // that created the job is a system user. See PPP-2350
        requiresUpdate = SecurityHelper.getInstance().runAsAnonymous( actionBeanRunner );
      } else {
        requiresUpdate = SecurityHelper.getInstance().runAsUser( actionUser, actionBeanRunner );
      }
    } catch ( final Throwable t ) {
      WorkItemLifecycleEventUtil.publish( workItemName, params, WorkItemLifecyclePhase.FAILED, t.toString() );
      status.setThrowable( t );
    }
    status.setRequiresUpdate( requiresUpdate );
    // Set the execution Status
    status.setExecutionStatus( actionBean.isExecutionSuccessful() );
    return status;
  }

  @Override
  public boolean isSupportedAction( IAction action ) {
    return true; // supports all
  }
}
