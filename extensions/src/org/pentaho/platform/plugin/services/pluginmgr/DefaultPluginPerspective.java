package org.pentaho.platform.plugin.services.pluginmgr;

import org.pentaho.platform.api.engine.IPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;

public class DefaultPluginPerspective implements IPluginPerspective {

  private String id;
  private String title;
  private String contentUrl;
  private XulOverlay menuBarOverlay;
  private XulOverlay toolBarOverlay;

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

}