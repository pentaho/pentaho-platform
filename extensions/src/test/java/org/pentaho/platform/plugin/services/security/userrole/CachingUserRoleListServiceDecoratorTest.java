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

package org.pentaho.platform.plugin.services.security.userrole;

import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.cache.MockPlatformCache;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class CachingUserRoleListServiceDecoratorTest {

  @Test
  public void testGetAllRoles() throws Exception {
    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getAllRoles() ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getAllRoles();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getAllRoles();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getAllRoles();
  }

  @Test
  public void testGetSystemRoles() throws Exception {
    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getSystemRoles() ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getSystemRoles();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getSystemRoles();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getSystemRoles();

  }

  private ITenant tenant = mock( ITenant.class );

  @Test
  public void testGetRolesForUser() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );

    when( mockService.getRolesForUser( tenant, "joe" ) ).thenReturn( Arrays.asList( "foo", "bar" ) );
    when( mockService.getRolesForUser( tenant, "admin" ) ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getRolesForUser( tenant, "joe" );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getRolesForUser( tenant, "joe" );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    allRoles = decorator.getRolesForUser( tenant, "admin" );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getRolesForUser( tenant, "joe" );
    verify( mockService, times( 1 ) ).getRolesForUser( tenant, "admin" );

  }

  @Test
  public void testGetAllUsers() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getAllUsers() ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getAllUsers();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getAllUsers();
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getAllUsers();
  }

  @Test
  public void testGetAllRoles1() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getAllRoles( tenant ) ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getAllRoles( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getAllRoles( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getAllRoles( tenant );
  }

  @Test
  public void testGetUsersInRole() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getUsersInRole( tenant, "ceo" ) ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getUsersInRole( tenant, "ceo" );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getUsersInRole( tenant, "ceo" );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getUsersInRole( tenant, "ceo" );
  }

  @Test
  public void testGetAllUsers1() throws Exception {

    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getAllUsers( tenant ) ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator =
      new CachingUserRoleListServiceDecorator( mockService, new MockPlatformCache() );
    List<String> allRoles = decorator.getAllUsers( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getAllUsers( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getAllUsers( tenant );
  }
}
