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
 * Copyright (c) 2002-2022 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.pentaho.platform.config.PropertiesFileConfiguration;

import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
