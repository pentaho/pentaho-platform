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
 * Copyright (c) 2002-2021 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.plugin.services.security.userrole.ldap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 10/30/15.
 */
@RunWith( MockitoJUnitRunner.class )
public class RolePreprocessingMapperTest {

  RolePreprocessingMapper roleMapper;


  @Before
  public void setUp() throws Exception {
    roleMapper = new RolePreprocessingMapper();
  }

  @Test
  public void testCreateAuthority() throws Exception {
    roleMapper.setTokenName( "token" );
    assertEquals( "token", roleMapper.getTokenName() );

    assertNull( roleMapper.createAuthority( null ) );
  }

  @Test
  public void testPreprocessRole_nonString() throws Exception {
    roleMapper = new RolePreprocessingMapper( "test" );
    assertEquals( "test", roleMapper.getTokenName() );
    assertEquals( 3L, roleMapper.preprocessRole( 3L ) );
  }

  @Test
  public void testPreprocessRole() throws Exception {
    roleMapper = new RolePreprocessingMapper( "ou" );
    assertEquals( "users", roleMapper.preprocessRole( "uid=suzy,ou=users,dc=pentaho,dc=org" ) );
  }

  @Test
  public void testPreprocessRole_noRoleFound() throws Exception {
    roleMapper = new RolePreprocessingMapper( "xx" );
    assertNull( roleMapper.preprocessRole( "uid=suzy,ou=users,dc=pentaho,dc=org" ) );
  }

  @Test
  public void testAfterPropertiesSet() throws Exception {
    // no-op, just calling it for code coverage
    roleMapper.afterPropertiesSet();
  }
}
