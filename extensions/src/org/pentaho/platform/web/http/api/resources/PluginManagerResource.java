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

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.drools.util.StringUtils;
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

@Path("/plugin-manager/")
public class PluginManagerResource {

  private static final String NEW_TOOLBAR_BUTTON_SETTING = "new-toolbar-button"; //$NON-NLS-1$

  public PluginManagerResource() {
  }

  @GET
  @Path("/overlays")
  @Produces({ APPLICATION_JSON })
  public List<Overlay> getOverlays(@QueryParam("id") @DefaultValue("") String id) {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); //$NON-NLS-1$
    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<Overlay> result = new ArrayList<Overlay>();
    for (XulOverlay overlay : overlays) {
      if(!id.isEmpty() && !overlay.getId().equals(id)) continue;
      Overlay tempOverlay = new Overlay(overlay.getId(), overlay.getOverlayUri(), overlay.getSource(), overlay.getResourceBundleUri(), overlay.getPriority());
      result.add(tempOverlay);
    }
    return result;
  }

  @GET
  @Path("/perspectives")
  @Produces({ APPLICATION_JSON })
  public ArrayList<PluginPerspective> getPluginPerpectives() {
    IPluginPerspectiveManager manager = PentahoSystem.get(IPluginPerspectiveManager.class, PentahoSessionHolder.getSession()); //$NON-NLS-1$

    ArrayList<PluginPerspective> perspectives = new ArrayList<PluginPerspective>();

    for (IPluginPerspective perspective : manager.getPluginPerspectives()) {
      PluginPerspective pp = new PluginPerspective();
      pp.setId(perspective.getId());
      pp.setTitle(perspective.getTitle());
      pp.setContentUrl(perspective.getContentUrl());
      pp.setLayoutPriority(perspective.getLayoutPriority());
      pp.setRequiredSecurityActions(perspective.getRequiredSecurityActions());
      pp.setResourceBundleUri(perspective.getResourceBundleUri());
      if (perspective.getOverlays() != null) {
        ArrayList<Overlay> safeOverlays = new ArrayList<Overlay>();
        for (XulOverlay orig : perspective.getOverlays()) {
          Overlay tempOverlay = new Overlay(orig.getId(), orig.getOverlayUri(), orig.getSource(), orig.getResourceBundleUri(), orig.getPriority());
          safeOverlays.add(tempOverlay);
        }
        pp.setOverlays(safeOverlays);
      }
      perspectives.add(pp);
    }

    return perspectives;
  }

  @GET
  @Path("/ids")
  @Produces({ APPLICATION_JSON })
  public Response getPluginIds() {
    if(canAdminister()) {
      IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession());
      return Response.ok(new StringListWrapper(pluginManager.getRegisteredPlugins()), MediaType.APPLICATION_JSON).build();
    } else {
      return Response.status(UNAUTHORIZED).build();
    }
  }

  @GET
  @Path("/{pluginId}/setting/{settingName}")
  @Produces({ APPLICATION_JSON })
  public String getPluginSetting(@PathParam("pluginId") String pluginId, @PathParam("settingName") String settingName) {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); //$NON-NLS-1$
    return (String) pluginManager.getPluginSetting(pluginId, settingName, null);
  }

  @GET
  @Path("/settings/{settingName}")
  @Produces({ APPLICATION_JSON })
  public Response getPluginSettings(@PathParam("settingName") String settingName) {
    // A non-admin still require this setting. All other settings should be admin only
    if(!NEW_TOOLBAR_BUTTON_SETTING.equals(settingName)) {
      if(!canAdminister()) {
        return Response.status(UNAUTHORIZED).build();  
      }
    }
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); //$NON-NLS-1$
    ArrayList<Setting> settings = new ArrayList<Setting>();
    for (String id : pluginManager.getRegisteredPlugins()) {
      Setting s = new Setting(id, (String) pluginManager.getPluginSetting(id, settingName, null));
      if (!StringUtils.isEmpty(s.getValue())) {
        settings.add(s);
      }
    }
    return Response.ok(new JaxbList<Setting>(settings), MediaType.APPLICATION_JSON).build();
  }

  private boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
    return policy.isAllowed(RepositoryReadAction.NAME) && policy.isAllowed(RepositoryCreateAction.NAME)
        && (policy.isAllowed(AdministerSecurityAction.NAME));
  }
}
