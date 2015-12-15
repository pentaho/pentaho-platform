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

package org.pentaho.mantle.client.admin;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class ContentCleanerPanelTest {
  ContentCleanerPanel contentCleanerPanel;

  @Before public void setUp() {
    contentCleanerPanel = spy( ContentCleanerPanel.getInstance() );
  }

  @Test public void testGetInstance() {
    assertNotNull( contentCleanerPanel );
  }

  @Test public void testActivate() throws RequestException {
    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( contentCleanerPanel )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    contentCleanerPanel.activate();

    verify( contentCleanerPanel ).clear();
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "Content-Type", "application/json" );
    verify( mockRequestBuilder ).setHeader( "accept", "application/json" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    doThrow( RequestException.class ).when( mockRequestBuilder )
        .sendRequest( anyString(), any( RequestCallback.class ) );

    contentCleanerPanel.activate();
  }

  @Test public void testGetId() {
    assertEquals( "contentCleanerPanel", contentCleanerPanel.getId() );
  }

  @Test public void testPassivate() {
    AsyncCallback mockAsyncCallback = mock( AsyncCallback.class );

    // TEST1
    contentCleanerPanel.passivate( mockAsyncCallback );

    verify( mockAsyncCallback ).onSuccess( true );
  }

  @Test public void testDeleteContentNow() throws RequestException {
    long age = 3;

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( contentCleanerPanel )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    contentCleanerPanel.deleteContentNow( age );

    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "Content-Type", "application/json" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    doThrow( RequestException.class ).when( mockRequestBuilder )
        .sendRequest( anyString(), any( RequestCallback.class ) );

    contentCleanerPanel.deleteContentNow( age );
  }
}
