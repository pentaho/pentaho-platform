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

import java.util.HashMap;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.commands.LoginCommand;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.ui.xul.MantleXul;
import org.pentaho.mantle.client.usersettings.IMantleSettingsListener;
import org.pentaho.mantle.client.usersettings.IUserSettingsListener;
import org.pentaho.mantle.client.usersettings.JsSetting;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleApplication implements IUserSettingsListener, IMantleSettingsListener {

  public static boolean showAdvancedFeatures = false;

  public static String mantleRevisionOverride = null;
  public static boolean submitOnEnter = true;

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
    // registered our native JSNI hooks
    setupNativeHooks(this, new LoginCommand());

    // listen to any reloads of user settings
    UserSettingsManager.getInstance().addUserSettingsListener(this);

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

  public void onFetchUserSettings(JsArray<JsSetting> settings) {
    // listen to any reloads of mantle settings
    MantleSettingsManager.getInstance().addMantleSettingsListener(this);
  }

  public void onFetchMantleSettings(final HashMap<String, String> settings) {

    final String startupPerspective = Window.Location.getParameter("startupPerspective");

    mantleRevisionOverride = settings.get("user-console-revision");
    RootPanel.get("pucMenuBar").add(MantleXul.getInstance().getMenubar());
    if (!StringUtils.isEmpty(startupPerspective)) {
      RootPanel.get("pucMenuBar").setVisible(false);
    }

    RootPanel.get("pucPerspectives").add(PerspectiveManager.getInstance());
    if (!StringUtils.isEmpty(startupPerspective)) {
      RootPanel.get("pucPerspectives").setVisible(false);
    }

    RootPanel.get("pucToolBar").add(MantleXul.getInstance().getToolbar());
    if (!StringUtils.isEmpty(startupPerspective)) {
      RootPanel.get("pucToolBar").setVisible(false);
    }

    // update supported file types
    PluginOptionsHelper.buildEnabledOptionsList(settings);

    // show stuff we've created/configured
    contentDeck.add(new Label());
    contentDeck.showWidget(0);
    contentDeck.add(SolutionBrowserPanel.getInstance());
    if (!StringUtils.isEmpty(startupPerspective)) {
      SolutionBrowserPanel.getInstance().setVisible(false);
    }

    contentDeck.setStyleName("applicationShell");

    // menubar=no,location=no,resizable=yes,scrollbars=no,status=no,width=1200,height=800
    try {
      RootPanel.get("pucContent").add(contentDeck);
    } catch (Throwable t) {
      // onLoad of something is causing problems
    }

    RootPanel.get().add(WaitPopup.getInstance());

    // Add in the overlay panel
    overlayPanel.setVisible(false);
    overlayPanel.setHeight("100%");
    overlayPanel.setWidth("100%");
    overlayPanel.getElement().getStyle().setProperty("zIndex", "1000");
    overlayPanel.getElement().getStyle().setProperty("position", "absolute");
    RootPanel.get().add(overlayPanel, 0, 0);

    String showAdvancedFeaturesSetting = settings.get("show-advanced-features"); //$NON-NLS-1$ 
    showAdvancedFeatures = showAdvancedFeaturesSetting == null ? showAdvancedFeatures : Boolean.parseBoolean(showAdvancedFeaturesSetting);

    String submitOnEnterSetting = settings.get("submit-on-enter-key");
    submitOnEnter = submitOnEnterSetting == null ? submitOnEnter : Boolean.parseBoolean(submitOnEnterSetting);

    try {
      String restUrl = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, restUrl);
      requestBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onError(Request arg0, Throwable arg1) {
          MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
          dialogBox.center();
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onResponseReceived(Request arg0, Response response) {
          Boolean isAdministrator = Boolean.parseBoolean(response.getText());
          SolutionBrowserPanel.getInstance().setAdministrator(isAdministrator);

          try {
            String restUrl2 = GWT.getHostPageBaseURL() + "api/repo/files/canSchedule"; //$NON-NLS-1$
            RequestBuilder requestBuilder2 = new RequestBuilder(RequestBuilder.GET, restUrl2);
            requestBuilder2.sendRequest(null, new RequestCallback() {
              @Override
              public void onError(Request arg0, Throwable arg1) {
                MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), arg1.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
                dialogBox.center();
              }

              public void onResponseReceived(Request arg0, Response response) {
                Boolean isScheduler = Boolean.parseBoolean(response.getText());
                SolutionBrowserPanel.getInstance().setScheduler(isScheduler);

                String numStartupURLsSetting = settings.get("num-startup-urls");
                if (numStartupURLsSetting != null) {
                  int numStartupURLs = Integer.parseInt(numStartupURLsSetting); //$NON-NLS-1$
                  for (int i = 0; i < numStartupURLs; i++) {
                    String url = settings.get("startup-url-" + (i + 1)); //$NON-NLS-1$
                    String name = settings.get("startup-name-" + (i + 1)); //$NON-NLS-1$
                    if (url != null && !"".equals(url)) { //$NON-NLS-1$
                      SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab(name != null ? name : url, url, url, false);
                    }
                  }
                }
                if (SolutionBrowserPanel.getInstance().getContentTabPanel().getWidgetCount() > 0) {
                  SolutionBrowserPanel.getInstance().getContentTabPanel().selectTab(0);
                }

                // startup-url on the URL for the app, wins over settings
                String startupURL = Window.Location.getParameter("startup-url"); //$NON-NLS-1$
                if (startupURL != null && !"".equals(startupURL)) { //$NON-NLS-1$
                  String title = Window.Location.getParameter("name"); //$NON-NLS-1$
                  startupURL = URL.decodeComponent(startupURL);
                  SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab(title, title, startupURL, false);
                }
              }
            });
          } catch (RequestException e) {
            MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
            dialogBox.center();
          }
        }
      });
    } catch (RequestException e) {
      MessageDialogBox dialogBox = new MessageDialogBox(Messages.getString("error"), e.getLocalizedMessage(), false, false, true); //$NON-NLS-1$
      dialogBox.center();
    }

    if (!StringUtils.isEmpty(startupPerspective)) {
      ICallback<Void> callback = new ICallback<Void>() {
        public void onHandle(Void nothing) {
          PerspectiveManager.getInstance().setPerspective(startupPerspective);
        }
      };
      PerspectiveManager.getInstance().addPerspectivesLoadedCallback(callback);
    }

  }

  public DeckPanel getContentDeck() {
    return contentDeck;
  }

  public void setContentDeck(DeckPanel contentDeck) {
    this.contentDeck = contentDeck;
  }
}
