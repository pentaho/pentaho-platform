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
 * Copyright (c) 2002-2014 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SystemServiceTest {

  private SystemService systemService;

  @Before
  public void setUp() throws Exception {
    systemService = spy( new SystemService() );
  }

  @After
  public void tearDown() throws Exception {
    systemService = null;
  }

  @Test
  public void testGetUsers() throws Exception {
    IUserRoleListService service = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( service );

    doReturn( true ).when( systemService ).canAdminister();
    assertNotNull( systemService.getUsers() );

    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testGetUsersNoPermission() throws Exception {
    IUserRoleListService service = mock( IUserRoleListService.class );
    PentahoSystem.registerObject( service );

    doReturn( false ).when( systemService ).canAdminister();
    try {
      systemService.getUsers();
      fail();
    } catch ( IllegalAccessException e ) {
      //expected exception
    }

    PentahoSystem.clearObjectFactory();
  }
}
