package org.pentaho.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.security.ILoginAttemptService;
import org.pentaho.platform.engine.security.LoginAttemptService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;


public class PreventBruteForceUsernamePasswordAuthenticationFilterTest {

  private ILoginAttemptService mockLoginAttemptService;
  private HttpServletRequest mockRequest;
  private HttpServletResponse mockResponse;
  private AuthenticationManager mockAuthenticationManager;
  private Authentication mockAuthentication;
  private String ip;

  @Before
  public void setUp() throws Exception {
    mockLoginAttemptService = mock( LoginAttemptService.class );
    mockRequest = mock( HttpServletRequest.class );
    mockResponse = mock( HttpServletResponse.class );
    mockAuthenticationManager = mock( AuthenticationManager.class );
    mockAuthentication = mock( Authentication.class );
    ip = "192.168.0.25";
    when( mockRequest.getHeader( "X-Forwarded-For" ) ).thenReturn( null );
    when( mockRequest.getRemoteAddr() ).thenReturn( ip );
    when( mockRequest.getMethod() ).thenReturn( "POST" );
  }


  @Test
  public void testAttemptAuthenticationWithoutBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
    spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( false );
    when( mockAuthenticationManager.authenticate( any() ) ).thenReturn( mockAuthentication );

    setInternalState( authenticationFilter, "authenticationManager", mockAuthenticationManager );

    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
    verify( mockLoginAttemptService, times( 1 ) ).isBlocked( ip );

  }

  @Test( expected = PreventBruteForceException.class )
  public void testAttemptAuthenticationWithBlock() {
    PreventBruteForceUsernamePasswordAuthenticationFilter authenticationFilter =
      spy( new PreventBruteForceUsernamePasswordAuthenticationFilter( mockLoginAttemptService ) );

    when( mockLoginAttemptService.isBlocked( ip ) ).thenReturn( true );

    authenticationFilter.attemptAuthentication( mockRequest, mockResponse );
    verify( mockLoginAttemptService.isBlocked( ip ), times( 1 ) );

  }
}
