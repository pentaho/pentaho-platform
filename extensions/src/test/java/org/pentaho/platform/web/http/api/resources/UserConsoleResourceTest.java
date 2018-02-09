/*!
 *
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
 *
 * Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.web.http.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.UserConsoleService;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UserConsoleResourceTest {

  UserConsoleResource userConsoleResource;

  @Before
  public void setup() {

    // Bypass constructor by using mock instead of spy
    userConsoleResource = mock( UserConsoleResource.class );
    doCallRealMethod().when( userConsoleResource ).isAdministrator();
    doCallRealMethod().when( userConsoleResource ).isAuthenticated();

    userConsoleResource.userConsoleService = mock( UserConsoleService.class );
  }

  @After
  public void teardown() {
    userConsoleResource = null;
  }

  @Test
  public void testIsAdministrator() {
    boolean isAdministrator = true;
    doReturn( isAdministrator ).when( userConsoleResource.userConsoleService ).isAdministrator();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userConsoleResource ).buildOkResponse( String.valueOf( isAdministrator ) );

    Response testResponse = userConsoleResource.isAdministrator();
    assertEquals( mockResponse, testResponse );

    verify( userConsoleResource.userConsoleService, times( 1 ) ).isAdministrator();
    verify( userConsoleResource, times( 1 ) ).buildOkResponse( String.valueOf( isAdministrator ) );
  }

  @Test
  public void testIsAuthenticated() {
    boolean isAuthenticated = true;
    doReturn( isAuthenticated ).when( userConsoleResource.userConsoleService ).isAuthenticated();

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( userConsoleResource ).buildOkResponse( String.valueOf( isAuthenticated ) );

    Response testResponse = userConsoleResource.isAuthenticated();
    assertEquals( mockResponse, testResponse );

    verify( userConsoleResource.userConsoleService, times( 1 ) ).isAuthenticated();
    verify( userConsoleResource, times( 1 ) ).buildOkResponse( String.valueOf( isAuthenticated ) );
  }
}
