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



package org.pentaho.platform.engine.security;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

public class LoginAttemptServiceTest {

  private ILoginAttemptService loginAttemptService;
  private String key;

  @Before
  public void setUp() throws Exception {
    int maxAttempt = 10;
    int cacheMinutes = 24 * 60;
    loginAttemptService = new LoginAttemptService( maxAttempt, cacheMinutes );
    key = UUID.randomUUID().toString();
  }

  @Test
  public void testLoginAttemptServiceConstructorParameters() {
    int maxAttempt = 3;
    int cacheMinutes = 60;
    ILoginAttemptService loginAttemptService = new LoginAttemptService( maxAttempt, cacheMinutes );
    assertEquals( maxAttempt, ((LoginAttemptService)loginAttemptService).maxAttempt );
  }

  @Test
  public void testLoginFailed() throws ExecutionException {
    Integer attempts = 30;
    testLoginFailed( attempts );
  }

  private void testLoginFailed( Integer attempts ) throws ExecutionException {

    for ( int i = 1; i <= attempts; i++ ) {
      loginAttemptService.loginFailed( key );
      assertEquals( Integer.valueOf( i ), ( ( (LoginAttemptService) loginAttemptService ).attemptsCache ).get( key ) );
    }
  }

  @Test
  public void testLoginSucceeded() throws ExecutionException {
    testLoginFailed();
    loginAttemptService.loginSucceeded( key );
    assertEquals( Integer.valueOf( 0 ), ( ( (LoginAttemptService) loginAttemptService ).attemptsCache ).get( key ) );
  }

  @Test
  public void testIsBlocked() throws ExecutionException {
    assertEquals( false, loginAttemptService.isBlocked( key ) );
    testLoginFailed( 30 );
    assertEquals( true, loginAttemptService.isBlocked( key ) );
  }
}
