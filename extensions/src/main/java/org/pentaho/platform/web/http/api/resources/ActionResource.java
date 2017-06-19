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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.action.ActionInvocationException;
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
import org.slf4j.MDC;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

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
  @Consumes( { TEXT_PLAIN } )
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
    final String actionParams ) {

    // https://docs.oracle.com/javase/7/docs/api/java/lang/Boolean.html#parseBoolean(java.lang.String)
    boolean isAsyncExecution = Boolean.parseBoolean( async );
    int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR; // default ( pessimistic )

    if ( isAsyncExecution ) {

      // default scenario for execution
      executorService.submit( createCallable( actionId, actionClass, actionUser, actionParams ) );
      httpStatus = HttpStatus.SC_ACCEPTED;

    } else {

      try {

        IActionInvokeStatus status = createCallable( actionId, actionClass, actionUser, actionParams ).call();
        httpStatus = ( status != null && status.getThrowable() == null ) ? HttpStatus.SC_OK : HttpStatus.SC_INTERNAL_SERVER_ERROR;

      } catch ( Throwable t ) {
        getLogger().error( t );
      }
    }

    return Response.status( httpStatus ).build();
  }

  /**
   * Returns a {@link CallableAction} that creates the {@link IAction} and invokes it.
   *
   * @param actionId     the action id, if applicable
   * @param actionClass  the action class name, if applicable
   * @param user         the user invoking the action
   * @param actionParams the action parameters needed to instantiate and invoke the action
   * @return a {@link CallableAction} that creates the {@link IAction} and invokes it
   */
  protected CallableAction createCallable( final String actionId, final String actionClass, final String user, final
    String actionParams ) {
    return new CallableAction( this, actionId, actionClass, user, actionParams );
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
    protected String actionId;
    protected String actionClass;
    protected String user;
    protected String actionParams;
    private Map<String, String> mdcContextMap = MDC.getCopyOfContextMap();
    CallableAction() {
    }

    public CallableAction( final ActionResource resource, final String actionId, final String actionClass, final
      String user, final String actionParams ) {
      this.resource = resource;
      this.actionClass = actionClass;
      this.actionId = actionId;
      this.user = user;
      this.actionParams = actionParams;
    }

    IAction createActionBean( final String actionClass, final String actionId ) throws Exception {
      return ActionUtil.createActionBean( actionClass, actionId );
    }

    Map<String, Serializable> deserialize( final IAction action, final String actionParams )
            throws IOException, ActionInvocationException {
      return ActionParams.deserialize( action, ActionParams.fromJson( actionParams ) );
    }

    @Override
    public IActionInvokeStatus call() throws Exception {
      try {
        Optional.ofNullable( mdcContextMap ).ifPresent( s -> MDC.setContextMap( mdcContextMap ) );
        if ( logger.isDebugEnabled() ) {
          logger.debug( "Running action: " + actionId );
        }
        // instantiate the DefaultActionInvoker directly to force local invocation of the action
        final IActionInvoker actionInvoker = resource.getDefaultActionInvoker();
        final IAction action = createActionBean( actionClass, actionId );
        final Map<String, Serializable> params = deserialize( action, actionParams );

        IActionInvokeStatus status = actionInvoker.invokeAction( action, user, params );

        if ( status != null && status.getThrowable() == null ) {
          getLogger().info( Messages.getInstance().getRunningInBgLocallySuccess( action.getClass().getName(), params ),
                  status.getThrowable() );
        } else {
          getLogger().error( Messages.getInstance().getCouldNotInvokeActionLocally( action.getClass().getName(), params ),
                  ( status != null ? status.getThrowable() : null ) );
        }

        return status;

      } catch ( final Throwable thr ) {
        getLogger()
                .error( Messages.getInstance().getCouldNotInvokeActionLocallyUnexpected( ( StringUtil.isEmpty( actionClass )
                        ? actionId : actionClass ), actionParams ), thr );
      }

      return null;
    }
  }

  public static Log getLogger() {
    return logger;
  }
}
