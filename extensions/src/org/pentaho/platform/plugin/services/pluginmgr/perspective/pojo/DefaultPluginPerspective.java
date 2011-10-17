package org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo;

import java.io.Serializable;
import java.util.ArrayList;

import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;

public class DefaultPluginPerspective implements Serializable, IPluginPerspective {

  private String id;
  private String title;
  private String contentUrl;
  private XulOverlay menuBarOverlay;
  private XulOverlay toolBarOverlay;
  private int layoutPriority;
  private ArrayList<String> securityActions;
  
  public DefaultPluginPerspective() {
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }

  public XulOverlay getMenuBarOverlay() {
    return menuBarOverlay;
  }

  public void setMenuBarOverlay(XulOverlay menuBarOverlay) {
    this.menuBarOverlay = menuBarOverlay;
  }

  public XulOverlay getToolBarOverlay() {
    return toolBarOverlay;
  }

  public void setToolBarOverlay(XulOverlay toolBarOverlay) {
    this.toolBarOverlay = toolBarOverlay;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective#getLayoutPriority()
   */
  public int getLayoutPriority() {
    return layoutPriority;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective#setLayoutPriority(int)
   */
  public void setLayoutPriority(int layoutPriority) {
    this.layoutPriority = layoutPriority;
  }

  public ArrayList<String> getRequiredSecurityActions() {
    return securityActions;
  }

  public void setRequiredSecurityActions(ArrayList<String> securityActions) {
    this.securityActions = securityActions;
  }
}