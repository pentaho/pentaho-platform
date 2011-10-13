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
 * Copyright 2008 Pentaho Corporation.  All rights reserved.
 *
 * Created Mar 25, 2008
 * @author Michael D'Amour
 */
package org.pentaho.mantle.client;

import java.util.ArrayList;
import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.commands.LoginCommand;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.toolbars.XulMainToolbar;
import org.pentaho.mantle.client.ui.PerspectiveSwitcher;
import org.pentaho.mantle.client.usersettings.IMantleSettingsListener;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.platform.api.usersettings.pojo.IUserSetting;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleApplication implements IUserSettingsListener, IMantleSettingsListener {

  public static boolean showAdvancedFeatures = false;

  public static String mantleRevisionOverride = null;

  // menu items (to be enabled/disabled)
  private MantleMainMenuBar menuBar;

  // solution browser view
  private SolutionBrowserPanel solutionBrowserPerspective;

  private CommandExec commandExec = GWT.create(CommandExec.class);

  private DeckPanel contentDeck = new DeckPanel();

  // Floating clear div that when shown intercepts mouse events.
  public static AbsolutePanel overlayPanel = new AbsolutePanel();

  private static MantleApplication instance;

  private MantleApplication() {
  }

  public static MantleApplication getInstance() {
    if (instance == null) {
      instance = new MantleApplication();
    }
    return instance;
  }

  public void loadApplication() {
    menuBar = new MantleMainMenuBar();
    solutionBrowserPerspective = SolutionBrowserPanel.getInstance(menuBar);

    // registered our native JSNI hooks
    setupNativeHooks(this, new LoginCommand());

    // listen to any reloads of user settings
    UserSettingsManager.getInstance().addUserSettingsListener(this);

    // listen to any reloads of mantle settings
    MantleSettingsManager.getInstance().addMantleSettingsListener(this);
  }

  public native void setupNativeHooks(MantleApplication mantle, LoginCommand loginCmd)
  /*-{
    $wnd.mantle_initialized = true;
    $wnd.mantle_showMessage = function(title, message) {
      mantle.@org.pentaho.mantle.client.MantleApplication::showMessage(Ljava/lang/String;Ljava/lang/String;)(title, message);
    }
    
    $wnd.addGlassPaneListener = function(callback) { 
      if($wnd.addDataAccessGlassPaneListener){
        $wnd.addDataAccessGlassPaneListener(callback);
      }
      mantle.@org.pentaho.mantle.client.MantleApplication::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
    
    $wnd.executeCommand = function(commandName) { 
      mantle.@org.pentaho.mantle.client.MantleApplication::executeCommand(Ljava/lang/String;)(commandName);      
    }
    
    $wnd.authenticate = function(callback) {
      loginCmd.@org.pentaho.mantle.client.commands.LoginCommand::loginWithCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }    
    
    $wnd.urlCommand = function(url, title, showInDialog, dialogWidth, dialogHeight) {
      @org.pentaho.mantle.client.commands.UrlCommand::_execute(Ljava/lang/String;Ljava/lang/String;ZII)(url, title, showInDialog, dialogWidth, dialogHeight);
    }
        
  }-*/;

  private void executeCommand(String commandName) {
    commandExec.execute(commandName);
  }

  private void addGlassPaneListener(JavaScriptObject obj) {
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
  }

  /**
   * This method is used by things like jpivot in order to show a 'mantle' looking alert dialog instead of a standard alert dialog.
   * 
   * @param title
   * @param message
   */
  private void showMessage(String title, String message) {
    MessageDialogBox dialog = new MessageDialogBox(title, message, true, false, true);
    dialog.center();
  }

  public void onFetchUserSettings(ArrayList<IUserSetting> settings) {
    if (settings == null) {
      return;
    }

    for (IUserSetting setting : settings) {
      try {
        if (IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals(setting.getSettingName())) {
          boolean showNavigator = "true".equals(setting.getSettingValue()); //$NON-NLS-1$
          solutionBrowserPerspective.setNavigatorShowing(showNavigator);
        }
      } catch (Exception e) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), Messages.getString("couldNotGetUserSettings"), false, false, true); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }
    }
  }

  public void onFetchMantleSettings(final HashMap<String, String> settings) {
    mantleRevisionOverride = settings.get("user-console-revision");
    FlexTable menuAndLogoPanel = new FlexTable();
    menuAndLogoPanel.setCellPadding(0);
    menuAndLogoPanel.setCellSpacing(0);
    menuAndLogoPanel.setStyleName("menuBarAndLogoPanel"); //$NON-NLS-1$
    menuAndLogoPanel.setWidth("100%"); //$NON-NLS-1$

    if ("true".equals(settings.get("show-menu-bar"))) {
      menuAndLogoPanel.setWidget(0, 0, menuBar);
      menuAndLogoPanel.getCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_MIDDLE);
    }

    menuAndLogoPanel.setWidget(0, 1, new PerspectiveSwitcher());
    menuAndLogoPanel.getCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT);
    menuAndLogoPanel.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_MIDDLE);

    if ("true".equals(settings.get("show-main-toolbar"))) {
      XulMainToolbar mainToolbar = XulMainToolbar.getInstance();
      menuAndLogoPanel.setWidget(1, 0, mainToolbar);
      menuAndLogoPanel.getFlexCellFormatter().setColSpan(1, 0, 2);
      mainToolbar.setWidth("100%"); //$NON-NLS-1$
    }

    VerticalPanel mainApplicationPanel = new VerticalPanel();
    mainApplicationPanel.setStyleName("applicationShell"); //$NON-NLS-1$
    mainApplicationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
    mainApplicationPanel.add(menuAndLogoPanel);
    mainApplicationPanel.setCellHeight(menuAndLogoPanel, "1px"); //$NON-NLS-1$

    // update supported file types
    PluginOptionsHelper.buildEnabledOptionsList(settings);

    // show stuff we've created/configured
    contentDeck.add(solutionBrowserPerspective);
    contentDeck.showWidget(contentDeck.getWidgetIndex(solutionBrowserPerspective));
    contentDeck.setStyleName("applicationShell");
    
    mainApplicationPanel.add(contentDeck);

    // menubar=no,location=no,resizable=yes,scrollbars=no,status=no,width=1200,height=800
    RootPanel.get().add(mainApplicationPanel);
    RootPanel.get().add(WaitPopup.getInstance());

    // Add in the overlay panel
    overlayPanel.setVisible(false);
    overlayPanel.setHeight("100%");
    overlayPanel.setWidth("100%");
    overlayPanel.getElement().getStyle().setProperty("zIndex", "1000");
    overlayPanel.getElement().getStyle().setProperty("position", "absolute");
    RootPanel.get().add(overlayPanel, 0, 0);

    showAdvancedFeatures = "true".equals(settings.get("show-advanced-features")); //$NON-NLS-1$ //$NON-NLS-2$

    String moduleBaseURL = GWT.getModuleBaseURL();
    String moduleName = GWT.getModuleName();
    String contextURL = moduleBaseURL.substring(0, moduleBaseURL.lastIndexOf(moduleName));
    String restUrl = contextURL + "api/repo/files/canAdminister"; //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, restUrl);
    builder.setCallback(new RequestCallback() {

      @Override
      public void onError(Request arg0, Throwable arg1) {
        MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
        dialogBox.center();
      }

      @SuppressWarnings("deprecation")
      @Override
      public void onResponseReceived(Request arg0, Response response) {
        Boolean isAdministrator = Boolean.parseBoolean(response.getText());
        solutionBrowserPerspective.setAdministrator(isAdministrator);
        menuBar.buildMenuBar(settings, isAdministrator);

        int numStartupURLs = Integer.parseInt(settings.get("num-startup-urls")); //$NON-NLS-1$
        for (int i = 0; i < numStartupURLs; i++) {
          String url = settings.get("startup-url-" + (i + 1)); //$NON-NLS-1$
          String name = settings.get("startup-name-" + (i + 1)); //$NON-NLS-1$
          if (url != null && !"".equals(url)) { //$NON-NLS-1$
            solutionBrowserPerspective.getContentTabPanel().showNewURLTab(name != null ? name : url, url, url, false);
          }
        }
        if (solutionBrowserPerspective.getContentTabPanel().getWidgetCount() > 0) {
          solutionBrowserPerspective.getContentTabPanel().selectTab(0);
        }

        // startup-url on the URL for the app, wins over user-settings
        String startupURL = Window.Location.getParameter("startup-url"); //$NON-NLS-1$
        if (startupURL != null && !"".equals(startupURL)) { //$NON-NLS-1$
          String title = Window.Location.getParameter("name"); //$NON-NLS-1$
          startupURL = URL.decodeComponent(startupURL);
          solutionBrowserPerspective.getContentTabPanel().showNewURLTab(title, title, startupURL, false);
        }
      }
    });
    try {
      builder.send();
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  public DeckPanel getContentDeck() {
    return contentDeck;
  }

  public void setContentDeck(DeckPanel contentDeck) {
    this.contentDeck = contentDeck;
  }
}
