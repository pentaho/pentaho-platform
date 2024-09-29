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

package org.pentaho.mantle.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.menuitem.MenuCloner;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.MantleUtils;
import org.pentaho.mantle.client.admin.ISysAdminPanel;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.PerspectivesLoadedEvent;
import org.pentaho.mantle.client.objects.MantleXulOverlay;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.CustomDropDown.MODE;
import org.pentaho.mantle.client.ui.xul.JsPerspective;
import org.pentaho.mantle.client.ui.xul.JsXulOverlay;
import org.pentaho.mantle.client.ui.xul.MantleXul;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.platform.plugin.services.pluginmgr.perspective.pojo.DefaultPluginPerspective;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.gwt.util.ResourceBundleTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings( { "deprecation", "unused" } )
public class PerspectiveManager extends SimplePanel {
  private static final String ALLOW_TRANSPARENCY_ATTRIBUTE = "allowTransparency";
  private static final String REMOVE_IFRAME_BORDERS = "frameBorder";
  public static final String ADMIN_PERSPECTIVE = "admin.perspective";
  public static final String SCHEDULES_PERSPECTIVE = "schedules.perspective";
  public static final String OPENED_PERSPECTIVE = "opened.perspective";
  public static final String HOME_PERSPECTIVE = "home.perspective";
  public static final String BROWSER_PERSPECTIVE = "browser.perspective";
  public static final String PROPERTIES_EXTENSION = ".properties"; //$NON-NLS-1$
  public static final String SEPARATOR = "/"; //$NON-NLS-1$
  private static final PerspectiveManager instance = new PerspectiveManager();
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String APPLICATION_JSON = "application/json";
  public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
  public static final String IF_MODIFIED_SINCE_DATE = "01 Jan 1970 00:00:00 GMT";
  private CustomDropDown perspectiveDropDown;
  private final HashMap<String, MenuItem> perspectiveMenuItemMap = new HashMap<>();
  private PentahoMenuItem browserMenuItem;
  private PentahoMenuItem schedulesMenuItem;
  // create an overlay list to later register with the main toolbar/menubar
  private final ArrayList<XulOverlay> overlays = new ArrayList<>();
  private ArrayList<IPluginPerspective> perspectives;
  private IPluginPerspective activePerspective;
  private boolean loaded = false;
  private boolean canSchedule;

  public static PerspectiveManager getInstance() {
    return instance;
  }

  private PerspectiveManager() {
    getElement().setId( "mantle-perspective-switcher" );
    setStyleName( "mantle-perspective-switcher" );
    init();
  }

  private Collection<MenuItem> getMenuItems() {
    return perspectiveMenuItemMap.values();
  }

  private void init() {
    final String url = MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/canSchedule?ts="
      + System.currentTimeMillis(); //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( CONTENT_TYPE, APPLICATION_JSON );
    builder.setHeader( IF_MODIFIED_SINCE, IF_MODIFIED_SINCE_DATE );
    canSchedule = false;

    try {
      builder.sendRequest( null, new RequestCallback() {
        @Override
        public void onError( Request request, Throwable throwable ) {
          canExecuteSchedules();
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == 200 ) {
            canSchedule = "true".equals( response.getText() );

            if ( canSchedule ) {
              setupPerspectiveManager();
              return;
            }
          }

          canExecuteSchedules();
        }
      } );
    } catch ( Exception ignored ) {
      // ignore
    }
  }

  private void canExecuteSchedules() {
    try {
      final String url = MantleUtils.getSchedulerPluginContextURL() + "api/scheduler/canExecuteSchedules?ts="
        + System.currentTimeMillis(); //$NON-NLS-1$
      RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
      requestBuilder.setHeader( CONTENT_TYPE, APPLICATION_JSON );
      requestBuilder.setHeader( IF_MODIFIED_SINCE, IF_MODIFIED_SINCE_DATE );
      requestBuilder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable caught ) {
          setupPerspectiveManager();
        }

        public void onResponseReceived( Request request, Response response ) {
          canSchedule = "true".equalsIgnoreCase( response.getText() ); //$NON-NLS-1$
          setupPerspectiveManager();
        }
      } );
    } catch ( RequestException ignored ) {
      // ignore
    }
  }

  private void setupPerspectiveManager() {
    final String url =
      GWT.getHostPageBaseURL() + "api/plugin-manager/perspectives?ts=" + System.currentTimeMillis(); //$NON-NLS-1$
    RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
    builder.setHeader( CONTENT_TYPE, APPLICATION_JSON );
    builder.setHeader( IF_MODIFIED_SINCE, IF_MODIFIED_SINCE_DATE );

    try {
      builder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          Window.alert( "getPluginPerspectives fail: " + exception.getMessage() );
        }

        public void onResponseReceived( Request request, Response response ) {
          JsArray<JsPerspective> jsPerspectives =
            JsPerspective.parseJson( JsonUtils.escapeJsonForEval( response.getText() ) );
          ArrayList<IPluginPerspective> pluginPerspectives = new ArrayList<>();

          for ( int i = 0; i < jsPerspectives.length(); i++ ) {
            JsPerspective jsperspective = jsPerspectives.get( i );

            // Don't include schedules if user doesn't have schedule permissions
            if ( "${schedules}".equals( jsperspective.getTitle() ) && !canSchedule ) {
              continue;
            }

            DefaultPluginPerspective perspective = new DefaultPluginPerspective();
            perspective.setContentUrl( jsperspective.getContentUrl() );
            perspective.setId( jsperspective.getId() );
            perspective.setLayoutPriority( Integer.parseInt( jsperspective.getLayoutPriority() ) );

            ArrayList<String> requiredSecurityActions = new ArrayList<>();

            if ( jsperspective.getRequiredSecurityActions() != null ) {
              for ( int j = 0; j < jsperspective.getRequiredSecurityActions().length(); j++ ) {
                requiredSecurityActions.add( jsperspective.getRequiredSecurityActions().get( j ) );
              }
            }

            // will need to iterate over jsOverlays and convert to MantleXulOverlay
            ArrayList<XulOverlay> xulOverlays = new ArrayList<>();

            if ( jsperspective.getOverlays() != null ) {
              for ( int j = 0; j < jsperspective.getOverlays().length(); j++ ) {
                JsXulOverlay o = jsperspective.getOverlays().get( j );
                MantleXulOverlay overlay =
                  new MantleXulOverlay( o.getId(), o.getOverlayUri(), o.getSource(), o.getResourceBundleUri() );
                xulOverlays.add( overlay );
              }
            }

            perspective.setOverlays( xulOverlays );

            perspective.setRequiredSecurityActions( requiredSecurityActions );
            perspective.setResourceBundleUri( jsperspective.getResourceBundleUri() );
            perspective.setTitle( jsperspective.getTitle() );

            pluginPerspectives.add( perspective );
          }

          setPluginPerspectives( pluginPerspectives );
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }

    registerFunctions( this );
  }

  protected void setPluginPerspectives( final ArrayList<IPluginPerspective> perspectives ) {
    this.perspectives = perspectives;

    clear();

    // sort perspectives
    perspectives.sort( ( o1, o2 ) -> {
      Integer p1 = o1.getLayoutPriority();
      Integer p2 = o2.getLayoutPriority();
      return p1.compareTo( p2 );
    } );

    MenuBar perspectiveMenuBar = new MenuBar( true );
    perspectiveDropDown = new CustomDropDown( "", perspectiveMenuBar, MODE.MAJOR );
    setWidget( perspectiveDropDown );
    loadResourceBundle( perspectiveDropDown, perspectives.get( 0 ) );

    ScheduledCommand noopCmd = () -> {
    };

    for ( final IPluginPerspective perspective : perspectives ) {
      // if we have overlays add it to the list
      if ( perspective.getOverlays() != null ) {
        overlays.addAll( perspective.getOverlays() );
      }

      final MenuItem menuItem = new MenuItem( "", noopCmd );
      perspectiveMenuItemMap.put( perspective.getId(), menuItem );
      ScheduledCommand cmd = () -> {
        showPerspective( perspective );
        perspectiveDropDown.setText( menuItem.getText() );
        perspectiveDropDown.hidePopup();
      };
      menuItem.setScheduledCommand( cmd );
      perspectiveMenuBar.addItem( menuItem );
      loadResourceBundle( menuItem, perspective );
    }

    // register overlays with XulMainToolbar
    MantleXul.getInstance().addOverlays( overlays );

    setPerspective( perspectives.get( 0 ).getId() );

    loaded = true;
    EventBusUtil.EVENT_BUS.fireEvent( new PerspectivesLoadedEvent() );
  }

  private void loadResourceBundle( final HasText textWidget, final IPluginPerspective perspective ) {
    try {
      String bundle = perspective.getResourceBundleUri();

      if ( bundle == null ) {
        return;
      }

      String folder = ""; //$NON-NLS-1$
      String baseName = bundle;

      // we have to separate the folder from the base name
      if ( bundle.contains( SEPARATOR ) ) {
        folder = bundle.substring( 0, bundle.lastIndexOf( SEPARATOR ) + 1 );
        baseName = bundle.substring( bundle.lastIndexOf( SEPARATOR ) + 1 );
      }

      // some may put the .properties on incorrectly
      if ( baseName.contains( PROPERTIES_EXTENSION ) ) {
        baseName = baseName.substring( 0, baseName.indexOf( PROPERTIES_EXTENSION ) );
      }

      // some may put the .properties on incorrectly
      if ( baseName.contains( PROPERTIES_EXTENSION ) ) {
        baseName = baseName.substring( 0, baseName.indexOf( PROPERTIES_EXTENSION ) );
      }

      final ResourceBundle messageBundle = new ResourceBundle();
      messageBundle.loadBundle( folder, baseName, true, arg0 -> {
        String title = ResourceBundleTranslator.translate( perspective.getTitle(), messageBundle );
        perspective.setTitle( title );
        textWidget.setText( title );
      } );

    } catch ( Exception e ) {
      Window.alert( "Error loading message bundle: " + e.getMessage() ); //$NON-NLS-1$
    }
  }

  public void enablePerspective( final String perspectiveId, boolean enabled ) {
    if ( perspectives == null ) {
      return;
    }

    // return value to indicate if perspective now disabled
    if ( perspectives.stream().anyMatch( perspective -> perspective.getId().equalsIgnoreCase( perspectiveId ) ) ) {
      perspectiveMenuItemMap.get( perspectiveId ).setEnabled( enabled );
    }
  }

  public boolean setPerspective( final String perspectiveId ) {
    if ( perspectives == null ) {
      return false;
    }

    // return value to indicate if perspective now shown
    for ( IPluginPerspective perspective : perspectives ) {
      if ( perspective.getId().equalsIgnoreCase( perspectiveId ) ) {
        showPerspective( perspective );
        return true;
      }
    }

    return false;
  }

  /**
   * Show the perspective defined as with the highest priority
   */
  public void showPerspectiveWithHighestPriority() {
    // perspectives list is sorted by priority, so show the first one
    setPerspective( perspectives.get( 0 ).getId() );
  }

  private void showPerspective( final IPluginPerspective perspective ) {
    if ( activePerspective == perspective ) {
      return;
    }

    if ( activePerspective != null && ADMIN_PERSPECTIVE.equals( activePerspective.getId() ) ) {
      final Widget activeAdminPanel = MantleXul.getInstance().getAdminContentDeck().getWidget( MantleXul.getInstance()
        .getAdminContentDeck().getVisibleWidget() );

      if ( activeAdminPanel != null ) {
        if ( activeAdminPanel instanceof ISysAdminPanel ) {
          ( (ISysAdminPanel) activeAdminPanel ).passivate( new AsyncCallback<Boolean>() {
            @Override
            public void onFailure( Throwable caught ) {
              // ignore
            }

            @Override
            public void onSuccess( Boolean result ) {
              showPerspectiveContinue( perspective );
            }
          } );
        } else {
          showPerspectiveContinue( perspective );
        }
      }
    } else {
      showPerspectiveContinue( perspective );
    }
  }

  private void showPerspectiveContinue( IPluginPerspective perspective ) {
    if ( !perspective.getTitle().startsWith( "${" ) ) {
      perspectiveDropDown.setText( perspective.getTitle() );
    }

    for ( MenuItem m : perspectiveMenuItemMap.values() ) {
      m.getElement().removeClassName( "custom-dropdown-selected" );
    }

    perspectiveMenuItemMap.get( perspective.getId() ).getElement().addClassName( "custom-dropdown-selected" );

    // before we show de-activate current perspective (based on shown widget)
    Widget w =
      MantleApplication.getInstance().getContentDeck().getWidget(
        MantleApplication.getInstance().getContentDeck().getVisibleWidget() );

    if ( w instanceof Frame && !perspective.getId().equals( w.getElement().getId() ) ) {
      // invoke deactivation method
      Frame frame = (Frame) w;
      perspectiveDeactivated( frame.getElement() );
    }

    // remove current perspective overlays
    if ( activePerspective != null ) {
      for ( XulOverlay o : activePerspective.getOverlays() ) {
        if ( !o.getId().startsWith( "startup" ) && !o.getId().startsWith( "sticky" ) ) {
          MantleXul.getInstance().removeOverlay( o.getId() );
        }
      }

      for ( XulOverlay overlay : MantleXul.getInstance().getOverlays() ) {
        if ( overlay.getId().startsWith( activePerspective.getId() + ".overlay." ) ) {
          MantleXul.getInstance().removeOverlay( overlay.getId() );
        }
      }
    }

    // now it's safe to set active
    this.activePerspective = perspective;

    if ( perspective.getOverlays() != null ) {
      // handle PERSPECTIVE overlays
      for ( XulOverlay overlay : perspective.getOverlays() ) {
        if ( !overlay.getId().startsWith( "startup" ) && !overlay.getId().startsWith( "sticky" ) ) {
          MantleXul.getInstance().applyOverlay( overlay.getId() );
        }
      }

      // handle PLUGIN overlays
      for ( XulOverlay overlay : MantleXul.getInstance().getOverlays() ) {
        if ( overlay.getId().startsWith( perspective.getId() + ".overlay." ) ) {
          MantleXul.getInstance().applyOverlay( overlay.getId() );
        }
      }
    }

    if ( !perspective.getId().equals( OPENED_PERSPECTIVE ) && !perspective.getId().equals( SCHEDULES_PERSPECTIVE )
      && !perspective.getId().equals( ADMIN_PERSPECTIVE ) ) {
      hijackContentArea( perspective );
    }

    // if the selected perspective is "opened.perspective"
    if ( perspective.getId().equals( OPENED_PERSPECTIVE ) ) {
      showOpenedPerspective( true, false );
    } else if ( perspective.getId().equals( SCHEDULES_PERSPECTIVE ) ) {
      showSchedulesPerspective();
    } else if ( perspective.getId().equals( ADMIN_PERSPECTIVE ) ) {
      showAdminPerspective( false, false );
    }
  }

  private void showOpenedPerspective( boolean browserChecked, boolean schedulesChecked ) {
    DeckPanel contentDeck = MantleApplication.getInstance().getContentDeck();

    if ( MantleApplication.getInstance().getContentDeck().getWidgetIndex( SolutionBrowserPanel.getInstance() ) == -1 ) {
      contentDeck.add( SolutionBrowserPanel.getInstance() );
    }

    // show stuff we've created/configured
    contentDeck.showWidget( contentDeck.getWidgetIndex( SolutionBrowserPanel.getInstance() ) );
    SolutionBrowserPanel.getInstance().setNavigatorShowing( SolutionBrowserPanel.getInstance().isNavigatorShowing() );
    setCheckMMenuItem( browserChecked, schedulesChecked );
  }

  private void showSchedulesPerspective() {
    DeckPanel contentDeck = MantleApplication.getInstance().getContentDeck();

    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        // Use a SimplePanel to hold the returned element,
        // and add it as a child widget if it is not there yet.
        SimplePanel activePerspectiveWidget = (SimplePanel) findPerspectiveWidget( contentDeck, activePerspective );
        boolean isFirstTime = activePerspectiveWidget == null;

        if ( isFirstTime ) {
          // Setup
          activePerspectiveWidget = new SimplePanel();
          activePerspectiveWidget.getElement().setId( activePerspective.getId() );
          contentDeck.add( activePerspectiveWidget );
        }

        com.google.gwt.dom.client.Element element =
          getSchedulesPerspectiveElement( activePerspectiveWidget.getElement() );

        if ( isFirstTime ) {
          activePerspectiveWidget.getElement().appendChild( element );
        }

        contentDeck.showWidget( contentDeck.getWidgetIndex( activePerspectiveWidget ) );
      }

      public void onFailure( Throwable reason ) {
        // ignore
      }
    } );

    setCheckMMenuItem( false, true );
  }

  private static Widget findPerspectiveWidget( DeckPanel contentDeck, IPluginPerspective perspective ) {
    int count = contentDeck.getWidgetCount();

    for ( int i = 0; i < count; i++ ) {
      Widget widget = contentDeck.getWidget( i );

      if ( perspective.getId().equals( widget.getElement().getId() ) ) {
        return widget;
      }
    }

    return null;
  }

  private void showAdminPerspective( boolean browserChecked, boolean schedulesChecked ) {
    DeckPanel contentDeck = MantleApplication.getInstance().getContentDeck();

    if ( MantleApplication.getInstance().getContentDeck()
      .getWidgetIndex( MantleXul.getInstance().getAdminPerspective() ) == -1 ) {
      contentDeck.add( MantleXul.getInstance().getAdminPerspective() );
    }

    contentDeck.showWidget( contentDeck.getWidgetIndex( MantleXul.getInstance().getAdminPerspective() ) );
    MantleXul.getInstance().customizeAdminStyle();
    MantleXul.getInstance().configureAdminCatTree();
    // disable Browser and schedules menuItem
    setCheckMMenuItem( browserChecked, schedulesChecked );
  }

  private void hijackContentArea( IPluginPerspective perspective ) {
    // hijack content area (or simply find and select existing content)
    Frame frame = null;

    for ( int i = 0; i < MantleApplication.getInstance().getContentDeck().getWidgetCount(); i++ ) {
      Widget w = MantleApplication.getInstance().getContentDeck().getWidget( i );

      if ( w instanceof Frame && perspective.getId().equals( w.getElement().getId() ) ) {
        frame = (Frame) w;
      }
    }

    if ( frame == null ) {
      frame = new Frame( perspective.getContentUrl() );
      Element frameElement = frame.getElement();
      frameElement.setTabIndex( 0 );
      frameElement.setAttribute( ALLOW_TRANSPARENCY_ATTRIBUTE, "true" );
      // BISERVER-7661 Mantle sections have a border on IE9 (not on chrome, firefox)
      frameElement.setAttribute( REMOVE_IFRAME_BORDERS, "0" );
      frame.getElement().setId( perspective.getId() );
      MantleApplication.getInstance().getContentDeck().add( frame );
    }

    MantleApplication.getInstance().getContentDeck().showWidget(
      MantleApplication.getInstance().getContentDeck().getWidgetIndex( frame ) );

    final Element frameElement = frame.getElement();
    perspectiveActivated( frameElement );
  }

  public native com.google.gwt.dom.client.Element getSchedulesPerspectiveElement(
    com.google.gwt.dom.client.Element containerElement ) /*-{
    return $wnd.pho.getSchedulesPerspectiveElement(containerElement);
  }-*/;

  private native void perspectiveActivated( Element frameElement )
    /*-{
      try {
        frameElement.contentWindow.perspectiveActivated();
      } catch (e) {
      }
    }-*/;

  private native void perspectiveDeactivated( Element frameElement )
    /*-{
      try {
        frameElement.contentWindow.perspectiveDeactivated();
      } catch (e) {
      }
    }-*/;

  private native void registerFunctions( PerspectiveManager manager )
    /*-{
      $wnd.mantle_getPerspectives = function () {
        return manager.@org.pentaho.mantle.client.ui.PerspectiveManager::getPerspectives()();
      }
      $wnd.mantle_setPerspective = function (perspectiveId) {
        manager.@org.pentaho.mantle.client.ui.PerspectiveManager::setPerspective(Ljava/lang/String;)(perspectiveId);
      }
    }-*/;

  private JsArrayString getPerspectives() {
    JsArrayString stringArray = getJsArrayString();

    for ( IPluginPerspective perspective : perspectives ) {
      stringArray.push( perspective.getId() );
    }

    return stringArray;
  }

  private native JsArrayString getJsArrayString()
    /*-{
      return [];
    }-*/;

  public IPluginPerspective getActivePerspective() {
    return activePerspective;
  }

  public void setActivePerspective( IPluginPerspective activePerspective ) {
    this.activePerspective = activePerspective;
    setPerspective( activePerspective.getId() );
  }

  public void setBrowserMenuItem( PentahoMenuItem menuItem ) {
    this.browserMenuItem = menuItem;
  }

  public void setSchedulesMenuItem( PentahoMenuItem menuItem ) {
    this.schedulesMenuItem = menuItem;
  }

  private void setCheckMMenuItem( boolean browserChecked, boolean schedulesChecked ) {
    if ( this.browserMenuItem != null && this.schedulesMenuItem != null ) {
      this.browserMenuItem.setChecked( browserChecked );
      this.schedulesMenuItem.setChecked( schedulesChecked );
    }
  }

  public boolean isLoaded() {
    return loaded;
  }

  public void setLoaded( boolean loaded ) {
    this.loaded = loaded;
  }

  public MenuItem getBurgerBarPerspectiveMenuItem() {
    BurgerMenuBar perspectivesMenuBar = new BurgerMenuBar();

    MenuCloner<BurgerMenuBar> menuCloner = new MenuCloner<>( m -> new BurgerMenuBar() );
    getMenuItems().forEach( m -> perspectivesMenuBar.addItem( menuCloner.clone( m ) ) );

    MenuItem burgerBarPerspectiveMenuItem =
      new MenuItem( getActivePerspective().getTitle(), (Scheduler.ScheduledCommand) null );
    burgerBarPerspectiveMenuItem.setSubMenu( perspectivesMenuBar );
    return burgerBarPerspectiveMenuItem;
  }

  public void hidePopup() {
    if ( perspectiveDropDown != null ) {
      perspectiveDropDown.hidePopup();
    }
  }
}
