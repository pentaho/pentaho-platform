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

package org.pentaho.test.platform.security.userrole.ws;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.engine.security.userroledao.UserRoleInfo;
import org.pentaho.platform.security.userrole.ws.DefaultUserRoleListWebService;
import org.pentaho.platform.security.userrole.ws.IUserRoleListWebService;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.security.MockUserRoleListService;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings( "nls" )
public class UserDetailsRoleListWebServiceTest {
  private MicroPlatform microPlatform;

  @Before
  public void init0() {
    microPlatform = new MicroPlatform();
    microPlatform.define( IUserRoleListService.class, MockUserRoleListService.class );
  }

  public IUserRoleListWebService getUserRoleListWebService() {
    return new DefaultUserRoleListWebService();
  }

  @Test
  public void testGetAllRoles() throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {
      List<String> allRoles = service.getAllRoles();
      assertNotNull( allRoles );
      assertEquals( allRoles.size(), 7 ); // Should have exactly 7 roles
      assertEquals( allRoles.get( 0 ), "dev" ); //$NON-NLS-1$
      assertEquals( allRoles.get( 6 ), "is" ); //$NON-NLS-1$

    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testGetAllUsers() throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {
      List<String> allUsers = service.getAllUsers();
      assertNotNull( allUsers );
      assertEquals( allUsers.size(), 4 );
      assertEquals( allUsers.get( 0 ), "pat" ); //$NON-NLS-1$
      assertEquals( allUsers.get( 3 ), "suzy" ); //$NON-NLS-1$

    } catch ( Exception e ) {
      Assert.fail();
    }
  }

  @Test
  public void testGetUserRoleInfo() throws Exception {
    IUserRoleListWebService service = getUserRoleListWebService();

    try {

      UserRoleInfo userRoleInfo = service.getUserRoleInfo(); //$NON-NLS-1$
      assertNotNull( userRoleInfo );
      assertEquals( userRoleInfo.getRoles().size(), 7 );
      assertEquals( userRoleInfo.getUsers().size(), 4 );
    } catch ( Exception e ) {
      Assert.fail();
    }
  }
}
