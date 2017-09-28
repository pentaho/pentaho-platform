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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.action.IActionInvokeStatus;
import org.pentaho.platform.plugin.action.ActionParams;
import org.pentaho.platform.util.ActionUtil;
import org.pentaho.platform.web.http.api.resources.services.ActionService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This resource performs action related tasks, such as running/invoking the action in the background.
 */
@Path( "/action" )
public class ActionResource {

  protected static final Log logger = LogFactory.getLog( ActionResource.class );

  private ActionService service = new ActionService(); // default

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

    final boolean isAsyncExecution = Boolean.parseBoolean( async );
    int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR; // default ( pessimistic )

    if ( isAsyncExecution ) {

      // default scenario for execution
      getService().invokeAction( async, actionId, actionClass, actionUser, actionParams );
      httpStatus = HttpStatus.SC_ACCEPTED;

    } else {

      final IActionInvokeStatus status = getService().invokeAction( async, actionId, actionClass, actionUser, actionParams );
      httpStatus = ( status != null && status.getThrowable() == null ) ? HttpStatus.SC_OK : HttpStatus
        .SC_INTERNAL_SERVER_ERROR;

    }

    return Response.status( httpStatus ).build();
  }

  public static Log getLogger() {
    return logger;
  }

  public ActionService getService() {
    return service;
  }

  public void setService(ActionService service) {
    this.service = service;
  }
}
