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

import org.pentaho.platform.config.PropertiesFileConfiguration;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * This service provides methods related to mantle deeplink allowed hosts
 *
 * @author anramos
 */
@Path( "/deeplinkAllowedHosts" )
public class MantleResource {

  private final Log log = LogFactory.getLog( MantleResource.class );
  private static final List<String> whiteListedHosts = new ArrayList<>();

  @GET
  @Path( "/getDeeplinkAllowedHosts" )
  @Produces( { APPLICATION_JSON } )
  @StatusCodes( { @ResponseCode( code = 200, condition = "Successfully sent result." ),
    @ResponseCode( code = 500, condition = "Failed to get data." ) } )
  public List<String> getDeeplinkAllowedHosts() {
    return getListHosts();
  }

  private List<String> getListHosts() {
    if ( whiteListedHosts.isEmpty() ) {

      File propFile = new File(
        PentahoSystem.getApplicationContext().getSolutionRootPath() + "/system/deeplink_allowed_urls.properties" );

      try {

        PropertiesFileConfiguration config = new PropertiesFileConfiguration( "mantle", propFile );

        for ( Map.Entry<Object, Object> prop : config.getProperties().entrySet() ) {
          if ( ( (String) prop.getKey() ).startsWith( "allowedHost." ) ) {
            whiteListedHosts.add( (String) prop.getValue() );
          }
        }
      } catch ( IOException e ) {
        log.warn( propFile.getAbsolutePath()
          + " not found. Failed during loading of properties. Resetting to default config.", e );
      } finally {
        //always adds localhost
        if ( whiteListedHosts.isEmpty() ) {
          whiteListedHosts.add( "http://localhost" );
        }
      }
    }
    return whiteListedHosts;
  }
}
