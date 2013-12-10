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

import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.admin.ContentCleanerPanel;
import org.pentaho.mantle.client.admin.EmailAdminPanelController;
import org.pentaho.mantle.client.admin.UserRolesAdminPanelController;
import org.pentaho.mantle.client.commands.FilePropertiesCommand;
import org.pentaho.mantle.client.commands.NewDropdownCommand;
import org.pentaho.mantle.client.commands.OpenDocCommand;
import org.pentaho.mantle.client.commands.OpenFileCommand;
import org.pentaho.mantle.client.commands.PrintCommand;
import org.pentaho.mantle.client.commands.RefreshRepositoryCommand;
import org.pentaho.mantle.client.commands.RefreshSchedulesCommand;
import org.pentaho.mantle.client.commands.SaveCommand;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.ISolutionBrowserEvent;
import org.pentaho.mantle.client.events.SolutionBrowserCloseEvent;
import org.pentaho.mantle.client.events.SolutionBrowserCloseEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserDeselectEvent;
import org.pentaho.mantle.client.events.SolutionBrowserDeselectEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserOpenEvent;
import org.pentaho.mantle.client.events.SolutionBrowserOpenEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEvent;
import org.pentaho.mantle.client.events.SolutionBrowserSelectEventHandler;
import org.pentaho.mantle.client.events.SolutionBrowserUndefinedEvent;
import org.pentaho.mantle.client.events.SolutionBrowserUndefinedEventHandler;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileItem;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.platform.api.engine.perspective.pojo.IPluginPerspective;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

public class MantleModel extends XulEventSourceAdapter implements SolutionBrowserOpenEventHandler,
    SolutionBrowserCloseEventHandler, SolutionBrowserSelectEventHandler, SolutionBrowserDeselectEventHandler,
    SolutionBrowserUndefinedEventHandler {

  private MantleXul main;

  private boolean saveEnabled;

  private boolean saveAsEnabled;

  private boolean newAnalysisEnabled;

  private boolean contentEditEnabled;

  private boolean contentEditSelected;

  private boolean showBrowserSelected;

  private boolean showNavigatorSelected;

  private boolean propertiesEnabled;

  private boolean printVisible;

  private FileItem selectedFileItem;

  private JavaScriptObject callback;

  public MantleModel( MantleXul main ) {
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserOpenEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserCloseEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserSelectEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserDeselectEvent.TYPE, this );
    EventBusUtil.EVENT_BUS.addHandler( SolutionBrowserUndefinedEvent.TYPE, this );
    this.main = main;
  }

  @Bindable
  public boolean isPropertiesEnabled() {
    return this.propertiesEnabled;
  }

  @Bindable
  public void setPropertiesEnabled( Boolean enabled ) {
    boolean prevVal = this.propertiesEnabled;
    propertiesEnabled = enabled;
    this.firePropertyChange( "propertiesEnabled", prevVal, propertiesEnabled );
  }

  @Bindable
  public boolean isSaveEnabled() {
    return this.saveEnabled;
  }

  @Bindable
  public void setSaveEnabled( Boolean enabled ) {
    boolean prevVal = this.saveEnabled;
    saveEnabled = enabled;
    this.firePropertyChange( "saveEnabled", prevVal, saveEnabled );
  }

  @Bindable
  public boolean isSaveAsEnabled() {
    return this.saveAsEnabled;
  }

  @Bindable
  public void setSaveAsEnabled( Boolean enabled ) {
    boolean prevVal = this.saveAsEnabled;
    saveAsEnabled = enabled;
    this.firePropertyChange( "saveAsEnabled", prevVal, saveAsEnabled );
  }

  @Bindable
  public void setPrintVisible( Boolean visible ) {
    boolean prevVal = printVisible;
    printVisible = visible;
    this.firePropertyChange( "printVisible", prevVal, printVisible );
  }

  @Bindable
  public boolean isPrintVisible() {
    return this.printVisible;
  }

  @Bindable
  public void executePropertiesCommand() {
    FilePropertiesCommand propertiesCommand = new FilePropertiesCommand( selectedFileItem.getRepositoryFile() );
    propertiesCommand.execute();
  }

  @Bindable
  public void executeSaveCommand() {
    SaveCommand saveCommand = new SaveCommand();
    saveCommand.execute();
  }

  @Bindable
  public void executeSaveAsCommand() {
    SaveCommand saveAsCommand = new SaveCommand( true );
    saveAsCommand.execute();
  }

  @Bindable
  public void executeEditContent() {
    OpenFileCommand cmd = new OpenFileCommand( COMMAND.EDIT );
    cmd.execute();
  }

  @Bindable
  public void executeShareContent() {
    OpenFileCommand cmd = new OpenFileCommand( COMMAND.SHARE );
    cmd.execute();
  }

  @Bindable
  public void executeScheduleContent() {
    OpenFileCommand cmd = new OpenFileCommand( COMMAND.SCHEDULE_NEW );
    cmd.execute();
  }

  @Bindable
  public void executePrintCommand() {
    PrintCommand printCommand = new PrintCommand();
    printCommand.execute();
  }

  @Bindable
  public void showSchedules() {
    IPluginPerspective perspective = PerspectiveManager.getInstance().getActivePerspective();
    boolean showing = perspective.getId().equalsIgnoreCase( PerspectiveManager.SCHEDULES_PERSPECTIVE );
    if ( !showing || !this.showBrowserSelected ) {
      PerspectiveManager.getInstance().setPerspective( PerspectiveManager.SCHEDULES_PERSPECTIVE );
    }
  }

  @Bindable
  public void showBrowser() {
    IPluginPerspective perspective = PerspectiveManager.getInstance().getActivePerspective();
    boolean showing = perspective.getId().equalsIgnoreCase( PerspectiveManager.OPENED_PERSPECTIVE );
    if ( !showing ) {
      PerspectiveManager.getInstance().setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
    }
  }

  @Bindable
  public void loadAdminContent( final String securityPanelId, final String url ) {
    // hijack content area (or simply find and select existing content)
    Frame frame = null;
    for ( int i = 0; i < MantleXul.getInstance().getAdminContentDeck().getWidgetCount(); i++ ) {
      Widget w = MantleXul.getInstance().getAdminContentDeck().getWidget( i );
      if ( w instanceof Frame && securityPanelId.equals( w.getElement().getId() ) ) {
        frame = (Frame) w;
      }
    }
    if ( frame == null ) {
      frame = new Frame( url );
      frame.getElement().setId( securityPanelId );
      frame.getElement().setAttribute( "frameBorder", "0" );
      frame.getElement().setAttribute( "allowTransparency", "true" );
      MantleXul.getInstance().getAdminContentDeck().add( frame );
    }
    MantleXul.getInstance().getAdminContentDeck().showWidget(
        MantleXul.getInstance().getAdminContentDeck().getWidgetIndex( frame ) );
  }

  @Bindable
  public void loadSettingsPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        DeckPanel contentDeck = MantleXul.getInstance().getAdminContentDeck();
        if ( contentDeck.getWidgetIndex( ContentCleanerPanel.getInstance() ) == -1 ) {
          contentDeck.add( ContentCleanerPanel.getInstance() );
        }
        contentDeck.showWidget( contentDeck.getWidgetIndex( ContentCleanerPanel.getInstance() ) );
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void loadUserRolesAdminPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        DeckPanel contentDeck = MantleXul.getInstance().getAdminContentDeck();
        if ( MantleApplication.getInstance().getContentDeck().getWidgetIndex(
            UserRolesAdminPanelController.getInstance() ) == -1 ) {
          contentDeck.add( UserRolesAdminPanelController.getInstance() );
        }
        contentDeck.showWidget( contentDeck.getWidgetIndex( UserRolesAdminPanelController.getInstance() ) );
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void loadEmailAdminPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        DeckPanel contentDeck = MantleXul.getInstance().getAdminContentDeck();
        if ( MantleApplication.getInstance().getContentDeck().getWidgetIndex( EmailAdminPanelController
          .getInstance() ) == -1 ) {
          contentDeck.add( EmailAdminPanelController.getInstance() );
        }
        contentDeck.showWidget( contentDeck.getWidgetIndex( EmailAdminPanelController.getInstance() ) );
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void refreshContent() {
    if ( PerspectiveManager.SCHEDULES_PERSPECTIVE.equals( PerspectiveManager.getInstance().getActivePerspective()
        .getId() ) ) {
      Command cmd = new RefreshSchedulesCommand();
      cmd.execute();
    } else {
      Command cmd = new RefreshRepositoryCommand();
      cmd.execute();
    }
  }

  @Bindable
  public void toggleUseDescriptionsForTooltips() {
    SolutionBrowserPanel.getInstance().toggleUseDescriptionCommand.execute();
  }

  @Bindable
  public void toggleShowHideFiles() {
    SolutionBrowserPanel.getInstance().toggleShowHideFilesCommand.execute();
  }

  @Bindable
  public void openDocumentation() {
    OpenDocCommand cmd = new OpenDocCommand();
    cmd.execute();
  }

  @Bindable
  public void setNewAnalysisEnabled( Boolean enabled ) {
    boolean prevVal = this.newAnalysisEnabled;
    newAnalysisEnabled = enabled;

    this.firePropertyChange( "newAnalysisEnabled", prevVal, newAnalysisEnabled );
  }

  @Bindable
  public void executeOpenFileCommand() {
    OpenFileCommand openFileCommand = new OpenFileCommand();
    openFileCommand.execute();
  }

  @Bindable
  public void launchNewDropdownCommand( XulToolbarbutton button ) {
    NewDropdownCommand launchNewDropdownCommand =
        new NewDropdownCommand( ( (ToolbarButton) button.getManagedObject() ).getPushButton() );
    launchNewDropdownCommand.execute();
  }

  public void onUndefinedEvent( SolutionBrowserUndefinedEvent event ) {
    onSolutionBrowserEvent( event );
  }

  public void onTabOpened( SolutionBrowserOpenEvent event ) {
    onSolutionBrowserEvent( event );
  }

  public void onTabSelected( SolutionBrowserSelectEvent event ) {
    onSolutionBrowserEvent( event );
  }

  public void onTabClosed( SolutionBrowserCloseEvent event ) {
    onSolutionBrowserEvent( event );
  }

  public void onTabDeselected( SolutionBrowserDeselectEvent event ) {
    onSolutionBrowserEvent( event );
  }

  private void onSolutionBrowserEvent( ISolutionBrowserEvent event ) {
    FileItem selectedItem = null;
    if ( event.getFileItems() != null && event.getFileItems().size() > 0 ) {
      selectedItem = event.getFileItems().get( 0 );
    }
    try {
      handleSolutionBrowserEvent( event.getWidget(), selectedItem );
    } catch ( Throwable t ) {
      MantleApplication.log( t.getMessage() );
    }
    if ( event.getWidget() != null ) {
      main.removeOverlays( ( (IFrameTabPanel) event.getWidget() ).getOverlayIds() );
    }
  }

  private void handleSolutionBrowserEvent( Widget panel, FileItem selectedFileItem ) {
    this.selectedFileItem = selectedFileItem;
    setPropertiesEnabled( selectedFileItem != null && selectedFileItem.getRepositoryFile() != null );
    setSaveEnabled( selectedFileItem != null && selectedFileItem.getRepositoryFile() != null );
    setSaveAsEnabled( selectedFileItem != null && selectedFileItem.getRepositoryFile() != null );

    boolean saveEnabled = false;
    boolean editIsEnabled = false;
    boolean editSelected = false;
    boolean printVisible = false;

    JavaScriptObject callback = null;

    if ( panel != null && panel instanceof IFrameTabPanel ) {
      IFrameTabPanel tbp = (IFrameTabPanel) panel;
      saveEnabled = tbp.isSaveEnabled();
      editIsEnabled = tbp.isEditEnabled();
      editSelected = tbp.isEditSelected();
      printVisible = tbp.isPrintVisible();
    }

    setSaveEnabled( saveEnabled );
    setSaveAsEnabled( saveEnabled );
    setContentEditEnabled( editIsEnabled );
    setContentEditSelected( editSelected );
    setPrintVisible( printVisible );
    setCallback( callback );
    this.showNavigatorSelected = SolutionBrowserPanel.getInstance().isNavigatorShowing();
    setShowBrowserSelected( this.showNavigatorSelected );
  }

  @Bindable
  public boolean isShowBrowserSelected() {
    return showBrowserSelected;
  }

  @Bindable
  public void setShowBrowserSelected( boolean showBrowserSelected ) {
    boolean prevVal = this.showBrowserSelected;
    this.showBrowserSelected = showBrowserSelected;
    this.firePropertyChange( "showBrowserSelected", prevVal, showBrowserSelected );
  }

  @Bindable
  public void setContentEditEnabled( boolean enable ) {
    boolean prevVal = this.contentEditEnabled;
    contentEditEnabled = enable;
    this.firePropertyChange( "contentEditEnabled", prevVal, contentEditEnabled );
  }

  @Bindable
  public void setContentEditSelected( boolean selected ) {
    boolean prevVal = this.contentEditSelected;
    contentEditSelected = selected;
    this.firePropertyChange( "contentEditSelected", prevVal, contentEditSelected );
  }

  @Bindable
  public boolean isContentEditSelected() {
    return this.contentEditSelected;
  }

  @Bindable
  public void setContentEditToggled() {
    setContentEditSelected( !this.contentEditSelected );
  }

  @Bindable
  public boolean isContentEditEnabled() {
    return contentEditEnabled;
  }

  public JavaScriptObject getCallback() {
    return callback;
  }

  public void setCallback( JavaScriptObject callback ) {
    this.callback = callback;
  }

  @Bindable
  public boolean isShowNavigatorSelected() {
    return this.showNavigatorSelected;
  }

  @Bindable
  public void setShowNavigatorSelected( boolean showNavigator ) {
    this.showNavigatorSelected = showNavigator;
  }

}
