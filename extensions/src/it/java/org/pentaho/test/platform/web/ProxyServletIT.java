/*!
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 *
 * Copyright (c) 2002-2024 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.test.platform.web;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;
import com.mockrunner.mock.web.MockServletConfig;
import org.apache.http.client.utils.URIBuilder;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.web.servlet.ProxyServlet;
import org.pentaho.test.platform.engine.core.BaseTestCase;
import org.pentaho.test.platform.utils.TestResourceLocation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests for <code>org.pentaho.platform.web.servlet.ProxyServlet</code>.
 *
 * @author mlowery
 */
public class ProxyServletIT extends BaseTestCase {
  private static final String SOLUTION_PATH = TestResourceLocation.TEST_RESOURCES + "/web-servlet-solution";

  public String getSolutionPath() {
    return SOLUTION_PATH;
  }

  public void setUp() {
    // BaseTestCase constructor initializes the PentahoSystem.
    if ( PentahoSystem.getInitializedStatus() == PentahoSystem.SYSTEM_INITIALIZED_OK ) {
      PentahoSystem.shutdown();
    }

    PentahoSessionHolder.setSession( getPentahoSession() );

    StandaloneApplicationContext applicationContext =
      new StandaloneApplicationContext( getSolutionPath(), "" );
    PentahoSystem.init( applicationContext, getRequiredListeners() );
  }

  @Override
  protected void tearDown() throws Exception {
    PentahoSystem.shutdown();
    LocaleHelper.setThreadLocaleOverride( null );
    PentahoSessionHolder.setSession( null );
  }

  protected Map<String, String> getRequiredListeners() {
    return new HashMap<String, String>() {
      {
        put( "globalObjects", "globalObjects" );
      }
    };
  }

  public void testServiceWithNoConfig() throws ServletException, IOException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession( session );
    request.setupAddParameter( "ProxyURL", "http://www.pentaho.org" );

    MockHttpServletResponse response = new MockHttpServletResponse();
    ProxyServlet servlet = new ProxyServlet();
    servlet.service( request, response );
  }

  // region init( config )
  public void testServiceInitParameterProxyURL() throws ServletException {

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "http://www.pentaho.org" );

    ProxyServlet servlet = new ProxyServlet();
    servlet.init( config );

    assertEquals( servlet.getProxyURL(), "http://www.pentaho.org" );
  }

  public void testServiceInitParameterErrorURL() throws ServletException {

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ErrorURL", "http://www.pentaho.org" );

    ProxyServlet servlet = new ProxyServlet();
    servlet.init( config );

    assertEquals( servlet.getErrorURL(), "http://www.pentaho.org" );
  }

  public void testServiceInitParameterErrorURLDefault() throws ServletException {

    MockServletConfig config = new MockServletConfig();

    ProxyServlet servlet = new ProxyServlet();
    servlet.init( config );

    assertNull( servlet.getErrorURL() );
  }

  public void testServiceInitParameterLocaleOverrideEnabled() throws ServletException {

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "LocaleOverrideEnabled", "false" );

    ProxyServlet servlet = new ProxyServlet();
    servlet.init( config );

    assertFalse( servlet.isLocaleOverrideEnabled() );
  }

  public void testServiceInitParameterLocaleOverrideEnabledDefault() throws ServletException {

    MockServletConfig config = new MockServletConfig();

    ProxyServlet servlet = new ProxyServlet();
    servlet.init( config );

    assertTrue( servlet.isLocaleOverrideEnabled() );
  }
  // endregion

  // region service

  // IT tests are not in the same package of classes being tested.
  // One way of spying protected methods is to inherit from the class, locally.
  // We're only stubbing the parts where network access would occur.
  private static class TestProxyServlet extends ProxyServlet {
    @Override
    protected void doProxyCore( final URI requestUri, final HttpServletRequest request, final HttpServletResponse response ) {
      // Do not access network.
    }
  }

  public void testServiceTrustUserQueryParameter() throws ServletException, IOException, URISyntaxException {

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setServletPath( "/pentaho" );

    MockHttpSession session = new MockHttpSession();
    request.setSession( session );

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "http://foo.bar" );

    URIBuilder uriBuilder = new URIBuilder( "http://foo.bar/pentaho" );
    uriBuilder.addParameter( "_TRUST_USER_", "system" );
    uriBuilder.addParameter( "_TRUST_LOCALE_OVERRIDE_", "en_PT" );

    TestProxyServlet servlet = spy( new TestProxyServlet() );
    servlet.init( config );

    servlet.service( request, response );

    verify( servlet ).doProxyCore( uriBuilder.build(), request, response );
  }

  public void testServiceTrustLocaleOverrideQueryParameter() throws ServletException, IOException, URISyntaxException {

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setServletPath( "/pentaho" );

    MockHttpSession session = new MockHttpSession();
    request.setSession( session );

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "http://foo.bar" );

    URIBuilder uriBuilder = new URIBuilder( "http://foo.bar/pentaho" );
    uriBuilder.addParameter( "_TRUST_USER_", "system" );
    uriBuilder.addParameter( "_TRUST_LOCALE_OVERRIDE_", "pt_PT" );


    TestProxyServlet servlet = spy( new TestProxyServlet() );
    servlet.init( config );

    LocaleHelper.setThreadLocaleOverride( Locale.forLanguageTag( "pt-PT" ) );

    servlet.service( request, response );

    verify( servlet ).doProxyCore( uriBuilder.build(), request, response );
  }

  public void testServiceNoUserSessionQueryParameter() throws ServletException, IOException, URISyntaxException {

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    request.setServletPath( "/pentaho" );

    MockHttpSession session = new MockHttpSession();
    request.setSession( session );

    PentahoSessionHolder.setSession( new StandaloneSession( "" ) );

    MockServletConfig config = new MockServletConfig();
    config.setInitParameter( "ProxyURL", "http://foo.bar" );

    URIBuilder uriBuilder = new URIBuilder( "http://foo.bar/pentaho" );

    TestProxyServlet servlet = spy( new TestProxyServlet() );
    servlet.init( config );

    servlet.service( request, response );

    verify( servlet ).doProxyCore( uriBuilder.build(), request, response );
  }
  // endregion

}
