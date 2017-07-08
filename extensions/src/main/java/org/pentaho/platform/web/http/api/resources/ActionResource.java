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

package org.pentaho.platform.web.http.api.resources;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.action.IAction;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.api.action.IActionInvoker;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.ActionParams;
import org.pentaho.platform.plugin.action.DefaultActionInvoker;
import org.pentaho.platform.plugin.action.messages.Messages;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.util.StringUtil;
import org.pentaho.platform.workitem.WorkItemLifecyclePhase;
import org.pentaho.platform.workitem.WorkItemLifecyclePublisher;
import org.slf4j.MDC;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This resource performs action related tasks, such as running/invoking the action in the background.
 */
@Path( "/action" )
public class ActionResource {
  protected static final Log logger = LogFactory.getLog( ActionResource.class );
  protected static final int MAX_THREADS = 8;
  protected static ExecutorService executorService = Executors.newFixedThreadPool( MAX_THREADS,
    new ThreadFactoryBuilder().setNameFormat( "worker-thread-%d" ).build() );

  /**
   * Runs the action defined within the provided json feed in the background asynchronously.
   *
   * @param actionId     the action id, if applicable
   * @param actionClass  the action class name, if applicable
   * @param actionUser   the user invoking the action
   * @param actionParams the action parameters needed to instantiate and invoke the action
   * @return a {@link Response}
   */
  @POST
  @Path( "/invoke" )
  @Consumes( { APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Action invoked successfully." ),
      @ResponseCode( code = 400, condition = "Bad input - could not invoke action." ),
      @ResponseCode( code = 401, condition = "User does not have permissions to invoke action" ),
      @ResponseCode( code = 500, condition = "Error while retrieving system resources." ),
    } )
  public Response invokeAction(
    @QueryParam( ActionUtil.INVOKER_ASYNC_EXEC ) @DefaultValue( ActionUtil.INVOKER_DEFAULT_ASYNC_EXEC_VALUE ) String async,
    @QueryParam( ActionUtil.INVOKER_ACTIONID ) String actionId,
    @QueryParam( ActionUtil.INVOKER_ACTIONCLASS ) String actionClass,
    @QueryParam( ActionUtil.INVOKER_ACTIONUSER ) String actionUser,
    final ActionParams actionParams ) {

    IAction action = null;
    Map<String, Serializable> params = null;

    try {
      action = createActionBean( actionClass, actionId );
      params = ActionParams.deserialize( action, actionParams );
    } catch ( final Exception e ) {
      logger.error( e.getLocalizedMessage() );
      // we're not able to get the work item UID at this point
      WorkItemLifecyclePublisher.publish( "?", params, WorkItemLifecyclePhase.FAILED, e.getLocalizedMessage() );
      return Response.status( HttpStatus.SC_BAD_REQUEST ).build();
    }

    final String workItemUid = ActionUtil.extractUid( params );
    WorkItemLifecyclePublisher.publish( workItemUid, params, WorkItemLifecyclePhase.RECEIVED );

    final boolean isAsyncExecution = Boolean.parseBoolean( async );
    int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR; // default ( pessimistic )

    if ( isAsyncExecution ) {
      // default scenario for execution
      executorService.submit( createCallable( action, actionUser, params ) );
      httpStatus = HttpStatus.SC_ACCEPTED;
    } else {
      final IActionInvokeStatus status = createCallable( action, actionUser, params ).call();
      httpStatus = ( status != null && status.getThrowable() == null ) ? HttpStatus.SC_OK : HttpStatus
        .SC_INTERNAL_SERVER_ERROR;
    }

    return Response.status( httpStatus ).build();
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
   * Returns the appropriate {@link IActionInvoker}.
   *
   * @return the {@link IActionInvoker}
   */
  IActionInvoker getActionInvoker() {
    return PentahoSystem.get( IActionInvoker.class, "IActionInvoker", PentahoSessionHolder.getSession() );
  }

  DefaultActionInvoker getDefaultActionInvoker() {
    return new DefaultActionInvoker();
  }

  /**
   * A {@link Callable} implementation that creates the {@link IAction} and invokes it.
   */
  static class CallableAction implements Callable {

    protected ActionResource resource;
    protected IAction action;
    protected String actionUser;
    protected Map<String, Serializable> params;

    private Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();

    CallableAction() {
    }

    public CallableAction( final ActionResource resource, final IAction action, final String actionUser, final
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
        final IActionInvoker actionInvoker = resource.getDefaultActionInvoker();

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
