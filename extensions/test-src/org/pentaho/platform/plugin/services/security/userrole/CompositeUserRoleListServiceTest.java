/*
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
 * Copyright 2014 Pentaho Corporation. All rights reserved.
 */

package org.pentaho.platform.plugin.services.security.userrole;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by nbaker on 5/13/14.
 */
public class CompositeUserRoleListServiceTest {
  CompositeUserRoleListService compositeService;
  private IUserRoleListService service1;
  private IUserRoleListService service2;
  private ITenant mockTenant;

  @Before
  public void setup() {

    service1 = mock( IUserRoleListService.class );
    service2 = mock( IUserRoleListService.class );
    mockTenant = mock( ITenant.class );
    String joe = "joe";
    String ceo = "ceo";
    when( service1.getAllRoles() ).thenReturn( Arrays.asList( "ceo", "admin" ) );
    when( service1.getAllRoles( mockTenant ) ).thenReturn( Arrays.asList( "ceo", "admin" ) );
    when( service1.getAllUsers() ).thenReturn( Arrays.asList( "joe", "suzy" ) );
    when( service1.getAllUsers( mockTenant ) ).thenReturn( Arrays.asList( "joe", "suzy" ) );
    when( service1.getRolesForUser( mockTenant, joe ) ).thenReturn( Arrays.asList( "admin" ) );
    when( service1.getSystemRoles() ).thenReturn( Arrays.asList( "authenticated", "anonymous" ) );
    when( service1.getUsersInRole( mockTenant, ceo ) ).thenReturn( Arrays.asList( "suzy" ) );

    when( service2.getAllRoles() ).thenReturn( Arrays.asList( "extraRole1", "admin" ) );
    when( service2.getAllRoles( mockTenant ) ).thenReturn( Arrays.asList( "extraRole1", "admin" ) );
    when( service2.getAllUsers() ).thenReturn( Arrays.asList( "user1", "suzy" ) );
    when( service2.getAllUsers( mockTenant ) ).thenReturn( Arrays.asList( "user1", "suzy" ) );
    when( service2.getRolesForUser( mockTenant, joe ) ).thenReturn( Arrays.asList( "admin", "rockstar" ) );
    when( service2.getSystemRoles() ).thenReturn( Arrays.asList( "serveradmin", "anonymous" ) );
    when( service2.getUsersInRole( mockTenant, ceo ) ).thenReturn( Arrays.asList( "ted", "suzy" ) );

    compositeService = new CompositeUserRoleListService( Arrays.asList( service1, service2 ) );
  }

  @Test
  public void testSetStrategy() throws Exception {
    compositeService = new CompositeUserRoleListService( Collections.<IUserRoleListService>emptyList() );
    assertEquals( CompositeUserRoleListService.STRATEGY.FIRST_MATCH, compositeService.getActiveStrategy() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    assertEquals( CompositeUserRoleListService.STRATEGY.ADDITIVE, compositeService.getActiveStrategy() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.FIRST_MATCH );
    assertEquals( CompositeUserRoleListService.STRATEGY.FIRST_MATCH, compositeService.getActiveStrategy() );
  }

  @Test
  public void testSetStrategy1() throws Exception {

    compositeService = new CompositeUserRoleListService( Collections.<IUserRoleListService>emptyList() );
    assertEquals( CompositeUserRoleListService.STRATEGY.FIRST_MATCH, compositeService.getActiveStrategy() );
    compositeService.setStrategy( "ADDITIVE" );
    assertEquals( CompositeUserRoleListService.STRATEGY.ADDITIVE, compositeService.getActiveStrategy() );
    compositeService.setStrategy( "FIRST_MATCH" );
    assertEquals( CompositeUserRoleListService.STRATEGY.FIRST_MATCH, compositeService.getActiveStrategy() );
  }

  @Test
  public void testGetAllRoles() throws Exception {

    List<String> allRoles = compositeService.getAllRoles();
    assertEquals( 2, allRoles.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allRoles = compositeService.getAllRoles();
    assertEquals( 3, allRoles.size() );
    assertTrue( unsortedEqualArrays( allRoles.toArray( new String[allRoles.size()] ),
        new String[]{"ceo", "admin", "extraRole1"} ) );
  }

  private static boolean unsortedEqualArrays( String[] arry1, String[] arry2 ) {
    Arrays.sort( arry1 );
    Arrays.sort( arry2 );
    return Arrays.equals( arry1, arry2 );
  }

  @Test
  public void testGetSystemRoles() throws Exception {

    List<String> allRoles = compositeService.getSystemRoles();
    assertEquals( 2, allRoles.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allRoles = compositeService.getSystemRoles();
    assertEquals( 3, allRoles.size() );
    assertTrue( unsortedEqualArrays( allRoles.toArray( new String[allRoles.size()] ),
        new String[]{"authenticated", "anonymous", "serveradmin"} ) );
  }

  @Test
  public void testGetAllRoles1() throws Exception {

    List<String> allRoles = compositeService.getAllRoles( mockTenant );
    assertEquals( 2, allRoles.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allRoles = compositeService.getAllRoles( mockTenant );
    assertEquals( 3, allRoles.size() );
    assertTrue( unsortedEqualArrays( allRoles.toArray( new String[allRoles.size()] ),
        new String[]{"ceo", "admin", "extraRole1"} ) );
  }

  @Test
  public void testGetAllUsers() throws Exception {

    List<String> allUsers = compositeService.getAllUsers();
    assertEquals( 2, allUsers.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allUsers = compositeService.getAllUsers();
    assertEquals( 3, allUsers.size() );
    assertTrue( unsortedEqualArrays( allUsers.toArray( new String[allUsers.size()] ),
        new String[]{"user1", "suzy", "joe"} ) );
  }

  @Test
  public void testGetAllUsers1() throws Exception {

    List<String> allUsers = compositeService.getAllUsers( mockTenant );
    assertEquals( 2, allUsers.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allUsers = compositeService.getAllUsers( mockTenant );
    assertEquals( 3, allUsers.size() );
    assertTrue( unsortedEqualArrays( allUsers.toArray( new String[allUsers.size()] ),
        new String[]{"user1", "suzy", "joe"} ) );

  }

  @Test
  public void testGetUsersInRole() throws Exception {

    List<String> allUsers = compositeService.getUsersInRole( mockTenant, "ceo" );
    assertEquals( 1, allUsers.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allUsers = compositeService.getUsersInRole( mockTenant, "ceo" );
    assertEquals( 2, allUsers.size() );
    assertTrue( unsortedEqualArrays( allUsers.toArray( new String[allUsers.size()] ),
        new String[]{"ted", "suzy"} ) );
  }

  @Test
  public void testGetRolesForUser() throws Exception {

    List<String> allRoles = compositeService.getRolesForUser( mockTenant, "joe" );
    assertEquals( 1, allRoles.size() );
    compositeService.setStrategy( CompositeUserRoleListService.STRATEGY.ADDITIVE );
    allRoles = compositeService.getRolesForUser( mockTenant, "joe" );
    assertEquals( 2, allRoles.size() );
    assertTrue( unsortedEqualArrays( allRoles.toArray( new String[allRoles.size()] ),
        new String[]{"admin", "rockstar"} ) );
  }
}
