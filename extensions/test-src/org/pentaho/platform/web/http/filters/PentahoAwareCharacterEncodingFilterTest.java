package org.pentaho.platform.web.http.filters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.mockrunner.mock.web.MockFilterChain;

public class PentahoAwareCharacterEncodingFilterTest {

  private static final String NEW_ENCODING = "win-1252";

  private PentahoAwareCharacterEncodingFilterImpl filter;
  private HttpServletRequest request;
  private MockFilterConfig filterConfig;

  @Before
  public void setUp() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    HttpSession session = req.getSession();
    MockServletContext ctx = (MockServletContext) session.getServletContext();
    ctx.addInitParameter( PentahoAwareCharacterEncodingFilterImpl.INIT_PARAM_ENCODING, NEW_ENCODING );

    request = spy( req );
    doReturn( session ).when( request ).getSession( anyBoolean() );

    filterConfig = new MockFilterConfig();

    filter = new PentahoAwareCharacterEncodingFilterImpl();
  }


  @Test
  public void doFilter_ignoreFlagIsSet() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.TRUE.toString() );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingIsNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_ignoreFlagIsCleared_requestEncodingNotNull() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( NEW_ENCODING );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( NEW_ENCODING, request.getCharacterEncoding() );
  }

  @Test
  public void doFilter_noEncodingIsProvided() throws Exception {
    filterConfig.addInitParameter( "ignore", Boolean.FALSE.toString() );

    request.setCharacterEncoding( null );

    MockServletContext ctx = (MockServletContext) request.getSession().getServletContext();
    ctx.addInitParameter( PentahoAwareCharacterEncodingFilterImpl.INIT_PARAM_ENCODING, "" );

    filter.init( filterConfig );
    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );

    assertEquals( PentahoAwareCharacterEncodingFilterImpl.DEFAULT_CHAR_ENCODING, request.getCharacterEncoding() );
  }
}