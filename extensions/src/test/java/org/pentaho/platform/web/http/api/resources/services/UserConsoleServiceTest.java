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

package org.pentaho.platform.web.http.api.resources.services;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IPentahoSession;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.platform.web.http.api.resources.services.UserConsoleService.DEFAULT_SCHEDULER_FOLDER;
import static org.pentaho.platform.web.http.api.resources.services.UserConsoleService.DEFAULT_SHOW_OVERRIDE_DIALOG;

public class UserConsoleServiceTest {
  private static final String INVALID_KEY = "invalid_key";
  private IPentahoSession mockPentahoSession;
  private UserConsoleService userConsoleService;

  @Before
  public void setUp() {
    mockPentahoSession = mock( IPentahoSession.class );

    userConsoleService = spy( TestableUserConsoleService.class );
    doReturn( mockPentahoSession ).when( userConsoleService ).getPentahoSession();
  }

  @Test
  public void testSetSessionVariableWithValidKeySetsAndReturnsValue() {
    String value = "/home/user";
    when( mockPentahoSession.getAttribute( DEFAULT_SCHEDULER_FOLDER ) ).thenReturn( value );

    Object result = userConsoleService.setSessionVariable( DEFAULT_SCHEDULER_FOLDER, value );

    assertEquals( value, result );
    verify( mockPentahoSession ).setAttribute( DEFAULT_SCHEDULER_FOLDER, value );
    verify( mockPentahoSession ).getAttribute( DEFAULT_SCHEDULER_FOLDER );
  }

  @Test( expected = ForbiddenSessionVariableException.class )
  public void testSetSessionVariableWithInvalidKeyThrowsException() {
    String value = "some_value";

    userConsoleService.setSessionVariable( INVALID_KEY, value );
  }

  @Test
  public void testGetSessionVariableWithValidKeyReturnsValue() {
    String expectedValue = "true";
    when( mockPentahoSession.getAttribute( DEFAULT_SHOW_OVERRIDE_DIALOG ) ).thenReturn( expectedValue );

    Object result = userConsoleService.getSessionVariable( DEFAULT_SHOW_OVERRIDE_DIALOG );

    assertEquals( expectedValue, result );
    verify( mockPentahoSession ).getAttribute( DEFAULT_SHOW_OVERRIDE_DIALOG );
  }

  @Test( expected = ForbiddenSessionVariableException.class )
  public void testGetSessionVariableWithInvalidKeyThrowsException() {
    userConsoleService.getSessionVariable( INVALID_KEY );
  }

  @Test
  public void testClearSessionVariableWithValidKeyRemovesAndReturnsValue() {
    String previousValue = "/previous/path";
    when( mockPentahoSession.removeAttribute( DEFAULT_SCHEDULER_FOLDER ) ).thenReturn( previousValue );

    Object result = userConsoleService.clearSessionVariable( DEFAULT_SCHEDULER_FOLDER );

    assertEquals( previousValue, result );
    verify( mockPentahoSession ).removeAttribute( DEFAULT_SCHEDULER_FOLDER );
  }

  @Test( expected = ForbiddenSessionVariableException.class )
  public void testClearSessionVariableWithInvalidKeyThrowsException() {
    userConsoleService.clearSessionVariable( INVALID_KEY );
  }

  @Test
  public void testSetSessionVariableWithNullValueHandlesGracefully() {
    String value = null;
    when( mockPentahoSession.getAttribute( DEFAULT_SCHEDULER_FOLDER ) ).thenReturn( value );

    Object result = userConsoleService.setSessionVariable( DEFAULT_SCHEDULER_FOLDER, value );

    assertNull( result );
    verify( mockPentahoSession ).setAttribute( DEFAULT_SCHEDULER_FOLDER, value );
    verify( mockPentahoSession ).getAttribute( DEFAULT_SCHEDULER_FOLDER );
  }

  @Test
  public void testGetSessionVariableWhenKeyNotFoundReturnsNull() {
    when( mockPentahoSession.getAttribute( DEFAULT_SHOW_OVERRIDE_DIALOG ) ).thenReturn( null );

    Object result = userConsoleService.getSessionVariable( DEFAULT_SHOW_OVERRIDE_DIALOG );

    assertNull( result );
    verify( mockPentahoSession ).getAttribute( DEFAULT_SHOW_OVERRIDE_DIALOG );
  }

  @Test
  public void testClearSessionVariableWhenKeyNotFoundReturnsNull() {
    when( mockPentahoSession.removeAttribute( DEFAULT_SCHEDULER_FOLDER ) ).thenReturn( null );

    Object result = userConsoleService.clearSessionVariable( DEFAULT_SCHEDULER_FOLDER );

    assertNull( result );
    verify( mockPentahoSession ).removeAttribute( DEFAULT_SCHEDULER_FOLDER );
  }

  private static class TestableUserConsoleService extends UserConsoleService {
    @Override
    protected List<String> loadWhiteList( String property ) {
      return List.of( DEFAULT_SCHEDULER_FOLDER, DEFAULT_SHOW_OVERRIDE_DIALOG );
    }
  }
}
