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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.engine.security.DefaultRoleComparator;
import org.pentaho.platform.engine.security.DefaultUsernameComparator;
import org.pentaho.platform.plugin.services.security.userrole.memory.InMemoryUserRoleListService;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMap;
import org.pentaho.platform.plugin.services.security.userrole.memory.UserRoleListEnhancedUserMapEditor;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.memory.InMemoryDaoImpl;
import org.springframework.security.userdetails.memory.UserMap;
import org.springframework.security.userdetails.memory.UserMapEditor;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings( "nls" )
public class InMemoryUserRoleListServiceTests {

  private static final Log logger = LogFactory.getLog( InMemoryUserRoleListServiceTests.class );

  InMemoryUserRoleListService dao;

  public InMemoryUserRoleListServiceTests() {
    super();
  }

  @Before
  public void setUp() throws Exception {
    dao = new InMemoryUserRoleListService();
    dao.setUserRoleListEnhancedUserMap( makeUserRoleListEnhancedUserMap() );
    dao.setAllRoles( makeAllAuthorities() );
    InMemoryDaoImpl wrapped = new InMemoryDaoImpl();
    wrapped.setUserMap( makeUserMap() );
    wrapped.afterPropertiesSet();
    dao.setUserDetailsService( wrapped );
    dao.afterPropertiesSet();
  }

  protected List<String> makeAllAuthorities() {
    return Arrays.asList( new String[] { "ROLE_ONE", "ROLE_TWO", "ROLE_THREE" } );
  }

  @Test
  public void testGetAllUserNames() throws Exception {
    List<String> allUserNames = dao.getAllUsers();
    assertTrue( "User list should not be empty", allUserNames.size() > 0 ); //$NON-NLS-1$
    for ( String username : allUserNames ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "testGetAllUserNames(): User name: " + username ); //$NON-NLS-1$
      }
      assertTrue( "User name must be marissa or scott", ( username.equals( "marissa" ) || username.equals( "scott" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  @Test
  public void testGetAllUserNamesSorted() throws Exception {
    dao.setUsernameComparator( new DefaultUsernameComparator() );
    List<String> usernames = dao.getAllUsers();
    if ( logger.isDebugEnabled() ) {
      logger.debug( "testGetAllUserNamesSorted(): Usernames: " + usernames ); //$NON-NLS-1$
    }
    assertTrue( usernames.indexOf( "marissa" ) < usernames.indexOf( "scott" ) );
  }

  @Test
  public void testGetAllAuthorities() throws Exception {
    List<String> allAuthorities = dao.getAllRoles();
    assertTrue( "Authority list should contain three roles", allAuthorities.size() == 3 ); //$NON-NLS-1$
    for ( String role : allAuthorities ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "testGetAllAuthorities(): Authority: " + role ); //$NON-NLS-1$
      }
      assertTrue( "Authority name must be ROLE_ONE, ROLE_TWO or ROLE_THREE", ( //$NON-NLS-1$
          role.equals( "ROLE_ONE" ) //$NON-NLS-1$
              || role.equals( "ROLE_TWO" ) //$NON-NLS-1$
          || role.equals( "ROLE_THREE" ) //$NON-NLS-1$
          ) );
    }
  }

  @Test
  public void testGetAllAuthoritiesSorted() throws Exception {
    dao.setRoleComparator( new DefaultRoleComparator() );
    List<String> authorities = dao.getAllRoles();
    if ( logger.isDebugEnabled() ) {
      logger.debug( "testGetAllAuthoritiesSorted(): Authorities: " + authorities ); //$NON-NLS-1$
    }
    assertTrue( authorities.indexOf( new GrantedAuthorityImpl( "ROLE_THREE" ) ) < authorities
        .indexOf( new GrantedAuthorityImpl( "ROLE_TWO" ) ) );
  }

  @Test
  public void testGetAllUserNamesInRole() throws Exception {
    List<String> allUserNames = dao.getUsersInRole( null, "ROLE_ONE" ); //$NON-NLS-1$
    assertTrue( "Two users should be in the role ROLE_ONE", allUserNames.size() == 2 ); //$NON-NLS-1$
    for ( String username : allUserNames ) {
      if ( logger.isDebugEnabled() ) {
        logger.debug( "testGetAllUserNamesInRole(): User name: " + username ); //$NON-NLS-1$
      }
      assertTrue( "User name must be marissa or scott", ( username.equals( "marissa" ) || username.equals( "scott" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
  }

  @Test
  public void testGetAllUserNamesInRoleSorted() throws Exception {
    dao.setUsernameComparator( new DefaultUsernameComparator() );
    List<String> usernames = dao.getUsersInRole( null, "ROLE_ONE" );
    if ( logger.isDebugEnabled() ) {
      logger.debug( "testGetAllUserNamesInRoleSorted(): Usernames: " + usernames ); //$NON-NLS-1$
    }
    assertTrue( usernames.indexOf( "marissa" ) < usernames.indexOf( "scott" ) );
  }

  @Test
  public void testGetRolesForUser() throws Exception {
    List<String> userAuths = dao.getRolesForUser( null, "marissa" ); //$NON-NLS-1$
    if ( logger.isDebugEnabled() ) {
      logger.debug( "testGetRolesForUser(): Roles: " + userAuths ); //$NON-NLS-1$
    }
    assertNotNull( userAuths );
    assertTrue( userAuths.size() == 2 );
    assertEquals( userAuths.get( 0 ), "ROLE_ONE" ); //$NON-NLS-1$
    assertEquals( userAuths.get( 1 ), "ROLE_TWO" ); //$NON-NLS-1$
  }

  @Test
  public void testGetRolesForUserSorted() throws Exception {
    dao.setRoleComparator( new DefaultRoleComparator() );
    List<String> authorities = dao.getRolesForUser( null, "scott" ); //$NON-NLS-1$
    if ( logger.isDebugEnabled() ) {
      logger.debug( "testGetRolesForUser(): Roles: " + authorities ); //$NON-NLS-1$
    }

    assertTrue( authorities.indexOf( new GrantedAuthorityImpl( "ROLE_ONE" ) ) < authorities
        .indexOf( new GrantedAuthorityImpl( "ROLE_THREE" ) ) );
  }

  private UserRoleListEnhancedUserMap makeUserRoleListEnhancedUserMap() {
    UserRoleListEnhancedUserMapEditor editor = new UserRoleListEnhancedUserMapEditor();
    editor.setAsText( "marissa=koala,ROLE_ONE,ROLE_TWO,enabled\r\nscott=wombat,ROLE_ONE,ROLE_THREE,enabled" ); //$NON-NLS-1$
    return (UserRoleListEnhancedUserMap) editor.getValue();
  }

  private UserMap makeUserMap() {
    UserMapEditor editor = new UserMapEditor();
    editor.setAsText( "scott=wombat,ROLE_THREE,ROLE_ONE,enabled\r\nmarissa=koala,ROLE_ONE,ROLE_TWO,enabled" ); //$NON-NLS-1$
    return (UserMap) editor.getValue();
  }

}
