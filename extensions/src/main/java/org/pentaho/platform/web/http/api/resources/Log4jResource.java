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

import javax.ws.rs.FormParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Enumeration;


@Path( "/logconfig" )
public class Log4jResource {

  private static final Logger LOGGER = Logger.getLogger( Log4jResource.class );
  private static final String CONFIG = "log4j.xml";

  @PUT
  @Path ( "/reload" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully reload from configuration." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  public Response reloadConfiguration() throws Exception {
    LOGGER.setLevel( Level.INFO );
    LOGGER.info( "Reloading configuration..." );

    DOMConfigurator.configure( Loader.getResource( CONFIG ) );
    return Response.ok( "Done" ).build();
  }

  @PUT
  @Path ( "/update" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully update log level." ),
    @ResponseCode( code = 304, condition = "Log level is not modified." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  public Response updateLogLevel( @FormParam(  "level" ) String targetLevel, @FormParam( "category" ) String category ) throws Exception {
    LOGGER.setLevel( Level.INFO );

    if ( StringUtils.isBlank( targetLevel ) && StringUtils.isBlank( category ) ) {
      return Response.notModified( "No parameter provided, log level not modified." ).build();
    }

    Logger root = LogManager.getRootLogger();

    if ( StringUtils.isNotBlank( targetLevel ) ) {
      LOGGER.info( "Request to set log level: " + targetLevel );

      if ( StringUtils.isNotBlank( category ) ) {
        LOGGER.info( "Request to set log level for package: " + category );
        Logger catLog = LogManager.exists( category );
        if ( catLog != null ) {
          catLog.setLevel( Level.toLevel( targetLevel, root.getLevel() ) );
          return Response.ok( "Setting log level for: '" + catLog.getName() + "' to be: " + catLog.getLevel() ).build();
        }
        return Response.notModified( "Category: '" + Encode.forHtml( category ) + "' not found, log level not modified." ).build();
      }

      root.setLevel( Level.toLevel( targetLevel, root.getLevel() ) );
      LOGGER.info( "Root logger level set to: " + root.getLevel() );

      Enumeration e = LogManager.getCurrentLoggers();
      while ( e.hasMoreElements() ) {
        Logger logger = (Logger) e.nextElement();
        logger.setLevel( Level.toLevel( targetLevel, root.getLevel() ) );
      }
    }

    return Response.ok( "Log level updated." ).build();
  }
}
