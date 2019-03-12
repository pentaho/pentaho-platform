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
 * Copyright (c) 2002-2019 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Enumeration;

import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;


@Path( "/logconfig" )
public class Log4jResource {

  private static final Logger LOGGER = Logger.getLogger( Log4jResource.class );
  private static final String CONFIG = "log4j.xml";

  private IAuthorizationPolicy policy;

  public Log4jResource() {
    init();
  }

  private void init() {
    try {
      policy = PentahoSystem.get( IAuthorizationPolicy.class );
    } catch ( Exception ex ) {
      LOGGER.warn( "Unable to get IAuthorizationPolicy: " + ex.getMessage() );
    }
  }

  /**
   * Reloads the log4j.xml file updating the log levels and settings.
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/api/logconfig/reload
   * </p>
   *
   * @return In text/plain the word "Done" if successfully executed.
   */
  @PUT
  @Path ( "/reload" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully reload from configuration." ),
    @ResponseCode( code = 401, condition =  "User is not an administrator and is unauthorized to perform action." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  public Response reloadConfiguration() throws Exception {
    if ( canAdminister() ) {
      LOGGER.setLevel( Level.INFO );
      LOGGER.info( "Reloading configuration..." );

      DOMConfigurator.configure( Loader.getResource( CONFIG ) );
      return Response.ok( "Done" ).build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Updates the log level for an existing log category. This change does not update the log4j.xml and does not persist across restarts.
   *
   * If the category parameter is provided, this will only update the level for the specific category specified.  If the category parameter is not provided, this will update the log level for every logger.
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/api/logconfig/reload
   * </p>
   *
   * @param category (Optional) The logging category to update the log level.
   * @param level The log level to set.  Valid values are (ALL, DEBUG, INFO, WARN, ERROR, FATAL, OFF, and TRACE).
   *
   * @return In text/plain the string "Log level updated." if successfully executed if the category is empty.  In text/plain the string "Setting log level for: '${category}' to be: ${level}" if the category is not empty.
   */
  @PUT
  @Path ( "/update" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully update log level." ),
    @ResponseCode( code = 304, condition = "Log level is not modified." ),
    @ResponseCode( code = 401, condition =  "User is not an administrator and is unauthorized to perform action." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  public Response updateLogLevel( @FormParam(  "level" ) String level, @FormParam( "category" ) String category ) throws Exception {
    if ( canAdminister() ) {
      LOGGER.setLevel( Level.INFO );

      if ( StringUtils.isBlank( level ) ) {
        return Response.notModified( "No parameter provided, log level not modified." ).build();
      }

      Logger root = LogManager.getRootLogger();


      LOGGER.info( "Request to set log level: " + level );

      if ( StringUtils.isNotBlank( category ) ) {
        LOGGER.info( "Request to set log level for package: " + category );
        Logger catLog = LogManager.exists( category );
        if ( catLog != null ) {
          catLog.setLevel( Level.toLevel( level, root.getLevel() ) );
          return Response.ok( "Setting log level for: '" + catLog.getName() + "' to be: " + catLog.getLevel() ).build();
        }
        return Response
          .notModified( "Category: '" + Encode.forHtml( category ) + "' not found, log level not modified." ).build();
      }

      root.setLevel( Level.toLevel( level, root.getLevel() ) );
      LOGGER.info( "Root logger level set to: " + root.getLevel() );

      Enumeration e = LogManager.getCurrentLoggers();
      while ( e.hasMoreElements() ) {
        Logger logger = (Logger) e.nextElement();
        logger.setLevel( Level.toLevel( level, root.getLevel() ) );
      }


      return Response.ok( "Log level updated." ).build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Check if user has the rights to administrator
   *
   */
  private boolean canAdminister() {
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME ) );

  }
}
