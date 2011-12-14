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
package org.pentaho.mantle.client.ui.xul;

import java.util.Map;

import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.SwitchThemeCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class MantleController extends AbstractXulEventHandler {

  private MantleModel model;

  private XulToolbarbutton openBtn;
  private XulToolbarbutton saveBtn;
  private XulToolbarbutton saveAsBtn;
  private XulToolbarbutton newAdhocBtn;
  private XulToolbarbutton newAnalysisBtn;
  private XulToolbarbutton showBrowserBtn;
  private XulToolbarbutton contentEditBtn;

  private XulMenuitem propertiesMenuItem;
  private XulMenuitem saveMenuItem;
  private XulMenuitem saveAsMenuItem;
  private XulMenuitem showBrowserMenuItem;
  private XulMenuitem showWorkspaceMenuItem;
  private XulMenuitem useDescriptionsMenuItem;
  private XulMenuitem showHiddenFilesMenuItem;

  private XulMenubar languageMenu;
  private XulMenubar themesMenu;
  private XulMenubar toolsMenu;

  public MantleController(MantleModel model) {
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

    propertiesMenuItem = (XulMenuitem) document.getElementById("propertiesMenuItem");
    saveMenuItem = (XulMenuitem) document.getElementById("saveMenuItem");
    saveAsMenuItem = (XulMenuitem) document.getElementById("saveAsMenuItem");
    showBrowserMenuItem = (XulMenuitem) document.getElementById("showBrowserMenuItem");
    showWorkspaceMenuItem = (XulMenuitem) document.getElementById("showWorkspaceMenuItem");
    useDescriptionsMenuItem = (XulMenuitem) document.getElementById("useDescriptionsMenuItem");
    showHiddenFilesMenuItem = (XulMenuitem) document.getElementById("showHiddenFilesMenuItem");
    languageMenu = (XulMenubar) document.getElementById("languagemenu");
    themesMenu = (XulMenubar) document.getElementById("themesmenu");
    toolsMenu = (XulMenubar) document.getElementById("toolsmenu");

    // install language sub-menus
    Map<String, String> supportedLanguages = Messages.getResourceBundle().getSupportedLanguages();
    if (supportedLanguages != null && supportedLanguages.keySet() != null && !supportedLanguages.isEmpty()) {
      MenuBar langMenu = (MenuBar) languageMenu.getManagedObject();
      for (String lang : supportedLanguages.keySet()) {
        MenuItem langMenuItem = new MenuItem(supportedLanguages.get(lang), new SwitchLocaleCommand(lang)); //$NON-NLS-1$
        langMenuItem.getElement().setId(supportedLanguages.get(lang) + "_menu_item");
        langMenu.addItem(langMenuItem);
      }
    }

    // install themes
    MantleServiceCache.getService().getActiveTheme(new AsyncCallback<String>() {
      public void onFailure(Throwable throwable) {
      }

      public void onSuccess(final String activeTheme) {
        MantleServiceCache.getService().getSystemThemes(new AsyncCallback<Map<String, String>>() {
          public void onFailure(Throwable throwable) {

          }

          public void onSuccess(Map<String, String> strings) {
            for (String themeId : strings.keySet()) {
              PentahoMenuItem themeMenuItem = new PentahoMenuItem(strings.get(themeId), new SwitchThemeCommand(themeId)); //$NON-NLS-1$
              themeMenuItem.getElement().setId(themeId + "_menu_item");
              themeMenuItem.setChecked(themeId.equals(activeTheme));
              ((MenuBar) themesMenu.getManagedObject()).addItem(themeMenuItem);
            }
          }
        });
      }
    });

    MantleServiceCache.getService().isAdministrator(new AsyncCallback<Boolean>() {
      public void onFailure(Throwable caught) {
      }

      public void onSuccess(Boolean isAdministrator) {
        toolsMenu.setVisible(isAdministrator);
      }
    });

    bf.createBinding(model, "propertiesEnabled", propertiesMenuItem, "!disabled");
    bf.createBinding(model, "saveEnabled", saveMenuItem, "!disabled");
    bf.createBinding(model, "saveAsEnabled", saveAsMenuItem, "!disabled");

    // init known values
    ((PentahoMenuItem) showBrowserMenuItem.getManagedObject()).setChecked(SolutionBrowserPanel.getInstance().isNavigatorShowing());
    ((PentahoMenuItem) showWorkspaceMenuItem.getManagedObject()).setChecked("workspace.perspective".equals(PerspectiveManager.getInstance()
        .getActivePerspective().getId()));

    setupNativeHooks(this);
  }

  public native void setupNativeHooks(MantleController controller)
  /*-{
    $wnd.mantle_isToolbarButtonEnabled = function(id) { 
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::isToolbarButtonEnabled(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_setToolbarButtonEnabled = function(id, enabled) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::setToolbarButtonEnabled(Ljava/lang/String;Z)(id, enabled);      
    }
    $wnd.mantle_doesToolbarButtonExist = function(id) { 
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::doesToolbarButtonExist(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_isMenuItemEnabled = function(id) { 
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::isMenuItemEnabled(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_setMenuItemEnabled = function(id, enabled) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::setMenuItemEnabled(Ljava/lang/String;Z)(id, enabled);      
    }
    $wnd.mantle_doesMenuItemExist = function(id) { 
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::doesMenuItemExist(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_loadOverlay = function(id) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::loadOverlay(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_removeOverlay = function(id) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::removeOverlay(Ljava/lang/String;)(id);      
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
    } catch (Throwable t) {
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
    return "mantleXulHandler";
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

  public MantleModel getModel() {

    return model;
  }

  public void setModel(MantleModel model) {

    this.model = model;
  }

  public boolean isMenuItemEnabled(String id) {
    XulMenuitem item = (XulMenuitem) document.getElementById(id);
    return !item.isDisabled();
  }

  public void setMenuItemEnabled(String id, boolean enabled) {
    XulMenuitem item = (XulMenuitem) document.getElementById(id);
    item.setDisabled(!enabled);
  }

  public boolean doesMenuItemExist(String id) {
    try {
      XulMenuitem item = (XulMenuitem) document.getElementById(id);
      return (item != null);
    } catch (Throwable t) {
      return false;
    }
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
  public boolean isSaveEnabled() {
    return !saveMenuItem.isDisabled();
  }

  @Bindable
  public boolean isSaveAsEnabled() {
    return !saveAsMenuItem.isDisabled();
  }

  @Bindable
  public void propertiesClicked() {
    model.executePropertiesCommand();
  }

  @Bindable
  public void shareContentClicked() {
    model.executeShareContent();
  }

  @Bindable
  public void scheduleContentClicked() {
    model.executeScheduleContent();
  }

  @Bindable
  public void showWorkspaceClicked() {
    boolean checked = ((PentahoMenuItem) showWorkspaceMenuItem.getManagedObject()).isChecked();
    ((PentahoMenuItem) showWorkspaceMenuItem.getManagedObject()).setChecked(!checked);
    model.toggleShowWorkspace();
  }

  @Bindable
  public void useDescriptionsForTooltipsClicked() {
    boolean checked = ((PentahoMenuItem) useDescriptionsMenuItem.getManagedObject()).isChecked();
    ((PentahoMenuItem) useDescriptionsMenuItem.getManagedObject()).setChecked(!checked);
    model.toggleUseDescriptionsForTooltips();
  }

  @Bindable
  public void showHiddenFilesClicked() {
    boolean checked = ((PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject()).isChecked();
    ((PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject()).setChecked(!checked);
    SolutionBrowserPanel.getInstance().toggleShowHideFilesCommand.execute();
  }

  @Bindable
  public void refreshContent() {
    model.refreshContent();
  }

  @Bindable
  public void documentationClicked() {
    model.openDocumentation();
  }

  public void loadOverlay(String id) {
    // TODO We need to convert ths to use the common interface method,
    // once they become available
    GwtXulDomContainer container = (GwtXulDomContainer) getXulDomContainer();
    try {
      container.loadOverlay(id);
    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void removeOverlay(String id) {
    // TODO We need to convert ths to use the common interface method,
    // once they become available
    GwtXulDomContainer container = (GwtXulDomContainer) getXulDomContainer();
    try {
      container.removeOverlay(id);
    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
