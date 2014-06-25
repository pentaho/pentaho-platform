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

package org.pentaho.mantle.client.ui.tabs;

import org.pentaho.gwt.widgets.client.dialogs.IDialogCallback;
import org.pentaho.gwt.widgets.client.dialogs.PromptDialogBox;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.MantlePopupPanel;
import org.pentaho.mantle.client.solutionbrowser.tabs.IFrameTabPanel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class MantleTab extends org.pentaho.gwt.widgets.client.tabs.PentahoTab {

  private PopupPanel popupMenu = new MantlePopupPanel( true );
  private boolean solutionBrowserShowing;

  private static enum TABCOMMANDTYPE {
    BACK, RELOAD, RELOAD_ALL, CLOSE, CLOSE_ALL, CLOSE_OTHERS, NEW_WINDOW, CREATE_DEEP_LINK
  }

  private class TabCommand implements Command {

    TABCOMMANDTYPE mode = TABCOMMANDTYPE.RELOAD;
    PopupPanel popupMenu;

    public TabCommand( TABCOMMANDTYPE inMode, PopupPanel popupMenu ) {
      this.mode = inMode;
      this.popupMenu = popupMenu;
    }

    public void execute() {
      popupMenu.hide();
      if ( mode == TABCOMMANDTYPE.RELOAD ) {
        reloadTab();
      } else if ( mode == TABCOMMANDTYPE.RELOAD_ALL ) {
        reloadAllTabs();
      } else if ( mode == TABCOMMANDTYPE.CLOSE ) {
        closeTab();
      } else if ( mode == TABCOMMANDTYPE.CLOSE_OTHERS ) {
        getTabPanel().closeOtherTabs( MantleTab.this );
      } else if ( mode == TABCOMMANDTYPE.CLOSE_ALL ) {
        getTabPanel().closeAllTabs();
      } else if ( mode == TABCOMMANDTYPE.NEW_WINDOW ) {
        openTabInNewWindow();
      } else if ( mode == TABCOMMANDTYPE.CREATE_DEEP_LINK ) {
        createDeepLink();
      } else if ( mode == TABCOMMANDTYPE.BACK ) {
        back();
      }
    }
  }

  public void createDeepLink() {
    if ( getContent() instanceof IFrameTabPanel ) {
      PromptDialogBox dialogBox =
          new PromptDialogBox(
              Messages.getString( "deepLink" ), Messages.getString( "ok" ), Messages.getString( "cancel" ), false, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
              true );

      String startup = ( (IFrameTabPanel) getContent() ).getUrl();
      if ( !StringUtils.isEmpty( ( (IFrameTabPanel) getContent() ).getDeepLinkUrl() ) ) {
        startup = ( (IFrameTabPanel) getContent() ).getDeepLinkUrl();
      }

      UrlBuilder builder = new UrlBuilder();
      builder.setProtocol( Window.Location.getProtocol() );
      builder.setHost( Window.Location.getHostName() );
      builder.setPort( Integer.parseInt( Window.Location.getPort() ) );
      builder.setPath( Window.Location.getPath() );
      //UrlBuilder will encode spaces as '+' which is a valid special character so we replace all spaces with '%20'
      builder.setParameter( "name", getLabelText().replaceAll( "\\s", "%20" ) );
      //the startup string is already encoded with ':' being replaced with '\t' and then encoded again...
      builder.setParameter( "startup-url", startup );

      final TextArea urlbox = new TextArea();
      //encode any space characters
      urlbox.setText( builder.buildString() );
      urlbox.setReadOnly( true );
      urlbox.setVisibleLines( 3 );
      dialogBox.setContent( urlbox );
      urlbox.setHeight( "80px" );
      urlbox.setWidth( "600px" );
      urlbox.addClickHandler( new ClickHandler() {
        public void onClick( ClickEvent event ) {
          urlbox.selectAll();
        }
      } );
      dialogBox.center();
      urlbox.selectAll();
    }
  }

  public void openTabInNewWindow() {
    if ( getContent() instanceof IFrameTabPanel ) {
      VerticalPanel vp = new VerticalPanel();
      vp.add( new Label( Messages.getString( "openWindowQuestion" ) ) ); //$NON-NLS-1$

      final PromptDialogBox openNewWindowConfirmDialog =
          new PromptDialogBox(
              Messages.getString( "openWindowConfirm" ), Messages.getString( "yes" ), Messages.getString( "no" ), false, true, vp ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
          openNewWindowConfirmDialog.hide();
        }

        public void okPressed() {
          ( (IFrameTabPanel) getContent() ).openTabInNewWindow();
          openNewWindowConfirmDialog.hide();
        }
      };
      openNewWindowConfirmDialog.setCallback( callback );
      openNewWindowConfirmDialog.center();
    }
  }

  public void back() {
    ( (IFrameTabPanel) getContent() ).back();
  }

  public void reloadTab() {
    if ( getContent() instanceof IFrameTabPanel ) {
      VerticalPanel vp = new VerticalPanel();
      vp.add( new Label( Messages.getString( "reloadQuestion" ) ) ); //$NON-NLS-1$

      final PromptDialogBox reloadConfirmDialog =
          new PromptDialogBox(
              Messages.getString( "reloadConfirm" ), Messages.getString( "yes" ), Messages.getString( "no" ), false, true, vp ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      final IDialogCallback callback = new IDialogCallback() {

        public void cancelPressed() {
          reloadConfirmDialog.hide();
        }

        public void okPressed() {
          ( (IFrameTabPanel) getContent() ).reload();
          reloadConfirmDialog.hide();
        }
      };
      reloadConfirmDialog.setCallback( callback );
      reloadConfirmDialog.center();
    }
  }

  public void reloadAllTabs() {
    for ( int i = 0; i < getTabPanel().getTabCount(); i++ ) {
      if ( getTabPanel().getTab( i ).getContent() instanceof IFrameTabPanel ) {
        ( (IFrameTabPanel) getTabPanel().getTab( i ).getContent() ).reload();
      }
    }
  }

  public MantleTab( String text, String tooltip, MantleTabPanel tabPanel, Widget content, boolean closeable ) {
    super( text, tooltip, tabPanel, content, closeable );
    popupMenu.addCloseHandler( new CloseHandler<PopupPanel>() {
      public void onClose( CloseEvent<PopupPanel> event ) {
        FrameUtils.setEmbedVisibility( ( (IFrameTabPanel) getTabPanel().getSelectedTab().getContent() ).getFrame(),
            true );
        new Timer() {
          public void run() {
            getContent().getElement().getStyle().setHeight( 100, Unit.PCT );
          }
        } .schedule( 250 );
      }
    } );
  }

  public void onDoubleClick( Event event ) {
    openTabInNewWindow();
  }

  /**
   * Correct 'left' if necessary to avoid menu be very narrow if it is too close to right edge
   */
  private int adjustLeftIfNecessary( int left ) {
    int WIDHT = 225; //supposed width of popup menu

    if ( left + WIDHT > Window.getClientWidth() ) {
      return Window.getClientWidth() - WIDHT;
    }
    return left;
  }

  public void onRightClick( Event event ) {
    FrameUtils.setEmbedVisibility( ( (IFrameTabPanel) getTabPanel().getSelectedTab().getContent() ).getFrame(), false );

    int left = Window.getScrollLeft() + DOM.eventGetClientX( event );
    int top = Window.getScrollTop() + DOM.eventGetClientY( event );
    popupMenu.setPopupPosition( adjustLeftIfNecessary( left ), top );
    MenuBar menuBar = new MenuBar( true );
    menuBar.setAutoOpen( true );
    if ( getContent() instanceof IFrameTabPanel ) {
      MenuItem backMenuItem =
          new MenuItem( Messages.getString( "back" ), new TabCommand( TABCOMMANDTYPE.BACK, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( backMenuItem );
      backMenuItem.getElement().setId( "back" ); //$NON-NLS-1$
      menuBar.addSeparator();
      MenuItem reloadTabMenuItem =
          new MenuItem( Messages.getString( "reloadTab" ), new TabCommand( TABCOMMANDTYPE.RELOAD, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( reloadTabMenuItem );
      reloadTabMenuItem.getElement().setId( "reloadTab" ); //$NON-NLS-1$
    }
    if ( getTabPanel().getTabCount() > 1 ) {
      MenuItem reloadAllTabsMenuItem =
          new MenuItem( Messages.getString( "reloadAllTabs" ), new TabCommand( TABCOMMANDTYPE.RELOAD_ALL, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( reloadAllTabsMenuItem );
      reloadAllTabsMenuItem.getElement().setId( "reloadAllTabs" ); //$NON-NLS-1$
    } else {
      MenuItem reloadAllTabsMenuItem = new MenuItem( Messages.getString( "reloadAllTabs" ), (Command) null ); //$NON-NLS-1$
      menuBar.addItem( reloadAllTabsMenuItem );
      reloadAllTabsMenuItem.getElement().setId( "reloadAllTabs" ); //$NON-NLS-1$
      reloadAllTabsMenuItem.setStyleName( "disabledMenuItem" ); //$NON-NLS-1$
    }
    menuBar.addSeparator();
    if ( getContent() instanceof IFrameTabPanel ) {
      MenuItem openTabInNewWindowMenuItem =
          new MenuItem(
              Messages.getString( "openTabInNewWindow" ), new TabCommand( TABCOMMANDTYPE.NEW_WINDOW, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( openTabInNewWindowMenuItem );
      openTabInNewWindowMenuItem.getElement().setId( "openTabInNewWindow" ); //$NON-NLS-1$
      MenuItem createDeepLinkMenuItem =
          new MenuItem(
              Messages.getString( "createDeepLink" ), new TabCommand( TABCOMMANDTYPE.CREATE_DEEP_LINK, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( createDeepLinkMenuItem );
      createDeepLinkMenuItem.getElement().setId( "deepLink" ); //$NON-NLS-1$
      menuBar.addSeparator();
    }
    menuBar
        .addItem( new MenuItem( Messages.getString( "closeTab" ), new TabCommand( TABCOMMANDTYPE.CLOSE, popupMenu ) ) ); //$NON-NLS-1$
    if ( getTabPanel().getTabCount() > 1 ) {
      MenuItem closeOtherTabsMenuItem =
          new MenuItem( Messages.getString( "closeOtherTabs" ), new TabCommand( TABCOMMANDTYPE.CLOSE_OTHERS, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( closeOtherTabsMenuItem );
      closeOtherTabsMenuItem.getElement().setId( "closeOtherTabs" ); //$NON-NLS-1$
      MenuItem closeAllTabsMenuItem =
          new MenuItem( Messages.getString( "closeAllTabs" ), new TabCommand( TABCOMMANDTYPE.CLOSE_ALL, popupMenu ) ); //$NON-NLS-1$
      menuBar.addItem( closeAllTabsMenuItem );
      closeAllTabsMenuItem.getElement().setId( "closeAllTabs" ); //$NON-NLS-1$
    } else {
      MenuItem closeOtherTabsMenuItem = new MenuItem( Messages.getString( "closeOtherTabs" ), (Command) null ); //$NON-NLS-1$
      closeOtherTabsMenuItem.setStyleName( "disabledMenuItem" ); //$NON-NLS-1$
      MenuItem closeAllTabsMenuItem = new MenuItem( Messages.getString( "closeAllTabs" ), (Command) null ); //$NON-NLS-1$
      closeAllTabsMenuItem.setStyleName( "disabledMenuItem" ); //$NON-NLS-1$
      menuBar.addItem( closeOtherTabsMenuItem );
      menuBar.addItem( closeAllTabsMenuItem );
      closeOtherTabsMenuItem.getElement().setId( "closeOtherTabs" ); //$NON-NLS-1$
      closeAllTabsMenuItem.getElement().setId( "closeAllTabs" ); //$NON-NLS-1$
    }
    popupMenu.setWidget( menuBar );
    popupMenu.hide();
    popupMenu.show();
  }

  @Override
  public void setLabelText( String text ) {
    super.setLabelText( text );

    if ( getContent() instanceof IFrameTabPanel ) {
      // this causes saved frames of the same type to all enjoy the same ID, which is
      // severely problematic for getting the callback hook - it will give you the wrong one
      // ((IFrameTabPanel) getContent()).setId(text);
      ( (IFrameTabPanel) getContent() ).setId( text + System.currentTimeMillis() );
    }
  }

  public boolean isSolutionBrowserShowing() {
    return solutionBrowserShowing;
  }

  public void setSolutionBrowserShowing( boolean solutionBrowserShowing ) {
    this.solutionBrowserShowing = solutionBrowserShowing;
  }
}
