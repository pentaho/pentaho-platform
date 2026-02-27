/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


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
