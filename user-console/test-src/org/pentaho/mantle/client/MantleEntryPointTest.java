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

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class MantleEntryPointTest {
  MantleEntryPoint mantleEntryPoint;

  @Before public void setUp() {
    mantleEntryPoint = spy( new MantleEntryPoint() );
  }

  @Test public void testOnModuleLoad() throws RequestException {
    ResourceBundle mockResourceBundle = mock( ResourceBundle.class );
    doReturn( mockResourceBundle ).when( mantleEntryPoint ).getResourceBundle();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( mantleEntryPoint )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    doReturn( "" ).when( mantleEntryPoint ).getLocationParameter( anyString() );
    mantleEntryPoint.onModuleLoad();

    verify( mockResourceBundle ).loadBundle( anyString(), eq( "mantleMessages" ), eq( true ), eq( mantleEntryPoint ) );
    verify( mockRequestBuilder, never() ).setHeader( anyString(), anyString() );

    // TEST2
    String locale = "locale";
    doReturn( locale ).when( mantleEntryPoint ).getLocationParameter( anyString() );
    mantleEntryPoint.onModuleLoad();

    verify( mockResourceBundle, times( 2 ) )
        .loadBundle( anyString(), eq( "mantleMessages" ), eq( true ), eq( mantleEntryPoint ) );
    verify( mantleEntryPoint ).getRequestBuilder( eq( RequestBuilder.POST ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( eq( locale ), any( RequestCallback.class ) );

    // TEST3
    doThrow( new RequestException() ).when( mockRequestBuilder )
        .sendRequest( eq( locale ), any( RequestCallback.class ) );

    mantleEntryPoint.onModuleLoad();
    verify( mockRequestBuilder, times( 2 ) ).sendRequest( eq( locale ), any( RequestCallback.class ) );
    verify( mockResourceBundle, times( 3 ) )
        .loadBundle( anyString(), eq( "mantleMessages" ), eq( true ), eq( mantleEntryPoint ) );
  }

  @Test public void testBundleLoaded() {
    String bundleName = "bundleName";

    MantleApplication mockMantleApplication = mock( MantleApplication.class );
    doReturn( mockMantleApplication ).when( mantleEntryPoint ).getMantleApplication();

    RootPanel mockRootPanel = mock( RootPanel.class );

    // TEST1
    doReturn( null ).when( mantleEntryPoint ).getRootPanel( anyString() );
    mantleEntryPoint.bundleLoaded( bundleName );

    verify( mockMantleApplication ).loadApplication();
    verify( mockRootPanel, never() ).removeFromParent();
    verify( mockRootPanel, never() ).setVisible( false );
    verify( mockRootPanel, never() ).setHeight( "0px" );

    // TEST2
    doReturn( mockRootPanel ).when( mantleEntryPoint ).getRootPanel( anyString() );
    mantleEntryPoint.bundleLoaded( bundleName );

    verify( mockMantleApplication, times( 2 ) ).loadApplication();
    verify( mockRootPanel ).removeFromParent();
    verify( mockRootPanel ).setVisible( false );
    verify( mockRootPanel ).setHeight( "0px" );

  }
}
