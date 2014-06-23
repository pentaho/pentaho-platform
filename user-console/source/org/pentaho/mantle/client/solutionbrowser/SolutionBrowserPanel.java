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

package org.pentaho.mantle.client.solutionbrowser;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.Widget;

import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.filechooser.RepositoryFile;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.mantle.client.EmptyRequestCallback;
import org.pentaho.mantle.client.commands.AbstractCommand;
import org.pentaho.mantle.client.commands.ExecuteUrlInNewTabCommand;
import org.pentaho.mantle.client.commands.ShareFileCommand;
import org.pentaho.mantle.client.dialogs.scheduling.ScheduleHelper;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.ShowDescriptionsEvent;
import org.pentaho.mantle.client.events.ShowHiddenFilesEvent;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEvent;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEventHandler;
import org.pentaho.mantle.client.events.SolutionFileHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.SolutionFileInfo;
import org.pentaho.mantle.client.solutionbrowser.PluginOptionsHelper.ContentTypePlugin;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.filelist.FilesListPanel;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickList;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickList;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.solutionbrowser.toolbars.BrowserToolbar;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTree;
import org.pentaho.mantle.client.solutionbrowser.tree.SolutionTreeWrapper;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.ui.tabs.MantleTabPanel;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings ( "deprecation" )
public class SolutionBrowserPanel extends HorizontalPanel {

  private final int defaultSplitPosition = 220; //$NON-NLS-1$

  private SplitLayoutPanel navigatorAndContentSplit = new SplitLayoutPanel() {
    @Override
    public void onResize() {
      super.onResize();
      adjustContentPanelSize();
    }
  };
  private SplitLayoutPanel solutionNavigatorPanel = new SplitLayoutPanel();
  private SolutionTree solutionTree = new SolutionTree( true );
  private FilesListPanel filesListPanel = new FilesListPanel();
  private Timer resizeTimer;

  private MantleTabPanel contentTabPanel = new MantleTabPanel( true );
  private boolean showSolutionBrowser = false;
  private boolean isAdministrator = false;
  private boolean isScheduler = false;
  private PickupDragController dragController;
  private List<String> executableFileExtensions = new ArrayList<String>();
  private JsArrayString filters;

  private Command ToggleLocalizedNamesCommand = new Command() {
    public void execute() {
      solutionTree.setShowLocalizedFileNames( !solutionTree.isShowLocalizedFileNames() );

      // update setting
      final String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_LOCALIZED_FILENAMES"; //$NON-NLS-1$
      RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      try {
        builder.sendRequest( "" + solutionTree.isShowLocalizedFileNames(), EmptyRequestCallback.getInstance() );
      } catch ( RequestException e ) {
        // showError(e);
      }
    }
  };

  public Command toggleShowHideFilesCommand = new Command() {
    public void execute() {
      filesListPanel.setShowHiddenFiles( !solutionTree.isShowHiddenFiles() );
      solutionTree.setShowHiddenFiles( !solutionTree.isShowHiddenFiles() );
      solutionTree.setSelectedItem( solutionTree.getSelectedItem(), true );

      // send event
      final ShowHiddenFilesEvent event = new ShowHiddenFilesEvent();
      event.setValue( solutionTree.isShowHiddenFiles() );
      EventBusUtil.EVENT_BUS.fireEvent( event );

      // update setting
      final String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_HIDDEN_FILES"; //$NON-NLS-1$
      RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
      try {
        builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        builder.sendRequest( "" + solutionTree.isShowHiddenFiles(), EmptyRequestCallback.getInstance() );
        RepositoryFileTreeManager.getInstance().fetchRepositoryFileTree( true, null, null,
            solutionTree.isShowHiddenFiles() );
      } catch ( RequestException e ) {
        // showError(e);
      }
    }
  };

  public Command toggleUseDescriptionCommand = new Command() {
    public void execute() {
      solutionTree.setUseDescriptionsForTooltip( !solutionTree.isUseDescriptionsForTooltip() );
      solutionTree.setSelectedItem( solutionTree.getSelectedItem(), true );

      // send event
      final ShowDescriptionsEvent event = new ShowDescriptionsEvent();
      event.setValue( solutionTree.isUseDescriptionsForTooltip() );
      EventBusUtil.EVENT_BUS.fireEvent( event );

      // update setting
      final String url = GWT.getHostPageBaseURL() + "api/user-settings/MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS"; //$NON-NLS-1$
      RequestBuilder builder = new RequestBuilder( RequestBuilder.POST, url );
      try {
        builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
        builder.sendRequest( "" + solutionTree.isUseDescriptionsForTooltip(), EmptyRequestCallback.getInstance() );
      } catch ( RequestException e ) {
        // showError(e);
      }
    }
  };

  private TreeListener treeListener = new TreeListener() {

    public void onTreeItemSelected( TreeItem item ) {
      filesListPanel.setShowHiddenFiles( solutionTree.isShowHiddenFiles() );
      filesListPanel.populateFilesList( SolutionBrowserPanel.this, solutionTree, item, filters );
      filesListPanel.getToolbar().setEnabled( false );
    }

    public void onTreeItemStateChanged( TreeItem item ) {
      solutionTree.setSelectedItem( item, false );
    }

  };

  private static SolutionBrowserPanel instance;

  private SolutionBrowserPanel() {
    RootPanel.get().getElement().getStyle().setProperty( "position", "relative" );
    dragController = new SolutionBrowserDragController( contentTabPanel );
    instance = this;

    ExecuteUrlInNewTabCommand.setupNativeHooks();
    SolutionBrowserPanel.setupNativeHooks( this );

    solutionTree.addTreeListener( treeListener );
    initializeExecutableFileTypes();
    buildUI();
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserSelectEvent.TYPE, new SolutionBrowserSelectEventHandler() {
      public void onTabSelected( SolutionBrowserSelectEvent event ) {
        adjustContentPanelSize();
      }
    } );
  }

  public static SolutionBrowserPanel getInstance() {
    if ( instance == null ) {
      instance = new SolutionBrowserPanel();
    }
    return instance;
  }

  private void buildUI() {
    FlowPanel topPanel = new FlowPanel();
    SimplePanel toolbarWrapper = new SimplePanel();
    toolbarWrapper.setWidget( new BrowserToolbar() );
    toolbarWrapper.setStyleName( "files-toolbar" ); //$NON-NLS-1$
    topPanel.add( toolbarWrapper );
    topPanel.add( new SolutionTreeWrapper( solutionTree ) );

    solutionNavigatorPanel.setStyleName( "puc-vertical-split-panel" );
    solutionNavigatorPanel.setWidth( "100%" );
    solutionNavigatorPanel.addNorth( topPanel, 500 );
    solutionNavigatorPanel.add( filesListPanel );

    navigatorAndContentSplit.setStyleName( "puc-horizontal-split-panel" );
    navigatorAndContentSplit.addWest( solutionNavigatorPanel, 300 );
    navigatorAndContentSplit.add( contentTabPanel );
    navigatorAndContentSplit.getElement().setAttribute( "id", "solutionNavigatorAndContentPanel" );

    Window.addResizeHandler( new ResizeHandler() {
      @Override
      public void onResize( ResizeEvent event ) {
        adjustContentPanelSize();
      }
    } );

    solutionNavigatorPanel.getElement().getParentElement().addClassName( "puc-navigator-panel" );
    solutionNavigatorPanel.getElement().getParentElement().removeAttribute( "style" );

    setStyleName( "panelWithTitledToolbar" ); //$NON-NLS-1$  
    setHeight( "100%" ); //$NON-NLS-1$
    setWidth( "100%" ); //$NON-NLS-1$

    add( navigatorAndContentSplit );

    sinkEvents( Event.MOUSEEVENTS );

    navigatorAndContentSplit.getWidget( 1 ).setWidth( "100%" );
    navigatorAndContentSplit.getElement().getStyle().setHeight( 1, Unit.PX );
    contentTabPanel.getElement().getStyle().setHeight( 1, Unit.PX );
  }

  private static native String setElementHeightOffset( Element ele, int offset )
  /*-{
    var h = 0;
    if ($wnd.innerHeight) {
      h = $wnd.innerHeight;
    }
    else if ($wnd.document.documentElement && $wnd.document.documentElement.clientHeight != 0) {
      h = $wnd.document.documentElement.clientHeight;
    }
    else if ($wnd.document.body) {
      h = $wnd.document.body.clientHeight;
    }

    var height = h + offset - 5;
    var offSetHeight = height + 'px';
    ele.style.height = offSetHeight;
  }-*/;

  private void adjustHeight() {
    Element pucHeader = DOM.getElementById( "pucHeader" );
    if ( pucHeader != null ) {
      final boolean isIE = RootPanel.getBodyElement().getClassName().contains( "IE8" )
          || RootPanel.getBodyElement().getClassName().contains( "IE9" )
          || RootPanel.getBodyElement().getClassName().contains( "IE10" );
      final int offset = pucHeader.getOffsetHeight();
      setElementHeightOffset( navigatorAndContentSplit.getElement(), -1 * offset );
      setElementHeightOffset( contentTabPanel.getElement(), isIE ? -1 * ( offset + 36 ) : -1 * offset );
      Timer t = new Timer() {
        public void run() {
          setElementHeightOffset( navigatorAndContentSplit.getElement(), -1 * offset );
          setElementHeightOffset( contentTabPanel.getElement(), isIE ? -1 * ( offset + 36 ) : -1 * offset );
        }
      };
      t.schedule( 100 );
    }
  }

  private void adjustWidth() {
    int splitterWidth = navigatorAndContentSplit.getSplitterSize();
    int adjustedWidth = solutionNavigatorPanel.getOffsetWidth() + splitterWidth;
    int width = this.getOffsetWidth() - adjustedWidth;
    if ( width > 0 ) {
      contentTabPanel.setWidth( width + "px" );
    }
  }

  public void adjustContentPanelSize() {
    if ( resizeTimer == null ) {
      resizeTimer = new Timer() {
        public void run() {
          resizeTimer = null;
          adjustWidth();
          adjustHeight();
        }
      };
      resizeTimer.schedule( 100 );
    }
  }

  private static native void setupNativeHooks( SolutionBrowserPanel solutionNavigator )
  /*-{
    $wnd.sendMouseEvent = function (event) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::mouseUp(Lcom/google/gwt/user/client/Event;)(event);
    }
    $wnd.mantle_setNavigatorShowing = function (show) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::setNavigatorShowing(Z)(show);
    }
    $wnd.mantle_confirmBackgroundExecutionDialog = function (url) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      @org.pentaho.mantle.client.dialogs.scheduling.ScheduleHelper::confirmBackgroundExecutionDialog(Ljava/lang/String;)(url);
    }
    $wnd.mantle_openRepositoryFile = function (pathToFile, mode) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::openFile(Ljava/lang/String;Ljava/lang/String;)(pathToFile, mode);
    }
    $wnd.mantle_addFavorite = function (pathToFile, title) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::addFavorite(Ljava/lang/String;Ljava/lang/String;)(pathToFile, title);
    }
    $wnd.mantle_removeFavorite = function (pathToFile) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::removeFavorite(Ljava/lang/String;)(pathToFile);
    }
    $wnd.mantle_isNavigatorShowing = function () {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::isNavigatorShowing()();
    }
    $wnd.mantle_setDashboardsFilter = function (filters) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      solutionNavigator.@org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel::setDashboardsFilter(Lcom/google/gwt/core/client/JsArrayString;)(filters);
    }
  }-*/;

  public void setDashboardsFilter( JsArrayString filters ) {
    this.filters = filters;
  }

  /**
   * This method is called via JSNI
   */
  private void mouseUp( Event e ) {
    navigatorAndContentSplit.onBrowserEvent( e );
  }

  @SuppressWarnings ( "nls" )
  public static String pathToId( String path ) {
    String id = NameUtils.encodeRepositoryPath( path );
    return NameUtils.URLEncode( id );
  }

  public List<String> getExecutableFileExtensions() {
    return executableFileExtensions;
  }

  public void openFile( final String fileNameWithPath, final String mode ) {
    FileCommand.COMMAND realMode = COMMAND.RUN;
    try {
      realMode = FileCommand.COMMAND.valueOf( mode.toUpperCase() );
    } catch ( IllegalArgumentException e ) {
      // bad mode passed in, using default
    }
    openFile( fileNameWithPath, realMode );
  }

  public void getFile( final String solutionPath, final SolutionFileHandler handler ) {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String path = solutionPath; // Expecting some encoding here
    final String url = contextURL + "api/repo/files/" + pathToId( path ) + "/properties"; //$NON-NLS-1$

    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "repositoryFileDto", (JSONObject) JSONParser.parseLenient( response.getText() ) );
            RepositoryFile repositoryFile = new RepositoryFile( jsonObject );
            handler.handle( repositoryFile );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  public void openFile( final String fileNameWithPath, final FileCommand.COMMAND mode ) {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String path = fileNameWithPath; // Expecting some encoding here
    final String url = contextURL + "api/repo/files/" + pathToId( path ) + "/properties"; //$NON-NLS-1$

    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );

    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put( "repositoryFileDto", (JSONObject) JSONParser.parseLenient( response.getText() ) );
            RepositoryFile repositoryFile = new RepositoryFile( jsonObject );
            openFile( repositoryFile, mode );
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  public void openFile( final RepositoryFile repositoryFile, final FileCommand.COMMAND mode ) {

    String fileNameWithPath = repositoryFile.getPath();
    if ( mode == FileCommand.COMMAND.EDIT ) {
      PerspectiveManager.getInstance().setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
      editFile( repositoryFile );
    } else if ( mode == FileCommand.COMMAND.SCHEDULE_NEW ) {
      ScheduleHelper.createSchedule( repositoryFile );
      return;
    } else if ( mode == FileCommand.COMMAND.SHARE ) {
      ShareFileCommand sfc = new ShareFileCommand();
      sfc.setSolutionPath( fileNameWithPath );
      sfc.execute();
    } else {
      String url = null;
      String extension = ""; //$NON-NLS-1$
      if ( fileNameWithPath.lastIndexOf( "." ) > 0 ) { //$NON-NLS-1$
        extension = fileNameWithPath.substring( fileNameWithPath.lastIndexOf( "." ) + 1 ); //$NON-NLS-1$
      }
      if ( !executableFileExtensions.contains( extension ) ) {
        url = getPath() + "api/repos/" + pathToId( fileNameWithPath ) + "/content"; //$NON-NLS-1$ //$NON-NLS-2$ 
      } else {
        ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin( fileNameWithPath );
        url =
            getPath()
                + "api/repos/" + pathToId( fileNameWithPath ) + "/" + ( plugin != null && ( plugin.getCommandPerspective( mode ) != null ) ? plugin.getCommandPerspective( mode ) : "generatedContent" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
      // force to open pdf files in another window due to issues with pdf readers in IE browsers
      // via class added on themeResources for IE browsers
      boolean pdfReaderEmbeded = RootPanel.getBodyElement().getClassName().contains( "pdfReaderEmbeded" );
      if ( mode == FileCommand.COMMAND.NEWWINDOW || ( extension.equals( "pdf" ) && pdfReaderEmbeded ) ) {
        Window.open( url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no" ); //$NON-NLS-1$ //$NON-NLS-2$
      } else {
        PerspectiveManager.getInstance().setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
        contentTabPanel.showNewURLTab( repositoryFile.getTitle(), repositoryFile.getTitle(), url, true );
        addRecent( fileNameWithPath, repositoryFile.getTitle() );
      }
    }

    // Store representation of file in the frame for reference later when
    // save is called
    SolutionFileInfo fileInfo = new SolutionFileInfo();
    fileInfo.setName( repositoryFile.getName() );
    fileInfo.setPath( repositoryFile.getPath() );
    fileInfo.setType( SolutionFileInfo.Type.XACTION );
    if ( contentTabPanel != null && contentTabPanel.getCurrentFrame() != null ) {
      contentTabPanel.getCurrentFrame().setFileInfo( fileInfo );
    }
  }

  public void addRecent( String fileNameWithPath, String title ) {
    RecentPickItem recentPickItem = new RecentPickItem( fileNameWithPath );
    recentPickItem.setTitle( title );
    recentPickItem.setLastUse( System.currentTimeMillis() );
    RecentPickList.getInstance().add( recentPickItem );
  }

  public void removeRecent( String fileNameWithPath ) {
    RecentPickItem recentItem = new RecentPickItem( fileNameWithPath );
    RecentPickList.getInstance().remove( recentItem );
  }

  public void addFavorite( String fileNameWithPath, String title ) {
    FavoritePickItem favoritePickItem = new FavoritePickItem( fileNameWithPath );
    favoritePickItem.setTitle( title );
    FavoritePickList.getInstance().add( favoritePickItem );
  }

  public void removeFavorite( String fileNameWithPath ) {
    FavoritePickItem favoritePickItem = new FavoritePickItem( fileNameWithPath );
    FavoritePickList.getInstance().remove( favoritePickItem );
  }

  public void setDeepLinkUrl( String fileNameWithPath ) {
    ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin( fileNameWithPath );
    if ( plugin != null && plugin.hasCommand( COMMAND.RUN ) ) {
      String url =
          getPath() + "api/repos/" + pathToId( fileNameWithPath ) + "/" + plugin.getCommandPerspective( COMMAND.RUN ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      contentTabPanel.getCurrentFrame().setDeepLinkUrl( url );
    }
  }

  protected void initializeExecutableFileTypes() {
    final String moduleBaseURL = GWT.getModuleBaseURL();
    final String moduleName = GWT.getModuleName();
    final String contextURL = moduleBaseURL.substring( 0, moduleBaseURL.lastIndexOf( moduleName ) );
    final String url = contextURL + "api/repos/executableTypes"; //$NON-NLS-1$
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.GET, url );
    executableTypesRequestBuilder.setHeader( "accept", "application/json" );
    executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      executableTypesRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == Response.SC_OK ) {
            JSONObject jsonObject = (JSONObject) JSONParser.parse( response.getText() );
            JSONArray jsonList = (JSONArray) jsonObject.get( "executableFileTypeDto" );
            for ( int i = 0; i < jsonList.size(); i++ ) {
              JSONObject executableType = (JSONObject) jsonList.get( i );
              executableFileExtensions.add( executableType.get( "extension" ).isString().stringValue() );
            }
          }
        }
      } );
    } catch ( RequestException e ) {
      // showError(e);
    }
  }

  public void editFile( final RepositoryFile file ) {

    if ( file.getName().endsWith( ".analysisview.xaction" ) ) { //$NON-NLS-1$
      openFile( file, COMMAND.RUN );
    } else {
      // check to see if a plugin supports editing
      ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin( file.getName() );
      if ( plugin != null && plugin.hasCommand( COMMAND.EDIT ) ) {
        // load the editor for this plugin
        String editUrl =
            getPath()
                + "api/repos/" + pathToId( file.getPath() ) + "/" + ( plugin != null && ( plugin.getCommandPerspective( COMMAND.EDIT ) != null ) ? plugin.getCommandPerspective( COMMAND.EDIT ) : "editor" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // See if it's already loaded
        for ( int i = 0; i < contentTabPanel.getTabCount(); i++ ) {
          Widget w = contentTabPanel.getTab( i ).getContent();
          if ( w instanceof IFrameTabPanel && ( (IFrameTabPanel) w ).getUrl().endsWith( editUrl ) ) {
            // Already up, select and exit
            contentTabPanel.selectTab( i );
            return;
          }
        }

        contentTabPanel
            .showNewURLTab(
                Messages.getString( "editingColon" ) + file.getTitle(),
                Messages.getString( "editingColon" ) + file.getTitle(), editUrl, true ); //$NON-NLS-1$ //$NON-NLS-2$

      } else {
        MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), //$NON-NLS-1$
            Messages.getString( "cannotEditFileType" ), //$NON-NLS-1$
            true, false, true );
        dialogBox.center();
      }
    }
  }

  public void editFile() {
    if ( filesListPanel.getSelectedFileItems() == null || filesListPanel.getSelectedFileItems().size() != 1 ) {
      return;
    }

    RepositoryFile file = filesListPanel.getSelectedFileItems().get( 0 ).getRepositoryFile();
    if ( file.getName().endsWith( ".analysisview.xaction" ) ) { //$NON-NLS-1$
      openFile( file, COMMAND.RUN );
    } else {
      // check to see if a plugin supports editing
      ContentTypePlugin plugin = PluginOptionsHelper.getContentTypePlugin( file.getName() );
      if ( plugin != null && plugin.hasCommand( COMMAND.EDIT ) ) {
        // load the editor for this plugin
        String editUrl =
            getPath()
                + "api/repos/" + pathToId( file.getPath() ) + "/" + ( plugin != null && ( plugin.getCommandPerspective( COMMAND.EDIT ) != null ) ? plugin.getCommandPerspective( COMMAND.EDIT ) : "editor" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$       
        // See if it's already loaded
        for ( int i = 0; i < contentTabPanel.getTabCount(); i++ ) {
          Widget w = contentTabPanel.getTab( i ).getContent();
          if ( w instanceof IFrameTabPanel && ( (IFrameTabPanel) w ).getUrl().endsWith( editUrl ) ) {
            // Already up, select and exit
            contentTabPanel.selectTab( i );
            return;
          }
        }

        contentTabPanel
            .showNewURLTab(
                Messages.getString( "editingColon" ) + file.getTitle(), Messages.getString( "editingColon" ) + file.getTitle(), editUrl, true ); //$NON-NLS-1$ //$NON-NLS-2$

      } else {
        MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), //$NON-NLS-1$
            Messages.getString( "cannotEditFileType" ), //$NON-NLS-1$
            true, false, true );
        dialogBox.center();
      }
    }
  }

  public void executeActionSequence( final FileCommand.COMMAND mode ) {
    if ( filesListPanel.getSelectedFileItems() == null || filesListPanel.getSelectedFileItems().size() != 1 ) {
      return;
    }

    // open in content panel
    AbstractCommand authCmd = new AbstractCommand() {
      protected void performOperation() {
        performOperation( false );
      }

      protected void performOperation( boolean feedback ) {
        final FileItem selectedFileItem = filesListPanel.getSelectedFileItems().get( 0 );
        String url = null;
        url =
            "api/repo/files/" + SolutionBrowserPanel.pathToId( filesListPanel.getSelectedFileItems().get( 0 ).getRepositoryFile().getPath() ) + "/generatedContent"; //$NON-NLS-1$ //$NON-NLS-2$
        url = getPath() + url;

        if ( mode == FileCommand.COMMAND.BACKGROUND ) {
          MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "info" ), //$NON-NLS-1$
              Messages.getString( "backgroundExecutionWarning" ), //$NON-NLS-1$
              true, false, true );
          dialogBox.center();

          url += "&background=true"; //$NON-NLS-1$

          RequestBuilder builder = new RequestBuilder( RequestBuilder.GET, url );
          try {
            builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
            builder.sendRequest( null, new RequestCallback() {

              public void onError( Request request, Throwable exception ) {
                MessageDialogBox dialogBox =
                    new MessageDialogBox(
                        Messages.getString( "error" ), Messages.getString( "couldNotBackgroundExecute" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
                dialogBox.center();
              }

              public void onResponseReceived( Request request, Response response ) {
              }

            } );
          } catch ( RequestException e ) {
            //ignored
          }
        } else if ( mode == FileCommand.COMMAND.NEWWINDOW ) {
          // popup blockers might attack this
          Window.open( url, "_blank", "menubar=yes,location=no,resizable=yes,scrollbars=yes,status=no" ); //$NON-NLS-1$ //$NON-NLS-2$
        } else if ( mode == FileCommand.COMMAND.SUBSCRIBE ) {
          final String myurl = url + "&subscribepage=yes"; //$NON-NLS-1$
          contentTabPanel.showNewURLTab( selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(),
              myurl, true );
        } else {
          contentTabPanel.showNewURLTab( selectedFileItem.getLocalizedName(), selectedFileItem.getLocalizedName(), url,
              true );
        }
      }

    };

    authCmd.execute();
  }

  public MantleTabPanel getContentTabPanel() {
    return contentTabPanel;
  }

  public boolean isAdministrator() {
    return isAdministrator;
  }

  public void setAdministrator( boolean isAdministrator ) {
    this.isAdministrator = isAdministrator;
    solutionTree.setAdministrator( isAdministrator );
  }

  public boolean isScheduler() {
    return isScheduler;
  }

  public void setScheduler( boolean isScheduler ) {
    this.isScheduler = isScheduler;
  }

  public boolean isNavigatorShowing() {
    return showSolutionBrowser;
  }

  public void setNavigatorShowing( final boolean showSolutionBrowser ) {
    this.showSolutionBrowser = showSolutionBrowser;
    if ( showSolutionBrowser ) {
      solutionNavigatorPanel.setVisible( true ); //$NON-NLS-1$
      navigatorAndContentSplit.setWidgetSize( solutionNavigatorPanel, defaultSplitPosition );

      // Show splitter pane
      if ( this.navigatorAndContentSplit != null ) {
        for ( int i = 0; i < this.navigatorAndContentSplit.getWidgetCount(); i++ ) {
          if ( this.navigatorAndContentSplit.getWidget( i ).getStyleName().equals( "gwt-SplitLayoutPanel-HDragger" ) ) {
            this.navigatorAndContentSplit.getWidget( i ).getElement().getParentElement().removeClassName( "hidden" );
            this.contentTabPanel.getElement().getParentElement().removeClassName( "alignleft" );
          }
        }
      }
    } else {
      // Hide splitter pane
      if ( this.navigatorAndContentSplit != null ) {
        for ( int i = 0; i < this.navigatorAndContentSplit.getWidgetCount(); i++ ) {
          if ( this.navigatorAndContentSplit.getWidget( i ).getStyleName().equals( "gwt-SplitLayoutPanel-HDragger" ) ) {
            this.navigatorAndContentSplit.getWidget( i ).getElement().getParentElement().setClassName( "hidden" );
            this.contentTabPanel.getElement().getParentElement().setClassName( "alignleft" );
          }
        }
      }
      navigatorAndContentSplit.setWidgetSize( solutionNavigatorPanel, 0 );
      solutionNavigatorPanel.setVisible( false );
    }
    adjustContentPanelSize();
  }

  public SolutionTree getSolutionTree() {
    return solutionTree;
  }

  public FilesListPanel getFilesListPanel() {
    return filesListPanel;
  }

  public PickupDragController getDragController() {
    return dragController;
  }

  public void setDragController( PickupDragController dragController ) {
    this.dragController = dragController;
  }

  public String getPath() {
    String mypath = Window.Location.getPath();
    if ( !mypath.endsWith( "/" ) ) { //$NON-NLS-1$
      mypath = mypath.substring( 0, mypath.lastIndexOf( "/" ) + 1 ); //$NON-NLS-1$
    }
    mypath = mypath.replaceAll( "/mantle/", "/" ); //$NON-NLS-1$ //$NON-NLS-2$
    if ( !mypath.endsWith( "/" ) ) { //$NON-NLS-1$
      mypath = "/" + mypath; //$NON-NLS-1$
    }
    return mypath;
  }

  private static final native String encodeUri( String URI )
  /*-{
    return encodeURIComponent(URI);
  }-*/;

}
