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
 * Copyright (c) 2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.pentaho.platform.action.ActionInvokeStatus;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.ActionParams;
import org.pentaho.platform.plugin.action.LocalActionInvoker;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.web.http.api.resources.WorkerNodeActionInvokerAuditor;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecycleEventUtil;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This service performs action related tasks, such as running/invoking the action in the background.
 */
public class ActionService {

    protected static final Log logger = LogFactory.getLog( ActionService.class );
    protected static final int MAX_THREADS = 8;
    protected static ExecutorService executorService = Executors.newFixedThreadPool( MAX_THREADS,
            new ThreadFactoryBuilder().setNameFormat( "worker-thread-%d" ).build() );


    public IActionInvokeStatus invokeAction( String async, String actionId, String actionClass, String actionUser,
            final ActionParams actionParams ) {

        IAction action = null;
        Map<String, Serializable> params = null;

        IActionInvokeStatus status = new ActionInvokeStatus();

        try {
            action = createActionBean( actionClass, actionId );
            params = ActionParams.deserialize( action, actionParams );
        } catch ( final Exception e ) {
            logger.error( e.getLocalizedMessage() );
            // we're not able to get the work item UID at this point
            WorkItemLifecycleEventUtil.publish( "?", params, WorkItemLifecyclePhase.FAILED, e.toString() );
            status.setThrowable( new Exception( "Bad request" ) );
        }

        final String workItemUid = ActionUtil.extractUid( params );
        WorkItemLifecycleEventUtil.publish( workItemUid, params, WorkItemLifecyclePhase.RECEIVED );

        final boolean isAsyncExecution = Boolean.parseBoolean( async );
        int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR; // default ( pessimistic )

        if ( isAsyncExecution ) {
            // default scenario for execution
            executorService.submit( createCallable( action, actionUser, params ) );

        } else {
            status = createCallable( action, actionUser, params ).call();
        }

        return status;
    }

    /**
     * Returns a {@link CallableAction} that creates the {@link IAction} and invokes it.
     *
     * @param actionUser   the user invoking the action
     * @return a {@link CallableAction} that creates the {@link IAction} and invokes it
     */
    protected CallableAction createCallable( final IAction action, final String actionUser, final Map<String,
            Serializable> params ) {
        return new CallableAction( this, action, actionUser, params );
    }

    protected IAction createActionBean( final String actionClass, final String actionId ) throws Exception {
        return ActionUtil.createActionBean( actionClass, actionId );
    }

    /**
     * Returns the appropriate {@link IActionInvoker}, as defined in spring config. This is safe to do, as this
     * resource class is only expected to be used by the worker node, where the spring configuration for the {@code
     * IActionInvoker} bean is expected to exist.
     *
     * @return the {@link IActionInvoker}
     */
    IActionInvoker getActionInvoker() {
        return PentahoSystem.get( IActionInvoker.class, "IActionInvoker", PentahoSessionHolder.getSession() );
    }

    IActionInvoker getDefaultActionInvoker() {
        return new LocalActionInvoker();
    }

    /**
     * A {@link Callable} implementation that creates the {@link IAction} and invokes it.
     */
    static class CallableAction implements Callable {

        protected ActionService resource;
        protected IAction action;
        protected String actionUser;
        protected Map<String, Serializable> params;

        private Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();

        CallableAction() {
        }

        public CallableAction( final ActionService resource, final IAction action, final String actionUser, final
        Map<String, Serializable> params ) {
            this.resource = resource;
            this.action = action;
            this.actionUser = actionUser;
            this.params = params;
        }

        @Override
        public IActionInvokeStatus call() {
            try {
                Optional.ofNullable( mdcContextMap ).ifPresent( s -> MDC.setContextMap( mdcContextMap ) );

                // instantiate the DefaultActionInvoker directly to force local invocation of the action
                final IActionInvoker actionInvoker = new WorkerNodeActionInvokerAuditor( resource.getDefaultActionInvoker() );

                IActionInvokeStatus status = actionInvoker.invokeAction( action, actionUser, params );

                if ( status != null && status.getThrowable() == null ) {
                    getLogger().info( Messages.getInstance().getRunningInBgLocallySuccess( action.getClass().getName(), params ),
                            status.getThrowable() );
                } else {
                    final String failureMessage = Messages.getInstance().getCouldNotInvokeActionLocally( action.getClass()
                            .getName(), params );
                    getLogger().error( failureMessage, ( status != null ? status.getThrowable() : null ) );
                }

                return status;

            } catch ( final Throwable thr ) {
                getLogger()
                        .error( Messages.getInstance().getCouldNotInvokeActionLocallyUnexpected( action.getClass().getName(),
                                StringUtil.getMapAsPrettyString( params ) ), thr );
            }

            return null;
        }
    }

    public static Log getLogger() {
        return logger;
    }
}
