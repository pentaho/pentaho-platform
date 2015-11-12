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

package org.pentaho.mantle.client;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.mantle.client.commands.LoginCommand;
import org.pentaho.mantle.client.events.MantleSettingsLoadedEvent;
import org.pentaho.mantle.client.events.PerspectivesLoadedEventHandler;
import org.pentaho.mantle.client.events.UserSettingsLoadedEvent;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.ui.UserDropDown;
import org.pentaho.mantle.client.ui.xul.MantleXul;
import org.pentaho.mantle.client.usersettings.MantleSettingsManager;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class MantleApplicationTest {
  MantleApplication mantleApplication;

  @Before public void setUp() {
    mantleApplication = spy( MantleApplication.getInstance() );
  }

  @Test public void testGetInstance() {
    assertNotNull( mantleApplication );
  }

  @Test public void testLoadApplication() {
    UserSettingsManager mockUserSettingsManager = mock( UserSettingsManager.class );
    doReturn( mockUserSettingsManager ).when( mantleApplication ).getUserSettingsManager();

    // TEST1
    mantleApplication.loadApplication();

    verify( mantleApplication ).setupNativeHooks( eq( mantleApplication ), any( LoginCommand.class ) );
    verify( mockUserSettingsManager ).getUserSettings( any( AsyncCallback.class ), eq( false ) );
  }

  @Test public void testNotifyGlasspaneListeners() {
    GlassPane mockGlassPane = mock( GlassPane.class );
    doReturn( mockGlassPane ).when( mantleApplication ).getGlassPane();

    // TEST1
    mantleApplication.notifyGlasspaneListeners( true );

    verify( mockGlassPane ).show();

    // TEST2
    mantleApplication.notifyGlasspaneListeners( false );

    verify( mockGlassPane ).hide();
  }

  @Test public void testOnUserSettingsLoaded() {
    UserSettingsLoadedEvent mockUserSettingsLoadedEvent = mock( UserSettingsLoadedEvent.class );

    MantleSettingsManager mockMantleSettingsManager = mock( MantleSettingsManager.class );
    doReturn( mockMantleSettingsManager ).when( mantleApplication ).getMantleSettingsManager();

    // TEST1
    mantleApplication.onUserSettingsLoaded( mockUserSettingsLoadedEvent );

    verify( mockMantleSettingsManager ).getMantleSettings( any( AsyncCallback.class ), eq( false ) );
  }

  @Test public void testOnMantleSettingsLoaded() throws RequestException {
    MantleSettingsLoadedEvent mockMantleSettingsLoadedEvent = mock( MantleSettingsLoadedEvent.class );

    HashMap mockSettings = mock( HashMap.class );
    doReturn( mockSettings ).when( mockMantleSettingsLoadedEvent ).getSettings();

    String showOnlyPerspectiveSetting = "false";
    doReturn( showOnlyPerspectiveSetting ).when( mockSettings ).get( "showOnlyPerspective" );

    String userConsoleRevisionSetting = "userConsoleRevisionSetting";
    doReturn( userConsoleRevisionSetting ).when( mockSettings ).get( "user-console-revision" );

    RootPanel mockRootPanel = mock( RootPanel.class );
    doReturn( mockRootPanel ).when( mantleApplication ).rootPanelGet( anyString() );
    doReturn( mockRootPanel ).when( mantleApplication ).rootPanelGet( null );

    MantleXul mockMantleXul = mock( MantleXul.class );
    doReturn( mockMantleXul ).when( mantleApplication ).getMantleXul();

    PerspectiveManager mockPerspectiveManager = mock( PerspectiveManager.class );
    doReturn( mockPerspectiveManager ).when( mantleApplication ).getPerspectiveManager();

    Widget mockMenubarWidget = mock( Widget.class );
    doReturn( mockMenubarWidget ).when( mockMantleXul ).getMenubar();

    Widget mockToolbarWidget = mock( Widget.class );
    doReturn( mockToolbarWidget ).when( mockMantleXul ).getToolbar();

    UserDropDown mockUserDropDown = mock( UserDropDown.class );
    doReturn( mockUserDropDown ).when( mantleApplication ).getUserDropDown();

    Label mockLabel = mock( Label.class );
    doReturn( mockLabel ).when( mantleApplication ).getLabel();

    DeckPanel mockDeckPanel = mock( DeckPanel.class );
    mantleApplication.setContentDeck( mockDeckPanel );

    SolutionBrowserPanel mockSolutionBrowserPanel = mock( SolutionBrowserPanel.class );
    doReturn( mockSolutionBrowserPanel ).when( mantleApplication ).getSolutionBrowserPanel();

    Element mockDeckPanelElement = mock( Element.class );
    doReturn( mockDeckPanelElement ).when( mockDeckPanel ).getElement();

    AbsolutePanel mockAbsolutePanel = mock( AbsolutePanel.class );
    MantleApplication.overlayPanel = mockAbsolutePanel;

    Element mockAbsolutePanelElement = mock( Element.class );
    doReturn( mockDeckPanelElement ).when( mockAbsolutePanel ).getElement();

    Style mockAbsolutePanelStyle = mock( Style.class );
    when( mockAbsolutePanel.getElement().getStyle() ).thenReturn( mockAbsolutePanelStyle );

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( mantleApplication )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( mantleApplication )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean() );

    Element mockPucContentElement = mock( Element.class );
    doReturn( mockPucContentElement ).when( mockRootPanel ).getElement();

    Style mockPucContentElementStyle = mock( Style.class );
    when( mockRootPanel.getElement().getStyle() ).thenReturn( mockPucContentElementStyle );

    EventBus mockEventBus = mock( EventBus.class );
    doReturn( mockEventBus ).when( mantleApplication ).getEventBus();

    // TEST1
    String startupPerspective = "";
    doReturn( startupPerspective ).when( mantleApplication ).getWindowLocationParameter( "startupPerspective" );

    String submitOnEnterSetting = null;
    doReturn( submitOnEnterSetting ).when( mockSettings ).get( "submit-on-enter-key" );

    doReturn( "" ).when( mantleApplication ).getWindowLocationParameter( "showOnlyPerspective" );

    mantleApplication.onMantleSettingsLoaded( mockMantleSettingsLoadedEvent );

    verify( mockSettings ).get( "showOnlyPerspective" );
    verify( mockSettings ).get( "user-console-revision" );
    assertEquals( MantleApplication.mantleRevisionOverride, userConsoleRevisionSetting );
    verify( mantleApplication ).rootPanelGet( "pucMenuBar" );
    verify( mantleApplication ).rootPanelGet( "pucPerspectives" );
    verify( mantleApplication ).rootPanelGet( "pucToolBar" );
    verify( mantleApplication ).rootPanelGet( "pucUserDropDown" );
    verify( mockRootPanel ).add( mockMenubarWidget );
    verify( mockRootPanel ).add( mockToolbarWidget );
    verify( mockRootPanel ).add( mockPerspectiveManager );
    verify( mockRootPanel ).add( mockUserDropDown );
    verify( mantleApplication, never() ).rootPanelGet( "pucHeader" );
    verify( mockDeckPanel ).add( mockLabel );
    verify( mockDeckPanel ).showWidget( 0 );
    verify( mockDeckPanel ).add( mockSolutionBrowserPanel );
    verify( mockSolutionBrowserPanel, never() ).setVisible( false );
    verify( mockDeckPanelElement ).setId( "applicationShell" );
    verify( mockDeckPanel ).setStyleName( "applicationShell" );
    verify( mantleApplication ).rootPanelGet( "pucContent" );
    verify( mockRootPanel ).add( mockDeckPanel );
    verify( mockAbsolutePanel ).setVisible( false );
    verify( mockAbsolutePanel ).setHeight( "100%" );
    verify( mockAbsolutePanel ).setWidth( "100%" );
    verify( mockAbsolutePanelStyle ).setProperty( "zIndex", "1000" );
    verify( mockAbsolutePanelStyle ).setProperty( "position", "absolute" );
    verify( mockRootPanel ).add( mockAbsolutePanel, 0, 0 );
    verify( mockSettings ).get( "submit-on-enter-key" );
    assertTrue( mantleApplication.submitOnEnter );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    doThrow( new RequestException() ).when( mockRequestBuilder )
        .sendRequest( anyString(), any( RequestCallback.class ) );

    doReturn( "true" ).when( mockSettings ).get( "startupPerspective" );

    submitOnEnterSetting = "false";
    doReturn( submitOnEnterSetting ).when( mockSettings ).get( "submit-on-enter-key" );

    startupPerspective = "perspective";
    doReturn( startupPerspective ).when( mantleApplication ).getWindowLocationParameter( "startupPerspective" );

    showOnlyPerspectiveSetting = "true";
    doReturn( showOnlyPerspectiveSetting ).when( mockSettings ).get( "showOnlyPerspective" );

    String showOnlyPerspectiveParam = "true";
    doReturn( showOnlyPerspectiveParam ).when( mantleApplication ).getWindowLocationParameter( "showOnlyPerspective" );

    doReturn( true ).when( mockPerspectiveManager ).isLoaded();

    mantleApplication.onMantleSettingsLoaded( mockMantleSettingsLoadedEvent );

    verify( mantleApplication ).rootPanelGet( "pucHeader" );
    verify( mantleApplication, times( 3 ) ).rootPanelGet( "pucContent" );
    verify( mockSolutionBrowserPanel ).setVisible( false );
    assertFalse( mantleApplication.submitOnEnter );
    verify( mantleApplication ).getMessageDialogBox( anyString(), anyString(), eq( false ), eq( false ), eq( true ) );
    verify( mockPucContentElementStyle ).setTop( 0, Style.Unit.PX );
    verify( mockPerspectiveManager ).setPerspective( startupPerspective );

    // TEST3
    doReturn( false ).when( mockPerspectiveManager ).isLoaded();

    mantleApplication.onMantleSettingsLoaded( mockMantleSettingsLoadedEvent );

    verify( mockEventBus ).addHandler( any( GwtEvent.Type.class ), any( PerspectivesLoadedEventHandler.class ) );
  }
}
