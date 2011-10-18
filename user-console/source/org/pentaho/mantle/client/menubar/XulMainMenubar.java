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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client.menubar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.SimplePanel;

public class XulMainMenubar extends SimplePanel implements IXulLoaderCallback {

  private Map<String, XulOverlay> overlayMap = new HashMap<String, XulOverlay>();

  private GwtXulDomContainer container;
  private boolean fetchedOverlays = false;
  private ICallback<Void> loadCompleteCallback = null;
  private int numStartupOverlays = 0;
  private int startupOverlaysLoaded = 0;

  private static XulMainMenubar instance;

  private XulMainMenubar() {
    reset(null);
  }

  public static XulMainMenubar getInstance() {
    if (instance == null) {
      instance = new XulMainMenubar();
    }
    return instance;
  }

  public void reset(ICallback<Void> callback) {
    loadCompleteCallback = callback;
    // Invoke the async loading of the XUL DOM.
    AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "xul/main_menubar.xul", GWT.getModuleBaseURL() + "messages/mantleMessages", this);
  }

  /**
   * Callback method for the MantleXulLoader. This is called when the Xul file has been processed.
   * 
   * @param runner
   *          GwtXulRunner instance ready for event handlers and initializing.
   */
  public void xulLoaded(GwtXulRunner runner) {
    // handlers need to be wrapped generically in GWT, create one and pass it our reference.

    // instantiate our Model and Controller
    MainMenubarController controller = new MainMenubarController(new MainMenubarModel());

    // Add handler to container
    container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);
    container.addEventHandler(controller);

    try {
      runner.initialize();
    } catch (XulException e) {
      Window.alert("Error initializing XUL runner: " + e.getMessage()); //$NON-NLS-1$
      e.printStackTrace();
      return;
    }

    // Get the toolbar from the XUL doc
    MenuBar bar = (MenuBar) container.getDocumentRoot().getElementById("mainMenubar").getManagedObject(); //$NON-NLS-1$
    this.setWidget(bar);

    fetchOverlays();
  }

  private void fetchOverlays() {
    if (!fetchedOverlays) {
      fetchedOverlays = true;
      AbstractCommand cmd = new AbstractCommand() {
        protected void performOperation(boolean feedback) {
          performOperation();
        }

        protected void performOperation() {
          AsyncCallback<ArrayList<XulOverlay>> callback = new AsyncCallback<ArrayList<XulOverlay>>() {
            public void onFailure(Throwable caught) {
              Window.alert(caught.getMessage());
            }

            public void onSuccess(ArrayList<XulOverlay> overlays) {
              XulMainMenubar.this.loadOverlays(overlays);
            }
          };
          MantleServiceCache.getService().getOverlays(callback);
        }
      };
      cmd.execute();

    } else {
      XulMainMenubar.this.loadOverlays(new ArrayList<XulOverlay>(overlayMap.values()));
    }
  }

  public void overlayLoaded() {
    if (numStartupOverlays == startupOverlaysLoaded && loadCompleteCallback != null) {
      loadCompleteCallback.onHandle(null);
      loadCompleteCallback = null;
    }
  }

  public void loadOverlays(List<XulOverlay> overlays) {
    // add/merge these overlays with existing map
    for (XulOverlay overlay : overlays) {
      overlayMap.put(overlay.getId(), overlay);
    }

    // count number of startup overlays
    numStartupOverlays = 0;
    for (XulOverlay overlay : overlayMap.values()) {
      if (overlay.getId().startsWith("startup")) {
        numStartupOverlays++;
      }
    }

    startupOverlaysLoaded = 0;
    for (XulOverlay overlay : overlayMap.values()) {
      if (overlay.getId().startsWith("startup")) {
        AsyncXulLoader.loadOverlayFromSource(overlay.getSource(), overlay.getResourceBundleUri(), container, new IXulLoaderCallback() {
          public void overlayLoaded() {
            startupOverlaysLoaded++;
            XulMainMenubar.this.overlayLoaded();
          }

          public void overlayRemoved() {
            XulMainMenubar.this.overlayRemoved();
          }

          public void xulLoaded(GwtXulRunner xulRunner) {
            XulMainMenubar.this.xulLoaded(xulRunner);
          }
        });
      }
    }
  }

  public void applyOverlays(Set<String> overlayIds) {
    if (overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        applyOverlay(overlayId);
      }
    }
  }

  public void applyOverlay(String id) {
    if (overlayMap != null && !overlayMap.isEmpty()) {
      if (overlayMap.containsKey(id)) {
        XulOverlay overlay = overlayMap.get(id);
        AsyncXulLoader.loadOverlayFromSource(overlay.getSource(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void removeOverlays(Set<String> overlayIds) {
    if (overlayIds != null && !overlayIds.isEmpty()) {
      for (String overlayId : overlayIds) {
        removeOverlay(overlayId);
      }
    }
  }

  public void removeOverlay(String id) {
    if (overlayMap != null && !overlayMap.isEmpty()) {
      if (overlayMap.containsKey(id)) {
        XulOverlay overlay = overlayMap.get(id);
        AsyncXulLoader.removeOverlayFromSource(overlay.getSource(), overlay.getResourceBundleUri(), container, this);
      } else {
        // Should I log this or throw an exception here
      }
    }
  }

  public void overlayRemoved() {
    // TODO Auto-generated method stub

  }

}
