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

package org.pentaho.platform.web.servlet.matchers;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.web.http.api.resources.RepositoryResource;
import org.pentaho.platform.web.servlet.GenericServlet;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequireJsConfigRequestMatcherTest {

  private RepositoryResource repositoryResource;
  private GenericServlet genericServlet;
  private RequireJsConfigRequestMatcher matcher;
  private HttpServletRequest request;

  @Before
  public void setUp() {
    repositoryResource = mock( RepositoryResource.class );
    genericServlet = mock( GenericServlet.class );
    request = mock( HttpServletRequest.class );

    matcher = new RequireJsConfigRequestMatcher( repositoryResource, genericServlet );
  }

  @Test
  public void testMatches_True_WhenAllConditionsMet_GenericServlet() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/content" );
    when( request.getPathInfo() ).thenReturn( "/reporting/web/require-js-cfg.js" );

    when( genericServlet.isStaticResource( request ) ).thenReturn( true );
    when( repositoryResource.isStaticResource( request ) ).thenReturn( false );

    assertTrue( matcher.matches( request ) );
  }

  @Test
  public void testMatches_True_WhenAllConditionsMet_RepositoryResource() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/reporting/scripts/require-js-bundles-cfg.js" );

    when( genericServlet.isStaticResource( request ) ).thenReturn( false );
    when( repositoryResource.isStaticResource( request ) ).thenReturn( true );

    assertTrue( matcher.matches( request ) );
  }

  @Test
  public void testMatches_False_WhenMethodNotGet() {
    when( request.getMethod() ).thenReturn( "POST" );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/reporting/scripts/require-js-cfg.js" );

    when( genericServlet.isStaticResource( request ) ).thenReturn( true );
    when( repositoryResource.isStaticResource( request ) ).thenReturn( false );

    assertFalse( matcher.matches( request ) );
  }

  @Test
  public void testMatches_False_WhenPathDoesNotMatch() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/reporting/scripts/not-require-js-cfg.txt" );

    when( genericServlet.isStaticResource( request ) ).thenReturn( true );
    when( repositoryResource.isStaticResource( request ) ).thenReturn( false );

    assertFalse( matcher.matches( request ) );
  }

  @Test
  public void testMatches_False_WhenNotStaticResource() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/repos/reporting/scripts/require-js-cfg.js" );

    when( genericServlet.isStaticResource( request ) ).thenReturn( false );
    when( repositoryResource.isStaticResource( request ) ).thenReturn( false );

    assertFalse( matcher.matches( request ) );
  }

  @Test
  public void testIsRequireJsConfigRequest_VariousPatterns_Positive() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/api" );

    assertRequireJsConfigRequest( "/require.js", true );
    assertRequireJsConfigRequest( "/require-js.js", true );
    assertRequireJsConfigRequest( "/require-cfg.js", true );
    assertRequireJsConfigRequest( "/require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/require-js-bundles-cfg.js", true );

    assertRequireJsConfigRequest( "/abc-require.js", true );
    assertRequireJsConfigRequest( "/abc-require-js.js", true );
    assertRequireJsConfigRequest( "/abc-require-cfg.js", true );
    assertRequireJsConfigRequest( "/abc-require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/abc-require-js-bundles-cfg.js", true );

    assertRequireJsConfigRequest( "/plugin/require.js", true );
    assertRequireJsConfigRequest( "/plugin/require-js.js", true );
    assertRequireJsConfigRequest( "/plugin/require-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/require-js-bundles-cfg.js", true );

    assertRequireJsConfigRequest( "/plugin/abc-require.js", true );
    assertRequireJsConfigRequest( "/plugin/abc-require-js.js", true );
    assertRequireJsConfigRequest( "/plugin/abc-require-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/abc-require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/abc-require-js-bundles-cfg.js", true );

    assertRequireJsConfigRequest( "/plugin/reporting/require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/reporting/abc-require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/reporting/resources/scripts/abc-require-js-cfg.js", true );
    assertRequireJsConfigRequest( "/plugin/reporting/çà-require-js-cfg.js", true );
  }

  @Test
  public void testIsRequireJsConfigRequest_VariousPatterns_Negative() {
    when( request.getMethod() ).thenReturn( "GET" );
    when( request.getServletPath() ).thenReturn( "/api" );

    assertRequireJsConfigRequest( "/plugin/abc-require-js-bundles-cfg.json", false );
    assertRequireJsConfigRequest( "/plugin/require-cfg-js.js", false );
    assertRequireJsConfigRequest( "/plugin/reporting/noseparatordashrequire-js-cfg.js", false );
  }

  private void assertRequireJsConfigRequest( String pathInfo, boolean expected ) {
    when( request.getPathInfo() ).thenReturn( pathInfo );
    if ( expected ) {
      assertTrue( matcher.isRequireJsConfigRequest( request ) );
    } else {
      assertFalse( matcher.isRequireJsConfigRequest( request ) );
    }
  }

  @Test
  public void testIsRequireJsConfigRequest_FalseForNonGet() {
    when( request.getMethod() ).thenReturn( "POST" );
    when( request.getServletPath() ).thenReturn( "/api" );
    when( request.getPathInfo() ).thenReturn( "/require-js-cfg.js" );

    assertFalse( matcher.isRequireJsConfigRequest( request ) );
  }
}
