/*!
 *
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
 *
 * Copyright (c) 2002-2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.mantle.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserDialog;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.commands.CommandExec;
import org.pentaho.mantle.client.commands.LoginCommand;
import org.pentaho.mantle.client.dialogs.WaitPopup;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.MantleSettingsLoadedEvent;
import org.pentaho.mantle.client.events.MantleSettingsLoadedEventHandler;
import org.pentaho.mantle.client.events.PerspectivesLoadedEvent;
import org.pentaho.mantle.client.events.UserSettingsLoadedEvent;
import org.pentaho.mantle.client.events.UserSettingsLoadedEventHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.ui.UserDropDown;
import org.pentaho.mantle.client.ui.xul.MantleXul;
import org.pentaho.mantle.client.usersettings.JsSetting;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class MantleApplication implements UserSettingsLoadedEventHandler, MantleSettingsLoadedEventHandler {

  public static String mantleRevisionOverride = null;
  public static boolean submitOnEnter = true;

  private static CommandExec commandExec = GWT.create( CommandExec.class );
  private static EventBusUtil eventBusUtil = GWT.create( EventBusUtil.class );

  private DeckPanel contentDeck = new DeckPanel();

  // Floating clear div that when shown intercepts mouse events.
  public static AbsolutePanel overlayPanel = new AbsolutePanel();

  private static MantleApplication instance;

  private static UserDropDown userDropDown;

  private MantleApplication() {
  }

  public static MantleApplication getInstance() {
    if ( instance == null ) {
      instance = new MantleApplication();
    }
    return instance;
  }

  public void loadApplication() {
    // registered our native JSNI hooks
    setupNativeHooks( this, new LoginCommand() );
    FileChooserDialog.setupNativeHooks();
    loadWhitelistedHosts();
    UserSettingsManager.getInstance().getUserSettings( new AsyncCallback<JsArray<JsSetting>>() {
      public void onSuccess( JsArray<JsSetting> settings ) {
        onUserSettingsLoaded( new UserSettingsLoadedEvent( settings ) );
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }
  
  private boolean canEdit() {
    return MantleXul.getInstance().isEditable();
  }
  public native void setupNativeHooks( MantleApplication mantle, LoginCommand loginCmd )
  /*-{
      $wnd.mantle_initialized = true;
      $wnd.canEdit = mantle.@org.pentaho.mantle.client.MantleApplication::canEdit();
      $wnd.mantle_showMessage = function (title, message) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          mantle.@org.pentaho.mantle.client.MantleApplication::showMessage(Ljava/lang/String;Ljava/lang/String;)(title, message);
      }

      $wnd.mantle_showConfirmDlg = function (title, message, okTxt, cancelTxt, callback) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          mantle.@org.pentaho.mantle.client.MantleApplication::showConfirmationDialog(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(title, message, okTxt, cancelTxt, callback);
      }

      $wnd.addGlassPaneListener = function (callback) {
          var daId = '';
          if ($wnd.addDataAccessGlassPaneListener) {
              daId = $wnd.addDataAccessGlassPaneListener(callback);
          }
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          return daId + "###" + mantle.@org.pentaho.mantle.client.MantleApplication::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
      }

      $wnd.removeGlassPaneListenerById = function (uuid) {
          var uuids = uuid.split('###');
          if ($wnd.removeDataAccessGlassPaneListenerById) {
              $wnd.removeDataAccessGlassPaneListenerById(uuids[0]);
          }
          mantle.@org.pentaho.mantle.client.MantleApplication::removeGlassPaneListenerById(Ljava/lang/String;)(uuids[1]);
      }

      $wnd.executeCommand = function (commandName, parameterMap) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          @org.pentaho.mantle.client.MantleApplication::executeCommand(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(commandName, parameterMap);
      }

      $wnd.authenticate = function (callback) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          loginCmd.@org.pentaho.mantle.client.commands.LoginCommand::loginWithCallback(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
      }

      $wnd.urlCommand = function (url, title, showInDialog, dialogWidth, dialogHeight) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          @org.pentaho.mantle.client.commands.UrlCommand::_execute(Ljava/lang/String;Ljava/lang/String;ZII)(url, title, showInDialog, dialogWidth, dialogHeight);
      }

      $wnd.mantle_addHandler = function (type, handler) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          return @org.pentaho.mantle.client.MantleApplication::addHandler(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(type, handler);
      }

      $wnd.mantle_removeHandler = function (handler) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          @org.pentaho.mantle.client.MantleApplication::removeHandler(Lcom/google/gwt/event/shared/HandlerRegistration;)(handler);
      }

      $wnd.mantle_fireEvent = function (type, parameterMap) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          @org.pentaho.mantle.client.MantleApplication::fireEvent(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(type, parameterMap);
      }

      // globally available busy indicator
      $wnd.mantle_notifyGlasspaneListeners = function (isShown) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          mantle.@org.pentaho.mantle.client.MantleApplication::notifyGlasspaneListeners(Z)(isShown);
      }
  }-*/;

  public static HandlerRegistration addHandler( final String type, final JavaScriptObject handler ) {
    return eventBusUtil.addHandler( type, handler );
  }

  public static void removeHandler( final HandlerRegistration handler ) {
    handler.removeHandler();
  }

  private static void fireEvent( final String eventType, final JavaScriptObject parametersMap ) {
    eventBusUtil.fireEvent( eventType, parametersMap );
  }

  public static native void showBusyIndicator( String title, String message )
  /*-{
      $wnd.require([
              "common-ui/util/BusyIndicator"
          ],

          function (busy) {
              busy.show(title, message);
          });
  }-*/;

  public static native void hideBusyIndicator()
  /*-{
      $wnd.require([
              "common-ui/util/BusyIndicator"
          ],

          function (busy) {
              busy.hide();
          });
  }-*/;

  public static native void showBusyIndicatorById( String title, String message, String id )
  /*-{
      $wnd.require([
              "common-ui/util/BusyIndicator"
          ],

          function (busy) {
              busy.show(title, message, id);
          });
  }-*/;

  public static native void hideBusyIndicatorById( String id )
  /*-{
      $wnd.require([
              "common-ui/util/BusyIndicator"
          ],

          function (busy) {
              busy.hide(id);
          });
  }-*/;

  public static native void log( String message )
    /*-{
        try {
            console.log(message);
        } catch (e) {
        }
    }-*/;

  private native void notifyDialogCallbackOk( JavaScriptObject callback )
    /*-{
      callback.onOk();
    }-*/;

  private native void notifyDialogCallbackCancel( JavaScriptObject callback )
    /*-{
      callback.onCancel();
    }-*/;

  public void notifyGlasspaneListeners( boolean isShown ) {
    if ( isShown ) {
      GlassPane.getInstance().show();
    } else {
      GlassPane.getInstance().hide();
    }
  }

  private static void executeCommand( String commandName, JavaScriptObject parameterMap ) {
    commandExec.execute( commandName, parameterMap );
  }

  private String addGlassPaneListener( JavaScriptObject obj ) {
    return GlassPane.getInstance().addGlassPaneListener( new GlassPaneNativeListener( obj ) );
  }

  private void removeGlassPaneListenerById( String uuid ) {
    GlassPane.getInstance().removeGlassPaneListenerById( uuid );
  }

  /**
   * This method is used by things to show a 'mantle' looking alert dialog instead of a standard alert dialog.
   *
   * @param title
   * @param message
   */
  private void showMessage( String title, String message ) {
    MessageDialogBox dialog = new MessageDialogBox( title, message, true );
    dialog.center();
  }

  /**
   * This method is used by things to show a 'mantle' looking confirmation dialog instead of a standard alert.
   *
   * @param title
   * @param message
   */
  private void showConfirmationDialog( String title, String message,
                                       String okText, String cancelText, final JavaScriptObject callback ) {
    MessageDialogBox dialog = new MessageDialogBox( title, message, true, false, true, okText, null, cancelText );
    dialog.setCallback( new IDialogCallback() {
      @Override
      public void okPressed() {
        notifyDialogCallbackOk( callback );
      }

      @Override
      public void cancelPressed() {
        notifyDialogCallbackCancel( callback );
      }
    } );
    dialog.center();
  }

  public void onUserSettingsLoaded( final UserSettingsLoadedEvent event ) {
    // listen to any reloads of mantle settings
    MantleSettingsManager.getInstance().getMantleSettings( new AsyncCallback<HashMap<String, String>>() {
      public void onSuccess( HashMap<String, String> mantleSettings ) {
        // merge user settings with mantle settings for possible system/tenant/user overrides
        JsArray<JsSetting> userSettings = event.getSettings();
        if ( userSettings != null ) {
          for ( int i = 0; i < userSettings.length(); i++ ) {
            JsSetting setting = userSettings.get( i );
            mantleSettings.put( setting.getName(), setting.getValue() );
          }
        }
        onMantleSettingsLoaded( new MantleSettingsLoadedEvent( mantleSettings ) );
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );
  }

  public void onMantleSettingsLoaded( MantleSettingsLoadedEvent event ) {
    final HashMap<String, String> settings = event.getSettings();

    final boolean showOnlyPerspective =
        Boolean.parseBoolean( StringUtils.isEmpty( Window.Location.getParameter( "showOnlyPerspective" ) ) ? settings.get( "showOnlyPerspective" ) : Window.Location.getParameter( "showOnlyPerspective" ) );
    final String startupPerspective =
        StringUtils.isEmpty( Window.Location.getParameter( "startupPerspective" ) ) ? settings.get( "startupPerspective" ) : Window.Location.getParameter( "startupPerspective" );

    mantleRevisionOverride = settings.get( "user-console-revision" );

    RootPanel.get( "pucMenuBar" ).add( MantleXul.getInstance().getMenubar() );
    RootPanel.get( "pucBurgerToolbar" ).add( MantleXul.getInstance().getBurgerToolbarWrapper() );
    RootPanel.get( "pucTabsMenuBar" ).add( MantleXul.getInstance().getTabsMenuBarWrapper() );
    RootPanel.get( "pucPerspectives" ).add( PerspectiveManager.getInstance() );
    RootPanel.get( "pucToolBar" ).add( MantleXul.getInstance().getToolbar() );
    RootPanel.get( "pucUserDropDown" ).add( getUserDropDown() );

    if ( showOnlyPerspective && !StringUtils.isEmpty( startupPerspective ) ) {
      RootPanel.get( "pucHeader" ).setVisible( false );
      RootPanel.get( "pucContent" ).getElement().getStyle().setTop( 0, Unit.PX );
    }

    // update supported file types
    PluginOptionsHelper.buildEnabledOptionsList( settings );

    // show stuff we've created/configured
    contentDeck.add( new Label() );
    contentDeck.showWidget( 0 );
    contentDeck.add( SolutionBrowserPanel.getInstance() );

    if ( showOnlyPerspective && !StringUtils.isEmpty( startupPerspective ) ) {
      SolutionBrowserPanel.getInstance().setVisible( false );
    }

    contentDeck.getElement().setId( "applicationShell" );
    contentDeck.setStyleName( "applicationShell" );

    // menubar=no,location=no,resizable=yes,scrollbars=no,status=no,width=1200,height=800
    try {
      RootPanel.get( "pucContent" ).add( contentDeck );
    } catch ( Throwable t ) {
      // onLoad of something is causing problems
    }

    RootPanel.get().add( WaitPopup.getInstance() );

    // Add in the overlay panel
    overlayPanel.setVisible( false );
    overlayPanel.setHeight( "100%" );
    overlayPanel.setWidth( "100%" );
    overlayPanel.getElement().getStyle().setProperty( "zIndex", "1000" );
    overlayPanel.getElement().getStyle().setProperty( "position", "absolute" );
    RootPanel.get().add( overlayPanel, 0, 0 );

    String submitOnEnterSetting = settings.get( "submit-on-enter-key" );
    submitOnEnter = submitOnEnterSetting == null ? submitOnEnter : Boolean.parseBoolean( submitOnEnterSetting );

    try {
      String restUrl = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, restUrl );
      requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      requestBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request arg0, Throwable arg1 ) {
          MessageDialogBox dialogBox =
              new MessageDialogBox( Messages.getString( "error" ), arg1.getLocalizedMessage(), false, false, true ); //$NON-NLS-1$
          dialogBox.center();
        }

        @Override
        public void onResponseReceived( Request arg0, Response response ) {
          boolean isAdministrator = Boolean.parseBoolean( response.getText() );
          SolutionBrowserPanel.getInstance().setAdministrator( isAdministrator );

          try {
            String restUrl2 = MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/canSchedule"; //$NON-NLS-1$
            RequestBuilder requestBuilder2 = new RequestBuilder( RequestBuilder.GET, restUrl2 );
            requestBuilder2.setHeader( "accept", "application/json" ); //$NON-NLS-1$ //$NON-NLS-2$
            requestBuilder2.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
            requestBuilder2.sendRequest( null, new RequestCallback() {
              @Override
              public void onError( Request arg0, Throwable arg1 ) {
                MessageDialogBox dialogBox =
                    new MessageDialogBox( Messages.getString( "error" ), arg1.getLocalizedMessage(), false, false, true ); //$NON-NLS-1$
                dialogBox.center();
              }

              public void onResponseReceived( Request arg0, Response response ) {
                boolean isScheduler = Boolean.parseBoolean( response.getText() );
                SolutionBrowserPanel.getInstance().setScheduler( isScheduler );

                if ( PerspectiveManager.getInstance().isLoaded() ) {
                  showStartupURL( settings );
                } else {
                  EventBusUtil.EVENT_BUS.addHandler( PerspectivesLoadedEvent.TYPE, event -> showStartupURL( settings ) );
                }
              }
            } );
          } catch ( RequestException e ) {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), e.getLocalizedMessage(), false, false, true ); //$NON-NLS-1$
            dialogBox.center();
          }
        }
      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox =
          new MessageDialogBox( Messages.getString( "error" ), e.getLocalizedMessage(), false, false, true ); //$NON-NLS-1$
      dialogBox.center();
    }

    if ( !StringUtils.isEmpty( startupPerspective ) ) {
      if ( PerspectiveManager.getInstance().isLoaded() ) {
        PerspectiveManager.getInstance().setPerspective( startupPerspective );
      } else {
        EventBusUtil.EVENT_BUS.addHandler( PerspectivesLoadedEvent.TYPE, event1 -> PerspectiveManager.getInstance().setPerspective( startupPerspective ) );
      }
    }

  }

  private void showStartupURL( HashMap<String, String> settings ) {
    String numStartupURLsSetting = settings.get( "num-startup-urls" );
    if ( numStartupURLsSetting != null ) {
      int numStartupURLs = Integer.parseInt( numStartupURLsSetting ); //$NON-NLS-1$
      for ( int i = 0; i < numStartupURLs; i++ ) {
        String url = settings.get( "startup-url-" + ( i + 1 ) ); //$NON-NLS-1$
        String name = settings.get( "startup-name-" + ( i + 1 ) ); //$NON-NLS-1$
        if ( StringUtils.isEmpty( url ) == false ) { //$NON-NLS-1$
          url = URL.decodeQueryString( url );
          name = URL.decodeQueryString( name );
          SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab( name != null ? name : url,
              url, url, false );
        }
      }
    }
    if ( SolutionBrowserPanel.getInstance().getContentTabPanel().getWidgetCount() > 0 ) {
      SolutionBrowserPanel.getInstance().getContentTabPanel().selectTab( 0 );
    }

    // startup-url on the URL for the app, wins over settings
    String startupURL = Window.Location.getParameter( "startup-url" ); //$NON-NLS-1$
    if ( startupURL != null && !"".equals( startupURL ) && isURLInWhitelistedDomain( startupURL ) ) { //$NON-NLS-1$
      // Spaces were double encoded so that they wouldn't be replaced with '+' when creating a deep
      // link so when following a deep link we need to replace '%20' with a space even after decoding
      String title = Window.Location.getParameter( "name" ).replaceAll( "%20", " " ); //$NON-NLS-1$
      SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab( title, title, startupURL,
          false );
    }
  }

  public DeckPanel getContentDeck() {
    return contentDeck;
  }

  /**
   * Get the UserDropDown
   * @return UserDropDown
   */
  public static UserDropDown getUserDropDown() {
    if ( userDropDown == null ) {
      userDropDown = new UserDropDown();
    }
    return userDropDown;
  }

  private void loadWhitelistedHosts() {

    if ( getWhitelistedHosts() != null ) {
      return;
    }
    String restUrl = GWT.getHostPageBaseURL() + "api/deeplinkAllowedHosts/getDeeplinkAllowedHosts"; //$NON-NLS-1$
    RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, restUrl );
    try {
      requestBuilder.sendRequest( null, new RequestCallback() {
        @Override public void onResponseReceived( Request request, Response response ) {
          List<String> whitelistedHosts = new ArrayList<>();
          JSONArray data = new JSONArray( JsonUtils.safeEval( response.getText() ) );
          for ( int i = 0; i < data.size(); i++ ) {
            whitelistedHosts.add( cleanString( data.get( i ).toString() ) );
          }
          setWhitelistedHosts( whitelistedHosts );
        }

        @Override public void onError( Request request, Throwable throwable ) {
          MessageDialogBox dialogBox =
            new MessageDialogBox( Messages.getString( "error" ), throwable.getLocalizedMessage(), false, false,
              true ); //$NON-NLS-1$
          dialogBox.center();
        }
      } );

    } catch ( RequestException e ) {
      MessageDialogBox dialogBox =
        new MessageDialogBox( Messages.getString( "error" ), e.getLocalizedMessage(), false, false,
          true ); //$NON-NLS-1$
      dialogBox.center();
    }
  }

  public static native void setWhitelistedHosts( List<String> hosts )
  /*-{
      window.parent.mantle_whitelistedHosts = hosts;
  }-*/;

  public static native List<String> getWhitelistedHosts()
  /*-{
      return window.parent.mantle_whitelistedHosts;
  }-*/;


  private boolean isURLInWhitelistedDomain( String startUpURL ) {

    if ( startUpURL == null || startUpURL.isEmpty() ) {
      //if there is nothing on the url, it goes back to the home page
      return true;
    }
    if ( cleanString( startUpURL ).startsWith( "/" ) || cleanString( startUpURL ).startsWith( "%2F" ) ) {
      //if it is deeplink or an internal path
      return true;
    }

    if ( getWhitelistedHosts() == null || getWhitelistedHosts().isEmpty() ) {
      return false;
    }

    for ( String host : getWhitelistedHosts() ) {
      if ( validateHost( startUpURL, host ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean validateHost( String url, String configuredHost ) {
    return cleanString( url ).startsWith( configuredHost );
  }

  private String cleanString( String str ) {
    return  str.replace( "\"", "" ).replace( "]", "" ).replace( "[", "" );
  }

  public void setContentDeck( DeckPanel contentDeck ) {
    this.contentDeck = contentDeck;
  }

  public void pucToolBarVisibility( boolean visible ) {
    RootPanel.get( "pucToolBar" ).setVisible( visible );
  }
}
