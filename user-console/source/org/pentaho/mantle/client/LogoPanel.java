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
 */
package org.pentaho.mantle.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LogoPanel extends VerticalPanel {

  private String launchURL;

  public LogoPanel(String launchURL) {
    this.launchURL = launchURL;
    setStyleName("logoPanel-ContainerToolBar"); //$NON-NLS-1$
    setSpacing(0);
    setHeight("100%"); //$NON-NLS-1$
    setWidth("100%"); //$NON-NLS-1$

    VerticalPanel toolBarBackgroundPanel = new VerticalPanel();
    toolBarBackgroundPanel.setSpacing(0);
    toolBarBackgroundPanel.setStyleName("logoPanel-Container"); //$NON-NLS-1$
    toolBarBackgroundPanel.addStyleName("puc-logo");
    toolBarBackgroundPanel.setWidth("100%"); //$NON-NLS-1$
    toolBarBackgroundPanel.setHeight("100%"); //$NON-NLS-1$

    Image logoImage = new Image();
      logoImage.setUrl("mantle/images/spacer.gif"); //$NON-NLS-1$
    if (launchURL != null && !"".equals(launchURL)) { //$NON-NLS-1$
      logoImage.setStyleName("launchImage"); //$NON-NLS-1$
      logoImage.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          Window.open(getLaunchURL(), "_blank", ""); //$NON-NLS-1$ //$NON-NLS-2$
        }
      });
    }
    logoImage.setStyleName("puc-logo-spacer");
    toolBarBackgroundPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    toolBarBackgroundPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    toolBarBackgroundPanel.add(logoImage);

    add(toolBarBackgroundPanel);
  }

  public String getLaunchURL() {
    return launchURL;
  }

  public void setLaunchURL(String launchURL) {
    this.launchURL = launchURL;
  }

}
