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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.api.engine.security.userroledao;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by bgroves on 11/6/15.
 */
public class UserRoleInfoTest {
  private static final String USER_ONE = "Admin";
  private static final String USER_TWO = "Susan";
  private static final String ROLE_ONE = "Scrum Master";
  private static final String ROLE_TWO = "Developer";

  private static final List<String> USERS = Arrays.asList( USER_ONE, USER_TWO );
  private static final List<String> ROLES = Arrays.asList( ROLE_ONE, ROLE_TWO );

  @Test
  public void testGettersSetters() {
    UserRoleInfo info = new UserRoleInfo();
    assertTrue( info.getRoles().isEmpty() );
    assertTrue( info.getUsers().isEmpty() );

    info.setUsers( USERS );
    info.setRoles( ROLES );
    assertEquals( USERS, info.getUsers() );
    assertEquals( ROLES, info.getRoles() );
  }
}
