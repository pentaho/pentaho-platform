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
import org.pentaho.platform.api.engine.IPentahoSession;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SessionResourceTest {

  SessionResource sessionResource;

  @Before
  public void setup() {
    sessionResource = spy( new SessionResource() );
  }

  @After
  public void teardown() {
    sessionResource = null;
  }

  @Test
  public void testDoGetCurrentUserDir() {
    IPentahoSession mockPentahoSession = mock( IPentahoSession.class );
    doReturn( mockPentahoSession ).when( sessionResource ).getSession();

    String username = "username";
    doReturn( username ).when( mockPentahoSession ).getName();

    String userHomeFolderPath = "path";
    doReturn( userHomeFolderPath ).when( sessionResource ).getUserHomeFolderPath( username );

    String testString = sessionResource.doGetCurrentUserDir();
    assertEquals( userHomeFolderPath + "/workspace", testString );

    verify( sessionResource, times( 1 ) ).getUserHomeFolderPath( username );
    verify( sessionResource, times( 1 ) ).getSession();
    verify( mockPentahoSession, times( 1 ) ).getName();
  }

  @Test
  public void testDoGetUserDir() {
    String user = "user";

    String userHomeFolderPath = "path";
    doReturn( userHomeFolderPath ).when( sessionResource ).getUserHomeFolderPath( user );

    String testString = sessionResource.doGetUserDir( user );
    assertEquals( userHomeFolderPath + "/workspace", testString );

    verify( sessionResource, times( 1 ) ).getUserHomeFolderPath( user );
  }
}
