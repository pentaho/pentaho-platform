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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.admin.EmailAdminPanelController;
import org.pentaho.mantle.client.admin.ISysAdminPanel;
import org.pentaho.mantle.client.admin.JsSysAdminPanel;
import org.pentaho.mantle.client.admin.SecurityPanel;
import org.pentaho.mantle.client.admin.UserRolesAdminPanelController;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.SwitchThemeCommand;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.AbstractFilePickList;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickList;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.IFilePickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.IFilePickListListener;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickList;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.JsSetting;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.tags.GwtConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.XulDialogCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

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

  private XulMenubar recentMenu;

  private XulMenubar favoriteMenu;

  private BindingFactory bf;

  HashMap<String, ISysAdminPanel> sysAdminPanelsMap = new HashMap<String, ISysAdminPanel>();

  RecentPickList recentPickList = RecentPickList.getInstance();

  FavoritePickList favoritePickList = FavoritePickList.getInstance();

  class SysAdminPanelInfo {
    String id;

    String url;

    public SysAdminPanelInfo() {

    };

    public SysAdminPanelInfo(String panelId, String panelUrl) {
      id = panelId;
      url = panelUrl;
    };
  }

  SysAdminPanelInfo adminPanelAwaitingActivation = null;

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

    bf = new GwtBindingFactory(document);
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
    recentMenu = (XulMenubar) document.getElementById("recentmenu");
    favoriteMenu = (XulMenubar) document.getElementById("favoritesmenu");

    //let the manager have access to these menu items to toggle checks off and on 
    PerspectiveManager.getInstance().setBrowserMenuItem((PentahoMenuItem) showBrowserMenuItem.getManagedObject());
    PerspectiveManager.getInstance().setWorkspaceMenuItem((PentahoMenuItem) showWorkspaceMenuItem.getManagedObject());
    ((PentahoMenuItem)showBrowserMenuItem.getManagedObject()).setChecked(true);
    
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
    buildFavoritesAndRecent(false);
    
    //Bindings to keep menu and toolbar in sync with BrowserPanel state showBrowserSelected
    final List<Binding> bindingsToUpdate = new ArrayList<Binding>();
  
    //For the menu item
    bindingsToUpdate.add(
        bf.createBinding(model, "showBrowserSelected", showBrowserMenuItem, "checked")
    );    
   
    UserSettingsManager.getInstance().addUserSettingsListener(new IUserSettingsListener() {

      @Override
      public void onFetchUserSettings(JsArray<JsSetting> settings) {
        if (settings == null) {
          return;
        }

        for (int i = 0; i < settings.length(); i++) {
          JsSetting setting = settings.get(i);
          try {
            if (IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals(setting.getName())) {
              boolean showNavigator = "true".equals(setting.getValue()); //$NON-NLS-1$

              model.setShowNavigatorSelected(showNavigator);

              for (Binding b : bindingsToUpdate) {
                try {
                  b.fireSourceChanged();
                } catch (Exception e) {
                  e.printStackTrace();
                }
              }

            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals(setting.getName())) {
              boolean checked = "true".equals(setting.getValue()); //$NON-NLS-1$
              ((PentahoMenuItem) useDescriptionsMenuItem.getManagedObject()).setChecked(checked);
            } else if (IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals(setting.getName())) {
              boolean checked = "true".equals(setting.getValue()); //$NON-NLS-1$
              ((PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject()).setChecked(checked);
            }
          } catch (Exception e) {
            MessageDialogBox dialogBox = new MessageDialogBox(
                Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
            dialogBox.center();
          }
        }
      }

    });

    // install themes
    RequestBuilder getActiveThemeRequestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()
        + "api/theme/active");
    try {
      getActiveThemeRequestBuilder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          // showError(exception);
        }

        public void onResponseReceived(Request request, Response response) {
          final String activeTheme = response.getText();
          RequestBuilder getThemesRequestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()
              + "api/theme/list");
          getThemesRequestBuilder.setHeader("accept", "application/json");

          try {
            getThemesRequestBuilder.sendRequest(null, new RequestCallback() {
              public void onError(Request arg0, Throwable arg1) {
              }

              public void onResponseReceived(Request request, Response response) {
                try {
                  final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
                  RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
                  requestBuilder.setHeader("accept", "text/plain");
                  requestBuilder.sendRequest(null, new RequestCallback() {

                    public void onError(Request request, Throwable caught) {
                    }

                    public void onResponseReceived(Request request, Response response) {
                      toolsMenu.setVisible("true".equalsIgnoreCase(response.getText()));
                    }

                  });
                } catch (RequestException e) {
                  Window.alert(e.getMessage());
                }

                JsArray<JsTheme> themes = JsTheme.getThemes(JsonUtils.escapeJsonForEval(response.getText()));

                for (int i = 0; i < themes.length(); i++) {
                  JsTheme theme = themes.get(i);
                  PentahoMenuItem themeMenuItem = new PentahoMenuItem(theme.getName(), new SwitchThemeCommand(theme
                      .getId())); //$NON-NLS-1$
                  themeMenuItem.getElement().setId(theme.getId() + "_menu_item");
                  themeMenuItem.setChecked(theme.getId().equals(activeTheme));
                  ((MenuBar) themesMenu.getManagedObject()).addItem(themeMenuItem);
                }

                bf.createBinding(model, "propertiesEnabled", propertiesMenuItem, "!disabled");
                bf.createBinding(model, "saveEnabled", saveMenuItem, "!disabled");
                bf.createBinding(model, "saveAsEnabled", saveAsMenuItem, "!disabled");

                PerspectiveManager.getInstance().addPerspectivesLoadedCallback(new ICallback<Void>() {
                  public void onHandle(Void v) {
                    executeAdminContent();
                  }
                });

                setupNativeHooks(MantleController.this);
              }
            });

          } catch (RequestException e) {
            // showError(e);
          }
        }

      });

    } catch (RequestException e) {
      Window.alert(e.getMessage());
      // showError(e);
    }
  }

  /**
   * 
   * @param force Force the reload of user settings from server
   * rather than use cache.
   * 
   */
  public void buildFavoritesAndRecent(boolean force) {

    loadRecentAndFavorites(force);
    refreshPickListMenu(recentMenu, recentPickList);
    refreshPickListMenu(favoriteMenu, favoritePickList);

    recentPickList.addItemsChangedListener(new IFilePickListListener<RecentPickItem>() {

      public void itemsChanged(AbstractFilePickList<RecentPickItem> filePickList) {
        refreshPickListMenu(recentMenu, recentPickList);
        recentPickList.save("recent");
      }
    });

    favoritePickList.addItemsChangedListener(new IFilePickListListener<FavoritePickItem>() {

      public void itemsChanged(AbstractFilePickList<FavoritePickItem> filePickList) {
        refreshPickListMenu(favoriteMenu, favoritePickList);
        favoritePickList.save("favorites");
      }
    });
  }

  /**
   * Loads an arbitrary <code>FilePickList</code> into a menu
   *  
   * @param pickMenu  The XulMenuBar to host the menu entries
   * @param filePickList The files to list in natural order
   */
  private void refreshPickListMenu(XulMenubar pickMenu, final AbstractFilePickList<? extends IFilePickItem> filePickList) {
    final MenuBar menuBar = (MenuBar) pickMenu.getManagedObject();
    menuBar.clearItems();

    if (filePickList.size() > 0) {
      for (IFilePickItem filePickItem : filePickList.getFilePickList()) {
        final String text = filePickItem.getFullPath();
        menuBar.addItem(filePickItem.getTitle(), new Command() {
          public void execute() {
            SolutionBrowserPanel.getInstance().openFile(text, COMMAND.RUN);
          }
        });
      }
      menuBar.addSeparator();
      menuBar.addItem(Messages.getString("clearItems"), new Command() {
        public void execute() {
          //confirm the clear
          GwtConfirmBox warning = new GwtConfirmBox();
          warning.setHeight(117);
          warning.setMessage(Messages.getString("clearItemsMessage"));
          warning.setTitle(Messages.getString("clearItems"));
          warning.addDialogCallback(new XulDialogCallback<String>() {
            public void onClose(XulComponent sender, Status returnCode, String retVal) {
              if (returnCode == Status.ACCEPT) {
                filePickList.clear();
              }
            }

            public void onError(XulComponent sender, Throwable t) {
            }
          });
          warning.show();
        }
      });
    } else {
      menuBar.addItem(Messages.getString("empty"), new Command() {
        public void execute() {
          //Do nothing
        }
      });
    }
  }

  private void loadRecentAndFavorites(boolean force) {
    UserSettingsManager.getInstance().fetchUserSettings(new AsyncCallback<JsArray<JsSetting>>() {

      public void onSuccess(JsArray<JsSetting> result) {
        JsSetting setting;
        for (int j = 0; j < result.length(); j++) {
          setting = result.get(j);
          if ("favorites".equalsIgnoreCase(setting.getName())) {
            try {
              // handle favorite
              JSONArray favorites = JSONParser.parseLenient(setting.getValue()).isArray();
              if (favorites != null) {
                // Create the FavoritePickList object from the JSONArray
                favoritePickList = FavoritePickList.getInstanceFromJSON(favorites);
              } else {
                favoritePickList = FavoritePickList.getInstance();
              }
            } catch (Throwable t) {
            }
          } else if ("recent".equalsIgnoreCase(setting.getName())) {
            try {
              // handle recent
              JSONArray recents = JSONParser.parseLenient(setting.getValue()).isArray();
              if (recents != null) {
                // Create the RecentPickList object from the JSONArray
                recentPickList = RecentPickList.getInstanceFromJSON(recents);
              } else {
                recentPickList = RecentPickList.getInstance();
              }
              recentPickList.setMaxSize(10);
            } catch (Throwable t) {
            }
          }
        }
      }

      public void onFailure(Throwable caught) {
      }

    }, force);
  }

  private void executeAdminContent() {

    try {
      RequestCallback internalCallback = new RequestCallback() {

        public void onError(Request request, Throwable exception) {
        }

        public void onResponseReceived(Request request, Response response) {
          JsArray<JsSetting> jsSettings = null;
          try {
            jsSettings = JsSetting.parseSettingsJson(response.getText());
          } catch (Throwable t) {
            // happens when there are no settings
          }
          if (jsSettings == null) {
            return;
          }
          for (int i = 0; i < jsSettings.length(); i++) {
            String content = jsSettings.get(i).getValue();
            StringTokenizer nameValuePairs = new StringTokenizer(content, ";");
            String perspective = null, content_panel_id = null, content_url = null;
            for (int j = 0; j < nameValuePairs.countTokens(); j++) {
              String currentToken = nameValuePairs.tokenAt(j).trim();
              if (currentToken.startsWith("perspective=")) {
                perspective = currentToken.substring("perspective=".length());
              }
              if (currentToken.startsWith("content-panel-id=")) {
                content_panel_id = currentToken.substring("content-panel-id=".length());
              }
              if (currentToken.startsWith("content-url=")) {
                content_url = currentToken.substring("content-url=".length());
              }
            }
            if (perspective != null) {
              PerspectiveManager.getInstance().setPerspective(perspective);             
            }
            if (content_panel_id != null && content_url != null) {
              loadAdminContent(content_panel_id, content_url);
            }
            if (perspective == null && content_panel_id == null && content_url == null) {
              GwtMessageBox warning = new GwtMessageBox();
              warning.setTitle(Messages.getString("warning"));
              warning.setMessage(content);
              warning.setButtons(new Object[GwtMessageBox.ACCEPT]);
              warning.setAcceptLabel(Messages.getString("close"));
              warning.show();
            }
          }
        }
      };

      RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, GWT.getHostPageBaseURL()
          + "api/mantle/getAdminContent");
      builder.setHeader("accept", "application/json");
      builder.sendRequest(null, internalCallback);
      //TO DO Reset the menuItem click for browser and workspace here?
    } catch (RequestException e) {
    }
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
    $wnd.mantle_registerSysAdminPanel = function(sysAdminPanel) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::registerSysAdminPanel(Lorg/pentaho/mantle/client/admin/JsSysAdminPanel;)(sysAdminPanel);      
    } 
    $wnd.mantle_activateWaitingSecurityPanel = function(okToSwitchToNewPanel) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::activateWaitingSecurityPanel(Z)(okToSwitchToNewPanel);      
    } 
    $wnd.mantle_enableUsersRolesTreeItem = function(enabled) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::enableUsersRolesTreeItem(Z)(enabled);      
    } 
    $wnd.mantle_selectAdminCatTreeTreeItem = function(treeLabel) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::selectAdminCatTreeTreeItem(Ljava/lang/String;)(treeLabel);      
    } 
  }-*/;
  
  public void enableUsersRolesTreeItem(boolean enabled) {
    MantleXul.getInstance().enableUsersRolesTreeItem(enabled);
  }

  public void selectAdminCatTreeTreeItem(String treeLabel) {
    MantleXul.getInstance().selectAdminCatTreeTreeItem(treeLabel);
  }

  public void registerSysAdminPanel(JsSysAdminPanel sysAdminPanel) {
    sysAdminPanelsMap.put(sysAdminPanel.getId(), sysAdminPanel);
  }

  public void activateWaitingSecurityPanel(boolean activate) {
    if (activate && (adminPanelAwaitingActivation != null)) {
      for (int i = 0; i < MantleXul.getInstance().getAdminContentDeck().getWidgetCount(); i++) {
        Widget w = MantleXul.getInstance().getAdminContentDeck().getWidget(i);
        if (adminPanelAwaitingActivation.id.equals(w.getElement().getId())) {
          ISysAdminPanel sysAdminPanel = sysAdminPanelsMap.get(adminPanelAwaitingActivation.id);
          if (sysAdminPanel != null) {
            sysAdminPanel.activate();
          }
          break;
        }
      }

      GWT.runAsync(new RunAsyncCallback() {
        public void onSuccess() {
          if ((SecurityPanel.getInstance()).getId().equals(adminPanelAwaitingActivation.id)) {
            model.loadSecurityPanel();
            SecurityPanel.getInstance().getElement().setId((SecurityPanel.getInstance()).getId());
          } else if (UserRolesAdminPanelController.getInstance().getId().equals(adminPanelAwaitingActivation.id)) {
            model.loadUserRolesAdminPanel();
            UserRolesAdminPanelController.getInstance().getElement()
                .setId((UserRolesAdminPanelController.getInstance()).getId());
          } else if ((EmailAdminPanelController.getInstance()).getId().equals(adminPanelAwaitingActivation.id)) {
            model.loadEmailAdminPanel();
            EmailAdminPanelController.getInstance().getElement()
                .setId((EmailAdminPanelController.getInstance()).getId());
          } else {
            model.loadAdminContent(adminPanelAwaitingActivation.id, adminPanelAwaitingActivation.url);
          }         
        }

        public void onFailure(Throwable reason) {
        }
      });

    } else if (!activate) {
      adminPanelAwaitingActivation = null;
    }
  }

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
  private void disableMenuItemChecks(){
    ((PentahoMenuItem) showWorkspaceMenuItem.getManagedObject()).setChecked(false);
    ((PentahoMenuItem) showBrowserMenuItem.getManagedObject()).setChecked(false);
  }

  @Bindable
  public void showBrowserClicked() {
    model.setShowBrowserSelected(true);    
    model.showBrowser();   
    MantleApplication.getInstance().pucToolBarVisibility(true);  
  }
  
  @Bindable
  public void showNavigatorClicked(){
    model.setShowNavigatorSelected(!model.isShowNavigatorSelected());  
    ShowBrowserCommand showBrowserCommand = new ShowBrowserCommand(model.isShowNavigatorSelected());   
    showBrowserCommand.execute();
    //showBrowserBtn.setSelected(flag, false);
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

  private void passivateActiveSecurityPanels(final String idOfSecurityPanelToBeActivated,
      final String urlOfSecurityPanelToBeActivated) {
    adminPanelAwaitingActivation = new SysAdminPanelInfo(idOfSecurityPanelToBeActivated,
        urlOfSecurityPanelToBeActivated);
    int visiblePanelIndex = MantleXul.getInstance().getAdminContentDeck().getVisibleWidget();
    if (visiblePanelIndex >= 0) {
      String visiblePanelId = MantleXul.getInstance().getAdminContentDeck().getWidget(visiblePanelIndex).getElement()
          .getId();
      if ((visiblePanelId != null) && !visiblePanelId.equals(idOfSecurityPanelToBeActivated)) {
        ISysAdminPanel sysAdminPanel = sysAdminPanelsMap.get(visiblePanelId);
        if (sysAdminPanel != null) {
          sysAdminPanel.passivate(new AsyncCallback<Boolean>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(Boolean passivateComplete) {
              if (passivateComplete) {
                activateWaitingSecurityPanel(passivateComplete);
              }
            }
          });
        } else {
          activateWaitingSecurityPanel(true);
        }
      } else {
        activateWaitingSecurityPanel(false);
      }
    } else {
      activateWaitingSecurityPanel(true);
    }
  }

  @Bindable
  public void loadAdminContent(final String panelId, final String url) {
    this.disableMenuItemChecks();
    passivateActiveSecurityPanels(panelId, url);
  }

  @Bindable
  public void loadSecurityPanel() {
    GWT.runAsync(new RunAsyncCallback() {
      public void onSuccess() {
        String securityPanelId = SecurityPanel.getInstance().getId();
        if (!sysAdminPanelsMap.containsKey(securityPanelId)) {
          sysAdminPanelsMap.put(securityPanelId, SecurityPanel.getInstance());
        }
        loadAdminContent(securityPanelId, null);
      }

      public void onFailure(Throwable reason) {
      }
    });
  }

  @Bindable
  public void loadUserRolesAdminPanel() {
    GWT.runAsync(new RunAsyncCallback() {
      public void onSuccess() {
        String usersAndGroupsPanelId = UserRolesAdminPanelController.getInstance().getId();
        if (!sysAdminPanelsMap.containsKey(usersAndGroupsPanelId)) {
          sysAdminPanelsMap.put(usersAndGroupsPanelId, UserRolesAdminPanelController.getInstance());
        }
        loadAdminContent(usersAndGroupsPanelId, null);
      }

      public void onFailure(Throwable reason) {
      }
    });
  }

  @Bindable
  public void loadEmailAdminPanel() {
    GWT.runAsync(new RunAsyncCallback() {
      public void onSuccess() {
        String emailPanelId = EmailAdminPanelController.getInstance().getId();
        if (!sysAdminPanelsMap.containsKey(emailPanelId)) {
          sysAdminPanelsMap.put(emailPanelId, EmailAdminPanelController.getInstance());
        }
        loadAdminContent(emailPanelId, null);
      }

      public void onFailure(Throwable reason) {
      }
    });
  }

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

    executeEditContentCallback(SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().getFrame()
        .getElement(), model.isContentEditSelected());
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
    model.setShowBrowserSelected(false);    
    model.showWorkspace();   
    MantleApplication.getInstance().pucToolBarVisibility(false);
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