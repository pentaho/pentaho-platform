/*
 * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
 *
 * This software was developed by Pentaho Corporation and is provided under the terms
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use
 * this file except in compliance with the license. If you need a copy of the license,
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
 * the license for the specific language governing your rights and limitations.
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
