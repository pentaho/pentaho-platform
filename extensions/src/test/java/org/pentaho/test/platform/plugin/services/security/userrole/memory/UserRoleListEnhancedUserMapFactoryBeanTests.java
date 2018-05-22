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

package org.pentaho.test.platform.plugin.services.security.userrole.memory;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMapFactoryBean;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

public class UserRoleListEnhancedUserMapFactoryBeanTests extends AbstractUserMapFactoryBeanTestBase {

  @Before
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Test
  public void testGetObject() throws Exception {
    UserRoleListEnhancedUserMapFactoryBean bean = new UserRoleListEnhancedUserMapFactoryBean();
    Properties props = new Properties();
    props.load( new ByteArrayInputStream( userMapText.getBytes() ) );
    bean.setUserMap( props );
    UserRoleListEnhancedUserMap map = (UserRoleListEnhancedUserMap) bean.getObject();
    /* assertNotNull( map.getUser( "admin" ) ); //$NON-NLS-1$ TODO */
    // Next assert is unnecessary by interface contract
    // assertTrue(map.getUser("admin") instanceof UserDetails); //$NON-NLS-1$
    assertTrue( isRolePresent( map.getAllAuthorities(), "ROLE_REPORT_AUTHOR" ) ); //$NON-NLS-1$
    assertTrue( isRolePresent( map.getAllAuthorities(), "ROLE_AUTHENTICATED" ) ); //$NON-NLS-1$
    assertTrue( isRolePresent( map.getAllAuthorities(), "ROLE_ADMINISTRATOR" ) ); //$NON-NLS-1$
    assertTrue( isRolePresent( map.getAllAuthorities(), "ROLE_BUSINESS_ANALYST" ) ); //$NON-NLS-1$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_REPORT_AUTHOR" ), "tiffany" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_AUTHENTICATED" ), "admin" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_AUTHENTICATED" ), "pat" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_AUTHENTICATED" ), "suzy" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_AUTHENTICATED" ), "tiffany" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_ADMINISTRATOR" ), "admin" ) ); //$NON-NLS-1$//$NON-NLS-2$
    assertTrue( isUserPresent( map.getUserNamesInRole( "ROLE_BUSINESS_ANALYST" ), "pat" ) ); //$NON-NLS-1$//$NON-NLS-2$
    // System.out.println(StringUtils.arrayToCommaDelimitedString(map.getAllUsers()));
    assertTrue( isUserPresent( map.getAllUsers(), "pat" ) ); //$NON-NLS-1$
    assertTrue( isUserPresent( map.getAllUsers(), "suzy" ) ); //$NON-NLS-1$
    assertTrue( isUserPresent( map.getAllUsers(), "admin" ) ); //$NON-NLS-1$
    assertTrue( isUserPresent( map.getAllUsers(), "tiffany" ) ); //$NON-NLS-1$
    // System.out.println(map.getUser("admin"));
  }

  protected boolean isRolePresent( final String[] roles, final String role ) {
    Assert.hasLength( role );
    for ( int i = 0; i < roles.length; i++ ) {
      if ( null != roles[i] && roles[i].equals( role ) ) {
        return true;
      }
    }
    return false;
  }

  protected boolean isUserPresent( final String[] users, final String user ) {
    Assert.hasLength( user );
    for ( int i = 0; i < users.length; i++ ) {
      if ( user.equals( users[i] ) ) {
        return true;
      }
    }
    return false;
  }

}
