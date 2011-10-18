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
 */
package org.pentaho.mantle.client.menubar;

import org.pentaho.mantle.client.toolbars.MainToolbarController;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;

public class MainMenubarController extends AbstractXulEventHandler {

  private MainMenubarModel model;
  private XulMenuitem propertiesMenuItem;
  private XulMenuitem saveMenuItem;
  private XulMenuitem saveAsMenuItem;

  public MainMenubarController(MainMenubarModel model) {
    this.model = model;
  }

  /**
   * Called when the Xul Dom is ready, grab all Xul references here.
   */
  @Bindable
  public void init() {
    propertiesMenuItem = (XulMenuitem) document.getElementById("propertiesMenuItem");
    saveMenuItem = (XulMenuitem) document.getElementById("saveMenuItem");
    saveAsMenuItem = (XulMenuitem) document.getElementById("saveAsMenuItem");
    BindingFactory bf = new GwtBindingFactory(this.document);
    bf.createBinding(model, "propertiesEnabled", propertiesMenuItem, "!disabled");
    bf.createBinding(model, "saveEnabled", saveMenuItem, "!disabled");
    bf.createBinding(model, "saveAsEnabled", saveAsMenuItem, "!disabled");
  }

  public native void setupNativeHooks(MainToolbarController controller)
  /*-{
  }-*/;

  public boolean isMenuItemEnabled(String id) {
    XulMenuitem item = (XulMenuitem) document.getElementById(id);
    return !item.isDisabled();
  }

  public void setMenuItemEnabled(String id, boolean enabled) {
    XulMenuitem item = (XulMenuitem) document.getElementById(id);
    item.setDisabled(!enabled);
  }

  @Bindable
  public void setPropertiesEnabled(boolean enable) {
    propertiesMenuItem.setDisabled(!enable);
  }

  @Bindable
  public boolean isPropertiesEnabled() {
    return !propertiesMenuItem.isDisabled();
  }

  @Bindable
  public void setSaveEnabled(boolean enable) {
    saveMenuItem.setDisabled(!enable);
  }

  @Bindable
  public boolean isSaveEnabled() {
    return !saveMenuItem.isDisabled();
  }

  @Bindable
  public void setSaveAsEnabled(boolean enable) {
    saveAsMenuItem.setDisabled(!enable);
  }

  @Bindable
  public boolean isSaveAsEnabled() {
    return !saveAsMenuItem.isDisabled();
  }

  @Bindable
  public void saveClicked() {
    model.executeSaveCommand();
  }

  @Bindable
  public void saveAsClicked() {
    model.executeSaveAsCommand();
  }

  @Bindable
  public void propertiesClicked() {
    model.executePropertiesCommand();
  }

  @Bindable
  public void editContentClicked() {
    model.executeEditContent();
  }

  @Bindable
  public void shareContentClicked() {
    model.executeShareContent();
  }

  @Bindable
  public void scheduleContentClicked() {
    model.executeScheduleContent();
  }

  private native void executeJS(JavaScriptObject obj, String js)
  /*-{
    try{
      var tempObj = obj;
      eval("tempObj."+js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+" "+"tempObj."+js);
    }
  }-*/;

  @Bindable
  public native void openUrl(String title, String name, String uri)
  /*-{
    try {
      $wnd.eval("openURL('"+name+"','"+title+"','"+uri+"')");
    } catch (e) {
      $wnd.mantle_showMessage("Javascript Error",e.message);
    }
  }-*/;

  private native void executeMantleCall(String js)
  /*-{
    try{
      $wnd.eval(js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+"\n\n"+js);
    }
  }-*/;

  @Bindable
  public void executeMantleCommand(String cmd) {
    String js = "executeCommand('" + cmd + "')";
    executeMantleCall(js);
  }

  @Override
  public String getName() {
    return "mainMenubarHandler";
  }

  public MainMenubarModel getModel() {
    return model;
  }

  public void setModel(MainMenubarModel model) {
    this.model = model;
  }

}
