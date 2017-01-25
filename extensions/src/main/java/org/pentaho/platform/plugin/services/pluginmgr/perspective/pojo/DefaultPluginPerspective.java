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

package org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo;

import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;

import java.io.Serializable;
import java.util.ArrayList;

public class DefaultPluginPerspective implements Serializable, IPluginPerspective {

  private String id;
  private String title;
  private String contentUrl;
  private String resourceBundleUri;

  private ArrayList<XulOverlay> overlays;
  private int layoutPriority;
  private ArrayList<String> securityActions;

  public DefaultPluginPerspective() {
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl( String contentUrl ) {
    this.contentUrl = contentUrl;
  }

  public String getResourceBundleUri() {
    return resourceBundleUri;
  }

  public void setResourceBundleUri( String resourceBundleUri ) {
    this.resourceBundleUri = resourceBundleUri;
  }

  public ArrayList<XulOverlay> getOverlays() {
    return overlays;
  }

  public void setOverlays( ArrayList<XulOverlay> overlays ) {
    this.overlays = overlays;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective#getLayoutPriority()
   */
  public int getLayoutPriority() {
    return layoutPriority;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective#setLayoutPriority(int)
   */
  public void setLayoutPriority( int layoutPriority ) {
    this.layoutPriority = layoutPriority;
  }

  public ArrayList<String> getRequiredSecurityActions() {
    return securityActions;
  }

  public void setRequiredSecurityActions( ArrayList<String> securityActions ) {
    this.securityActions = securityActions;
  }
}
