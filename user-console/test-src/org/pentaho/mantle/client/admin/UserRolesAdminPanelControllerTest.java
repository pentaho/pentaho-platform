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
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith( GwtMockitoTestRunner.class ) public class UserRolesAdminPanelControllerTest {

  UserRolesAdminPanelController userRolesAdminPanelController;

  @Before public void setUp() {
    userRolesAdminPanelController = spy( UserRolesAdminPanelController.getInstance() );
  }

  @Test public void testGetInstance() {
    assertNotNull( userRolesAdminPanelController );
  }

  @Test public void testSaveUser() throws RequestException {
    String user = "user";
    String password = "password";

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( userRolesAdminPanelController )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    userRolesAdminPanelController.saveUser( user, password );

    verify( userRolesAdminPanelController ).getRequestBuilder( eq( RequestBuilder.PUT ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "Content-Type", "application/json" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( userRolesAdminPanelController )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyString() );

    RequestException mockRequestException = mock( RequestException.class );
    doThrow( mockRequestException ).when( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    userRolesAdminPanelController.saveUser( user, password );

    verify( mockMessageDialogBox ).center();
  }

  @Test public void testSaveRole() throws RequestException {
    String name = "name";

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( userRolesAdminPanelController )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    userRolesAdminPanelController.saveRole( name );

    verify( userRolesAdminPanelController ).getRequestBuilder( eq( RequestBuilder.PUT ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( userRolesAdminPanelController )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyString() );

    RequestException mockRequestException = mock( RequestException.class );
    doThrow( mockRequestException ).when( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    userRolesAdminPanelController.saveRole( name );

    verify( mockMessageDialogBox ).center();
  }

  @Test public void testDeleteRoles() throws RequestException {
    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( userRolesAdminPanelController )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    userRolesAdminPanelController.deleteUsers();

    verify( userRolesAdminPanelController ).getRequestBuilder( eq( RequestBuilder.PUT ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( userRolesAdminPanelController )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyString() );

    RequestException mockRequestException = mock( RequestException.class );
    doThrow( mockRequestException ).when( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    userRolesAdminPanelController.deleteUsers();

    verify( mockMessageDialogBox ).center();
  }

  @Test public void testUpdatePassword() throws RequestException {
    String newPassword = "newPassword";

    String username = "username";
    doReturn( username ).when( userRolesAdminPanelController ).getSelectedUserValue();

    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( userRolesAdminPanelController )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    userRolesAdminPanelController.updatePassword( newPassword );

    verify( userRolesAdminPanelController ).getRequestBuilder( eq( RequestBuilder.PUT ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "Content-Type", "application/json" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    // TEST2
    MessageDialogBox mockMessageDialogBox = mock( MessageDialogBox.class );
    doReturn( mockMessageDialogBox ).when( userRolesAdminPanelController )
        .getMessageDialogBox( anyString(), anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyString() );

    RequestException mockRequestException = mock( RequestException.class );
    doThrow( mockRequestException ).when( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );

    userRolesAdminPanelController.updatePassword( newPassword );

    verify( mockMessageDialogBox ).center();
  }

  @Test public void testActivate() throws RequestException {
    RequestBuilder mockRequestBuilder = mock( RequestBuilder.class );
    doReturn( mockRequestBuilder ).when( userRolesAdminPanelController )
        .getRequestBuilder( any( RequestBuilder.Method.class ), anyString() );

    // TEST1
    userRolesAdminPanelController.activate();

    verify( userRolesAdminPanelController ).getRequestBuilder( eq( RequestBuilder.GET ), anyString() );
    verify( mockRequestBuilder ).setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
    verify( mockRequestBuilder ).setHeader( "accept", "application/json" );
    verify( mockRequestBuilder ).sendRequest( anyString(), any( RequestCallback.class ) );
  }

  @Test public void testGetId() {
    assertEquals( "userRolesAdminPanel", userRolesAdminPanelController.getId() );
  }

  @Test public void testPassivate() {
    AsyncCallback mockAsyncCallback = mock( AsyncCallback.class );

    userRolesAdminPanelController.passivate( mockAsyncCallback );

    verify( mockAsyncCallback ).onSuccess( true );
  }

}
