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
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.ui.xul.XulOverlay;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

@Path( "/plugin-manager/" )
public class PluginManagerResource {

  private static final String NEW_TOOLBAR_BUTTON_SETTING = "new-toolbar-button"; //$NON-NLS-1$

  public PluginManagerResource() {
  }

  /**
   * Retrieve the list of XUL overlays for the provided id
   * 
   * @param id
   * @return list of <code> Overlay </code>
   */
  @GET
  @Path( "/overlays" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public OverlayWrapper getOverlays( @QueryParam( "id" ) @DefaultValue( "" ) String id ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<Overlay> result = new ArrayList<Overlay>();
    for ( XulOverlay overlay : overlays ) {
      if ( !id.isEmpty() && !overlay.getId().equals( id ) ) {
        continue;
      }
      Overlay tempOverlay =
          new Overlay( overlay.getId(), overlay.getOverlayUri(), overlay.getSource(), overlay.getResourceBundleUri(),
              overlay.getPriority() );
      result.add( tempOverlay );
    }
    return new OverlayWrapper( result );
  }

  /**
   * Retrieve the list of plugin perspective in the platform
   * 
   * @return list of <code> PluginPerspective </code>
   */
  @GET
  @Path( "/perspectives" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public PluginPerspectiveWrapper getPluginPerpectives() {
    IPluginPerspectiveManager manager =
        PentahoSystem.get( IPluginPerspectiveManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$

    ArrayList<PluginPerspective> perspectives = new ArrayList<PluginPerspective>();

    for ( IPluginPerspective perspective : manager.getPluginPerspectives() ) {
      PluginPerspective pp = new PluginPerspective();
      pp.setId( perspective.getId() );
      pp.setTitle( perspective.getTitle() );
      pp.setContentUrl( perspective.getContentUrl() );
      pp.setLayoutPriority( perspective.getLayoutPriority() );
      pp.setRequiredSecurityActions( perspective.getRequiredSecurityActions() );
      pp.setResourceBundleUri( perspective.getResourceBundleUri() );
      if ( perspective.getOverlays() != null ) {
        ArrayList<Overlay> safeOverlays = new ArrayList<Overlay>();
        for ( XulOverlay orig : perspective.getOverlays() ) {
          Overlay tempOverlay =
              new Overlay( orig.getId(), orig.getOverlayUri(), orig.getSource(), orig.getResourceBundleUri(), orig
                  .getPriority() );
          safeOverlays.add( tempOverlay );
        }
        pp.setOverlays( safeOverlays );
      }
      perspectives.add( pp );
    }

    return new PluginPerspectiveWrapper( perspectives );
  }

  /**
   * Retrieve the list of registered plugin IDs
   * 
   * @return list of ids
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response getPluginIds() {
    if ( canAdminister() ) {
      IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() );
      return Response.ok( new StringListWrapper( pluginManager.getRegisteredPlugins() ), MediaType.APPLICATION_JSON )
          .build();
    } else {
      return Response.status( UNAUTHORIZED ).build();
    }
  }

  /**
   * Retrieve the plugins setting with a provided setting name. This will search the plugins's settings.xml and return
   * the selected setting
   * @param pluginId (Plugin ID for the setting being searched)
   * @param settingName (Setting name of a selected plugin)
   * @return Value of the setting
   */
  @GET
  @Path( "/{pluginId}/setting/{settingName}" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public String getPluginSetting( @PathParam( "pluginId" ) String pluginId,
      @PathParam( "settingName" ) String settingName ) {
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    return (String) pluginManager.getPluginSetting( pluginId, settingName, null );
  }

  /**
   * Retrieve the list of setting of a selected setting name from all registered plugins. 
   * 
   * @param settingName (name of the plugin setting)
   * @return list of <code> Setting </code>
   */
  @GET
  @Path( "/settings/{settingName}" )
  @Produces( { APPLICATION_JSON } )
  @Facet ( name = "Unsupported" )
  public Response getPluginSettings( @PathParam( "settingName" ) String settingName ) {
    // A non-admin still require this setting. All other settings should be admin only
    if ( !NEW_TOOLBAR_BUTTON_SETTING.equals( settingName ) ) {
      if ( !canAdminister() ) {
        return Response.status( UNAUTHORIZED ).build();
      }
    }
    IPluginManager pluginManager = PentahoSystem.get( IPluginManager.class, PentahoSessionHolder.getSession() ); //$NON-NLS-1$
    ArrayList<Setting> settings = new ArrayList<Setting>();
    for ( String id : pluginManager.getRegisteredPlugins() ) {
      Setting s = new Setting( id, (String) pluginManager.getPluginSetting( id, settingName, null ) );
      if ( !StringUtils.isEmpty( s.getValue() ) ) {
        settings.add( s );
      }
    }
    return Response.ok( new JaxbList<Setting>( settings ), MediaType.APPLICATION_JSON ).build();
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }
}
