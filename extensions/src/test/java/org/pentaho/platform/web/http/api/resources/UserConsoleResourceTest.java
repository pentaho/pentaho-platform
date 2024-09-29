/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


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
