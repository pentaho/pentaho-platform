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
package org.pentaho.mantle.client.ui.menubar;

import java.util.Map;

import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.SwitchThemeCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.service.MantleServiceCache;
import org.pentaho.mantle.client.ui.toolbar.MainToolbarController;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

public class MainMenubarController extends AbstractXulEventHandler {

  private MainMenubarModel model;
  private XulMenuitem propertiesMenuItem;
  private XulMenuitem saveMenuItem;
  private XulMenuitem saveAsMenuItem;
  private XulMenuitem showBrowserMenuItem;
  private XulMenuitem showWorkspaceMenuItem;

  private XulMenubar languageMenu;
  private XulMenubar themesMenu;
  private XulMenubar toolsMenu;

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
    showBrowserMenuItem = (XulMenuitem) document.getElementById("showBrowserMenuItem");
    showWorkspaceMenuItem = (XulMenuitem) document.getElementById("showWorkspaceMenuItem");
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
              CheckBoxMenuItem themeMenuItem = new CheckBoxMenuItem(strings.get(themeId), new SwitchThemeCommand(themeId)); //$NON-NLS-1$
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

  @Bindable
  public void showBrowserClicked() {
    boolean checked = ((CheckBoxMenuItem) showBrowserMenuItem.getManagedObject()).isChecked();
    ((CheckBoxMenuItem) showBrowserMenuItem.getManagedObject()).setChecked(!checked);
    model.toggleShowBrowser();
  }
  
  @Bindable
  public void showWorkspaceClicked() {
    boolean checked = ((CheckBoxMenuItem) showWorkspaceMenuItem.getManagedObject()).isChecked();
    ((CheckBoxMenuItem) showWorkspaceMenuItem.getManagedObject()).setChecked(!checked);
    model.toggleShowWorkspace();
  }

  @Bindable
  public void useDescriptionsForTooltipsClicked() {
    model.toggleUseDescriptionsForTooltips();
  }

  @Bindable
  public void refreshContent() {
    model.refreshContent();
  }

  @Bindable
  public void documentationClicked() {
    model.openDocumentation();
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
