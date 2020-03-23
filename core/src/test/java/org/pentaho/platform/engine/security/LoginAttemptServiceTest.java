package org.pentaho.platform.engine.security;

import com.google.common.cache.LoadingCache;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.powermock.reflect.Whitebox.getInternalState;
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
    assertEquals( maxAttempt, (int) getInternalState( loginAttemptService, "maxAttempt" ) );
    assertEquals( cacheMinutes, (int) getInternalState( loginAttemptService, "cacheMinutes" ) );
  }

  @Test
  public void testLoginFailed() throws ExecutionException {
    Integer attempts = 30;
    testLoginFailed( attempts );
  }

  private void testLoginFailed( Integer attempts ) throws ExecutionException {

    for ( int i = 1; i <= attempts; i++ ) {
      loginAttemptService.loginFailed( key );
      assertEquals(
        Integer.valueOf( i ),
        ( (LoadingCache<String, Integer>) getInternalState( loginAttemptService, "attemptsCache" ) ).get( key )
      );
    }
  }



  @Test
  public void testLoginSucceeded() throws ExecutionException {
    testLoginFailed();
    loginAttemptService.loginSucceeded( key );
    assertEquals(
      Integer.valueOf( 0 ),
      ( (LoadingCache<String, Integer>) getInternalState( loginAttemptService, "attemptsCache" ) ).get( key ) );
  }

  @Test
  public void testIsBlocked() throws ExecutionException {
    assertEquals( false, loginAttemptService.isBlocked( key ) );
    testLoginFailed( 30 );
    assertEquals( true, loginAttemptService.isBlocked( key ) );
  }
}
