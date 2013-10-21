/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.mantle.client.ui.xul;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.SolutionBrowserCloseEvent;
import org.pentaho.mantle.client.events.SolutionBrowserCloseEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserDeselectEvent;
import org.pentaho.mantle.client.events.SolutionBrowserDeselectEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserOpenEvent;
import org.pentaho.mantle.client.events.SolutionBrowserOpenEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEvent;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEventHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.MantleXulOverlay;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulOverlay;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.tags.GwtTree;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MantleXul implements IXulLoaderCallback, SolutionBrowserOpenEventHandler,
    SolutionBrowserCloseEventHandler, SolutionBrowserSelectEventHandler, SolutionBrowserDeselectEventHandler {

  public static final String PROPERTIES_EXTENSION = ".properties"; //$NON-NLS-1$
  public static final String SEPARATOR = "/"; //$NON-NLS-1$  

  private GwtXulDomContainer container;

  private static MantleXul instance;

  private SimplePanel toolbar = new SimplePanel();
  private SimplePanel menubar = new SimplePanel();
  private SimplePanel adminPerspective = new SimplePanel();
  private DeckPanel adminContentDeck = new DeckPanel();

  private MantleController controller;

  private ArrayList<XulOverlay> overlays = new ArrayList<XulOverlay>();

  private HashSet<String> loadedOverlays = new HashSet<String>();

  private MantleXul() {
    AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "xul/mantle.xul", GWT.getModuleBaseURL()
        + "messages/mantleMessages", this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserOpenEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserCloseEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserSelectEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserDeselectEvent.TYPE, this );
  }

  public static MantleXul getInstance() {
    if ( instance == null ) {
      instance = new MantleXul();
    }
    return instance;
  }

  /**
   * Callback method for the MantleXulLoader. This is called when the Xul file has been processed.
   * 
   * @param runner
   *          GwtXulRunner instance ready for event handlers and initializing.
   */
  public void xulLoaded( GwtXulRunner runner ) {
    // handlers need to be wrapped generically in GWT, create one and pass it our reference.

    // instantiate our Model and Controller
    controller = new MantleController( new MantleModel( this ) );

    // Add handler to container
    container = (GwtXulDomContainer) runner.getXulDomContainers().get( 0 );
    container.addEventHandler( controller );

    try {
      runner.initialize();
    } catch ( XulException e ) {
      Window.alert( "Error initializing XUL runner: " + e.getMessage() ); //$NON-NLS-1$
      e.printStackTrace();
      return;
    }

    // Get the toolbar from the XUL doc
    Widget bar = (Widget) container.getDocumentRoot().getElementById( "mainToolbarWrapper" ).getManagedObject(); //$NON-NLS-1$
    Widget xultoolbar = (Widget) container.getDocumentRoot().getElementById( "mainToolbar" ).getManagedObject(); //$NON-NLS-1$
    xultoolbar.getElement().removeClassName( "toolbar" );
    toolbar.setStylePrimaryName( "mainToolbar-Wrapper" );
    toolbar.setWidget( bar );

    // Get the menubar from the XUL doc
    Widget menu = (Widget) container.getDocumentRoot().getElementById( "mainMenubar" ).getManagedObject(); //$NON-NLS-1$
    menubar.setWidget( menu );

    // check based on user permissions
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String url = contextURL + "api/repo/files/canCreate"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "accept", "text/plain" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            Boolean visible = new Boolean( response.getText() );
            controller.setMenuBarEnabled( "newmenu", visible );
            controller.setToolBarButtonEnabled( "newButton", visible );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }

    // get the admin perspective from the XUL doc
    Widget admin = (Widget) container.getDocumentRoot().getElementById( "adminPerspective" ).getManagedObject(); //$NON-NLS-1$
    admin.setStyleName( "admin-perspective" );
    admin.getElement().getStyle().clearHeight();
    admin.getElement().getStyle().clearWidth();
    adminPerspective.setWidget( admin );

    Panel adminContentPanel =
        (Panel) container.getDocumentRoot().getElementById( "adminContentPanel" ).getManagedObject();
    adminContentPanel.add( adminContentDeck );
    adminContentDeck.setHeight( "100%" );
    adminContentDeck.getElement().getStyle().setProperty( "height", "100%" );
    fetchPluginOverlays();
  }

  public void customizeAdminStyle() {
    Timer t = new Timer() {
      public void run() {
        if ( container != null ) {
          cancel();
          // call this method when Elements are added to DOM
          GwtTree adminCatTree = (GwtTree) container.getDocumentRoot().getElementById( "adminCatTree" );
          adminCatTree.getTree().removeStyleName( "gwt-Tree" );
          Panel adminContentPanel =
              (Panel) container.getDocumentRoot().getElementById( "adminContentPanel" ).getManagedObject();
          adminContentPanel.setWidth( "100%" );

          for ( int i = 0; i < adminCatTree.getTree().getItemCount(); i++ ) {
            TreeItem treeItem = adminCatTree.getTree().getItem( i );
            Element e = treeItem.getElement();
            e.getStyle().clearPadding();
            e.addClassName( "adminCatTreeItem" );
            if ( i == adminCatTree.getTree().getItemCount() - 1 ) {
              e.addClassName( "adminCatTreeItemLast" );
            }
          }

          MantleXul.this.selectAdminCatTreeTreeItem( Messages.getString( "manageUsersAndRoles" ).replaceAll( "&amp;",
              "&" ) );
        }
      }
    };
    t.scheduleRepeating( 250 );
  }

  public void configureAdminCatTree() {
    String serviceUrl = GWT.getHostPageBaseURL() + "api/ldap/config/getAttributeValues";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, serviceUrl );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          String securityProvider = response.getText();
          enableUsersRolesTreeItem( securityProvider.contains( "DB_BASED_AUTHENTICATION" ) );
        }
      } );
    } catch ( RequestException e ) {
      //ignored
    }
  }

  public void enableUsersRolesTreeItem( final boolean enabled ) {

    Timer t = new Timer() {
      public void run() {
        if ( container != null ) {
          cancel();
          String usersRolesLabel = Messages.getString( "users" ) + "/" + Messages.getString( "roles" );
          GwtTree adminCatTree = (GwtTree) container.getDocumentRoot().getElementById( "adminCatTree" );

          TreeItem usersRolesTreeItem = null;
          Tree adminTree = adminCatTree.getTree();
          Iterator<TreeItem> adminTreeItr = adminTree.treeItemIterator();
          while ( adminTreeItr.hasNext() ) {
            usersRolesTreeItem = adminTreeItr.next();
            if ( usersRolesTreeItem.getText().equals( usersRolesLabel ) ) {
              usersRolesTreeItem.setVisible( enabled );
              break;
            }
          }
        }
      }
    };
    t.scheduleRepeating( 250 );
  }

  public void selectAdminCatTreeTreeItem( final String treeLabel ) {
    GwtTree adminCatTree = (GwtTree) container.getDocumentRoot().getElementById( "adminCatTree" );
    Tree adminTree = adminCatTree.getTree();
    adminTree.setSelectedItem( null, true );
    Iterator<TreeItem> adminTreeItr = adminTree.treeItemIterator();
    while ( adminTreeItr.hasNext() ) {
      TreeItem treeItem = adminTreeItr.next();
      if ( treeItem.getText().equals( treeLabel ) ) {
        adminTree.setSelectedItem( treeItem, true );
        break;
      }
    }
  }

  public DeckPanel getAdminContentDeck() {
    return adminContentDeck;
  }

  public Widget getToolbar() {
    return toolbar;
  }

  public Widget getMenubar() {
    return menubar;
  }

  public Widget getAdminPerspective() {
    return adminPerspective;
  }

  private void fetchPluginOverlays() {
    AbstractCommand cmd = new AbstractCommand() {
      protected void performOperation( boolean feedback ) {
        performOperation();
      }

      protected void performOperation() {
        final String url = GWT.getHostPageBaseURL() + "api/plugin-manager/overlays"; //$NON-NLS-1$
        RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
        builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        builder.setHeader( "accept", "application/json" );

        try {
          builder.sendRequest( null, new RequestCallback() {

            public void onError( Request request, Throwable exception ) {
              Window.alert( exception.getMessage() );
            }

            public void onResponseReceived( Request request, Response response ) {

              JsArray<JsXulOverlay> jsoverlays =
                  JsXulOverlay.parseJson( JsonUtils.escapeJsonForEval( response.getText() ) );

              ArrayList<XulOverlay> overlays = new ArrayList<XulOverlay>();
              for ( int i = 0; i < jsoverlays.length(); i++ ) {
                JsXulOverlay o = jsoverlays.get( i );
                MantleXulOverlay overlay;
                overlay =
                    new MantleXulOverlay( o.getId(), o.getOverlayUri(), o.getSource(), o.getResourceBundleUri(),
                        Integer.parseInt( o.getPriority() ) );
                overlays.add( overlay );
              }

              MantleXul.this.addOverlays( overlays );

              final String url = GWT.getHostPageBaseURL() + "plugin/data-access/api/permissions/hasDataAccess"; //$NON-NLS-1$
              RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
              builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
              builder.setHeader( "accept", "application/json" );

              try {
                builder.sendRequest( null, new RequestCallback() {

                  public void onError( Request request, Throwable exception ) {
                    Window.alert( exception.getMessage() );
                  }

                  public void onResponseReceived( Request request, Response response ) {
                    if ( response.getText().equals( "true" ) ) {
                      controller.loadOverlay( "dataaccess" );
                    }
                  }
                } );
              } catch ( RequestException e ) {
                // showError(e);
              }
            }
          } );
        } catch ( RequestException e ) {
          // showError(e);
        }
      }
    };
    cmd.execute();
  }

  public void overlayLoaded() {
  }

  /**
   * Class to compare Overlays using their priority attribute
   */
  private class OverlayPriority implements Comparator<XulOverlay> {

    @Override
    public int compare( XulOverlay o1, XulOverlay o2 ) {
      int value = 0;
      if ( o1 != null && o2 != null ) {
        if ( o1.getPriority() > o2.getPriority() ) {
          value = 1;
        } else if ( o1.getPriority() < o2.getPriority() ) {
          value = -1;
        }
      } else if ( o1 == null && o2 != null ) {
        value = -1;
      } else if ( o2 == null && o1 != null ) {
        value = 1;
      }
      return value;
    }
  }

  /**
   * Class to compare Overlays using their priority attribute
   */
  private class OverlayLoader {
    private final List<XulOverlay> overlays;

    public OverlayLoader( List<XulOverlay> overlays ) {
      this.overlays = new ArrayList<XulOverlay>( overlays );
    }

    public void loadOverlays() {
      loadOverlays( 0 );
    }

    private void loadOverlays( final int index ) {
      if ( index < overlays.size() ) {
        final XulOverlay overlayToLoad = overlays.get( index );
        final String id = overlayToLoad.getId();
        if ( loadedOverlays.contains( id ) ) {
          // already loaded - skip this one and load the next
          OverlayLoader.this.loadOverlays( index + 1 );
        } else {
          loadedOverlays.add( id );
          final boolean applyOnStart = id.startsWith( "startup" ) || id.startsWith( "sticky" );
          final Document doc = XMLParser.parse( overlayToLoad.getSource() );
          final String bundleUri = overlayToLoad.getResourceBundleUri();
          String folder = ""; //$NON-NLS-1$
          String baseName = bundleUri;

          // we have to separate the folder from the base name
          if ( bundleUri.indexOf( SEPARATOR ) > -1 ) {
            folder = bundleUri.substring( 0, bundleUri.lastIndexOf( SEPARATOR ) + 1 );
            baseName = bundleUri.substring( bundleUri.lastIndexOf( SEPARATOR ) + 1 );
          }

          // some may put the .properties on incorrectly
          if ( baseName.contains( PROPERTIES_EXTENSION ) ) {
            baseName = baseName.substring( 0, baseName.indexOf( PROPERTIES_EXTENSION ) );
          }
          // some may put the .properties on incorrectly
          if ( baseName.contains( ".properties" ) ) {
            baseName = baseName.substring( 0, baseName.indexOf( ".properties" ) );
          }

          try {
            final ResourceBundle bundle = new ResourceBundle();
            bundle.loadBundle( folder, baseName, true, new IResourceBundleLoadCallback() {
              public void bundleLoaded( String arg0 ) {
                try {
                  container.loadOverlay( doc, bundle, applyOnStart );
                } catch ( XulException e ) {
                  //ignored
                } finally {
                  OverlayLoader.this.loadOverlays( index + 1 );
                }
              }
            } );
          } catch ( Exception e ) {
            Window.alert( "Error loading message bundle: " + e.getMessage() ); //$NON-NLS-1$
            e.printStackTrace();
          }
        }
      }
    }
  }

  public void addOverlays( ArrayList<XulOverlay> overlays ) {
    this.overlays.addAll( overlays );
    Collections.sort( this.overlays, new OverlayPriority() );

    if ( this.overlays.size() > 0 ) {
      // wait for container to be loaded/ready
      Timer loadOverlayTimer = new Timer() {
        public void run() {
          if ( container != null ) {
            cancel();
            new OverlayLoader( MantleXul.this.overlays ).loadOverlays();
          }
        }
      };
      loadOverlayTimer.scheduleRepeating( 250 );
    }
  }

  public void applyOverlays( Set<String> overlayIds ) {
    if ( overlayIds != null && !overlayIds.isEmpty() ) {
      for ( String overlayId : overlayIds ) {
        applyOverlay( overlayId );
      }
    }
  }

  public void applyOverlay( final String id ) {
    if ( container != null ) {
      try {
        container.loadOverlay( id );
      } catch ( XulException e ) {
        //ignored
      }
    } else {
      Timer t = new Timer() {
        public void run() {
          try {
            if ( container != null ) {
              cancel();
              container.loadOverlay( id );
            }
          } catch ( XulException e ) {
            //ignored
          }
        }
      };
      t.scheduleRepeating( 250 );
    }
  }

  public void removeOverlays( Set<String> overlayIds ) {
    if ( overlayIds != null && !overlayIds.isEmpty() ) {
      for ( String overlayId : overlayIds ) {
        removeOverlay( overlayId );
      }
    }
  }

  public void removeOverlay( final String id ) {
    if ( container != null ) {
      try {
        container.removeOverlay( id );
      } catch ( XulException e ) {
        //ignored
      }
    } else {
      Timer t = new Timer() {
        public void run() {
          try {
            if ( container != null ) {
              cancel();
              container.removeOverlay( id );
            }
          } catch ( XulException e ) {
            //ignored
          }
        }
      };
      t.scheduleRepeating( 250 );
    }
  }

  public void overlayRemoved() {
  }

  public ArrayList<XulOverlay> getOverlays() {
    return overlays;
  }

  public void onTabOpened( SolutionBrowserOpenEvent event ) {
    if ( event.getWidget() != null ) {
      applyOverlays( ( (IFrameTabPanel) event.getWidget() ).getOverlayIds() );
    }
  }

  public void onTabSelected( SolutionBrowserSelectEvent event ) {
    if ( event.getWidget() != null ) {
      applyOverlays( ( (IFrameTabPanel) event.getWidget() ).getOverlayIds() );
    }
  }

  public void onTabClosed( SolutionBrowserCloseEvent event ) {
    if ( event.getWidget() != null ) {
      removeOverlays( ( (IFrameTabPanel) event.getWidget() ).getOverlayIds() );
    }
  }

  public void onTabDeselected( SolutionBrowserDeselectEvent event ) {
    if ( event.getWidget() != null ) {
      removeOverlays( ( (IFrameTabPanel) event.getWidget() ).getOverlayIds() );
    }
  }

}
