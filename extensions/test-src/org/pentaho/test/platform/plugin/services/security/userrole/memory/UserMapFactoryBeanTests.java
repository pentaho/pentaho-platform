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

package org.pentaho.test.platform.plugin.services.security.userrole.memory;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserMapFactoryBean;
import org.springframework.security.userdetails.memory.UserMap;

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
    UserMap map = (UserMap) bean.getObject();
    assertNotNull( map.getUser( "admin" ) ); //$NON-NLS-1$
    assertNotNull( map.getUser( "tiffany" ) ); //$NON-NLS-1$
    // Next assert is unnecessary as by contract, the getUser returns a UserDetails
    // assertTrue(map.getUser("admin") instanceof UserDetails); //$NON-NLS-1$
    // System.out.println(map.getUser("admin"));
  }
}
