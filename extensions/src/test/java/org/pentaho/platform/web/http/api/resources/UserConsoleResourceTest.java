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


package org.pentaho.platform.web.http.api.resources;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.services.ForbiddenSessionVariableException;
import org.pentaho.platform.web.http.api.resources.services.UserConsoleService;

import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserConsoleResourceTest {

  private UserConsoleService mockUserConsoleService;

  private UserConsoleResource userConsoleResource;

  @Before
  public void setUp() {
    mockUserConsoleService = mock( UserConsoleService.class );
    userConsoleResource = new UserConsoleResource( mockUserConsoleService );
  }

  // region isAdministrator
  @Test
  public void testIsAdministratorWhenUserIsAdminReturnsOkResponseWithTrue() {
    when( mockUserConsoleService.isAdministrator() ).thenReturn( true );

    Response response = userConsoleResource.isAdministrator();

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "true", response.getEntity() );
    verify( mockUserConsoleService ).isAdministrator();
  }

  @Test
  public void testIsAdministratorWhenUserIsNotAdminReturnsOkResponseWithFalse() {
    when( mockUserConsoleService.isAdministrator() ).thenReturn( false );

    Response response = userConsoleResource.isAdministrator();

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "false", response.getEntity() );
    verify( mockUserConsoleService ).isAdministrator();
  }
  // endregion

  // region isAuthenticated
  @Test
  public void testIsAuthenticatedWhenUserIsAuthenticatedReturnsOkResponseWithTrue() {
    when( mockUserConsoleService.isAuthenticated() ).thenReturn( true );

    Response response = userConsoleResource.isAuthenticated();

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "true", response.getEntity() );
    verify( mockUserConsoleService ).isAuthenticated();
  }

  @Test
  public void testIsAuthenticatedWhenUserIsNotAuthenticatedReturnsOkResponseWithFalse() {
    when( mockUserConsoleService.isAuthenticated() ).thenReturn( false );

    Response response = userConsoleResource.isAuthenticated();

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( "false", response.getEntity() );
    verify( mockUserConsoleService ).isAuthenticated();
  }
  // endregion

  // region setSessionVariable
  @Test
  public void testSetSessionVariableWithValidKeyReturnsOkResponseWithValue() {
    String key = "scheduler_folder";
    String value = "/home/user";
    String returnedValue = "/home/user";
    when( mockUserConsoleService.setSessionVariable( key, value ) ).thenReturn( returnedValue );

    Response response = userConsoleResource.setSessionVariable( key, value );

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( returnedValue, response.getEntity() );
    verify( mockUserConsoleService ).setSessionVariable( key, value );
  }

  @Test
  public void testSetSessionVariableWithInvalidKeyReturnsForbiddenResponse() {
    String key = "invalid_key";
    String value = "some_value";
    when( mockUserConsoleService.setSessionVariable( key, value ) )
      .thenThrow( new ForbiddenSessionVariableException( "Setting session variable not allowed: " + key ) );

    Response response = userConsoleResource.setSessionVariable( key, value );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), response.getStatus() );
    verify( mockUserConsoleService ).setSessionVariable( key, value );
  }
  // endregion

  // region getSessionVariable
  @Test
  public void testGetSessionVariableWithValidKeyAndExistingValueReturnsOkResponseWithValue() {

    String key = "showOverrideDialog";
    String value = "true";
    when( mockUserConsoleService.getSessionVariable( key ) ).thenReturn( value );

    Response response = userConsoleResource.getSessionVariable( key );

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( value, response.getEntity() );
    verify( mockUserConsoleService ).getSessionVariable( key );
  }

  @Test
  public void testGetSessionVariableWithValidKeyButNullValueReturnsNoContentResponse() {
    String key = "showOverrideDialog";
    when( mockUserConsoleService.getSessionVariable( key ) ).thenReturn( null );

    Response response = userConsoleResource.getSessionVariable( key );

    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), response.getStatus() );
    verify( mockUserConsoleService ).getSessionVariable( key );
  }

  @Test
  public void testGetSessionVariableWithInvalidKeyReturnsForbiddenResponse() {

    String key = "invalid_key";
    when( mockUserConsoleService.getSessionVariable( key ) )
      .thenThrow( new ForbiddenSessionVariableException( "Getting session variable not allowed: " + key ) );

    Response response = userConsoleResource.getSessionVariable( key );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), response.getStatus() );
    verify( mockUserConsoleService ).getSessionVariable( key );
  }
  // endregion

  // region clearSessionVariable
  @Test
  public void testClearSessionVariableWithValidKeyReturnsOkResponseWithPreviousValue() {
    String key = "scheduler_folder";
    String previousValue = "/previous/path";
    when( mockUserConsoleService.clearSessionVariable( key ) ).thenReturn( previousValue );

    Response response = userConsoleResource.clearSessionVariable( key );

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( previousValue, response.getEntity() );
    verify( mockUserConsoleService ).clearSessionVariable( key );
  }

  @Test
  public void testClearSessionVariableWithInvalidKeyReturnsForbiddenResponse() {
    String key = "invalid_key";
    when( mockUserConsoleService.clearSessionVariable( key ) )
      .thenThrow( new ForbiddenSessionVariableException( "Clearing session variable not allowed: " + key ) );

    Response response = userConsoleResource.clearSessionVariable( key );

    assertEquals( Response.Status.FORBIDDEN.getStatusCode(), response.getStatus() );
    verify( mockUserConsoleService ).clearSessionVariable( key );
  }
  // endregion

  // region registeredPlugins
  @Test
  public void testRegisteredPluginsReturnsOkResponseWithPluginList() {
    List<String> plugins = Arrays.asList( "plugin1", "plugin2", "plugin3" );
    when( mockUserConsoleService.getRegisteredPlugins() ).thenReturn( plugins );

    Response response = userConsoleResource.registeredPlugins();

    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    assertEquals( plugins.toString(), response.getEntity() );
    verify( mockUserConsoleService ).getRegisteredPlugins();
  }
  // endregion
}
