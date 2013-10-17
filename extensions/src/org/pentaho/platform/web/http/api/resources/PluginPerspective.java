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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;

@XmlRootElement
public class PluginPerspective implements Serializable {

  private static final long serialVersionUID = 629986294902644407L;

  private String id;
  private String title;
  private String contentUrl;
  private String resourceBundleUri;
  private ArrayList<Overlay> overlays;
  private int layoutPriority;
  private ArrayList<String> requiredSecurityActions;

  public PluginPerspective() {
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

  public ArrayList<Overlay> getOverlays() {
    return overlays;
  }

  public void setOverlays( ArrayList<Overlay> overlays ) {
    this.overlays = overlays;
  }

  public int getLayoutPriority() {
    return layoutPriority;
  }

  public void setLayoutPriority( int layoutPriority ) {
    this.layoutPriority = layoutPriority;
  }

  public ArrayList<String> getRequiredSecurityActions() {
    return requiredSecurityActions;
  }

  public void setRequiredSecurityActions( ArrayList<String> requiredSecurityActions ) {
    this.requiredSecurityActions = requiredSecurityActions;
  }

}
