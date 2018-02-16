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
