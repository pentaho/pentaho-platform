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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LoginAttemptServiceTest {

  private ILoginAttemptService loginAttemptService;
  private String key;

  /**
   * The following values are the defaults to be used under this unit test class.
   */
  private static final int MAX_ATTEMPTS = 10;
  private static final int CACHE_MINUTES = 5;
  /**
   * Number of attempts that should trigger a block.
   * This value is greater than {@link #MAX_ATTEMPTS} so that the blocking behaviour is exercised.
   */
  private static final int ATTEMPTS_TO_LOCK = MAX_ATTEMPTS + 5;

  @Before
  public void setUp() throws Exception {
    loginAttemptService = new LoginAttemptService( MAX_ATTEMPTS, CACHE_MINUTES );
    key = UUID.randomUUID().toString();
  }

  @Test
  public void testLoginAttemptServiceConstructorParameters() {
    // Test something different from the test class default values, to ensure parameters are being set correctly
    int maxAttempts = MAX_ATTEMPTS + 1;
    LoginAttemptService las = new LoginAttemptService( maxAttempts, CACHE_MINUTES );
    assertEquals( maxAttempts, las.getMaxAttempt() );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testCacheMinutesShouldNotBeNegative() {
    LoginAttemptService las = new LoginAttemptService( MAX_ATTEMPTS, -1 );
    fail( "An IllegalArgumentException should have been thrown for negative cache minutes" );
  }

  @Test( expected = IllegalArgumentException.class )
  public void testMaxAttemptsShouldNotBeNegative() {
    LoginAttemptService las = new LoginAttemptService( -1, CACHE_MINUTES );
    fail( "An IllegalArgumentException should have been thrown for negative maximum attempts" );
  }

  @Test
  public void testLoginFailedAttempts() throws ExecutionException {
    // Fail login once
    simulateFailedLoginAttempts( key, 1 );
    // The fail count should be 1
    assertEquals( Integer.valueOf( 1 ), loginAttemptService.getAllAttempts().get( key ) );

    // Fail two more times
    simulateFailedLoginAttempts( key, 2 );
    // The fail count should be 3 (1+2)
    assertEquals( Integer.valueOf( 3 ), loginAttemptService.getAllAttempts().get( key ) );

    // Fail three more times
    simulateFailedLoginAttempts( key, 3 );
    // The fail count should be 6 (3+3)
    assertEquals( Integer.valueOf( 6 ), loginAttemptService.getAllAttempts().get( key ) );
  }

  /**
   * Helper method to simulate failed login attempts with a specified number of attempts.
   *
   * @param key      the key to use for the login attempts
   * @param attempts number of failed login attempts to simulate
   */
  private void simulateFailedLoginAttempts( String key, Integer attempts ) {
    for ( int i = 1; i <= attempts; i++ ) {
      loginAttemptService.loginFailed( key );
    }
  }

  @Test
  public void testIsBlocked() throws ExecutionException {
    // Initially, the key should not be blocked
    assertFalse( loginAttemptService.isBlocked( key ) );

    // Block the key with failed attempts
    simulateFailedLoginAttempts( key, ATTEMPTS_TO_LOCK );

    // The key should, now, be blocked
    assertTrue( loginAttemptService.isBlocked( key ) );
  }

  @Test
  public void testLoginSucceeded() throws ExecutionException {
    // Block the key with failed attempts
    simulateFailedLoginAttempts( key, ATTEMPTS_TO_LOCK );
    // The key should, now, be blocked
    assertTrue( loginAttemptService.isBlocked( key ) );

    // Successful login should reset the attempts count for the key
    loginAttemptService.loginSucceeded( key );
    assertFalse( loginAttemptService.isBlocked( key ) );
  }

  @Test
  public void testRemoveFromCache() throws ExecutionException {
    // Initially, the key should not be blocked
    assertFalse( loginAttemptService.isBlocked( key ) );

    // Block the key with failed attempts
    simulateFailedLoginAttempts( key, ATTEMPTS_TO_LOCK );

    // Have some other keys in the cache to ensure only the specified key is removed
    int numberOfOtherKeys = 6;
    for ( int i = 1; i <= numberOfOtherKeys; i++ ) {
      simulateFailedLoginAttempts( key + i, i );
    }

    // The key should, now, be blocked
    assertTrue( loginAttemptService.isBlocked( key ) );


    // Remove the key from the cache
    loginAttemptService.removeFromCache( key );


    // The key should not exist in the cache
    assertNull( loginAttemptService.getAllAttempts().get( key ) );
    // The key should not be blocked
    assertFalse( loginAttemptService.isBlocked( key ) );
    // The key should, now, exist but been zeroed out in the cache
    assertEquals( Integer.valueOf( 0 ), loginAttemptService.getAllAttempts().get( key ) );

    // All other existing keys should maintain their former values
    for ( int i = 1; i <= numberOfOtherKeys; i++ ) {
      assertEquals( Integer.valueOf( i ), loginAttemptService.getAllAttempts().get( key + i ) );
    }
  }

  @Test
  public void testClearCache() throws ExecutionException {
    // Initially, the key should not be blocked
    assertFalse( loginAttemptService.isBlocked( key ) );

    // Block the key with failed attempts
    simulateFailedLoginAttempts( key, ATTEMPTS_TO_LOCK );

    // Have some other keys in the cache to ensure only the specified key is removed
    int numberOfOtherKeys = 6;
    for ( int i = 1; i <= numberOfOtherKeys; i++ ) {
      simulateFailedLoginAttempts( key + i, i );
    }

    // The key should, now, be blocked
    assertTrue( loginAttemptService.isBlocked( key ) );


    // Clear the cache
    loginAttemptService.clearCache();


    // The key should not exist in the cache
    assertNull( loginAttemptService.getAllAttempts().get( key ) );
    // The key should not be blocked
    assertFalse( loginAttemptService.isBlocked( key ) );
    // The key should, now, exist but been zeroed out in the cache
    assertEquals( Integer.valueOf( 0 ), loginAttemptService.getAllAttempts().get( key ) );

    // All other existing keys should have been reset also
    for ( int i = 1; i <= numberOfOtherKeys; i++ ) {
      // The key should not exist in the cache
      assertNull( loginAttemptService.getAllAttempts().get( key + i ) );
      // The key should not be blocked
      assertFalse( loginAttemptService.isBlocked( key + i ) );
      // The key should, now, exist but been zeroed out in the cache
      assertEquals( Integer.valueOf( 0 ), loginAttemptService.getAllAttempts().get( key + i ) );
    }
  }
}
