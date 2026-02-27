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
