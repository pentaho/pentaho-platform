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


package org.pentaho.platform.api.engine.security.userroledao;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
