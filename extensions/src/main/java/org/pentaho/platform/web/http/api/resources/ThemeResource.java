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
import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.WILDCARD;


/**
 * Resource manages themes for the platform
 * 
 *
 */
@Facet( name = "Unsupported" )
@Path( "/theme" )
public class ThemeResource extends AbstractJaxRSResource {

  protected static final Log logger = LogFactory.getLog( ThemeResource.class );

  public ThemeResource() {
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * List the current supported themes in the platform
   * 
   * @return list of themes
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @Facet( name = "Unsupported" )
  public ThemeWrapper getSystemThemes() {
    ArrayList<Theme> themes = new ArrayList<Theme>();
    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class );
    List<String> ids = themeManager.getSystemThemeIds();
    for ( String id : ids ) {
      org.pentaho.platform.api.ui.Theme theme = themeManager.getSystemTheme( id );
      if ( theme.isHidden() == false ) {
        themes.add( new Theme( id, theme.getName() ) );
      }
    }
    return new ThemeWrapper( themes );
  }

  /**
   * Set the current theme to the one provided in this request
   * 
   * 
   * @param theme (theme to be changed to)
   * 
   * @return
   */
  @POST
  @Path( "/set" )
  @Consumes( { WILDCARD } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully set theme." ),
    @ResponseCode ( code = 403, condition = "Illegal set operation." ) } )
  @Produces( "text/plain" )
  @Facet ( name = "Unsupported" )
  public Response setTheme( String theme ) {
    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class );
    List<String> ids = themeManager.getSystemThemeIds();
    if ( ( ids != null ) && ( ids.indexOf( theme ) >= 0 ) ) {
      getPentahoSession().setAttribute( "pentaho-user-theme", theme );
      IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
      settingsService.setUserSetting( "pentaho-user-theme", theme );
      return getActiveTheme();
    } else {
      String cleanTheme = theme.replace( '\n', ' ' ).replace( '\r', ' ' ); // Prevent log forging/injection
      logger.error( "Attempt to set invalid theme: " + cleanTheme ); // We do not want to NLS-ize this message
      return Response.status( Response.Status.FORBIDDEN ).entity( "" ).build();
    }
  }

  /**
   * Return the name of the active theme
   * 
   * @return active theme
   */
  @GET
  @Path( "/active" )
  @Produces( "text/plain" )
  @Facet ( name = "Unsupported" )
  public Response getActiveTheme() {
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    return Response.ok(
      StringUtils.defaultIfEmpty( (String) getPentahoSession().getAttribute( "pentaho-user-theme" ), settingsService
        .getUserSetting( "pentaho-user-theme", PentahoSystem.getSystemSetting( "default-theme", "ruby" ) )
          .getSettingValue() ) ).type( MediaType.TEXT_PLAIN ).build();
  }

}
