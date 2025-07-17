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

import jakarta.xml.bind.annotation.XmlRootElement;
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
