/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.xml.XmlConfigurationFactory;
import org.apache.logging.log4j.core.util.Loader;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.owasp.encoder.Encode;
import org.pentaho.platform.api.util.LogUtil;

import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import java.io.InputStream;
import java.util.Collection;

@Path( "/logconfig" )
public class Log4jResource {

  private static final Logger LOGGER = LogManager.getLogger( Log4jResource.class );
  private static final String CONFIG = "log4j2.xml";

  @PUT
  @Path ( "/reload" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully reload from configuration." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  public Response reloadConfiguration() throws Exception {
    LogUtil.setLevel(LOGGER, Level.INFO);
    LOGGER.info( "Reloading configuration..." );
    InputStream is = Loader.getResourceAsStream( CONFIG, Loader.getClassLoader() );
    ConfigurationSource source = new ConfigurationSource(is);
    LoggerContext ctx = (LoggerContext) LogManager.getContext(true);
    Configuration config = XmlConfigurationFactory.getInstance().getConfiguration(ctx, source);
    ctx.stop();
    ctx.start(config);
    return Response.ok( "Done" ).build();
  }

  @PUT
  @Path ( "/update" )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully update log level." ),
    @ResponseCode( code = 304, condition = "Log level is not modified." )
    } )
  @Produces( { MediaType.TEXT_PLAIN } )
  @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
  public Response updateLogLevel( @FormParam(  "level" ) String targetLevel, @FormParam( "category" ) String category ) throws Exception {
    LogUtil.setLevel(LOGGER, Level.INFO);
    if ( StringUtils.isBlank( targetLevel ) && StringUtils.isBlank( category ) ) {
      return Response.notModified( "No parameter provided, log level not modified." ).build();
    }

    Logger root = LogManager.getRootLogger();

    if ( StringUtils.isNotBlank( targetLevel ) ) {
      LOGGER.info( "Request to set log level: " + targetLevel );

      if ( StringUtils.isNotBlank( category ) ) {
        LOGGER.info( "Request to set log level for package: " + category );
        if ( LogUtil.exists( category ) ) {
          Logger catLog = LogManager.getLogger( category );
          LogUtil.setLevel( catLog, Level.toLevel( targetLevel, root.getLevel() ) );
          return Response.ok( "Setting log level for: '" + catLog.getName() + "' to be: " + catLog.getLevel() ).build();
        }
        return Response.notModified( "Category: '" + Encode.forHtml( category ) + "' not found, log level not modified." ).build();
      }

      LogUtil.setRootLoggerLevel(Level.toLevel( targetLevel, root.getLevel() ));
      LOGGER.info( "Root logger level set to: " + root.getLevel() );

      LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
      Collection<org.apache.logging.log4j.core.Logger> allLoggers = logContext.getLoggers();
      allLoggers.forEach(logger -> {
        LogUtil.setLevel(logger, Level.toLevel( targetLevel, root.getLevel() ));
      });
    }

    return Response.ok( "Log level updated." ).build();
  }

}
