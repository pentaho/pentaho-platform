package org.pentaho.test.platform.web.http.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.engine.IConfiguration;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.web.http.security.RequestParameterAuthenticationFilter;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.AuthenticationManager;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.ui.WebAuthenticationDetails;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class RequestParameterAuthenticationFilterTest {

  private RequestParameterAuthenticationFilter filter;

  private AuthenticationManager authManagerMock;

  @Before
  public void beforeTest() throws KettleException, IOException {
    KettleClientEnvironment.init();
    filter = new RequestParameterAuthenticationFilter();
    authManagerMock = mock( AuthenticationManager.class );
    filter.setAuthenticationManager( authManagerMock );
    final Properties properties = new Properties();
    properties.setProperty( "requestParameterAuthenticationEnabled", "true" );
    IConfiguration config = mock( IConfiguration.class );
    ISystemConfig mockISystemConfig = mock( ISystemConfig.class );
    mockISystemConfig.registerConfiguration( config );
    filter.setSystemConfig( mockISystemConfig );
    doReturn( config ).when( mockISystemConfig ).getConfiguration( "security" );
    doReturn( properties ).when( config ).getProperties();
  }

  @Test
  public void userNamePasswordEncrypted() throws IOException, ServletException {
    final MockHttpServletRequest request =
      new MockHttpServletRequest(
        "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans/?rep=dev&userid=admin&password=Encrypted"
          + "%202be98afc86aa7f2e4bb18bd63c99dbdde&trans=/home/admin/Trans1" );
    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( "admin", "password" );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );
    verify( authManagerMock ).authenticate( Mockito.eq( authRequest ) );

  }

  @Test
  public void userNamePasswordUnencrypted() throws IOException, ServletException {
    final MockHttpServletRequest request =
      new MockHttpServletRequest( "GET",
        "http://localhost:9080/pentaho-di/kettle/executeTrans/?rep=dev&userid=admin&password=password&trans=/home"
          + "/admin/Trans1" );
    request.addParameter( "userid", "admin" );
    request.addParameter( "password", "password" );

    filter.doFilter( request, new MockHttpServletResponse(), new MockFilterChain() );
    UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken( "admin", "password" );
    authRequest.setDetails( new WebAuthenticationDetails( request ) );
    verify( authManagerMock ).authenticate( Mockito.eq( authRequest ) );

  }
}
