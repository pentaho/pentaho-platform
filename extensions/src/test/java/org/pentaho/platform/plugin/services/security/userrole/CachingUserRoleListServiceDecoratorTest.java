package org.pentaho.platform.plugin.services.security.userrole;

import org.junit.Test;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

public class CachingUserRoleListServiceDecoratorTest {

  @Test
  public void testGetAllRoles() throws Exception {
    IUserRoleListService mockService = mock( IUserRoleListService.class );
    when( mockService.getAllRoles() ).thenReturn( Arrays.asList( "foo", "bar" ) );


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
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


    CachingUserRoleListServiceDecorator decorator = new CachingUserRoleListServiceDecorator( mockService );
    List<String> allRoles = decorator.getAllUsers( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    // second call should be from cache
    allRoles = decorator.getAllUsers( tenant );
    assertArrayEquals( "does not match", new String[] { "foo", "bar" }, allRoles.toArray() );

    verify( mockService, times( 1 ) ).getAllUsers( tenant );
  }
}
