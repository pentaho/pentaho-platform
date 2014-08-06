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

package org.pentaho.platform.web.http.api.resources.services;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UserRoleListServiceTest {

  private UserRoleListService userRoleListService;

  @Before
  public void setUp() {
    userRoleListService = spy( new UserRoleListService() );
  }

  @After
  public void cleanup() {
    userRoleListService = null;
  }

  @Test
  public void testDoGetRolesForUser() throws Exception {
    doReturn( true ).when( userRoleListService ).canAdminister();
    doReturn( "admin, guest" ).when( userRoleListService ).getRolesForUser( "Administrator" );
    String roles = userRoleListService.doGetRolesForUser( "Administrator" );
    assertTrue( roles.length() > 0 );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetRolesForUser( "unauthorized" );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }

  @Test
  public void testDoGetUsersInRole() throws Exception {
    doReturn( true ).when( userRoleListService ).canAdminister();
    doReturn( "Administrator, Guest" ).when( userRoleListService ).getUsersInRole( "admin" );
    String users = userRoleListService.doGetUsersInRole( "admin" );
    assertTrue( users.length() > 0 );

    try {
      doReturn( false ).when( userRoleListService ).canAdminister();
      userRoleListService.doGetUsersInRole( "unauthorized" );
    } catch ( Exception e ) {
      assertTrue( e instanceof UserRoleListService.UnauthorizedException );
    }
  }
}
