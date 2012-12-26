/*
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
 * Copyright 2012 Pentaho Corporation.  All rights reserved.
 *
 */
package org.pentaho.platform.web.http.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.api.engine.IPluginManager;
import org.pentaho.platform.api.engine.perspective.IPluginPerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.ui.xul.XulOverlay;

@Path("/plugin-manager/")
public class PluginManagerResource {

  public PluginManagerResource() {
  }

  @GET
  @Path("/overlays")
  @Produces({ APPLICATION_JSON })
  public List<Overlay> getOverlays() {
    IPluginManager pluginManager = PentahoSystem.get(IPluginManager.class, PentahoSessionHolder.getSession()); //$NON-NLS-1$
    List<XulOverlay> overlays = pluginManager.getOverlays();
    ArrayList<Overlay> result = new ArrayList<Overlay>();
    for (XulOverlay overlay : overlays) {
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

}
