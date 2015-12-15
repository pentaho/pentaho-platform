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

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.mantle.client.EmptyRequestCallback;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.ui.PerspectiveManager;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class CollapseBrowserCommandTest {
  CollapseBrowserCommand collapseBrowserCommand;

  @Before public void setUp() {
    collapseBrowserCommand = spy( new CollapseBrowserCommand() );
  }

  @Test public void testPerformOperation() throws RequestException {
    SolutionBrowserPanel mockSolutionBrowserPanel = mock( SolutionBrowserPanel.class );
    doReturn( mockSolutionBrowserPanel ).when( collapseBrowserCommand ).getSolutionBrowserPanel();

    PerspectiveManager mockPerspectiveManager = mock( PerspectiveManager.class );
    doReturn( mockPerspectiveManager ).when( collapseBrowserCommand ).getPerspectiveManager();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( collapseBrowserCommand )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    doReturn( false ).when( mockSolutionBrowserPanel ).isNavigatorShowing();

    // TEST1
    doNothing().when( collapseBrowserCommand ).performOperation( anyBoolean() );
    collapseBrowserCommand.performOperation();

    verify( collapseBrowserCommand ).performOperation( false );

    // TEST2
    doCallRealMethod().when( collapseBrowserCommand ).performOperation( anyBoolean() );

    collapseBrowserCommand.performOperation( true );

    verify( mockPerspectiveManager ).setPerspective( PerspectiveManager.OPENED_PERSPECTIVE );
    verify( mockSolutionBrowserPanel ).setNavigatorShowing( false );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( "false", EmptyRequestCallback.getInstance() );
  }
}
