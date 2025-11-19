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

import org.codehaus.enunciate.Facet;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.usersettings.IUserSettingService;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.web.http.api.resources.utils.EscapeUtils;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * This resource manages the user settings of the platform.
 *
 * User settings are persisted across user sessions and shared by all active sessions of a user.
 * Contrast this with <i>user session variables</i>, accessed via {@link UserConsoleResource},
 * which are reset at every new user session, and are local to each user session.
 *
 * The state changing operations of this service are protected against CSRF attacks,
 * and thus require a CSRF token to be called.
 *
 * @see UserConsoleResource
 */
@Path( "/user-settings" )
@Facet( name = "Unsupported" )
public class UserSettingsResource extends AbstractJaxRSResource {

  public UserSettingsResource() {
  }

  private IPentahoSession getPentahoSession() {
    return PentahoSessionHolder.getSession();
  }

  /**
   * Retrieve the global settings and the user settings for the current user
   * 
   * @return list of settings for the platform
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  @Facet ( name = "Unsupported" )
  public SettingsWrapper getUserSettings() {
    try {
      IUserSettingService settingsService = getUserSettingService();
      List<IUserSetting> userSettings =  settingsService.getUserSettings();

      ArrayList<Setting> settings = new ArrayList<Setting>();
      for ( IUserSetting userSetting : userSettings ) {
        settings.add( new Setting( userSetting.getSettingName(), userSetting.getSettingValue() ) );
      }

      return new SettingsWrapper( settings );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Retrieve a particular user setting for the current user
   * 
   * @param setting (Name of the setting)
   * 
   * @return value of the setting for the user
   */
  @GET
  @Path( "{setting : .+}" )
  @Facet ( name = "Unsupported" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public Response getUserSetting( @PathParam( "setting" ) String setting ) {
    IUserSettingService settingsService = getUserSettingService();
    IUserSetting userSetting = settingsService.getUserSetting( setting, null );

    if ( userSetting != null && userSetting.getSettingValue() != null ) {
      return Response.ok( userSetting.getSettingValue() ).build();
    } else {
      // this returns a 204 http status which will not trigger the jQuery JSON parser while still returning a success status
      return Response.noContent().build();
    }
  }

  /**
   * Save the value of a particular setting for the current user.
   *
   * @param setting  (Setting name)
   * @param settingValue   (Value of the setting)
   * 
   * @return 
   */
  @POST
  @Path( "{setting : .+}" )
  @Facet ( name = "Unsupported" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public Response setUserSetting( @PathParam( "setting" ) String setting, String settingValue ) {
    IUserSettingService settingsService = getUserSettingService();

    //preventing stored XSS(PPP-3464)

    settingValue = EscapeUtils.escapeJsonOrRaw( settingValue );
    settingsService.setUserSetting( setting, settingValue );
    return Response.ok( settingValue ).build();
  }

  IUserSettingService getUserSettingService() {
    return PentahoSystem.get( IUserSettingService.class, getPentahoSession() );
  }

}
