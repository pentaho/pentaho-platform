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


package org.pentaho.test.platform.plugin.services.security.userrole.memory;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserMapFactoryBean;

import static org.junit.Assert.assertNotNull;

public class UserMapFactoryBeanTests extends AbstractUserMapFactoryBeanTestBase {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testGetObject() throws Exception {
    UserMapFactoryBean bean = new UserMapFactoryBean();
    bean.setUserMap( userMapText );
    /* TODO
    UserMap map = (UserMap) bean.getObject();
    assertNotNull( map.getUser( "admin" ) ); //$NON-NLS-1$
    assertNotNull( map.getUser( "tiffany" ) ); //$NON-NLS-1$
    */
    // Next assert is unnecessary as by contract, the getUser returns a UserDetails
    // assertTrue(map.getUser("admin") instanceof UserDetails); //$NON-NLS-1$
    // System.out.println(map.getUser("admin"));
  }
}
