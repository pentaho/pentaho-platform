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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.menubar.XulMainMenubar;
import org.pentaho.mantle.client.ui.toolbar.XulMainToolbar;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.gwt.util.ResourceBundleTranslator;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;

public class PerspectiveSwitcher extends HorizontalPanel {

  public static final String PROPERTIES_EXTENSION = ".properties"; //$NON-NLS-1$
  public static final String SEPARATOR = "/"; //$NON-NLS-1$

  private ArrayList<ToggleButton> toggles = new ArrayList<ToggleButton>();

  // create an overlay list to later register with the main toolbar/menubar
  private ArrayList<XulOverlay> overlays = new ArrayList<XulOverlay>();
  private ArrayList<IPluginPerspective> perspectives;

  public PerspectiveSwitcher() {
    setWidth("100%");
    getElement().setId("mantle-perspective-switcher");
    setStyleName("mantle-perspective-switcher");
    AsyncCallback<ArrayList<IPluginPerspective>> callback = new AsyncCallback<ArrayList<IPluginPerspective>>() {
      public void onFailure(Throwable caught) {
        Window.alert("getPluginPerpectives fail: " + caught.getMessage());
      }

      public void onSuccess(ArrayList<IPluginPerspective> perspectives) {
        setPluginPerspectives(perspectives);
      }
    };
    MantleServiceCache.getService().getPluginPerpectives(callback);
  }

  // public void selectDefaultPerspective() {
  // showPerspective(toggles.get(0), perspectives.get(0));
  // }

  protected void setPluginPerspectives(final ArrayList<IPluginPerspective> perspectives) {

    this.perspectives = perspectives;

    // layoutPriority of -1 is for the default perspective
    // anything lower will be added before the default
    // anything higher will be added after the default
    // TODO: anything larger than 100 will be added in the "More v" drop down

    clear();
    setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
    setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    SimplePanel p = new SimplePanel();
    p.setWidth("100%");
    add(p);
    setCellWidth(p, "100%");

    // sort perspectives
    Collections.sort(perspectives, new Comparator<IPluginPerspective>() {
      public int compare(IPluginPerspective o1, IPluginPerspective o2) {
        Integer p1 = new Integer(o1.getLayoutPriority());
        Integer p2 = new Integer(o2.getLayoutPriority());
        return p1.compareTo(p2);
      }
    });

    for (final IPluginPerspective perspective : perspectives) {
      // if we have overlays add it to the list
      if (perspective.getOverlays() != null) {
        overlays.addAll(perspective.getOverlays());
      }

      ToggleButton tb = new ToggleButton(perspective.getTitle(), new ClickHandler() {
        public void onClick(ClickEvent event) {
          if (((ToggleButton) event.getSource()).isDown()) {
            History.newItem(perspective.getId());
          } else {
            IPluginPerspective defaultPerspective = perspectives.get(0);
            History.newItem(defaultPerspective.getId());
          }
          showPerspective((ToggleButton) event.getSource(), perspective);
        }
      });
      tb.setStyleName("mantle-perspective-toggle");
      tb.getElement().setAttribute("layoutPriority", "" + perspective.getLayoutPriority());
      toggles.add(tb);
      add(tb);
      loadResourceBundle(tb, perspective);
    }

    // register all toolbar overlays with XulMainToolbar
    XulMainToolbar.getInstance().loadOverlays(overlays);

    // register all menubar overlays with XulMainMenubar
    XulMainMenubar.getInstance().loadOverlays(overlays);

    String historyTokenPerspectiveId = History.getToken();
    boolean loadDefaultPerspective = true;
    for (int i = 0; i < perspectives.size(); i++) {
      if (perspectives.get(i).getId().equalsIgnoreCase(historyTokenPerspectiveId)) {
        toggles.get(i).setDown(true);
        loadDefaultPerspective = false;
        showPerspective(toggles.get(i), perspectives.get(i));
        break;
      }
    }
    if (loadDefaultPerspective) {
      showPerspective(toggles.get(0), perspectives.get(0));
    }
  }
  
  private void loadResourceBundle(final ToggleButton button, final IPluginPerspective perspective) {
    try {
      String bundle = perspective.getResourceBundleUri();
      if (bundle == null) {
        return;
      }
      String folder = ""; //$NON-NLS-1$
      String baseName = bundle;

      // we have to separate the folder from the base name
      if (bundle.indexOf(SEPARATOR) > -1) {
        folder = bundle.substring(0, bundle.lastIndexOf(SEPARATOR) + 1);
        baseName = bundle.substring(bundle.lastIndexOf(SEPARATOR) + 1);
      }

      // some may put the .properties on incorrectly
      if (baseName.contains(PROPERTIES_EXTENSION)) {
        baseName = baseName.substring(0, baseName.indexOf(PROPERTIES_EXTENSION));
      }
      // some may put the .properties on incorrectly
      if (baseName.contains(".properties")) {
        baseName = baseName.substring(0, baseName.indexOf(".properties"));
      }

      final ResourceBundle messageBundle = new ResourceBundle();
      messageBundle.loadBundle(folder, baseName, true, new IResourceBundleLoadCallback() {
        public void bundleLoaded(String arg0) {
          String title = ResourceBundleTranslator.translate(perspective.getTitle(), messageBundle);
          button.setText(title);
        }
      });

    } catch (Throwable t) {
      Window.alert("Error loading message bundle: " + t.getMessage()); //$NON-NLS-1$
      t.printStackTrace();
    }
  }

  private void showPerspective(final ToggleButton source, final IPluginPerspective perspective) {

    final IPluginPerspective defaultPerspective = perspectives.get(0);

    // deselect all other toggles
    for (ToggleButton disableMe : toggles) {
      if (disableMe != source) {
        disableMe.setDown(false);
      }
    }

    // remove all existing perspective overlays
    XulMainToolbar.getInstance().reset(new ICallback<Void>() {
      public void onHandle(Void nothing) {
        // apply current overlay or default if none selected
        if (source.isDown() && perspective.getOverlays() != null) {
          for (XulOverlay overlay : perspective.getOverlays()) {
            XulMainToolbar.getInstance().applyOverlay(overlay.getId());
          }
        } else if (!source.isDown() && defaultPerspective.getOverlays() != null) {
          // apply default perspective overlay
          for (XulOverlay overlay : defaultPerspective.getOverlays()) {
            XulMainToolbar.getInstance().applyOverlay(overlay.getId());
          }
        }
      }
    });
    XulMainMenubar.getInstance().reset(new ICallback<Void>() {
      public void onHandle(Void nothing) {
        // apply current overlay or default if none selected
        if (source.isDown() && perspective.getOverlays() != null) {
          for (XulOverlay overlay : perspective.getOverlays()) {
            XulMainMenubar.getInstance().applyOverlay(overlay.getId());
          }
        } else if (!source.isDown() && defaultPerspective.getOverlays() != null) {
          // apply default perspective overlay
          for (XulOverlay overlay : defaultPerspective.getOverlays()) {
            XulMainMenubar.getInstance().applyOverlay(overlay.getId());
          }
        }
      }
    });

    // see if we need to show the default perspective
    // if source is not down then no perspectives are selected, select the first one
    if (!source.isDown()) {
      toggles.get(0).setDown(true);
      if (defaultPerspective.getId().equals("default.perspective")) {
        MantleApplication.getInstance().getContentDeck()
            .showWidget(MantleApplication.getInstance().getContentDeck().getWidgetIndex(SolutionBrowserPanel.getInstance()));
        return;
      }
    }

    // if the selected perspective is "default.perspective"
    if (perspective.getId().equals("default.perspective")) {
      MantleApplication.getInstance().getContentDeck()
          .showWidget(MantleApplication.getInstance().getContentDeck().getWidgetIndex(SolutionBrowserPanel.getInstance()));
      return;
    }

    hijackContentArea(perspective);
  }

  private void hijackContentArea(IPluginPerspective perspective) {
    // hijack content area (or simply find and select existing content)
    Frame frame = null;
    for (int i = 0; i < MantleApplication.getInstance().getContentDeck().getWidgetCount(); i++) {
      Widget w = MantleApplication.getInstance().getContentDeck().getWidget(i);
      if (w instanceof Frame && perspective.getId().equals(w.getElement().getId())) {
        frame = (Frame) w;
      }
    }
    if (frame == null) {
      frame = new Frame(perspective.getContentUrl());
      frame.getElement().setId(perspective.getId());
      MantleApplication.getInstance().getContentDeck().add(frame);
    }

    MantleApplication.getInstance().getContentDeck().showWidget(MantleApplication.getInstance().getContentDeck().getWidgetIndex(frame));
  }

}