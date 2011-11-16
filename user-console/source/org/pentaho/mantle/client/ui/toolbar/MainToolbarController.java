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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 * 
 */
package org.pentaho.mantle.client.ui.toolbar;

import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;

/**
 * 
 * Warning: currently all methods must not take references. If
 * 
 * @author NBaker
 */
public class MainToolbarController extends AbstractXulEventHandler {

  private MainToolbarModel model;
  private XulToolbarbutton openBtn;
  private XulToolbarbutton saveBtn;
  private XulToolbarbutton saveAsBtn;
  private XulToolbarbutton newAdhocBtn;
  private XulToolbarbutton newAnalysisBtn;
  private XulToolbarbutton showBrowserBtn;
  private XulToolbarbutton contentEditBtn;

  public MainToolbarController(MainToolbarModel model) {
    this.model = model;
  }

  /**
   * Called when the Xul Dom is ready, grab all Xul references here.
   */
  @Bindable
  public void init() {
    openBtn = (XulToolbarbutton) document.getElementById("openButton");
    saveBtn = (XulToolbarbutton) document.getElementById("saveButton");
    saveAsBtn = (XulToolbarbutton) document.getElementById("saveAsButton");
    newAnalysisBtn = (XulToolbarbutton) document.getElementById("newAnalysisButton");
    showBrowserBtn = (XulToolbarbutton) document.getElementById("showBrowserButton");
    contentEditBtn = (XulToolbarbutton) document.getElementById("editContentButton");

    BindingFactory bf = new GwtBindingFactory(this.document);
    bf.createBinding(model, "saveEnabled", saveBtn, "!disabled");
    bf.createBinding(model, "saveAsEnabled", saveAsBtn, "!disabled");
    bf.createBinding(model, "contentEditEnabled", contentEditBtn, "!disabled");
    bf.createBinding(model, "contentEditSelected", this, "editContentSelected");

    setupNativeHooks(this);
  }

  public native void setupNativeHooks(MainToolbarController controller)
  /*-{
    $wnd.mantle_isToolbarButtonEnabled = function(id) { 
      return controller.@org.pentaho.mantle.client.ui.toolbar.MainToolbarController::isToolbarButtonEnabled(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_setToolbarButtonEnabled = function(id, enabled) { 
      controller.@org.pentaho.mantle.client.ui.toolbar.MainToolbarController::setToolbarButtonEnabled(Ljava/lang/String;Z)(id, enabled);      
    }
    $wnd.mantle_doesToolbarButtonExist = function(id) { 
      controller.@org.pentaho.mantle.client.ui.toolbar.MainToolbarController::doesToolbarButtonExist(Ljava/lang/String;)(id);      
    }
  }-*/;

  public boolean isToolbarButtonEnabled(String id) {
    XulToolbarbutton button = (XulToolbarbutton) document.getElementById(id);
    return !button.isDisabled();
  }

  public void setToolbarButtonEnabled(String id, boolean enabled) {
    XulToolbarbutton button = (XulToolbarbutton) document.getElementById(id);
    button.setDisabled(!enabled);
  }

  public boolean doesToolbarButtonExist(String id) {
    try {
      XulToolbarbutton button = (XulToolbarbutton) document.getElementById(id);
      return (button != null);
    } catch (Throwable t){
      return false;
    }
  }
  
  
  @Bindable
  public void setEditContentSelected(boolean selected) {
    contentEditBtn.setSelected(selected, false);
  }

  @Bindable
  public void openClicked() {
    model.executeOpenFileCommand();
  }

  @Bindable
  public void newAnalysisClicked() {
    model.executeAnalysisViewCommand();
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
  public void showBrowserClicked() {
    ShowBrowserCommand showBrowserCommand = new ShowBrowserCommand();
    showBrowserCommand.execute();
  }

  @Bindable
  public void setShowBrowserSelected(boolean flag) {
    // called by the MainToolbarModel to change state.
    showBrowserBtn.setSelected(flag);
  }

  @Bindable
  public void setSaveEnabled(boolean flag) {
    // called by the MainToolbarModel to change state.
    saveBtn.setDisabled(!flag);
  }

  @Bindable
  public void setSaveAsEnabled(boolean flag) {
    // called by the MainToolbarModel to change state.
    saveAsBtn.setDisabled(!flag);
  }

  @Bindable
  public void setNewAnalysisEnabled(boolean flag) {
    // called by the MainToolbarModel to change state.
    newAnalysisBtn.setDisabled(!flag);
  }

  @Override
  public String getName() {
    return "mainToolbarHandler";
  }

  @Bindable
  public void executeCallback(String jsScript) {
    executeJS(model.getCallback(), jsScript);
  }

  @Bindable
  public void executeMantleFunc(String funct) {
    executeMantleCall(funct);
  }

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
  
  private native void executeJS(JavaScriptObject obj, String js)
  /*-{
    try{
      var tempObj = obj;
      eval("tempObj."+js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+"          "+"tempObj."+js);
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

  @Bindable
  public void setContentEditEnabled(boolean enable) {
    contentEditBtn.setDisabled(!enable);
  }

  @Bindable
  public void setContentEditSelected(boolean selected) {
    contentEditBtn.setSelected(selected);
  }

  @Bindable
  /*
   * Notifies currently active Javascript callback of an edit event.
   */
  public void editContentClicked() {
    model.setContentEditToggled();

    executeEditContentCallback(SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().getFrame().getElement(), model.isContentEditSelected());
  }

  private native void executeEditContentCallback(Element obj, boolean selected)
  /*-{
    try {
      obj.contentWindow.editContentToggled(selected);
    } catch (e){if(console){console.log(e);}}
  }-*/;

  public MainToolbarModel getModel() {

    return model;
  }

  public void setModel(MainToolbarModel model) {

    this.model = model;
  }

}
