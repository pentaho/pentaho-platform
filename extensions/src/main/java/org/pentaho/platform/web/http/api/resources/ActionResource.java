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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
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
  protected static ExecutorService executorService = Executors.newFixedThreadPool( MAX_THREADS );

  /**
   * Runs the action defined within the provided json feed in the background asynchronously.
   *
   * @param actionId     the action id, if applicable
   * @param actionClass  the action class name, if applicable
   * @param user         the user invoking the action
   * @param actionParams the action parameters needed to instantiate and invoke the action
   * @return a {@link Response}
   */
  @POST
  @Path( "/runInBackground" )
  @Consumes( { TEXT_PLAIN } )
  @StatusCodes(
    {
      @ResponseCode( code = 200, condition = "Action invoked successfully." ),
      @ResponseCode( code = 400, condition = "Bad input - could not invoke action." ),
      @ResponseCode( code = 401, condition = "User does not have permissions to invoke action" ),
      @ResponseCode( code = 500, condition = "Error while retrieving system resources." ),
    }
  )
  public Response runInBackground(
    @QueryParam( ActionUtil.INVOKER_ACTIONID ) String actionId,
    @QueryParam( ActionUtil.INVOKER_ACTIONCLASS ) String actionClass,
    @QueryParam( ActionUtil.INVOKER_ACTIONUSER ) String user,
    final String actionParams ) {

    executorService.submit( createRunnable( actionId, actionClass, user, actionParams ) );
    return Response.status( HttpStatus.SC_ACCEPTED ).build();
  }

  /**
   * Returns a {@link RunnableAction} that creates the {@link IAction} and invokes it.
   *
   * @param actionId     the action id, if applicable
   * @param actionClass  the action class name, if applicable
   * @param user         the user invoking the action
   * @param actionParams the action parameters needed to instantiate and invoke the action
   * @return a {@link RunnableAction} that creates the {@link IAction} and invokes it
   */
  protected RunnableAction createRunnable( final String actionId, final String actionClass, final String user, final
    String actionParams ) {
    return new RunnableAction( this, actionId, actionClass, user, actionParams );
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
   * A {@link Runnable} implementation that creates the {@link IAction} and invokes it.
   */
  static class RunnableAction implements Runnable {

    protected ActionResource resource;
    protected String actionId;
    protected String actionClass;
    protected String user;
    protected String actionParams;

    RunnableAction() {
    }

    public RunnableAction( final ActionResource resource, final String actionId, final String actionClass, final
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

    public void run() {
      try {
        // instantiate the DefaultActionInvoker directly to force local invocation of the action
        final IActionInvoker actionInvoker = resource.getDefaultActionInvoker();
        final IAction action = createActionBean( actionClass, actionId );
        final Map<String, Serializable> params = deserialize( action, actionParams );

        final IActionInvokeStatus status = actionInvoker.runInBackground( action, user, params );
        if ( status.getThrowable() == null ) {
          logger.info( Messages.getInstance().getRunningInBgLocallySuccess( action.getClass().getName(), params ),
            status.getThrowable() );
        } else {
          logger.error( Messages.getInstance().getCouldNotInvokeActionLocally( action.getClass().getName(), params ),
            status.getThrowable() );
        }
      } catch ( final Throwable thr ) {
        logger
          .error( Messages.getInstance().getCouldNotInvokeActionLocallyUnexpected( ( StringUtil.isEmpty( actionClass )
            ? actionId : actionClass ), actionParams ), thr );
      }
    }
  }
}
