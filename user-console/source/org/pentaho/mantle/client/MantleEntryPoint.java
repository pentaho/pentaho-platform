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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client;

import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleEntryPoint implements EntryPoint, IResourceBundleLoadCallback {

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    // just some quick sanity setting of the platform effective locale based on the override
    // which comes from the url parameter
    if (!StringUtils.isEmpty(Window.Location.getParameter("locale"))) {
      MantleServiceCache.getService().setLocaleOverride(Window.Location.getParameter("locale"), null);
    }
    ResourceBundle messages = new ResourceBundle();
    Messages.setResourceBundle(messages);
    messages.loadBundle(GWT.getModuleBaseURL() + "messages/", "mantleMessages", true, MantleEntryPoint.this); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public void bundleLoaded(String bundleName) {
    Window.setTitle(Messages.getString("productName")); //$NON-NLS-1$

    MantleApplication mantle = MantleApplication.getInstance();
    mantle.loadApplication();

    RootPanel loadingPanel = RootPanel.get("loading"); //$NON-NLS-1$
    if (loadingPanel != null) {
      loadingPanel.removeFromParent();
      loadingPanel.setVisible(false);
      loadingPanel.setHeight("0px"); //$NON-NLS-1$
    }
  }

}
