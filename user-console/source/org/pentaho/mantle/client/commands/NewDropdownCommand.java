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

package org.pentaho.mantle.client.commands;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.utils.FrameUtils;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.objects.JsCreateNewConfig;
import org.pentaho.mantle.client.objects.JsCreateNewConfigComparator;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;

import java.util.ArrayList;
import java.util.Collections;

public class NewDropdownCommand extends AbstractCommand {

  private static FocusPanel pageBackground = null;
  private Widget anchorWidget;

  public NewDropdownCommand() {
  }

  public NewDropdownCommand( Widget anchorWidget ) {
    this.anchorWidget = anchorWidget;
  }

  protected void performOperation() {
    performOperation( true );
  }

  protected void performOperation( boolean feedback ) {
    final PopupPanel popup = new PopupPanel( true, false ) {
      public void show() {
        // show glass pane
        super.show();
        if ( pageBackground == null ) {
          pageBackground = new FocusPanel() {
            public void onBrowserEvent( Event event ) {
              int type = event.getTypeInt();
              switch ( type ) {
                case Event.ONKEYDOWN: {
                  if ( (char) event.getKeyCode() == KeyCodes.KEY_ESCAPE ) {
                    event.stopPropagation();
                    hide();
                  }
                  return;
                }
              }
              super.onBrowserEvent( event );
            };
          };
          pageBackground.addClickHandler( new ClickHandler() {
            public void onClick( ClickEvent event ) {
              hide();
              pageBackground.setVisible( false );
              pageBackground.getElement().getStyle().setDisplay( Display.NONE );
            }
          } );
          RootPanel.get().add( pageBackground, 0, 0 );
        }
        pageBackground.setSize( "100%", Window.getClientHeight() + Window.getScrollTop() + "px" ); //$NON-NLS-1$ //$NON-NLS-2$
        pageBackground.setVisible( true );
        pageBackground.getElement().getStyle().setDisplay( Display.BLOCK );

        // hide <embeds>
        // TODO: migrate to GlassPane Listener
        FrameUtils.toggleEmbedVisibility( false );

        // Notify listeners that we're showing a dialog (hide PDFs, flash).
        GlassPane.getInstance().show();
      }

      public void hide( boolean autoClosed ) {
        super.hide( autoClosed );
        pageBackground.setVisible( false );
        GlassPane.getInstance().hide();
      }

      protected void onPreviewNativeEvent( final NativePreviewEvent event ) {
        // Switch on the event type
        int type = event.getTypeInt();
        switch ( type ) {
          case Event.ONKEYDOWN: {
            Event nativeEvent = Event.as( event.getNativeEvent() );
            if ( (char) nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE ) {
              event.cancel();
              hide();
            }
            return;
          }
        }
      };
    };

    if ( popup.isShowing() ) {
      popup.hide();
      return;
    }

    String url = GWT.getHostPageBaseURL() + "api/plugin-manager/settings/new-toolbar-button"; //$NON-NLS-1$
    RequestBuilder rb = new RequestBuilder( RequestBuilder.GET, url );
    rb.setHeader( "Content-Type", "text/plain" ); //$NON-NLS-1$//$NON-NLS-2$
    rb.setHeader( "accept", "application/json" ); //$NON-NLS-1$//$NON-NLS-2$
    rb.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    try {
      rb.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), exception.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
              false, false, true );
          dialogBox.center();
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() == 200 ) {
            JsArray<JsCreateNewConfig> jsarray = parseJson( JsonUtils.escapeJsonForEval( response.getText() ) );
            final ArrayList<JsCreateNewConfig> sorted = new ArrayList<JsCreateNewConfig>();
            for ( int i = 0; i < jsarray.length(); i++ ) {
              sorted.add( jsarray.get( i ) );
            }
            Collections.sort( sorted, new JsCreateNewConfigComparator() );
            popup.setStyleName( "newToolbarDropdown" );
            VerticalPanel buttonPanel = new VerticalPanel();
            popup.add( buttonPanel );
            for ( int i = 0; i < sorted.size(); i++ ) {
              final int finali = i;
              Button button = new Button( Messages.getString( sorted.get( i ).getLabel() ) );
              button.setStyleName( "pentaho-button" );
              button.getElement().addClassName( "newToolbarDropdownButton" );
              button.addClickHandler( new ClickHandler() {
                public void onClick( ClickEvent event ) {
                    if (sorted.get( finali ).getActionUrl().startsWith("javascript:")) {
                      doEvalJS(sorted.get( finali ).getActionUrl().substring("javascript:".length()));
                    } else {
                      SolutionBrowserPanel.getInstance().getContentTabPanel().showNewURLTab(
                        Messages.getString( sorted.get( finali ).getTabName() ),
                        Messages.getString( sorted.get( finali ).getTabName() ), sorted.get( finali ).getActionUrl(),
                        false );
                    }
                  popup.hide();
                }
              } );
              String name = sorted.get( i ).getName();
              if ("data-access".equals(name)) {
                  buttonPanel.add(new HTML("<hr style='color: #a7a7a7' />"));
              }
              buttonPanel.add( button );
            }
            popup.setPopupPosition( anchorWidget.getAbsoluteLeft(), anchorWidget.getAbsoluteTop()
                + anchorWidget.getOffsetHeight() );
            popup.show();
          } else {
            MessageDialogBox dialogBox =
                new MessageDialogBox( Messages.getString( "error" ), Messages.getString( "error" ), //$NON-NLS-1$ //$NON-NLS-2$
                    false, false, true );
            dialogBox.center();
          }
        }

      } );
    } catch ( RequestException e ) {
      MessageDialogBox dialogBox = new MessageDialogBox( Messages.getString( "error" ), e.getMessage(), //$NON-NLS-1$ //$NON-NLS-2$
          false, false, true );
      dialogBox.center();
    }

  }

  public static native void doEvalJS(String js) /*-{
    eval(js);
}-*/;
  
  private native JsArray<JsCreateNewConfig> parseJson( String json )
  /*-{
    var obj = eval('(' + json + ')');
    return obj.Item;
  }-*/;

  public Widget getAnchorWidget() {
    return anchorWidget;
  }

  public void setAnchorWidget( Widget anchorWidget ) {
    this.anchorWidget = anchorWidget;
  }
}
