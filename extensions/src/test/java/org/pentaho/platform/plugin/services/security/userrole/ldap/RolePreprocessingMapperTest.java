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
