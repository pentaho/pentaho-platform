package org.pentaho.platform.web.http.filters;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IServerStatusProvider;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SystemInitStatusFilterTest {
  private SystemInitStatusFilter filter;

  @Before
  public void setUp() throws Exception {
    filter = mock( SystemInitStatusFilter.class );
  }

  @Test
  public void testInit() throws Exception {
    doCallRealMethod().when( filter ).init( any( FilterConfig.class ) );

    final FilterConfig filterConfig = mock( FilterConfig.class );

    filter.init( filterConfig );
    verify( filter ).setRedirectPath( "/" + SystemInitStatusFilter.DEFAULT_PAGE );

    final String testRedirect = "testRedirect";
    when( filterConfig.getInitParameter( SystemInitStatusFilter.REDIRECT_PAGE_KEY ) ).thenReturn( testRedirect );
    filter.init( filterConfig );
    verify( filter ).setRedirectPath( "/" + testRedirect );
  }

  @Test
  public void testIsEnable() throws Exception {
    doCallRealMethod().when( filter ).isEnable();

    filter.serverStatusProvider = mock( IServerStatusProvider.class );

    when( filter.serverStatusProvider.getStatus() ).thenReturn( IServerStatusProvider.ServerStatus.STARTING );
    assertTrue( filter.isEnable() );
    when( filter.serverStatusProvider.getStatus() ).thenReturn( IServerStatusProvider.ServerStatus.DOWN );
    assertTrue( filter.isEnable() );
    when( filter.serverStatusProvider.getStatus() ).thenReturn( IServerStatusProvider.ServerStatus.STOPPING );
    assertTrue( filter.isEnable() );
    when( filter.serverStatusProvider.getStatus() ).thenReturn( IServerStatusProvider.ServerStatus.ERROR );
    assertTrue( filter.isEnable() );

    when( filter.serverStatusProvider.getStatus() ).thenReturn( IServerStatusProvider.ServerStatus.STARTED );
    assertFalse( filter.isEnable() );
  }

  @Test
  public void testDoFilter() throws Exception {
    doCallRealMethod().when( filter ).doFilter( any( ServletRequest.class ), any( ServletResponse.class ), any( FilterChain.class ) );

    final HttpServletRequest rq = mock( HttpServletRequest.class );
    final FilterChain filterChain = mock( FilterChain.class );

    when( rq.getServletPath() ).thenReturn( "/content" );
    filter.doFilter( rq, mock( ServletResponse.class ), filterChain );
    verify( filter, never() ).isEnable();

    when( rq.getServletPath() ).thenReturn( "/GetResource" );
    when( rq.getQueryString() ).thenReturn( "serverStatus" );
    filter.doFilter( rq, mock( ServletResponse.class ), filterChain );
    verify( filter, never() ).isEnable();

    when( rq.getServletPath() ).thenReturn( "testServletPath" );
    when( rq.getQueryString() ).thenReturn( "testQueryString" );
    filter.doFilter( rq, mock( ServletResponse.class ), filterChain );
    verify( filter ).isEnable();
  }
}
