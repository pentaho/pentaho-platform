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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources;

import org.apache.commons.lang.StringUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.ui.IThemeManager;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.*;

/**
 * Resource manages themes for the platform
 * 
 *
 */
@Path( "/theme" )
public class ThemeResource extends AbstractJaxRSResource {

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
  public List<Theme> getSystemThemes() {
    ArrayList<Theme> themes = new ArrayList<Theme>();
    IThemeManager themeManager = PentahoSystem.get( IThemeManager.class );
    List<String> ids = themeManager.getSystemThemeIds();
    for ( String id : ids ) {
      org.pentaho.platform.api.ui.Theme theme = themeManager.getSystemTheme( id );
      if ( theme.isHidden() == false ) {
        themes.add( new Theme( id, theme.getName() ) );
      }
    }
    return themes;
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
  @Produces( "text/plain" )
  public Response setTheme( String theme ) {
    getPentahoSession().setAttribute( "pentaho-user-theme", theme );
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    settingsService.setUserSetting( "pentaho-user-theme", theme );
    return getActiveTheme();
  }

  /**
   * Return the name of the active theme
   * 
   * @return active theme
   */
  @GET
  @Path( "/active" )
  @Produces( "text/plain" )
  public Response getActiveTheme() {
    IUserSettingService settingsService = PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
    return Response.ok(
      StringUtils.defaultIfEmpty( (String) getPentahoSession().getAttribute( "pentaho-user-theme" ), settingsService
        .getUserSetting( "pentaho-user-theme", PentahoSystem.getSystemSetting( "default-theme", "onyx" ) )
          .getSettingValue() ) ).type( MediaType.TEXT_PLAIN ).build();
  }

}
