package org.pentaho.platform.web.http.filters;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.Callable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ProxyTrustingFilterTest {

  private static final String TRUSTED_IP = "127.0.0.1";
  private static final String UNTRUSTED_IP = "8.8.8.8";

  private ISecurityHelper securityHelper;
  private MockHttpServletRequest request;
  private ProxyTrustingFilter filter;

  @Before
  public void setUp() throws Exception {
    securityHelper = mock( ISecurityHelper.class );
    SecurityHelper.setMockInstance( securityHelper );

    request = new MockHttpServletRequest(  );

    MockFilterConfig cfg = new MockFilterConfig(  );
    cfg.addInitParameter( "TrustedIpAddrs", "1.1.1.1," + TRUSTED_IP );

    filter = new ProxyTrustingFilter();
    filter.init( cfg );
  }

  @After
  public void tearDown() {
    SecurityHelper.setMockInstance( null );
  }


  @Test
  public void doFilterForTrusted() throws Exception {
    request.setRemoteHost( TRUSTED_IP );
    request.addParameter( filter.getParameterName(), "user" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( securityHelper ).runAsUser( anyString(), any( Callable.class ) );
  }

  @Test
  public void doFilterForUntrusted() throws Exception {
    request.setRemoteHost( UNTRUSTED_IP );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    verify( securityHelper, never() ).runAsUser( anyString(), any( Callable.class ) );
  }

}